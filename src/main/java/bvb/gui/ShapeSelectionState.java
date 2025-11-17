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
