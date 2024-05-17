package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Pram 1: rsi I Pram 2: rsi II Pram 3: ADX Pram 4: ADX Limit
 *
 * @author Alexander Koentjara
 */
public class RsiCrossAdxBuySellStrategy extends BuySellStrategy implements IMomentumType {

   private static final long serialVersionUID = -7205975889528454010L;

   public RsiCrossAdxBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public RsiCrossAdxBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      rsi(0, 1, 2, strategyParams.getStrategyValue(0), gr, plotter);
      rsi(3, 4, 5, strategyParams.getStrategyValue(1), gr, plotter);
      adx(6, strategyParams.getStrategyValue(2), gr, plotter);
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
      double adx = getCurrentBar().indexedIndicators[6];
      int adxLimit = strategyParams.getStrategyValue(3);

      if (adx < adxLimit) {
         return SignalDirection.NEUTRAL;
      } else {
         return super.formulateNewDirection(currIdx, currentTick, oldDirection);
      }
   }


   protected int getIndicatorCount() {
      return 7;
   }

}
