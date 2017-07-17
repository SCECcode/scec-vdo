package org.scec.vtk.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

@SuppressWarnings("serial")
public class Help extends JEditorPane{
	public Help() {
        //User Guide
		InputStream is = this.getClass().getResourceAsStream("userGuide.html"); 
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
        HTMLEditorKit kit = new HTMLEditorKit();
        
        setEditable(false);
        setEnabled(true);
        setEditorKit(kit);
       
        setContentType("text/html");
        setText(fileAsString);
        setCaretPosition(0);
        getHyperlinkListeners();
        addHyperlinkListener(new HyperlinkListener() {
            @Override 
            public void hyperlinkUpdate(final HyperlinkEvent pE) {
                if (HyperlinkEvent.EventType.ACTIVATED == pE.getEventType()) {
                    String desc = pE.getDescription();
                    if (desc == null || !desc.startsWith("#")) return;
                    desc = desc.substring(1);
                    scrollToReference(desc);
                }
            }
        });
      }
  }