package bvb.illustration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;

import bdv.export.ExportMipmapInfo;
import bdv.export.ExportScalePyramid;
import bdv.export.ProgressWriter;
import bdv.export.ProposeMipmaps;
import bdv.export.SubTaskProgressWriter;
import bdv.export.WriteSequenceToHdf5;
import bdv.export.ExportScalePyramid.LoopbackHeuristic;
import bdv.ij.util.PluginHelper;
import bdv.ij.util.ProgressWriterIJ;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bvb.core.BigVolumeBrowser;
import bvb.gui.data.BVBSpimDataInfo;
import ij.ImageJ;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoints;

public class ShowLOD
{
	public static void main( final String[] args )
	{
				
		new ImageJ();

		//start BVB
		BigVolumeBrowser bvb = new BigVolumeBrowser(); 		
		bvb.startBVB("");
		long [] dims = new long[3];
		dims[0] = 256;
		dims[1] = 256;
		dims[2] = 2048;
//		for(int d = 0; d < 3; d++)
//		{
//			dims[d] = 512;
//		}
		AbstractSpimData< ? > spimData = ColoredLODSpimData.getLODSpimData( dims, 6 ) ;
		bvb.addSpimData( spimData, new BVBSpimDataInfo("LOD_test", bvb.dataTreeModel.getIconOneSource()) );
		
//		final ProgressWriter progressWriter = new ProgressWriterIJ();
//		progressWriter.out().println( "starting export..." );
//		String fullPathNoExt = "/home/eugene/Desktop/test_lod";
//		final File hdf5File = new File( fullPathNoExt + ".h5" );
//		final File xmlFile = new File( fullPathNoExt + ".xml" );
//		AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
//		
//		final ExportMipmapInfo autoMipmapSettings = ProposeMipmaps.proposeMipmaps( seq.getViewSetups().get( 0 ) );
//		final Map< Integer, ExportMipmapInfo > perSetupExportMipmapInfo = new HashMap<>();
//		final ExportMipmapInfo mipmapInfo = autoMipmapSettings;
//		for ( final BasicViewSetup setup : seq.getViewSetupsOrdered() )
//			perSetupExportMipmapInfo.put( setup.getId(), mipmapInfo );
//		
//		final int numCellCreatorThreads = Math.max( 1, PluginHelper.numThreads() - 1 );
//		final LoopbackHeuristic loopbackHeuristic = new LoopbackHeuristic()
//		{
//			@Override
//			public boolean decide( final RandomAccessibleInterval< ? > originalImg, final int[] factorsToOriginalImg, final int previousLevel, final int[] factorsToPreviousLevel, final int[] chunkSize )
//			{
//				if ( previousLevel < 0 )
//					return false;
//
//				if ( Intervals.numElements( factorsToOriginalImg ) / Intervals.numElements( factorsToPreviousLevel ) >= 8 )
//					return true;
//
//				return false;
//			}
//		};
//		final ExportScalePyramid.AfterEachPlane afterEachPlane = usedLoopBack ->
//		{ };
//		WriteSequenceToHdf5.writeHdf5File( seq, perSetupExportMipmapInfo, true, hdf5File, loopbackHeuristic, afterEachPlane, numCellCreatorThreads, new SubTaskProgressWriter( progressWriter, 0, 0.95 ) );
//		//save spimdata
//
//		final Hdf5ImageLoader hdf5Loader = new Hdf5ImageLoader( hdf5File, null, null, false );
//		final SequenceDescriptionMinimal seqh5 = new SequenceDescriptionMinimal( ( SequenceDescriptionMinimal ) seq, hdf5Loader );
//		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
//		registrations.add( new ViewRegistration( 0, 0, new AffineTransform3D() ) );
//		final File basePath = xmlFile.getParentFile();
//		final SpimDataMinimal spimSaveData = new SpimDataMinimal( basePath, seqh5, new ViewRegistrations( registrations ) );
//		try
//		{
//			new XmlIoSpimDataMinimal().save( spimSaveData, xmlFile.getAbsolutePath() );
//			progressWriter.setProgress( 1.0 );
//		}
//		catch ( final Exception e )
//		{
//			throw new RuntimeException( e );
//		}
	}
}
