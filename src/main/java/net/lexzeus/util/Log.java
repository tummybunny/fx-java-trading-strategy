package net.lexzeus.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiFunction;

/**
 * Simple plain no fuss logger, fast but not thread safe.
 *
 * Log to standard out/err with format HEADER MESSSAGE, where the default HEADER:
 *      2024-05-17T22:14:30.170 INFO [{NAME}]
 *
 * Thus printing:
 *      2024-05-17T22:14:30.170 INFO [{NAME}] {MESSAGE}
 *
 * To change header format, use {@link Log#setHeaderFormatter(BiFunction)}.
 */
public class Log {

   public enum Level {
      DEBUG, INFO, WARN, ERROR
   }

   private static Level LEVEL;
   private static final String ROOT = "ROOT";
   private static Formatter formatter = new DefaultFormatter();
   private final String logger;

   static {
      String level = System.getProperty("log.level");
      if (level == null) {
         level = System.getenv("LOG_LEVEL");
      }
      if (level == null) {
         LEVEL = Level.INFO;
      } else {
         LEVEL = Level.valueOf(level);
      }
   }

   private Log(String logger) {
      this.logger = logger;
   }

   private static final Log ROOT_LOG = Log.get(ROOT);

   public static Log get() {
      return get(ROOT);
   }

   public static void setHeaderFormatter(BiFunction<Level, String, StringBuilder> formatHeader) {
      formatter = (level, name) -> formatHeader.apply(level, name);
   }

   public static Log get(Object obj) {
      if (ROOT_LOG != null && (obj == null || ROOT_LOG.equals(obj))) {
         return ROOT_LOG;
      }
      String name = obj instanceof String ? (String) obj : obj instanceof Class ?
         ((Class) obj).getSimpleName() : obj.getClass().getSimpleName();
      return new Log(name);
   }

   public void debug(Object... any) {
      if (LEVEL.ordinal() >= Level.DEBUG.ordinal()) System.out.println(format(any));
   }

   public void info(Object... any) {
      if (LEVEL.ordinal() >= Level.INFO.ordinal()) System.out.println(format(any));
   }

   public void warn(Object... any) {
      if (LEVEL.ordinal() >= Level.WARN.ordinal()) System.out.println(format(any));
   }

   public void error(Object... any) {
      if (LEVEL.ordinal() >= Level.ERROR.ordinal()) System.err.println(format(any));
   }

   private String format(Object[] any) {
      StringBuilder sb = formatter.formatHeader(LEVEL, logger);
      for (Object o: any) {
         sb.append(o == null ? "null" : o instanceof String ? (String) o : o instanceof Throwable ?
            formatError((Throwable) o) : o.toString());
      }
      return sb.toString();
   }

   private String formatError(Throwable o) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      o.printStackTrace(pw);
      pw.println();
      return sw.toString();
   }

   private interface Formatter {
      StringBuilder formatHeader(Level level, String name);
   }

   private static class DefaultFormatter implements Formatter {
      private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS ");
      private final Date date = new Date();

      @Override
      public StringBuilder formatHeader(Level level, String name) {
         StringBuilder sb = new StringBuilder();
         date.setTime(System.currentTimeMillis());
         sb.append(SDF.format(date)).append(LEVEL.name()).append(" [").append(name).append("] ");
         return sb;
      }

   }
}
