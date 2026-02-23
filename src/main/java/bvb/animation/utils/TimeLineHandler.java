package bvb.animation.utils;

import java.awt.Color;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import bdv.viewer.SourceAndConverter;
import bvb.animation.KeyFrameScene;
import bvb.core.BigVolumeBrowser;
import bvb.io.ObjectHashStorage;
import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvb.utils.transform.TransformSetups;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.source.converters.Clippable3D;
import bvvpg.source.converters.GammaConverterSetup;

public class TimeLineHandler
{
	final ClipSetups clipSetups;
	
	final TransformSetups transformSetups;
	
	final VolumeViewerPanel bvvViewer;
	
	final ObjectHashStorage objectHashStorage;
	
	final Timeline timeline;
	
	public TimeLineHandler (final BigVolumeBrowser bvb, final Timeline timeline)
    {
    	clipSetups = bvb.bvbCards.clipPanel.clipSetups;
    	transformSetups = bvb.bvbCards.transformPanel.transformSetups;
    	bvvViewer = bvb.bvvViewer;
    	objectHashStorage = bvb.objectHashStorage;
    	this.timeline = timeline;
    }
	
    String getObjectName(final Object obj)
    {
    	return objectHashStorage.getBVBHashString( obj );
    }
	
    public void addKeyframeTransform(final Object obj, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String clName = getObjectName(obj);
    	if(clName == null)
    	{
    		return;
    	}
		//it is both BasicShape and GammaConverterSetup
    	if(obj instanceof Clippable3D )
    	{	      	
	    	// transform centers
	    	final Property<double []> pTrCenter = new Property<double []>() {
			    @Override
				public double [] get() { 
			    	{
			    		double [] out = new double [3];
			    		System.arraycopy( transformSetups.transformCenters.getCenters( obj ), 0, out, 0, 3 );
			    		return out;
			    	}}
			    @Override
				public void set(double [] v) 
			    	{ transformSetups.transformCenters.setCenters( obj, v );  
			    	  transformSetups.updateTransform( obj, null, false ); }
			};
			timeline.addKeyframe(clName + "_transform_center", pTrCenter, Interpolator.doubleArrayLerp, easing, keyFrameScene );
	    	
	    	// transform rotation
	    	final Property<double []> pTrRotation = new Property<double []>() {
			    @Override
				public double [] get() { 
			    	{
			    		double [] out = new double [4];
			    		System.arraycopy( transformSetups.transformRotation.getQuaternion( obj ), 0, out, 0, 4 );
			    		return out;
			    	}}
			    @Override
				public void set(double [] v) {
			    	transformSetups.transformRotation.setQuaternion( obj, v );
			    	 transformSetups.updateTransform( obj, null, false );
			    	}
			};
			timeline.addKeyframe(clName + "_transform_rotation", pTrRotation, Interpolator.quatSLerp, easing, keyFrameScene );
	    	
	    	// transform scale
	    	final Property<double []> pTrScale = new Property<double []>() {
			    @Override
				public double [] get() { 
			    	{
			    		double [] out = new double [3];
			    		System.arraycopy( transformSetups.transformScale.getScale( obj ), 0, out, 0, 3 );
			    		return out;
			    	}}
			    @Override
				public void set(double [] v) 
			    	{ transformSetups.transformScale.setScale( obj, v );  
			    	  transformSetups.updateTransform( obj, null, false ); }
			};
			timeline.addKeyframe(clName + "_transform_scale", pTrScale, Interpolator.doubleArrayLerp, easing, keyFrameScene );

	    	// transform deskew
	    	final Property<Double> pTrDeskew = new Property<Double>() {
			    @Override
				public Double get() { return transformSetups.transformDeskew.getAngle( obj ); }
			    @Override
				public void set(Double v) { 
			    	transformSetups.transformDeskew.setAngle( obj, v ); 
			    	transformSetups.updateTransform( obj, null, false );}
			};
			timeline.addKeyframe(clName + "_transform_deskew", pTrDeskew, Interpolator.doubleLerp, easing, keyFrameScene );

    	}
    }
    
    public void addKeyframeClippable3D(final Clippable3D clippable, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String clName = getObjectName (clippable);
    	if(clName == null)
    	{
    		return;
    	}
    	// clip type    	
    	final Property<Integer> pClipType = new Property<Integer>() {
		    @Override
			public Integer get() { return clippable.getClipState(); }
		    @Override
			public void set(Integer v) { clippable.setClipState( v );}
		};
		timeline.addKeyframe(clName + "_clip_type", pClipType, Interpolator.integerStep, easing, keyFrameScene );

    	// clip range
    	final Property<RealInterval> pClipRange = new Property<RealInterval>() {
		    @Override
			public RealInterval get() { 
			    	RealInterval interval = clippable.getClipInterval();
			    	//not sure this is the best, but let's keep it for now
			    	if(interval != null)
			    	{
			    		return interval;
			    	}
			    	final Bounds3D bounds = new Bounds3D(clipSetups.clipRangeBounds.getBounds( clippable ));
			    	return new FinalRealInterval(bounds.getMinBound(),bounds.getMaxBound(), true);
		    	}
		    @Override
			public void set(RealInterval v) { 
		    	if(clippable.getClipState() > 0 && v != null) 
		    	{ clippable.setClipInterval( v );} }
		};
		timeline.addKeyframe(clName + "_clip_range", pClipRange, Interpolator.realInterval, easing, keyFrameScene );
    	
    	// clip centers
    	final Property<double []> pClipCenter = new Property<double []>() {
		    @Override
			public double [] get() { 
		    	{
		    		double [] out = new double [3];
		    		System.arraycopy( clipSetups.clipCenters.getCenters( clippable ), 0, out, 0, 3 );
		    		return out;
		    	}}
		    @Override
			public void set(double [] v) { 
		    	clipSetups.clipCenters.setCenters( clippable, v ); 
		    	clipSetups.updateClipTransform( clippable, null );}
		};
		timeline.addKeyframe(clName + "_clip_center", pClipCenter, Interpolator.doubleArrayLerp, easing, keyFrameScene );
    	
    	// clip rotation
    	final Property<double []> pClipRotation = new Property<double []>() {
		    @Override
			public double [] get() { 
		    	{
		    		double [] out = new double [4];
		    		System.arraycopy( clipSetups.clipRotation.getQuaternion( clippable ) , 0, out, 0, 4 );
		    		return out;
		    	}}
		    @Override
			public void set(double [] v) { clipSetups.clipRotation.setQuaternion( clippable, v ); clipSetups.updateClipTransform( clippable, null );}
		};
		timeline.addKeyframe(clName + "_clip_rotation", pClipRotation, Interpolator.quatSLerp, easing, keyFrameScene );

    }
    
    public void addKeyframeConverterSetup(final GammaConverterSetup cs, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	
    	String csName = getObjectName(cs);
    	if(csName == null)
    	{
    		return;
    	}
    	final SourceAndConverter< ? > sac = transformSetups.converterSetups.getSource( cs );
    	
    	//visibility
    	final Property<Boolean> pDisplayVisible = new Property<Boolean>() {
		    @Override
			public Boolean get() { return bvvViewer.state().isSourceVisible(sac);  }
		    @Override
			public void set(Boolean v) { bvvViewer.state().setSourceActive( sac, v);}
		};   	
		timeline.addKeyframe(csName + "_visibility", pDisplayVisible, Interpolator.booleanStep, easing, keyFrameScene );
    	
    	//display range
    	final Property<double[]> pDisplayRange = new Property<double[]>() {
		    @Override
			public double[] get() { return new double[] {cs.getDisplayRangeMin(), cs.getDisplayRangeMax()}; }
		    @Override
			public void set(double[] v) { cs.setDisplayRange( v[0], v[1] ); }
		};   	
		timeline.addKeyframe(csName + "_displayRange", pDisplayRange, Interpolator.doubleArrayLerp, easing, keyFrameScene );
    	
		// alpha    	
    	final Property<Double> pAlpha = new Property<Double>() {
		    @Override
			public Double get() { return cs.getDisplayGamma(); }
		    @Override
			public void set(Double v) { cs.setDisplayGamma( v ); }
		};
		timeline.addKeyframe(csName + "_alpha", pAlpha, Interpolator.doubleLerp, easing, keyFrameScene );

    	//alpha range
    	final Property<double[]> pAlphaRange = new Property<double[]>() {
		    @Override
			public double[] get() { return new double[] {cs.getAlphaRangeMin(), cs.getAlphaRangeMax()}; }
		    @Override
			public void set(double[] v) { cs.setAlphaRange( v[0], v[1] ); }
		};   	
		timeline.addKeyframe(csName + "_alphaRange", pAlphaRange, Interpolator.doubleArrayLerp, easing, keyFrameScene ); 	
    	
		// alpha gamma    	
    	final Property<Double> pAlphaGamma = new Property<Double>() {
		    @Override
			public Double get() { return cs.getAlphaGamma(); }
		    @Override
			public void set(Double v) { cs.setAlphaGamma( v );}
		};
		timeline.addKeyframe(csName + "_alpha_gamma", pAlphaGamma, Interpolator.doubleLerp, easing, keyFrameScene );

		// render type    	
    	final Property<Integer> pRenderType = new Property<Integer>() {
		    @Override
			public Integer get() { return cs.getRenderType(); }
		    @Override
			public void set(Integer v) { cs.setRenderType( v );}
		};
		timeline.addKeyframe(csName + "_render_type", pRenderType, Interpolator.integerStep, easing, keyFrameScene );

		// render lighting type
    	final Property<Integer> pRenderLight = new Property<Integer>() {
		    @Override
			public Integer get() { return cs.getLightingType(); }
		    @Override
			public void set(Integer v) { cs.setLightingType( v );}
		};
		timeline.addKeyframe(csName + "_lighting_type", pRenderLight, Interpolator.integerStep, easing, keyFrameScene );

		// render voxel interpolation
    	final Property<Integer> pVoxelInterpolation = new Property<Integer>() {
		    @Override
			public Integer get() { return cs.getVoxelRenderInterpolation(); }
		    @Override
			public void set(Integer v) { cs.setVoxelRenderInterpolation( v ); }
		};
		timeline.addKeyframe(csName + "_voxel_interpolation", pVoxelInterpolation, Interpolator.integerStep, easing, keyFrameScene );

    }
    
    public void addKeyframeBasicShape(final BasicShape shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName (shape);
    	if(shapeName == null)
    	{
    		return;
    	}
    	// visibility
    	final Property<Boolean> pVisible = new Property<Boolean>() {
		    @Override
			public Boolean get() { return shape.isVisible(); }
		    @Override
			public void set(Boolean v) { shape.setVisible( v ); }
		};

		timeline.addKeyframe(shapeName + "_visible", pVisible, Interpolator.booleanStep, easing, keyFrameScene );
		if(shape instanceof BasicMeshShape)
		{
			addKeyframeBasicMeshShape((BasicMeshShape)shape, keyFrameScene, easing);
		}
    }
    
    public void addKeyframeBasicMeshShape(final BasicMeshShape shape, final KeyFrameScene keyFrameScene, final Easing easing)
    {
    	String shapeName = getObjectName (shape);
    	if(shapeName == null)
    	{
    		return;
    	}
    	// mesh color
    	final Property<Color> pColor = new Property<Color>() {
		    @Override
			public Color get() { return shape.getColor(); }
		    @Override
			public void set(Color v) { shape.setColor( v ); }
		};
		timeline.addKeyframe(shapeName + "_mesh_color", pColor, new HSBColorInterpolator(), easing, keyFrameScene );
		
		//use of texture
		if(shape.hasTexture())
		{
	    	// texture
	    	final Property<Boolean> pTexture = new Property<Boolean>() {
			    @Override
				public Boolean get() { return shape.isTextureUsed(); }
			    @Override
				public void set(Boolean v) { shape.useTexture( v ); }
			};
			timeline.addKeyframe(shapeName + "_use_texture", pTexture, Interpolator.booleanStep, easing, keyFrameScene );			
		}
		
		//render type
    	final Property<Integer> pRenderType = new Property<Integer>() {
		    @Override
			public Integer get() { return shape.getRenderType(); }
		    @Override
			public void set(Integer v) { shape.setRenderType( v ); }
		};
		timeline.addKeyframe(shapeName + "_render_type", pRenderType, Interpolator.integerStep, easing, keyFrameScene );			
    	
		// point size
    	final Property<Float> pPointSize = new Property<Float>() {
		    @Override
			public Float get() { return shape.getPointSize(); }
		    @Override
			public void set(Float v) { shape.setPointSize( v );}
		};
		timeline.addKeyframe(shapeName + "_point_size", pPointSize, Interpolator.floatLerp, easing, keyFrameScene );			

		//surface render type
    	final Property<Integer> pSurfaceRenderType = new Property<Integer>() {
		    @Override
			public Integer get() { return shape.getSurfaceRender(); }
		    @Override
			public void set(Integer v) { shape.setSurfaceRender( v ); }
		};
		timeline.addKeyframe(shapeName + "_surface_render_type", pSurfaceRenderType, Interpolator.integerStep, easing, keyFrameScene );			

		
		//surface grid type
    	final Property<Integer> pSurfaceGrid = new Property<Integer>() {
		    @Override
			public Integer get() { return shape.getSurfaceGrid(); }
		    @Override
			public void set(Integer v) { shape.setSurfaceGrid( v ); }
		};
		timeline.addKeyframe(shapeName + "_surface_grid", pSurfaceGrid, Interpolator.integerStep, easing, keyFrameScene );			

		// wire width 
    	final Property<Float> pWireWidth = new Property<Float>() {
		    @Override
			public Float get() { return shape.getWireLineWidth(); }
		    @Override
			public void set(Float v) { shape.setWireLineWidth( v );}
		};
		timeline.addKeyframe(shapeName + "_wire_width", pWireWidth, Interpolator.floatLerp, easing, keyFrameScene );			

		// wire width 
    	final Property<Float> pSilhouetteDecay = new Property<Float>() {
		    @Override
			public Float get() { return shape.getSilhouetteDecay(); }
		    @Override
			public void set(Float v) { shape.setSilhouetteDecay( v );}
		};
		timeline.addKeyframe(shapeName + "_sil_decay", pSilhouetteDecay, Interpolator.floatLerp, easing, keyFrameScene );			

		
    }
}
