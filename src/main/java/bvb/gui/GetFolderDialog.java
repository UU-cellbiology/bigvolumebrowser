package bvb.gui;

import java.io.File;

import javax.swing.JFileChooser;

import bvb.core.BVBSettings;

import ij.Prefs;

public class GetFolderDialog
{
	public static String getSelectedFolder(final String sTitle)
	{
		final JFileChooser fc = new JFileChooser();
		
		fc.setDialogTitle( sTitle );
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory( new File(BVBSettings.lastDir) );
		
		int returnVal = fc.showSaveDialog( null );
		
		if(returnVal == JFileChooser.APPROVE_OPTION) 
		{
		    File saveFolder = fc.getSelectedFile();
		    BVBSettings.lastDir = saveFolder.getAbsolutePath();
		    Prefs.set( "BVB.lastDir", BVBSettings.lastDir );
		    return saveFolder.getAbsolutePath() + File.separator;

		}
		return null;
	}
}
