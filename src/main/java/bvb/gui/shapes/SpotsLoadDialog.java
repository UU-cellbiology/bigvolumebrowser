/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.gui.shapes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.utils.Misc;
import ij.Prefs;

public class SpotsLoadDialog
{
	
	final BigVolumeBrowser bvb;

	JPanel pLoadSpots = null;

	public File fileSpots = null;
	
	final JTextField jlFileName = new JTextField("No file selected");
	
	final JLabel jStatus = new JLabel ("Status:");
	
	int panelWidth = 1000;
	
	int panelHeight = 400;
	
	final public JCheckBox cbHasHeader;
	
	final public JComboBox<String> cbSeparator;
	
	final public JComboBox<String> cbUnits;
	
	final public JComboBox<String> cbSize;
	
	final public ArrayList<JComboBox<String>> cbColumnsAssign = new ArrayList<>();
	
	String sStatus = "Error: ";
	
    JTable table = null;
    
    String[] headers = new String[3];
    
    final ArrayList<String[]> dataParsed = new ArrayList<>();
    
    boolean bParsedColumns = false;
    
    boolean bColumnsAssigned = false;
    
    final JButton butOk;
    
    final JButton butCancel;
    
    public boolean bAllSuccess = false;
    
	public boolean bLAZfile = false;
	
	public SpotsLoadDialog(BigVolumeBrowser bvb_)
	{
		bvb = bvb_;
		
		///OK/CANCEL BUTTONS		
		butOk = new JButton("OK");
		GridBagConstraints gbc = new GridBagConstraints();
		butOk.setEnabled( false );
		butOk.addActionListener(e -> {
			bAllSuccess = true;
			JOptionPane pane = getOptionPane((JComponent)e.getSource());
            pane.setValue(butOk);
			}); 
		butCancel = new JButton("Cancel");
		butCancel.addActionListener(e -> {
            JOptionPane pane = getOptionPane((JComponent)e.getSource());
            pane.setValue(butCancel);
			}); 

		//FILE SELECTION PANEL
		JPanel pFileSelect = new JPanel(new GridBagLayout());
		jlFileName.setHorizontalAlignment( SwingConstants.LEFT );
		
		Dimension minTxtFileDim = jlFileName.getMinimumSize();
		
		minTxtFileDim.width = 300;
		
		jlFileName.setMinimumSize( minTxtFileDim );
		jlFileName.setEditable( false );
		jlFileName.setText( "No file selected" );		

		JButton butSelectFile = new JButton("Select file..");
		butSelectFile.addActionListener( (e)-> {
			JFileChooser chooser = new JFileChooser(BVBSettings.lastDir);
	        chooser.setDialogTitle( "Open Table Data" );
	        
	        int returnVal = chooser.showOpenDialog(null);
	        
	        if(returnVal == JFileChooser.APPROVE_OPTION) 
	        {
	            BVBSettings.lastDir = chooser.getSelectedFile().getParent();
	            Prefs.set( "BVB.lastDir",  BVBSettings.lastDir );
	            fileSpots = chooser.getSelectedFile();
	            
	            jlFileName.setText( Misc.getSourceStyleName( fileSpots ));
	            jlFileName.setCaretPosition( 0 );
	            bParsedColumns = false;
	            updateWindow();
	            //pLoadSpots.updateUI();
	        }
		});
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.2;
		pFileSelect.add(jlFileName, gbc);
		gbc.gridx ++;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.NONE;
		pFileSelect.add(butSelectFile, gbc);	

		///HEADER/SEPARATOR/UNITS/SIZE PANEL
		JPanel pHeaderSeparator = new JPanel(new GridBagLayout());
		cbHasHeader = new JCheckBox("Has header?");
		cbHasHeader.setHorizontalTextPosition( SwingConstants.LEFT );
		cbHasHeader.setSelected( Prefs.get( "BVB.bSpotsImportHasHeader", true ));
		cbHasHeader.addItemListener( (e)->{
			bParsedColumns = false;
			updateWindow();							
		} );
		String[] sSeparators = { ",", ";", "space", "tab" };
		cbSeparator = new JComboBox<>(sSeparators);
		cbSeparator.setSelectedIndex( (int)Prefs.get( "BVB.nSpotsSeparator", 0 ) );
		
		cbSeparator.addActionListener( (e)->{
			bParsedColumns = false;
			updateWindow();				
			});
		String[] sUnits = { "milli", "micro", "nano"};
		cbUnits =  new JComboBox<>(sUnits);
		cbUnits.setSelectedIndex( (int)Prefs.get( "BVB.nSpotsUnits", 1 ) );
		
		
		String[] sSize = { "diameter", "radius", "SD"};
		cbSize =  new JComboBox<>(sSize);
		cbSize.setSelectedIndex( (int)Prefs.get( "BVB.nSpotsSize", 0 ) );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,10,0,40);
		pHeaderSeparator.add( cbHasHeader,gbc );
		gbc.gridx++;
		gbc.insets = new Insets(0,0,0,0);
		pHeaderSeparator.add(new JLabel("Separator"),gbc);
		gbc.gridx++;
		pHeaderSeparator.add(cbSeparator, gbc);
		gbc.gridx++;
		gbc.insets = new Insets(0,40,0,0);
		pHeaderSeparator.add(new JLabel("Units"),gbc);
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx++;
		pHeaderSeparator.add(cbUnits, gbc);
		
		gbc.gridx++;
		gbc.insets = new Insets(0,40,0,0);
		pHeaderSeparator.add(new JLabel("Size is"),gbc);
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx++;
		pHeaderSeparator.add(cbSize, gbc);

		///COLUMN ASSIGNMENT PANEL COORDINATES XYZT
		JPanel pColumnsAssignCoords = new JPanel();
		for (int i = 0; i < 4; i++)
		{
			cbColumnsAssign.add(  new JComboBox<>(new String[] {"NA"}));
			cbColumnsAssign.get( i ).setEnabled( false );
			cbColumnsAssign.get( i ).addActionListener( (e)-> updateWindow());
		}
		
		String [] sColSelectionLabels = new String [] {"X", "Y", "Z", "T", "SizeX", "SizeY", "SizeZ", "Property"};		

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,5,0,5);
		pColumnsAssignCoords.add(new JLabel("Coords:") , gbc); 
		gbc.insets = new Insets(0,0,0,0);
		for (int i = 0; i < 4; i++)
		{
			gbc.gridx++;
			pColumnsAssignCoords.add(new JLabel(sColSelectionLabels[i]), gbc);
			gbc.gridx++;
			pColumnsAssignCoords.add( cbColumnsAssign.get( i ), gbc );
		}

		///COLUMN ASSIGNMENT PANEL SIZES/PROPERTY
		JPanel pColumnsAssignSize = new JPanel();
		for (int i = 4; i < 8; i++)
		{
			cbColumnsAssign.add(  new JComboBox<>(new String[] {"NA"}));
			cbColumnsAssign.get( i ).setEnabled( false );
			cbColumnsAssign.get( i ).addActionListener( (e)-> updateWindow());
		}
	
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0,0,0,0);
		for (int i = 4; i < 8; i++)
		{
			pColumnsAssignSize.add(new JLabel(sColSelectionLabels[i]), gbc);
			gbc.gridx++;
			pColumnsAssignSize.add( cbColumnsAssign.get( i ), gbc );
			gbc.gridx++;
		}
		
		
		//TABLE WITH PARSED FILE CONTENT

		table = new JTable(dummyTableModel());
		table.setFillsViewportHeight(true);
		table.setEnabled( false );
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		
		JScrollPane scrollTable = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
		///FINAL PANEL
		pLoadSpots = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		pLoadSpots.add(pFileSelect, gbc);
		
		gbc.gridx = 0;
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.anchor  = GridBagConstraints.WEST;
	    gbc.gridy++;
	    gbc.insets = new Insets(10,2,10,2);
		pLoadSpots.add(jStatus, gbc);
		
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 0;
	    gbc.gridy++;
		pLoadSpots.add(pHeaderSeparator, gbc);

		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 0;
	    gbc.gridy++;
		pLoadSpots.add(pColumnsAssignCoords, gbc);		
		
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 0;
	    gbc.gridy++;
		pLoadSpots.add(pColumnsAssignSize, gbc);		
		
		
		gbc.gridx = 0;
	    gbc.gridy++;
	    gbc.weighty = 0.2;
	    gbc.fill = GridBagConstraints.BOTH;
		pLoadSpots.add(scrollTable, gbc);
		
		//filler
		gbc.gridx = 0;
	    gbc.gridy++;
	    gbc.weightx = 0.01;
	    gbc.weighty = 0.01;
	    pLoadSpots.add(new JLabel(), gbc);
	    
		gbc.gridy ++;
		gbc.anchor = GridBagConstraints.SOUTHEAST;
		pLoadSpots.setPreferredSize( new Dimension(panelWidth,panelHeight) );

	}
	
    protected JOptionPane getOptionPane(JComponent parent) {
        JOptionPane pane = null;
        if (!(parent instanceof JOptionPane)) {
            pane = getOptionPane((JComponent)parent.getParent());
        } else {
            pane = (JOptionPane) parent;
        }
        return pane;
    }
	
	public void show()
	{
		  JOptionPane.showOptionDialog(
                  null, 
                  pLoadSpots, 
                  "Load spots/particle", 
                  JOptionPane.YES_NO_OPTION, 
                  JOptionPane.PLAIN_MESSAGE, 
                  null, 
                  new Object[]{butOk, butCancel}, 
                  butOk);
	}
	
	boolean analyzeFile()
	{
		boolean bSameColN = true;
		boolean bOut = true;
		int nRow = 0;
		int nHeaderCols = -1;
		dataParsed.clear();
		sStatus = "Error: ";
		String headerUnParsed = "";
		String[] sSeparators = { ",", ";", " ", "\t" };
		String sSeparator = sSeparators[cbSeparator.getSelectedIndex()];
		
		try ( BufferedReader br = new BufferedReader(new FileReader(fileSpots));) 
		{
			String [] la;
			String line;
			
			while(true)
			{
			
				line = br.readLine();
				if(line == null)
					break;
				nRow++;
				la = line.split(sSeparator);
				if( la.length > 100 )
				{
					if(bOut)
					{
						sStatus = sStatus +" too many (>100) columns. Check separator?";
						bOut = false;
					}
				}
				if( la.length < 2 )
				{
					
					if(bOut)
					{
						bOut = false;
						sStatus = sStatus +" too little (<2) columns. Check separator?";
					}
						
				}
				//header
				if(nRow == 1)
				{
					headerUnParsed = line;
					headers = new String[la.length];
					nHeaderCols = la.length;
					for(int i=0; i<la.length; i++)
					{
						if(cbHasHeader.isSelected())
						{
							headers[i] = String.valueOf( la[i]);
						}
						else
						{
							headers[i] = "Column" + Integer.toString( i+1 );
						}
					}
					if(!cbHasHeader.isSelected())
					{
						dataParsed.add( la );
					}
				}
				else
				{
					//number of header or first row is not the same as the second/following
					if(nHeaderCols != la.length)
					{
						if(nHeaderCols > la.length)
						{
							if(bOut)
							{
								bOut = false;
								sStatus = sStatus +" # headers column larger than # data colums";							
							}		
							if(cbHasHeader.isSelected())
							{
								headers = new String [] {headerUnParsed};
							}
							else
							{	
								headers = new String [] {"Column1"};
							}
							if(nRow == 2 &&  !cbHasHeader.isSelected())
							{
								dataParsed.clear();
								dataParsed.add( new String[] {headerUnParsed });
							}
							dataParsed.add( new String[] {line} );
						}
						else
						{
							if(bSameColN)
							{
								bSameColN = false;
								sStatus = "Warning: # headers column smaller than # data colums";
							}
							String [] trunc = new String[nHeaderCols];
							for(int i=0;i<nHeaderCols;i++)
							{
								trunc[i]=la[i];
							}
							dataParsed.add( trunc );
						}
					}
					else
					{
						dataParsed.add( la );
					}
				}
				if(nRow == 5)
				{
					break;
				}
			}
		}
		
		catch ( FileNotFoundException exc )
		{
			exc.printStackTrace();
		}
		catch ( IOException exc )
		{		
			exc.printStackTrace();
		}
		if(nRow == 0 || (nRow == 1 && cbHasHeader.isSelected()))
		{
			if(bOut)
			{
				sStatus = sStatus +" too little rows. Check file?";
				bOut = false;
			}
		}
		if(bOut && bSameColN)
		{
			sStatus = "Status: file columns parsed ok.";
		}

			
		
		return bOut;
	}
	
	void updateWindow()
	{
		if (fileSpots != null)
		{
			//check if it is LAS LAZ file
			
			String fileName = fileSpots.getName();
			if(fileName.toLowerCase().endsWith( "las" )||fileName.toLowerCase().endsWith( "laz" ))
			{
				bLAZfile = true;
				jStatus.setText( "Ready to load LAS/LAZ dataset");
				butOk.setEnabled( true );
				for(int i = 0; i<cbColumnsAssign.size(); i++ )
				{
					cbColumnsAssign.get( i).setEnabled( false );	
				}
				table.setEnabled( false );
				table.setModel(dummyTableModel());
				cbHasHeader.setEnabled( false );
				cbSeparator.setEnabled( false );
				cbUnits.setEnabled( false );
				cbSize.setEnabled( false );
			}
			else
			{
				cbHasHeader.setEnabled( true );
				cbSeparator.setEnabled( true );
				cbUnits.setEnabled( true );
				cbSize.setEnabled( true );
				boolean bTriggerHeadersUpdate = false;
		        if(!bParsedColumns)
		        {
		        	bParsedColumns = analyzeFile();
		        	table.setModel(parsedTableModel());
		        	jStatus.setText( sStatus );
		        	bTriggerHeadersUpdate = true; 
		        	butOk.setEnabled( false );
		        }
	        	if(bTriggerHeadersUpdate)
	        	{
	        		updateAssignColumnsContent(bParsedColumns);
	        	}
	        	else
	        	{
		        	if(bParsedColumns)
		        	{
		        		checkColumnAssignment();
		        	}
	        	}
			}
		}

	}
	
	void checkColumnAssignment()
	{
		HashMap< JComboBox<String>, Integer> mapCols = new HashMap<>();
		
		int nCoordAssign = 0;
		for(int i = 0; i < cbColumnsAssign.size(); i++)
		{
			final JComboBox<String> cbBox = cbColumnsAssign.get( i );
			if(cbBox.getSelectedIndex() != 0)
			{
				mapCols.put( cbBox,  cbBox.getSelectedIndex() );
				if(i <= 2)
				{
					nCoordAssign++;
				}
			}
		}
		
		Collection< Integer > colNames = mapCols.values();
		Set<Integer> colUniq = new HashSet<>();
		colUniq.addAll( colNames );
		if(nCoordAssign > 1)
		{
			butOk.setEnabled( true );
			if(colUniq.size() != mapCols.size())
			{
				jStatus.setText( "Warning: some columns assigned to values multiple times!" );
			}
			else
			{
				jStatus.setText("Ready to parse.");
			}
		}
		else
		{
			butOk.setEnabled( false );
			jStatus.setText("Need to assign at least 2 coordinate columns.");
		}
	}
	
	void updateAssignColumnsContent(boolean bEnabled)
	{
		
		if(bEnabled)
		{										
			final String [] colNames = new String[headers.length+1];
			colNames[0] = "NA";
			for(int i = 0; i < headers.length; i++)
			{
				colNames[i+1] = headers[i].substring( 0, Math.min(10, headers[i].length()) );
			}
		
			for(JComboBox<String> cbBox: cbColumnsAssign)
			{
				cbBox.setEnabled( bEnabled );
				cbBox.setModel(  new DefaultComboBoxModel<>(colNames) );
			}
		}
		else
		{
			for(JComboBox<String> cbBox: cbColumnsAssign)
			{
				cbBox.setSelectedIndex( 0 );
				cbBox.setEnabled( bEnabled );
			}
			
		}
		
	}
	
	DefaultTableModel parsedTableModel()
	{
		if(dataParsed.size()==0)
		{
			return dummyTableModel();
		}
		 String [][] data = new String[dataParsed.size()][headers.length];
		 for(int i = 0; i < dataParsed.size(); i++)
		 {
			 for(int j = 0; j < headers.length; j++)
			 {
				 data[i][j] = dataParsed.get( i )[j];
			 }
		 }
		DefaultTableModel tableModel = new DefaultTableModel(data, headers) {

			    @Override
			    public boolean isCellEditable(int row, int column) {
			       //all cells false
			       return false;
			    }
			};
			return tableModel;
	}
	
	DefaultTableModel dummyTableModel()
	{
	    String[] columnNames = new String[3];
	    
	    Float [][] data = null;
	    
		data = new Float[3][3];

		for(int i = 0; i < 3; i++)
		{
			columnNames[i] = "Column"+Integer.toString( i+1 );
			for(int j = 0; j < 3; j++)
			{
				data[i][j] = new Float(0.0f);
			}
		}
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) 
		{

		    @Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
		       return false;
		    }
		};
		return tableModel;
	}
}
