package net.lexzeus.fx.simul;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class FixedLayout implements LayoutManager {

   private HashMap<Component, Point> components = new HashMap<Component, Point>();
   private Dimension dimension;

   public FixedLayout(Dimension dimension) {
      this.dimension = dimension;
   }


   public FixedLayout(int width, int height) {
      this(new Dimension(width, height));
   }


   public void addLayoutComponent(String name, Component comp) {
      components.put(comp, toPosition(name));
   }

   private Point toPosition(String txt) {
      try {
         String[] arr = txt.trim().replace(" ", "").split(",");
         return new Point(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
      } catch (Exception e) {
         return new Point(0, 0);
      }
   }


   public void layoutContainer(Container parent) {
      for (Map.Entry<Component, Point> e : components.entrySet()) {
         Component c = e.getKey();
         Point p = e.getValue();
         c.setLocation(p);
         c.setSize(c.getPreferredSize());
      }
   }

   public Dimension minimumLayoutSize(Container parent) {
      return dimension;
   }

   public Dimension preferredLayoutSize(Container parent) {
      return dimension;
   }

   public void removeLayoutComponent(Component comp) {
      components.remove(comp);
   }

}
