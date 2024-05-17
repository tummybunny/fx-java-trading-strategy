/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.lexzeus.fx.simul;

import net.lexzeus.fx.simul.indicators.TechnicalIndicator;
import net.lexzeus.fx.simul.strategies.StrategyParams;
import net.lexzeus.util.Log;

import java.io.*;
import java.util.Date;

import static net.lexzeus.fx.simul.Configuration.VERSION_KEY;
import static net.lexzeus.fx.simul.Configuration.getProperty;

/**
 * SnapshotData encapsulates state of market data, technical indicators, and strategy parameters; allowing them to be
 * persisted into storage (e.g. file) for future uses. One can think this as a data structure for saving and loading
 * chart and its information to file.
 *
 * @author Alexander Koentjara
 */
public class SnapshotData implements Serializable {

   private static final long serialVersionUID = -3467178253669182448L;

   private static final String HEADER = "Snapshot file v " + getProperty(VERSION_KEY);
   private MarketDataSource marketData;
   private Date start;
   private Date stop;
   private TechnicalIndicator[] strategies;
   private boolean candle;
   private boolean closingLine;
   private MarketDataSource.ChartIntervalType intervalType;
   private int intervalValue;
   private StrategyParams strategyParams;
   private String strategyClass;
   private static final Log log = Log.get(SnapshotData.class);

   public SnapshotData() {
   }

   public SnapshotData(
      MarketDataSource marketData, Date start, Date stop, TechnicalIndicator[] strategies, boolean candle,
      boolean closingLine, MarketDataSource.ChartIntervalType intervalType, int intervalValue,
      StrategyParams strategyParams, String strategyClass
   ) {
      this.marketData = marketData;
      this.start = start;
      this.stop = stop;
      this.strategies = strategies;
      this.candle = candle;
      this.closingLine = closingLine;
      this.intervalType = intervalType;
      this.intervalValue = intervalValue;
      this.strategyParams = strategyParams;
      this.strategyClass = strategyClass;
   }

   public String getStrategyClass() {
      return strategyClass;
   }

   public void setStrategyClass(String strategyClass) {
      this.strategyClass = strategyClass;
   }

   public StrategyParams getStrategyParam() {
      return strategyParams;
   }

   public void setStrategyParam(StrategyParams strategyParams) {
      this.strategyParams = strategyParams;
   }

   public MarketDataSource getMarketData() {
      return marketData;
   }

   public void setMarketData(MarketDataSource marketData) {
      this.marketData = marketData;
   }

   public Date getStart() {
      return start;
   }

   public void setStart(Date start) {
      this.start = start;
   }

   public Date getStop() {
      return stop;
   }

   public void setStop(Date stop) {
      this.stop = stop;
   }

   public TechnicalIndicator[] getStrategies() {
      return strategies;
   }

   public void setStrategies(TechnicalIndicator[] strategies) {
      this.strategies = strategies;
   }

   public boolean isCandle() {
      return candle;
   }

   public void setCandle(boolean candle) {
      this.candle = candle;
   }

   public boolean isClosingLine() {
      return closingLine;
   }

   public void setClosingLine(boolean closingLine) {
      this.closingLine = closingLine;
   }

   public MarketDataSource.ChartIntervalType getIntervalType() {
      return intervalType;
   }

   public void setIntervalType(MarketDataSource.ChartIntervalType intervalType) {
      this.intervalType = intervalType;
   }

   public int getIntervalValue() {
      return intervalValue;
   }

   public void setIntervalValue(int intervalValue) {
      this.intervalValue = intervalValue;
   }

   public static void persistToFile(File file, SnapshotData data) {
      try (ObjectOutputStream stream = new ObjectOutputStream(
         new BufferedOutputStream(new FileOutputStream(file, false), 5120))) {
         stream.writeUTF(HEADER);
         stream.writeObject(data);
         log.info("Saved snapshot file: ", file);
      } catch (Exception e) {
         log.error("Unable to save snapshot to file: ", file, e);
      }
   }

   public static SnapshotData loadFromFile(File file) {
      try (ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file), 5120))) {
         String str = stream.readUTF();
         if (HEADER.equals(str)) {
            SnapshotData data = (SnapshotData) stream.readObject();
            log.info("Loaded snapshot file: ", file);
            return data;
         } else {
            return null;
         }
      } catch (Exception e) {
         log.error("Unable to load snapshot from file: ", file, e);
         return null;
      }
   }

}
