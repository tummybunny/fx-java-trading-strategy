package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Is not a technical indicator, but rather to draw open/close/high/low line in the chart. No parameter is required, if
 * supplied they will be ignored.
 *
 * @author Alexander Koentjara
 */
public class LineTechIndicator extends TechnicalIndicator implements NonParameterizedIndicator {

   private static final long serialVersionUID = -1349811230173662147L;

   public static final LineTechIndicator INSTANCE = new LineTechIndicator();

   public LineTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] prices, PricePlotter plotter, Graphics graphics) {
      if (graphics != null) {
         graphics.setColor(this.getColor());
      }
      Point oldPt = null;
      for (int i = 0; i < prices.length; i++) {
         double val = getPrice(prices, i);

         if (graphics != null) {
            Date dt = prices[i].getTimestamp();
            Point newPt = plotter.plotPriceAt(val, dt);
            if (oldPt != null) {
               graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);
            }
            oldPt = newPt;
         }
      }

      return null;
   }

   @Override
   public String getName() {
      return "Price Line";
   }

}
