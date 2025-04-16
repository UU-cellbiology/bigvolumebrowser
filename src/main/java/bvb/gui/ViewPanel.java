package bvb.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import bdv.tools.brightness.ColorIcon;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import ij.Prefs;

public class ViewPanel extends JPanel
{
	
	final BigVolumeBrowser bvb;
	JToggleButton butOrigin;
	JToggleButton butVBox;
	JButton butProjType;
	JButton butFullScreen;
	JButton butSettings;
	final ImageIcon [] projIcon = new ImageIcon[2];
	final String[] projToolTip = new String[2];
	
	public ColorUserSettings selectColors = new ColorUserSettings();
	
	public ViewPanel(final BigVolumeBrowser bvb_)
	{
		super();
		setLayout(new GridBagLayout());
		bvb = bvb_;
		//this.setBorder(new PanelTitle(" View "));
		
		//ORIGIN
		URL icon_path = this.getClass().getResource("/icons/orig.png");
	    ImageIcon tabIcon = new ImageIcon(icon_path);
	    butOrigin = new JToggleButton(tabIcon);
	    //butOrigin.setSelected(btdata.bShowOrigin);
	    butOrigin.setToolTipText("Show XYZ axes");
	    
	    //BOX AROUND
		icon_path = this.getClass().getResource("/icons/boxvolume.png");
	    tabIcon = new ImageIcon(icon_path);
	    butVBox = new JToggleButton(tabIcon);
	    //butVBox.setSelected(btdata.bVolumeBox);
	    butVBox.setToolTipText("Volume Box");
	    butVBox.setSelected( BVBSettings.bShowVolumeBoxes  );
	    butVBox.addItemListener(new ItemListener() {

	    	@Override
	    	public void itemStateChanged(ItemEvent e) 
	    	{
	    		if(e.getStateChange() == ItemEvent.SELECTED)
	    		{
	    			bvb.volumeBoxes.setVisible( true );
	    			Prefs.set("BVB.bShowVolumeBoxes", true);
	    			bvb.repaintBVV();
	    		} else if(e.getStateChange()==ItemEvent.DESELECTED)
	    		{
	    			bvb.volumeBoxes.setVisible( false );
	    			Prefs.set("BVB.bShowVolumeBoxes", false);
	    			bvb.repaintBVV();
	    		}
	    	}
	    });
	    
	    //PROJECTION MATRIX
	    projToolTip[0] = "Perspective";
	    projToolTip[1] = "Orthographic";
		icon_path = this.getClass().getResource("/icons/proj_persp.png");
		projIcon[0] = new ImageIcon(icon_path);
		icon_path = this.getClass().getResource("/icons/proj_ortho.png");
		projIcon[1] = new ImageIcon(icon_path);

	    butProjType = new JButton(projIcon[bvb.bvvViewer.getProjectionType()]);
	    butProjType.setToolTipText( projToolTip[bvb.bvvViewer.getProjectionType() ]);
	    
	    butProjType.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				int newProj = 0; 
				if(bvb.bvvViewer.getProjectionType() == 0)
				{
					newProj = 1;
				}
				butProjType.setIcon( projIcon[newProj] );
				butProjType.setToolTipText( projToolTip[newProj]);
				bvb.bvvViewer.setProjectionType(newProj);
			}
	
		});
		
	    //FULL SCREEN
		icon_path = this.getClass().getResource("/icons/fullscreen.png");
	    tabIcon = new ImageIcon(icon_path);
	    butFullScreen = new JButton(tabIcon);
	    butFullScreen.setToolTipText("Full Screen");
	    butFullScreen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent arg0 )
			{
				bvb.makeFullScreen();								
			}
	    	
	    });	    
	    
		//SETTINGS
		icon_path = this.getClass().getResource("/icons/settings.png");
	    tabIcon = new ImageIcon(icon_path);
	    butSettings = new JButton(tabIcon);
	    butSettings.setToolTipText("Settings");
	    butSettings.addActionListener(new ActionListener()
	    		{
					@Override
					public void actionPerformed( ActionEvent arg0 )
					{
						
						dialSettings();
					}
	    	
	    		});
	    
	    GridBagConstraints gbc = new GridBagConstraints();

	    gbc.gridx = 0;
	    gbc.gridy = 0;
		this.add(butOrigin,gbc);
		
		gbc.gridx++;	    
		this.add(butVBox,gbc);
		
		gbc.gridx++;	    
		this.add(butProjType,gbc);
		
		gbc.gridx++;	    
		this.add(butFullScreen,gbc);
		
		gbc.gridx++;	    
		this.add(butSettings,gbc);

	}
	
	public void dialSettings()
	{
		JPanel pViewSettings = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
//		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
//		symbols.setDecimalSeparator('.');
//		DecimalFormat df3 = new DecimalFormat ("#.#####", symbols);
		
		JButton butCanvasBGColor = new JButton( new ColorIcon( BVBSettings.canvasBGColor ) );	
		butCanvasBGColor.addActionListener( e -> {
			Color newColor = JColorChooser.showDialog(bvb.controlPanel.cpFrame, "Choose background color", BVBSettings.canvasBGColor );
			if (newColor != null)
			{
				selectColors.setColor(newColor, 0);

				butCanvasBGColor.setIcon(new ColorIcon(newColor));
			}
			
		});
		
		NumberField nfAnimationDuration = new NumberField(5);
		nfAnimationDuration.setIntegersOnly(true);
		nfAnimationDuration.setText(Integer.toString(BVBSettings.nTransformAnimationDuration));
		
		gbc.gridx=0;
		gbc.gridy=0;	
		GBCHelper.alighLoose(gbc);
		
		pViewSettings.add(new JLabel("Background color: "),gbc);
		gbc.gridx++;
		pViewSettings.add(butCanvasBGColor,gbc);
		
		gbc.gridx=0;
		gbc.gridy++;
		pViewSettings.add(new JLabel("Transform animation duration (ms): "),gbc);
		gbc.gridx++;
		pViewSettings.add(nfAnimationDuration,gbc);
		
		
		int reply = JOptionPane.showConfirmDialog(null, pViewSettings, "View/Navigation Settings", 
		        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (reply == JOptionPane.OK_OPTION) 
		{
			Color tempC;
			
			tempC = selectColors.getColor(0);
			if(tempC != null)
			{
				setCanvasBGColor(tempC);
			}
			
			BVBSettings.nTransformAnimationDuration = Integer.parseInt(nfAnimationDuration.getText());
			Prefs.set("BVB.nTransformAnimationDuration",BVBSettings.nTransformAnimationDuration);
			bvb.repaintBVV();
		}
	}
	
	public void setCanvasBGColor(final Color bgColor)
	{
		BVBSettings.canvasBGColor = new Color(bgColor.getRed(),bgColor.getGreen(),bgColor.getBlue(),bgColor.getAlpha());
		selectColors.setColor(null, 0);
		Prefs.set("BVB.canvasBGColor", bgColor.getRGB());
		final Color bbFrameColor = BVBSettings.getInvertedColor(bgColor);
		bvb.volumeBoxes.setLineColor( bbFrameColor );
		//bt.clipBox.setLineColor( bbFrameColor.darker() );
	}
}
