package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * +DI indicator, based on http://technical.traders.com/tradersonline/display.asp?art=278
 * <p>Parameter(s):</p>
 * <li>Period (required)</li>
 *
 * @author Alexander Koentjara
 */
public class PlusDiTechIndicator extends TechnicalIndicator implements IMomentumType {

   private static final long serialVersionUID = 7829120622773633117L;

   public PlusDiTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] ticks, PricePlotter plotter, Graphics graphics) {
      double[] tr = new double[ticks.length];
      double[] dmPlus = new double[ticks.length];
      double[] trN = new double[ticks.length];
      double[] dmPlusN = new double[ticks.length];
      double[] di = new double[ticks.length];

      int smoothRatio = this.getFirstTechIndicatorValue(); // 14

      if (graphics != null) {
         graphics.setColor(this.getColor());
      }
      Point oldPt = null;

      for (int i = 1; i < ticks.length - 1; i++) {
         Tick prev = ticks[i - 1];
         Tick cur = ticks[i];

         tr[i] = Math.max(
            Math.max(cur.getHigh() - cur.getLow(), Math.abs(cur.getHigh() - prev.getClosePrice())),
            Math.abs(cur.getLow() - prev.getClosePrice())
         );

         double v1 = cur.getHigh() - prev.getHigh();
         double v2 = prev.getLow() - cur.getLow();
         dmPlus[i] = v1 > v2 ? Math.max(v1, 0) : 0;

         double diPlus = 0;

         if (i - smoothRatio + 1 < 1) {
            // nothing
         } else {
            if (i - smoothRatio + 1 == 1) {
               for (int j = i - smoothRatio + 1; j <= i; j++) {
                  dmPlusN[i] += dmPlus[j];
                  trN[i] += tr[j];
               }
            } else {
               trN[i] = (trN[i - 1] - (trN[i - 1] / (double) smoothRatio)) + tr[i];
               dmPlusN[i] = (dmPlusN[i - 1] - (dmPlusN[i - 1] / (double) smoothRatio)) + dmPlus[i];
            }

            diPlus = (int) (100 * (dmPlusN[i] / trN[i]));
            di[i] = diPlus;

            if (graphics != null) {
               Date dt = ticks[i].getTimestamp();
               Point newPt = plotter.plotScaleAt(diPlus, dt);
               if (oldPt != null) {

                  graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);
               }
               oldPt = newPt;
            }
         }
      }

      return di;
   }

   @Override
   public String getName() {
      return "+DI";
   }

}