package bvb.gui.shapes;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import bdv.tools.brightness.ColorIcon;
import bvb.core.BigVolumeBrowser;
import bvb.gui.ColorUserSettings;
import bvb.gui.GBCHelper;
import bvb.gui.JPanelConsistent;
import bvb.gui.NumberField;
import bvb.shapes.BasicShape;
import bvb.shapes.Spots;

public class SpotsPropertiesPanel extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final JPanelConsistent pColor;
	final JPanelConsistent pRender;
	final JPanelConsistent pPointSize;
	final JPanelConsistent pShape;
	
	final JPanelConsistent pGaussNorm;
	
	final NumberField nfSpSize;
	final JButton butColor;
	final JComboBox<String> cbShape;
	final JComboBox<String> cbRender;

	
	final NumberField nfGaussNorm;
	
	final JSplitPane splitPane;
	
	final JPanel topPanel;
	final JPanel bottomPanel;
	
	final ArrayList<Component> allComp = new ArrayList<>();
	
	ColorUserSettings selectColors = new ColorUserSettings();
	
	private boolean blockUpdates = false;
	
	public SpotsPropertiesPanel(final BigVolumeBrowser bvb_)
	{
		super();	
		
		bvb = bvb_;
		
		setLayout(new GridBagLayout());
	
		
		GridBagConstraints gbc = new GridBagConstraints();		

		butColor = new JButton( new ColorIcon( Color.WHITE ) );
		butColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(null, "Choose spots color", 
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
		
		nfSpSize = new NumberField(5);
		nfSpSize.setLimits( 0.0, Double.MAX_VALUE );
		nfSpSize.addListener( (v)->
		{
			double in = Math.max( Math.abs(v), 0.0001 );
			updatePointSize(Math.abs( in ));
			//String.format("%.2f", in);
		} );
		pPointSize = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pPointSize.add( new JLabel("Point size: "), gbc );
		gbc.gridx++;
		pPointSize.add( nfSpSize, gbc );
		
		String[] sShapes = {"Round", "Square"};
		cbShape = new JComboBox< >(sShapes);
		cbShape.addActionListener( (e)->{
			updateShape();				
			});
		pShape = new JPanelConsistent(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		pShape.add( new JLabel("Shape: "), gbc );
		gbc.gridx++;
		pShape.add( cbShape, gbc );

		String[] sRender = {"Filled", "Outline", "Gauss", "Gauss norm"};
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
		
		
		nfGaussNorm = new NumberField(5);
		nfGaussNorm.setLimits( 0.0, Double.MAX_VALUE );
		nfGaussNorm.addListener( (v)->
		{
			updateGaussNorm(Math.abs( v ));
			//String.format("%.2f", in);
		} );
		pGaussNorm = new JPanelConsistent(new GridBagLayout());
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		pGaussNorm.add( new JLabel("Gauss norm: "), gbc );
		gbc.gridx++;
		pGaussNorm.add( nfGaussNorm, gbc );
		
		allComp.add( butColor );
		allComp.add( nfSpSize );
		allComp.add( cbShape );
		allComp.add( cbRender );
		allComp.add( nfGaussNorm );
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		GBCHelper.alighLoose(gbc);
		gbc.insets = new Insets(0,0,0,0);
		
		//split panels		
		topPanel = new JPanel(new GridBagLayout());
		bottomPanel = new JPanel(new GridBagLayout());
		//Top Panel
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		topPanel.add(pColor, gbc );
		
		gbc.gridy ++;	
		topPanel.add(pPointSize, gbc );
		
		gbc.gridy ++;		
		topPanel.add(pShape, gbc );
		
		gbc.gridy ++;
		topPanel.add(pRender, gbc );
		
		//Bottom Panel
		gbc.gridx = 0;
		gbc.gridy = 0;
		bottomPanel.add( pGaussNorm, gbc );
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
		splitPane.setResizeWeight(0.5);
	    splitPane.setOneTouchExpandable(true);
	    splitPane.setContinuousLayout(true);
	    ///collapse the bottom panel
	    splitPane.addAncestorListener(new AncestorListener() 
	    {
	        @Override
			public void ancestorAdded(AncestorEvent ev) 
	        {
	           // Pressing the oneTouch button shows/hides a pane while remembering the last custom dragged size
	           BasicSplitPaneUI uicurr = (BasicSplitPaneUI) splitPane.getUI();
	           JButton neededOneTouchBtn = (JButton) uicurr.getDivider().getComponent(1);
	           neededOneTouchBtn.doClick();
	         
	         // only run once
	          splitPane.removeAncestorListener(this);
	        }

	        @Override
			public void ancestorRemoved(AncestorEvent ev) 
	        {
	        }

	        @Override
			public void ancestorMoved(AncestorEvent ev) 
	        {
	        }
	      });
	    this.add( splitPane );
	}
	
	synchronized void updateGUI(boolean updateText)
	{
		boolean bFirstMesh = true;
		
		boolean bColorSame = true;
		boolean bPointSizeSame = true;
		boolean bShapeSame = true;
		boolean bRenderSame = true;
		boolean bGaussNormSame = true;
		float fPointSizeIn;
		float fPointSize = -1.0f;
		Color currColor = Color.WHITE;
		int nRender = 0;
		int nShape = 0;
		float fGaussNorm = -1.0f;
		
		final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
		for ( final BasicShape sh: shapeList)
		{
			if(sh instanceof Spots)
			{
				final Spots spotsShape = (Spots)sh;
				if(bFirstMesh)
				{
					currColor = spotsShape.getColor();
					fPointSizeIn = spotsShape.getPointSize();
					if(fPointSizeIn>0.0)
					{
						fPointSize = spotsShape.getPointSize();
					}
					nShape = spotsShape.getPointShape();
					nRender = spotsShape.getRenderType();
					fGaussNorm = spotsShape.getGaussSDNorm();
					bFirstMesh = false;
				}
				else
				{
					bRenderSame &= (nRender ==  spotsShape.getRenderType());
					bShapeSame &= (nShape == spotsShape.getPointShape());
					bColorSame &= currColor.equals( spotsShape.getColor() );
					bPointSizeSame &= Math.abs( fPointSize - spotsShape.getPointSize())<0.0001;
					bGaussNormSame &= Math.abs( fGaussNorm - spotsShape.getGaussSDNorm())<0.00000001;
				}
			}
		}
		
		final Color cColorFin = currColor;
		final float fPointSizeFin = fPointSize;		
		final int nShapeFin = nShape;
		final int nRenderFin = nRender;		
		final float fGaussNormFin = fGaussNorm;		
		
		final boolean bColorSameFin = bColorSame;
		final boolean bPointSizeSameFin = bPointSizeSame;
		final boolean bShapeSameFin = bShapeSame;
		final boolean bRenderSameFin = bRenderSame;
		final boolean bGaussNormSameFin = bGaussNormSame;


		SwingUtilities.invokeLater( () -> {
			synchronized ( SpotsPropertiesPanel.this )
			{
				blockUpdates = true;
				
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setDecimalSeparator('.');
				DecimalFormat df3 = new DecimalFormat ("#.######", symbols);
				for (int d=0;d<3;d++)
				{					
					pColor.setConsistent( bColorSameFin );
					pPointSize.setConsistent( bPointSizeSameFin );
					pShape.setConsistent( bShapeSameFin );
					pRender.setConsistent( bRenderSameFin );
					pGaussNorm.setConsistent( bGaussNormSameFin );
					
					if(bColorSameFin)
					{
						selectColors.setColor( cColorFin, 0 );
						butColor.setIcon(  new ColorIcon( cColorFin ) );
					}
					
					if(bPointSizeSameFin)
					{
						if(bPointSizeSameFin && fPointSizeFin < 0.0)
						{
							nfSpSize.setText( "various");
							nfSpSize.setEnabled( false );
						}
						else
						{
							nfSpSize.setEnabled( true );
							nfSpSize.setText( df3.format( fPointSizeFin));
						}

					}
					if(updateText)
					{
						nfGaussNorm.setText( df3.format( fGaussNormFin) );
					}
					if(bRenderSameFin)
					{
						cbRender.setSelectedIndex( nRenderFin );
					}

					if(bShapeSameFin)
					{
						cbShape.setSelectedIndex( nShapeFin );
					}
						
				}
				blockUpdates = false;
			}
		} );
		
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		for(final Component nC:allComp)
		{
			nC.setEnabled( bEnabled );
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
				if(sh instanceof Spots)
				{
					((Spots)sh).setColor( cColor );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
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
				if(sh instanceof Spots)
				{
					if(((Spots)sh).getPointSize()>0.0f)
					{
						((Spots)sh).setPointSize( fv );
					}
				}
			}
			bvb.updateSceneRender();
			updateGUI(false);
		}
	}
	
	synchronized void updateShape()
	{
		if(!blockUpdates)
		{
			final int nShapeType = cbShape.getSelectedIndex();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof Spots)
				{
					((Spots)sh).setPointShape( nShapeType );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
	synchronized void updateRender()
	{
		if(!blockUpdates)
		{
			final int nRenderType = cbRender.getSelectedIndex();
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof Spots)
				{
					((Spots)sh).setRenderType( nRenderType );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
	
	synchronized void updateGaussNorm (double fGaussNorm)
	{
		if(!blockUpdates)
		{
			final List< BasicShape> shapeList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh: shapeList)
			{
				if(sh instanceof Spots)
				{
					((Spots)sh).setGaussNorm( (float)fGaussNorm );
				}
			}
			bvb.repaintBVV();
			updateGUI(false);
		}
	}
}
