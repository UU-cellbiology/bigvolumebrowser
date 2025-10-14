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
package bvb.gui.clip;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import bdv.util.BoundedRange;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.Clippable3D;
import bvvpg.ui.panels.BoundedRangePanelPG;

public class ClipRangePanel extends JPanel
{

	private static final long serialVersionUID = 1885320351623882576L;

	final ClipSetups clipSetups;
	
	private BoundedRangePanelPG [] clipAxesPanels = new BoundedRangePanelPG[3];

	private boolean blockUpdates = false;
	

	public ClipRangePanel(final ClipSetups clipSetups_) 
	{
		super();

		clipSetups = clipSetups_;
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);

		cd.gridy = 0;
		cd.gridx = 0;
		cd.fill = GridBagConstraints.BOTH;
		cd.weightx = 1.0;
		final JPopupMenu [] menus = new JPopupMenu[3];
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			clipAxesPanels[d] = new BoundedRangePanelPG();
			menus[d] = new JPopupMenu();
			menus[d].add( runnableItem(  "set bounds ...", clipAxesPanels[d]::setBoundsDialog ) );
			menus[d].add( runnableItem(  "shrink bounds to selection", clipAxesPanels[d]::shrinkBoundsToRange ) );

			this.add(clipAxesPanels[d],cd);
		}
		menus[0].add( runnableItem(  "reset bounds", () -> resetBounds(0)));
		menus[1].add( runnableItem(  "reset bounds", () -> resetBounds(1)));
		menus[2].add( runnableItem(  "reset bounds", () -> resetBounds(2)));
	
		clipAxesPanels[0].setPopup( () -> menus[0] );
		clipAxesPanels[1].setPopup( () -> menus[1] );
		clipAxesPanels[2].setPopup( () -> menus[2] );

		clipAxesPanels[0].changeListeners().add( () -> updateClipAxisRangeBounds(0));
		clipAxesPanels[1].changeListeners().add( () -> updateClipAxisRangeBounds(1));
		clipAxesPanels[2].changeListeners().add( () -> updateClipAxisRangeBounds(2));
	
		updateGUI();
	}
	
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i = 0; i < 3; i++)
		{
			clipAxesPanels[i].setEnabled( bEnabled );
		}
	}
	
	synchronized void updateGUI()
	{
		if(!clipSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
	
		blockUpdates = true;
		BoundedRange [] range = new BoundedRange[3];
		boolean bFirstCS = true;
		boolean [] allRangesEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allRangesEqual[d] = true;
		}

		//update bounds
		final double [] min = new double [3];
		final double [] max = new double [3];
		
		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			if(objCl.getClipState() != 0)
			{
				final Bounds3D bounds = new Bounds3D(clipSetups.clipAxesBounds.getBounds( objCl ));
				final double [] minBound = bounds.getMinBound();
				final double [] maxBound = bounds.getMaxBound();
				final RealInterval clipInterval = objCl.getClipInterval();
				if(clipInterval == null)
				{
					for(int d=0;d<3;d++)
					{
						min[d] = minBound[d];
						max[d] = maxBound[d];
					}
					objCl.setClipInterval( new FinalRealInterval(min,max));
				}
				else
				{
					clipInterval.realMin( min );
					clipInterval.realMax( max );
				}
//				final AffineTransform3D clipTr = new AffineTransform3D();
//				objCl.getClipTransform( clipTr );
//				clipTr.apply( min, min );
//				clipTr.apply( max, max );
//				clipTr.apply( minBound, minBound );
//				clipTr.apply( maxBound, maxBound );
				if(clipSetups.bLocalCoordinates)
				{
					//convert to relative			
					//double [] relShift = clipSetups.getSourceMinWithScaleTranslation( obj );
					//double [] clipCenter = clipSetups.clipCenters.getCenters( objCl );
					double [] relShift = clipSetups.getCurrentObjectCenter( obj );
					for(int d=0;d<3;d++)
					{
						//relShift[d]-=clipCenter[d];
						min[d] -= relShift[d];
						max[d] -= relShift[d];
						
						minBound[d] -= relShift[d];
						maxBound[d] -= relShift[d];	
//						min[d] -= clipCenter[d];
//						max[d] -= clipCenter[d];
//						
//						minBound[d] -= clipCenter[d];
//						maxBound[d] -= clipCenter[d];	
					}
				}
				if(bFirstCS)
				{
					for (int d=0; d<3; d++)
					{
						range[d] = new BoundedRange( minBound[d], maxBound[d], min[d], max[d] );
					}
					bFirstCS = false;
				}
				else
				{
					for (int d = 0; d < 3; d++)
					{
						final BoundedRange axisRange = new BoundedRange( minBound[d], maxBound[d], min[d], max[d] );
						allRangesEqual[d] &= Misc.compareBoundedRanges(range[d], axisRange );
						range[d] = range[d].join( axisRange );
					}
				}
			}
		}
		blockUpdates = false;
		if(!bFirstCS)
		{		
			final BoundedRange [] finalRange = range;
			final boolean [] isConsistent = allRangesEqual;
			SwingUtilities.invokeLater( () -> {
				synchronized ( ClipRangePanel.this )
				{
					blockUpdates = true;
					for (int d=0;d<3;d++)
					{
	
						clipAxesPanels[d].setConsistent( isConsistent[d] );
						clipAxesPanels[d].setRange( finalRange[d] );
					}
					blockUpdates = false;
				}
			} );
		}
	}
	
	public void updateClipAxisRangeBounds(int nAxis)
	{
		
		if(!clipSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		final BoundedRange rangeOld = clipAxesPanels[nAxis].getRange();
		boolean bUpdateView = false;
		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			//convert to relative
			//double [] relShift = clipSetups.getSourceMinWithScaleTranslation( obj );
			double [] relShift =  clipSetups.getCurrentObjectCenter( obj );
			//double [] clipCenter = clipSetups.clipCenters.getCenters( objCl );
			BoundedRange range = null;
			if(clipSetups.bLocalCoordinates)
			{
				range = Misc.translateBoundedRange( rangeOld, relShift[nAxis] );
			}
			else
			{
				range = rangeOld;
			}
			//convert range to absolute
			RealInterval clipInt = objCl.getClipInterval();
			final Bounds3D bounds = clipSetups.clipAxesBounds.getBounds( objCl );
			if(clipInt == null)
			{
				clipInt  = new FinalRealInterval(bounds.getMinBound(),bounds.getMaxBound());
			}
			if(range.getMinBound() != bounds.getMinBound()[nAxis] || range.getMaxBound() != bounds.getMaxBound()[nAxis])
			{
				bounds.getMinBound()[nAxis] = range.getMinBound();
				bounds.getMaxBound()[nAxis] = range.getMaxBound();
				clipSetups.clipAxesBounds.setBounds( objCl, bounds );
			}
			
			final double [] min = clipInt.minAsDoubleArray();
			final double [] max = clipInt.maxAsDoubleArray();
			min[nAxis] = range.getMin();
			max[nAxis] = range.getMax();
			
			objCl.setClipInterval( new FinalRealInterval(min,max) );
			clipSetups.clipCenters.updateCenters( objCl );
			if(obj instanceof BasicShape)
			{
				bUpdateView = true;
			}
		}
		if( bUpdateView )
		{
			clipSetups.bvb.updateSceneRender();
		}
		blockUpdates = false;
		updateGUI();
	}
	
	/** sets bounds along the axis including all selected sources **/
	public void resetBounds(int nAxis)
	{
		
		if(!clipSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		Bounds3D range3D = null;

		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			if(range3D == null)
				range3D = clipSetups.clipAxesBounds.getDefaultBounds( objCl );
			else
				range3D = range3D.join( clipSetups.clipAxesBounds.getDefaultBounds( objCl ) );			
		}

		if(range3D != null)
		{
			final BoundedRange currRangeAxis = clipAxesPanels[nAxis].getRange();
			double bmin = range3D.getMinBound()[nAxis];
			double bmax = range3D.getMaxBound()[nAxis];
			double max = Math.min( bmax, currRangeAxis.getMax() );
			max = Math.max( max, bmin );
			double min = Math.max( bmin, currRangeAxis.getMin() );
			min = Math.min( max, min );
			final BoundedRange newRange = new BoundedRange (bmin, bmax, min, max);
			clipAxesPanels[nAxis].setRange( newRange );
			updateClipAxisRangeBounds(nAxis);
		}
	}
	
	void resetRange()
	{
		if(!clipSetups.selectedObjects.isAnythingSelected())
		{
			return;
		}
		final List< Object > objList = clipSetups.selectedObjects.getSelectedObjects();
		for ( final Object obj: objList)
		{
			final Clippable3D objCl = (Clippable3D)obj;
			if(objCl.getClipState() != 0)
			{
				Bounds3D range3D = clipSetups.clipAxesBounds.getDefaultBounds( objCl );
				if(range3D != null)
				{
					clipSetups.clipAxesBounds.setBounds( objCl, range3D );
					objCl.setClipInterval(new FinalRealInterval(range3D.getMinBound(),range3D.getMaxBound()));
					clipSetups.clipCenters.setCenters(objCl, clipSetups.clipCenters.getDefaultCenters( objCl ));
					clipSetups.clipCenterBounds.setBounds( objCl, clipSetups.clipCenterBounds.getDefaultBounds( objCl ) );
					clipSetups.updateClipTransform( objCl, null);
				}
			}
		}
		clipSetups.bvb.updateSceneRender();
		updateGUI();
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int i=0;i<3;i++)
		{
			clipAxesPanels[i].setSliderForeground( colors[i] );	
		}
	}
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
