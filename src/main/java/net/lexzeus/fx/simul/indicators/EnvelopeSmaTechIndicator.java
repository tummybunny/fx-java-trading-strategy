package net.lexzeus.fx.simul.indicators;

import java.awt.Graphics;
import java.awt.Point;
import java.util.Date;

import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * Envelope based on Simple Moving Average.
 * <p>Parameter(s):</p>
 * <li>Period (required)</li>
 * <li>Envelope, in a ten thousandth (e.g. 25 means 0.0025)
 *
 * @author Alexander Koentjara
 */
public class EnvelopeSmaTechIndicator extends SmaTechIndicator {

   private static final long serialVersionUID = -6022660826051142590L;

   public EnvelopeSmaTechIndicator() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] prices, PricePlotter plotter, Graphics graphics) {
      double[] retVal = super.applyIndicatorImpl(prices, plotter, graphics);
      double envelope = ((double) getSecondTechIndicatorValue()) / 10000d;
      if (graphics != null && prices.length > 1) {
         for (int i = 1; i < prices.length; i++) {
            draw(plotter, graphics, retVal[i - 1], prices[i - 1].getTimestamp(), retVal[i], prices[i].getTimestamp(),
                 envelope
            );
            draw(plotter, graphics, retVal[i - 1], prices[i - 1].getTimestamp(), retVal[i], prices[i].getTimestamp(),
                 -envelope
            );
         }
      }
      return retVal;
   }


   private void draw(
      PricePlotter plotter, Graphics graphics, double prevSma, Date d1, double sma, Date d2, double envelope
   ) {
      Point p1 = plotter.plotPriceAt(prevSma + envelope, d1);
      Point p2 = plotter.plotPriceAt(sma + envelope, d2);
      graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
   }

   @Override
   public String getName() {
      return "ENV-SMA";
   }

}
