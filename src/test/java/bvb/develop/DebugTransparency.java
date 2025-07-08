package bvb.develop;

import java.awt.Color;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.ValuePair;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.MeshColor;
import bvvpg.vistools.BvvStackSource;
import ij.ImageJ;
import mpicbg.spim.data.generic.AbstractSpimData;

public class DebugTransparency
{
	public static void main( final String[] args )
	{
		new ImageJ();
	
		//start BVB
		BigVolumeBrowser bvb = new BigVolumeBrowser(); 		
		
		bvb.startBVB("");
		
		MeshColor cube = new MeshColor("/home/eugene/Desktop/projects/BVB/stl/3D_model_of_a_Cube.stl");
		cube.setColor( new Color(0,255,255,255) );
		AffineTransform3D tr = new AffineTransform3D();
		tr.translate( -0.5,-0.5,-0.5 );
		cube.setTransform( tr );
		bvb.addShape( cube );
		ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > vp = bvb.loadBioFormats( "/home/eugene/Desktop/projects/BVB/30x30x30_white.tif" );
		BvvStackSource< ? > source = vp.getB().get( 0 );
		
		source.setLUT( "Grays" );//.setColor( null );
		source.setRenderType( 1 );
		source.setDisplayRange( 0,1 );
		
		
	}
}
