package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;

/**
 * The Bollinger Band, using standard deviation to draw band above and below SMA
 * <p>Parameter(s):</p>
 * <li>SMA Period (required)</li>
 * <li>Standard Deviation (optional, or 2 if not specified).</li>
 *
 * @author Alexander Koentjara
 */
public class BollingerBandTechIndicator extends TechnicalIndicator {

   private static final long serialVersionUID = 2287361934367681975L;

   @Override
   protected double[] applyIndicatorImpl(Tick[] prices, PricePlotter plotter, Graphics graphics) {
      double[] retVal = new double[prices.length];
      int period = getFirstTechIndicatorValue();

      double deviations = getSecondTechIndicatorValue();
      if (deviations < 0) {
         deviations = 2;
      } else {
         deviations = deviations / 100d;
      }

      if (graphics != null) {
         graphics.setColor(this.getColor());
      }

      Point oldPtSma = null;
      Point oldPtBbUpper = null;
      Point oldPtBbLower = null;
      for (int i = prices.length - 1; i > 0; i--) {
         double v1 = 0, v2 = 0;
         double ctr = 0;
         for (int j = i; j >= i - period + 1; j--) {
            double priceAtJ = j < 0 ? getPrice(prices, 0) : getPrice(prices, j);
            v1 += priceAtJ;
            v2 += priceAtJ * priceAtJ;
            ctr += 1;
         }

         double sma = v1 / ctr;
         double stdDev = Math.abs((ctr * v2) - (v1 * v1));
         stdDev = Math.sqrt(stdDev / (ctr * (ctr - 1)));
         double bband = deviations * stdDev;
         double bbUpper = sma + bband;
         double bbLower = sma - bband;

         retVal[i] = bband;
         if (graphics != null) {
            Date dt = prices[i].getTimestamp();
            Point newPtSma = plotter.plotPriceAt(sma, dt);
            Point newPtBbUpper = plotter.plotPriceAt(bbUpper, dt);
            Point newPtBbLower = plotter.plotPriceAt(bbLower, dt);
            if (oldPtSma != null) {
               graphics.drawLine(oldPtSma.x, oldPtSma.y, newPtSma.x, newPtSma.y);
               graphics.drawLine(oldPtBbUpper.x, oldPtBbUpper.y, newPtBbUpper.x, newPtBbUpper.y);
               graphics.drawLine(oldPtBbLower.x, oldPtBbLower.y, newPtBbLower.x, newPtBbLower.y);
            }
            oldPtSma = newPtSma;
            oldPtBbUpper = newPtBbUpper;
            oldPtBbLower = newPtBbLower;
         }
      }
      if (prices.length > 0) {
         retVal[0] = getPrice(prices, 0);
      }
      return retVal;
   }

   @Override
   public String getName() {
      return "Boll-Band";
   }

}
