package net.lexzeus.fx.simul;

import net.lexzeus.util.Log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The "source" of market data.
 *
 * @author Alexander Koentjara
 */
public class MarketDataSource implements Serializable {

   private static final long serialVersionUID = 4121342123622301000L;

   public static final int MAX_CACHED_TICKS = 25000;
   private static final Log log = Log.get(MarketDataSource.class);
   private transient ArrayList<IMarketMoveListener> listeners;
   private String name = "Unknown";

   protected ArrayList<Tick> allTicks;

   public enum ChartIntervalType {
      TICK, MINUTE, HOUR, DAY
   }


   public final static String CSV_DELIMIBER = ",";

   /**
    * Expect csv format ( comma delimited ) with columns: date, time, volume, open, close, min, max.
    * <p>
    * Date format: dd/mm/yyyy
    * <p>
    * Time format: hh24:mm:ss
    * <p>
    * The first line is ignored in the csv will be ignored as it will be treated as header.
    *
    * @param f
    * @return
    */
   public static MarketDataSource fromFile(File f) {
      return fromTicks(loadFile(f));
   }

   /**
    * Expect csv format ( comma delimited ) with columns: date, time, volume, open, close, min, max.
    * <p>
    * Date format: dd/mm/yyyy
    * <p>
    * Time format: hh24:mm:ss
    * <p>
    * The first line is ignored in the csv will be ignored as it will be treated as header.
    *
    * @param f
    * @return
    */
   public static MarketDataSource fromFile(String f) {
      return fromFile(new File(f));
   }

   /**
    * Expect csv format ( comma delimited ) with columns: date, time, volume, open, close, min, max.
    * <p>
    * Date format: dd/mm/yyyy
    * <p>
    * Time format: hh24:mm:ss
    * <p>
    * The first line is ignored in the csv will be ignored as it will be treated as header.
    *
    * @param f
    * @return
    */
   public static MarketDataSource fromCsvString(String f) {
      BufferedReader br = new BufferedReader(new StringReader((f)));
      Tick[] ticks = loadFromReader(br);
      return fromTicks(ticks);
   }

   /**
    * Create market data directly from ticks
    *
    * @param ticks
    * @return
    */
   public static MarketDataSource fromTicks(Tick[] ticks) {
      return new MarketDataSource(ticks);
   }

   private static Tick[] loadFile(File f) {
      try (FileReader fr = new FileReader(f)) {
         return loadFromReader(new BufferedReader(fr));
      } catch (Exception ex) {
         log.error(ex);
         return new Tick[0];
      }
   }

   private static Tick[] loadFromReader(BufferedReader br) {
      try (br) {
         String line = null;
         ArrayList<Tick> ticks = new ArrayList<Tick>();
         SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

         while ((line = br.readLine()) != null) {
            Tick t = parseTick(sdf, line);
            if (t != null) {
               ticks.add(t);
            }
         }
         return ticks.toArray(new Tick[ticks.size()]);
      } catch (Exception e) {
         log.error("Unable to load market data", e);
         return new Tick[0];
      }
   }

   private static Tick parseTick(SimpleDateFormat sdf, String line) {
      String[] arr = line.split(CSV_DELIMIBER);
      if (arr.length == 7) {
         try {
            String date = new StringBuilder().append(arr[0].trim()).append(" ").append(arr[1].trim()).toString();
            long vol = Long.parseLong(arr[2]);
            double open = Double.parseDouble(arr[3]);
            double close = Double.parseDouble(arr[4]);
            double min = Double.parseDouble(arr[5]);
            double max = Double.parseDouble(arr[6]);

            return new Tick(sdf.parse(date), vol, open, close, min, max);
         } catch (Exception ex) {
            log.warn("Unable to parse: ", line, ex);
         }
      }
      return null;
   }

   /**
    * Default constructor - empty market data
    */
   public MarketDataSource() {
      this.allTicks = new ArrayList<Tick>();
   }

   /**
    * Default constructor - with specified ticks as market data source
    */
   public MarketDataSource(Tick[] ticks) {
      ArrayList<Tick> list = new ArrayList<Tick>(ticks.length);
      list.addAll(Arrays.asList(ticks));
      Collections.sort(list);
      this.allTicks = list;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }

   /**
    * To compress the market data by changing its interval type and value, e.g. converting ticks to 1 hourly basis
    * should reduce the number of ticks kept in this market data source if previously were kept in 1 minutely basis.
    *
    * @param type
    * @param intervalValue
    */
   public void compress(ChartIntervalType type, int intervalValue) {
      if (allTicks.size() > 1) {
         // to compress market data by minute ...
         Tick[] newTicks = getMarketData(allTicks.get(0).getTimestamp(),
                                         allTicks.get(allTicks.size() - 1).getTimestamp(), type, intervalValue
         );
         ArrayList<Tick> list = new ArrayList<Tick>(newTicks.length);
         list.addAll(Arrays.asList(newTicks));
         this.allTicks = list;
      }
   }

   /**
    * Retrieve market data series for specified time period and interval type. Having interval type MINUTE and value 15
    * translates into 15 minutely market data.
    *
    * @param start         The lower bound timestamp
    * @param stop          The upper bound timestamp
    * @param type          The interval type
    * @param intervalValue The interval value
    * @return
    */
   public Tick[] getMarketData(Date start, Date stop, ChartIntervalType type, int intervalValue) {
      if (intervalValue < 1) {
         intervalValue = 1;
      }

      Tick[] tickArray = getMarketData(start, stop);

      if (tickArray == null || tickArray.length == 0 || (type == ChartIntervalType.TICK && intervalValue == 1)) {
         return tickArray;
      }
      ArrayList<Tick> list = new ArrayList<Tick>(tickArray.length);
      list.addAll(Arrays.asList(tickArray));
      Collections.sort(list);

      ArrayList<Tick> retVal = new ArrayList<Tick>();

      if (type == ChartIntervalType.TICK) {
         int ctr = 0;
         Date date = null;
         long volume = 0L;
         double open = 0d;
         double close = 0d;
         double min = Integer.MAX_VALUE;
         double max = 0d;

         for (Tick currTick : list) {
            ctr++;

            date = currTick.getTimestamp();
            volume += currTick.getVolume();
            open = open == 0d ? currTick.getOpenPrice() : open;
            close = currTick.getClosePrice();
            min = Math.min(currTick.getLow(), min);
            max = Math.max(currTick.getHigh(), max);

            if (ctr % intervalValue == 0) {
               if (date != null) {
                  retVal.add(new Tick(date, volume, open, close, min, max));
               }
               date = null;
               volume = 0L;
               open = 0d;
               close = 0d;
               min = Integer.MAX_VALUE;
               max = 0d;
            }
         }
         if (date != null) {
            retVal.add(new Tick(date, volume, open, close, min, max));
         }
      } else {
         Tick t1 = tickArray[0];
         Calendar c = Calendar.getInstance();
         c.setTime(t1.getTimestamp());

         int intervalPeriod = 0;
         int dd = c.get(Calendar.DAY_OF_MONTH);
         int mm = c.get(Calendar.MONTH);
         int yy = c.get(Calendar.YEAR);
         int hour = c.get(Calendar.HOUR_OF_DAY);
         int minu = c.get(Calendar.MINUTE);

         switch (type) {
            case MINUTE:
               intervalPeriod = 1000 * 60 * intervalValue;
               break;
            case HOUR:
               intervalPeriod = 1000 * 60 * 60 * intervalValue;
               minu = 0;
               break;
            case DAY:
               intervalPeriod = 1000 * 60 * 60 * 24 * intervalValue;
               minu = 0;
               hour = 0;
               break;
         }

         Calendar startC = Calendar.getInstance();
         Calendar stopC = Calendar.getInstance();

         startC.set(yy, mm, dd, hour, minu, 0);
         stopC.set(Calendar.SECOND, 0);
         stopC.setTimeInMillis(startC.getTimeInMillis() + intervalPeriod);

         int idx = 0;

         Date date = null;
         long volume = 0L;
         double open = 0d;
         double close = 0d;
         double min = Integer.MAX_VALUE;
         double max = 0d;

         while (true) {
            Date startD = startC.getTime();
            Date stopD = stopC.getTime();
            boolean exhausted = idx >= list.size();
            while (idx < list.size()) {
               // consolidate ticks within time window/interval
               Tick currTick = list.get(idx++);

               exhausted = currTick.getTimestamp().after(stop);
               if (exhausted) {
                  break;
               }

               if (currTick.getTimestamp().before(stopD)) {
                  if (currTick.getTimestamp().after(startD)) {
                     date = currTick.getTimestamp();
                     volume += currTick.getVolume();
                     open = open == 0d ? currTick.getOpenPrice() : open;
                     close = currTick.getClosePrice();
                     min = Math.min(currTick.getLow(), min);
                     max = Math.max(currTick.getHigh(), max);
                  }
               } else {
                  if (date != null) {
                     retVal.add(new Tick(date, volume, open, close, min, max));
                     date = null;
                     volume = 0L;
                     open = 0d;
                     close = 0d;
                     min = Integer.MAX_VALUE;
                     max = 0d;
                  } else {
                     retVal.add(currTick);
                  }
                  break;
               }
            }

            if (exhausted) {
               break;
            } else {
               long nextMsec = startC.getTimeInMillis() + intervalPeriod;
               startC.setTimeInMillis(nextMsec);
               stopC.setTimeInMillis(nextMsec + intervalPeriod);
            }
         }
      }

      return retVal.toArray(new Tick[retVal.size()]);
   }

   /**
    * Return the market data for specified time period
    *
    * @param start The lower bound timestamp
    * @param stop  The upper bound timestamp
    * @return
    */
   public Tick[] getMarketData(Date start, Date stop) {
      Tick[] tickArray = getMarketData();
      int idxStart = tickArray.length - 1;
      int idxStop = 0;

      int idx = Arrays.binarySearch(tickArray, new Tick(start, 0L, 0d, 0d, 0d, 0d));
      idx = idx < 0 ? 0 : idx;

      for (int i = idx; i < tickArray.length; i++) {
         if (tickArray[i].getTimestamp().getTime() >= start.getTime()) {
            idxStart = i;
            break;
         }
      }

      if (tickArray.length > 0 && tickArray[tickArray.length - 1].getTimestamp().before(stop)) {
         idxStop = tickArray.length - 1;
      } else {
         idx = Arrays.binarySearch(tickArray, new Tick(stop, 0L, 0d, 0d, 0d, 0d));
         idx = idx < 0 ? 0 : idx + 2;
         idx = idx >= tickArray.length ? tickArray.length - 1 : idx;

         for (int i = idx; i >= 0; i--) {
            if (tickArray[i].getTimestamp().getTime() <= stop.getTime()) {
               idxStop = i;
               break;
            }
         }
      }

      if (idxStart <= idxStop && idxStart >= 0 && idxStop >= 0) {
         int items = idxStop - idxStart + 1;
         Tick[] retVal = new Tick[items];
         System.arraycopy(tickArray, idxStart, retVal, 0, items);
         return retVal;
      } else {
         return new Tick[0];
      }
   }

   /**
    * Get all market data available in this source
    *
    * @return
    */
   public Tick[] getMarketData() {
      synchronized (this) {
         return this.allTicks.toArray(new Tick[this.allTicks.size()]);
      }
   }

   /**
    * Add a single tick into this source
    *
    * @param newTick
    */
   public void addTick(Tick newTick) {
      synchronized (this) {
         allTicks.add(newTick);
         while (allTicks.size() > MAX_CACHED_TICKS) {
            allTicks.remove(0);
         }
      }
      broadcastMarketMove(newTick);
   }

   /**
    * Subscribe to market data movement event
    *
    * @param lst
    */
   public void addMarketMoveListener(IMarketMoveListener lst) {
      if (listeners == null)
         listeners = new ArrayList<IMarketMoveListener>();

      if (!this.listeners.contains(lst)) {
         this.listeners.add(lst);
      }
   }

   /**
    * Unsubscribe from market data movement event
    *
    * @param lst
    */
   public void removeMarketMoveListener(IMarketMoveListener lst) {
      if (listeners == null)
         listeners = new ArrayList<IMarketMoveListener>();

      this.listeners.remove(lst);
   }

   /**
    * Remove all market data movement event subscribers from this source
    */
   public void clearMarketMoveListener() {
      if (listeners == null)
         listeners = new ArrayList<IMarketMoveListener>();

      this.listeners.clear();
      ;
   }

   /**
    * Broadcast to all market data movement event subscribers when there is new tick
    *
    * @param newTick
    */
   protected void broadcastMarketMove(Tick newTick) {
      if (listeners == null)
         listeners = new ArrayList<IMarketMoveListener>();

      for (IMarketMoveListener lst : listeners) {
         lst.marketMove(this, newTick);
      }
   }

   /**
    * Get the latest tick available in this source
    *
    * @return
    */
   public Tick getLastTick() {
      synchronized (this) {
         int sz = allTicks.size();
         return (sz > 0) ? allTicks.get(sz - 1) : null;
      }
   }

   @Override
   public String toString() {
      return "MarketDataSource '" + this.name + "' with " + (allTicks.size() - 1) + " tick(s).";
   }

}
