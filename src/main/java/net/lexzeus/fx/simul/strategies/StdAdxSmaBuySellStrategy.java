package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * <pre>
 * Experimental strategy that is quite complicated to explain.
 *
 * In a nutshell, it is strategy that uses ADX as a factor to Envelope, and compares it against Bollinger Band.
 * As we know the Bollinger Band will narrow when market consolidates, and expand when volatility
 * increases. When the envelope falls within the Bollinger Band, it generates NEUTRAL signal (market
 * is consolidating, hence the Strategy emits NEUTRAL signal -- or CLOSE signal when we have open
 * position).
 *
 * Parameters required:
 * 		0 --> ADX Divider
 * 		1 --> ADX Bottom
 * 		2 --> ADX Param
 * 		3 --> Std Limit in 0.0001
 * 		4 --> SMA Param
 * 		5 --> Std Deviations
 *
 * Indicators:
 * 		0 --> ADX
 * 		1 --> SMA
 * 		2 --> Std Dev
 *
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class StdAdxSmaBuySellStrategy extends BuySellStrategy implements IMomentumType {

   public StdAdxSmaBuySellStrategy(StrategyParams p) {
      super(p);
   }

   public StdAdxSmaBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      adx(0, strategyParams.getStrategyValue(2), gr, plotter);
      double deviations = ((double) strategyParams.getStrategyValue(5)) / 100d;

      double adx = getCurrentBar().indexedIndicators[0];
      adx = getCurrentBarIndex() + 1 >= strategyParams.getStrategyValue(
         2) * 2 - 2 ? (adx < 2 ? 2 : adx) : strategyParams.getStrategyValue(4);

      double adxBottom = adx - strategyParams.getStrategyValue(1);
      adxBottom = adxBottom < 1 ? 1 : adxBottom;
      double limit = 0.0001d * (double) strategyParams.getStrategyValue(
         3) * adxBottom / (double) strategyParams.getStrategyValue(0);

      stdDev(1, 2, limit, strategyParams.getStrategyValue(4), deviations < 0.1d ? 0.1d : deviations, gr, plotter);
   }

   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics gr, PricePlotter plotter
   ) {
      double price = currentTick.getClosePrice();

      double adx = getBarAt(currIdx).indexedIndicators[0];
      adx = currIdx + 1 >= strategyParams.getStrategyValue(
         2) * 2 - 2 ? (adx < 2 ? 2 : adx) : strategyParams.getStrategyValue(4);

      double adxBottom = adx - strategyParams.getStrategyValue(1);
      adxBottom = adxBottom < 1 ? 1 : adxBottom;
      double limit = 0.0001d * (double) strategyParams.getStrategyValue(
         3) * adxBottom / (double) strategyParams.getStrategyValue(0);

      // compare bollinger band with limit
      boolean tradingInRange = getBarAt(currIdx).indexedIndicators[2] > limit;

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
         double currSlow = getBarAt(currIdx).indexedIndicators[1];

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
