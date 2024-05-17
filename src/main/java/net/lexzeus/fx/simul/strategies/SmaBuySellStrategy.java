package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * One of my favorite strategy, it is a trend trading strategy, basically you use Envelope to find out whether the
 * market is in consolidation and avoid trading. When it is not, trade on the direction pointed out by SMA.
 * <pre>
 * Parameter:
 * 1. SMA
 * 2. Envelope (optional, in a ten thousandth, i.e. 20000 means 0.0002).
 *
 * To calculate the upper and lower envelopes, basically it calculates the SMA based on param 1,
 * and add/substract the SMA with param 2 / 10000.
 *
 * Upper envelope = SMA[Param 1] + (Envelope[Param 2] / 1000)
 * Lower envelope = SMA[Param 1] - (Envelope[Param 2] / 1000)
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class SmaBuySellStrategy extends BuySellStrategy {

   private static final long serialVersionUID = 6616892341909973251L;

   public SmaBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public SmaBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      sma(0, strategyParams.getStrategyValue(0), gr, plotter);
   }

   @Override
   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics gr, PricePlotter plotter
   ) {
      double deviationLimit = strategyParams.getStrategyValue(1);
      if (deviationLimit > 0) {
         // this is technique to predict trading in range ...
         double sma = getCurrentBar().indexedIndicators[0];
         double price = getPrice(getCurrentBarIndex());
         double deviation = Math.abs(price - sma);
         double envelope = deviationLimit / 10000d;
         boolean tradingInRange = envelope > deviation;

         if (gr != null) {
            if (currIdx > 0) {
               drawLine(getPreviousBar().indexedIndicators[0] + envelope, getPreviousTick().getTimestamp(),
                        sma + envelope, getCurrentTick().getTimestamp(), COLORS[0], gr, plotter
               );
               drawLine(getPreviousBar().indexedIndicators[0] - envelope, getPreviousTick().getTimestamp(),
                        sma - envelope, getCurrentTick().getTimestamp(), COLORS[0], gr, plotter
               );
            }

            // draw red dot when market is trading in range, or green
            // when it is not...
            if (tradingInRange) {
               gr.setColor(Color.RED);
            } else {
               gr.setColor(Color.GREEN);
            }
            Point pt = plotter.plotScaleAt(5, currentTick.getTimestamp());
            gr.drawLine(pt.x, pt.y, pt.x, pt.y);
         }

         if (tradingInRange) {
            // trading in range is detected ...
            return SignalDirection.NEUTRAL;
         }
      }

      return super.formulateNewDirection(currIdx, currentTick, oldDirection, gr, plotter);
   }

   @Override
   protected int getEffectiveMarketDataStartingPosition() {
      return strategyParams.getStrategyValue(0);
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return getPrice(idx);
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[0];
   }

   @Override
   protected int getIndicatorCount() {
      return 1;
   }

}
