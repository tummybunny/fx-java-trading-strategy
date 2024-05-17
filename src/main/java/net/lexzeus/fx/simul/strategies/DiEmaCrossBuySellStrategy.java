package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Experimental strategy with following indicators: Plus Minus DI + Bollinger Band + EMA.
 * <pre>
 * Parameters required:
 * 		0 --> EMA I
 * 		1 --> EMA II
 * 		2 --> DI parameter
 * 		3 --> DI limit (e.g. buy signal when +DI > 20, the limit is 20 in this case)
 *
 * Indicators produced:
 * 		0 --> EMA I
 * 		1 --> EMA II
 * 		2 --> +DI
 * 		3 --> -DI
 *
 * Signals:
 * 		BUY -> EMA crossover produces BUY and +DI &gt; DI Limit
 *      SELL -> EMA crossover produces SELL and -DI &gt; DI Limit
 *      NEUTRAL -> Other than above
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class DiEmaCrossBuySellStrategy extends BuySellStrategy implements IMomentumType {

   public DiEmaCrossBuySellStrategy(StrategyParams p) {
      super(p);
   }

   public DiEmaCrossBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      ema(0, strategyParams.getStrategyValue(0), gr, plotter);
      ema(1, strategyParams.getStrategyValue(1), gr, plotter);
      int diPlusMinusParam = strategyParams.getStrategyValue(2);
      directionalIndicator(2, 3, diPlusMinusParam, lastTick, gr, plotter);
   }

   protected int getIndicatorCount() {
      return 6;
   }

   //	protected Direction formulateNewDirection(int currIdx, Tick currentTick, Direction oldDirection)
   //	{
   //		int diLimit = strategyParam.getStrategyValue(3);
   //		boolean buyDi = allIndicator.get(currIdx).indicator[2] > diLimit;
   //		boolean sellDi = allIndicator.get(currIdx).indicator[3] > diLimit;
   //
   //		Direction diDirection =
   //			(buyDi && sellDi) ? Direction.NEUTRAL :
   //			(buyDi && !sellDi) ? Direction.BUY :
   //			(!buyDi && sellDi) ? Direction.SELL :
   //			Direction.NEUTRAL;
   //
   //		double price = currentTick.getClosePrice();
   //
   //		// calculate if the price is within the upper / lower bollinger bands
   //		Direction bbandDirection = price > allIndicator.get(currIdx).indicator[4] ? Direction.BUY
   //				: price < allIndicator.get(currIdx).indicator[5] ? Direction.SELL : Direction.NEUTRAL;
   //
   //		// outside the bands, check if ema confirms it...
   //		double currFast = allIndicator.get(currIdx).indicator[0];
   //		double currSlow = allIndicator.get(currIdx).indicator[1];
   //
   //		Direction emaDirection = currSlow > currFast ? BuySell.Direction.SELL : BuySell.Direction.BUY;
   //
   //		// di, ema, and bband have to produce same buy sell signal in order to confirm the signal,
   //		// otherwise we return neutral
   //		return (diDirection == bbandDirection) && (diDirection == emaDirection) ? diDirection : Direction.NEUTRAL;
   //	}

   protected SignalDirection formulateNewDirection(int currIdx, Tick currentTick, SignalDirection oldDirection) {
      int diLimit = strategyParams.getStrategyValue(3);
      boolean buyDi = getBarAt(currIdx).indexedIndicators[2] > diLimit;
      boolean sellDi = getBarAt(currIdx).indexedIndicators[3] > diLimit;

      SignalDirection diDirection =
         (buyDi && sellDi) ? SignalDirection.NEUTRAL : (buyDi && !sellDi) ? SignalDirection.BUY : (!buyDi && sellDi) ? SignalDirection.SELL : SignalDirection.NEUTRAL;

      // outside the bands, check if ema confirms it...
      double currFast = getBarAt(currIdx).indexedIndicators[0];
      double currSlow = getBarAt(currIdx).indexedIndicators[1];

      SignalDirection emaDirection =
         currSlow > currFast ? SignalDirection.SELL : currSlow < currFast ? SignalDirection.BUY : SignalDirection.NEUTRAL;

      return emaDirection == diDirection ? emaDirection : SignalDirection.NEUTRAL;
   }


   protected SignalDirection formulateOldDirection(int currIdx, Tick currentTick, SignalDirection newDirection) {
      return newDirection;
   }

   @Override
   protected double getFastIndicatorValue(int idx) {
      // irrelevant
      return 0;
   }

   @Override
   protected double getSlowIndicatorValue(int idx) {
      // irrelevant
      return 0;
   }

}
