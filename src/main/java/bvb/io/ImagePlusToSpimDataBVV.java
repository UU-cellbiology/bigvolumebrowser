package bvb.io;

import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.LutLoader;
import ij.process.LUT;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.base.ViewSetupAttributes;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.MissingViews;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.ViewId;
import spimdata.util.Displaysettings;

// modified version of
// https://github.com/BIOP/bigdataviewer-image-loaders/blob/5d3a3d56da3e73052e34d64f301a0e3e8f9803ac/src/main/java/ch/epfl/biop/bdv/img/imageplus/ImagePlusToSpimData.java
public class ImagePlusToSpimDataBVV {

	static final private Logger logger = LoggerFactory.getLogger(
			ImagePlusToSpimDataBVV.class);

	// Function stolen and modified from bigdataviewer_fiji
	public static AbstractSpimData<?> getSpimData(ImagePlus imp)
			throws UnsupportedOperationException
	{
		// check the image type
		switch (imp.getType()) {
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
				break;
			default:
				String message = "Error in image " + imp.getShortTitle() +
						": Only 8, 16 are supported currently!";
				logger.error(message);
				throw new UnsupportedOperationException(message);
		}

		// get calibration and image size
		final double pw = imp.getCalibration().pixelWidth;
		final double ph = imp.getCalibration().pixelHeight;
		final double pd = imp.getCalibration().pixelDepth;
		String punit = imp.getCalibration().getUnit();
		if (punit == null || punit.isEmpty()) punit = "px";
		final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions(punit, pw,
				ph, pd);
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getNSlices();
		final FinalDimensions size = new FinalDimensions(w, h, d);

		int originTimePoint = 0;
		final BasicImgLoader imgLoader;
		{
			switch (imp.getType()) {
				case ImagePlus.GRAY8:
					imgLoader = ImagePlusImageLoaderBVV.createUnsignedByteInstance(imp,
							originTimePoint);
					break;
				default:
					//ImagePlus.GRAY16:
					imgLoader = ImagePlusImageLoaderBVV.createUnsignedShortInstance(imp,
							originTimePoint);
					break;
			}
		}

		final int numTimepoints = imp.getNFrames();
		final int numSetups = imp.getNChannels();
		ViewSetupAttributes.registerManually(XmlIoLutNameFIJI.class);
		// create setups from channels
		final HashMap<Integer, BasicViewSetup> setups = new HashMap<>(numSetups);
		for (int s = 0; s < numSetups; ++s) {
			final BasicViewSetup setup = new BasicViewSetup(s, String.format(imp
					.getTitle() + " channel %d", s + 1), size, voxelSize);
			
			
			setup.setAttribute(new Channel(s + 1));
			Displaysettings ds = new Displaysettings(s + 1);
			imp.setPositionWithoutUpdate(s+1,1,1);
			ds.min = imp.getDisplayRangeMin();
			ds.max = imp.getDisplayRangeMax();
			LUTNameFIJI lutName = new LUTNameFIJI(s+1);
			
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				ds.isSet = false;
				lutName.isSet = false;
			}
			else {
				ds.isSet = true;
				LUT[] luts = imp.getLuts();
				LUT lut = luts.length>s ? luts[s]:luts[0];
				ds.color = new int[] { lut.getRed(255), lut.getGreen(255), lut.getBlue(
						255), lut.getAlpha(255) };				
				
				lutName.sLUTName = getProperLUTName(lut);
				if(lutName.sLUTName == null)
				{
					lutName.sLUTName = "";
				}
				lutName.isSet = true;
			}
			setup.setAttribute(ds);
			setup.setAttribute(lutName);
			setups.put(s, setup);
		}

		// create timepoints
		final ArrayList<TimePoint> timepoints = new ArrayList<>(numTimepoints);

		MissingViews mv = null;

		if (originTimePoint > 0) {

			Set<ViewId> missingViewIds = new HashSet<>();
			for (int t = 0; t < originTimePoint; t++) {
				for (int s = 0; s < numSetups; ++s) {
					ViewId vId = new ViewId(t, s);
					missingViewIds.add(vId);
				}
			}
			mv = new MissingViews(missingViewIds);
		}

		for (int t = 0; t < numTimepoints + originTimePoint; ++t)
			timepoints.add(new TimePoint(t));
		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal(
				new TimePoints(timepoints), setups, imgLoader, mv);

		// create ViewRegistrations from the images calibration
		final AffineTransform3D sourceTransform = getMatrixFromImagePlus(imp);
		
		final ArrayList<ViewRegistration> registrations = new ArrayList<>();
		for (int t = 0; t < numTimepoints + originTimePoint; ++t)
			for (int s = 0; s < numSetups; ++s)
				registrations.add(new ViewRegistration(t, s, sourceTransform));

		final File basePath = new File(".");

		return new SpimDataMinimal(basePath, seq,
				new ViewRegistrations(registrations));
	}
	
	public static AffineTransform3D getMatrixFromImagePlus(ImagePlus imp) 
	{

		// Otherwise : use Calibration from ImagePlus
		if (imp.getCalibration() != null) {
			AffineTransform3D at3D = new AffineTransform3D();
			// Matrix built from calibration
			at3D.scale(imp.getCalibration().pixelWidth, imp
					.getCalibration().pixelHeight, imp.getCalibration().pixelDepth);
			at3D.translate(imp.getCalibration().xOrigin * imp
					.getCalibration().pixelWidth, imp.getCalibration().yOrigin * imp
					.getCalibration().pixelHeight, imp.getCalibration().zOrigin * imp
					.getCalibration().pixelDepth);
			return at3D;
		}

		// Default : returns identity
		return new AffineTransform3D();
	}
	
	/** this is pretty complicated method to get proper LUT name,
	 * but I could not find anything more simple **/
	public static String getProperLUTName(LUT lut)
	{
		String[] allLuts = IJ.getLuts();
		for(int i=0; i<allLuts.length; i++)
		{
			IndexColorModel icm = LutLoader.getLut(allLuts[i]);
			if(icm.equals( lut.getColorModel() ))
			{
				if(compareICM(icm,lut.getColorModel()))
				{
					return allLuts[i];
				}
			}
		}
		return null;
	}
	public static boolean compareICM(IndexColorModel icm1, IndexColorModel icm2)
	{
		if(icm1.getMapSize()!=icm2.getMapSize())
			return false;
		final int size = icm1.getMapSize();

		final int [] all1 = new int [size];
		final int [] all2 = new int [size];

		icm1.getRGBs( all1 );
		icm2.getRGBs( all2 );
		for(int i=0; i<size;i++)
		{
			if(all1[i]!=all2[i])
				return false;
		}
		return true;
		
	}
}