package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;

/**
 * Strategy that uses Bollinger Band to determine whether market is at consolidation (if so, it emits Neutral signal)
 * and EMA to find the trend direction.
 * <p>
 * Parameters:
 * <li>EMA
 * <li>Bollinger Band period
 * <li>Bollinger Band std deviation (in a hundreth, i.e. 200 means 2).
 *
 * @author Alexander Koentjara
 */
public class BollingerEmaBuySellStrategy extends BuySellStrategy {

   public BollingerEmaBuySellStrategy(StrategyParams p) {
      super(p);
   }

   public BollingerEmaBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   protected int getIndicatorCount() {
      return 4;
   }

   protected int getEffectiveMarketDataStartingPosition() {
      return Math.max(strategyParams.getStrategyValue(0), strategyParams.getStrategyValue(1));
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return getCurrentTick().getClosePrice(); //price
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[0]; // EMA
   }


   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      ema(0, strategyParams.getStrategyValue(0), gr, plotter);
      double deviations = ((double) strategyParams.getStrategyValue(2)) / 100d;
      bollinger(1, 2, 3, strategyParams.getStrategyValue(1), deviations < 0.1d ? 0.1d : deviations, gr, plotter);
   }

   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics graphics, PricePlotter plotter
   ) {
      double price = currentTick.getClosePrice();

      boolean isTradingInRange = price <= getBarAt(currIdx).indexedIndicators[2] && price >= getBarAt(
         currIdx).indexedIndicators[3];

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
         // use default strategy: price and EMA cross over
         return super.formulateNewDirection(currIdx, currentTick, oldDirection);
      }
   }

}
