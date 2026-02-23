package bvb.animation.utils;

import java.awt.Color;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.io.ObjectHashStorage;
import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvb.utils.transform.TransformSetups;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.source.converters.Clippable3D;
import bvvpg.source.converters.GammaConverterSetup;

public class PropertyRegistry
{
	final ObjectHashStorage objectHashStorage;
	
	final ClipSetups clipSetups;
	
	final TransformSetups transformSetups;
	
	final VolumeViewerPanel bvvViewer;
	
	public PropertyRegistry(final BigVolumeBrowser bvb)
	{
    	clipSetups = bvb.bvbCards.clipPanel.clipSetups;
    	transformSetups = bvb.bvbCards.transformPanel.transformSetups;
    	bvvViewer = bvb.bvvViewer;
    	objectHashStorage = bvb.objectHashStorage;
	
	}
	
	public <T> PropertyBinding<T> get(String objectId, String propertyName) 
	{
		final Object obj = objectHashStorage.getObjectFromHash( objectId );
		
		if(obj instanceof Clippable3D )
		{
			Clippable3D clippable = (Clippable3D) obj;
			final PropertyBinding<T> binding = getPropertyTransformAndClippable(clippable, propertyName);
			if(binding != null)
			return binding;
		}
		
		if(obj instanceof GammaConverterSetup )
		{
			GammaConverterSetup cs = (GammaConverterSetup) obj;
			final PropertyBinding<T> binding = getPropertyCS(cs, propertyName);
			if(binding != null)
			return binding;
		}
		
		if(obj instanceof BasicShape )
		{
			BasicShape shape = (BasicShape) obj;
			final PropertyBinding<T> binding = getPropertyBasicShape(shape, propertyName);
			if(binding != null)
			return binding;
		}
		
        return null;
    }
	@SuppressWarnings( "unchecked" )
	public <T> PropertyBinding<T> getPropertyTransformAndClippable(final Clippable3D obj, String propertyName) 
	{
		
		if (propertyName.equals( "transform_center" ))
		{
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
			final PropertyBinding<double []> binding = new PropertyBinding<>();
			binding.property = pTrCenter;
			binding.interpolator = Interpolator.doubleArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "transform_rotation" ))
		{
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
			final PropertyBinding<double []> binding = new PropertyBinding<>();
			binding.property = pTrRotation;
			binding.interpolator = Interpolator.quatSLerp;
			return ( PropertyBinding< T > ) binding;
		}
		if (propertyName.equals( "transform_scale" ))
		{
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
			final PropertyBinding<double []> binding = new PropertyBinding<>();
			binding.property = pTrScale;
			binding.interpolator = Interpolator.doubleArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "transform_deskew" ))
		{
	    	final Property<Double> pTrDeskew = new Property<Double>() {
			    @Override
				public Double get() { return transformSetups.transformDeskew.getAngle( obj ); }
			    @Override
				public void set(Double v) { 
			    	transformSetups.transformDeskew.setAngle( obj, v ); 
			    	transformSetups.updateTransform( obj, null, false );}
			};
			final PropertyBinding<Double> binding = new PropertyBinding<>();
			binding.property = pTrDeskew;
			binding.interpolator = Interpolator.doubleLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "clip_type" ))
		{
	    	final Property<Integer> pClipType = new Property<Integer>() {
			    @Override
				public Integer get() { return obj.getClipState(); }
			    @Override
				public void set(Integer v) { obj.setClipState( v );}
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pClipType;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "clip_range" ))
		{
	    	final Property<RealInterval> pClipRange = new Property<RealInterval>() {
			    @Override
				public RealInterval get() { 
				    	RealInterval interval = obj.getClipInterval();
				    	//not sure this is the best, but let's keep it for now
				    	if(interval != null)
				    	{
				    		return interval;
				    	}
				    	final Bounds3D bounds = new Bounds3D(clipSetups.clipRangeBounds.getBounds( obj ));
				    	return new FinalRealInterval(bounds.getMinBound(),bounds.getMaxBound(), true);
			    	}
			    @Override
				public void set(RealInterval v) { 
			    	if(obj.getClipState() > 0 && v != null) 
			    	{ obj.setClipInterval( v );} }
			};
			final PropertyBinding<RealInterval> binding = new PropertyBinding<>();
			binding.property = pClipRange;
			binding.interpolator = Interpolator.realInterval;
			return ( PropertyBinding< T > ) binding;
		}
		if (propertyName.equals( "clip_center" ))
		{
	    	final Property<double []> pClipCenter = new Property<double []>() {
			    @Override
				public double [] get() { 
			    	{
			    		double [] out = new double [3];
			    		System.arraycopy( clipSetups.clipCenters.getCenters( obj ), 0, out, 0, 3 );
			    		return out;
			    	}}
			    @Override
				public void set(double [] v) { 
			    	clipSetups.clipCenters.setCenters( obj, v ); 
			    	clipSetups.updateClipTransform( obj, null );}
			};
			final PropertyBinding<double []> binding = new PropertyBinding<>();
			binding.property = pClipCenter;
			binding.interpolator = Interpolator.doubleArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "clip_rotation" ))
		{
	    	final Property<double []> pClipRotation = new Property<double []>() {
			    @Override
				public double [] get() { 
			    	{
			    		double [] out = new double [4];
			    		System.arraycopy( clipSetups.clipRotation.getQuaternion( obj ) , 0, out, 0, 4 );
			    		return out;
			    	}}
			    @Override
				public void set(double [] v) { clipSetups.clipRotation.setQuaternion( obj, v ); clipSetups.updateClipTransform( obj, null );}
			};
			final PropertyBinding<double []> binding = new PropertyBinding<>();
			binding.property = pClipRotation;
			binding.interpolator = Interpolator.quatSLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		return null;
	}
	
	@SuppressWarnings( "unchecked" )
	public <T> PropertyBinding<T> getPropertyCS(final GammaConverterSetup cs, String propertyName) 
	{
		
		if (propertyName.equals( "cs_visibility" ))
		{
	    	final SourceAndConverter< ? > sac = transformSetups.converterSetups.getSource( cs );
	    	
	    	//visibility
	    	final Property<Boolean> pDisplayVisible = new Property<Boolean>() {
			    @Override
				public Boolean get() { return bvvViewer.state().isSourceVisible(sac);  }
			    @Override
				public void set(Boolean v) { bvvViewer.state().setSourceActive( sac, v);}
			};  
			final PropertyBinding<Boolean> binding = new PropertyBinding<>();
			binding.property = pDisplayVisible;
			binding.interpolator = Interpolator.booleanStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_displayRange" ))
		{
	    	final Property<double[]> pDisplayRange = new Property<double[]>() {
			    @Override
				public double[] get() { return new double[] {cs.getDisplayRangeMin(), cs.getDisplayRangeMax()}; }
			    @Override
				public void set(double[] v) { cs.setDisplayRange( v[0], v[1] ); }
			}; 
			final PropertyBinding<double[]> binding = new PropertyBinding<>();
			binding.property = pDisplayRange;
			binding.interpolator = Interpolator.doubleArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_gamma" ))
		{
	    	final Property<Double> pAlpha = new Property<Double>() {
			    @Override
				public Double get() { return cs.getDisplayGamma(); }
			    @Override
				public void set(Double v) { cs.setDisplayGamma( v ); }
			};
			final PropertyBinding<Double> binding = new PropertyBinding<>();
			binding.property = pAlpha;
			binding.interpolator = Interpolator.doubleLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_alphaRange" ))
		{
	    	final Property<double[]> pAlphaRange = new Property<double[]>() {
			    @Override
				public double[] get() { return new double[] {cs.getAlphaRangeMin(), cs.getAlphaRangeMax()}; }
			    @Override
				public void set(double[] v) { cs.setAlphaRange( v[0], v[1] ); }
			};   
			final PropertyBinding<double[]> binding = new PropertyBinding<>();
			binding.property = pAlphaRange;
			binding.interpolator = Interpolator.doubleArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_alphaGamma" ))
		{
	    	final Property<Double> pAlphaGamma = new Property<Double>() {
			    @Override
				public Double get() { return cs.getAlphaGamma(); }
			    @Override
				public void set(Double v) { cs.setAlphaGamma( v );}
			};
			final PropertyBinding<Double> binding = new PropertyBinding<>();
			binding.property = pAlphaGamma;
			binding.interpolator = Interpolator.doubleLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_renderType" ))
		{
	    	final Property<Integer> pRenderType = new Property<Integer>() {
			    @Override
				public Integer get() { return cs.getRenderType(); }
			    @Override
				public void set(Integer v) { cs.setRenderType( v );}
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pRenderType;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_lightingType" ))
		{
	    	final Property<Integer> pRenderLight = new Property<Integer>() {
			    @Override
				public Integer get() { return cs.getLightingType(); }
			    @Override
				public void set(Integer v) { cs.setLightingType( v );}
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pRenderLight;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_voxelInterpolation" ))
		{
	    	final Property<Integer> pVoxelInterpolation = new Property<Integer>() {
			    @Override
				public Integer get() { return cs.getVoxelRenderInterpolation(); }
			    @Override
				public void set(Integer v) { cs.setVoxelRenderInterpolation( v ); }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pVoxelInterpolation;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		return null;
	}
	
	@SuppressWarnings( "unchecked" )
	public <T> PropertyBinding<T> getPropertyBasicShape(final BasicShape shape, String propertyName) 
	{
		if (propertyName.equals( "bs_visible" ))
		{
	    	final Property<Boolean> pVisible = new Property<Boolean>() {
			    @Override
				public Boolean get() { return shape.isVisible(); }
			    @Override
				public void set(Boolean v) { shape.setVisible( v ); }
			};
			final PropertyBinding<Boolean> binding = new PropertyBinding<>();
			binding.property = pVisible;
			binding.interpolator = Interpolator.booleanStep;
			return ( PropertyBinding< T > ) binding;
		}
		if(shape instanceof BasicMeshShape)
		{
			return getPropertyBasicMeshShape((BasicMeshShape) shape, propertyName); 
		}
		
		if(shape instanceof BasicSpots)
		{
			return getPropertyBasicSpots((BasicSpots) shape, propertyName); 
		}
		return null;
	}
	
	@SuppressWarnings( "unchecked" )
	public <T> PropertyBinding<T> getPropertyBasicMeshShape(final BasicMeshShape shape, String propertyName) 
	{
		if (propertyName.equals( "mesh_color" ))
		{
	    	final Property<Color> pColor = new Property<Color>() {
			    @Override
				public Color get() { return shape.getColor(); }
			    @Override
				public void set(Color v) { shape.setColor( v ); }
			};
			final PropertyBinding<Color> binding = new PropertyBinding<>();
			binding.property = pColor;
			binding.interpolator = new HSBColorInterpolator();
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_useTexture" ))
		{
	    	final Property<Boolean> pTexture = new Property<Boolean>() {
			    @Override
				public Boolean get() { return shape.isTextureUsed(); }
			    @Override
				public void set(Boolean v) { shape.useTexture( v ); }
			};
			final PropertyBinding<Boolean> binding = new PropertyBinding<>();
			binding.property = pTexture;
			binding.interpolator = Interpolator.booleanStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_renderType" ))
		{
	    	final Property<Integer> pRenderType = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getRenderType(); }
			    @Override
				public void set(Integer v) { shape.setRenderType( v ); }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pRenderType;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_pointSize" ))
		{
	    	final Property<Float> pPointSize = new Property<Float>() {
			    @Override
				public Float get() { return shape.getPointSize(); }
			    @Override
				public void set(Float v) { shape.setPointSize( v );}
			};
			final PropertyBinding<Float> binding = new PropertyBinding<>();
			binding.property = pPointSize;
			binding.interpolator = Interpolator.floatLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_surfaceRenderType" ))
		{
	    	final Property<Integer> pSurfaceRenderType = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getSurfaceRender(); }
			    @Override
				public void set(Integer v) { shape.setSurfaceRender( v ); }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pSurfaceRenderType;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_surfaceGrid" ))
		{
	    	final Property<Integer> pSurfaceGrid = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getSurfaceGrid(); }
			    @Override
				public void set(Integer v) { shape.setSurfaceGrid( v ); }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pSurfaceGrid;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_wireWidth" ))
		{
	    	final Property<Float> pWireWidth = new Property<Float>() {
			    @Override
				public Float get() { return shape.getWireLineWidth(); }
			    @Override
				public void set(Float v) { shape.setWireLineWidth( v );}
			};
			final PropertyBinding<Float> binding = new PropertyBinding<>();
			binding.property = pWireWidth;
			binding.interpolator = Interpolator.floatLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "mesh_silDecay" ))
		{
	    	final Property<Float> pSilhouetteDecay = new Property<Float>() {
			    @Override
				public Float get() { return shape.getSilhouetteDecay(); }
			    @Override
				public void set(Float v) { shape.setSilhouetteDecay( v );}
			};
			final PropertyBinding<Float> binding = new PropertyBinding<>();
			binding.property = pSilhouetteDecay;
			binding.interpolator = Interpolator.floatLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		return null;
	}
	
	@SuppressWarnings( "unchecked" )
	public <T> PropertyBinding<T> getPropertyBasicSpots(final BasicSpots shape, String propertyName) 
	{
		if (propertyName.equals( "spots_pointSize" ))
		{
        	final Property<Float> pPointSize = new Property<Float>() {
    		    @Override
    			public Float get() { return shape.getPointSize(); }
    		    @Override
    			public void set(Float v) { shape.setPointSize( v ); }
    		};
			final PropertyBinding<Float> binding = new PropertyBinding<>();
			binding.property = pPointSize;
			binding.interpolator = Interpolator.floatLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_pointSizeScale" ))
		{
        	final Property<Float> pPointScale = new Property<Float>() {
    		    @Override
    			public Float get() { return shape.getSizeScale(); }
    		    @Override
    			public void set(Float v) { shape.setSizeScale( v );  }
    		};
			final PropertyBinding<Float> binding = new PropertyBinding<>();
			binding.property = pPointScale;
			binding.interpolator = Interpolator.floatLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_color" ))
		{
        	final Property<Color> pSpotColor = new Property<Color>() {
    		    @Override
    			public Color get() { return shape.getColor(); }
    		    @Override
    			public void set(Color v) { shape.setColor( v ); }
    		};
			final PropertyBinding<Color> binding = new PropertyBinding<>();
			binding.property = pSpotColor;
			binding.interpolator = new HSBColorInterpolator();
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_extraAlpha" ))
		{
	    	final Property<Float> pExtraAlpha = new Property<Float>() {
			    @Override
				public Float get() { return shape.getExtraAlphaCoefficient(); }
			    @Override
				public void set(Float v) { shape.setExtraAlphaCoefficient( v );  }
			};
			final PropertyBinding<Float> binding = new PropertyBinding<>();
			binding.property = pExtraAlpha;
			binding.interpolator = Interpolator.floatLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_renderType" ))
		{
	    	final Property<Integer> pRenderType = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getRenderType(); }
			    @Override
				public void set(Integer v) { shape.setRenderType( v );  }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pRenderType;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_shape" ))
		{
	    	final Property<Integer> pShape = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getPointShape(); }
			    @Override
				public void set(Integer v) { shape.setPointShape( v );  }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pShape;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_shade" ))
		{
	    	final Property<Integer> pShade = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getPointShade(); }
			    @Override
				public void set(Integer v) { shape.setPointShade( v );  }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pShade;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_LUT" ))
		{
	    	final Property<String> pLUT = new Property<String>() {
			    @Override
				public String get() { return shape.getLUTName(); }
			    @Override
				public void set(String v) { shape.setLUT( v );  }
			};
			final PropertyBinding<String> binding = new PropertyBinding<>();
			binding.property = pLUT;
			binding.interpolator = Interpolator.stringStep;
			return ( PropertyBinding< T > ) binding;
		}
		return null;
	}
}
