package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;

/**
 * Simple strategy that uses SMA crossover.
 * <pre>
 * Parameter:
 * 1. SMA I
 * 2. SMA II
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class SmaCrossBuySellStrategy extends BuySellStrategy {

   private static final long serialVersionUID = -654393004196023165L;

   public SmaCrossBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public SmaCrossBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      sma(0, strategyParams.getStrategyValue(0), gr, plotter);
      sma(1, strategyParams.getStrategyValue(1), gr, plotter);
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
