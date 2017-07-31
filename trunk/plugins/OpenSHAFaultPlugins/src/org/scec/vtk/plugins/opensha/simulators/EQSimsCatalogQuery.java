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
import java.text.DateFormat;
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
    
    private String defaultCatalogURL = "http://rsqsim.usc.edu/catalogs/";		//Default URL for getting events catalog
    private CheckAllTable tablePanel;											//Panel containing main table
    String[] tags = {"authors", "date", "description", "region"};				//XML tags to look for on the index.xml at the url
    String[] fileTypes = {".flt", ".eList", ".pList", ".dList", ".tList"};		//File types to download
    
    /**
     * 
     */
    public EQSimsCatalogQuery() {
        super("Catalog Downloads");									// Set application title.
        setSize(640, 480);										
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
					System.out.println("Parsing error");
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
        tablePanel = new CheckAllTable(empty, "title");							//Create empty checkalltable as placeholder
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(southButtonPanel, BorderLayout.SOUTH);

    }
    
    /**
     * @return
     * 
     * Get date from the 3rd column of the table
     */
	protected ArrayList<Date> getDate() {
    	ArrayList<Date> dates = new ArrayList<Date>();
    	for (int i = 0; i < tablePanel.getTable().getRowCount(); i++)
    		if ((boolean) tablePanel.getTable().getValueAt(i, 0)) {
    			String dateStr = (String)tablePanel.getTable().getValueAt(i, 3);
    			Date date = new Date();
    			//dates.add(DateFormat.parse(dateStr));
    		}
    	return dates;
    }
    
	/**
	 * @return
	 * Build URLs for each file type to download. Return title of the catalog and the URLs to download from.
	 */
    protected HashMap<String, ArrayList<URL>> getDownloadURLs() {
    	HashMap<String, ArrayList<URL>> downloads = new HashMap<String, ArrayList<URL>>();
    	ArrayList<URL> URLList = new ArrayList<URL>();
    	for (int i = 0; i < tablePanel.getTable().getRowCount(); i++)
    		if ((boolean) tablePanel.getTable().getValueAt(i, 0)) {
    			try {
    				String title = (String)tablePanel.getTable().getValueAt(i, 1);
    				for (int j = 0; j < fileTypes.length; j++) {
    					URL url = new URL(defaultCatalogURL + title + "/" + title + fileTypes[j]);
    					URLList.add(url);
    				}
					downloads.put(title, URLList);
				} catch (MalformedURLException e) {
					System.out.println("URL Invalid");
				}
    		}
    	return downloads;
	}
    
    /**
     * Convert NodeList to ArrayList
     * @param nodeList
     * @return
     */
	private ArrayList<String> convertNodeList(NodeList nodeList) {
    	ArrayList<String> arrayList = new ArrayList<String>();
    	for (int i = 0; i < nodeList.getLength(); i++) {
        	arrayList.add(nodeList.item(i).getTextContent());
        }
    	return arrayList;
    }
    
	/**
	 * Clicking go parses index.xml on the chosen webpage. Populates the table.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
    private void actionGo() throws SAXException, IOException, ParserConfigurationException {
        URL verifiedUrl = verifyUrl(locationTextField.getText() + "index.xml");
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
            System.out.println("Invalid URL");
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