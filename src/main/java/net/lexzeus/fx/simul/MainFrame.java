package net.lexzeus.fx.simul;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.lexzeus.fx.simul.indicators.TechnicalIndicator;
import net.lexzeus.fx.simul.indicators.TechnicalIndicatorFactory;

import static net.lexzeus.fx.simul.Configuration.MARKET_DATA_DIR_KEY;
import static net.lexzeus.fx.simul.Configuration.getProperty;
import static net.lexzeus.fx.simul.Util.defaultIfNull;

public class MainFrame extends JFrame implements IMainFrame {

   public static void main(String[] args) {
      MainFrame f = new MainFrame();
      f.pack();
      f.setLocationRelativeTo(null);
      f.setVisible(true);
   }

   private static final long serialVersionUID = 4125238764597052535L;
   private static final String VERSION = getProperty("version");
   private static final String ABOUT =
      "<html><body>&nbsp;<b>Trading Strategy Runner</b><br>&nbsp;Ver " + VERSION + "<br><br>&nbsp;Licensed under MIT License" + "<br><br>&nbsp;Copyright (c) 2024 Alexander Yanuar Koentjara<br><br>&nbsp;" + "<a href='https://github.com/tummybunny/tradingstrategy'>https://github.com/tummybunny/tradingstrategy</a>" + "<br><br></body></html>";

   private ChartView chartView;
   private IControlPanel controlPanel;
   private String marketDataDir;

   public MainFrame() {
      super("Trading Strategy Runner v" + getProperty("version", "1.0.0"));
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setSize(new Dimension(200, 100));
      createMenuBar();
      createUpperComponent();
      createCenterComponent();
   }

   private void createMenuBar() {
      JMenuBar menuBar = new JMenuBar();

      JMenu fileMenu = new JMenu("File");
      fileMenu.setMnemonic(KeyEvent.VK_F);
      fileMenu.add(createMenuItem("Open Market Data", KeyStroke.getKeyStroke('O', ActionEvent.CTRL_MASK), 'O',
                                  (e) -> openMarketData()
      ));
      fileMenu.addSeparator();
      fileMenu.add(createMenuItem("Load Snapshot", KeyStroke.getKeyStroke('L', ActionEvent.CTRL_MASK), 'L',
                                  (e) -> loadSnapshot()
      ));
      fileMenu.add(createMenuItem("Save Snapshot", KeyStroke.getKeyStroke('S', ActionEvent.CTRL_MASK), 'S',
                                  (e) -> saveSnapshot()
      ));
      fileMenu.addSeparator();
      fileMenu.add(
         createMenuItem("Exit", KeyStroke.getKeyStroke('X', ActionEvent.CTRL_MASK), 'x', (e) -> System.exit(0)));
      menuBar.add(fileMenu);

      JMenu helpMenu = new JMenu("Help");
      helpMenu.setMnemonic(KeyEvent.VK_H);
      helpMenu.add(createMenuItem("About", null, 'A', (e) -> {
         JOptionPane.showMessageDialog(MainFrame.this, ABOUT, "About Trading Strategy Runner",
                                       JOptionPane.INFORMATION_MESSAGE
         );
      }));

      helpMenu.add(
         createMenuItem("Help", KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), 'H', (e) -> new HelpFrame()));
      menuBar.add(helpMenu);
      this.setJMenuBar(menuBar);
   }

   private JMenuItem createMenuItem(String title, KeyStroke stroke, Character mnemonic, ActionListener actionListener) {
      JMenuItem item = new JMenuItem(title);
      if (stroke != null) {
         item.setAccelerator(stroke);
      }
      if (mnemonic != null) {
         item.setMnemonic(mnemonic);
      }
      item.addActionListener(actionListener);
      return item;
   }

   private void createCenterComponent() {
      Container c = this.getContentPane();
      this.chartView = new ChartView(this);
      c.add(chartView, BorderLayout.CENTER);
   }

   private void createUpperComponent() {
      Container c = this.getContentPane();
      this.controlPanel = new ControlPanel(this);
      c.add((ControlPanel) controlPanel, BorderLayout.NORTH);
   }

   public ChartView getChartView() {
      return this.chartView;
   }

   public IControlPanel getChartTool() {
      return this.controlPanel;
   }


   public void saveSnapshot() {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileFilter(new FileFilter() {

         public boolean accept(File f) {
            return f.getAbsolutePath().toLowerCase().endsWith(".snap") || f.isDirectory();
         }

         @Override
         public String getDescription() {
            return "Market data snapshot file (*.snap)";
         }
      });
      chooser.setDialogTitle("Save Snapshot File");

      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
         File f = chooser.getSelectedFile();
         if (!f.getAbsolutePath().toLowerCase().endsWith(".snap")) {
            f = new File(f.getAbsolutePath() + ".snap");
         }
         SnapshotData data = chartView.createSnapshot();
         SnapshotData.persistToFile(f, data);
      }
   }

   public void loadSnapshot() {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileFilter(new FileFilter() {

         public boolean accept(File f) {
            return f.getAbsolutePath().toLowerCase().endsWith(".snap") || f.isDirectory();
         }

         @Override
         public String getDescription() {
            return "Market data snapshot file (*.snap)";
         }
      });
      chooser.setDialogTitle("Load Snapshot File");

      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
         File f = chooser.getSelectedFile();
         SnapshotData data = SnapshotData.loadFromFile(f);
         if (data != null) {
            this.controlPanel.setMarketDataStartTimestamp(data.getStart());
            this.controlPanel.setMarketDataStopTimestamp(data.getStop());
            this.controlPanel.setCandleStickVisible(data.isCandle());
            this.controlPanel.setClosingPriceLineVisible(data.isClosingLine());
            this.controlPanel.setChartIntervalValue(data.getIntervalValue());
            this.controlPanel.setChartIntervalType(data.getIntervalType());

            MarketDataSource md = data.getMarketData();
            TechnicalIndicator[] strats = data.getStrategies();

            this.chartView.setMarketData(md);
            this.chartView.clearTechnicalIndicators();
            if (strats != null) {
               for (TechnicalIndicator s : strats) {
                  this.chartView.addTechnicalIndicator(s);
               }
            }
            this.chartView.setStrategy(data.getStrategyClass(), data.getStrategyParam());
            refresh();
         }
      }

   }

   public void openMarketData() {
      JFileChooser chooser = new JFileChooser(defaultIfNull(marketDataDir, getProperty(MARKET_DATA_DIR_KEY)));
      chooser.setFileFilter(new FileFilter() {
         @Override
         public boolean accept(File f) {
            return f.getAbsolutePath().toLowerCase().endsWith(".csv") || f.isDirectory();
         }

         @Override
         public String getDescription() {
            return "CSV market data file (*.csv)";
         }
      });
      chooser.setDialogTitle("Open Market Data File");

      if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
         File f = chooser.getSelectedFile();

         marketDataDir = f.getPath();

         MarketDataSource md = MarketDataSource.fromFile(f);
         chartView.setMarketData(md);
         Tick[] tk = md.getMarketData();
         if (tk != null && tk.length > 0) {
            controlPanel.setMarketDataStartTimestamp(tk[0].getTimestamp());
            controlPanel.setMarketDataStopTimestamp(tk[tk.length - 1].getTimestamp());
         }
         refresh();
      }
   }

   public void refresh() {
      chartView.setInterval(controlPanel.getChartIntervalType(), controlPanel.getChartIntervalValue());
      chartView.setPeriod(controlPanel.getMarketDataStartTimestamp(), controlPanel.getMarketDataStopTimestamp());
      chartView.setCandleStickVisible(controlPanel.isCandleStickVisible());
      chartView.setCloseLineVisible(controlPanel.isClosingPriceLineVisible());
      chartView.refresh();
   }

   public void startRecordProfitAndLoss() {
      chartView.startPnL();
   }

   public void stopRecordProfitAndLoss() {
      PnL[] pnls = chartView.stopPnL();
      if (pnls.length > 0) {
         JFileChooser jf = new JFileChooser();
         jf.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
               return f.getAbsolutePath().toLowerCase().endsWith(".csv") || f.isDirectory();
            }

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
            PnLCalculator.INSTANCE.calculateToCsvFile(pnls, f);
         }
      }
   }

   public void clearAllTechnicalIndicator() {
      chartView.clearTechnicalIndicators();
      refresh();
   }

   public void addSelectedTechnicalIndicator() {
      TechnicalIndicator ti = createInstance();
      if (chartView.addTechnicalIndicator(ti)) {
         refresh();
      }
   }

   public void removeSelectedTechnicalIndicator() {
      TechnicalIndicator ti = createInstance();
      if (chartView.removeTechnicalIndicator(ti)) {
         chartView.refresh();
      }
   }

   private TechnicalIndicator createInstance() {
      String name = controlPanel.getSelectedTechnicalIndicatorName();
      int[] techIndicatorVals = controlPanel.getTechnicalIndicatorValues();
      Color col = controlPanel.getTechnicalIndicatorColor();
      TechnicalIndicator.PriceType priceType = controlPanel.getTechnicalIndicatorPriceType();

      return TechnicalIndicatorFactory.getInstance().createIndicator(name, techIndicatorVals, priceType, col);
   }

}
