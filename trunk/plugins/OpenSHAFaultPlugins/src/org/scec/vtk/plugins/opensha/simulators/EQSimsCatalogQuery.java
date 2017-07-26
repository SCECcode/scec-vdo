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
    // These are the buttons for iterating through the page list.
    @SuppressWarnings("unused")
	private JButton Download;
     
    // Page location text field.
    private JTextField locationTextField;
     
    // Editor pane for displaying pages.
    private JEditorPane displayEditorPane;
     
    // Browser's list of pages that have been visited.
    private ArrayList<String> pageList = new ArrayList<String>();
    
    private String defaultCatalogURL = "http://rsqsim.usc.edu/catalogs/index.xml";
    private File file;
    private CheckAllTable tablePanel;
     
    // Constructor for Mini Web Browser.
    public EQSimsCatalogQuery(ArrayList<String> data, String tableTitle) {
        // Set application title.
        super("Mini Browser");
         
        // Set window size.
        setSize(640, 480);
         
        // Handle closing events.
        setVisible(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                actionExit();
//            }
//        });
//         
       
        JPanel buttonPanel = new JPanel();
//        
        locationTextField = new JTextField(defaultCatalogURL);
        locationTextField.setSize(300, 50);
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
        buttonPanel.add(locationTextField);
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
        buttonPanel.add(goButton);
        
//        this.setLayout(new FlowLayout());
        
        JPanel southbuttonPanel = new JPanel();
//        buttonPanel.add(southbuttonPanel, FlowLayout.RIGHT);
        southbuttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JButton Download = new JButton ("Download");
        southbuttonPanel.add(Download);
//        this.add(southbuttonPanel,BorderLayout.SOUTH);

        Download.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionDownload();

                
            }
        });
         
        // Set up page display.
        
        displayEditorPane = new JEditorPane();
        displayEditorPane.setContentType("text/html");
        displayEditorPane.setEditable(false);
         
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(southbuttonPanel, BorderLayout.SOUTH);
        ArrayList<String> s = new ArrayList<String>();
        s.add("sdfa");
        tablePanel = new CheckAllTable(s, "wow");
       // getContentPane().add(tablePanel,
        //        BorderLayout.CENTER);
        
        URL url = getClass().getResource("index.xml");
        file = new File(url.getPath());
        
    }
     
    protected void actionDownload() {
		// TODO Auto-generated method stub
		
	}

	// Exit this program.
    private void actionExit() {
        System.exit(0);
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
            //showPage(verifiedUrl, true);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = verifiedUrl.openStream();
            org.w3c.dom.Document document = documentBuilder.parse(stream);
            String[] tags = {"authors", "date", "title", "description", "region"};
            for (int i = 0; i < tags.length; i++) {
            	NodeList nodeList = ((org.w3c.dom.Document) document).getElementsByTagName(tags[i]);
            	 ArrayList<String> arrayList = convertNodeList(nodeList);
            }
            this.getContentPane().remove(tablePanel);
           // tablePanel = new CheckAllTable(n, "Catalog List");
            this.getContentPane().add(tablePanel, BorderLayout.CENTER);
            this.pack();
            //tablePanel.addColumn;
        } else {
            
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