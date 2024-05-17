package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Exponential Moving Average
 * <p>Parameter(s):</p>
 * <li>Period (required)</li>
 *
 * @author Alexander Koentjara
 */
public class EmaTechIndicator extends TechnicalIndicator {

   private static final long serialVersionUID = 6652030452163168603L;

   public EmaTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] prices, PricePlotter plotter, Graphics graphics) {
      double alpha = 2d / (1d + (double) getFirstTechIndicatorValue());
      double[] retVal = new double[prices.length];

      if (prices.length > 0) {
         // assume this is sma .. because we can't compute sma at 0
         retVal[0] = getPrice(prices, 0);
      }

      if (graphics != null) {
         graphics.setColor(this.getColor());
      }
      Point oldPt = null;
      for (int i = 1; i < prices.length; i++) {
         double emaYesterday = retVal[i - 1];
         double emaToday = emaYesterday + (alpha * (getPrice(prices, i) - emaYesterday));
         retVal[i] = emaToday;

         if (graphics != null) {
            Date dt = prices[i].getTimestamp();
            Point newPt = plotter.plotPriceAt(emaToday, dt);
            if (oldPt != null) {
               graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);
            }
            oldPt = newPt;
         }
      }

      return retVal;
   }

   @Override
   public String getName() {
      return "EMA";
   }

}
