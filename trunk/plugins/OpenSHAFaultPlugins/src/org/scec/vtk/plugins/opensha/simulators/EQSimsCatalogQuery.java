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
public class EQSimsCatalogQuery extends JFrame
         
        implements HyperlinkListener {
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
        displayEditorPane.addHyperlinkListener(this);
         
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(southbuttonPanel, BorderLayout.SOUTH);
        getContentPane().add(new JScrollPane(tablePanel),
                BorderLayout.CENTER);
        
        URL url = getClass().getResource("index.xml");
        file = new File(url.getPath());
        
    }
    
    private static void downloadURL(URL url, File toFile) throws IOException {
		System.out.println("downloading: "+url.toString()+"\nto: "+toFile.getAbsolutePath());
		InputStream is = url.openStream();
		DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
		FileOutputStream fout = new FileOutputStream(toFile);
		int b = dis.read();
		while (b >= 0) {
			fout.write(b);
			b = dis.read();
		}
		fout.flush();
		fout.close();
		System.out.println("done.");
	}
     
    protected void actionDownload() {
		// TODO Auto-generated method stub
		
	}

	// Exit this program.
    private void actionExit() {
        System.exit(0);
    }
     
    // Go back to the page viewed before the current page.
//    private void actionBack() {
//        URL currentUrl = displayEditorPane.getPage();
// 
//        int pageIndex = pageList.indexOf(currentUrl.toString());
//        try {
//            showPage(
//                    new URL((String) pageList.get(pageIndex - 1)), false);
//        } catch (Exception e) {}
//    }
//     
    // Go forward to the page viewed after the current page.
//    private void actionForward() {
//        URL currentUrl = displayEditorPane.getPage();
//        int pageIndex = pageList.indexOf(currentUrl.toString());
//        try {
//            showPage(
//                    new URL((String) pageList.get(pageIndex + 1)), false);
//        } catch (Exception e) {}
//    }
     
    // Load and show the page specified in the location text field.
    private void actionGo() throws SAXException, IOException, ParserConfigurationException {
        URL verifiedUrl = verifyUrl(locationTextField.getText());
        if (verifiedUrl != null) {
            showPage(verifiedUrl, true);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputStream stream = verifiedUrl.openStream();
            org.w3c.dom.Document document = documentBuilder.parse(stream);
            String usr = ((org.w3c.dom.Document) document).getElementsByTagName("authors").item(0).getTextContent();
            System.out.println(usr);
            ArrayList<String> n = new ArrayList<String>();
            n.add(usr);
            tablePanel = new CheckAllTable(n, "Catalog List");
            //tablePanel.addColumn;
            //String pwd = document.getElementsByTagName("password").item(0).getTextContent();
        } else {
            showError("Invalid URL");
        }
    }
     
    // Show dialog box with error message.
    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage,
                "Error", JOptionPane.ERROR_MESSAGE);
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
     
  /* Show the specified page and add it to
     the page list if specified. */
    private void showPage(URL pageUrl, boolean addToList) {
        // Show hour glass cursor while crawling is under way.
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         
        try {
            // Get URL of page currently being displayed.
            URL currentUrl = displayEditorPane.getPage();
             
            // Load and display specified page.
            displayEditorPane.setPage(pageUrl);
             
            // Get URL of new page being displayed.
            URL newUrl = displayEditorPane.getPage();
             
            // Add page to list if specified.
            if (addToList) {
                int listSize = pageList.size();
                if (listSize > 0) {
                    int pageIndex =
                            pageList.indexOf(currentUrl.toString());
                    if (pageIndex < listSize - 1) {
                        for (int i = listSize - 1; i > pageIndex; i--) {
                            pageList.remove(i);
                        }
                    }
                }
                pageList.add(newUrl.toString());
            }
             
            // Update location text field with URL of current page.
            locationTextField.setText(newUrl.toString());
             
            // Update buttons based on the page being displayed.
//            updateButtons();
        } catch (Exception e) {
            // Show error messsage.
            showError("Unable to load page");
        } finally {
            // Return to default cursor.
            setCursor(Cursor.getDefaultCursor());
        }
    }
     
  /* Update back and forward buttons based on
     the page being displayed. */
//    private void updateButtons() {
//        if (pageList.size() < 2) {
//            backButton.setEnabled(false);
//            forwardButton.setEnabled(false);
//        } else {
//            URL currentUrl = displayEditorPane.getPage();
//            int pageIndex = pageList.indexOf(currentUrl.toString());
//            backButton.setEnabled(pageIndex > 0);
//            forwardButton.setEnabled(
//                    pageIndex < (pageList.size() - 1));
//        }
//    }
     
    // Handle hyperlink's being clicked.
    public void hyperlinkUpdate(HyperlinkEvent event) {
        HyperlinkEvent.EventType eventType = event.getEventType();
        if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
            if (event instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent linkEvent =
                        (HTMLFrameHyperlinkEvent) event;
                HTMLDocument document =
                        (HTMLDocument) displayEditorPane.getDocument();
                document.processHTMLFrameHyperlinkEvent(linkEvent);
            } else {
                showPage(event.getURL(), true);
            }
        }
    }
     
    // Run the Mini Browser.
//    @SuppressWarnings("deprecation")
//	public static void main(String[] args) {
//        EQSimsCatalogQuery browser = new EQSimsCatalogQuery();
//        browser.show();
//    }
}