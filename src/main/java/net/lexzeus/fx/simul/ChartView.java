package net.lexzeus.fx.simul;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JPanel;

import net.lexzeus.fx.simul.MarketDataSource.ChartIntervalType;
import net.lexzeus.fx.simul.indicators.CandleStick;
import net.lexzeus.fx.simul.indicators.LineTechIndicator;
import net.lexzeus.fx.simul.indicators.TechnicalIndicator;
import net.lexzeus.fx.simul.strategies.BuySellStrategy;
import net.lexzeus.fx.simul.strategies.BuySellStrategyFactory;
import net.lexzeus.fx.simul.strategies.StrategyParams;
import net.lexzeus.util.Log;

/**
 * The "chart" for displaying market data series, technical indicators, and strategy.
 *
 * @author Alexander Koentjara
 */
public class ChartView extends JPanel {

   private static final long serialVersionUID = 4482664068336371287L;
   private static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);
   private static final Color DARK = new Color(50, 50, 50);

   private static final int VERT_MARIGIN = 30;
   private static final int HORZ_MARIGIN = 50;
   private static final Color VERY_DARK = new Color(40, 0, 0);
   private final Log log = Log.get(this);

   private boolean isStale = true;
   private MarketDataSource marketDataSource;
   private ArrayList<TechnicalIndicator> techIndicators = new ArrayList<TechnicalIndicator>();
   private boolean isCandleStickVisible = true;
   private boolean isCloseLineVisible = true;
   private Date startPeriod;
   private Date endPeriod;
   private BufferedImage buffImg;
   private int intervalValue;
   private ChartIntervalType intervalType;
   private Tick[] ticks;
   private int mouseX = 0;
   private int mouseY = 0;
   private ArrayList<PnL> pnls;
   private Date timeAtMouse = null;
   private double priceAtMouse = 0d;
   private transient BuySellStrategy buySellStrategy;
   private String buySellStrategyName = BuySellStrategyFactory.NONE;
   private StrategyParams strategyParams = new StrategyParams(new int[]{5, 50}, 0.0100d, 0.0010d, 0.0050d);
   private Date startTimeDrag = null;

   /**
    * Default constructor
    */
   public ChartView(final IMainFrame mainFrame) {
      super();
      clearTechnicalIndicators();
      addMouseMotionListener(new MouseMotionListener() {
         public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
         }

         public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
         }

      });
      addMouseListener(new MouseAdapter() {
         long timeStamp = 0;

         @Override
         public void mousePressed(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
               startTimeDrag = timeAtMouse;
            }
         }

         @Override
         public void mouseReleased(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK && startTimeDrag != null && timeAtMouse != null && !startTimeDrag.equals(
               timeAtMouse)) {
               Date minDate = startTimeDrag.before(timeAtMouse) ? startTimeDrag : timeAtMouse;
               Date maxDate = startTimeDrag.before(timeAtMouse) ? timeAtMouse : startTimeDrag;
               if (marketDataSource.getMarketData(minDate, maxDate).length > 2) {
                  mainFrame.getChartTool().setMarketDataStartTimestamp(minDate);
                  mainFrame.getChartTool().setMarketDataStopTimestamp(maxDate);
                  mainFrame.refresh();
               }
            }
            startTimeDrag = null;
         }

         @Override
         public void mouseClicked(MouseEvent e) {
            if (marketDataSource != null) {
               if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
                  long now = System.currentTimeMillis();
                  if (now - timeStamp < 500 && marketDataSource != null) {
                     Tick[] tck = marketDataSource.getMarketData();
                     Date minDate = tck[0].getTimestamp();
                     Date maxDate = tck[tck.length - 1].getTimestamp();
                     if (tck != null && ticks.length > 0 && marketDataSource.getMarketData(
                        minDate, maxDate).length > 2) {
                        mainFrame.getChartTool().setMarketDataStartTimestamp(minDate);
                        mainFrame.getChartTool().setMarketDataStopTimestamp(maxDate);
                        mainFrame.refresh();
                     }
                  }
                  timeStamp = now;
               } else {
                  if (ChartView.this.pnls != null) {
                     if (e.getButton() == MouseEvent.BUTTON1) {
                        ChartView.this.buyAtMouseLocation();
                     } else {
                        ChartView.this.sellAtMouseLocation();
                     }
                  } else {
                     if (e.getButton() == MouseEvent.BUTTON1) {
                        boolean createNew = StrategyFrame.INSTANCE == null;
                        StrategyFrame frame =
                           StrategyFrame.INSTANCE == null ? new StrategyFrame() : StrategyFrame.INSTANCE;
                        frame.setStrategyName(buySellStrategyName);
                        frame.setStrategyParam(strategyParams);
                        frame.setMarketDataView(ChartView.this);

                        if (createNew) {
                           frame.pack();
                        }

                        PointerInfo info = MouseInfo.getPointerInfo();
                        frame.setLocation(info.getLocation());
                        frame.setVisible(true);
                     }
                  }
               }
            }
         }

      });
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(800, 600);
   }

   /**
    * Toggle the visibility of closing price line in the chart
    *
    * @param state
    * @return
    */
   public boolean setCloseLineVisible(boolean state) {
      if (this.isCloseLineVisible != state) {
         this.isCloseLineVisible = state;
         this.isStale = true;
         return true;
      }
      return false;
   }

   /**
    * Toggle the visibility of candle stick bars in the chart
    *
    * @param state
    * @return
    */
   public boolean setCandleStickVisible(boolean state) {
      if (this.isCandleStickVisible != state) {
         this.isCandleStickVisible = state;
         this.isStale = true;
         return true;
      }
      return false;
   }

   /**
    * Set the market data used in this chart
    *
    * @param marketData
    */
   public void setMarketData(MarketDataSource marketData) {
      this.marketDataSource = marketData;
      this.isStale = true;
   }

   /**
    * Set the market data period viewable by this chart
    *
    * @param startPeriod
    * @param endPeriod
    */
   public void setPeriod(Date startPeriod, Date endPeriod) {
      this.startPeriod = startPeriod;
      this.endPeriod = endPeriod;
      this.isStale = true;
   }

   /**
    * Set the time interval of individual candle stick bar / point in chart.
    *
    * @param intervalType
    * @param intervalValue
    */
   public void setInterval(MarketDataSource.ChartIntervalType intervalType, int intervalValue) {
      this.intervalType = intervalType;
      this.intervalValue = intervalValue;
      this.isStale = true;
   }

   /**
    * Set the buy sell strategy name and the parameter used for this chart
    *
    * @param strategyName
    * @param param
    */
   public void setStrategy(String strategyName, StrategyParams param) {
      this.buySellStrategyName = strategyName;
      this.strategyParams = param;
      isStale = true;
      refresh();
   }

   /**
    * Add/replace technical indicator into this chart. If existing technical indicator is already exist with same
    * parameters (but having different color), the operation will be replace.
    *
    * @param indicator
    * @return
    */
   public boolean addTechnicalIndicator(TechnicalIndicator indicator) {
      synchronized (techIndicators) {
         if (!techIndicators.contains(indicator)) {
            techIndicators.add(indicator);
            this.isStale = true;
            return true;
         } else {
            techIndicators.remove(indicator);
            techIndicators.add(indicator);
            this.isStale = true;
            return true;
         }
      }
   }

   /**
    * Remove the technical indicator from this chart
    *
    * @param strat
    * @return
    */
   public boolean removeTechnicalIndicator(TechnicalIndicator strat) {
      synchronized (techIndicators) {
         if (techIndicators.contains(strat)) {
            techIndicators.remove(strat);
            this.isStale = true;
            return true;
         }
      }
      return false;
   }

   /**
    * To remove all technical indicators.
    */
   public void clearTechnicalIndicators() {
      synchronized (techIndicators) {
         techIndicators.clear();
         this.isStale = true;
      }
   }

   /**
    * To request repaint operation on this chart
    */
   public void refresh() {
      if (this.marketDataSource != null) {
         repaint();
      }
   }

   @Override
   public void paint(Graphics canvasGraphic) {
      canvasGraphic.setFont(DEFAULT_FONT);

      if (this.marketDataSource == null) {
         canvasGraphic.setColor(Color.BLACK);
         canvasGraphic.fillRect(0, 0, this.getWidth(), this.getHeight());
         canvasGraphic.setColor(Color.YELLOW);
         canvasGraphic.drawString("Please load market data using menu File - Open Market Data...", 20, 20);
      } else {
         try {
            if (buySellStrategy == null || isStale) {
               if (buySellStrategy != null) {
                  this.marketDataSource.removeMarketMoveListener(buySellStrategy);
               }

               buySellStrategy = StrategyFrame.createDefaultStrategy(buySellStrategyName, strategyParams);
               buySellStrategy.setMarketData(this.marketDataSource);
               buySellStrategy.setInterval(this.intervalType, this.intervalValue);
               buySellStrategy.initialize();
               isStale = true;
            }

            ArrayList<TechnicalIndicator> indicatorCopy;
            synchronized (techIndicators) {
               indicatorCopy = new ArrayList<TechnicalIndicator>(techIndicators);
            }

            if (isCloseLineVisible && !indicatorCopy.contains(LineTechIndicator.INSTANCE)) {
               indicatorCopy.add(LineTechIndicator.INSTANCE);
            }

            BufferedImage image = buffImg;
            int wh = this.getWidth();
            int hg = this.getHeight();

            if (image != null && (image.getWidth() != wh || image.getHeight() != hg)) {
               image = null;
               isStale = true;
            }

            if (image == null) {
               image = new BufferedImage(wh, hg, BufferedImage.TYPE_INT_BGR);
            }

            Graphics tempGraph = image.getGraphics();
            if (isStale) {
               tempGraph.setColor(Color.BLACK);
               tempGraph.fillRect(0, 0, wh, hg);

               ticks = marketDataSource.getMarketData(startPeriod, endPeriod, intervalType, intervalValue);
            }

            if (ticks != null && ticks.length > 0) {
               timeAtMouse = null;
               priceAtMouse = 0d;
               double tmax = 0;
               double tmin = Integer.MAX_VALUE;

               for (Tick t : ticks) {
                  tmax = Math.max(t.getHigh(), tmax);
                  tmin = Math.min(t.getLow(), tmin);
               }

               double tadder = (tmax - tmin) * 0.1d;
               tmax = tmax + tadder;
               tmin = tmin - tadder;
               final double max = tmax;
               final double min = tmin;
               final DecimalFormat decFormat = new DecimalFormat("0.0000");

               final int width = this.getWidth() - HORZ_MARIGIN * 2;
               final int height = this.getHeight() - VERT_MARIGIN;

               final double hdistance = max - min;
               final double vincreament = height / hdistance;
               int step = (int) (ticks.length / width);
               step = step < 1 ? 1 : step;

               final double startPlot = ticks[0].getTimestamp().getTime();
               final double plotLength =
                  ticks[ticks.length - 1].getTimestamp().getTime() - ticks[0].getTimestamp().getTime();

               for (int ctr = 0; ctr < ticks.length; ctr += step) {
                  Tick t = ticks[ctr];
                  double x =
                     (((((double) t.getTimestamp().getTime()) - startPlot) / plotLength) * width) + HORZ_MARIGIN;
                  if ((int) x < mouseX) {
                     timeAtMouse = t.getTimestamp();
                  }
               }

               PricePlotter plotter = new PricePlotter() {
                  public Point plotPriceAt(double price, Date date) {
                     if (plotLength == 0) {
                        return new Point(0, 0);
                     }

                     double x = (((((double) date.getTime()) - startPlot) / plotLength) * width) + HORZ_MARIGIN;
                     double y = calculateY(vincreament, price, min);
                     return new Point((int) x, (int) y);
                  }

                  public Point plotScaleAt(double scale, Date timestamp) {
                     double deltaRatio = (max - min) / 100d;
                     scale = scale < 0 ? 0 : scale > 100 ? 100 : scale;

                     return plotPriceAt((scale * deltaRatio) + min, timestamp);
                  }
               };

               if (isStale) {
                  tempGraph.setColor(DARK);

                  boolean isScaleRequired = buySellStrategy instanceof IMomentumType;
                  if (!isScaleRequired) {
                     for (TechnicalIndicator s : indicatorCopy) {
                        if (s instanceof IMomentumType) {
                           isScaleRequired = true;
                           break;
                        }
                     }
                  }

                  if (isScaleRequired) {
                     for (int i = 0; i < 11; i++) {
                        int y = 1 + (int) (height / 10d * (double) i);
                        tempGraph.drawLine(HORZ_MARIGIN, y, getWidth(), y);
                        tempGraph.drawString("" + (100 - (10 * i)), 10, i == 0 ? 10 : i == 10 ? y : y + 5);
                     }
                  }

                  double vstep = (max - min) / 20d;
                  double priceStep = max;
                  for (int ctr = 1; ctr < 20; ctr++) {
                     priceStep -= vstep;
                     Point p = plotter.plotPriceAt(priceStep, ticks[0].getTimestamp());
                     tempGraph.setColor(VERY_DARK);
                     tempGraph.drawLine(HORZ_MARIGIN, p.y, getWidth() - HORZ_MARIGIN, p.y);

                     tempGraph.setColor(Color.WHITE);
                     tempGraph.drawString(decFormat.format(priceStep), getWidth() - HORZ_MARIGIN, p.y + 5);
                  }

                  for (TechnicalIndicator s : indicatorCopy) {
                     s.applyIndicator(
                        startPeriod, endPeriod, marketDataSource, plotter, tempGraph, intervalType, intervalValue);
                  }

                  if (this.isCandleStickVisible) {
                     new CandleStick().applyIndicator(startPeriod, endPeriod, this.marketDataSource, plotter, tempGraph,
                                                      intervalType, intervalValue
                     );

                  }

                  buySellStrategy.executeStrategy(startPeriod, endPeriod, tempGraph, plotter);
               }

               // drawing on the real canvas...
               canvasGraphic.drawImage(image, 0, 0, this);

               canvasGraphic.setColor(Color.WHITE);
               int xline =
                  mouseX < HORZ_MARIGIN ? HORZ_MARIGIN : mouseX > getWidth() - HORZ_MARIGIN ? getWidth() - HORZ_MARIGIN : mouseX;
               canvasGraphic.drawLine(xline, 0, xline, height);
               canvasGraphic.drawLine(HORZ_MARIGIN, mouseY > height ? height : mouseY, getWidth() - HORZ_MARIGIN,
                                      mouseY > height ? height : mouseY
               );

               priceAtMouse = (((double) (this.getHeight() - VERT_MARIGIN - mouseY)) / vincreament) + min;

               String dateStr = Util.formatDate(timeAtMouse);
               canvasGraphic.drawString(
                  "Date :" + dateStr + "  Price: " + decFormat.format(priceAtMouse), 5, getHeight() - 5);

               int posx = 5;
               for (TechnicalIndicator s : indicatorCopy) {
                  canvasGraphic.setColor(s.getColor());
                  canvasGraphic.fillRect(posx, getHeight() - 28, 8, 8);
                  canvasGraphic.setColor(Color.WHITE);
                  canvasGraphic.drawString(s.toString(), posx + 8 + 4, getHeight() - 20);

                  posx += (int) canvasGraphic.getFontMetrics().getStringBounds(s.toString(), canvasGraphic)
                                             .getWidth() + 17;
               }

               if (startTimeDrag != null && timeAtMouse != null) {
                  Date minDate = startTimeDrag.before(timeAtMouse) ? startTimeDrag : timeAtMouse;
                  Date maxDate = startTimeDrag.before(timeAtMouse) ? timeAtMouse : startTimeDrag;

                  Point from = plotter.plotPriceAt(max, minDate);
                  Point to = plotter.plotPriceAt(max, maxDate);
                  canvasGraphic.setColor(new Color(255, 255, 255, 128));
                  canvasGraphic.fillRect(from.x, 0, to.x - from.x, getHeight() - VERT_MARIGIN);
               }
            }

            buffImg = image;
            isStale = false;

         } catch (Exception e) {
            log.warn("Error at rendering", e);
         }
      }
   }

   /**
    * To start recording the PnL
    */
   public void startPnL() {
      pnls = new ArrayList<PnL>();
   }

   /**
    * To stop recording the PnL
    *
    * @return
    */
   public PnL[] stopPnL() {
      try {
         if (pnls != null) {
            return pnls.toArray(new PnL[pnls.size()]);
         } else {
            return new PnL[0];
         }
      } finally {
         pnls = null;
      }
   }

   private void buyAtMouseLocation() {
      if (timeAtMouse != null) {
         pnls.add(new PnL(timeAtMouse, -priceAtMouse));
      }
   }

   private void sellAtMouseLocation() {
      if (timeAtMouse != null) {
         pnls.add(new PnL(timeAtMouse, priceAtMouse));
      }
   }

   private double calculateY(double vincreament, double pos, double min) {
      return this.getHeight() - VERT_MARIGIN - (vincreament * (pos - min));
   }

   /**
    * Create SnapshotData instance out of charting information available in this chart
    *
    * @return
    */
   public SnapshotData createSnapshot() {
      return new SnapshotData(this.marketDataSource, this.startPeriod, this.endPeriod,
                              techIndicators.toArray(new TechnicalIndicator[techIndicators.size()]),
                              this.isCandleStickVisible, this.isCloseLineVisible, this.intervalType, this.intervalValue,
                              this.strategyParams, this.buySellStrategyName
      );
   }

}
