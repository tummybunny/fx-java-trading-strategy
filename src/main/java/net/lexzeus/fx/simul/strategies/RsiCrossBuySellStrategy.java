package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * <pre>
 * Experimental strategy that uses the following:
 * Param 1 - RSI I (fast)
 * Param 2 - RSI II (slow)
 * Param 3 - RSI limit (default 20 - that is when RSI reach 70 (50+20) or 30 (50-30), it emits NEUTRAL / CLOSE signal)
 * Param 4 - SMA period (optional, to detect market consolidation)
 * Param 5 - Envelope (optional, in a ten thousandth, i.e. 20000 = 0.0002, this is to detect market consolidation)
 *
 * Buy sell signal is a bit complicated here:
 * 1. First, if Param 4 and 5 is specified, we plot SMA, and use Param 5 to calculate the envelope:
 *    Upper envelope: SMA + (Param 5 / 10000)
 *    Lower envelope: SMA - (Param 5 / 10000)
 *
 *    At anytime, when price fall within the envelope range, then NEUTRAL signal will be produced,
 *    and RED horizontal line is plotted somewhere at the bottom of the chart, otherwise
 *    it is GREEN line.
 *    This is to avoid trading when there is market consolidation.
 *
 * 2. When (1) does not produce NEUTRAL, next we check RSI. If it is &gt upper limit or &lt lower limit,
 *    NEUTRAL signal will be produced.
 *
 * 3. Check RSI, if fast RSI crosses the slow RSI up, it generates BUY, else it is SELL.
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class RsiCrossBuySellStrategy extends BuySellStrategy implements IMomentumType {

   private static final long serialVersionUID = -7205975889528454010L;

   public RsiCrossBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public RsiCrossBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      rsi(0, 1, 2, strategyParams.getStrategyValue(0), gr, plotter);
      rsi(3, 4, 5, strategyParams.getStrategyValue(1), gr, plotter);

      if (strategyParams.getStrategyValue(3) > 0) {
         sma(6, strategyParams.getStrategyValue(3), gr, plotter);
      }
   }

   @Override
   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics gr, PricePlotter plotter
   ) {
      double deviationLimit = strategyParams.getStrategyValue(4);
      if (deviationLimit > 0) {
         // this is technique to predict trading in range ...
         double sma = getCurrentBar().indexedIndicators[6];
         double price = getPrice(getCurrentBarIndex());
         double deviation = Math.abs(price - sma);
         boolean tradingInRange = (deviationLimit / 10000) > deviation;

         if (gr != null) {
            if (tradingInRange) {
               gr.setColor(Color.RED);
            } else {
               gr.setColor(Color.GREEN);
            }
            Point pt = plotter.plotScaleAt(1, currentTick.getTimestamp());
            gr.drawLine(pt.x, pt.y, pt.x, pt.y);
         }

         if (tradingInRange) {
            // trading in range is detected ...
            return SignalDirection.NEUTRAL;
         }
      }

      int limit = strategyParams.getStrategyValue(2); // return -1 if not specified
      limit = limit < 0 ? 20 : limit;               // if -1, return 20 which is the default..

      double rsi = getCurrentBar().indexedIndicators[2];
      if (rsi > (50 + limit) || rsi < (50 - limit)) {
         return strategyRunner.getOpenPosition() == null ? SignalDirection.NEUTRAL : SignalDirection.CLOSE;
      }
      return super.formulateNewDirection(currIdx, currentTick, oldDirection);
   }

   @Override
   protected int getEffectiveMarketDataStartingPosition() {
      return 5;
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[2];
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[5];
   }

   protected int getIndicatorCount() {
      return (strategyParams.getStrategyValue(3) > 0) ? 7 : 6;
   }

}
