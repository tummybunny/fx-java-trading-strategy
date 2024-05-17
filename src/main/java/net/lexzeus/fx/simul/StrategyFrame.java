package net.lexzeus.fx.simul;

import net.lexzeus.fx.simul.strategies.*;
import net.lexzeus.util.Log;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Simple frame to choose the buy/sell strategy and customize its parameters.
 *
 * @author Alexander Koentjara
 */
public class StrategyFrame extends JFrame implements ISizeCallback {

   public static StrategyFrame INSTANCE;

   private static final long serialVersionUID = 634811073633636485L;

   private int lastWidth;
   private JTextField values;
   private JTextField takeProfit;
   private JTextField cutLoss;
   private JTextField dynCutLoss;
   private JComboBox strategies;

   private JLabel profitLossLb;

   private static ChartView chartView;
   private final Log log = Log.get(this);

   public static void main(String[] args) {
      StrategyFrame f = new StrategyFrame();
      f.setDefaultCloseOperation(EXIT_ON_CLOSE);
      f.pack();
      f.setLocationRelativeTo(null);
      f.setVisible(true);
   }

   public StrategyFrame() {
      super("Strategy");
      setDefaultCloseOperation(HIDE_ON_CLOSE);
      GuiUtil.setSizeCallback(this);

      StrategyFrame.INSTANCE = this;
      Container c = this.getContentPane();

      strategies = GuiUtil.createCombo(BuySellStrategyFactory.getInstance().getAllStrategyNames());
      c.add(strategies, BorderLayout.NORTH);

      JPanel panel = new JPanel();
      panel.setLayout(new FixedLayout(200, 130));

      profitLossLb = GuiUtil.createLabel("00000000000000", GuiUtil.BOLD_FONT, Color.WHITE, Color.BLACK);
      profitLossLb.setPreferredSize(profitLossLb.getPreferredSize());
      profitLossLb.setHorizontalAlignment(SwingConstants.RIGHT);
      profitLossLb.setText("0 ");

      panel.add("1,13", GuiUtil.createLabel("Indicator Values:"));
      int x = lastWidth + 5;
      panel.add("1,33", GuiUtil.createLabel("Take Profit:"));
      panel.add("1,53", GuiUtil.createLabel("Cut Loss:"));
      panel.add("1,73", GuiUtil.createLabel("Dyn. Cut Loss:"));
      panel.add("1,103", GuiUtil.createButton(" TEST ", (e) -> testStrategy()));

      values = GuiUtil.createTextField("10 20 30 40 50 60");
      values.setText("10");
      panel.add(x + ",11", values);

      takeProfit = GuiUtil.createTextField("00.0000");
      takeProfit.setText("0.0000");
      panel.add(x + ",31", takeProfit);

      cutLoss = GuiUtil.createTextField("00.0000");
      cutLoss.setText("0.0000");
      panel.add(x + ",51", cutLoss);

      dynCutLoss = GuiUtil.createTextField("00.0000");
      dynCutLoss.setText("0.0000");
      panel.add(x + ",71", dynCutLoss);

      panel.add(x + ",103", profitLossLb);

      c.add(panel, BorderLayout.CENTER);

      JPanel bottom = new JPanel();
      bottom.setLayout(new FlowLayout());
      bottom.add(GuiUtil.createButton("Save P&L", (e) -> recordStrategyResult()));
      bottom.add(GuiUtil.createLabel("    "));
      bottom.add(GuiUtil.createButton("APPLY", (e) -> applyStrategy()));
      c.add(bottom, BorderLayout.SOUTH);
   }

   protected void testStrategy() {
      try {
         StrategyParams param = createParam();

         String strategyName = String.valueOf(strategies.getSelectedItem());
         BuySellStrategy strategy = BuySellStrategyFactory.getInstance().createStrategy(strategyName, param);
         SnapshotData data = chartView.createSnapshot();
         strategy.setInterval(data.getIntervalType(), data.getIntervalValue());
         strategy.setMarketData(data.getMarketData());
         strategy.initialize();
         PnLListener listener = new PnLListener();
         strategy.addBuySellListener(listener);
         strategy.executeStrategy(data.getStart(), data.getStop(), null, null);

         PnL[] pnls = listener.getRecords();
         if (pnls.length > 0) {
            double profitLoss = PnLCalculator.INSTANCE.calculateToCsvBuffer(pnls, new StringBuilder(1024));
            profitLossLb.setForeground(profitLoss >= 0 ? Color.WHITE : Color.RED);
            profitLossLb.setText(Util.formatPrice(profitLoss) + " ");
         }
      } catch (Exception e) {
         log.warn(e);
      }
   }

   protected void recordStrategyResult() {
      try {
         StrategyParams param = createParam();

         String strategyName = String.valueOf(strategies.getSelectedItem());
         BuySellStrategy strategy = BuySellStrategyFactory.getInstance().createStrategy(strategyName, param);
         SnapshotData data = chartView.createSnapshot();
         strategy.setInterval(data.getIntervalType(), data.getIntervalValue());
         strategy.setMarketData(data.getMarketData());
         strategy.initialize();
         PnLListener listener = new PnLListener();
         strategy.addBuySellListener(listener);
         strategy.executeStrategy(data.getStart(), data.getStop(), null, null);

         PnL[] pnls = listener.getRecords();
         if (pnls.length > 0) {
            JFileChooser jf = new JFileChooser();
            jf.setFileFilter(new FileFilter() {

               @Override
               public boolean accept(File f) {
                  return f.getAbsolutePath().toLowerCase().endsWith(".csv") || f.isDirectory();
               }

               @Override
               public String getDescription() {
                  return "*.csv";
               }
            });
            jf.setDialogTitle("Save Profit And Loss");

            if (jf.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
               File f = jf.getSelectedFile();
               if (!f.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                  f = new File(f.getAbsolutePath() + ".csv");
               }
               double profitLoss = PnLCalculator.INSTANCE.calculateToCsvFile(pnls, f);

               JOptionPane.showMessageDialog(this, profitLoss >= 0 ? "Nett profit: " + Util.formatPrice(
                                                profitLoss) : "Nett loss: " + Util.formatPrice(profitLoss), "Saved to " + f.getName(),
                                             JOptionPane.INFORMATION_MESSAGE
               );
            }
         }
      } catch (Exception e) {
         log.warn(e);
      }
   }

   protected void applyStrategy() {
      try {
         testStrategy();

         StrategyParams param = createParam();

         String strategyName = String.valueOf(strategies.getSelectedItem());
         chartView.setStrategy(strategyName, param);
         BuySellStrategy.toggleGraphic();
      } catch (Exception e) {
         log.warn(e);
      }
   }


   private StrategyParams createParam() {
      return new StrategyParams(toValues(values.getText()), parseDouble(takeProfit.getText()),
                                parseDouble(cutLoss.getText()), parseDouble(dynCutLoss.getText())
      );
   }

   private double parseDouble(String text) {
      try {
         return Double.parseDouble(text);
      } catch (Exception e) {
         return 0d;
      }
   }

   public void onPreferredSizeCalculated(Dimension dimension) {
      lastWidth = dimension.width;
   }


   public void setStrategyName(String strategyName) {
      if (BuySellStrategyFactory.getInstance().exists(strategyName)) {
         strategies.setSelectedItem(strategyName);
      }
   }


   public void setStrategyParam(StrategyParams strategyParams) {
      DecimalFormat df = new DecimalFormat("0.0000");
      String values = toString(strategyParams.getStrategyValues());
      this.values.setText(values);
      this.takeProfit.setText(df.format(strategyParams.getTakeProfit()));
      this.cutLoss.setText(df.format(strategyParams.getCutLoss()));
      this.dynCutLoss.setText(df.format(strategyParams.getDynamicCutLoss()));
   }


   public void setMarketDataView(ChartView chartView) {
      StrategyFrame.chartView = chartView;

   }

   public static BuySellStrategy createDefaultStrategy(String strategyName, StrategyParams strategyParams) {
      return (strategyName == null) ? new NoBuySellStrategy(strategyParams) : BuySellStrategyFactory.getInstance()
                                                                                                    .createStrategy(
                                                                                                       strategyName,
                                                                                                       strategyParams
                                                                                                    );
   }

   private String toString(int[] values) {
      if (values != null) {
         StringBuilder sb = new StringBuilder();
         for (int v : values) {
            sb.append(v).append(" ");
         }
         return sb.toString();
      }
      return "";
   }

   private int[] toValues(String text) {
      String[] vals = text.trim().split(" ");
      ArrayList<Integer> lst = new ArrayList<Integer>();
      for (String v : vals) {
         v = v.trim();
         if (v.length() > 0) {
            lst.add(Integer.parseInt(v));
         }
      }

      int[] v = new int[lst.size()];
      for (int i = 0; i < v.length; i++) {
         v[i] = lst.get(i);
      }
      return v;
   }

   class PnLListener implements ITradePositionListener {

      ArrayList<PnL> list;

      PnLListener() {
         list = new ArrayList<PnL>();
      }

      public void openPosition(
         BuySellStrategy source, MarketDataSource data, TradePosition newPosition, double cutLoss, double takeProfit
      ) {
         add(newPosition);
      }

      public void closePosition(
         BuySellStrategy source, MarketDataSource data, TradePosition prevPosition, TradePosition reversalPosition,
         double profit
      ) {
         add(reversalPosition);
      }

      private void add(TradePosition newPosition) {
         list.add(new PnL(newPosition.getTick().getTimestamp(),
                          newPosition.getDirection() == SignalDirection.BUY ? newPosition.getTick()
                                                                                         .getBuyPrice() * -1 : newPosition
                             .getTick().getSellPrice()
         ));
      }

      PnL[] getRecords() {
         return list.toArray(new PnL[list.size()]);
      }

   }

}
