package net.lexzeus.fx.simul.strategytest;

import java.text.DecimalFormat;

import net.lexzeus.fx.simul.MarketDataSource;
import net.lexzeus.fx.simul.Util;
import net.lexzeus.fx.simul.strategies.BuySellStrategy;
import net.lexzeus.fx.simul.strategies.ITradePositionListener;
import net.lexzeus.fx.simul.strategies.TradePosition;


public class TestListener implements ITradePositionListener {

   private double totalProfit = 0;
   DecimalFormat df = new DecimalFormat("0.0000");

   public TestListener() {
   }

   public void openPosition(
      BuySellStrategy source, MarketDataSource data, TradePosition newPosition, double cutLoss, double takeProfit
   ) {
      //System.out.println(newPosition);
   }

   long prevDay = 0;
   double lastProfit = 0d;
   double ctr = 0;

   public void closePosition(
      BuySellStrategy source, MarketDataSource data, TradePosition prevPosition, TradePosition reversalPosition,
      double profit
   ) {
      //profit-=0.00025d; // bid ask spread

      totalProfit += profit;
      //System.out.println(reversalPosition+","+Util.formatPrice(profit)+","+Util.formatPrice(totalProfit));

      long day = reversalPosition.getTick().getTimestamp().getTime();
      long divider = 1000 * 60 * 60 * 24;

      long curDay = (long) ((double) day / (double) divider);
      if (curDay != prevDay) {
         ctr++;
         double todayProfit = totalProfit - lastProfit;
         double average = totalProfit / ctr;
         System.out.println("[DAY #" + curDay + " " + Util.formatDate(
            reversalPosition.getTick().getTimestamp()) + "] Today Profit " + df.format(
            todayProfit) + " of Total " + df.format(totalProfit) + " Average " + df.format(average));
         lastProfit = totalProfit;
      }
      prevDay = curDay;
   }

   public double getProfit() {
      return totalProfit;
   }

}
