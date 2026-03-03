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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.IndexColorModel;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import net.imglib2.type.numeric.ARGBType;

import org.scijava.listeners.Listeners;

import bdv.ui.UIUtils;
import bvvpg.ui.panels.ColorIconPG;
import ij.IJ;
import ij.plugin.LutLoader;

public class LUTSelectionPanel extends JPanel 
{
	public final JButton lutButton;

	private final ARGBType color = new ARGBType();
	
	public final JCheckBox cbInverted = new JCheckBox("Inv");
	
	private IndexColorModel icm = null;
	
	private String icmName = null;

	public interface ChangeListener
	{
		void lutChanged();
	}

	private final Listeners.List< ChangeListener > listeners = new Listeners.SynchronizedList<>();

	/**
	 * Whether the color reflects a set of sources all having the same color
	 */
	private boolean isConsistent = true;

	/**
	 * Panel background if color reflects a set of sources all having the same color
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if color reflects a set of sources with different colors
	 */
	private Color inConsistentBg = Color.WHITE;

	public LUTSelectionPanel()
	{
		setLayout( new GridBagLayout() );
		updateColors();
		GridBagConstraints gbc = new GridBagConstraints();	
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.anchor = GridBagConstraints.WEST;
		this.add( new JLabel("Lookup table:"), gbc );
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx++;
		lutButton = new JButton();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.5;
		this.add( lutButton, gbc );
		gbc.gridx++;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.NONE;
		//gbc.weightx = 0.0;

		this.add( cbInverted, gbc );

		cbInverted.addItemListener( (e) -> updateButton() );

		//colorButton.addActionListener( e -> chooseColor() );

		lutButton.setBorderPainted( false );
		lutButton.setFocusPainted( false );
		lutButton.setContentAreaFilled( false );
		lutButton.setMinimumSize( new Dimension( 105, 30 ) );
		lutButton.setPreferredSize( new Dimension( 105, 30 ) );
		
		lutButton.addMouseListener( new MouseAdapter()
		{ 
			@Override
			public void mouseClicked(MouseEvent evt)
			{
			    
			    if (lutButton.isEnabled()) 
			    {
			    	JPopupMenu popup = new JPopupMenu();
			    	String [] luts = IJ.getLuts();
			    	JMenuItem itemMenu;
			    	for(int i = 0; i<luts.length;i++)
			    	{
			    		itemMenu = new JMenuItem(luts[i]);
			    		itemMenu.addActionListener( new ActionListener()
			    		{

			    			@Override
			    			public void actionPerformed( ActionEvent arg0 )
			    			{
			    				String sLUTName = ((JMenuItem)arg0.getSource()).getText();
			    				setICMbyName(sLUTName);
			    				listeners.list.forEach( ChangeListener::lutChanged );

			    			}

			    		});
			    		popup.add(itemMenu);  
			    	}
			    	popup.show( evt.getComponent(), evt.getX(), evt.getY() );
			    }
			}
		});

		
		setColor( null );
	}

	@Override
	public void setEnabled( final boolean enabled )
	{
		super.setEnabled( enabled );
		if ( lutButton != null )
			lutButton.setEnabled( enabled );
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		updateColors();
		if ( !isConsistent )
			setBackground( inConsistentBg );
	}

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

	public void setConsistent( final boolean isConsistent )
	{
		this.isConsistent = isConsistent;
		setBackground( isConsistent ? consistentBg : inConsistentBg );
	}

	public Listeners< ChangeListener > changeListeners()
	{
		return listeners;
	}

	public synchronized void setColor( final ARGBType color )
	{
		if ( color == null )
			this.color.set( 0xffaaaaaa );
		else
			this.color.set( color );
		icm = null;
		icmName = null;
		updateButton();
	}


	public void setICM(final IndexColorModel icm_, String icmName_)
	{
		
		this.icm = icm_;
		this.icmName = icmName_;
		updateButton();

	}
	
	public void updateButton()
	{
		if(this.icm == null)
		{
			lutButton.setIcon( new ColorIconPG( new Color( this.color.get() ), null, 100, 28, 10, 10, true ) );
		}
		else
		{
			final ColorIconPG lutIcon = new ColorIconPG( null, icm, 100, 28, 10, 10, true );
			if(cbInverted.isSelected())
				lutIcon.bInvertedICM = true;
			lutButton.setIcon( lutIcon );
		}
	}
	
	public synchronized void setICMbyName(String icmName)
	{
		if(LutLoader.getLut(icmName) == null)
		{
			setColor(null);
			return;
		}
		setICM(LutLoader.getLut(icmName), icmName);
	}

	public ARGBType getColor()
	{
		return color.copy();
	}
	
	public IndexColorModel getICM()
	{
		return icm;
	}
	
	public String getICMName()
	{
		return icmName;
	}
}
