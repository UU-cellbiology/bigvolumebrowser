package bvb.gui.shapes;


import javax.swing.table.AbstractTableModel;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicShape;

public class ShapesTableModel extends AbstractTableModel
{
	final BigVolumeBrowser bvb;

	
	public static final int NAME_COLUMN = 0;
	
	public static final int IS_VISIBLE_COLUMN = 1;
	
	public ShapesTableModel(BigVolumeBrowser bvb_)
	{
		super();
		
		bvb = bvb_;
	}
	
	@Override
	public int getColumnCount()
	{	
		return 2;
	}

	@Override
	public int getRowCount()
	{
		return bvb.shapes.size();//model.getShapes().size();
	}
	
	@Override
	public String getColumnName( final int column )
	{
		switch( column )
		{
		case NAME_COLUMN:
			return "name";
		case IS_VISIBLE_COLUMN:
			return "visible";
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		BasicShape shape = bvb.shapes.get( rowIndex );
		switch( columnIndex )
		{
		case NAME_COLUMN:
			return shape.toString();
		case IS_VISIBLE_COLUMN:
			return shape.isVisible();
		default:
			throw new IllegalArgumentException();
		}
	
	}

	@Override
	public Class< ? > getColumnClass( final int columnIndex )
	{
		switch( columnIndex )
		{
		case NAME_COLUMN:
			return String.class;
		case IS_VISIBLE_COLUMN:
			return Boolean.class;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public boolean isCellEditable( final int rowIndex, final int columnIndex )
	{
		//return columnIndex != 0;
		return false;
	}
	
	
    @Override
	public void setValueAt(Object value, int row, int col) 
    {
		switch( col )
		{
		case IS_VISIBLE_COLUMN:
			bvb.shapes.get( row ).setVisible( ( boolean ) value );
		}

    }
       

}
