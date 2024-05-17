package net.lexzeus.fx.simul;

/**
 * The main application frame.
 *
 * @author Alexander Koentjara
 */
public interface IMainFrame {

   ChartView getChartView();

   IControlPanel getChartTool();

   void saveSnapshot();

   void loadSnapshot();

   void openMarketData();

   void startRecordProfitAndLoss();

   void stopRecordProfitAndLoss();

   void refresh();

   void addSelectedTechnicalIndicator();

   void removeSelectedTechnicalIndicator();

   void clearAllTechnicalIndicator();

}
