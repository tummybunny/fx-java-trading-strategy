package net.lexzeus.fx.simul.strategies;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;
import net.lexzeus.fx.simul.Util;
import net.lexzeus.util.Log;

import java.awt.*;
import java.text.DecimalFormat;


/**
 * Default implementation of ISignalDirectionConsumer
 *
 * @author Alexander Koentjara
 * @see ISignalDirectionConsumer
 */
public class DefaultAlgorithmicTradeExecutor implements ISignalDirectionConsumer {

   protected volatile TradePosition lastOpenPosition;
   protected volatile boolean skipCurrentTick = false;
   protected volatile double prevProfit = 0d;
   protected StrategyParams strategyParams;
   protected double totalProfit;
   protected BuySellStrategy strategy;
   private PricePlotter pricePlotter;
   private Graphics graphics;
   private final Log log = Log.get(this);

   public DefaultAlgorithmicTradeExecutor() {
   }

   public void reset(BuySellStrategy strategy, StrategyParams param) {
      this.strategyParams = param;
      this.strategy = strategy;
      this.lastOpenPosition = null;
      this.skipCurrentTick = false;
      this.prevProfit = 0d;
      this.totalProfit = 0d;
      this.pricePlotter = null;
      this.graphics = null;
   }

   public void preProcess(int idx, Tick currentTick, SignalDirection newDirection, SignalDirection oldDirection) {
      skipCurrentTick = false;

      if (lastOpenPosition != null) {
         double profit = Util.calcProfit(lastOpenPosition, currentTick);

         if (strategyParams.isCutLossExist() && profit < 0) {
            if (Math.abs(profit) > strategyParams.getCutLoss()) {
               // cut loss immediately
               log.info("Unwind the losses: " + new DecimalFormat("0.0000").format(profit));

               TradePosition closure = new TradePosition(
                  lastOpenPosition.getDirection() == SignalDirection.BUY ? SignalDirection.SELL : SignalDirection.BUY,
                  SignalType.CLOSE, currentTick
               );

               // position is still open, need to be closed
               closePosition(lastOpenPosition, closure);

               skipCurrentTick = true;
            } else {
               // bear the loss up to certain tresshold
               log.info("Try to endure the losses: " + new DecimalFormat("0.0000").format(
                  profit) + " at tick " + currentTick);
               skipCurrentTick = true;
            }
         } else if (strategyParams.isTakeProfitExist() && profit > 0 && Math.abs(profit) > Math.abs(
            strategyParams.getTakeProfit())) {
            TradePosition closure = new TradePosition(
               lastOpenPosition.getDirection() == SignalDirection.BUY ? SignalDirection.SELL : SignalDirection.BUY,
               SignalType.CLOSE, currentTick
            );
            closePosition(lastOpenPosition, closure);
            log.info("Take profit at: " + new DecimalFormat("0.0000").format(profit));

            skipCurrentTick = true;
         } else if (strategyParams.isDynamicCutLossExist()) {
            if (profit > 0 && prevProfit >= 0 && profit > prevProfit + strategyParams.getDynamicCutLoss()) {
               prevProfit = profit - strategyParams.getDynamicCutLoss();
               if (prevProfit < 0) {
                  prevProfit = 0d;
               }
            } else if (profit > 0 && prevProfit > profit) {
               // close position ...
               TradePosition closure = new TradePosition(
                  lastOpenPosition.getDirection() == SignalDirection.BUY ? SignalDirection.SELL : SignalDirection.BUY,
                  SignalType.CLOSE, currentTick
               );
               closePosition(lastOpenPosition, closure);
               log.info("Dynamic cut losses after profit: " + new DecimalFormat("0.0000").format(profit));
               skipCurrentTick = true;
            }
         }
      }
   }

   public void onNeutralDirection(
      int currIdx, Tick currentTick, SignalDirection newDirection, SignalDirection oldDirection
   ) {
      // ignore .. what should I do?
      log.info("Indication: " + newDirection + " on " + currentTick);
   }

   public void onBuySellIndicatorChanges(
      int idx, Tick currentTick, SignalDirection newDirection, SignalDirection oldDirection
   ) {
      if (newDirection == SignalDirection.CLOSE) {
         if (lastOpenPosition != null) {
            TradePosition closure = new TradePosition(
               lastOpenPosition.getDirection() == SignalDirection.BUY ? SignalDirection.SELL : SignalDirection.BUY,
               SignalType.CLOSE, currentTick
            );

            // position is still open, need to be closed
            closePosition(lastOpenPosition, closure);
         }
         skipCurrentTick = true;
      }

      if (!skipCurrentTick) {
         if (lastOpenPosition != null) {
            if (lastOpenPosition.getDirection() != newDirection) {
               // close position, the signal has reversed

               TradePosition closure = new TradePosition(
                  lastOpenPosition.getDirection() == SignalDirection.BUY ? SignalDirection.SELL : SignalDirection.BUY,
                  SignalType.CLOSE, currentTick
               );

               // position is still open, need to be closed
               closePosition(lastOpenPosition, closure);
            } else {
               // remain open position ...
               return;
            }
         }

         // create new position...
         lastOpenPosition = new TradePosition(newDirection, SignalType.OPEN, currentTick);
         openPosition(lastOpenPosition);
      }
   }

   public void onBuySellIndicatorRemains(int currIdx, Tick currentTick, SignalDirection direction) {
   }

   public TradePosition getOpenPosition() {
      return lastOpenPosition;
   }

   public void setGui(Graphics graphic, PricePlotter plotter) {
      this.graphics = graphic;
      this.pricePlotter = plotter;
   }

   public void releaseGui() {
      graphics = null;
      pricePlotter = null;
   }

   public void openPosition(TradePosition position) {
      strategy.broadcastOpenPosition(position);

      if (graphics != null) {
         Point pt = pricePlotter.plotPriceAt(position.getTick().getClosePrice(), position.getTick().getTimestamp());
         graphics.setColor(position.getDirection() == SignalDirection.BUY ? Color.GREEN : Color.RED);
         graphics.fillOval(pt.x - 2, pt.y - 2, 5, 5);
      }
   }

   public double closePosition(TradePosition prevPosition, TradePosition closure) {
      prevProfit = 0d;

      double profit = 0;
      if (prevPosition.getDirection() == SignalDirection.BUY) {
         //long the security
         profit = closure.getTick().getSellPrice() - prevPosition.getTick().getBuyPrice();
      } else {
         //short the security
         profit = prevPosition.getTick().getSellPrice() - closure.getTick().getBuyPrice();
      }

      strategy.broadcastClosePosition(prevPosition, closure, profit);
      totalProfit += profit;

      if (graphics != null) {
         Point pt1 = pricePlotter.plotPriceAt(closure.getTick().getClosePrice(), closure.getTick().getTimestamp());
         Point pt2 = pricePlotter.plotPriceAt(
            prevPosition.getTick().getClosePrice(), prevPosition.getTick().getTimestamp());

         graphics.setColor(profit >= 0 ? Color.GREEN : Color.RED);
         graphics.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
         graphics.drawLine(pt1.x + 1, pt1.y, pt2.x + 1, pt2.y);
      }

      lastOpenPosition = null;
      return profit;
   }

}
