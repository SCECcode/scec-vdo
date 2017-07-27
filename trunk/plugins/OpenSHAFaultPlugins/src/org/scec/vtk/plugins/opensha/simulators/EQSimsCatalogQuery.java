package org.scec.vtk.plugins.opensha.simulators;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.html.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.scec.vtk.plugins.utils.components.CheckAllTable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
// The Simple Web Browser.
@SuppressWarnings("serial")
public class EQSimsCatalogQuery extends JFrame {
	JButton downloadButton;									//Download button
    private JTextField locationTextField;				    //Page location text field.
    private JEditorPane displayEditorPane;					//Editor pane for displaying table;
    
    private String defaultCatalogURL = "http://rsqsim.usc.edu/catalogs/index.xml";
    private CheckAllTable tablePanel;
    String[] tags = {"authors", "date", "description", "region"};
    
    public EQSimsCatalogQuery() {
        // Set application title.
        super("Catalog Downloads");
         
        // Set window size.
        setSize(640, 480);
         
        // Handle closing events.
        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
       
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(new EmptyBorder(5, 10, 0, 10));
        locationTextField = new JTextField(defaultCatalogURL);
        locationTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
						actionGo();
					} catch (SAXException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ParserConfigurationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
        });
        
        buttonPanel.add(locationTextField, BorderLayout.CENTER);
        JButton goButton = new JButton("GO");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
					actionGo();
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        buttonPanel.add(goButton, BorderLayout.LINE_END);
          
        JPanel southButtonPanel = new JPanel();
        southButtonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        downloadButton = new JButton ("Download");
        southButtonPanel.add(downloadButton);
        
        ArrayList<String> empty = new ArrayList<String>();
        empty.add("");
        tablePanel = new CheckAllTable(empty, "title");
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(southButtonPanel, BorderLayout.SOUTH);

    }
    
    protected ArrayList<String> getDownloadTitles() {
    	ArrayList<String> downloads = new ArrayList<String>();
    	for (int i = 0; i < tablePanel.getTable().getRowCount(); i++)
    		if ((boolean) tablePanel.getTable().getValueAt(i, 0)) {
    			downloads.add((String) tablePanel.getTable().getValueAt(i, 1));
    		}
    	return downloads;
	}

	private ArrayList<String> convertNodeList(NodeList nodeList) {
    	ArrayList<String> arrayList = new ArrayList<String>();
    	for (int i = 0; i < nodeList.getLength(); i++) {
        	arrayList.add(nodeList.item(i).getTextContent());
        }
    	return arrayList;
    }
     
    private void actionGo() throws SAXException, IOException, ParserConfigurationException {
        URL verifiedUrl = verifyUrl(locationTextField.getText());
        if (verifiedUrl != null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = verifiedUrl.openStream();
            org.w3c.dom.Document document = documentBuilder.parse(stream);
            NodeList titles = ((org.w3c.dom.Document) document).getElementsByTagName("title");
       	 	ArrayList<String> titleList = convertNodeList(titles);
            this.getContentPane().remove(tablePanel);
            
            tablePanel = new CheckAllTable(titleList, "title");
            for (int i = 0; i < tags.length; i++) {
            	 NodeList nodeList = ((org.w3c.dom.Document) document).getElementsByTagName(tags[i]);
            	 ArrayList<String> arrayList = convertNodeList(nodeList);
            	 tablePanel.addColumn(arrayList, tags[i]);
            }
            this.getContentPane().add(tablePanel, BorderLayout.CENTER);
            this.revalidate();
        } else {
            //INVALID URL
        }
    }
     
    // Verify URL format.
    private URL verifyUrl(String url) {
        // Only allow HTTP URLs.
        if (!url.toLowerCase().startsWith("http://"))
            return null;
         
        // Verify format of URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
         
        return verifiedUrl;
    }
}