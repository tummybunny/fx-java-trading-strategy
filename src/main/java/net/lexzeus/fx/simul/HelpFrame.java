package net.lexzeus.fx.simul;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import static net.lexzeus.fx.simul.Configuration.VERSION_KEY;
import static net.lexzeus.fx.simul.Configuration.getProperty;

public class HelpFrame extends JFrame {

   private static final long serialVersionUID = -5874791063284612853L;

   HelpFrame() {
      super("Help - Trading Strategy Runner v" + getProperty(VERSION_KEY));

      setDefaultCloseOperation(DISPOSE_ON_CLOSE);

      StringBuilder sb = new StringBuilder();
      try (InputStream help = this.getClass().getClassLoader().getResourceAsStream("help.html")) {
         BufferedReader reader = new BufferedReader(new InputStreamReader(help), 1024);
         String line = null;
         while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
         }

         reader.close();
      } catch (Exception e) {
         sb.append("<html><b>ERROR:</b> Cannot find help.html!</html>");
      }

      JTextPane textPane = new JTextPane();
      textPane.setEditable(false);
      textPane.setContentType("text/html");
      textPane.setText(sb.toString());

      JScrollPane scrollPane = new JScrollPane(textPane);

      this.getContentPane().add(scrollPane, BorderLayout.CENTER);

      this.pack();
      this.setSize(900, 800);
      this.setLocationRelativeTo(null);
      this.setVisible(true);

      textPane.setSelectionStart(1);
      textPane.setSelectionEnd(1);
   }

   public static void main(String[] args) {
      new HelpFrame().setDefaultCloseOperation(EXIT_ON_CLOSE);
   }

}
