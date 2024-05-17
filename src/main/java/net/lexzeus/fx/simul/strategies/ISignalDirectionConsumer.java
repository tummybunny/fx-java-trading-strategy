package net.lexzeus.fx.simul.strategies;

import java.awt.Graphics;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * An contract for a class that consume buy sell signal directions produced BuySellStrategy class. This contract can be
 * used to implement the logic to open/close/reverse trade positions.
 *
 * @author Alexander Koentjara
 */
public interface ISignalDirectionConsumer {

   /**
    * Called before the BuySellStrategy runs
    *
    * @param strategy
    * @param strategyParams
    */
   void reset(BuySellStrategy strategy, StrategyParams strategyParams);

   /**
    * Retrieve the open position, if any
    *
    * @return
    */
   TradePosition getOpenPosition();

   /**
    * Callback called before BuySellStrategy formulate the next direction
    *
    * @param currIdx
    * @param currentTick
    * @param newDirection
    * @param oldDirection
    */
   void preProcess(int currIdx, Tick currentTick, SignalDirection newDirection, SignalDirection oldDirection);

   /**
    * Callback called when BuySellStrategy emit Neutral direction.
    *
    * @param currIdx
    * @param currentTick
    * @param newDirection
    * @param oldDirection
    */
   void onNeutralDirection(int currIdx, Tick currentTick, SignalDirection newDirection, SignalDirection oldDirection);

   /**
    * Callback called when BuySellStrategy emit direction other than neutral, and it is different compared to the
    * previous direction. E.g. changes from Buy to Sell (vice versa), Neutral to Buy/Sell, Neutral to Close, and
    * Buy/Sell to Close (vice versa).
    *
    * @param currIdx
    * @param currentTick
    * @param newDirection
    * @param oldDirection
    */
   void onBuySellIndicatorChanges(
      int currIdx, Tick currentTick, SignalDirection newDirection, SignalDirection oldDirection
   );

   /**
    * When the new direction is the same as previous direction
    *
    * @param currIdx
    * @param currentTick
    * @param direction
    */
   void onBuySellIndicatorRemains(int currIdx, Tick currentTick, SignalDirection direction);

   /**
    * Set the graphic and plotter (if used in charting application)
    *
    * @param graphic
    * @param plotter
    */
   void setGui(Graphics graphic, PricePlotter plotter);

   /**
    * Called when the graphical resources should be released.
    *
    * @see #setGui(Graphics, PricePlotter)
    */
   void releaseGui();

   /**
    * Called when Buy Sell signal resulting in opening position
    *
    * @param position
    */
   void openPosition(TradePosition position);

   /**
    * Called when Buy Sell signal resulting in closing position
    *
    * @param prevPosition
    * @param closure
    */
   double closePosition(TradePosition prevPosition, TradePosition closure);

}
