package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Experimental strategy with following indicators: Plus & Minus DI + Bollinger Band + EMA.
 * <pre>
 * Parameters required:
 * 		0 --> EMA I
 * 		1 --> EMA II
 * 		2 --> DI parameter
 * 		3 --> DI limit (e.g. buy signal when +DI > 20, the limit is 20 in this case)
 * 		4 --> Bollinger band parameter (usually is 14)
 * 		5 --> Bollinger band deviations (in hundreds -- 250 means 2.5. Optional, default is 2)
 *
 * Indicators produced:
 * 		0 --> EMA I
 * 		1 --> EMA II
 * 		2 --> Upper Bollinger band
 * 		3 --> Lower Bollinger band
 * 		4 --> +DI
 * 		5 --> -DI
 *
 * Signals:
 * 		BUY -> EMA crossover produces BUY, +DI &gt; DI Limit, and Price breaches upper Bollinger Band limit
 *      SELL -> EMA crossover produces SELL, -DI &gt; DI Limit, and Price breaches lower Bollinger Band limit
 *      NEUTRAL -> Other than above
 * </pre>
 *
 * @author Alexander Koentjara
 */
public class DiBollingerEmaCrossBuySellStrategy extends BuySellStrategy implements IMomentumType {

   public DiBollingerEmaCrossBuySellStrategy(StrategyParams p) {
      super(p);
   }

   public DiBollingerEmaCrossBuySellStrategy(StrategyParams p, ISignalDirectionConsumer r) {
      super(p, r);
   }

   protected void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter) {
      ema(0, strategyParams.getStrategyValue(0), gr, plotter);
      ema(1, strategyParams.getStrategyValue(1), gr, plotter);
      int diPlusMinusParam = strategyParams.getStrategyValue(2);
      directionalIndicator(2, 3, diPlusMinusParam, lastTick, gr, plotter);
      double deviations = ((double) strategyParams.getStrategyValue(5)) / 100d;
      bollinger(4, 5, 6, strategyParams.getStrategyValue(4), deviations < 0.1d ? 0.1d : deviations, gr, plotter);
   }

   protected int getIndicatorCount() {
      return 7;
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

      double price = currentTick.getClosePrice();

      // calculate if the price is within the upper / lower bollinger bands
      SignalDirection bbandDirection = buyDi && price > getBarAt(
         currIdx).indexedIndicators[5] ? SignalDirection.BUY : sellDi && price < getBarAt(
         currIdx).indexedIndicators[6] ? SignalDirection.SELL : SignalDirection.NEUTRAL;

      // outside the bands, check if ema confirms it...
      double currFast = getBarAt(currIdx).indexedIndicators[0];
      double currSlow = getBarAt(currIdx).indexedIndicators[1];

      SignalDirection emaDirection = currSlow > currFast ? SignalDirection.SELL : SignalDirection.BUY;

      // di, ema, and bband have to produce same buy sell signal in order to confirm the signal,
      // otherwise we return neutral
      return bbandDirection == emaDirection ? bbandDirection : SignalDirection.NEUTRAL;
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
