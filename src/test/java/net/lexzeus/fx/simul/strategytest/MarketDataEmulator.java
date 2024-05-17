package net.lexzeus.fx.simul.strategytest;

import java.util.Date;

import net.lexzeus.fx.simul.MarketDataSource;
import net.lexzeus.fx.simul.Tick;

/**
 * To emulate running market data
 *
 * @author Alexander Koentjara
 */
public class MarketDataEmulator extends Thread {

   private MarketDataSource source;
   private MarketDataSource target;
   private boolean simple;
   private boolean finished = false;

   public MarketDataEmulator(MarketDataSource source, MarketDataSource target, boolean simple) {
      this.source = source;
      this.target = target;
      this.simple = simple;
      this.setDaemon(true);
   }

   @Override
   public void run() {
      Tick[] tk = source.getMarketData();
      for (int i = 0; i < tk.length; i++) {
         Tick now = tk[i];
         if (simple) {
            target.addTick(now);
            sleep();
         } else {
            Date d = now.getTimestamp();
            boolean upFirst = ((int) (Math.random() * 2d)) == 0;

            double high = Math.random() * (now.getHigh() - now.getClosePrice());
            double low = Math.random() * (now.getClosePrice() - now.getLow());
            if (upFirst) {
               target.addTick(new Tick(new Date(d.getTime() - 300), 10, now.getOpenPrice(), now.getClosePrice() + high,
                                       now.getLow(), now.getHigh()
               ));
               sleep();
               target.addTick(now);
               sleep();
               target.addTick(
                  new Tick(new Date(d.getTime() + 300), 20, now.getOpenPrice(), now.getClosePrice() - low, now.getLow(),
                           now.getHigh()
                  ));
               sleep();
            } else {
               target.addTick(
                  new Tick(new Date(d.getTime() - 300), 10, now.getOpenPrice(), now.getClosePrice() - low, now.getLow(),
                           now.getHigh()
                  ));
               target.addTick(now);
               sleep();
               target.addTick(new Tick(new Date(d.getTime() + 300), 20, now.getOpenPrice(), now.getClosePrice() + high,
                                       now.getLow(), now.getHigh()
               ));
               sleep();
            }
         }
      }

      synchronized (this) {
         finished = true;
         notify();
      }
   }

   public synchronized void waitTillfinished() {
      while (!finished) {
         try {
            this.wait();
         } catch (InterruptedException e) {
         }
      }
   }

   private void sleep() {
      /*
       * try { this.sleep(10L); } catch (InterruptedException e) { }
       */
   }

};