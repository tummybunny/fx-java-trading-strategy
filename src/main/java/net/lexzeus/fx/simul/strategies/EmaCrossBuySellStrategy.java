package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;

/**
 * Strategy that uses two EMAs cross over to generate BUY/SELL signal.
 * <p>
 * Parameters:
 * <li>EMA I
 * <li>EMA II
 *
 * @author Alexander Koentjara
 */
public class EmaCrossBuySellStrategy extends BuySellStrategy {

   private static final long serialVersionUID = -7205975889528454010L;

   public EmaCrossBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public EmaCrossBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      ema(0, strategyParams.getStrategyValue(0), gr, plotter);
      ema(1, strategyParams.getStrategyValue(1), gr, plotter);
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[0];
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[1];
   }

   protected int getIndicatorCount() {
      return 2;
   }

}
