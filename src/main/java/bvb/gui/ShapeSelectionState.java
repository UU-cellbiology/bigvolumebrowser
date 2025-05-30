package bvb.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import bvb.gui.shapes.ShapesTable;
import bvb.shapes.BasicShape;


public class ShapeSelectionState
{
	
	private final Supplier< List< BasicShape > > selectedShapes;
	
	private ArrayList<Listener> listeners =	new ArrayList<>();
	
	public ShapesTable table = null;
	
	public static interface Listener 
	{
		public void selectionShapesChanged(List< BasicShape > shList);

	}
	
	public ShapeSelectionState(final ShapesTable table)
	{
		this( table::getSelectedShapes );
		table.getSelectionModel().addListSelectionListener( e -> updateSelection() );
		this.table = table;
	}
	
	private ShapeSelectionState(final Supplier< List< BasicShape > > selectedShapesList)
	{
		this.selectedShapes = selectedShapesList;
	}
	
	public synchronized void updateSelection()
	{
		for(Listener l : listeners)
			l.selectionShapesChanged(selectedShapes.get());
	}
	public void addShapesSelectionStateListener(Listener l) 
	{
        listeners.add(l);
    }
}
