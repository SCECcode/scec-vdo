package org.scec.vtk.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class Help{
  public static void main(String[] args){
    new Help();
  }
  
  public Help() {
    SwingUtilities.invokeLater(new Runnable(){
      public void run() {

        final JEditorPane jEditorPane = new JEditorPane();
        JScrollPane scrollPane = new JScrollPane(jEditorPane);
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        
        // Styles for HTML
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("div { overflow-y:scroll; position:fixed; top:0; width: 100%; }");
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        styleSheet.addRule("h1 {color: blue;}");
        styleSheet.addRule("h2 {color: red;}");

        //User Guide
        InputStream is = null;
		try {
			is = new FileInputStream("Z:\\Grand Challenge\\SCEC-VDO Team\\userGuide.html");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(is)); 
        String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} 
        StringBuilder sb = new StringBuilder(); 
        while(line != null){ 
        	sb.append(line).append("\n"); 
        	try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        String fileAsString = sb.toString();

        Document doc = kit.createDefaultDocument();
        

        jEditorPane.setEditable(false);
        jEditorPane.setEnabled(true);
//        jEditorPane.setDocument(doc);
        jEditorPane.setContentType("text/html");
        jEditorPane.setText(fileAsString);

        JFrame j = new JFrame("SCEC VDO User Guide");
        j.getContentPane().add(scrollPane, BorderLayout.CENTER);
        j.setSize(new Dimension(500,500));
        j.setLocationRelativeTo(null);
        j.setVisible(true);    
        jEditorPane.setCaretPosition(0);
        jEditorPane.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(final HyperlinkEvent pE) {
                if (HyperlinkEvent.EventType.ACTIVATED == pE.getEventType()) {
                    String desc = pE.getDescription();
                    if (desc == null || !desc.startsWith("#")) return;
                    desc = desc.substring(1);
                    jEditorPane.scrollToReference(desc);
                }
            }
        });
      }
    });
  }
}