package net.lexzeus.fx.simul;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import static net.lexzeus.fx.simul.Util.defaultIfNull;

public class GuiUtil {

   public static final Font NORMAL_FONT = new Font("Arial", Font.PLAIN, 10);
   public static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 12);
   private static ISizeCallback sizeCallback;

   public static void setSizeCallback(ISizeCallback callback) {
      GuiUtil.sizeCallback = callback;
   }

   private static void callback(JComponent item) {
      if (sizeCallback != null) {
         sizeCallback.onPreferredSizeCalculated(item.getPreferredSize());
      }
   }

   public static JLabel createLabel(String text) {
      JLabel label = new JLabel(text);
      label.setFont(NORMAL_FONT);
      callback(label);
      return label;
   }

   public static JLabel createLabel(String text, Font font, Color foreColor, Color backColor) {
      JLabel label = new JLabel(text);
      label.setFont(font);
      label.setForeground(foreColor);
      label.setOpaque(true);
      label.setBackground(backColor);
      callback(label);
      return label;
   }

   public static JTextField createTextField(String string) {
      JTextField text = new JTextField(string);
      text.setFont(NORMAL_FONT);
      text.setPreferredSize(text.getPreferredSize());
      callback(text);
      return text;
   }

   public static JTextField createTextField(String string, Font font, Color foreColor, Color backColor) {
      return createTextField(string, font, foreColor, backColor, null);
   }

   public static JTextField createTextField(
      String string, Font font, Color foreColor, Color backColor, Integer preferredWidth
   ) {
      JTextField text = new JTextField(string);
      text.setFont(font);
      text.setCaretColor(foreColor);
      text.setForeground(foreColor);
      text.setBackground(backColor);
      Dimension dim = text.getPreferredSize();
      dim.setSize(defaultIfNull(preferredWidth, dim.getSize().width), dim.height);
      text.setPreferredSize(dim);
      callback(text);
      return text;
   }

   public static JComboBox createCombo(String[] vals) {
      JComboBox combo = new JComboBox(vals);
      combo.setFont(NORMAL_FONT);
      combo.setEditable(false);
      callback(combo);
      return combo;
   }

   public static JComboBox createCombo(String[] vals, Font font, Color foreColor, Color backColor) {
      JComboBox combo = new JComboBox(vals);
      combo.setFont(font);
      combo.setEditable(false);
      combo.setForeground(foreColor);
      combo.setBackground(backColor);
      callback(combo);
      return combo;
   }

   public static JCheckBox createCheckBox(String string) {
      JCheckBox chk = new JCheckBox(string);
      chk.setFont(NORMAL_FONT);
      chk.setSelected(false);
      callback(chk);
      return chk;
   }

   public static JCheckBox createCheckBox(String string, Font font, Color foreColor, Color backColor) {
      JCheckBox chk = new JCheckBox(string);
      chk.setFont(font);
      chk.setForeground(foreColor);
      chk.setBackground(backColor);
      chk.setSelected(false);
      callback(chk);
      return chk;
   }

   public static JButton createButton(
      String title, Font font, Color foreColor, Color backColor, ActionListener actionListener
   ) {
      JButton btn = new JButton(title);
      btn.setFont(font);
      btn.setForeground(foreColor);
      btn.setBackground(backColor);
      btn.addActionListener(actionListener);
      callback(btn);
      return btn;
   }

   public static JButton createButton(String title, ActionListener actionListener) {
      JButton btn = new JButton(title);
      btn.setFont(NORMAL_FONT);
      btn.addActionListener(actionListener);
      callback(btn);
      return btn;
   }

   public static JToggleButton createToggleButton(String title, ActionListener actionListener) {
      JToggleButton btn = new JToggleButton(title);
      btn.setFont(NORMAL_FONT);
      btn.addActionListener(actionListener);
      callback(btn);
      return btn;
   }

}
