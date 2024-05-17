package net.lexzeus.fx.simul.strategytest;

import java.text.DecimalFormat;

import net.lexzeus.fx.simul.MarketDataSource;
import net.lexzeus.fx.simul.MarketDataSource.ChartIntervalType;
import net.lexzeus.fx.simul.strategies.BuySellStrategy;
import net.lexzeus.fx.simul.strategies.SmaBuySellStrategy;
import net.lexzeus.fx.simul.strategies.StrategyParams;

/**
 * For testing a strategy
 *
 * @author Alexander Koentjara
 */
public class LaunchMe {

   public static final DecimalFormat df = new DecimalFormat("0.0000");

   public static final StrategyParams SMA_STRATEGY = new StrategyParams(
      new int[] {20, 10}, 0.0000d, 0.0000d, 0.0000d
   );


   public LaunchMe() {
   }

   static BuySellStrategy bsell = null;
   static TestListener listener = null;

   private double getProfit(String file, StrategyParams param, boolean continuous) {
      MarketDataSource md = MarketDataSource.fromFile(file);

      boolean simple = true;

      boolean created = bsell != null;
      bsell = continuous & created ? bsell : new SmaBuySellStrategy(param);
      if (!continuous || !created) {
         bsell.setInterval(ChartIntervalType.MINUTE, 10);
         bsell.initialize();
      }

      if (!continuous || listener == null) {
         listener = new TestListener();
         bsell.addBuySellListener(listener);
      }

      MarketDataSource newMd = new MarketDataSource();
      newMd.addMarketMoveListener(bsell);

      MarketDataEmulator emul = new MarketDataEmulator(md, newMd, simple);
      emul.start();
      emul.waitTillfinished();

      System.out.println(file + ": " + df.format(listener.getProfit()));

      return listener.getProfit();
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      System.out.println("START TEST...");

      final StrategyParams p = SMA_STRATEGY;

      Object[][] all =
         new Object[][]{new Object[]{"marketDataSamples\\eur_usd_2710_10s.csv", p}, new Object[]{"marketDataSamples\\eur_usd_2810_10s.csv", p}, new Object[]{"marketDataSamples\\eur_usd_2910_10s.csv", p}, new Object[]{"marketDataSamples\\eur_usd_3010_10s.csv", p}};


      double total = 0d;
      for (Object[] o : all) {
         total += new LaunchMe().getProfit((String) o[0], (StrategyParams) o[1], true);
      }

      System.out.println("TOTAL PROFIT: " + df.format(total));
   }

}
