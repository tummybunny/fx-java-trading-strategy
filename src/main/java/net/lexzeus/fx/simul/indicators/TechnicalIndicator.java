package net.lexzeus.fx.simul.indicators;


import net.lexzeus.fx.simul.MarketDataSource;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;
import net.lexzeus.util.Log;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;

/**
 * Base class for Technical Indicator.
 *
 * @author Alexander Koentjara
 */
public abstract class TechnicalIndicator implements Serializable {

   private static final long serialVersionUID = -2297944142042425658L;
   private final Log log = Log.get(this);

   public enum PriceType {
      Close, Open, High, Low
   }

   public static final double[] EMPTY_PRICE = new double[0];

   private Color color = new Color(150, 150, 150);
   private int[] techIndicatorValues = new int[]{1};
   private PriceType priceType = PriceType.Close;

   /**
    * Default constructor
    */
   public TechnicalIndicator() {
   }

   /**
    * Get the price type used in this indicator
    *
    * @return
    */
   public PriceType getPriceType() {
      return priceType;
   }

   /**
    * Set the price type used in this indicator
    *
    * @param priceType
    */
   public void setPriceType(PriceType priceType) {
      this.priceType = priceType;
   }

   /**
    * Get the color used for this indicator
    *
    * @return
    */
   public Color getColor() {
      return color;
   }

   /**
    * Set the color used for this indicator
    *
    * @param newColor
    */
   public void setColor(Color newColor) {
      this.color = newColor;
   }

   /**
    * Set the value of indicator
    *
    * @param value
    */
   public void setTechIndicatorValue(int[] value) {
      this.techIndicatorValues = value;
   }

   /**
    * Get the first indicator value
    *
    * @return
    */
   public int getFirstTechIndicatorValue() {
      return getTechIndicatorValueAt(0);
   }

   /**
    * Get the second indicator value
    *
    * @return
    */
   public int getSecondTechIndicatorValue() {
      return getTechIndicatorValueAt(1);
   }

   /**
    * Get the indicator value at specified idx
    *
    * @param idx
    * @return
    */
   public int getTechIndicatorValueAt(int idx) {
      return this.techIndicatorValues.length - 1 >= idx ? Math.abs(this.techIndicatorValues[idx]) : -1;
   }

   /**
    * Get all indicator values
    *
    * @return
    */
   public int[] getAllTechIndicatorValues() {
      return this.techIndicatorValues;
   }

   /**
    * Produce the technical indicator and return array of double. This method queries the ticks from market data source
    * for given start and end period, and the interval type and value. Then, it calls applyIndicatorImpl(Tick[],
    * PricePlotter, Graphics, double, double) to compute the technical indicators.
    *
    * @param startPeriod
    * @param endPeriod
    * @param mdata
    * @param plotter
    * @param graphics
    * @param type
    * @param intervalValue
    * @return array of double as technical indicator values
    * @see #applyIndicatorImpl(Tick[], PricePlotter, Graphics)
    */
   public double[] applyIndicator(
      Date startPeriod, Date endPeriod, MarketDataSource mdata, PricePlotter plotter, Graphics graphics,
      MarketDataSource.ChartIntervalType type, int intervalValue
   ) {
      try {
         return applyIndicatorImpl(mdata.getMarketData(startPeriod, endPeriod, type, intervalValue), plotter, graphics);
      } catch (Exception e) {
         log.warn(Level.WARNING, "Error applying indicator", e);
         return EMPTY_PRICE;
      }
   }

   /**
    * The real computation of indicator
    *
    * @param ticks
    * @param plotter
    * @param graphics
    * @return
    */
   protected abstract double[] applyIndicatorImpl(Tick[] ticks, PricePlotter plotter, Graphics graphics);


   @Override
   public boolean equals(Object obj) {
      TechnicalIndicator other = (TechnicalIndicator) obj;
      boolean same = this.getClass().equals(other.getClass()) && this.priceType == other.priceType;
      return (this instanceof NonParameterizedIndicator) ? same : same && hash(this.techIndicatorValues) == hash(
         other.techIndicatorValues);
   }

   @Override
   public int hashCode() {
      return (this instanceof NonParameterizedIndicator) ? this.getClass().hashCode() : this.getClass()
                                                                                            .hashCode() * hash(
         this.techIndicatorValues);
   }

   private int hash(int[] techIndicatorValues) {
      int hash = 1;
      for (int i : techIndicatorValues) {
         hash += i;
         hash *= i;
      }
      return hash;
   }

   @Override
   public String toString() {
      String str =
         ((this instanceof NonParameterizedIndicator) ? getName() : getName() + " " + getFirstTechIndicatorValue()) + " at ";

      switch (getPriceType()) {
         case High:
            str += "max";
            break;
         case Low:
            str += "min";
            break;
         case Open:
            str += "open";
            break;
         default:
            str += "close";
            break;
      }

      return str;
   }

   public abstract String getName();

   /**
    * Convenient way to access price for given ticks and index.
    *
    * @param ticks
    * @param idx
    * @return
    */
   protected double getPrice(Tick[] ticks, int idx) {
      switch (getPriceType()) {
         case High:
            return ticks[idx].getHigh();
         case Low:
            return ticks[idx].getLow();
         case Open:
            return ticks[idx].getOpenPrice();
         default:
            return ticks[idx].getClosePrice();
      }
   }

}
