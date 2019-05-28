package org.scec.vtk.plugins.opensha.ucerf3Rups;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.util.CustomFileFilter;
import org.scec.vtk.commons.opensha.gui.FaultHighlighter;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.ComparisonColorer;
import org.scec.vtk.plugins.opensha.ucerf3Rups.colorers.ComparisonColorer.ComparePlotType;

import scratch.UCERF3.FaultSystemSolution;

public class RuptureComparisonViewer extends JFrame implements MouseListener, ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FaultSystemSolution sol1;
	private FaultSystemSolution sol2;
	
	private JTable table;
	
	private ArrayList<ComparePlotType> comps;
	
	private FaultHighlighter highlight;
	
	private JFileChooser chooser;
	private CustomFileFilter csvFilter = new CustomFileFilter(".csv", "CSV file");
	private CustomFileFilter txtFilter = new CustomFileFilter(".txt", "TXT file (tab/space delimeted)");
	private JButton exportButton = new JButton("Export to File");
	
	public RuptureComparisonViewer(FaultSystemSolution sol1, FaultSystemSolution sol2, FaultHighlighter highlight) {
		this.sol1 = sol1;
		this.sol2 = sol2;
		this.highlight = highlight;
		comps = new ArrayList<ComparisonColorer.ComparePlotType>();
		for (ComparePlotType comp : ComparePlotType.values())
			comps.add(comp);
		table = new JTable(new CompTableModel());
		table.setAutoCreateRowSorter(true);
		int absDiffCol = CompTableModel.num_static_cols+comps.indexOf(ComparePlotType.ABSOLUTE_DIFFERENCE);
		// ascending sort
		table.getRowSorter().toggleSortOrder(absDiffCol);
		// descending sort (what we want)
		table.getRowSorter().toggleSortOrder(absDiffCol);
		table.addMouseListener(this);
		table.setDefaultRenderer(Double.class, new StringRenderer());
		
		JScrollPane scroll = new JScrollPane(table);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(exportButton, BorderLayout.SOUTH);
		
		exportButton.addActionListener(this);
		
		this.setContentPane(panel);
		
		this.setSize(900, 600);
		this.setVisible(true);
	}
	
	private class CompTableModel extends AbstractTableModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private static final int num_static_cols = 3;

		@Override
		public int getRowCount() {
			return sol1.getRupSet().getNumRuptures();
		}

		@Override
		public int getColumnCount() {
			return num_static_cols+comps.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			double rate1 = sol1.getRateForRup(rowIndex);
			double rate2 = sol2.getRateForRup(rowIndex);
			switch (columnIndex) {
			case 0:
				return rowIndex;
				
			case 1:
				return rate1;
			
			case 2:
				return rate2;

			default:
				int compInd = columnIndex - num_static_cols;
				if (compInd >= comps.size())
					throw new IllegalStateException();
				ComparePlotType comp = comps.get(compInd);
				return ComparisonColorer.getComparison(comp, rate1, rate2);
			}
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Rup ID";
				
			case 1:
				return "Rate 1";
			
			case 2:
				return "Rate 2";

			default:
				int compInd = column - num_static_cols;
				if (compInd >= comps.size())
					throw new IllegalStateException();
				ComparePlotType comp = comps.get(compInd);
				return comp.toString();
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0)
				return Integer.class;
			return Double.class;
		}
	}
	
	public class StringRenderer extends DefaultTableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return super.getTableCellRendererComponent(table, value.toString(), isSelected, hasFocus,
					row, column);
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
			int row = table.rowAtPoint(e.getPoint());
			if (row >= 0) {
				int rupID = table.getRowSorter().convertRowIndexToModel(row);
				System.out.println("Displaying rup at row: "+row+", rupID: "+rupID);
				highlight.unHilight();
				highlight.highlightFaults(sol1.getRupSet().getSectionsIndicesForRup(rupID), Color.GREEN);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == exportButton) {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.addChoosableFileFilter(csvFilter);
				chooser.addChoosableFileFilter(txtFilter);
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(csvFilter);
			}
			
			int response = chooser.showSaveDialog(this);
			
			if (response == JFileChooser.APPROVE_OPTION) {
				try {
					CSVFile<String> csv = new CSVFile<String>(true);
					
					TableModel model = table.getModel();
					
					ArrayList<String> header = new ArrayList<String>();
					for (int col=0; col<model.getColumnCount(); col++)
						header.add(model.getColumnName(col));
					csv.addLine(header);
					
					int rows = model.getRowCount();
					
					for (int i=0; i<rows; i++) {
						int row = table.getRowSorter().convertRowIndexToModel(i);
						ArrayList<String> line = new ArrayList<String>();
						for (int col=0; col<model.getColumnCount(); col++)
							line.add(model.getValueAt(row, col).toString());
						csv.addLine(line);
					}
					
					File file = chooser.getSelectedFile();
					
					CustomFileFilter filter = (CustomFileFilter)chooser.getFileFilter();
					
					if (!file.getName().toLowerCase().endsWith(filter.getExtension()))
						file = new File(file.getAbsolutePath()+filter.getExtension());
					
					if (filter == csvFilter)
						csv.writeToFile(file);
					else
						csv.writeToTabSeparatedFile(file, 1);
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this,
							"An error occured during export, see console output" +
							" for more details.\n"+e1.toString(), "Error Exporting File",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

}
