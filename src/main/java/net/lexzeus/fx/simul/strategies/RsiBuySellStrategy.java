package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Range trading strategy II: BUY when RSI touches lower limit and SELL when rsi touches upper limit, reach the RSI
 * limit (e.g. 80 or 20). Param 1 - RSI Param 2 - RSI limit (default 30 relative to 50, that is when RSI reach 80
 * (50+30) or 20 (50-30)
 *
 * @author Alexander Koentjara
 */
public class RsiBuySellStrategy extends BuySellStrategy implements IMomentumType {

   private static final long serialVersionUID = -7205975889528454010L;

   public RsiBuySellStrategy(StrategyParams param) {
      super(param);
   }

   public RsiBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   @Override
   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      rsi(0, 1, 2, strategyParams.getStrategyValue(0), gr, plotter);
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[2];
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      return getBarAt(idx).indexedIndicators[2];
   }

   protected int getIndicatorCount() {
      return 3;
   }

   @Override
   protected SignalDirection formulateNewDirection(int currIdx, Tick currentTick, SignalDirection oldDirection) {
      int limit = strategyParams.getStrategyValue(1); // return -1 if not specified
      limit = limit < 0 ? 20 : limit;               // if -1, return 20 which is the default..

      double prevRsi = getBarAt(currIdx - 1).indexedIndicators[2];
      double curRsi = getBarAt(currIdx).indexedIndicators[2];
      if (prevRsi < curRsi) {
         //going up
         if (curRsi >= 50 && prevRsi < 50) {
            // cross 0
            return SignalDirection.BUY;
         }
      } else {
         //going down
         if (curRsi <= 50 && prevRsi > 50) {
            // cross 0
            return SignalDirection.SELL;

         }
      }

      if ((curRsi >= (50 + limit) || curRsi <= (50 - limit)) && strategyRunner.getOpenPosition() != null) {
         return SignalDirection.CLOSE;
      }

      return SignalDirection.NEUTRAL;
   }

   @Override
   protected int getEffectiveMarketDataStartingPosition() {
      return strategyParams.getStrategyValue(0);
   }

}
