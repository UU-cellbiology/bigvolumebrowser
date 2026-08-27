package bvb.scene;

import bvb.core.BVBSettings;
import bvb.scene.shader.SegmentTypeComposite;
import bvb.scene.shader.SegmentTypeStatic;
import bvb.scene.shader.SegmentsLibrary;
import bvb.scene.shader.SpotsSegmentLibrary;
import bvb.scene.shader.SpotsSegmentType;
import bvb.shapes.BasicShape.AlphaType;
import bvvpg.core.shadergen.Shader;
import bvvpg.core.shadergen.generate.Segment;
import bvvpg.core.shadergen.generate.SegmentTemplate;
import bvvpg.core.shadergen.generate.SegmentedShaderBuilder;

public class ShaderSpots
{
	public static Shader buildSpotsShader(final VisSpots visSpots)
	{
		final SegmentedShaderBuilder builder = new SegmentedShaderBuilder();
		//vertex
		final Segment spVertex = SegmentsLibrary.compositeSegments.get( SegmentTypeComposite.VertexSpots ).instantiate();
		if(visSpots.getSize() < 0.0)
		{
			spVertex.insert( "spotsScaling", SegmentTemplate.fromCode("    fDiamfp = fDiam * fSizeScale;").instantiate());
		}
		else
		{
			spVertex.insert( "spotsScaling", SegmentTemplate.fromCode("    fDiamfp = pointSizeReal;").instantiate());
		}
		builder.vertex( spVertex );
		
		//fragment
		final Segment pointFp = SegmentsLibrary.compositeSegments
								.get( SegmentTypeComposite.FragmentSpots ).instantiate();
		//clipping
		if(visSpots.clipState != 0 && visSpots.clipInt != null)
		{
			pointFp.insert( "preClip", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.preClip) );
			pointFp.insert( "mClip", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.mClip ) );			
		}
		else
		{
			pointFp.insert( "preClip", SegmentsLibrary.emptySeg );
			pointFp.insert( "mClip", SegmentsLibrary.emptySeg );
		}
		final int nMapLUTMode = visSpots.getMapLUTMode();
		//spots color
		if(nMapLUTMode > 0)
		{
			//add variables
			pointFp.insert( "preColorLUT", (Segment)SpotsSegmentLibrary.spotSegments.
					get( SpotsSegmentType.preColorLUT) );
			final Segment colorLutMode = ((SegmentTemplate)SpotsSegmentLibrary.spotSegments
					.get( SpotsSegmentType.SpotsColorLUTMode )).instantiate();
			
			//axis mapping
			if(nMapLUTMode < 4)
			{
				colorLutMode.insert( "shaderMapLUTMode", (Segment)SpotsSegmentLibrary.spotSegments.
						get( SpotsSegmentType.LutAxis) );
			}
			else
			{
				if(nMapLUTMode == 4)
				{
					colorLutMode.insert( "shaderMapLUTMode", 
					 SegmentTemplate.fromCode("    val = fDiamfp;").instantiate()  );
				}
				if(nMapLUTMode == 5)
				{
					colorLutMode.insert( "shaderMapLUTMode", 
							SegmentTemplate.fromCode("        val = fPropertyfp;").instantiate()  );
				}
			}
			if(visSpots.bInvertLUT)
			{
				colorLutMode.insert( "invertColorLUT", 
  				  SegmentTemplate.fromCode("    val = 1.0 - val;").instantiate() );
			}
			else
			{
				colorLutMode.insert( "invertColorLUT", SegmentsLibrary.emptySeg );
			}
			pointFp.insert("spotsColor", colorLutMode);
		}
		else
		{
			pointFp.insert( "preColorLUT", SegmentsLibrary.emptySeg );
			if(visSpots.hasColors())
			{
				pointFp.insert("spotsColor",
						SegmentTemplate.fromCode("    vec4 colorout = fColorsfp;").instantiate());
			}
			else
			{
				pointFp.insert("spotsColor", 
						SegmentTemplate.fromCode("    vec4 colorout = colorin;").instantiate());

			}
		}
		final int nMapAlphaMode = visSpots.getMapAlphaMode();
		//spots alpha
		if(nMapAlphaMode > 0)
		{
			pointFp.insert( "preAlphaMap", (Segment)SpotsSegmentLibrary.spotSegments.
					get( SpotsSegmentType.preAlphaMap) );
			final Segment alphaMapMode = ((SegmentTemplate)SpotsSegmentLibrary.spotSegments
					.get( SpotsSegmentType.SpotsAlphaMapMode )).instantiate();
			
			//axis mapping
			if(nMapAlphaMode < 4)
			{
				alphaMapMode.insert( "alphaMapMode", (Segment)SpotsSegmentLibrary.spotSegments.
						get( SpotsSegmentType.alphaAxis) );
			}
			else
			{
				if(nMapAlphaMode == 4)
				{
					alphaMapMode.insert( "alphaMapMode", 
					 SegmentTemplate.fromCode("    fAlpha = fDiamfp;").instantiate()  );
				}
				if(nMapAlphaMode == 5)
				{
					alphaMapMode.insert( "alphaMapMode", 
							SegmentTemplate.fromCode("        fAlpha = fPropertyfp;").instantiate()  );
				}
			}
			
			if(visSpots.bInvertAlpha)
			{
				alphaMapMode.insert( "invertAlphaMap", 
  				  SegmentTemplate.fromCode("    fAlpha = 1.0 - fAlpha;").instantiate() );
			}
			else
			{
				alphaMapMode.insert( "invertAlphaMap", SegmentsLibrary.emptySeg );
			}
			pointFp.insert("spotsAlpha", alphaMapMode);
		}
		else
		{
			pointFp.insert("preAlphaMap",  SegmentsLibrary.emptySeg );
			pointFp.insert("spotsAlpha",  SegmentsLibrary.emptySeg );
		}
		//spots shape
		//
		final int renderType = visSpots.getRenderType();
		if(visSpots.getShape() == VisSpots.SHAPE_ROUND)
		{
			final Segment roundShape;
			if(BVBSettings.bMultiSampleSpots)
			{
				roundShape = ((SegmentTemplate)SpotsSegmentLibrary.spotSegments
						.get( SpotsSegmentType.SpotsRoundMSAA )).instantiate();
			}
			else
			{
				roundShape = ((SegmentTemplate)SpotsSegmentLibrary.spotSegments
						.get( SpotsSegmentType.SpotsRound )).instantiate();
			}
			
			final int spotShade = visSpots.getShade();

			switch(renderType)
			{
			case VisSpots.RENDER_FILLED:	
				if(spotShade != VisSpots.SHADE_PLANE)
				{
					final Segment roundDepth = ((SegmentTemplate)SpotsSegmentLibrary.spotSegments
							.get( SpotsSegmentType.SpotsRoundDepth )).instantiate();
					if(spotShade == VisSpots.SHADE_INDIVIDUAL)
					{
						roundDepth.insert( "spotsRoundShade", (Segment)SpotsSegmentLibrary.spotSegments.
								get( SpotsSegmentType.SpotsRoundShaded) );
					}
					else
					{
						roundDepth.insert( "spotsRoundShade", SegmentsLibrary.emptySeg );
					}
					roundShape.insert( "roundRenderType", roundDepth );	
				}
				else
				{
					roundShape.insert( "roundRenderType", SegmentsLibrary.emptySeg  );
				}				
				break;
			case VisSpots.RENDER_OUTLINE:
				if(BVBSettings.bMultiSampleSpots)
				{
					roundShape.insert( "roundRenderType", (Segment)SpotsSegmentLibrary.spotSegments.
							get( SpotsSegmentType.SpotsRoundOutlineMSAA));
				}
				else
				{
					roundShape.insert( "roundRenderType", (Segment)SpotsSegmentLibrary.spotSegments.
							get( SpotsSegmentType.SpotsRoundOutline));					
				}
				break;				
			case VisSpots.RENDER_GAUSS:
				roundShape.insert( "roundRenderType", (Segment)SpotsSegmentLibrary.spotSegments.
						get( SpotsSegmentType.SpotsRoundGauss));
				break;			
			}		
			pointFp.insert( "spotsShape", roundShape );

		}//square shape
		else
		{
			switch(renderType)
			{
			case VisSpots.RENDER_FILLED:
				pointFp.insert( "spotsShape", SegmentsLibrary.emptySeg );
				break;
			case VisSpots.RENDER_OUTLINE:
				pointFp.insert( "spotsShape", (Segment)SpotsSegmentLibrary.spotSegments.
						get( SpotsSegmentType.SpotsSquareOutline ));
				break;				
			case VisSpots.RENDER_GAUSS:
				pointFp.insert( "spotsShape", (Segment)SpotsSegmentLibrary.spotSegments.
						get( SpotsSegmentType.SpotsSquareGauss ));
				break;			
			}
			
		}
		
		//weighted OIT
		if(BVBSettings.transparentAlpha == AlphaType.OIT)
		{
			pointFp.insert( "preOIT", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.preOIT ) );
			pointFp.insert( "wOIT", SegmentsLibrary.staticSegments.get( SegmentTypeStatic.wOIT ) );
		}
		else
		{
			pointFp.insert( "preOIT", SegmentsLibrary.emptySeg );
			pointFp.insert( "wOIT", SegmentsLibrary.emptySeg );
		}
		
		builder.fragment( pointFp );

//		final StringBuilder vertexShaderCode = prog.getVertexShaderCode();
//		System.out.println( "vertexShaderCode  = \n" + vertexShaderCode  );
//		System.out.println( "\n\n--------------------------------\n\n" );
//
//		final StringBuilder fragmentShaderCode = prog.getFragmentShaderCode();
//		System.out.println( "fragmentShaderCode = \n" + fragmentShaderCode );
//		System.out.println( "\n\n--------------------------------\n\n" );
		return builder.build();
	}
}
