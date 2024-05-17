package net.lexzeus.fx.simul.strategies;

import net.lexzeus.fx.simul.Configuration;
import net.lexzeus.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Factory to create new instance of BuySellStrategy
 *
 * @author Alexander Koentjara
 */
public class BuySellStrategyFactory {

   public static final String NONE = "NONE";

   private HashMap<String, Class<? extends BuySellStrategy>> allStrategiesMap;
   private String[] allStrategies;
   private final Log log = Log.get(this);

   private static BuySellStrategyFactory instance = new BuySellStrategyFactory();

   /**
    * Get the singleton instance
    *
    * @return
    */
   public static BuySellStrategyFactory getInstance() {
      return instance;
   }

   @SuppressWarnings("unchecked")
   private BuySellStrategyFactory() {
      allStrategiesMap = new HashMap<String, Class<? extends BuySellStrategy>>();
      ArrayList<String> allStrategiesList = new ArrayList<String>();

      allStrategiesList.add(NONE);
      allStrategiesMap.put(NONE, NoBuySellStrategy.class);

      int count = Configuration.getIntProperty("strategy.count");
      for (int i = 1; i <= count; i++) {
         String name = Configuration.getProperty("strategy." + i + ".name");
         String clazz = Configuration.getProperty("strategy." + i + ".class");

         try {
            allStrategiesMap.put(name.trim(), (Class<? extends BuySellStrategy>) Class.forName(clazz.trim()));
            allStrategiesList.add(name.trim());
            log.info("Strategy '", name, "' registered...");
         } catch (Exception e) {
            log.error("Unable to register strategy: ", name, ", with class: ", clazz);
         }
      }

      allStrategies = allStrategiesList.toArray(new String[allStrategiesList.size()]);
   }

   /**
    * To get all strategy names available in this application
    *
    * @return
    */
   public String[] getAllStrategyNames() {
      return allStrategies;
   }

   /**
    * Create an instance of strategy for the specified name and parameter
    *
    * @param strategyName
    * @param strategyParams
    * @return
    */
   @SuppressWarnings("unchecked")
   public BuySellStrategy createStrategy(String strategyName, StrategyParams strategyParams) {
      if (strategyName == null) {
         return new NoBuySellStrategy(strategyParams);
      }

      try {
         Constructor cons = allStrategiesMap.get(strategyName).getConstructor(StrategyParams.class);
         return (BuySellStrategy) cons.newInstance(strategyParams);
      } catch (Exception e) {
         log.warn(e);
         return new NoBuySellStrategy(strategyParams);
      }
   }

   /**
    * To check if the specified strategy name is supported in this application
    *
    * @param strategyName
    * @return
    */
   public boolean exists(String strategyName) {
      return allStrategiesMap.containsKey(strategyName);
   }

}
