package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * <pre>
 * Experimental strategy that uses the following:
 * Pram 1: rsi I
 * Pram 2: rsi II
 * Pram 3: DI+ DI- Max Param
 * Pram 4: DI+ DI- Max Limit
 *
 * Buy sell signal is a bit complicated here:
 * 1. First, compute DI+ and DI- using param 3, have the the max number between them, say X.
 *    If X &lt; param 4, then produce NEUTRAL signal.
 *
 * 2. Check RSI, if fast RSI crosses the slow RSI up, it generates BUY, else it is SELL.
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class RsiCrossDiMaxBuySellStrategy extends BuySellStrategy implements IMomentumType {

   private static final long serialVersionUID = -7205975889528454010L;

   public RsiCrossDiMaxBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public RsiCrossDiMaxBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      rsi(0, 1, 2, strategyParams.getStrategyValue(0), gr, plotter);
      rsi(3, 4, 5, strategyParams.getStrategyValue(1), gr, plotter);
      directionalIndicator(6, 7, strategyParams.getStrategyValue(2), lastTick, gr, plotter);
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[2];
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[5];
   }

   protected SignalDirection formulateNewDirection(int currIdx, Tick currentTick, SignalDirection oldDirection) {
      TickIndicatorBar idc = getCurrentBar();
      double diMax = Math.max(idc.indexedIndicators[6], idc.indexedIndicators[7]);
      int diMaxLimit = strategyParams.getStrategyValue(3);

      if (diMax < diMaxLimit) {
         return (strategyRunner.getOpenPosition() != null) ? SignalDirection.CLOSE : SignalDirection.NEUTRAL;
      } else {
         return super.formulateNewDirection(currIdx, currentTick, oldDirection);
      }
   }


   protected int getIndicatorCount() {
      return 8;
   }

}
