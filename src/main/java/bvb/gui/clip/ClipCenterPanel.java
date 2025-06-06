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

import net.imglib2.realtransform.AffineTransform3D;

import bdv.tools.brightness.ConverterSetup;
import bdv.tools.transformation.TransformedSource;
import bdv.util.BoundedValueDouble;
import bvb.shapes.BasicShape;
import bvb.utils.BoundedValueDoubleBVB;
import bvb.utils.Bounds3D;
import bvb.utils.Misc;
import bvb.utils.clip.ClipSetups;
import bvvpg.source.converters.GammaConverterSetup;
import bvvpg.ui.panels.BoundedValuePanelPG;

public class ClipCenterPanel extends JPanel
{

	final ClipSetups clipSetups;

	private BoundedValuePanelPG [] clipCenterPanels = new BoundedValuePanelPG[3];

	private boolean blockUpdates = false;	
	
	public ClipCenterPanel( final ClipSetups clipSetups_ ) 
	{
		super();
			
		clipSetups = clipSetups_;
		
		GridBagLayout gridbag = new GridBagLayout();
		
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);
		
		cd.gridwidth = 0;
		cd.gridy = 0;
		cd.gridx = 0;
		cd.fill = GridBagConstraints.BOTH;
		cd.weightx = 1.0;
		final JPopupMenu [] menus = new JPopupMenu[3];
		for(int d=0;d<3;d++)
		{
			cd.gridy++;
			
			clipCenterPanels[d] = new BoundedValuePanelPG( new BoundedValueDouble( 0.0, 1.0, 0.5 ));
			menus[d] = new JPopupMenu();
			menus[d].add( runnableItem(  "set bounds ...", clipCenterPanels[d]::setBoundsDialog ) );
			this.add(clipCenterPanels[d],cd);
		}

		menus[0].add( runnableItem(  "reset bounds", () -> resetBounds(0)));
		menus[1].add( runnableItem(  "reset bounds", () -> resetBounds(1)));
		menus[2].add( runnableItem(  "reset bounds", () -> resetBounds(2)));
	
		clipCenterPanels[0].setPopup( () -> menus[0] );
		clipCenterPanels[1].setPopup( () -> menus[1] );
		clipCenterPanels[2].setPopup( () -> menus[2] );
		
		clipCenterPanels[0].changeListeners().add( () -> updateClipCenter(0));
		clipCenterPanels[1].changeListeners().add( () -> updateClipCenter(1));
		clipCenterPanels[2].changeListeners().add( () -> updateClipCenter(2));
		
		//add source selection listener
		clipSetups.selectedObjects.addObjectSelectionListener(()->updateGUI());
		
		//add listener in case number of sources, etc change
		clipSetups.converterSetups.listeners().add( s -> updateGUI() );
		
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			clipCenterPanels[i].setEnabled( bEnabled );
		}
	}
	
	synchronized void updateGUI()
	{
		
		if(!clipSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		
		blockUpdates = true;
		BoundedValueDoubleBVB [] boundValue = new BoundedValueDoubleBVB[3];
		boolean bFirstCS = true;
		boolean [] allCenterEqual = new boolean [3];
		for (int d=0;d<3;d++)
		{
			allCenterEqual[d] = true;
		}
		
		if(clipSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = clipSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs: csList)
			{
				if(((GammaConverterSetup)cs).clipActive())
				{
					final Bounds3D bounds = new Bounds3D(clipSetups.clipCenterBounds.getBounds( cs ));
					final double [] minBound = bounds.getMinBound();
					final double [] maxBound = bounds.getMaxBound();
					
					double [] center = new double [3];
					final double [] centerIn = clipSetups.clipCenters.getCenters( cs );
					
					//convert to relative if needed
					//double [] relShift = clipSetups.getSourceMinWithScaleTranslation( cs );
//					double [] relShift = Misc.getSourceMinNoFixedTransformAllTP( clipSetups.converterSetups.getSource( cs ).getSpimSource() );
//					final double [] scales = centerclipSetups.bvb.controlPanel.tabPanelView.transformPanel.transformSetups.transformScale.getScale( cs );
//					for(int d=0;d<3; d++)
//						relShift[d] *= scales[d];
					
					if(clipSetups.bLocalCoordinates)
					{
						AffineTransform3D tr = new AffineTransform3D();
						(( TransformedSource< ? > )clipSetups.converterSetups.getSource( cs ).getSpimSource()).getFixedTransform( tr );
						tr.inverse().apply( centerIn, center );
						//final double [] relShift = Misc.getSourceMinAllTP( clipSetups.converterSetups.getSource( cs ).getSpimSource() );
						final double [] relShift = Misc.getSourceMinNoFixedTransformAllTP( clipSetups.converterSetups.getSource( cs ).getSpimSource() );
						for(int d=0;d<3;d++)
						{
							//center[d] = centerIn[d]-relShift[d];
							minBound[d] -= relShift[d];
							maxBound[d] -= relShift[d];
						}
					}
					else
					{
						center = centerIn;
					}
					for(int d=0;d<3;d++)
					{
						minBound[d] = Math.min(minBound[d], center[d]);
						maxBound[d] = Math.max(maxBound[d], center[d]);
					}
					if(bFirstCS)
					{
						for (int d=0; d<3; d++)
						{
							boundValue[d] = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
						}
						bFirstCS = false;
					}
					else
					{
						for (int d=0; d<3; d++)
						{
							final BoundedValueDoubleBVB centerRange = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
							allCenterEqual[d] &= Misc.compareBoundedValues( boundValue[d], centerRange );
							boundValue[d] = boundValue[d].join( centerRange );
						}
					}
				}
			}
		}
		if(clipSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = clipSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				if(sh.clipActive())
				{
					final Bounds3D bounds = clipSetups.clipCenterBounds.getBounds( sh );
					final double [] minBound = bounds.getMinBound();
					final double [] maxBound = bounds.getMaxBound();
					
					double [] center = new double [3];
					center = clipSetups.clipCenters.getCenters( sh );
					
					
					if(bFirstCS)
					{
						for (int d=0; d<3; d++)
						{
							boundValue[d] = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
						}
						bFirstCS = false;
					}
					else
					{
						for (int d=0; d<3; d++)
						{
							final BoundedValueDoubleBVB centerRange = new BoundedValueDoubleBVB( minBound[d], maxBound[d], center[d]);
							allCenterEqual[d] &= Misc.compareBoundedValues(boundValue[d], centerRange );
							boundValue[d] = boundValue[d].join( centerRange );
						}
					}
				}
			}
		}
		
		blockUpdates = false;

		//if anything is present and changed
		if(!bFirstCS)
		{
			final BoundedValueDoubleBVB [] finalCenter = boundValue;
			final boolean [] isConsistent = allCenterEqual;
			SwingUtilities.invokeLater( () -> {
				synchronized ( ClipCenterPanel.this )
				{
					blockUpdates = true;
					for (int d=0;d<3;d++)
					{
						clipCenterPanels[d].setConsistent( isConsistent[d] );
						clipCenterPanels[d].setValue( finalCenter[d] );
					}
					blockUpdates = false;
				}
			} );
		}
	}
	
	synchronized void updateClipCenter(int nAxis)
	{
		
		if(!clipSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		blockUpdates = true;
		
		double currValAbs = clipCenterPanels[nAxis].getValue().getCurrentValue();
		double minBoundAbs = clipCenterPanels[nAxis].getValue().getRangeMin();
		double maxBoundAbs = clipCenterPanels[nAxis].getValue().getRangeMax();
		minBoundAbs = Math.min( currValAbs, minBoundAbs );
		maxBoundAbs = Math.max( currValAbs, maxBoundAbs );
		
		if(clipSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = clipSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs : csList )
			{
				double [] relShift  = new double [3];
			
				if(clipSetups.bLocalCoordinates)
				{
				//relative coordinates
					relShift = Misc.getSourceMinNoFixedTransformAllTP( clipSetups.converterSetups.getSource( cs ).getSpimSource() );
				//final double [] relShift = clipSetups.getSourceMinWithScaleTranslation( cs );
				}

				//tr.apply( currValAbs, currVal );
				double currVal = currValAbs ;//+ relShift[nAxis];
				double minBound = minBoundAbs + relShift[nAxis];
				double maxBound = maxBoundAbs + relShift[nAxis];
				
				final Bounds3D bounds = clipSetups.clipCenterBounds.getBounds( cs );
				
				if(minBound != bounds.getMinBound()[nAxis] || maxBound != bounds.getMaxBound()[nAxis])
				{
					bounds.getMinBound()[nAxis] = minBound;
					bounds.getMaxBound()[nAxis] = maxBound;
					clipSetups.clipCenterBounds.setBounds( cs, bounds );
				}
				AffineTransform3D tr = new AffineTransform3D();
				(( TransformedSource< ? > )clipSetups.converterSetups.getSource( cs ).getSpimSource()).getFixedTransform( tr );
				final double [] newCenter = clipSetups.clipCenters.getCenters( cs );
				if(clipSetups.bLocalCoordinates)
				{
					tr.inverse().apply( newCenter, newCenter );
				}
				newCenter[nAxis] = currVal;
				if(clipSetups.bLocalCoordinates)
				{
					tr.apply( newCenter, newCenter );
				}
				clipSetups.clipCenters.setCenters( cs, newCenter );
				clipSetups.updateClipTransform( ( GammaConverterSetup ) cs, null );
			}
		}
//		if(clipSetups.selectedObjects.areShapesSelected())
//		{
//			final List< BasicShape > shList = clipSetups.selectedObjects.getSelectedShapes();
//			for ( final BasicShape sh : shList )
//			{
//				final Bounds3D bounds = clipSetups.clipCenterBounds.getBounds( sh );
//				
//				if(minBound != bounds.getMinBound()[nAxis] || maxBound != bounds.getMaxBound()[nAxis])
//				{
//					bounds.getMinBound()[nAxis] = minBound;
//					bounds.getMaxBound()[nAxis] = maxBound;
//					clipSetups.clipCenterBounds.setBounds( sh, bounds );
//				}
//				final double [] newCenter = clipSetups.clipCenters.getCenters( sh );
//				newCenter[nAxis] = currVal;
//				
//				clipSetups.clipCenters.setCenters( sh, newCenter );
//				clipSetups.updateClipTransform( sh );
//			}
//			clipSetups.bvb.updateSceneRender();
//		}
		blockUpdates = false;
		updateGUI();
	}
	
	/** sets bounds along the axis including all selected sources **/
	public void resetBounds(int nAxis)
	{
		
		if(!clipSetups.selectedObjects.isAnythingSelected() || blockUpdates)
			return;
		Bounds3D range3D = null;
		if(clipSetups.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = clipSetups.selectedObjects.getSelectedSources();
			for ( final ConverterSetup cs : csList )
			{
				if(range3D == null)
					range3D = clipSetups.clipCenterBounds.getDefaultBounds( cs );
				else
					range3D = range3D.join( clipSetups.clipCenterBounds.getDefaultBounds( cs ) );			
			}
		}
		if(clipSetups.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = clipSetups.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				if(range3D == null)
					range3D = clipSetups.clipCenterBounds.getDefaultBounds( sh );
				else
					range3D = range3D.join( clipSetups.clipCenterBounds.getDefaultBounds( sh ) );
			}
		}
		if(range3D != null)
		{
			double currVal = clipCenterPanels[nAxis].getValue().getCurrentValue();
			double bmin = range3D.getMinBound()[nAxis];
			double bmax = range3D.getMaxBound()[nAxis];
			currVal = Math.min( bmax, currVal );
			currVal = Math.max( bmin, currVal );
			clipCenterPanels[nAxis].setValue( new BoundedValueDouble(bmin, bmax, currVal) );
			updateClipCenter(nAxis);
		}
	}
	
	void setSliderColors(Color [] colors)
	{
		for(int i=0;i<3;i++)
		{
			clipCenterPanels[i].setSliderForeground( colors[i] );	
		}
	}
	
	private JMenuItem runnableItem( final String text, final Runnable action )
	{
		final JMenuItem item = new JMenuItem( text );
		item.addActionListener( e -> action.run() );
		return item;
	}
}
