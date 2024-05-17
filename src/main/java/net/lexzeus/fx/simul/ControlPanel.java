package net.lexzeus.fx.simul;

import net.lexzeus.fx.simul.MarketDataSource.ChartIntervalType;
import net.lexzeus.fx.simul.indicators.TechnicalIndicator.PriceType;
import net.lexzeus.fx.simul.indicators.TechnicalIndicatorFactory;
import net.lexzeus.util.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;

public class ControlPanel extends JPanel implements IControlPanel, ISizeCallback {

   private static final long serialVersionUID = -3430915925608055415L;

   //private int lastHeight;
   private int lastWidth;

   private JTextField start;
   private JTextField stop;
   private JTextField interval;
   private JComboBox intervalType;
   private JCheckBox candle;
   private JCheckBox line;

   private JComboBox indicators;
   private JComboBox techPriceType;
   private JPanel techColor;
   private JTextField techParams;

   private JToggleButton pnlRecord;
   private final Log log = Log.get(this);

   public ControlPanel(final IMainFrame mainFrame) {
      this.setLayout(new FixedLayout(900, 55));
      this.setBackground(Color.DARK_GRAY);

      GuiUtil.setSizeCallback(this);

      //////////////////////////////////////////////////////
      // 						LEFT 						//
      //////////////////////////////////////////////////////
      JPanel left = new JPanel();
      left.setBackground(Color.DARK_GRAY);
      left.setLayout(new FixedLayout(300, 50));

      left.add("1,3", GuiUtil.createLabel("From", GuiUtil.BOLD_FONT, Color.YELLOW, Color.DARK_GRAY));
      int posX = 3 + lastWidth;
      left.add("1,25", GuiUtil.createLabel("To", GuiUtil.BOLD_FONT, Color.YELLOW, Color.DARK_GRAY));

      this.start = GuiUtil.createTextField("DD/MM/YYYY HH:MM:SS", GuiUtil.NORMAL_FONT, Color.YELLOW, Color.BLACK);
      left.add(posX + ",3", start);

      this.stop = GuiUtil.createTextField("DD/MM/YYYY HH:MM:SS", GuiUtil.NORMAL_FONT, Color.YELLOW, Color.BLACK);
      left.add(posX + ",25", stop);
      posX += lastWidth + 5;

      this.interval = GuiUtil.createTextField("000", GuiUtil.NORMAL_FONT, Color.YELLOW, Color.BLACK);
      interval.setText("1");
      left.add(posX + ",3", interval);

      this.intervalType = GuiUtil.createCombo(new String[]{"TICK", "MIN", "HOUR", "DAY"}, GuiUtil.NORMAL_FONT,
                                              Color.BLACK, Color.YELLOW
      );
      left.add(posX + ",23", intervalType);
      posX += lastWidth;

      this.line = GuiUtil.createCheckBox("LINE", GuiUtil.NORMAL_FONT, Color.YELLOW, Color.DARK_GRAY);
      line.setSelected(true);
      left.add(posX + ",1", line);

      this.candle = GuiUtil.createCheckBox("CANDLE", GuiUtil.NORMAL_FONT, Color.YELLOW, Color.DARK_GRAY);
      candle.setSelected(true);
      left.add(posX + ",25", candle);

      this.add("0,3", left);


      //////////////////////////////////////////////////////
      // 						CENTER 						//
      //////////////////////////////////////////////////////
      JPanel center = new JPanel();
      center.setBackground(Color.DARK_GRAY);
      center.setLayout(new FixedLayout(300, 50));

      this.techPriceType = GuiUtil.createCombo(new String[]{"Close", "Open", "High", "Low"}, GuiUtil.NORMAL_FONT,
                                               Color.BLACK, Color.GREEN
      );
      center.add("1,0", techPriceType);
      posX = 3 + lastWidth;

      this.indicators = GuiUtil.createCombo(TechnicalIndicatorFactory.getInstance().getAllTechnicalIndicatorNames(),
                                            GuiUtil.NORMAL_FONT, Color.BLACK, Color.GREEN
      );
      center.add(posX + ",0", indicators);
      posX += 3 + lastWidth;

      this.techColor = new JPanel();
      techColor.setLayout(new FixedLayout(20, 20));
      techColor.setBackground(Color.GREEN);
      techColor.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.BLUE));
      techColor.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            Color newColor = JColorChooser.showDialog(techColor, "Choose Technical Indicator Color",
                                                      techColor.getBackground()
            );

            techColor.setBackground(newColor);
         }
      });
      center.add(posX + ",1", techColor);
      posX += 3 + 20;

      this.techParams = GuiUtil.createTextField("", GuiUtil.NORMAL_FONT, Color.GREEN, Color.BLACK, 100);
      techParams.setText("10");
      center.add(posX + ",1", techParams);

      center.add("1,23", GuiUtil.createButton("Add", (e) -> mainFrame.addSelectedTechnicalIndicator()));
      posX = 3 + lastWidth;

      center.add(posX + ",23", GuiUtil.createButton("Remove", (e) -> mainFrame.removeSelectedTechnicalIndicator()));
      posX += 3 + lastWidth;

      center.add(posX + ",23", GuiUtil.createButton("Clear", (e) -> mainFrame.clearAllTechnicalIndicator()));
      this.add("350,3", center);


      //////////////////////////////////////////////////////
      // 						RIGHT 						//
      //////////////////////////////////////////////////////
      JPanel right = new JPanel();
      right.setBackground(Color.DARK_GRAY);
      right.setLayout(new FixedLayout(300, 50));

      right.add("1,3", GuiUtil.createButton("REFRESH", (e) -> mainFrame.refresh()));

      this.pnlRecord = GuiUtil.createToggleButton("Record P&L", (e) -> {
         if (pnlRecord.isSelected()) {
            mainFrame.startRecordProfitAndLoss();
            pnlRecord.setForeground(Color.RED);
         } else {
            mainFrame.stopRecordProfitAndLoss();
            pnlRecord.setForeground(Color.BLACK);
         }
      });
      right.add((3 + lastWidth) + ",3", pnlRecord);
      this.add("650,3", right);
   }

   public void onPreferredSizeCalculated(Dimension dimension) {
      lastWidth = dimension.width;
      //lastHeight = dimension.height;
   }

   public void setCandleStickVisible(boolean isVisible) {
      this.candle.setSelected(isVisible);
   }

   public void setChartIntervalType(ChartIntervalType intervalType) {
      switch (intervalType) {
         //Tick, Minute, Hour, Day
         case TICK:
            this.intervalType.setSelectedIndex(0);
            break;
         case MINUTE:
            this.intervalType.setSelectedIndex(1);
            break;
         case HOUR:
            this.intervalType.setSelectedIndex(2);
            break;
         case DAY:
            this.intervalType.setSelectedIndex(3);
            break;
         default:
            this.intervalType.setSelectedIndex(0);
            break;
      }
   }

   public void setChartIntervalValue(int intervalValue) {
      interval.setText(intervalValue + "");
   }

   public void setClosingPriceLineVisible(boolean isVisible) {
      line.setSelected(isVisible);
   }

   public void setMarketDataStartTimestamp(Date date) {
      start.setText(Util.formatDate(date));
   }

   public void setMarketDataStopTimestamp(Date date) {
      stop.setText(Util.formatDate(date));
   }

   public ChartIntervalType getChartIntervalType() {
      switch (intervalType.getSelectedIndex()) {
         //Tick, Minute, Hour, Day
         case 0:
            return ChartIntervalType.TICK;
         case 1:
            return ChartIntervalType.MINUTE;
         case 2:
            return ChartIntervalType.HOUR;
         case 3:
            return ChartIntervalType.DAY;
         default:
            return ChartIntervalType.TICK;
      }
   }

   public int getChartIntervalValue() {
      try {
         int v = Integer.parseInt(interval.getText().trim());
         return v < 1 ? 1 : v;
      } catch (Exception e) {
         log.warn(e);
         return 1;
      }
   }

   public Date getMarketDataStartTimestamp() {
      try {
         return Util.parseDate(start.getText().trim());
      } catch (Exception e) {
         log.warn(e);
         return new Date();
      }
   }

   public Date getMarketDataStopTimestamp() {
      try {
         return Util.parseDate(stop.getText().trim());
      } catch (Exception e) {
         log.warn(e);
         return new Date();
      }
   }

   public boolean isCandleStickVisible() {
      return candle.isSelected();
   }

   public boolean isClosingPriceLineVisible() {
      return line.isSelected();
   }

   public String getSelectedTechnicalIndicatorName() {
      return String.valueOf(indicators.getSelectedItem());
   }

   public Color getTechnicalIndicatorColor() {
      return techColor.getBackground();
   }

   public PriceType getTechnicalIndicatorPriceType() {
      switch (techPriceType.getSelectedIndex()) {
         //Tick, Minute, Hour, Day
         case 0:
            return PriceType.Close;
         case 1:
            return PriceType.Open;
         case 2:
            return PriceType.High;
         case 3:
            return PriceType.Low;
         default:
            return PriceType.Close;
      }
   }

   /**
    * Split the text found in the technical value field by space, and return them as int[]. When exception is
    * encountered during parsing String to int, this method will simply return new int[] { 10 }.
    *
    * @return the technical indicator values
    */
   public int[] getTechnicalIndicatorValues() {
      try {
         String txt = techParams.getText().trim();
         String[] ss = txt.split(" ");
         ArrayList<Integer> list = new ArrayList<Integer>();
         for (String s : ss) {
            Integer v = Integer.valueOf(s);
            list.add(v);
         }

         int[] retVal = new int[list.size()];
         for (int i = 0; i < retVal.length; i++) {
            retVal[i] = list.get(i);
         }

         return retVal;
      } catch (Exception e) {
         log.warn(e);
         return new int[]{10};
      }
   }

}
