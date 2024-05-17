package net.lexzeus.fx.simul;

import java.io.Serializable;
import java.util.Date;

/**
 * Represent a single tick of market data. A tick consist of the timestamp, opening price, closing price, the high and
 * low, and the volume. Ps: Volume may not be applicable for FOREX. It is provided so that it can be used to run
 * strategy for other asset classes too.
 *
 * @author Alexander Koentjara
 */
public class Tick implements Comparable<Tick>, Serializable {

   private static final long serialVersionUID = 703623908379411306L;

   /**
    * The bid/offer spread, e.g. in EUR/USD, this is generally 2 pips, that is 0.0002. Set this value before running a
    * strategy.
    */
   public static double BID_OFFER_SPREAD = 0d; //0.0002d;

   protected Date date;
   protected long volume;
   protected double open;
   protected double close;
   protected double low;
   protected double high;

   protected Tick() {
   }

   public Tick(Date date, long volume, double open, double close, double low, double high) {
      this.date = date;
      this.volume = volume;
      this.open = open;
      this.close = close;
      this.low = low;
      this.high = high;
   }

   public Date getTimestamp() {
      return date;
   }

   public long getVolume() {
      return volume;
   }

   public double getOpenPrice() {
      return open;
   }

   public double getClosePrice() {
      return close;
   }

   public double getLow() {
      return low;
   }

   public double getHigh() {
      return high;
   }

   /**
    * Is a function of closing price + (SPREAD / 2);
    *
    * @return
    */
   public double getBuyPrice() {
      return close + (BID_OFFER_SPREAD / 2d);
   }

   /**
    * Is a function of closing price - (SPREAD / 2);
    *
    * @return
    */
   public double getSellPrice() {
      return close - (BID_OFFER_SPREAD / 2d);
   }

   /**
    * Compare the current tick's timestamp with the other's
    */
   public int compareTo(Tick o) {
      long thisVal = this.date.getTime();
      long anotherVal = o.date.getTime();
      return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(Util.formatDate(date)).append(" P: ").append(getSellPrice()).append("/").append(getBuyPrice()).append(
         " H: ").append(high).append(" L: ").append(low).append(" O: ").append(open);

      return sb.toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Tick) {
         Tick o = (Tick) obj;
         return this.close == o.close && this.getTimestamp() == o.getTimestamp() && this.high == o.high && this.low == o.low && this.open == o.open;
      }
      return false;
   }

   @Override
   public int hashCode() {
      return date.hashCode();
   }

}
