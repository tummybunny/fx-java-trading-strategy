package net.lexzeus.fx.simul;

import net.lexzeus.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Simple class that performs PnL calculation and store them in csv format.
 *
 * @author Alexander Koentjara
 * @see PnL
 */
public class PnLCalculator {

   public static final PnLCalculator INSTANCE = new PnLCalculator();
   public static final String CSV_DELIMITER = ",";
   private final Log log = Log.get(this);

   protected PnLCalculator() {
   }

   /**
    * Calculate profit and loss, record the journal into a csv file.
    *
    * @param pnls
    * @param csvFile
    * @return the net profit/loss
    */
   public double calculateToCsvFile(PnL[] pnls, File csvFile) {
      StringBuilder buffer = new StringBuilder(1024);
      double profit = calculateToCsvBuffer(pnls, buffer);
      try (FileWriter fw = new FileWriter(csvFile, false)) {
         fw.write(buffer.toString());
      } catch (Exception ex) {
         log.error("Error generating P&L", ex);
      }

      return profit;
   }

   /**
    * Calculate profit and loss, record the journal into the specified string builder.
    *
    * @param pnls
    * @param builder
    * @return the net profit / loss.
    */
  public double calculateToCsvBuffer(PnL[] pnls, StringBuilder builder) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      DecimalFormat df = new DecimalFormat("0.0000");
      double profit = 0d;

      builder.append("TIME");
      builder.append(CSV_DELIMITER);
      builder.append("ORDER");
      builder.append(CSV_DELIMITER);
      builder.append("PRICE");
      builder.append(CSV_DELIMITER);
      builder.append("BALANCE");
      builder.append("\n");

      for (PnL p : pnls) {
         profit += p.geSignedPrice();
         builder.append(sdf.format(p.getTimestamp()));
         builder.append(CSV_DELIMITER);
         builder.append(p.isBuy() ? "BUY" : "SELL");
         builder.append(CSV_DELIMITER);
         builder.append(df.format(p.getPrice()));
         builder.append(CSV_DELIMITER);
         builder.append(df.format(profit));
         builder.append("\n");
      }

      if (profit >= 0d) {
         builder.append("\n\nTotal profit: ").append(df.format(profit));
      } else {
         builder.append("\n\nTotal loss: ").append(df.format(profit));
      }

      return profit;
   }

   /**
    * Calculate profit and loss, record the journal into the specified string builder.
    *
    * @param pnls
    * @return the net profit / loss.
    */
   public double calculate(PnL[] pnls) {
      double profit = 0d;
      for (PnL p : pnls) {
         profit += p.geSignedPrice();
      }
      return profit;
   }

}
