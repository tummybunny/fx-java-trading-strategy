package net.lexzeus.fx.simul.strategies;

import java.io.Serializable;

/**
 * Encapsulates the parameters used to run a strategy:
 * <li> strategy values: e.g. to specify the Moving Average ratio, etc.
 * <li> takeProfit: the profit at which open position should be closed to realize profit
 * <li> cutLoss: how much one can bear the loss
 * <li> dynamicCutLoss: how much one can bear the loss after making profit (similar to trailing stop order)
 *
 * @author Alexander Koentjara
 */
public class StrategyParams implements Serializable {

   private static final long serialVersionUID = -6242588625106326479L;

   private double takeProfit = Integer.MAX_VALUE;
   private double cutLoss = Integer.MAX_VALUE;
   private double dynamicCutLoss = Integer.MAX_VALUE;
   private int[] values = new int[10];

   public StrategyParams() {
   }

   public StrategyParams(int[] values, double takeProfit, double cutLoss, double dynamicCutLoss) {
      this.values = values == null || values.length == 0 ? new int[10] : values;
      this.takeProfit = takeProfit;
      this.cutLoss = cutLoss;
      this.dynamicCutLoss = dynamicCutLoss;
   }

   public double getCutLoss() {
      return cutLoss;
   }

   public void setCutLoss(double cutLoss) {
      this.cutLoss = cutLoss;
   }

   public double getDynamicCutLoss() {
      return dynamicCutLoss;
   }

   public void setDynamicCutLoss(double dynamicCutLoss) {
      this.dynamicCutLoss = dynamicCutLoss;
   }

   public int getStrategyValue(int idx) {
      return idx >= values.length ? -1 : values[idx];
   }

   public void setStrategyValues(int[] values) {
      this.values = values == null || values.length == 0 ? new int[10] : values;
   }

   public int[] getStrategyValues() {
      return this.values;
   }

   public double getTakeProfit() {
      return takeProfit;
   }

   public void setTakeProfit(double takeProfit) {
      this.takeProfit = takeProfit;
   }

   public boolean isCutLossExist() {
      return cutLoss > 0 && cutLoss < 100000d;
   }

   public boolean isDynamicCutLossExist() {
      return dynamicCutLoss > 0 && dynamicCutLoss < 100000d;
   }

   public boolean isTakeProfitExist() {
      return takeProfit > 0 && takeProfit < 100000d;
   }

}
