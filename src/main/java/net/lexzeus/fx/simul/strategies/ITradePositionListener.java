package net.lexzeus.fx.simul.strategies;

import net.lexzeus.fx.simul.MarketDataSource;

/**
 * Contract for implementing class that is interested in consuming trade position events, e.g. to convert such events as
 * market or entry order, or simply to calculate total profit and loss.
 *
 * @author Alexander Koentjara
 */
public interface ITradePositionListener {

   void openPosition(
      BuySellStrategy source, MarketDataSource data, TradePosition newPosition, double cutLoss, double takeProfit
   );

   void closePosition(
      BuySellStrategy source, MarketDataSource data, TradePosition prevPosition, TradePosition reversalPosition,
      double profit
   );

}
