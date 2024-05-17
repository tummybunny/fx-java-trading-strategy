package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Simple Moving Average.
 * <p>Parameter(s):</p>
 * <li>Period (required)</li>
 *
 * @author Alexander Koentjara
 */
public class SmaTechIndicator extends TechnicalIndicator {

   private static final long serialVersionUID = -6022660826051142590L;

   public SmaTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] prices, PricePlotter plotter, Graphics graphics) {
      double[] retVal = new double[prices.length];
      int backtrack = getFirstTechIndicatorValue();

      if (graphics != null) {
         graphics.setColor(this.getColor());
      }
      Point oldPt = null;
      for (int i = prices.length - 1; i > 0; i--) {
         double val = 0;
         double ctr = 0;
         for (int j = i; j >= i - backtrack + 1; j--) {
            val += (j < 0 ? getPrice(prices, 0) : getPrice(prices, j));
            ctr += 1;
         }
         retVal[i] = val / ctr;

         if (graphics != null) {
            Date dt = prices[i].getTimestamp();
            Point newPt = plotter.plotPriceAt(retVal[i], dt);
            if (oldPt != null) {
               graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);
            }
            oldPt = newPt;
         }
      }
      if (prices.length > 0) {
         retVal[0] = getPrice(prices, 0);
      }
      return retVal;
   }

   @Override
   public String getName() {
      return "SMA";
   }

}
