package net.lexzeus.fx.simul;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.lexzeus.fx.simul.strategies.SignalDirection;
import net.lexzeus.fx.simul.strategies.TradePosition;

/**
 * Utility class used in {@link net.lexzeus.fx.simul} package
 *
 * @author Alexander Koentjara
 */
public class Util {

   static DecimalFormat decFormat = new DecimalFormat("0.0000");
   static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

   public static <T> T defaultIfNull(T value, T defaultVal) {
      return value != null ? value : defaultVal;
   }

   public static synchronized String formatDate(Date date) {
      return date == null ? "" : dateFormat.format(date);
   }

   public static synchronized Date parseDate(String date) throws ParseException {
      return date == null ? null : dateFormat.parse(date);
   }

   public static synchronized String formatPrice(double totalProfit) {
      return decFormat.format(totalProfit);
   }

   public static final double calcProfit(TradePosition pos, Tick currentTick) {
      if (pos.getDirection() == SignalDirection.BUY) {
         // long the security
         return currentTick.getSellPrice() - pos.getTick().getBuyPrice();
      } else {
         // short the security
         return pos.getTick().getSellPrice() - currentTick.getBuyPrice();
      }
   }

}
