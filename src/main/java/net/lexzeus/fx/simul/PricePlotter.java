package net.lexzeus.fx.simul;

import java.awt.Point;
import java.util.Date;

/**
 * PricePlotter is an adaptor to let Technical Indicator and Strategy classes to query the absolute coordinate in the
 * chart given a price or scale and a timestamp.
 *
 * @author Alexander Koentjara
 */
public interface PricePlotter {

   /**
    * To get the chart coordinte given specified price and tick timestamp
    *
    * @param price     The price/technical indicator value of security at timestamp
    * @param timestamp
    * @return
    */
   Point plotPriceAt(double price, Date timestamp);

   /**
    * To get the chart coordinte given specified scale and tick timestamp
    *
    * @param scale     A number between 0-100
    * @param timestamp
    * @return
    */
   Point plotScaleAt(double scale, Date timestamp);

}
