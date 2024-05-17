package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Relative Strength Index.
 * <p>Parameter(s):</p>
 * <li>Period (required)</li>
 *
 * @author Alexander Koentjara
 */
public class RsiTechIndicator extends TechnicalIndicator implements IMomentumType {

   private static final long serialVersionUID = 8274264645293773145L;

   public RsiTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] prices, PricePlotter plotter, Graphics graphics) {
      double ups[] = new double[prices.length];
      double downs[] = new double[prices.length];
      double[] retVal = new double[prices.length];

      ups[0] = 0;
      downs[0] = 0;
      for (int i = 1; i < prices.length; i++) {
         double now = getPrice(prices, i);
         double prev = getPrice(prices, i - 1);
         if (now > prev) {
            ups[i] = now - prev;
         } else if (now < prev) {
            downs[i] = prev - now;
         }
      }

      ups = applyEma(ups);
      downs = applyEma(downs);

      if (graphics != null) {
         graphics.setColor(this.getColor());
      }

      for (int i = 0; i < prices.length; i++) {
         double u = ups[i];
         double d = downs[i];
         if (d == 0) {
            retVal[i] = 50;
         } else {
            double rs = u / d;
            retVal[i] = 100 - (100 * 1 / (1 + rs));
         }
      }

      retVal = applyEma(retVal);

      if (graphics != null && retVal.length > 1) {
         for (int i = 1; i < retVal.length; i++) {
            Date dt1 = prices[i - 1].getTimestamp();
            Point oldPt = plotter.plotScaleAt(retVal[i - 1], dt1);

            Date dt2 = prices[i].getTimestamp();
            Point newPt = plotter.plotScaleAt(retVal[i], dt2);
            graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);
         }
      }

      return retVal;
   }

   protected double[] applyEma(double[] prices) {
      double alpha = 2d / (1d + (double) getFirstTechIndicatorValue());
      double[] retVal = new double[prices.length];

      if (prices.length > 0) {
         // assume this is sma .. because we can't compute sma at 0
         retVal[0] = prices[0];
      }

      for (int i = 1; i < prices.length; i++) {
         double emaYesterday = retVal[i - 1];
         double emaToday = emaYesterday + (alpha * (prices[i] - emaYesterday));
         retVal[i] = emaToday;
      }
      return retVal;
   }

   @Override
   public String getName() {
      return "RSI";
   }

}
