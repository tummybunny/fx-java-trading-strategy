package net.lexzeus.fx.simul;

import java.awt.Color;
import java.util.Date;

import net.lexzeus.fx.simul.MarketDataSource.ChartIntervalType;
import net.lexzeus.fx.simul.indicators.TechnicalIndicator.PriceType;

/**
 * Contract for the graphical user interface input.
 *
 * @author Alexander Koentjara
 */
public interface IControlPanel {

   Date getMarketDataStartTimestamp();

   void setMarketDataStartTimestamp(Date formatDate);

   Date getMarketDataStopTimestamp();

   void setMarketDataStopTimestamp(Date formatDate);

   boolean isCandleStickVisible();

   void setCandleStickVisible(boolean isVisible);

   boolean isClosingPriceLineVisible();

   void setClosingPriceLineVisible(boolean isVisible);

   int getChartIntervalValue();

   void setChartIntervalValue(int intervalValue);

   ChartIntervalType getChartIntervalType();

   void setChartIntervalType(ChartIntervalType intervalType);


   String getSelectedTechnicalIndicatorName();

   int[] getTechnicalIndicatorValues();

   Color getTechnicalIndicatorColor();

   PriceType getTechnicalIndicatorPriceType();

}
