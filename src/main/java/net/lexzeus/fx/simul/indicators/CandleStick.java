package net.lexzeus.fx.simul.indicators;

import java.awt.Color;
import java.awt.Graphics;

import net.lexzeus.fx.simul.IMomentumType;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * To display candle stick in chart
 *
 * @author Alexander Koentjara
 */
public class CandleStick extends TechnicalIndicator implements IMomentumType {

   private static final long serialVersionUID = 7829120622773633117L;

   public CandleStick() {
      super();
   }

   @Override
   protected double[] applyIndicatorImpl(Tick[] ticks, PricePlotter plotter, Graphics graph) {
      for (int ctr = 0; ctr < ticks.length; ctr++) {
         Tick t = ticks[ctr];
         graph.setColor(t.getOpenPrice() < t.getClosePrice() ? Color.GREEN : Color.RED);

         double x = plotter.plotPriceAt(t.getOpenPrice(), t.getTimestamp()).getX();
         double openY = plotter.plotPriceAt(t.getOpenPrice(), t.getTimestamp()).getY();
         double closeY = plotter.plotPriceAt(t.getClosePrice(), t.getTimestamp()).getY();
         double maxY = plotter.plotPriceAt(t.getHigh(), t.getTimestamp()).getY();
         double minY = plotter.plotPriceAt(t.getLow(), t.getTimestamp()).getY();

         graph.drawLine((int) x, (int) maxY, (int) x, (int) minY);
         graph.fillRect((int) x - 1, (int) Math.min(openY, closeY), 3, (int) Math.abs(openY - closeY));
      }
      return null;
   }

   @Override
   public String getName() {
      return "CandleStick";
   }

}