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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	JFrame fLoadSpots = null;

	JPanel pLoadSpots = null;

	File fileSpots = null;
	
	final JTextField jlFileName = new JTextField("No file selected");
	
	JLabel jStatus = new JLabel ("Status:");
	
	int panelWidth = 600;
	int panelHeight = 400;
	
	JCheckBox cbHasHeader = null;
	
	JComboBox<String> cbSeparator = null;
	
	String sStatus = "Error: ";
	
    JTable table = null;
    
    String[] headers = new String[3];
    
    final ArrayList<String[]> dataParsed = new ArrayList<>();
    
    boolean bParsedColumns = false;
	
	public SpotsLoadDialog(BigVolumeBrowser bvb_)
	{
		bvb = bvb_;
	
		
		///OK/CANCEL PANEL		
		JPanel pOkCancel = new JPanel(new GridBagLayout());

		JButton butOk = new JButton("OK");
		GridBagConstraints gbc = new GridBagConstraints();
		butOk.setEnabled( false );
		JButton butCancel = new JButton("Cancel");
		butCancel.addActionListener(e -> {
			fLoadSpots.dispose();
			}); 
		//filler
		gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 0.01;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    pOkCancel.add(new JLabel(), gbc);
	    gbc.fill = GridBagConstraints.NONE;
	    gbc.weightx = 0.0;

		gbc.gridx ++;
		pOkCancel.add( butOk, gbc );
		gbc.gridx ++;
		pOkCancel.add( butCancel, gbc );

		
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

		///HEADER/SEPARATOR COLUMN
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
		cbSeparator.setSelectedIndex( 0 );
		
		cbSeparator.addActionListener( (e)->{
			bParsedColumns = false;
			updateWindow();				
			});
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
		
		//TABLE

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
		pLoadSpots.add(pOkCancel, gbc);


		fLoadSpots = new JFrame("Load spots/particles");
		fLoadSpots.add( pLoadSpots );
		fLoadSpots.setPreferredSize( new Dimension(panelWidth,panelHeight) );
	    java.awt.Point bvv_p = bvb.bvvFrame.getLocationOnScreen();
	    java.awt.Dimension bvv_d = bvb.bvvFrame.getSize();
	    fLoadSpots.setLocation(bvv_p.x + (int)Math.round(bvv_d.width*0.5- panelWidth*0.5), bvv_p.y+ (int)Math.round(bvv_d.height*0.5 - panelHeight*0.5));

	}
	public void show()
	{
		fLoadSpots.setVisible( true );
		fLoadSpots.pack();
	}
	
	boolean analyzeFile()
	{
		
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
					if(nHeaderCols != la.length)
					{
						if(bOut)
						{
							bOut = false;
							sStatus = sStatus +" # headers column not equal to # data colums";							
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
			// TODO Auto-generated catch block
			exc.printStackTrace();
		}
		catch ( IOException exc )
		{
			// TODO Auto-generated catch block
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
		if(bOut)
		{
			sStatus = "Status: file columns parsed ok.";
		}
		
		return bOut;
	}
	
	void updateWindow()
	{
		if (fileSpots != null)
		{
			
	        if(!bParsedColumns)
	        {
	        	bParsedColumns = analyzeFile();
	        	table.setModel(parsedTableModel());
	        }
	        jStatus.setText( sStatus );
		}

	}
	
	DefaultTableModel parsedTableModel()
	{
		if(dataParsed.size()==0)
		{
			return dummyTableModel();
		}
		 String [][] data = new String[dataParsed.size()][headers.length];
		 for(int i=0;i<dataParsed.size();i++)
		 {
			 for(int j=0;j<headers.length; j++)
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

		for(int i=0;i<3;i++)
		{
			columnNames[i] = "Column"+Integer.toString( i+1 );
			for(int j=0;j<3;j++)
			{
				data[i][j] = new Float(0.0f);
			}
		}
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {

		    @Override
		    public boolean isCellEditable(int row, int column) {
		       //all cells false
		       return false;
		    }
		};
		return tableModel;
	}
}
