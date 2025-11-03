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
	
	final JComboBox<String> cbRender;
	final NumberField nfMeshPointSize;
	final JButton butColor;
	final JCheckBox cbTexture;
	final JComboBox<String> cbSurface;	
	final JComboBox<String> cbGrid;
	
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
		pTexture.add( new JLabel("Texture"), gbc );
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
		
		String[] sGrid = {"Filled", "Wire", "Cartesian"};
		cbGrid = new JComboBox< >(sGrid);
		cbGrid.addActionListener( (e)->{
			updateGrid();				
			});
		pGrid = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pGrid.add( new JLabel("Grid: "), gbc );
		gbc.gridx++;
		pGrid.add( cbGrid, gbc );		
		
		allComp.add( butColor );
		allComp.add( cbTexture );
		allComp.add( cbSurface );
		allComp.add( cbGrid );
		allComp.add( cbRender );
		allComp.add( nfMeshPointSize );

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		GBCHelper.alighLoose(gbc);
		gbc.insets = new Insets(0,0,0,0);

		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add( pColor, gbc );

		gbc.gridy ++;	
		this.add( pTexture, gbc );
		gbc.gridy ++;	
		this.add( pRender, gbc );

		gbc.gridy ++;	
		this.add( pPointSize, gbc );
		
		gbc.gridy ++;	
		this.add( pSurface, gbc );
		
		gbc.gridy ++;	
		this.add( pGrid, gbc );

	}
	
	synchronized void updateGUI()
	{
		boolean bFirstMesh = true;
		boolean bRenderSame = true;
		boolean bColorSame = true;
		boolean bPointSizeSame = true;
		boolean bGridSame = true;
		boolean bSurfaceSame = true;
		
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
						bTextureSame &= (bTextureSame.booleanValue() && meshShape.isTextureUsed());
					}
				}
				if(bFirstMesh)
				{
					nRender = meshShape.getRenderType();
					currColor = meshShape.getColor();
					fPointSize = meshShape.getPointSize();
					nSurface = meshShape.getSurfaceRender();
					nGrid = meshShape.getSurfaceGrid();
					bFirstMesh = false;
				}
				else
				{
					bRenderSame &= (nRender ==  meshShape.getRenderType());
					bSurfaceSame &= (nSurface == meshShape.getSurfaceRender());
					bGridSame &= (nGrid == meshShape.getSurfaceGrid());
					bColorSame &= currColor.equals( meshShape.getColor() );
					bPointSizeSame &= Math.abs( fPointSize - meshShape.getPointSize())<0.0001;
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

		SwingUtilities.invokeLater( () -> {
			synchronized ( MeshesPropertiesPanel.this )
			{
				blockUpdates = true;
				
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
					//only shapes render
					if(nRenderFin == 0)
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

				blockUpdates = false;
			}
		} );
	}
	
	synchronized void updateRender()
	{
		if(!blockUpdates)
		{
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
	}
	
	synchronized void updateColors()
	{
		if(!blockUpdates)
		{
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
	}
	
	synchronized void updateUseOfTexture()
	{
		if(!blockUpdates)
		{
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
	}
	
	synchronized void updatePointSize(final double v)
	{
		if(!blockUpdates)
		{
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
	}
	
	synchronized void updateSurface()
	{
		if(!blockUpdates)
		{
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
	}
	
	synchronized void updateGrid()
	{
		if(!blockUpdates)
		{
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
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
		}
	}
	


}
