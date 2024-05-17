package net.lexzeus.fx.simul.strategies;

import java.io.Serializable;
import java.text.DecimalFormat;

import net.lexzeus.fx.simul.Tick;
import net.lexzeus.fx.simul.Util;


/**
 * Simple data structure that represents a trade position. Trade position can be an opening or closing. Opening means we
 * do not have any long/short position and we are going to create one. Closing means we have an existing long/short
 * position, and we close that position by submitting order with different direction.
 *
 * @author Alexander Koentjara
 */
public class TradePosition implements Serializable, Comparable<TradePosition> {

   private static final long serialVersionUID = 7786459870484349257L;

   private final Tick tick;
   private final SignalDirection direction;
   private final SignalType type;

   /**
    * The default constructor
    *
    * @param direction
    * @param type
    * @param tick
    */
   public TradePosition(SignalDirection direction, SignalType type, Tick tick) {
      if (direction != SignalDirection.BUY && direction != SignalDirection.SELL) {
         throw new RuntimeException("Cannot create trade position with direction: " + direction);
      }

      this.direction = direction;
      this.type = type;
      this.tick = tick;
   }

   @Override
   public String toString() {
      DecimalFormat df = new DecimalFormat("0.0000");
      StringBuilder sb = new StringBuilder(30)
         .append(direction).append(",")
         .append(df.format(direction == SignalDirection.BUY ? -1d * tick.getBuyPrice() : tick.getSellPrice()))
         .append(",").append(Util.formatDate(tick.getTimestamp())).append(", ").append(type);
      return sb.toString();
   }

   /**
    * Reference to the tick when this position is opened/closed
    *
    * @return
    */
   public Tick getTick() {
      return tick;
   }

   /**
    * Get the direction of this position
    *
    * @return
    */
   public SignalDirection getDirection() {
      return direction;
   }

   /**
    * Get the signal type of this position
    *
    * @return
    */
   public SignalType getSignalType() {
      return type;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof TradePosition) {
         TradePosition b = (TradePosition) obj;
         return this.direction == b.direction && this.type == b.type && this.tick.equals(b.tick);
      }
      return false;
   }

   public int compareTo(TradePosition o) {
      int i = this.tick.compareTo(o.getTick());
      return (i == 0) ? this.direction.toString().compareTo(o.direction.toString()) : i;
   }

   @Override
   public int hashCode() {
      return tick.hashCode();
   }

}
