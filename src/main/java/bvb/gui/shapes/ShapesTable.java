package bvb.gui.shapes;

import javax.swing.JTable;
import javax.swing.UIManager;

import bvb.core.BigVolumeBrowser;

public class ShapesTable extends JTable
{
	final BigVolumeBrowser bvb;
	
	public ShapesTable(BigVolumeBrowser bvb_, final ShapesTableModel model)
	{
		this.setModel( model );
		bvb = bvb_;
		setRowHeight( ( int ) Math.round( UIManager.getDefaults().getFont( "Table.font" ).getSize() * 1.5 ) );
		setFillsViewportHeight(true);
		setShowGrid( false );
		
		getColumnModel().getColumn( ShapesTableModel.IS_VISIBLE_COLUMN ).setMinWidth( 20 );
		getColumnModel().getColumn( ShapesTableModel.IS_VISIBLE_COLUMN ).setPreferredWidth( 5 );

	}
}
