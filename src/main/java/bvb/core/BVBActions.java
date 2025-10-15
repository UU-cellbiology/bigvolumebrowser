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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerModel;

import net.imglib2.RealInterval;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bvb.gui.CanvasSelection;
import bvb.gui.CenterZoomBVV;
import bvb.gui.Rotate3DViewerStyle;
import bvb.shapes.BasicShape;
import bvvpg.vistools.BvvHandle;

import ij.Prefs;

public class BVBActions
{
	final BigVolumeBrowser bvb;
	
	final Actions actions;
	final Behaviours behaviours;
	
	public BVBActions(final BigVolumeBrowser bvb_) 
	{
		bvb = bvb_;
		actions = new Actions( new InputTriggerConfig() );
		behaviours = new Behaviours( new InputTriggerConfig() );
		installBehaviors();
		installActions();
	}
	
	/** install smoother rotation **/
	void installBehaviors()
	{
		final BvvHandle handle = bvb.bvvHandle;
		
		//change drag rotation for navigation "3D Viewer" style
		final Rotate3DViewerStyle dragRotate = new Rotate3DViewerStyle( 0.75, handle);
		final Rotate3DViewerStyle dragRotateFast = new Rotate3DViewerStyle( 2.0, handle);
		final Rotate3DViewerStyle dragRotateSlow = new Rotate3DViewerStyle( 0.1, handle);
		
		behaviours.behaviour( dragRotate, "drag rotate", "button1" );
		behaviours.behaviour( dragRotateFast, "drag rotate fast", "shift button1" );
		behaviours.behaviour( dragRotateSlow, "drag rotate slow", "ctrl button1" );
		behaviours.install( handle.getTriggerbindings(), "BigTrace Behaviours" );
	}
	
	void installActions()
	{
		actions.runnableAction(() -> dummy(), "cycle current", "C" );
		actions.runnableAction(() -> actionCenterView(), "center view (zoom out)", "C" );
		actions.runnableAction(() -> actionToggleVisibility(), "toggle visibility", "V" );
		actions.runnableAction(() -> actionSelectObject(), "select object", "E" );
		actions.runnableAction(() -> showHelpWindow(), "help", "F1" );
		actions.runnableAction(() -> runSettingsCommand(), "settings", "F10" );
		
		actions.install( bvb.bvvHandle.getKeybindings(), "BigTrace actions" );
		
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
		JPanel pViewSettings = new JPanel(new GridBagLayout());
		
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
		for(int i=1; i<=4; i++)
		{
			labelTable.put( new Integer( i*2 ), new JLabel(Integer.toString( i*2 )) );
			
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
		dClipNear.addChangeListener( new ChangeListener()
				{

					@Override
					public void stateChanged( ChangeEvent arg0 )
					{
						int currNear =  ((Double)dClipNear.getValue()).intValue();
						((SpinnerNumberModel)dCam.getModel()).setMinimum( new Double(currNear+5) );
						if(currNear > ((Double)dCam.getValue()).intValue())
						{
							dCam.setValue( currNear+5 );
							//(dCam.getModel()).setMinimum( new Integer(currNear+5) );
						}
					}

			
				}
				);
		
		gbcL.insets = new Insets(5,5,5,5);
		gbcR.insets = new Insets(5,5,5,5);
		gbcL.anchor = GridBagConstraints.EAST;
		gbcR.fill = GridBagConstraints.HORIZONTAL;
		gbcR.weightx = 1.0;
		
		gbcL.gridx=0;
		gbcR.gridx=1;
		gbcL.gridy=0;
		gbcR.gridy=0;
		pViewSettings.add( new JLabel("Render width"), gbcL );	
		pViewSettings.add( renderWidth,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Render height"), gbcL );
		pViewSettings.add( renderHeight,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Dither window size"), gbcL );
		pViewSettings.add( ditherWidthsList, gbcR );

		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("Number of dither samples"), gbcL );
		pViewSettings.add( slNumDitherSamples,gbcR );

		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("GPU cache tile size"), gbcL );
		pViewSettings.add( cacheBlockSize,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
		pViewSettings.add( new JLabel("GPU cache size (in MB)"), gbcL );
		pViewSettings.add( maxCacheSizeInMB,gbcR );
		
		gbcL.gridy++;
		gbcR.gridy++;
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
		
		
		int reply = JOptionPane.showConfirmDialog(null, pViewSettings, "BVV canvas settings", 
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
			
			nTempInt = ditherWidthsList.getSelectedIndex()+1;
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
		if(!bvb.bLocked && !(c instanceof JTextField))
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
		if(!bvb.selectedObjects.isAnythingSelected() )
			return;
		
		if(bvb.selectedObjects.areSourcesSelected())
		{
			final List< ConverterSetup > csList = bvb.selectedObjects.getSelectedSources();
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
				+"&nbsp;<b>V</b> - toggle visibility of selected objects<br><br>"
				+"&nbsp;<b>O</b> - toggle render method<br><br>"
				+"&nbsp;<b>S</b> - separate brightness/color dialog<br><br>"
				+"&nbsp;<b>Shift + X/Y/Z</b> - rotate to major plane<br><br>"					
				+"&nbsp;<b>M</b>/<b>N</b> - timepoint +/- <br><br>"
				+"&nbsp;<b>F10</b> - BVV (canvas) settings<br><br></html>";
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(pHelp);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	void actionSelectObject()
	{
		Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		//solution for now, to not interfere with typing
		if(!bvb.bLocked && !(c instanceof JTextField))
		{
			CanvasSelection.findClosestObjectOnCanvasOnClick(bvb);
		}
	}
	
}
