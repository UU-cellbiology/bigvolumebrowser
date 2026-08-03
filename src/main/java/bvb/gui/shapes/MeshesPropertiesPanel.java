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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import bdv.tools.brightness.ColorIcon;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorUserSettings;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.gui.NumberField;
import bvb.scene.VisMesh;
import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;

public class MeshesPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	final JPanelConsistent pColor;
	final JPanelConsistent pTexture;
	final JPanelConsistent pRender;
	final JPanelConsistent pPointSize;
	final JPanelConsistent pSurface;
	final JPanelConsistent pGrid;
	final JPanelConsistent pSilDecay;
	
	final JComboBox<String> cbRender;
	final NumberField nfMeshPointSize;
	final JButton butColor;
	final JCheckBox cbTexture;
	final JComboBox<String> cbSurface;	
	final JComboBox<String> cbGrid;
	final NumberField nfSilDecay;
	
	final ArrayList<Component> allComp = new ArrayList<>();
	
	ColorUserSettings selectColors = new ColorUserSettings();
	
	private boolean blockUpdates = false;
	
	public MeshesPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
				
		butColor = new JButton( new ColorIcon( Color.WHITE ) );	
		butColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(null, "Choose mesh color", 
					selectColors.getColor( 0 ));
			if (newColor != null)
			{
				selectColors.setColor(newColor, 0);
				updateColors();
			}
			
		});
		GBCHelper.alighLeft(gbc);
		pColor = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pColor.add( new JLabel("Color: "), gbc );
		gbc.gridx++;
		pColor.add( butColor, gbc );
		
		cbTexture = new JCheckBox();
		cbTexture.addItemListener( (e)-> updateUseOfTexture());
		pTexture = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pTexture.add( new JLabel("  Texture"), gbc );
		gbc.gridx++;
		pTexture.add( cbTexture, gbc );

		String[] sRender = {"Surface", "Points"};
		cbRender = new JComboBox< >(sRender);
		cbRender.addActionListener( (e)->{
			updateRender();				
			});
		
		pRender = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pRender.add( new JLabel("Render: "), gbc );
		gbc.gridx++;
		pRender.add( cbRender, gbc );		
		
		nfMeshPointSize = new NumberField(5);		
		nfMeshPointSize.setText( "0.0" );
		nfMeshPointSize.addListener( (v)->
		{
			updatePointSize(v);
		} );
		pPointSize = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pPointSize.add( new JLabel("Point size: "), gbc );
		gbc.gridx++;
		pPointSize.add( nfMeshPointSize, gbc );
		
		
		String[] sSurface = {"Plain", "Shaded", "Shiny", "Silhouette"};
		cbSurface = new JComboBox< >(sSurface);
		cbSurface.addActionListener( (e)->{
			updateSurface();				
			});
		
		pSurface = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pSurface.add( new JLabel("Surface: "), gbc );
		gbc.gridx++;
		pSurface.add( cbSurface, gbc );
		
		String[] sGrid = {"Solid", "Wireframe"};
		cbGrid = new JComboBox< >(sGrid);
		cbGrid.addActionListener( (e)->{
			updateGrid();				
			});
		pGrid = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pGrid.add( cbGrid, gbc );	

		
		nfSilDecay = new NumberField(5);		
		nfSilDecay.setText( "0.0" );
		nfSilDecay.addListener( (v)->
		{
			updateSilhouetteDecay(v);
		} );
		pSilDecay = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pSilDecay.add( new JLabel("Silhouette decay "), gbc );
		gbc.gridx++;
		pSilDecay.add( nfSilDecay, gbc );
		
		allComp.add( butColor );
		allComp.add( cbTexture );
		allComp.add( cbSurface );
		allComp.add( cbGrid );
		allComp.add( cbRender );
		allComp.add( nfMeshPointSize );
		allComp.add( nfSilDecay );

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		GBCHelper.alighLoose(gbc);
		gbc.insets = new Insets(0,0,0,0);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add( pColor, gbc );

		gbc.gridx ++;	
		this.add( pTexture, gbc );
		gbc.gridx = 0;
		gbc.gridy ++;	
		gbc.gridwidth = 2;
		this.add( pRender, gbc );
		
		gbc.gridy ++;	
		this.add( pPointSize, gbc );
		
		gbc.gridwidth = 1;
		gbc.gridy ++;	
		this.add( pSurface, gbc );
		
		gbc.gridx ++;	
		this.add( pGrid, gbc );
		
		gbc.gridx = 0;
		gbc.gridy ++;	
		gbc.gridwidth = 2;
		this.add( pSilDecay, gbc );

	}
	
	void updateGUI()
	{
	    if (!SwingUtilities.isEventDispatchThread())
	    {
	        SwingUtilities.invokeLater(this::updateGUI);
	        return;
	    }
	    
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		
		boolean bFirstMesh = true;
		boolean bRenderSame = true;
		boolean bColorSame = true;
		boolean bPointSizeSame = true;
		boolean bGridSame = true;
		boolean bSurfaceSame = true;
		
		boolean bSilRenderPresent = false;
		boolean bSilDecaySame = true;
		float fSilDecay = 1.0f;
		
		Boolean bTextureSame = null;
		boolean bTextureActive = false;
		boolean bHasTexture = false;
		
		float fPointSize = 0.0f;
		Color currColor = Color.WHITE;
		int nRender = 0;
		int nSurface = 0;
		int nGrid = 0;
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				final BasicMeshShape meshShape = (BasicMeshShape)sh;
				bHasTexture = bHasTexture || meshShape.hasTexture();
				if(bHasTexture)
				{
					if(bTextureSame == null)
					{
						bTextureSame = true;
						bTextureActive = meshShape.isTextureUsed();
					}
					else
					{
						bTextureSame &= (bTextureActive == meshShape.isTextureUsed());
					}
				}
				if(bFirstMesh)
				{
					nRender = meshShape.getRenderType();
					currColor = meshShape.getColor();
					fPointSize = meshShape.getPointSize();
					nSurface = meshShape.getSurfaceRender();
					nGrid = meshShape.getSurfaceGrid();
					if(nSurface == VisMesh.SURFACE_SILHOUETTE)
					{
						bSilRenderPresent = true;
						fSilDecay = meshShape.getSilhouetteDecay();
					}
					bFirstMesh = false;
				}
				else
				{
					bRenderSame &= (nRender ==  meshShape.getRenderType());
					bSurfaceSame &= (nSurface == meshShape.getSurfaceRender());
					bGridSame &= (nGrid == meshShape.getSurfaceGrid());
					bColorSame &= currColor.equals( meshShape.getColor() );
					bPointSizeSame &= Math.abs( fPointSize - meshShape.getPointSize()) < 0.0001;
					if(meshShape.getSurfaceRender() == VisMesh.SURFACE_SILHOUETTE && !bSilRenderPresent)
					{
						bSilRenderPresent = true;
						fSilDecay = meshShape.getSilhouetteDecay();
					}
					else
					{
						bSilDecaySame &= Math.abs(fSilDecay - meshShape.getSilhouetteDecay()) < 0.001;
					}
				}
			}
		}
		
		final Color cColorFin = currColor;
		final boolean bHasTextureFin = bHasTexture;
		final boolean bTextureSameFin = (bTextureSame == null)?false:bTextureSame.booleanValue();
		final boolean bTextureActiveFin = bTextureActive;
		final float fPointSizeFin = fPointSize;
		final int nRenderFin = nRender;
		final int nSurfaceFin = nSurface;
		final int nGridFin = nGrid;
		final boolean bRenderSameFin = bRenderSame;
		final boolean bColorSameFin = bColorSame;
		final boolean bPointSizeSameFin = bPointSizeSame;
		final boolean bSurfaceSameFin = bSurfaceSame;
		final boolean bGridSameFin = bGridSame;
		final boolean bSilDecaySameFin = bSilDecaySame; 
		final boolean bSilRenderPresentFin = bSilRenderPresent; 
		final float fSilDecayFin = fSilDecay;


		blockUpdates = true;
		
		try 
		{
			pRender.setConsistent( bRenderSameFin );
			pColor.setConsistent( bColorSameFin );
			if(bHasTextureFin)
			{
				pTexture.setConsistent( bTextureSameFin );
				cbTexture.setEnabled( true );
				cbTexture.setSelected( bTextureActiveFin );
			}
			else
			{
				pTexture.setConsistent( true );
				cbTexture.setSelected( false );
				cbTexture.setEnabled( false );
			}
			pPointSize.setConsistent( bPointSizeSameFin );
			pSurface.setConsistent( bSurfaceSameFin );
			pGrid.setConsistent( bGridSameFin );
			nfMeshPointSize.setEnabled( true );
			cbSurface.setEnabled( true );	
			cbGrid.setEnabled( true );

			if(bRenderSameFin)
			{
				cbRender.setSelectedIndex( nRenderFin );
				//only points render
				if(nRenderFin == VisMesh.MESH)
				{
					pPointSize.setConsistent( true );	
					nfMeshPointSize.setEnabled( false );	
				}
				else
				{
					pSurface.setConsistent( true );
					pGrid.setConsistent( true );
					cbSurface.setEnabled( false );	
					cbGrid.setEnabled( false );
				}
			}

			if(bColorSameFin)
			{
				selectColors.setColor( cColorFin, 0 );
				butColor.setIcon(  new ColorIcon( cColorFin ) );
			}

			if(bPointSizeSameFin)
			{
				nfMeshPointSize.setText( String.format("%.2f", fPointSizeFin));
			}
			if(bSurfaceSameFin)
			{
				cbSurface.setSelectedIndex( nSurfaceFin );
			}

			if(bGridSameFin)
			{
				cbGrid.setSelectedIndex( nGridFin );
			}
			
			nfSilDecay.setEnabled( bSilRenderPresentFin );
			if(!bSilRenderPresentFin )
			{
				pSilDecay.setConsistent( true );
			}
			else
			{
				pSilDecay.setConsistent( bSilDecaySameFin);
				if(bSilDecaySameFin)
					nfSilDecay.setText( String.format("%.2f", fSilDecayFin));
			}
		}
		finally
		{
			blockUpdates = false;
		}
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(() -> setEnabled(bEnabled));
			return;
		}

		if(blockUpdates)
			return;

		blockUpdates = true;
		try
		{
			for(final Component nC:allComp)
			{
				nC.setEnabled( bEnabled );
			}
		}
		finally
		{
			blockUpdates = false;
		}
	}
	
	void updateRender()
	{

		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		
		final int nRenderType = cbRender.getSelectedIndex();
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				((BasicMeshShape)sh).setRenderType( nRenderType );
			}
		}
		bvb.repaintBVV();
		updateGUI();
	}
	
	void updateColors()
	{
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		final Color cColor = selectColors.getColor( 0 );
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				((BasicMeshShape)sh).setColor( cColor );
			}
		}
		bvb.repaintBVV();
		updateGUI();
		
	}
	
	void updateUseOfTexture()
	{
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		final boolean bUseTexture = cbTexture.isSelected();
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				((BasicMeshShape)sh).useTexture( bUseTexture );
			}
		}
		bvb.repaintBVV();
		updateGUI();
		
	}
	
	void updateSurface()
	{
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		final int nSurfaceType = cbSurface.getSelectedIndex();
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				if(((BasicMeshShape)sh).getRenderType() == VisMesh.MESH)
				{
					((BasicMeshShape)sh).setSurfaceRender( nSurfaceType );
				}
			}
		}
		bvb.repaintBVV();
		updateGUI();
		
	}
	
	void updateGrid()
	{
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		final int nGridType = cbGrid.getSelectedIndex();
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				if(((BasicMeshShape)sh).getRenderType() == VisMesh.MESH)
				{
					((BasicMeshShape)sh).setSurfaceGrid( nGridType );
				}
			}
		}
		bvb.repaintBVV();
		updateGUI();
	}
	
	void updatePointSize(final double v)
	{
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		final float fv = (float)v;
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				if(((BasicMeshShape)sh).getRenderType() == VisMesh.POINTS)
				{
					((BasicMeshShape)sh).setPointSize( fv );
				}
			}
		}
		bvb.repaintBVV();
		updateGUI();
	}
	
	void updateSilhouetteDecay(final double v)
	{
		if(!bvb.selectedObjects.areShapesSelected() || blockUpdates)
			return;
		final float fv = (float)Math.abs( v );
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof BasicMeshShape)
			{
				if(((BasicMeshShape)sh).getSurfaceRender() == VisMesh.SURFACE_SILHOUETTE)
				{
					((BasicMeshShape)sh).setSilhouetteDecay( fv );
				}
			}
		}
		bvb.repaintBVV();
		updateGUI();
	}

}
