package net.lexzeus.fx.simul.strategies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import net.lexzeus.fx.simul.IMarketMoveListener;
import net.lexzeus.fx.simul.MarketDataSource;
import net.lexzeus.fx.simul.PricePlotter;
import net.lexzeus.fx.simul.Tick;


/**
 * The base class of trading strategy.
 *
 * @author Alexander Koentjara
 */
@SuppressWarnings("unused")
public abstract class BuySellStrategy implements IMarketMoveListener {

   protected static boolean showTechIndicator;

   public static void toggleGraphic() {
      showTechIndicator = !showTechIndicator;
   }

   private static final long serialVersionUID = -8751387013013137204L;

   protected static final Color[] COLORS = new Color[] {
      new Color(75, 75, 0), new Color(0, 75, 75), new Color(75, 0, 75), new Color(50, 50, 50), new Color(150, 0, 0), new Color(0, 150, 0), new Color(0, 0, 150),
      new Color(100, 50, 0), new Color(0, 100, 50), new Color(50, 0, 100), new Color(50, 100, 0), new Color(0, 50, 100), new Color(100, 0, 50),
      new Color(75, 75, 0), new Color(0, 75, 75), new Color(75, 0, 75), new Color(50, 50, 50), new Color(150, 0, 0), new Color(0, 150, 0), new Color(0, 0, 150),
      new Color(100, 50, 0), new Color(0, 100, 50), new Color(50, 0, 100), new Color(50, 100, 0), new Color(0, 50, 100), new Color(100, 0, 50),
      new Color(75, 75, 0), new Color(0, 75, 75), new Color(75, 0, 75), new Color(50, 50, 50), new Color(150, 0, 0), new Color(0, 150, 0), new Color(0, 0, 150),
      new Color(100, 50, 0), new Color(0, 100, 50), new Color(50, 0, 100), new Color(50, 100, 0), new Color(0, 50, 100), new Color(100, 0, 50),
   };

   public static final int MAX_TICK_HISTORY_KEPT = 10000;

   /**
    * The parameter for this trategy
    */
   protected final StrategyParams strategyParams;

   /**
    * The staregy consumer/executor
    */
   protected final ISignalDirectionConsumer strategyRunner;

   private final ConcurrentHashMap<ITradePositionListener, Boolean> listeners =
      new ConcurrentHashMap<ITradePositionListener, Boolean>();
   private MarketDataSource market;
   private MarketDataSource.ChartIntervalType intervalType = MarketDataSource.ChartIntervalType.MINUTE;
   private int intervalValue = 1;
   private SignalDirection oldDirection = SignalDirection.NEUTRAL;
   private long tickCounter;
   private Tick prevTick;

   /**
    * Keep the indicators and historical market data ticks. Accessing tickIndicatorBars directly is discouraged, use
    * getBarAt(int), getPreviousBar(), getCurrentBar(), getTickAt(int), getCurrentTick(), getPreviousTick(), and
    * getPrice()
    *
    * @see #getBarAt(int)
    * @see #getCurrentBar()
    * @see #getPreviousBar()
    * @see #getTickAt(int)
    * @see #getCurrentTick()
    * @see #getPreviousTick()
    * @see #getPrice(int)
    */
   protected ArrayList<TickIndicatorBar> tickIndicatorBars = new ArrayList<TickIndicatorBar>(MAX_TICK_HISTORY_KEPT);

   /**
    * Get the buy sell signals. May return null or empty array
    *
    * @return
    */
   private boolean isInitialized = false;

   /**
    * Hold information of single bar in the chart, that consist of tick (the open, close, high, low prices and volume),
    * and the indicator values associated with that bar.
    */
   public class TickIndicatorBar {

      /**
       * The tick
       */
      public final Tick TICK;

      /**
       * Array that hold indicator values, accessably by index
       */
      public final double[] indexedIndicators;

      /**
       * Create new TickIndicatorBar
       *
       * @param tick            The tick
       * @param numOfIndicators The number of indicators to be contained in single bar
       */
      public TickIndicatorBar(Tick tick, int numOfIndicators) {
         this.TICK = tick;
         indexedIndicators = new double[numOfIndicators];
      }

   }

   /**
    * Default constructor
    *
    * @param strategyParams The strategy parameter
    */
   public BuySellStrategy(StrategyParams strategyParams) {
      this.strategyParams = strategyParams;
      this.strategyRunner = new DefaultAlgorithmicTradeExecutor();
   }

   /**
    * Default constructor
    *
    * @param strategyParams The strategy parameter
    * @param strategyRunner A custom ISignalDirectionConsumer
    */
   public BuySellStrategy(StrategyParams strategyParams, ISignalDirectionConsumer strategyRunner) {
      this.strategyParams = strategyParams;
      this.strategyRunner = strategyRunner;
   }

   /**
    * Strategy has to be initialized first before it can run.
    */
   public final void initialize() {
      isInitialized = true;
      strategyRunner.reset(this, strategyParams);
      oldDirection = SignalDirection.NEUTRAL;
      prevTick = null;
      tickCounter = 0;
      tickIndicatorBars.clear();
      if (_di_tr != null) {
         _di_tr = null;
         _di_dmPlus = null;
         _di_dmMinus = null;
         _di_trN = null;
         _di_dmPlusN = null;
         _di_dmMinusN = null;
         _dx = null;
      }
      initializeImp();
   }

   /**
    * to be implemented if initialization is needed
    */
   protected void initializeImp() {
   }

   protected final boolean isInitialized() {
      return isInitialized;
   }

   /**
    * Set the market data.
    *
    * @param md
    * @throws IllegalStateException when set after initialize()
    */
   public void setMarketData(MarketDataSource md) {
      checkInitialized();
      if (this.market != null && this.market != md) {
         this.market.removeMarketMoveListener(this);
      }
      if (this.market != md) {
         this.market = md;
         md.addMarketMoveListener(this);
      }
   }

   protected void checkInitialized() {
      if (isInitialized()) {
         throw new IllegalStateException("Cannot alter characteristic, the buy sell signaler has been initialized!");
      }
   }

   /**
    * Set the interval type and value
    *
    * @param type
    * @param intervalValue
    * @throws IllegalStateException when set after initialize()
    */
   public void setInterval(MarketDataSource.ChartIntervalType type, int intervalValue) {
      checkInitialized();
      if (intervalValue < 1) {
         intervalValue = 1;
      }

      this.intervalType = type;
      this.intervalValue = intervalValue;

      if (intervalValue < 1) {
         intervalValue = 1;
      }
   }

   /**
    * Get current price at specified index
    *
    * @param tickIdx
    * @return
    */
   protected double getPrice(int tickIdx) {
      Tick t = getTickAt(tickIdx);
      return t == null ? 0d : t.getClosePrice();
   }

   /**
    * Get tick at specified index
    *
    * @param idx
    * @return
    */
   protected Tick getTickAt(int idx) {
      return (idx < 0 || idx > tickIndicatorBars.size() - 1) ? null : tickIndicatorBars.get(idx).TICK;
   }

   /**
    * Get the current/latest tick
    *
    * @return can be null
    */
   protected Tick getCurrentTick() {
      int curIdx = getCurrentBarIndex();
      return tickIndicatorBars.get(curIdx).TICK;
   }

   /**
    * Get the previous tick (if any)
    *
    * @return can be null
    */
   protected Tick getPreviousTick() {
      int curIdx = tickIndicatorBars.size() - 2;
      return (curIdx < 0) ? null : tickIndicatorBars.get(curIdx).TICK;
   }

   /**
    * Return the current/latest index of TickIndicatorBar
    *
    * @return
    * @see #tickIndicatorBars
    */
   protected int getCurrentBarIndex() {
      return tickIndicatorBars.size() - 1;
   }

   /**
    * Return the previous TickIndicatorBar
    *
    * @return
    */
   protected TickIndicatorBar getPreviousBar() {
      int curIdx = tickIndicatorBars.size() - 2;
      return (curIdx < 0) ? null : tickIndicatorBars.get(curIdx);
   }

   /**
    * Return the current/latest TickIndicatorBar
    *
    * @return
    */
   protected TickIndicatorBar getCurrentBar() {
      int curIdx = getCurrentBarIndex();
      return tickIndicatorBars.get(curIdx);
   }

   /**
    * Get the TickIndicatorBar at specified index
    *
    * @param idx
    * @return
    */
   protected TickIndicatorBar getBarAt(int idx) {
      return (idx < 0 || idx > tickIndicatorBars.size() - 1) ? null : tickIndicatorBars.get(idx);
   }

   /**
    * To register ITradePositionListener object
    *
    * @param listener
    */
   public void addBuySellListener(ITradePositionListener listener) {
      this.listeners.put(listener, Boolean.TRUE);
   }

   /**
    * To register ITradePositionListener object
    *
    * @param listener
    */
   public void removeBuySellListener(ITradePositionListener listener) {
      this.listeners.remove(listener);
   }

   /**
    * To broadcast 'open position' to all registered ITradePositionListener
    *
    * @param newPosition
    */
   protected void broadcastOpenPosition(TradePosition newPosition) {
      Collection<ITradePositionListener> coll = this.listeners.keySet();
      for (ITradePositionListener lst : coll) {
         lst.openPosition(
            this, market, newPosition, this.strategyParams.getCutLoss() > 10000L ? 0 : this.strategyParams.getCutLoss(),
            this.strategyParams.getTakeProfit() > 10000L ? 0 : this.strategyParams.getTakeProfit()
         );
      }
   }

   /**
    * To broadcast 'close position' to all registered ITradePositionListener
    *
    * @param prevPosition
    * @param reversalPosition
    * @param profit
    */
   protected void broadcastClosePosition(TradePosition prevPosition, TradePosition reversalPosition, double profit) {
      Collection<ITradePositionListener> coll = this.listeners.keySet();
      for (ITradePositionListener lst : coll) {
         lst.closePosition(this, market, prevPosition, reversalPosition, profit);
      }
   }

   /**
    * Called when new tick is available
    */
   public void marketMove(MarketDataSource source, Tick lastTick) {
      marketMove(source, lastTick, null, null);
   }

   public synchronized void marketMove(MarketDataSource source, Tick latestTick, Graphics gr, PricePlotter plotter) {
      if (isInitialized() && latestTick != null) {
         boolean changed = false;
         if (intervalType != MarketDataSource.ChartIntervalType.TICK) {
            double interval = 0L;
            switch (intervalType) {
               case HOUR:
                  interval = 1000 * 60 * 60 * intervalValue;
                  break;
               case DAY:
                  interval = 1000 * 60 * 60 * 24 * intervalValue;
                  break;
               default:
                  interval = 1000 * 60 * intervalValue;
                  break;
            }

            long prevTickTime = prevTick == null ? 0 : prevTick.getTimestamp().getTime();
            long newTickTime = latestTick.getTimestamp().getTime();

            if (prevTickTime + interval < newTickTime) {
               if (prevTick != null) {
                  tickIndicatorBars.add(new TickIndicatorBar(
                     new Tick(latestTick.getTimestamp(), prevTick.getVolume(), prevTick.getOpenPrice(),
                              prevTick.getClosePrice(), prevTick.getLow(), prevTick.getHigh()
                     ), getIndicatorCount()));
                  changed = true;
               }
               prevTick = latestTick;
            } else {
               prevTick = new Tick(prevTick.getTimestamp(), prevTick.getVolume() + latestTick.getVolume(),
                                   prevTick.getOpenPrice(), latestTick.getClosePrice(),
                                   Math.min(prevTick.getLow(), latestTick.getLow()),
                                   Math.max(prevTick.getHigh(), latestTick.getHigh())
               );
            }
         } else {
            tickCounter++;
            if (tickCounter % intervalValue == 0) {
               if (prevTick != null) {
                  tickIndicatorBars.add(new TickIndicatorBar(
                     new Tick(latestTick.getTimestamp(), prevTick.getVolume(), prevTick.getOpenPrice(),
                              prevTick.getClosePrice(), prevTick.getLow(), prevTick.getHigh()
                     ), getIndicatorCount()));
                  changed = true;
               }
               prevTick = latestTick;
            } else if (prevTick != null) {
               prevTick = new Tick(prevTick.getTimestamp(), prevTick.getVolume() + latestTick.getVolume(),
                                   prevTick.getOpenPrice(), latestTick.getClosePrice(),
                                   Math.min(prevTick.getLow(), latestTick.getLow()),
                                   Math.max(prevTick.getHigh(), latestTick.getHigh())
               );
            } else {
               prevTick = latestTick;
            }
         }

         if (changed) {
            int idx = getCurrentBarIndex();
            if (gr != null && idx > 0) {
               Tick t1 = getPreviousTick();
               Tick t2 = getCurrentTick();

               drawLine(t1.getClosePrice(), t1.getTimestamp(), t2.getClosePrice(), t2.getTimestamp(), Color.GRAY, gr,
                        plotter
               );
            }
            computeTechnicalIndicator(latestTick, gr, plotter);

            while (tickIndicatorBars.size() >= MAX_TICK_HISTORY_KEPT) {
               tickIndicatorBars.remove(0);
               if (_di_tr != null) {
                  removeFromArray(_di_tr, 0);
                  removeFromArray(_di_dmPlus, 0);
                  removeFromArray(_di_dmMinus, 0);
                  removeFromArray(_di_trN, 0);
                  removeFromArray(_di_dmPlusN, 0);
                  removeFromArray(_di_dmMinusN, 0);
               }
               if (_dx != null)
                  removeFromArray(_dx, 0);
            }

         }
         computeSignalDirection(latestTick, gr, plotter);
      }
   }

   /**
    * Return the number of indicators used in this strategy, for example if it is combination of 2 EMA crosses and 1
    * RSI, it should return 3 so that each indicator can be accessed by id: 0, 1, and 2.
    *
    * @return
    */
   protected abstract int getIndicatorCount();

   /**
    * The actual technical indicator computation
    *
    * @param lastTick
    * @param gr
    * @param plotter
    */
   protected abstract void computeTechnicalIndicator(Tick lastTick, Graphics gr, PricePlotter plotter);

   /**
    * Called after computeTechnicalIndicator(), this method contains standard logic that formulate the new buy/sell
    * direction, and notify the ISignalDirectionConsumer object of the changes of directions.
    *
    * @param currentTick
    * @param gr
    * @param plotter
    */
   protected void computeSignalDirection(Tick currentTick, Graphics gr, PricePlotter plotter) {
      int currIdx = getCurrentBarIndex();

      if (currentTick != null && currIdx >= getEffectiveMarketDataStartingPosition()) {
         SignalDirection newDirection = formulateNewDirection(currIdx, currentTick, oldDirection, gr, plotter);

         TradePosition openPosition = strategyRunner.getOpenPosition();
         if (newDirection == oldDirection && openPosition != null && openPosition.getDirection() != newDirection) {
            //oldDirection = openPosition.getDirection();
         }

         strategyRunner.preProcess(currIdx, currentTick, newDirection, oldDirection);

         if (newDirection == SignalDirection.NEUTRAL) {
            strategyRunner.onNeutralDirection(currIdx, currentTick, newDirection, oldDirection);
         } else if (newDirection != oldDirection) {
            strategyRunner.onBuySellIndicatorChanges(currIdx, currentTick, newDirection, oldDirection);
         } else {
            strategyRunner.onBuySellIndicatorRemains(currIdx, currentTick, newDirection);
         }

         oldDirection = formulateOldDirection(currIdx, currentTick, newDirection);
      }
   }

   /**
    * The default is 20, that means this Strategy should start giving signal direction after it receives the 20th
    * Technical Indicator Bar from market data source.
    *
    * @return positive number greater than 1
    * @see #computeSignalDirection(Tick, Graphics, PricePlotter)
    */
   protected int getEffectiveMarketDataStartingPosition() {
      return 20;
   }

   /**
    * To formulate new direction based on current index and tick. The implementation is to call
    * formulateNewDirection(int, Tick, SignalDirection)
    *
    * @param currIdx      can be used to access allIndicator variable
    * @param currentTick  the tick newly received from market data source
    * @param oldDirection the previous direction generated for previous tick
    * @param gr           can be null
    * @param plotter      can be null
    * @return
    * @see #tickIndicatorBars
    */
   protected SignalDirection formulateNewDirection(
      int currIdx, Tick currentTick, SignalDirection oldDirection, Graphics gr, PricePlotter plotter
   ) {
      return formulateNewDirection(currIdx, currentTick, oldDirection);
   }

   /**
    * Default implementation of formulating new direction is by crossing over the fast and slow technical indicators. If
    * fast is over the slow, it generates BUY direction. If fast is below the slow, it generates SELL direction.
    *
    * @param currIdx
    * @param currentTick
    * @param oldDirection
    * @return
    * @see #getFastIndicatorValue(int)
    * @see #getSlowIndicatorValue(int)
    */
   protected SignalDirection formulateNewDirection(int currIdx, Tick currentTick, SignalDirection oldDirection) {
      double currFast = getFastIndicatorValue(currIdx);
      double currSlow = getSlowIndicatorValue(currIdx);

      return currSlow > currFast ? SignalDirection.SELL : SignalDirection.BUY;
   }


   /**
    * Default implementation is to return argument direction as it is
    *
    * @param currIdx
    * @param currentTick
    * @param newDirection
    * @return
    */
   protected SignalDirection formulateOldDirection(int currIdx, Tick currentTick, SignalDirection direction) {
      return direction;
   }

   /**
    * The basic strategy of producing buy/sell direction is crossover between fast and slow indicators; this method
    * should return the FAST indicator value (e.g. value of SMA 5 in SMA cross 5,15).
    *
    * @param idx
    * @return
    */
   protected abstract double getFastIndicatorValue(int idx);

   /**
    * The basic strategy of producing buy/sell direction is crossover between fast and slow indicators; this method
    * should return the SLOW indicator value (e.g. value of SMA 15 in SMA cross 5,15).
    *
    * @param idx
    * @return
    */
   protected abstract double getSlowIndicatorValue(int idx);

   /**
    * What this method will do is to simulate market data movement by feeding ticks available between the starting and
    * the ending period into this Strategy one at a time.
    *
    * @param startPeriod the starting period used to query ticks from MarketDataSource
    * @param endPeriod   the ending period used to query ticks from MarketDataSource
    * @param graphic     the graphics used to draw the technical indicators and profit/loss lines
    * @param plotter     the plotter to help Strategy to calculate specific coordinate in the chart
    */
   public void executeStrategy(Date startPeriod, Date endPeriod, Graphics graphic, PricePlotter plotter) {
      strategyRunner.reset(this, strategyParams);
      oldDirection = SignalDirection.NEUTRAL;

      initialize();

      try {
         strategyRunner.setGui(graphic, plotter);

         Tick[] ts = this.market.getMarketData(startPeriod, endPeriod);
         if (ts != null) {
            for (Tick t : ts) {
               marketMove(market, t, graphic, plotter);
            }
         }

         TradePosition openPosition = strategyRunner.getOpenPosition();
         if (openPosition != null) {
            Tick t = ts[ts.length - 1];
            TradePosition closure = new TradePosition(
               openPosition.getDirection() == SignalDirection.BUY ? SignalDirection.SELL : SignalDirection.BUY,
               SignalType.CLOSE, t
            );

            strategyRunner.closePosition(openPosition, closure);
         }
      } finally {
         strategyRunner.releaseGui();
      }
   }

   /**
    * Draw a line from coordinate pointed by (price1,date1) to coordinate pointed by (price2,date2) in the chart using
    * specified color.
    *
    * @param price1
    * @param date1
    * @param price2
    * @param date2
    * @param color
    * @param graphics
    * @param plotter
    */
   protected void drawLine(
      double price1, Date date1, double price2, Date date2, Color color, Graphics graphics, PricePlotter plotter
   ) {
      graphics.setColor(color);
      Point p1 = plotter.plotPriceAt(price1, date1);
      Point p2 = plotter.plotPriceAt(price2, date2);
      graphics.drawLine(p1.x, p1.y, p2.x, p2.y);
   }

   ///////////////////////////////////////////////////////////////
   /// TECH INDICATORS SECTION 								///
   /// To be refactored, to reuse logic available in 			///
   /// Technical Indicator classes rather than dupplicating	///
   /// them here..												///
   ///////////////////////////////////////////////////////////////

   private double[] _di_tr;
   private double[] _di_dmPlus;
   private double[] _di_dmMinus;
   private double[] _di_trN;
   private double[] _di_dmPlusN;
   private double[] _di_dmMinusN;
   private double[] _dx;

   protected void directionalIndicator(
      int plusDiTechIdx, int minusDiTechIdx, int diValue, Tick lastTick, Graphics graphics, PricePlotter plotter
   ) {
      if (_di_tr == null) {
         _di_tr = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmPlus = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmMinus = new double[MAX_TICK_HISTORY_KEPT];
         _di_trN = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmPlusN = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmMinusN = new double[MAX_TICK_HISTORY_KEPT];
      }

      tickIndicatorBars.get(0).indexedIndicators[plusDiTechIdx] = 0;
      tickIndicatorBars.get(0).indexedIndicators[minusDiTechIdx] = 0;

      if (tickIndicatorBars.size() > 1) {
         int currentIdx = getCurrentBarIndex();
         Tick prev = getPreviousTick();
         Tick cur = getCurrentTick();

         _di_tr[currentIdx] = Math.max(
            Math.max(cur.getHigh() - cur.getLow(), Math.abs(cur.getHigh() - prev.getClosePrice())),
            Math.abs(cur.getLow() - prev.getClosePrice())
         );

         double v1 = cur.getHigh() - prev.getHigh();
         double v2 = prev.getLow() - cur.getLow();
         _di_dmPlus[currentIdx] = v1 > v2 ? Math.max(v1, 0) : 0;
         _di_dmMinus[currentIdx] = v2 > v1 ? Math.max(v2, 0) : 0;

         if (currentIdx - diValue + 1 < 1) {
            // nothing
         } else {
            if (currentIdx - diValue + 1 == 1) {
               for (int j = currentIdx - diValue + 1; j <= currentIdx; j++) {
                  _di_dmPlusN[currentIdx] += _di_dmPlus[j];
                  _di_dmMinusN[currentIdx] += _di_dmMinus[j];
                  _di_trN[currentIdx] += _di_tr[j];
               }
            } else {
               _di_trN[currentIdx] =
                  (_di_trN[currentIdx - 1] - (_di_trN[currentIdx - 1] / (double) diValue)) + _di_tr[currentIdx];
               _di_dmPlusN[currentIdx] =
                  (_di_dmPlusN[currentIdx - 1] - (_di_dmPlusN[currentIdx - 1] / (double) diValue)) + _di_dmPlus[currentIdx];
               _di_dmMinusN[currentIdx] =
                  (_di_dmMinusN[currentIdx - 1] - (_di_dmMinusN[currentIdx - 1] / (double) diValue)) + _di_dmMinus[currentIdx];
            }

            double diPlus = (int) (100 * (_di_dmPlusN[currentIdx] / _di_trN[currentIdx]));
            double diMinus = (int) (100 * (_di_dmMinusN[currentIdx] / _di_trN[currentIdx]));

            getCurrentBar().indexedIndicators[plusDiTechIdx] = diPlus;
            getCurrentBar().indexedIndicators[minusDiTechIdx] = diMinus;

            if (showTechIndicator && graphics != null) {
               Date dt1 = prev.getTimestamp();
               Date dt2 = cur.getTimestamp();

               Point oldMinusPt = plotter.plotScaleAt(getPreviousBar().indexedIndicators[minusDiTechIdx], dt1);
               Point newMinusPt = plotter.plotScaleAt(diMinus, dt2);
               graphics.setColor(COLORS[plusDiTechIdx]);
               drawLine(getPreviousBar().indexedIndicators[plusDiTechIdx], dt1,
                        getCurrentBar().indexedIndicators[plusDiTechIdx], dt1, COLORS[plusDiTechIdx], graphics, plotter
               );

               graphics.setColor(COLORS[minusDiTechIdx]);
               graphics.drawLine(oldMinusPt.x, oldMinusPt.y, newMinusPt.x, newMinusPt.y);
            }
         }
      }
   }

   private void removeFromArray(double[] array, int index) {
      int numMoved = array.length - index - 1;
      if (numMoved > 0)
         System.arraycopy(array, index + 1, array, index, numMoved);
      array[array.length - 1] = 0d;
   }

   protected void ema(int emaTechIdx, int emaVal, Graphics graphics, PricePlotter plotter) {
      double alpha = 2d / (1d + (double) emaVal);
      int size = tickIndicatorBars.size();
      boolean reuseOld = size > 0;

      if (showTechIndicator && graphics != null) {
         graphics.setColor(COLORS[emaTechIdx]);
      }

      if (size > 0) {
         tickIndicatorBars.get(0).indexedIndicators[emaTechIdx] = getPrice(0);
      }

      int curIdx = getCurrentBarIndex();

      if (curIdx > 0) {
         double emaYesterday = getPreviousBar().indexedIndicators[emaTechIdx];
         double emaToday = emaYesterday + (alpha * (getPrice(curIdx) - emaYesterday));

         getCurrentBar().indexedIndicators[emaTechIdx] = emaToday;

         if (showTechIndicator && graphics != null) {
            drawLine(emaYesterday, getPreviousTick().getTimestamp(), emaToday, getCurrentTick().getTimestamp(),
                     COLORS[emaTechIdx], graphics, plotter
            );
         }
      }
   }

   protected void rsi(
      int upTechIdx, int downTechIdx, int rsiTechIdx, int rsiValue, Graphics graphics, PricePlotter plotter
   ) {
      int curIdx = getCurrentBarIndex();
      if (curIdx < 1) {
         tickIndicatorBars.get(0).indexedIndicators[upTechIdx] = 0;
         tickIndicatorBars.get(0).indexedIndicators[downTechIdx] = 0;
         tickIndicatorBars.get(0).indexedIndicators[rsiTechIdx] = 50;
      } else {
         double now = getPrice(curIdx);
         double prev = getPrice(curIdx - 1);

         if (now > prev) {
            getCurrentBar().indexedIndicators[upTechIdx] = now - prev;
         } else if (now < prev) {
            getCurrentBar().indexedIndicators[downTechIdx] = prev - now;
         }

         emaForIndicator(upTechIdx, rsiValue);
         emaForIndicator(downTechIdx, rsiValue);

         if (graphics != null) {
            graphics.setColor(COLORS[rsiTechIdx]);
         }

         double u = getCurrentBar().indexedIndicators[upTechIdx];
         double d = getCurrentBar().indexedIndicators[downTechIdx];
         double rsi = 0;
         if (d == 0) {
            rsi = 50;
         } else {
            double rs = u / d;
            rsi = 100 - (100 * 1 / (1 + rs));
         }

         getCurrentBar().indexedIndicators[rsiTechIdx] = rsi;
         emaForIndicator(rsiTechIdx, rsiValue);

         if (showTechIndicator && graphics != null) {
            Date prevdt = getPreviousTick().getTimestamp();
            Point oldPt = plotter.plotScaleAt(getPreviousBar().indexedIndicators[rsiTechIdx], prevdt);

            Date dt = getCurrentTick().getTimestamp();
            Point newPt = plotter.plotScaleAt(getCurrentBar().indexedIndicators[rsiTechIdx], dt);

            graphics.drawLine(oldPt.x, oldPt.y, newPt.x, newPt.y);

            //graphics.setColor(Color.WHITE);
            //graphics.drawLine(oldPt.x, oldPt.y, oldPt.x, oldPt.y);
         }
      }
   }

   protected void emaForIndicator(int emaTechIdx, int emaVal) {
      double alpha = 2d / (1d + (double) emaVal);
      int curIdx = getCurrentBarIndex();
      double emaYesterday = getPreviousBar().indexedIndicators[emaTechIdx];
      double emaToday = emaYesterday + (alpha * (getCurrentBar().indexedIndicators[emaTechIdx] - emaYesterday));

      getCurrentBar().indexedIndicators[emaTechIdx] = emaToday;
   }

   protected void adx(int adxTechIdx, int smoothRatio, Graphics graphics, PricePlotter plotter) {
      if (_di_tr == null) {
         _di_tr = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmPlus = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmMinus = new double[MAX_TICK_HISTORY_KEPT];
         _di_trN = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmPlusN = new double[MAX_TICK_HISTORY_KEPT];
         _di_dmMinusN = new double[MAX_TICK_HISTORY_KEPT];
         _dx = new double[MAX_TICK_HISTORY_KEPT];
      }

      if (tickIndicatorBars.size() > 0) {
         tickIndicatorBars.get(0).indexedIndicators[adxTechIdx] = 0;
      }

      if (tickIndicatorBars.size() > 1) {
         int currentIdx = getCurrentBarIndex();
         Tick prev = getPreviousTick();
         Tick cur = getCurrentTick();

         _di_tr[currentIdx] = Math.max(
            Math.max(cur.getHigh() - cur.getLow(), Math.abs(cur.getHigh() - prev.getClosePrice())),
            Math.abs(cur.getLow() - prev.getClosePrice())
         );

         double v1 = cur.getHigh() - prev.getHigh();
         double v2 = prev.getLow() - cur.getLow();
         _di_dmPlus[currentIdx] = v1 > v2 ? Math.max(v1, 0) : 0;
         _di_dmMinus[currentIdx] = v2 > v1 ? Math.max(v2, 0) : 0;

         if (currentIdx - smoothRatio + 1 < 1) {
            // nothing
         } else {
            if (currentIdx - smoothRatio + 1 == 1) {
               for (int j = currentIdx - smoothRatio + 1; j <= currentIdx; j++) {
                  _di_dmPlusN[currentIdx] += _di_dmPlus[j];
                  _di_dmMinusN[currentIdx] += _di_dmMinus[j];
                  _di_trN[currentIdx] += _di_tr[j];
               }
            } else {
               _di_trN[currentIdx] =
                  (_di_trN[currentIdx - 1] - (_di_trN[currentIdx - 1] / (double) smoothRatio)) + _di_tr[currentIdx];
               _di_dmPlusN[currentIdx] =
                  (_di_dmPlusN[currentIdx - 1] - (_di_dmPlusN[currentIdx - 1] / (double) smoothRatio)) + _di_dmPlus[currentIdx];
               _di_dmMinusN[currentIdx] =
                  (_di_dmMinusN[currentIdx - 1] - (_di_dmMinusN[currentIdx - 1] / (double) smoothRatio)) + _di_dmMinus[currentIdx];
            }

            double diPlus = (int) (100 * (_di_dmPlusN[currentIdx] / _di_trN[currentIdx]));
            double diMinus = (int) (100 * (_di_dmMinusN[currentIdx] / _di_trN[currentIdx]));

            double diDiff = Math.abs(diMinus - diPlus);
            double diSum = diMinus + diPlus;

            _dx[currentIdx] = (int) (100 * (diDiff / diSum));

            int dob = smoothRatio * 2 - 2;
            if (currentIdx < dob) {
               // nothing
            } else if (currentIdx == dob) {
               double adxt = 0;
               int ctr = 0;
               for (int j = smoothRatio; j <= currentIdx; j++) {
                  ctr++;
                  adxt += _dx[j];
               }

               getCurrentBar().indexedIndicators[adxTechIdx] = (int) adxt / (double) ctr;
            } else {
               getCurrentBar().indexedIndicators[adxTechIdx] =
                  (int) (((getPreviousBar().indexedIndicators[adxTechIdx] * ((double) smoothRatio - 1)) + _dx[currentIdx]) / (double) smoothRatio);
            }

            if (showTechIndicator && currentIdx - dob + 1 > 1 && graphics != null) {
               int adx = (int) getCurrentBar().indexedIndicators[adxTechIdx];

               Date dt1 = prev.getTimestamp();
               Date dt2 = cur.getTimestamp();

               Point oldAdxPt = plotter.plotScaleAt((int) getPreviousBar().indexedIndicators[adxTechIdx], dt1);
               Point newAdxPt = plotter.plotScaleAt(adx, dt2);
               graphics.setColor(COLORS[adxTechIdx]);
               graphics.drawLine(oldAdxPt.x, oldAdxPt.y, newAdxPt.x, newAdxPt.y);
            }
         }
      }
   }

   protected void stdDev(
      int smaTechIdx, int stdDevTechIdx, double limit, int stdDevValue, double deviations, Graphics graphics,
      PricePlotter plotter
   ) {
      if (tickIndicatorBars.size() > 0) {
         tickIndicatorBars.get(0).indexedIndicators[smaTechIdx] = getPrice(0);
         tickIndicatorBars.get(0).indexedIndicators[stdDevTechIdx] = 0;
      }

      int loop = 0;
      int curIdx = getCurrentBarIndex();
      if (curIdx > 0) {
         double v1 = 0, v2 = 0;
         double ctr = 0;
         for (int j = curIdx; j >= curIdx - stdDevValue + 1; j--) {
            double priceAtJ = j < 0 ? getPrice(0) : getPrice(j);
            v1 += priceAtJ;
            v2 += priceAtJ * priceAtJ;
            ctr += 1;
         }

         double sma = v1 / ctr;
         double stdDev = Math.abs((ctr * v2) - (v1 * v1));
         stdDev = Math.sqrt(stdDev / (ctr * (ctr - 1)));
         double bband = deviations * stdDev; // assuming we are using 2 deviations
         double bbUpper = sma + bband;
         double bbLower = sma - bband;

         getCurrentBar().indexedIndicators[smaTechIdx] = sma;
         getCurrentBar().indexedIndicators[stdDevTechIdx] = bband;

         if (showTechIndicator && graphics != null && curIdx > 1) {
            Date dt1 = getPreviousTick().getTimestamp();
            double oldBband = getPreviousBar().indexedIndicators[stdDevTechIdx];
            double oldSma = getPreviousBar().indexedIndicators[smaTechIdx];
            Point oldPtSma = plotter.plotPriceAt(oldSma, dt1);
            Point oldLmtUpper = plotter.plotPriceAt(oldSma + limit, dt1);
            Point oldLmtLower = plotter.plotPriceAt(oldSma - limit, dt1);
            Point oldPtBbUpper = plotter.plotPriceAt(oldSma + oldBband, dt1);
            Point oldPtBbLower = plotter.plotPriceAt(oldSma - oldBband, dt1);

            Date dt2 = getCurrentTick().getTimestamp();
            Point newPtSma = plotter.plotPriceAt(sma, dt2);
            Point newPtBbUpper = plotter.plotPriceAt(bbUpper, dt2);
            Point newPtBbLower = plotter.plotPriceAt(bbLower, dt2);
            Point newPtLmtUpper = plotter.plotPriceAt(sma + limit, dt2);
            Point newPtLmtLower = plotter.plotPriceAt(sma - limit, dt2);

            graphics.setColor(COLORS[smaTechIdx]);
            graphics.drawLine(oldPtSma.x, oldPtSma.y, newPtSma.x, newPtSma.y);

            graphics.setColor(COLORS[stdDevTechIdx]);
            graphics.drawLine(oldPtBbUpper.x, oldPtBbUpper.y, newPtBbUpper.x, newPtBbUpper.y);
            graphics.setColor(COLORS[stdDevTechIdx]);
            graphics.drawLine(oldPtBbLower.x, oldPtBbLower.y, newPtBbLower.x, newPtBbLower.y);

            graphics.setColor(COLORS[stdDevTechIdx + 1]);
            graphics.drawLine(oldLmtUpper.x, oldLmtUpper.y, newPtLmtUpper.x, newPtLmtUpper.y);
            graphics.drawLine(oldLmtLower.x, oldLmtLower.y, newPtLmtLower.x, newPtLmtLower.y);
         }
      }
   }

   protected void bollinger(
      int smaIdx, int upperBbTechIdx, int lowerBbTechIdx, int bbandValue, double deviations, Graphics graphics,
      PricePlotter plotter
   ) {
      if (tickIndicatorBars.size() > 0) {
         tickIndicatorBars.get(0).indexedIndicators[upperBbTechIdx] = getPrice(0);
         tickIndicatorBars.get(0).indexedIndicators[lowerBbTechIdx] = getPrice(0);
      }

      int loop = 0;

      int curIdx = getCurrentBarIndex();
      if (curIdx > 0) {
         double v1 = 0, v2 = 0;
         double ctr = 0;
         for (int j = curIdx; j >= curIdx - bbandValue + 1; j--) {
            double priceAtJ = j < 0 ? getPrice(0) : getPrice(j);
            v1 += priceAtJ;
            v2 += priceAtJ * priceAtJ;
            ctr += 1;
         }

         double sma = v1 / ctr;
         double stdDev = Math.abs((ctr * v2) - (v1 * v1));
         stdDev = Math.sqrt(stdDev / (ctr * (ctr - 1)));
         double bband = deviations * stdDev;
         double bbUpper = sma + bband;
         double bbLower = sma - bband;

         getCurrentBar().indexedIndicators[smaIdx] = sma;
         getCurrentBar().indexedIndicators[upperBbTechIdx] = bbUpper;
         getCurrentBar().indexedIndicators[lowerBbTechIdx] = bbLower;

         if (showTechIndicator && graphics != null && curIdx > 1) {
            Date dt1 = getPreviousTick().getTimestamp();
            Point oldPtBbUpper = plotter.plotPriceAt(getPreviousBar().indexedIndicators[upperBbTechIdx], dt1);
            Point oldPtBbLower = plotter.plotPriceAt(getPreviousBar().indexedIndicators[lowerBbTechIdx], dt1);
            Point oldPtSma = plotter.plotPriceAt(getPreviousBar().indexedIndicators[smaIdx], dt1);

            Date dt2 = getCurrentTick().getTimestamp();
            Point newPtBbUpper = plotter.plotPriceAt(bbUpper, dt2);
            Point newPtBbLower = plotter.plotPriceAt(bbLower, dt2);
            Point newPtSma = plotter.plotPriceAt(sma, dt2);

            graphics.setColor(COLORS[smaIdx]);
            graphics.drawLine(oldPtSma.x, oldPtSma.y, newPtSma.x, newPtSma.y);
            graphics.setColor(COLORS[upperBbTechIdx]);
            graphics.drawLine(oldPtBbUpper.x, oldPtBbUpper.y, newPtBbUpper.x, newPtBbUpper.y);
            graphics.setColor(COLORS[lowerBbTechIdx]);
            graphics.drawLine(oldPtBbLower.x, oldPtBbLower.y, newPtBbLower.x, newPtBbLower.y);
         }
      }
   }

   protected void sma(int smaTechIdx, int smaVal, Graphics graphics, PricePlotter plotter) {
      if (tickIndicatorBars.size() > 0) {
         tickIndicatorBars.get(0).indexedIndicators[smaTechIdx] = getPrice(0);
      }

      int curIdx = getCurrentBarIndex();

      if (curIdx > 0) {

         double val = 0;
         double ctr = 0;
         for (int j = curIdx; j >= curIdx - smaVal + 1; j--) {
            val += (j < 0 ? getPrice(0) : getPrice(j));
            ctr += 1;
         }

         double result = val / ctr;
         getCurrentBar().indexedIndicators[smaTechIdx] = result;

         if (showTechIndicator && graphics != null) {
            drawLine(getPreviousBar().indexedIndicators[smaTechIdx], getPreviousTick().getTimestamp(), result,
                     getCurrentTick().getTimestamp(), COLORS[smaTechIdx], graphics, plotter
            );
         }
      }
   }
}
