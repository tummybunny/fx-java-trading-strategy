package net.lexzeus.fx.simul.indicators;

import net.lexzeus.fx.simul.Configuration;
import net.lexzeus.fx.simul.indicators.TechnicalIndicator.PriceType;
import net.lexzeus.util.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static net.lexzeus.fx.simul.Configuration.getProperty;

/**
 * Factory to create new instance of TechnicalIndicator
 *
 * @author Alexander Koentjara
 */
public class TechnicalIndicatorFactory {

   private static TechnicalIndicatorFactory instance = new TechnicalIndicatorFactory();
   private HashMap<String, Class<? extends TechnicalIndicator>> allTechincalIndicatorMap;
   private ArrayList<String> allTechnicalIndicatorNames;
   private final Log log = Log.get(this);

   /**
    * Get the singleton instance
    *
    * @return
    */
   public static TechnicalIndicatorFactory getInstance() {
      return instance;
   }

   @SuppressWarnings("unchecked")
   private TechnicalIndicatorFactory() {
      allTechincalIndicatorMap = new HashMap<>();
      allTechnicalIndicatorNames = new ArrayList<>();

      int count = Configuration.getIntProperty("indicator.count");
      for (int i = 1; i <= count; i++) {
         String name = getProperty("indicator." + i + ".name");
         String clazz = getProperty("indicator." + i + ".class");
         if (name != null && clazz != null) {
            try {
               allTechincalIndicatorMap.put(name.trim(),
                                            (Class<? extends TechnicalIndicator>) Class.forName(clazz.trim())
               );
               allTechnicalIndicatorNames.add(name.trim());
               log.info("Indicator '", name, "' registered...");
            } catch (Exception e) {
               log.error("Unable to register indicator: ", name, ", with class: ", clazz, e);
            }
         } else {
            log.error("Missing indicator configuration: ", name);
         }
      }
   }

   /**
    * To get all technical indicator names available in this application
    *
    * @return
    */
   public String[] getAllTechnicalIndicatorNames() {
      return this.allTechnicalIndicatorNames.toArray(new String[allTechnicalIndicatorNames.size()]);
   }

   /**
    * To create new instance of technical indicator by name
    *
    * @param name
    * @param techValue
    * @param type
    * @param color
    * @return
    */
   public TechnicalIndicator createIndicator(
      String name, int[] techValue, PriceType type, Color color
   ) {
      try {
         Class<? extends TechnicalIndicator> clz = allTechincalIndicatorMap.get(name);
         TechnicalIndicator st = clz.newInstance();
         st.setColor(color);
         st.setPriceType(type);
         st.setTechIndicatorValue(techValue);
         return st;
      } catch (Exception e) {
         throw new RuntimeException("Error while strategy with name: " + name, e);
      }
   }

   /**
    * To check if the specified strategy name is supported in this application
    *
    * @param indicatorName
    * @return
    */
   public boolean exists(String indicatorName) {
      return allTechincalIndicatorMap.containsKey(indicatorName);
   }

}
