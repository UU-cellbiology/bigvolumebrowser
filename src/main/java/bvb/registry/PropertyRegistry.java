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
package bvb.registry;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.type.numeric.ARGBType;

import bdv.viewer.SourceAndConverter;
import bvb.animation.utils.HSBColorInterpolator;
import bvb.animation.utils.Interpolator;
import bvb.animation.utils.Property;
import bvb.animation.utils.PropertyBinding;
import bvb.core.BigVolumeBrowser;
import bvb.gui.shapes.SpotsMapSetups;
import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvb.shapes.MultiMeshShape;
import bvb.utils.Bounds3D;
import bvb.utils.clip.ClipSetups;
import bvb.utils.transform.TransformSetups;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.source.converters.Clippable3D;
import bvvpg.source.converters.GammaConverterSetup;

public class PropertyRegistry
{
	ObjectHashStorage objectHashStorage;
	
	ClipSetups clipSetups;
	
	TransformSetups transformSetups;
	
	VolumeViewerPanel bvvViewer;
	
	SpotsMapSetups spotsMapSetups;
	
	SpotsMapSetups spotsAlphaSetup;
	
	public PropertyRegistry()
	{	
	}
	
	public void bindBVB(final BigVolumeBrowser bvb)
	{
    	objectHashStorage = bvb.objectHashStorage;

    	clipSetups = bvb.bvbCards.clipPanel.clipSetups;
    	transformSetups = bvb.bvbCards.transformPanel.transformSetups;
    	bvvViewer = bvb.bvvViewer;
    	
    	spotsMapSetups = bvb.bvbCards.panelShapesProperties.panelSpotsProperties.colorCodePanel.spotsLUTSetup;
    	spotsAlphaSetup = bvb.bvbCards.panelShapesProperties.panelSpotsProperties.opacityPanel.spotsAlphaSetup;	
	}
	
	public static List<String> getPropertyNames(final Object obj)
	{
		final List<String> properties = new ArrayList<>();
		if(obj instanceof Clippable3D )
    	{
			properties.add( "transform_center" );
			properties.add( "transform_rotation" );
			properties.add( "transform_scale" );
			properties.add( "transform_deskew" );
			properties.add( "clip_type" );
			properties.add( "clip_range" );
			properties.add( "clip_center" );
			properties.add( "clip_rotation" );
    	}
		if(obj instanceof GammaConverterSetup)
		{
			properties.add( "cs_visibility" );
			properties.add( "cs_displayRange" );
			properties.add( "cs_gamma" );
			properties.add( "cs_alphaRange" );
			properties.add( "cs_alphaGamma" );
			properties.add( "cs_renderType" );
			properties.add( "cs_lightingType" );
			properties.add( "cs_voxelInterpolation" );
			properties.add( "cs_color" );
			properties.add( "cs_LUT" );
			
		}
		
		if(obj instanceof BasicShape)
		{
			properties.add( "bs_visible" );
			if(obj instanceof BasicMeshShape)
			{
				final BasicMeshShape mesh = (BasicMeshShape) obj;
				if(!(mesh instanceof MultiMeshShape))
				{
					properties.add( "mesh_color" );
				}
				if(mesh.hasTexture())
				{
					properties.add( "mesh_useTexture" );					
				}
				properties.add( "mesh_renderType" );
				properties.add( "mesh_pointSize" );
				properties.add( "mesh_surfaceRenderType" );
				properties.add( "mesh_surfaceGrid" );
				properties.add( "mesh_wireWidth" );
				properties.add( "mesh_silDecay" );
			}
			if(obj instanceof BasicSpots)
			{
				final BasicSpots spots = (BasicSpots) obj;
				// if point size is the same
		    	if(spots.getPointSize() >= 0.0)
		    	{
		    		// point size
		    		properties.add( "spots_pointSize" );
		    	}
		    	// each point has its own size
		    	else
		    	{
		    		properties.add( "spots_pointSizeScale" );
		    	}
		    	if(!spots.isMultiColor())
		    	{
		    		//spots color 
		    		properties.add( "spots_color" );
		    	}
		    	// extra transparency
		    	properties.add( "spots_extraAlpha" );
		    	// renderType
		    	properties.add( "spots_renderType" );
		    	// spots shape
		    	properties.add( "spots_shape" );
		    	// spots shading
		    	properties.add( "spots_shade" );
		    	// LUT mapping mode
		    	properties.add( "spots_mapLUTMode" );
		    	// LUT
		    	properties.add( "spots_LUT" );
		    	// LUT inversion
		    	properties.add( "spots_LUTInverse" );
		    	// LUT Range
		    	properties.add( "spots_LUTRange" );
		    	// alpha mapping mode
		    	properties.add( "spots_alphaMapMode" );
		    	// alpha map inversion
		    	properties.add( "spots_alphaInverse" );
		    	// LUT Range
		    	properties.add( "spots_alphaRange" );
		    	
			}
		}
		return properties;
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
		
		if (propertyName.equals( "cs_color" ))
		{
	    	final Property<Color> pColor = new Property<Color>() {
			    @Override
				public Color get() { return new Color(cs.getColor().get(),true ); }
			    @Override
				public void set(Color v) { cs.setColor( new ARGBType(v.getRGB()) ); }
			};
			final PropertyBinding<Color> binding = new PropertyBinding<>();
			binding.property = pColor;
			binding.interpolator = new HSBColorInterpolator();
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "cs_LUT" ))
		{
	    	final Property<String> pLUT = new Property<String>() {
			    @Override
				public String get() { 
			    	String lut = cs.getLUTName(); 
			    	if(lut == null)
			    		lut = "";
			    	return lut;
					}
			    @Override
				public void set(String v) { if(!v.equals( "" )) {cs.setLUT( v );}}
			};
			final PropertyBinding<String> binding = new PropertyBinding<>();
			binding.property = pLUT;
			binding.interpolator = Interpolator.stringStep;
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
		
		if (propertyName.equals( "spots_mapLUTMode" ))
		{
	    	final Property<Integer> pLUTMapMode = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getMapLUTMode(); }
			    @Override
				public void set(Integer v) { shape.setMapLUTMode( v );  }
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pLUTMapMode;
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
		
		if (propertyName.equals( "spots_LUTInverse" ))
		{
	    	final Property<Boolean> pLUTInv = new Property<Boolean>() {
			    @Override
				public Boolean get() { return shape.isInvertedLUT(); }
			    @Override
				public void set(Boolean v) { shape.setInvertedLUT( v );  }
			};
			final PropertyBinding<Boolean> binding = new PropertyBinding<>();
			binding.property = pLUTInv;
			binding.interpolator = Interpolator.booleanStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_LUTRange" ))
		{
	    	final Property<float[][]> pLUTRange = new Property<float[][]>() {
			    @Override
				public float[][] get() { 
			    	final float [][] range = spotsMapSetups.getMapAllFloat( shape );
			    	final float [][] copy = Arrays.stream(range).map(float[]::clone).toArray(float[][]::new);
			    	return copy; }
			    @Override
				public void set(float[][] v) { 
			    	final int nMode = shape.getMapLUTMode() - 1;
			    	if(nMode >= 0)
			    	{
				    	final float [][] copy = Arrays.stream(v).map(float[]::clone).toArray(float[][]::new);

			    		spotsMapSetups.setRanges( shape, copy );
			    		shape.setMapLUTRange( v[nMode][0], v[nMode][1] ); 
			    		shape.setMapLUTGamma( v[nMode][4] );
			    	}
			    	}
			};
			final PropertyBinding<float[][]> binding = new PropertyBinding<>();
			binding.property = pLUTRange;
			binding.interpolator = Interpolator.floatIndexArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_alphaMapMode" ))
		{
	    	final Property<Integer> pAlphaMapMode = new Property<Integer>() {
			    @Override
				public Integer get() { return shape.getMapAlphaMode(); }
			    @Override
				public void set(Integer v) { shape.setMapAlphaMode( v );}
			};
			final PropertyBinding<Integer> binding = new PropertyBinding<>();
			binding.property = pAlphaMapMode;
			binding.interpolator = Interpolator.integerStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_alphaInverse" ))
		{
	    	final Property<Boolean> pAlphaInv = new Property<Boolean>() {
			    @Override
				public Boolean get() { return shape.isInvertedAlpha(); }
			    @Override
				public void set(Boolean v) { shape.setInvertedAlpha( v );  }
			};
			final PropertyBinding<Boolean> binding = new PropertyBinding<>();
			binding.property = pAlphaInv;
			binding.interpolator = Interpolator.booleanStep;
			return ( PropertyBinding< T > ) binding;
		}
		
		if (propertyName.equals( "spots_alphaRange" ))
		{
	    	final Property<float[][]> pLUTRange = new Property<float[][]>() {
			    @Override
				public float[][] get() { 
			    	final float [][] range = spotsAlphaSetup.getMapAllFloat( shape );
			    	final float [][] copy = Arrays.stream(range).map(float[]::clone).toArray(float[][]::new);
			    	return copy; }
			    @Override
				public void set(float[][] v) { 
			    	final int nMode = shape.getMapAlphaMode() - 1;
			    	if(nMode >= 0)
			    	{
				    	final float [][] copy = Arrays.stream(v).map(float[]::clone).toArray(float[][]::new);

				    	spotsAlphaSetup.setRanges( shape, copy );
			    		shape.setMapAlphaRange( v[nMode][0], v[nMode][1] ); 
			    		shape.setMapAlphaGamma( v[nMode][4] );
			    	}
			    	}
			};
			final PropertyBinding<float[][]> binding = new PropertyBinding<>();
			binding.property = pLUTRange;
			binding.interpolator = Interpolator.floatIndexArrayLerp;
			return ( PropertyBinding< T > ) binding;
		}
		
		return null;
	}
}
