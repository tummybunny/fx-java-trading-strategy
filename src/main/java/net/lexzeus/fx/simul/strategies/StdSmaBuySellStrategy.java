package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * <pre>
 * Experimental strategy.
 *
 * In a nutshell, it is strategy that compare Bollinger Band with Envelope. When Bollinger Band is within
 * the envelope, it generates NEUTRAL signal (market is consolidating, hence the Strategy emits NEUTRAL
 * signal -- or CLOSE signal when we have open position).
 *
 * Parameters required:
 * 		0 --> Std Limit in 0.0001
 * 		1 --> SMA
 * 		2 --> Std Deviations
 *
 * @author Alexander Koentjara
 */
public class StdSmaBuySellStrategy extends BuySellStrategy implements IMomentumType {

   public StdSmaBuySellStrategy(StrategyParams p) {
      super(p);
   }

   public StdSmaBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      double deviations = ((double) strategyParams.getStrategyValue(2)) / 100d;
      stdDev(0, 1, (double) strategyParams.getStrategyValue(0) * 0.0001d, strategyParams.getStrategyValue(1),
             deviations < 0.1d ? 0.1d : deviations, gr, plotter
      );
   }

   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics gr, PricePlotter plotter
   ) {
      double price = currentTick.getClosePrice();

      double limit = 0.0001d * strategyParams.getStrategyValue(0);

      // calculate if the price is within the upper / lower bollinger bands
      boolean tradingInRange = getBarAt(currIdx).indexedIndicators[1] < limit;

      if (gr != null) {
         if (tradingInRange) {
            gr.setColor(Color.RED);
         } else {
            gr.setColor(Color.GREEN);
         }
         Point pt = plotter.plotScaleAt(5, currentTick.getTimestamp());
         gr.drawLine(pt.x, pt.y, pt.x, pt.y);
      }

      if (tradingInRange && strategyRunner.getOpenPosition() == null) {
         // within the bands
         return SignalDirection.NEUTRAL;
      } else if (tradingInRange && strategyRunner.getOpenPosition() != null) {
         // close position .. trading in range has begun
         return SignalDirection.CLOSE;
      } else {
         // outside the bands, check if sma confirms it...
         double currFast = price;
         double currSlow = getBarAt(currIdx).indexedIndicators[0];

         return currSlow > currFast ? SignalDirection.SELL : currSlow < currFast ? SignalDirection.BUY : SignalDirection.NEUTRAL;
      }
   }

   protected int getIndicatorCount() {
      return 3;
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      // irrelevant
      return 0;
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      // irrelevant
      return 0;
   }

}
