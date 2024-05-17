package net.lexzeus.fx.simul;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Representation of Profit and Loss item, in a simple dumb java bean.
 *
 * @author Alexander Koentjara
 * @see PnLCalculator
 */
public class PnL implements Comparable<PnL> {

   private final Date timestamp;
   private final double price;

   /**
    * The timestamp is when the buy/sell order is executed. The price is the buy/sell price, negative is buy, positive
    * is sell.
    *
    * @param timestamp
    * @param price
    */
   public PnL(Date timestamp, double price) {
      this.timestamp = timestamp;
      this.price = price;
   }

   public Date getTimestamp() {
      return timestamp;
   }

   public double getPrice() {
      return Math.abs(price);
   }

   public double geSignedPrice() {
      return price;
   }

   public boolean isBuy() {
      return price < 0;
   }

   public boolean isSell() {
      return price >= 0;
   }

   @Override
   public String toString() {
      return (isBuy() ? "Buy " : "Sell ") + getPrice() + " at " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(
         getTimestamp());
   }

   @Override
   public int hashCode() {
      return (int) timestamp.getTime();
   }

   @Override
   public boolean equals(Object obj) {
      PnL other = (PnL) obj;
      return this.timestamp.equals(other.timestamp) && this.price == other.price;
   }

   public int compareTo(PnL o) {
      return this.timestamp.compareTo(o.timestamp);
   }

}
