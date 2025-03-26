package bvb.gui;

import java.awt.Color;

import javax.swing.border.TitledBorder;

public class PanelTitle extends TitledBorder{

	/**
	 *  making a title for a panel
	 */
	private static final long serialVersionUID = 1L;
	
	public PanelTitle(String paramString) {
		super(paramString);
		this.setTitleColor(Color.DARK_GRAY);
		this.setTitleJustification(TitledBorder.CENTER);
	}



}
