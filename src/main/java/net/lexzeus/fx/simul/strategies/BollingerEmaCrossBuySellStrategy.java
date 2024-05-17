package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;

/**
 * Strategy that uses Bollinger Band to determine whether market is at consolidation (if so, it emits Neutral signal)
 * and EMA cross over to find the trend direction.
 * <p>
 * Parameters:
 * <li>EMA I
 * <li>EMA II
 * <li>Bollinger Band period
 * <li>Bollinger Band std deviation (in a hundreth, i.e. 200 means 2).
 *
 * @author Alexander Koentjara
 */
public class BollingerEmaCrossBuySellStrategy extends BuySellStrategy {

   public BollingerEmaCrossBuySellStrategy(StrategyParams p) {
      super(p);
   }

   public BollingerEmaCrossBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   protected int getIndicatorCount() {
      return 5;
   }

   @Override
   protected int getEffectiveMarketDataStartingPosition() {
      return Math.max(strategyParams.getStrategyValue(1), strategyParams.getStrategyValue(2));
   }

   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      ema(0, strategyParams.getStrategyValue(0), gr, plotter); // first indicator is for EMA I

      ema(1, strategyParams.getStrategyValue(1), gr, plotter); // second indicator is for EMA II

      double deviations = ((double) strategyParams.getStrategyValue(3)) / 100d;
      bollinger(2,       // SMA indicator for Bollinger Band
                3,       // Upper Band
                4,       // Lower Band
                strategyParams.getStrategyValue(2), deviations < 0.1d ? 0.1d : deviations, gr, plotter
      );
   }


   @Override
   protected double getFastIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[0]; // EMA I
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[1]; // EMA II
   }

   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics graphics, PricePlotter plotter
   ) {
      double price = currentTick.getClosePrice();

      boolean isTradingInRange = price <= getBarAt(currIdx).indexedIndicators[3] && price >= getBarAt(
         currIdx).indexedIndicators[4];

      if (graphics != null) {
         // draw red dot when market is trading in range, or green
         // when it is not...
         if (isTradingInRange) {
            graphics.setColor(Color.RED);
         } else {
            graphics.setColor(Color.GREEN);
         }
         Point pt = plotter.plotScaleAt(5, currentTick.getTimestamp());
         graphics.drawLine(pt.x, pt.y, pt.x, pt.y);
      }

      if (isTradingInRange) {
         // within the upper and lower bands, emit neutral signal
         return SignalDirection.NEUTRAL;
      } else {
         // use default strategy: EMA I and II cross over
         return super.formulateNewDirection(currIdx, currentTick, oldDirection);
      }
   }

}
