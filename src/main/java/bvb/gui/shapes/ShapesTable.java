package bvb.gui.shapes;

import java.awt.Point;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.imglib2.RealInterval;
import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicShape;


public class ShapesTable extends JTable
{
	final BigVolumeBrowser bvb;
	final ShapesTableModel model;
	
	public ShapesTable(BigVolumeBrowser bvb_, final ShapesTableModel model)
	{
		this.model = model; 
		this.setModel( this.model );
		bvb = bvb_;
		setRowHeight( ( int ) Math.round( UIManager.getDefaults().getFont( "Table.font" ).getSize() * 1.5 ) );
		setFillsViewportHeight(true);
		setShowGrid( false );
		
		getColumnModel().getColumn( ShapesTableModel.IS_VISIBLE_COLUMN ).setMinWidth( 20 );
		getColumnModel().getColumn( ShapesTableModel.IS_VISIBLE_COLUMN ).setPreferredWidth( 5 );

	}
	
	// -- Process clicks on active and current checkboxes --
	// These clicks are consumed, because they should not cause selection changes, etc, in the table.

	private Point pressedAt;
	private boolean consumeNext = false;
	private long releasedWhen = 0;

	@Override
	protected void processMouseEvent( final MouseEvent e )
	{
		//if ( e.getModifiers() == InputEvent.BUTTON1_MASK)
		if(SwingUtilities.isRightMouseButton(e))
		{
			this.clearSelection();
		}
		
		else if(SwingUtilities.isLeftMouseButton(e))//||SwingUtilities.isRightMouseButton(e))
		{
			if ( e.getID() == MouseEvent.MOUSE_PRESSED )
			{
				final Point point = e.getPoint();
				pressedAt = point;
				final int vcol = columnAtPoint( point );
				final int vrow = rowAtPoint( point );
				if ( vcol >= 0 && vrow >= 0 )
				{
					
					final int mcol = convertColumnIndexToModel( vcol );
					if(e.getClickCount()==2)
					{
						final int mrow = convertRowIndexToModel( vrow );
						focusOnSelectedShapes();
						final RealInterval shapeBox = bvb.shapes.get( mrow ).boundingBox();
						if(shapeBox != null)
						{
							bvb.focusOnRealInterval( shapeBox );
							return;

						}
					}
					else
					{
						switch ( mcol )
						{
						case ShapesTableModel.IS_VISIBLE_COLUMN:
							final int mrow = convertRowIndexToModel( vrow );
							if ( isRowSelected( mrow ) )
							{
								e.consume();
								consumeNext = true;
							}
						}
					}
				}
			}
			else if ( e.getID() == MouseEvent.MOUSE_RELEASED )
			{
				if ( consumeNext )
				{
					releasedWhen = e.getWhen();
					consumeNext = false;
					e.consume();
				}

				if ( pressedAt == null )
					return;

				final Point point = e.getPoint();
				if ( point.distanceSq( pressedAt ) > 2 )
					return;

				final int vcol = columnAtPoint( point );
				final int vrow = rowAtPoint( point );
				if ( vcol >= 0 && vrow >= 0 )
				{
					final int mcol = convertColumnIndexToModel( vcol );
					switch ( mcol )
					{
					case ShapesTableModel.IS_VISIBLE_COLUMN:
						final int mrow = convertRowIndexToModel( vrow );
						final BasicShape shape = bvb.shapes.get(  mrow );
						if ( mcol == ShapesTableModel.IS_VISIBLE_COLUMN )
						{
							if ( isRowSelected( mrow ) )
							{
								setShapesVisible( getSelectedShapes(), !shape.isVisible() );
							}
							else
							{
								shape.setVisible( !shape.isVisible() );
							}
						}
					}
				}
			}
			else if ( e.getID() == MouseEvent.MOUSE_CLICKED )
			{
				if ( e.getWhen() == releasedWhen )
					e.consume();
			}
		}
		
		super.processMouseEvent( e );
	}

	public List<BasicShape> getSelectedShapes()
	{
		final List< BasicShape > shapes = new ArrayList<>();
		for ( final int row : getSelectedRows() )
			shapes.add( bvb.shapes.get( row ));
		return shapes;
	}
	
	void setShapesVisible(final List<BasicShape> shapes, boolean bVisible)
	{
		for(BasicShape shape:shapes)
		{
			shape.setVisible( bVisible );
		}
		updateUI();
	}
	void focusOnSelectedShapes()
	{
		final List< BasicShape > shapes = new ArrayList<>();
		for ( final int row : getSelectedRows() )
			shapes.add( bvb.shapes.get( row ));
		
	}
}
