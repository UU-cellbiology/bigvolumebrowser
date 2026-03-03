/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
