package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;
import java.util.Date;

import net.lexzeus.fx.simul.MarketDataSource;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;

/**
 * The default strategy, which generates no signal and require no parameter.
 *
 * @author Alexander Koentjara
 */
public class NoBuySellStrategy extends BuySellStrategy {

   private static final long serialVersionUID = -8900057544422598405L;

   public NoBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public void marketMove(MarketDataSource source, Tick lastTick) {
   }

   public synchronized void marketMove(MarketDataSource source, Tick lastTick, Graphics gr, PricePlotter plotter) {

   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return 0;
   }

   @Override
   protected int getIndicatorCount() {
      return 0;
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return 0;
   }

   public void plotToGraphic(Date startPeriod, Date endPeriod, Graphics graphic, PricePlotter plotter) {
      this.strategyRunner.reset(this, strategyParams);
   }

}
