package bvb.gui;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.imglib2.Interval;

import bdv.tools.brightness.ConverterSetup;
import bdv.ui.UIUtils;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.source.converters.GammaConverterSetup;


public class ClipPanel extends JPanel implements ItemListener 
{

	private static final long serialVersionUID = 1885320351623882576L;
	private RangeSliderPanel [] bbAxes = new RangeSliderPanel[3];
	private ArrayList<Listener> listeners =	new ArrayList<>();

	public JCheckBox cbClipEnabled;
	public JLabel selectionWindow;
	
	private int nActiveWindow = -1;
	private List< ConverterSetup > csList = new ArrayList<>();
	
	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;
	
	public static interface Listener {
		public void boundingBoxChanged(long [][] box);

	}
	private synchronized void updateCS(int nSource, List< ConverterSetup > converterSetupList)
	{
		nActiveWindow = nSource;
		csList = converterSetupList;
		updateGUI();
	}
	
	@Override
	public void setEnabled(boolean bEnabled)
	{
		setEnabledSliders(bEnabled);
		cbClipEnabled.setEnabled( bEnabled );
		
	}
	
	public void setEnabledSliders(boolean bEnabled)
	{
		for(int i=0;i<3;i++)
		{
			bbAxes[i].setEnabled( bEnabled );
		}
	}
	
	private synchronized void updateGUI()
	{
		updateColors();
		switch (nActiveWindow)
		{
		case 0:
			selectionWindow.setText( "Sources");
			break;
		case 1:
			selectionWindow.setText( "Groups");
			break;
		default:
			selectionWindow.setText( "None");
		}	
		if(csList.size()==0)
		{
			setEnabled(false);
			return;
		}
		setEnabled(true);	
		//consistent clipping
		boolean bClipConsistent = true;
		int bClipEnabled = -1;
		for ( final ConverterSetup cs: csList)
		{
			 
			if(bClipEnabled<0)
			{
				bClipEnabled = ((GammaConverterSetup)cs).clipActive()?1:0;	
			}
			else
			{
				bClipConsistent &= (bClipEnabled==(((GammaConverterSetup)cs).clipActive()?1:0));
			}
			
		}
		if(bClipConsistent)
		{
			cbClipEnabled.setBackground( consistentBg );
			cbClipEnabled.setSelected( bClipEnabled !=0 );
			setEnabledSliders(bClipEnabled !=0);
		}
		else
		{
			setEnabledSliders(true);
			cbClipEnabled.setBackground(inConsistentBg );
		}
	}
	

	public ClipPanel(final VolumeViewerPanel viewer, SelectedSources sourceSelection) 
	{
		super();
		sourceSelection.addSourceSelectionListener(  new SelectedSources.Listener()
		{
			
			@Override
			public void selectedSourcesChanged(int nWindow, List< ConverterSetup > converterSetupList )
			{
				updateCS(nWindow, converterSetupList );
			}
		} );
		
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints cd = new GridBagConstraints();

		setLayout(gridbag);

		cd.gridy = 0;
		//cd.gridwidth=3;
		String[] axesS = {"X","Y","Z"};
		for(int d=0;d<3;d++)
		{
			bbAxes[d] = addRangeSlider(
					axesS[d],
					new int[] {0, 100},
					new int[] {0, 100},
					cd);
		}

		cd.gridwidth=1;
		cd.weightx = 0.1;
		cd.fill = GridBagConstraints.NONE;
		cd.anchor = GridBagConstraints.WEST;
		cbClipEnabled = new JCheckBox("Clipping", false);
		cbClipEnabled.addItemListener( this );
		cd.gridx = 1;
		this.add(cbClipEnabled,cd);
		selectionWindow = new JLabel("None");
		cd.gridx = 2;
		this.add(selectionWindow,cd);



		RangeSliderPanel.Listener bbListener = new RangeSliderPanel.Listener() {
			@Override
			public void sliderChanged() 
			{
				long [][] new_box = new long [2][3];
				for(int d=0;d<3;d++)
				{
					new_box [0][d] = bbAxes[d].getMin();
					new_box [1][d] = bbAxes[d].getMax();					
				}
				fireBoundingBoxChanged(new_box);
			}
		};
		for(int d=0;d<3;d++)
		{
			bbAxes[d].addSliderChangeListener(bbListener);
		}		
		updateGUI();
	}
	
	private RangeSliderPanel addRangeSlider(String label, int[] realMinMax, int[] setMinMax, GridBagConstraints c) {
		RangeSliderPanel slider = new RangeSliderPanel(realMinMax, setMinMax);

		GridBagLayout layout = (GridBagLayout)getLayout();

		c.gridx = 0;
		if(label != null) {
			JLabel theLabel = new JLabel(label);
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.EAST;
			c.gridwidth = 1;
			c.weightx = 0;
			layout.setConstraints(theLabel, c);
			add(theLabel);
			c.gridx++;
		}
		//c.gridx++;
		c.gridwidth=3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		layout.setConstraints(slider, c);
		add(slider);
		c.gridy++;
		return slider;
	}


	public void setBoundingBox(int bbx0, int bby0, int bbz0, int bbx1, int bby1, int bbz1) 
	{
		bbAxes[0].setMinAndMax(bbx0, bbx1);
		bbAxes[1].setMinAndMax(bby0, bby1);
		bbAxes[2].setMinAndMax(bbz0, bbz1);
	}
	
	public void setBoundingBox(final long [][] box) 
	{
		for(int d=0;d<3;d++)
		{
			bbAxes[d].setMinAndMax((int)box[0][d], (int)box[1][d]);
		}

	}
	
	public void setBoundingBox(final Interval interval) 
	{
		long [][] box = new long[2][3];
		box[0]=interval.minAsLongArray();
		box[1]=interval.maxAsLongArray();
		setBoundingBox(box);
	}
	
	public long [][] getBoundingBox()
	{
		long [][] boxout = new long[2][3];
		for(int d=0;d<3;d++)
		{
			boxout[0][d] = bbAxes[d].getMin();
			boxout[1][d] = bbAxes[d].getMax();			
		}
		
		return boxout;
	}


	public void addClipPanelListener(Listener l) {
        listeners.add(l);
    }

	private void fireBoundingBoxChanged(long [][] box) {
		for(Listener l : listeners)
			l.boundingBoxChanged(box);
	}
	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

	private void updateClipEnabled()
	{
		boolean bEnabled = cbClipEnabled.isSelected();
		for ( final ConverterSetup cs: csList)
		{
			  ((GammaConverterSetup)cs).setClipActive( bEnabled );
		}
	}
	@Override
	public void itemStateChanged( ItemEvent arg0 )
	{
		if(arg0.getSource() == cbClipEnabled)
			updateClipEnabled();
		updateGUI();
	}

}
