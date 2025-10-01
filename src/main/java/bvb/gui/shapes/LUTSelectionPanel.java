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
		this.add( new JLabel("Lookup table:"), gbc );
		gbc.gridx++;
		lutButton = new JButton();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.1;
		this.add( lutButton, gbc );

		//colorButton.addActionListener( e -> chooseColor() );

		lutButton.setBorderPainted( false );
		lutButton.setFocusPainted( false );
		lutButton.setContentAreaFilled( false );
		lutButton.setMinimumSize( new Dimension( 30, 30 ) );
		lutButton.setPreferredSize( new Dimension( 30, 30 ) );
		
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
		lutButton.setIcon( new ColorIconPG( new Color( this.color.get() ), null, 100, 28, 10, 10, true ) );
	}

	public void setICM(final IndexColorModel icm_, String icmName_)
	{
		this.icm = icm_;
		this.icmName = icmName_;
		lutButton.setIcon( new ColorIconPG( null, icm, 100, 28, 10, 10, true ) );
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
