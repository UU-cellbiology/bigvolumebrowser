package bvb.scijava;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.command.Command;

@Plugin(type = Command.class)//, menuPath = CommandConstants.CONTEXT_MENU_ITEMS_ROOT + "Configure BigVolumeViewer Rendering")
public class ConfigureRenderWindow implements Command
{
	@Parameter( label = "Render width" )
	public int renderWidth = 600;

	@Parameter( label = "Render height" )
	public int renderHeight = 600;

	@Parameter( label = "Dither window size",
			choices = { "none (always render full resolution)", "2x2", "3x3", "4x4", "5x5", "6x6", "7x7", "8x8" } )
	public String dithering = "3x3";
	
	@Parameter( label = "Number of dither samples",
			description = "Pixels are interpolated from this many nearest neighbors when dithering. This is not very expensive, it's fine to turn it up to 8.",
			min="1",
			max="8",
			style="slider")
	public int numDitherSamples = 3;

	@Parameter( label = "GPU cache tile size" )
	public int cacheBlockSize = 32;

	@Parameter( label = "GPU cache size (in MB)",
				description = "The size of the GPU cache texture will match this as close as possible with the given tile size." )
	public int maxCacheSizeInMB = 500;

	@Parameter( label = "Camera distance",
				description = "Distance from camera to z=0 plane. In units of pixel width." )
	public double dCam = 3000;

	@Parameter( label = "Clip distance far",
	description = "Visible depth from z=0 further away from the camera. In units of pixel width.")
	public double dClipFar = 1000;
	
	@Parameter( label = "Clip distance near",
			description = "Visible depth from z=0 closer to the camera. In units of pixel width. MUST BE SMALLER THAN CAMERA DISTANCE!")
	public double dClipNear = 1000;

	
	@Override
	public void run()
	{
		//MoBIE.getInstance().getViewManager().getBigVolumeViewer().updateBVVRenderSettings();
	}
}