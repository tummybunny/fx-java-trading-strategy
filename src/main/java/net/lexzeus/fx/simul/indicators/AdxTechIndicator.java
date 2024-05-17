package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * ADX, based on http://technical.traders.com/tradersonline/display.asp?art=278
 * <p>Parameter(s):</p>
 * <li>Period (required)</li>
 *
 * @author Alexander Koentjara
 */
public class AdxTechIndicator extends TechnicalIndicator implements IMomentumType {

   private static final long serialVersionUID = 7829120622773633117L;

   public AdxTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] ticks, PricePlotter plotter, Graphics graphics) {
      // See https://technical.traders.com/tradersonline/display.asp?art=278
      double[] tr = new double[ticks.length];
      double[] dmPlus = new double[ticks.length];
      double[] dmMinus = new double[ticks.length];
      double[] trN = new double[ticks.length];
      double[] dmPlusN = new double[ticks.length];
      double[] dmMinusN = new double[ticks.length];
      double[] dx = new double[ticks.length];
      double[] adx = new double[ticks.length];

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
         dmMinus[i] = v2 > v1 ? Math.max(v2, 0) : 0;

         double diPlus = 0;
         double diMinus = 0;
         double diDiff = 0;
         double diSum = 0;

         if (i - smoothRatio + 1 < 1) {
            // nothing
         } else {
            if (i - smoothRatio + 1 == 1) {
               for (int j = i - smoothRatio + 1; j <= i; j++) {
                  dmPlusN[i] += dmPlus[j];
                  dmMinusN[i] += dmMinus[j];
                  trN[i] += tr[j];
               }
            } else {
               trN[i] = (trN[i - 1] - (trN[i - 1] / (double) smoothRatio)) + tr[i];
               dmPlusN[i] = (dmPlusN[i - 1] - (dmPlusN[i - 1] / (double) smoothRatio)) + dmPlus[i];
               dmMinusN[i] = (dmMinusN[i - 1] - (dmMinusN[i - 1] / (double) smoothRatio)) + dmMinus[i];
            }

            diPlus = (int) (100 * (dmPlusN[i] / trN[i]));
            diMinus = (int) (100 * (dmMinusN[i] / trN[i]));

            diDiff = Math.abs(diMinus - diPlus);
            diSum = diMinus + diPlus;

            dx[i] = (int) (100 * (diDiff / diSum));

            int dob = smoothRatio * 2 - 2;
            if (i < dob) {
               // nothing
            } else if (i == dob) {
               double adxt = 0;
               int ctr = 0;
               for (int j = smoothRatio; j <= i; j++) {
                  ctr++;
                  adxt += dx[j];
               }
               adx[i] = (int) adxt / (double) ctr;
            } else {
               adx[i] = (int) (((adx[i - 1] * ((double) smoothRatio - 1)) + dx[i]) / (double) smoothRatio);
            }

            if (i - dob + 1 > 1 && graphics != null) {
               Date dt = ticks[i].getTimestamp();
               Point newPt = plotter.plotScaleAt(adx[i], dt);
               if (oldPt != null) {

                  graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);
               }
               oldPt = newPt;
            }
         }
      }

      return adx;
   }

   @Override
   public String getName() {
      return "ADX";
   }

}