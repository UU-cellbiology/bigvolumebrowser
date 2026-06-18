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
package bvb.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerModel;

import net.imglib2.RealInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Affine3DHelpers;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.animate.RotationAnimator;
import bvb.gui.AnisotropicTransformAnimator3D;
import bvb.gui.CanvasSelection;
import bvb.gui.CenterZoomBVV;
import bvb.gui.ColorTextOverlayAnimator;
import bvb.gui.TransformHandlerBVB;
import bvb.gui.overlays.TransformModeOverlayRenderer;
import bvb.gui.ColorTextOverlayAnimator.TextPosition;
import bvb.shapes.BasicShape;
import ij.Prefs;

public class BVBActions
{
	
	final BigVolumeBrowser bvb;
	
	final Actions actions;
	
	final Behaviours behaviours;
	
	private final static double cQuat = Math.cos( Math.PI / 4 );
	
	public static final String ALIGN_XY_PLANE = "align XY plane";
	public static final String ALIGN_ZY_PLANE = "align ZY plane";
	public static final String ALIGN_XZ_PLANE = "align XZ plane";
	
	public static final String[] ALIGN_XY_PLANE_KEYS = new String[] { "shift Z" };
	public static final String[] ALIGN_ZY_PLANE_KEYS = new String[] { "shift X" };
	public static final String[] ALIGN_XZ_PLANE_KEYS = new String[] { "shift Y", "shift A" };

	public static final String[] ROTATE_X_AXIS_VIEW = new String[] { "ctrl X" };
	public static final String[] ROTATE_Z_AXIS_VIEW  = new String[] { "ctrl Z" };
	public static final String[] ROTATE_Y_AXIS_VIEW   = new String[] { "ctrl Y", "ctrl A" };

	public static final String[] ROTATE_X_AXIS_WORLD = new String[] { "alt X" };
	public static final String[] ROTATE_Z_AXIS_WORLD  = new String[] { "alt Z" };
	public static final String[] ROTATE_Y_AXIS_WORLD   = new String[] { "alt Y", "alt A" };

	final long lRotationDuration = 100;
	
	final TransformModeOverlayRenderer transfromModeOverlay = new TransformModeOverlayRenderer();
	
	public BVBActions(final BigVolumeBrowser bvb_) 
	{
		bvb = bvb_;
		actions = new Actions( new InputTriggerConfig() );
		behaviours = new Behaviours( new InputTriggerConfig() );
		installBehaviors();
		installActions();
		transfromModeOverlay.bindBVB( bvb_ );
		bvb.bvvViewer.getDisplay().overlays().add( transfromModeOverlay );
	}
	
	/** install separate drag/rotation working with manual transform **/
	void installBehaviors()
	{		
		final TransformHandlerBVB transformHandlerBVB = new TransformHandlerBVB(bvb);

		transformHandlerBVB.install( behaviours );
		
		behaviours.install( bvb.bvvHandle.getTriggerbindings(), "BigVolumeBrowser Behaviours" );
	}
	
	void installActions()
	{
		actions.runnableAction(() -> dummy(), "cycle current", "C" );
		actions.runnableAction(() -> actionCenterView(), "center view (zoom out)", "C" );
		actions.runnableAction(() -> dummy(), "toggle manual transformation", "T" );

		actions.runnableAction(() -> actionToggleManualTransform(), "toggle manual transformation (BVB)", "T" );
		actions.runnableAction(() -> turnOffManualTransform(true), "off manual transform mode(BVB)", "ESCAPE" );		
		actions.runnableAction(() -> actionToggleVisibility(), "toggle visibility", "V" );
		actions.runnableAction(() -> actionSelectClosestObject(0), "select object", "E" );
		actions.runnableAction(() -> actionSelectClosestObject(1), "add object", "shift E" );
		actions.runnableAction(() -> actionSelectClosestObject(2), "toggle object selection", "ctrl E" );
		actions.runnableAction(() -> actionSelectAll(), "select all objects", "ctrl A" );
		actions.runnableAction(() -> alignToPlane( AlignPlaneBVB.XY ), ALIGN_XY_PLANE, ALIGN_XY_PLANE_KEYS );
		actions.runnableAction(() -> alignToPlane( AlignPlaneBVB.ZY ), ALIGN_ZY_PLANE, ALIGN_ZY_PLANE_KEYS );
		actions.runnableAction(() -> alignToPlane( AlignPlaneBVB.XZ ), ALIGN_XZ_PLANE, ALIGN_XZ_PLANE_KEYS );
		actions.runnableAction(() -> rotate(0, true), "rotate 90 x axis", ROTATE_X_AXIS_VIEW);
		actions.runnableAction(() -> rotate(1, true), "rotate 90 y axis", ROTATE_Y_AXIS_VIEW);
		actions.runnableAction(() -> rotate(2, true), "rotate 90 z axis", ROTATE_Z_AXIS_VIEW);
		actions.runnableAction(() -> rotate(0, false), "rotate 90 x axis wrld", ROTATE_X_AXIS_WORLD);
		actions.runnableAction(() -> rotate(1, false), "rotate 90 y axis wrld", ROTATE_Y_AXIS_WORLD);
		actions.runnableAction(() -> rotate(2, false), "rotate 90 z axis wrld", ROTATE_Z_AXIS_WORLD);
		actions.runnableAction(() -> bvb.bvbCards.animationPanel.makeSnapshot(), "make snapshot", "ctrl S" );
		actions.runnableAction(() -> showHelpWindow(), "help", "F1" );
		actions.runnableAction(() -> runSettingsCommand(), "settings", "F10" );
		
		actions.install( bvb.bvvHandle.getKeybindings(), "BigVolumeBrowser actions" );
		
	}
	
	public ActionMap getActionMap()
	{		
		return actions.getActionMap();
	}
	
	public InputMap getInputMap()
	{
		return actions.getInputMap();
	}
	
	void dummy() 
	{
		
	}

	void runSettingsCommand()
	{
		if( bvb.getInputLock() )
			return;
		JPanel pViewSettings = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbcTitle = new GridBagConstraints();
		GridBagConstraints gbcL = new GridBagConstraints();
		GridBagConstraints gbcR = new GridBagConstraints();
		
		SpinnerModel smW = new SpinnerNumberModel(BVVSettings.renderWidth, 10, 10000, 1);		
		JSpinner renderWidth = new JSpinner(smW);
		renderWidth.setEditor(new JSpinner.NumberEditor(renderWidth, "#"));
		renderWidth.setToolTipText( "Viewport render width"  );
		
		SpinnerModel smH = new SpinnerNumberModel(BVVSettings.renderHeight, 10, 10000, 1);		
		JSpinner renderHeight = new JSpinner(smH);
		renderHeight.setEditor(new JSpinner.NumberEditor(renderHeight, "#"));
		renderHeight.setToolTipText( "Viewport render height"  );
			
		
		String[] sDitherWidths = { "none (always render full resolution)", "2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8" };
		JComboBox<String> ditherWidthsList = new JComboBox<>(sDitherWidths);
		ditherWidthsList.setToolTipText( "Dither window size" );
		ditherWidthsList.setSelectedIndex(BVVSettings.ditherWidth-1);

		JSlider slNumDitherSamples = new JSlider(SwingConstants.HORIZONTAL,
                1, 8, BVVSettings.numDitherSamples);
		slNumDitherSamples.setToolTipText( "Pixels are interpolated from this many nearest neighbors when dithering. This is not very expensive, it's fine to turn it up to 8." );
		slNumDitherSamples.setMinorTickSpacing(1);
		Hashtable< Integer, JLabel > labelTable = new Hashtable<>();
		labelTable.put( new Integer( 1 ), new JLabel("1") );
		for(int i = 1; i <= 4; i++)
		{
			labelTable.put( new Integer( i * 2 ), new JLabel(Integer.toString( i * 2 )) );
			
		}
		slNumDitherSamples.setLabelTable( labelTable );
		slNumDitherSamples.setPaintTicks(true);
		slNumDitherSamples.setPaintLabels(true);
		
		SpinnerModel cacheBlockSizeM = new SpinnerNumberModel(BVVSettings.cacheBlockSize, 10, 1024, 1);		
		JSpinner cacheBlockSize = new JSpinner(cacheBlockSizeM);
		cacheBlockSize.setEditor(new JSpinner.NumberEditor(cacheBlockSize, "#"));
		
		SpinnerModel maxCacheSizeInMBM = new SpinnerNumberModel(BVVSettings.maxCacheSizeInMB, 10, Integer.MAX_VALUE, 1);		
		JSpinner maxCacheSizeInMB = new JSpinner(maxCacheSizeInMBM);
		maxCacheSizeInMB.setEditor(new JSpinner.NumberEditor(maxCacheSizeInMB, "#"));
		maxCacheSizeInMB.setToolTipText( "The size of the GPU cache texture. Increase it to the max available."  );
		
		SpinnerModel dCamM = new SpinnerNumberModel(BVVSettings.dCam, BVVSettings.dClipNear+5, Integer.MAX_VALUE, 1);		
		JSpinner dCam = new JSpinner(dCamM);
		dCam.setEditor(new JSpinner.NumberEditor(dCam, "#"));
		dCam.setToolTipText( "Distance from camera to z=0 plane (in space units)."  );
		
		
		SpinnerModel dClipFarM = new SpinnerNumberModel(BVVSettings.dClipFar, 10, Integer.MAX_VALUE, 1);		
		JSpinner dClipFar = new JSpinner(dClipFarM);
		dClipFar.setEditor(new JSpinner.NumberEditor(dClipFar, "#"));
		dClipFar.setToolTipText( "Visible depth from z=0 further away from the camera (in space units)."  );
		
		SpinnerModel dClipNearM = new SpinnerNumberModel(BVVSettings.dClipNear, 10, Integer.MAX_VALUE, 1);		
		JSpinner dClipNear = new JSpinner(dClipNearM);
		dClipNear.setEditor(new JSpinner.NumberEditor(dClipNear, "#"));
		dClipNear.setToolTipText( "Visible depth from z=0 closer to the camera (in space units). MUST BE SMALLER THAN CAMERA DISTANCE!"  );
		dClipNear.addChangeListener( (e)->
		{
			int currNear =  ((Double)dClipNear.getValue()).intValue();
			((SpinnerNumberModel)dCam.getModel()).setMinimum( new Double(currNear+5) );
			if(currNear > ((Double)dCam.getValue()).intValue())
			{
				dCam.setValue( currNear + 5 );
			}
		});
		
		gbcL.insets = new Insets(5,5,5,5);
		gbcR.insets = new Insets(5,5,5,5);
		gbcTitle.insets = new Insets(5,5,5,5);
		
		gbcTitle.anchor = GridBagConstraints.CENTER;
		gbcTitle.gridwidth = 2;
		gbcL.anchor = GridBagConstraints.EAST;
		gbcR.fill = GridBagConstraints.HORIZONTAL;
		gbcR.weightx = 1.0;
		
		gbcL.gridx = 0;
		gbcR.gridx = 1;
		gbcL.gridy = 0;
		gbcR.gridy = 0;
		JLabel hyperlink = new JLabel("<html><a href=\"https://github.com/UU-cellbiology/bigvolumebrowser/wiki/3D-rendering-parameters#gpu-usage-settings\">"
				+ "<b>GPU memory size (in MB)</b></a></html>");
		
		hyperlink.setForeground(Color.BLUE.darker());
		hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		hyperlink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://github.com/UU-cellbiology/bigvolumebrowser/wiki/3D-rendering-parameters#gpu-usage-settings"));
				}
				catch ( IOException | URISyntaxException exc )
				{
					exc.printStackTrace();
				}

			}
		});
		pViewSettings.add(hyperlink , gbcL );
		pViewSettings.add( maxCacheSizeInMB,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("GPU cache tile size"), gbcL );
		pViewSettings.add( cacheBlockSize,gbcR );
		
		gbcTitle.gridy = gbcL.gridy + 1;
		pViewSettings.add( new JLabel("Viewport render resolution"), gbcTitle );	
		
		gbcL.gridy += 2;
		gbcR.gridy += 2;
		pViewSettings.add( new JLabel("Render width"), gbcL );	
		pViewSettings.add( renderWidth,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Render height"), gbcL );
		pViewSettings.add( renderHeight,gbcR );
		
		gbcTitle.gridy = gbcL.gridy + 1;
		pViewSettings.add( new JLabel("Dither"), gbcTitle );	
		
		gbcL.gridy += 2;
		gbcR.gridy += 2;
		pViewSettings.add( new JLabel("Dither window size"), gbcL );
		pViewSettings.add( ditherWidthsList, gbcR );

		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Number of dither samples"), gbcL );
		pViewSettings.add( slNumDitherSamples,gbcR );
	
		gbcTitle.gridy = gbcL.gridy + 1;
		pViewSettings.add( new JLabel("Perspective camera"), gbcTitle );	
		
		gbcL.gridy += 2;
		gbcR.gridy += 2;
		pViewSettings.add( new JLabel("Camera distance"), gbcL );
		pViewSettings.add( dCam,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Clip distance far"), gbcL );
		pViewSettings.add( dClipFar,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Clip distance near"), gbcL );
		pViewSettings.add( dClipNear,gbcR );
		
		
		int reply = JOptionPane.showConfirmDialog(null, pViewSettings, "3D rendering settings", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			boolean bRestartBVV = false;
			BVVSettings.dCam = ((Double)dCam.getValue()).doubleValue();
			Prefs.set("BVB.dCam", BVVSettings.dCam);
			BVVSettings.dClipFar = ((Double)dClipFar.getValue()).doubleValue();
			Prefs.set("BVB.dClipFar", BVVSettings.dClipFar);
			BVVSettings.dClipNear = ((Double)dClipNear.getValue()).doubleValue();
			Prefs.set("BVB.dClipNear", BVVSettings.dClipNear);
			
			BVVSettings.setFNratio();
			
			int nTempInt =  ((Integer)renderWidth.getValue()).intValue();
			if(BVVSettings.renderWidth != nTempInt)
			{
				BVVSettings.renderWidth = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.renderWidth", BVVSettings.renderWidth);
			}
			
			nTempInt =  ((Integer)renderHeight.getValue()).intValue();
			if(BVVSettings.renderHeight != nTempInt)
			{
				BVVSettings.renderHeight = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.renderHeight", BVVSettings.renderHeight);
			}
			
			nTempInt = ditherWidthsList.getSelectedIndex() + 1;
			if(BVVSettings.ditherWidth != nTempInt)
			{
				BVVSettings.ditherWidth = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.ditherWidth", BVVSettings.ditherWidth);
			}
			
			nTempInt = slNumDitherSamples.getValue();
			if(BVVSettings.numDitherSamples != nTempInt)
			{
				BVVSettings.numDitherSamples = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.numDitherSamples", BVVSettings.numDitherSamples);
			}
			
			nTempInt = ((Integer)cacheBlockSizeM.getValue()).intValue();
			if(BVVSettings.cacheBlockSize != nTempInt)
			{
				BVVSettings.cacheBlockSize = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.cacheBlockSize", BVVSettings.cacheBlockSize);
			}
			
			nTempInt = ((Integer)maxCacheSizeInMB.getValue()).intValue();
			if(BVVSettings.maxCacheSizeInMB != nTempInt)
			{
				BVVSettings.maxCacheSizeInMB = nTempInt;
				bRestartBVV = true;
				Prefs.set("BVB.maxCacheSizeInMB", BVVSettings.maxCacheSizeInMB);
			}
			
			if(!bRestartBVV)
			{
				bvb.bvvViewer.setCamParams( BVVSettings.dCam, BVVSettings.dClipNear, BVVSettings.dClipFar );
				bvb.repaintBVV();
			}
			else
			{
				bvb.restartBVV();
			}
		}
	}

//	void runSettingsCommandSciJava()
//	{	
//		
//		//final Context ctx = new Context(); // you need to have one of these; make one with new if you don't already		
//		//CommandService cs = ctx.service(CommandService.class);
//		HelloWorld cn = new HelloWorld();
//		Services.commandService.context().inject( cn );
//		Future<CommandModule> f = Services.commandService.run(HelloWorld.class, true);
//
////		Future<CommandModule> f = Services.commandService.run(ConfigureBVVRenderWindow.class, true);
//		try
//		{
//			Module m = f.get();
//		}
//		catch ( InterruptedException exc )
//		{
//			// TO DO Auto-generated catch block
//			exc.printStackTrace();
//		}
//		catch ( ExecutionException exc )
//		{
//			// TO DO Auto-generated catch block
//			exc.printStackTrace();
//		} 
//		// wait for command to complete
//	//	Map<String, Object> outputs = m.getOutputs();
////		System.out.println("Processed data = " + outputs.get("processedData");
//
//	}
	
	public void actionCenterView()
	{
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		//solution for now, to not interfere with typing
		if(!bvb.getInputLock() && !(c instanceof JTextField))
		{
			//final RealInterval focusInt = CenterZoomBVV.getAllSelectedVisibleSourcesBoundindBox(bvb);
			final RealInterval focusInt = CenterZoomBVV.getAllSelectedVisibleObjectsBoundindBox(bvb);
			if(focusInt != null)
			{
				CenterZoomBVV.focusAnimateOnInterval(bvb, focusInt, BVBSettings.dFocusScreenFraction);
			}
		}
	}
	
	public void actionToggleVisibility()
	{
		if(!bvb.selectedObjects.isAnythingSelected() || bvb.getInputLock())
			return;
		
		if(bvb.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = bvb.selectedObjects.getSelectedConverterSetups();
			for ( final ConverterSetup cs : csList )
			{				
				SourceAndConverter< ? > sac = bvb.bvvHandle.getConverterSetups().getSource( cs );
				bvb.bvvViewer.state().setSourceActive( sac, !bvb.bvvViewer.state().isSourceVisible( sac ) );	
			}
		}
		if(bvb.selectedObjects.areShapesSelected())
		{
			final List< BasicShape > shList = bvb.selectedObjects.getSelectedShapes();
			for ( final BasicShape sh : shList )
			{
				sh.setVisible( !sh.isVisible() );
			}
			bvb.bvbCards.panelShapes.updateUI();
			bvb.repaintBVV();
		}
	}
	
	void showHelpWindow()
	{
		JPanel pHelp = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel txtBefore = new JLabel("Full documentation is available at");
		JLabel hyperlink = new JLabel("BigVolumeBrowser wiki page");
		hyperlink.setForeground(Color.BLUE.darker());
		hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		hyperlink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://github.com/UU-cellbiology/bigvolumebrowser/wiki"));
				}
				catch ( IOException | URISyntaxException exc )
				{
					exc.printStackTrace();
				}

			}
		});
		
		String shortCutInfo ="<html><center><b>Shortcuts:</b></center><br>"
				+"&nbsp;<b>P</b> - show BVB cards panel<br><br>"
				+"&nbsp;<b>C</b> - center the view on selected objects<br><br>"
				+"&nbsp;<b>E</b> - select object on canvas<br><br>"
				+"&nbsp;<b>Shift + E</b> - add object on canvas to selection<br><br>"
				+"&nbsp;<b>Ctrl + E</b> - toggle object selection on canvas<br><br>"				
				+"&nbsp;<b>V</b> - toggle visibility of selected objects<br><br>"
				+"&nbsp;<b>O</b> - toggle sources render method<br><br>"
				+"&nbsp;<b>S</b> - separate brightness/color dialog<br><br>"
				+"&nbsp;<b>Ctrl + S</b> - make a snapshot<br><br>"
				+"&nbsp;<b>Shift + X/Y/Z</b> - rotate to major plane<br><br>"					
				+"&nbsp;<b>M</b>/<b>N</b> - timepoint +/- <br><br>"
				+"&nbsp;<b>F10</b> - 3D rendering settings<br><br></html>";
		JLabel jlInfo = new JLabel(shortCutInfo);
		jlInfo.setVerticalAlignment(SwingConstants.TOP);
		jlInfo.setHorizontalAlignment(SwingConstants.CENTER);
		
		final int nLeftRightBorder = 40;
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(30,nLeftRightBorder,5,nLeftRightBorder);
		
		pHelp.add(txtBefore,gbc);
		gbc.gridy++;
		gbc.insets = new Insets(5,nLeftRightBorder,25,nLeftRightBorder);
		pHelp.add(hyperlink,gbc);
		gbc.gridy++;
		gbc.insets = new Insets(5,nLeftRightBorder,30,nLeftRightBorder);
		pHelp.add(jlInfo,gbc);
		
		JFrame frame = new JFrame("BigVolumeBrowser Help");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(pHelp);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	/** selects (nMode 0) or adds to selection (nMode 1) or toggles selection (nMode 2)
	 * of the closest object **/
	void actionSelectClosestObject(final int nMode)
	{
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		//solution for now, to not interfere with typing
		if(!bvb.getInputLock() && !(c instanceof JTextField))
		{
			final ArrayList<SourceAndConverter<?>> selectedSAC = new ArrayList<>();
			
			final ArrayList<BasicShape> selectedShapes = new ArrayList<>();

			boolean bShouldNotBeSelected = false;

			if( nMode >= 1 )
			{
				for(final SourceAndConverter<?> sac:bvb.selectedObjects.getSelectedSources())
				{
					selectedSAC.add( sac );
				}
				for(final BasicShape sh:bvb.selectedObjects.getSelectedShapes())
				{
					selectedShapes.add( sh );
				}
			}
			if( nMode == 1)
			{
				bShouldNotBeSelected = true;
			}
			
			final Object closestObj = CanvasSelection.findClosestObjectOnCanvasOnClick(bvb, bShouldNotBeSelected);

			if( nMode <= 1 )
			{
				if(closestObj instanceof SourceAndConverter)
				{
					selectedSAC.add( ( SourceAndConverter< ? > ) closestObj );
				}
				if(closestObj instanceof BasicShape)
				{
					selectedShapes.add( ( BasicShape ) closestObj );
				}
			}
			else
			{
				if(closestObj instanceof SourceAndConverter)
				{
					if(selectedSAC.contains( closestObj ))
					{
						selectedSAC.remove( closestObj );
					}
					else
					{
						selectedSAC.add( ( SourceAndConverter< ? > ) closestObj );
					}
				}
				if(closestObj instanceof BasicShape)
				{
					if(selectedShapes.contains( closestObj ))
					{
						selectedShapes.remove( closestObj );
					}
					else
					{
						selectedShapes.add( ( BasicShape ) closestObj );
					}
				}
			}
			bvb.bvvViewer.sourceSelection.table.setSelectedSources( selectedSAC );
			bvb.bvbCards.panelShapes.tableShapes.setSelectedShapes( selectedShapes );
			bvb.updateSceneRender();
		}
	}
	
	void actionSelectAll()
	{
		ArrayList< Object > focusSet = CenterZoomBVV.getAllVisibleObjects( bvb );
		if(focusSet.size() == 0)
			return;
		final ArrayList<SourceAndConverter<?>> selectedSAC = new ArrayList<>();
		final ArrayList<BasicShape> selectedShapes = new ArrayList<>();
		for(final Object obj : focusSet)
		{
			if(obj instanceof SourceAndConverter)
				selectedSAC.add( ( SourceAndConverter< ? > ) obj );
			if(obj instanceof BasicShape)
				selectedShapes.add( ( BasicShape ) obj );
		}
		bvb.bvvViewer.sourceSelection.table.setSelectedSources( selectedSAC );
		bvb.bvbCards.panelShapes.tableShapes.setSelectedShapes( selectedShapes );
		bvb.updateSceneRender();

	}
	
	public void turnOffManualTransform(final boolean bShowMessage)
	{
		if( bvb.getInputLock() )
			return;
		
		if(bvb.bManualTransformMode)
		{
			bvb.bManualTransformMode = false;
			if(bShowMessage)
			{
				bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "manual transform mode off", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
			}
			SwingUtilities.invokeLater(() -> {
				this.transfromModeOverlay.setEnabled(false);
				bvb.repaintBVV();
			});
		}
	}
	
	void actionToggleManualTransform()
	{
		if( bvb.getInputLock() )
			return;
		bvb.bManualTransformMode = !bvb.bManualTransformMode;
		
		if(bvb.bManualTransformMode)
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "manual transform mode on", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );			
		}
		else
		{
			bvb.bvvViewer.addOverlayAnimator( new ColorTextOverlayAnimator( "manual transform mode off", 800, TextPosition.BOTTOM_RIGHT, BVBSettings.canvasOverlayColor )  );
		}
		SwingUtilities.invokeLater(() -> {
			this.transfromModeOverlay.setEnabled( bvb.bManualTransformMode);
			bvb.repaintBVV();
		});
	}
	
	public void alignToAxis( final int nAxis )
	{
		switch (nAxis)
		{
		case 0:
			alignToPlane(AlignPlaneBVB.ZY);
			break;
		case 1:
			alignToPlane(AlignPlaneBVB.XZ);
			break;
		case 2:
			alignToPlane(AlignPlaneBVB.XY);
			break;
		case 3:
			alignToPlane(AlignPlaneBVB.YZ);
			break;
		case 4:
			alignToPlane(AlignPlaneBVB.ZX);
			break;
		case 5:
			alignToPlane(AlignPlaneBVB.YX);
			break;
		}
	}
	
	void alignToPlane(final AlignPlaneBVB plane)
	{
//		//test
//		double [] axis = new double[3];
//		axis[0] = 1.0;
//		double [] quatAddition = new double[4];
//		LinAlgHelpers.quaternionFromAngleAxis( axis, -Math.PI, quatAddition );
//		
//		double [] quatCurrent = new double[]{ 0, 0, 1, 0 };
//		
//		LinAlgHelpers.quaternionMultiply( quatCurrent, quatAddition, quatCurrent );
		
		final double[] qTarget = new double[ 4 ];
		LinAlgHelpers.quaternionInvert( plane.qAlign, qTarget );
		final AffineTransform3D transform = bvb.bvvViewer.state().getViewerTransform();
		final double centerX = bvb.bvvViewer.getWidth() * 0.5;
		final double centerY = bvb.bvvViewer.getHeight() * 0.5;
		bvb.bvvViewer.setTransformAnimator( new RotationAnimator( transform, centerX, centerY, qTarget, 300 ) );
	}
	
	public void rotate(final int nAxis, boolean bViewCoords)
	{
		final double centerX = bvb.bvvViewer.getWidth() * 0.5;
		final double centerY = bvb.bvvViewer.getHeight() * 0.5;
		final AffineTransform3D transform = bvb.bvvViewer.state().getViewerTransform();

		if(bViewCoords)
		{
			final AffineTransform3D transformNew = new  AffineTransform3D();
			transformNew.set( transform );
			// center shift
			transformNew.set( transformNew.get( 0, 3 ) - centerX, 0, 3 );
			transformNew.set( transformNew.get( 1, 3 ) - centerY, 1, 3 );	
			//rotate
			transformNew.rotate( nAxis, 0.5 * Math.PI );
			// center un-shift
			transformNew.set( transformNew.get( 0, 3 ) + centerX, 0, 3 );
			transformNew.set( transformNew.get( 1, 3 ) + centerY, 1, 3 );
			bvb.bvvViewer.setTransformAnimator( new AnisotropicTransformAnimator3D(transform, transformNew, lRotationDuration ));
		}
		else
		{
			final double[] qTarget = new double[ 4 ];
			Affine3DHelpers.extractRotationAnisotropic( transform, qTarget );
			final double[] dAxis = new double[3];
			dAxis[nAxis] = 1.0;
			final double[] qRot = new double[ 4 ];
			LinAlgHelpers.quaternionFromAngleAxis(dAxis, 0.5 * Math.PI, qRot);
			LinAlgHelpers.quaternionMultiply( qTarget, qRot, qTarget );
			LinAlgHelpers.normalize( qTarget );
			//LinAlgHelpers.quaternionInvert( qTarget, qTarget );			
			bvb.bvvViewer.setTransformAnimator( new RotationAnimator(transform, centerX, centerY, qTarget, lRotationDuration ));

		}
	}
	
	/**
	 * The planes which can be aligned with the viewer coordinate system: XY,
	 * ZY, and XZ plane.
	 * Diffenrent from BDV, since 
	 * in XY plain align Z looks towards viewer (and X, Y oriented as in ImageJ) 
	 * and Z looks up for two other (like in Blender)
	 */
	public enum AlignPlaneBVB
	{
		ZY( 0, new double[] { 0.5, -0.5, -0.5, 0.5 } ),
		XZ( 1, new double[] { 0, 0, cQuat, -cQuat } ),
		XY( 2, new double[] { 0, 0, 1, 0 } ),
		YZ( 3, new double[] { 0.5, -0.5, 0.5, -0.5 } ),
		ZX( 4, new double[] { cQuat, -cQuat, 0, 0 } ),
		YX( 5, new double[] { 0, 0, 0, 1 } );

		/**
		 * rotation from the xy-plane aligned coordinate system to this plane.
		 */
		public final double[] qAlign; 

		/**
		 * Axis index. The plane spanned by the remaining two axes will be
		 * transformed to the same plane by the computed rotation and the
		 * "rotation part" of the affine source transform.
		 * @see Affine3DHelpers#extractApproximateRotationAffine(AffineTransform3D, double[], int)
		 */
		public final int coerceAffineDimension;

		private AlignPlaneBVB( final int coerceAffineDimension, final double[] qAlign )
		{
			this.coerceAffineDimension = coerceAffineDimension;
			this.qAlign = qAlign;
		}
	}
	
}
