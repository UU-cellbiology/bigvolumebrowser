package bvb.core;

import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_RGBA8;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import bvb.shapes.BasicMeshShape;
import bvb.shapes.BasicShape;
import bvb.shapes.BasicSpots;
import bvvpg.core.offscreen.FlexibleFBO;
import bvvpg.core.offscreen.OffScreenFrameBufferWithDepth;
import bvvpg.core.render.RenderData;
import bvvpg.core.util.MatrixMath;


public class RenderSorter
{
	final BigVolumeBrowser bvb;

	final ArrayList<BasicShape> shOpaque = new ArrayList<>();
	final ArrayList<BasicShape> shTransparent = new ArrayList<>();
	
	final ArrayList<BasicShape> shOpaqueMSAA = new ArrayList<>();
	final ArrayList<BasicShape> shOpaqueRegular = new ArrayList<>();
	final ArrayList<BasicShape> shOpaqueEDL = new ArrayList<>();
	final ArrayList<BasicShape> shTransparentMSAA = new ArrayList<>();
	final ArrayList<BasicShape> shTransparentRegular = new ArrayList<>();
	
	FlexibleFBO flexibleFBO;
	
	int [] screen_size;
	Matrix4f pvm;
	Matrix4f view;
	Matrix4f vm; 
	int nTimePoint;
	
	public RenderSorter(final BigVolumeBrowser bvb_) 
	{
		bvb = bvb_;
	}
	
	public void initBuffer()
	{
		flexibleFBO = new FlexibleFBO(BVVSettings.renderWidth, BVVSettings.renderHeight, GL_RGBA8, false); 
	}
	
	public void drawOpaque(final GL3 gl, final RenderData data)
	{
		initDrawState(gl, data);
		sort();
		if(!shOpaqueRegular.isEmpty())
		{
			for(final BasicShape sh : shOpaqueRegular)
			{
				sh.draw( gl, pvm, pvm, screen_size, nTimePoint, false );
			}
		}
		if(!shOpaqueMSAA.isEmpty())
		{
			flexibleFBO.setMSAAEnabled( true );
			flexibleFBO.bind( gl );

			for(final BasicShape sh : shOpaqueMSAA)
			{
				sh.draw( gl, pvm, pvm, screen_size, nTimePoint, false );
			}			
			flexibleFBO.unbind( gl, false );
			flexibleFBO.drawQuadColorDepth( gl );
		}
		
		if(!shOpaqueEDL.isEmpty())
		{
			flexibleFBO.setMSAAEnabled( BVBSettings.bMultiSampleSpots );
			flexibleFBO.bind( gl );

			for(final BasicShape sh : shOpaqueEDL)
			{
				sh.draw( gl, pvm, pvm, screen_size, nTimePoint, false );
			}			
			flexibleFBO.unbind( gl, false );
			flexibleFBO.drawQuadEDL( gl, BVVSettings.fnratio, BVBSettings.fEDLRadius, BVBSettings.fEDLStrength );
		}
	}
	
	public void drawTransparent(final GL3 gl, final OffScreenFrameBufferWithDepth sceneVolBuffer)
	{
		flexibleFBO.setMSAAEnabled( false );	
		drawTransparentShapes(gl, sceneVolBuffer, shTransparentRegular, true, false );
		
		if(!shTransparentMSAA.isEmpty())
		{
			flexibleFBO.setMSAAEnabled( true );	
			drawTransparentShapes(gl, sceneVolBuffer, shTransparentMSAA, false, true );
		}
	}
	
	void drawTransparentShapes(final GL3 gl, 
			 				   final OffScreenFrameBufferWithDepth sceneVolBuffer, 
			 				   final List<BasicShape> shapes,
			 				   final boolean bDrawBoxes, final boolean bindMSAA)
	{
		if( BVBSettings.bWeightedOIT )
		{
			flexibleFBO.bind( gl );
			gl.glDepthMask(true);
			sceneVolBuffer.drawQuadOnlyDepth( gl, true );
			gl.glBlendFunc( GL.GL_ONE, GL.GL_ONE ); // Additive RGB + alpha
			gl.glBlendEquation( GL.GL_FUNC_ADD );
		}
		else if (bindMSAA)
		{
			flexibleFBO.bind( gl );
			sceneVolBuffer.drawQuadColorDepth( gl, true );
		}
		int shapeN = shapes.size();
		//disable depth writing
		gl.glDepthMask(false);
		
		if(bDrawBoxes)
		{
			bvb.volumeBoxes.draw( gl, pvm, vm, screen_size, nTimePoint, BVBSettings.bWeightedOIT );
			//draw clip boxes
			bvb.clipBoxes.draw( gl, pvm, vm, screen_size, nTimePoint, BVBSettings.bWeightedOIT );
		}
		
		for(int i = 0; i < shapeN; i++)
		{
			final BasicShape sh = shapes.get( i );			
			sh.draw( gl, pvm, vm, screen_size, nTimePoint, BVBSettings.bWeightedOIT );
		}
	
		gl.glDepthMask(true);
		if(BVBSettings.bWeightedOIT)
		{
			flexibleFBO.unbind( gl, false );
			gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
			gl.glDisable( GL_DEPTH_TEST );	
			flexibleFBO.drawQuadAlpha( gl );
			gl.glEnable( GL_DEPTH_TEST );
		}else if (bindMSAA)
		{
			flexibleFBO.unbind( gl, false );
			gl.glDisable( GL_DEPTH_TEST );
			flexibleFBO.drawQuadColorDepth( gl );
			//gl.glEnable( GL_DEPTH_TEST );
		}
	}
	
	public void sort()
	{
		List< BasicShape > shapes = bvb.shapes;

		shOpaque.clear();
		shTransparent.clear();
		shOpaqueMSAA.clear();
		shOpaqueRegular.clear();
		shOpaqueEDL.clear();
		shTransparentMSAA.clear();
		shTransparentRegular.clear();
		
		final int shapeN = shapes.size();
		
		for(int i = 0; i < shapeN; i++)
		{
			final BasicShape sh = shapes.get( i );	
			if(!sh.isTransparent())
			{
				shOpaque.add( sh );
			}
			else
			{
				shTransparent.add( sh );
			}
		}

		for(int i = 0; i < shOpaque.size(); i++)
		{
			final BasicShape sh = shOpaque.get( i );	
			//first find spots with EDL
			boolean isEDL = false;
			if(sh instanceof BasicSpots)
			{
				final BasicSpots spots = ( BasicSpots ) sh;
				if(spots.getPointShade() == 2 && spots.getRenderType() < 2)
				{
					isEDL = true;
					shOpaqueEDL.add( sh );
				}					
			}
			//the rest of shapes
			if(!isEDL)
			{
				if(sh instanceof BasicMeshShape )
				{
					if(BVBSettings.bMultiSampleMesh)
					{
						shOpaqueMSAA.add( sh );
					}
					else
					{
						shOpaqueRegular.add( sh );
					}
				}
				if(sh instanceof BasicSpots )
				{
					if(BVBSettings.bMultiSampleSpots)
					{
						shOpaqueMSAA.add( sh );
					}
					else
					{
						shOpaqueRegular.add( sh );
					}
				}
			}
		}
		
		for(int i = 0; i < shTransparent.size(); i++)
		{
			final BasicShape sh = shTransparent.get( i );	
			if(sh instanceof BasicMeshShape )
			{
				if(BVBSettings.bMultiSampleMesh)
				{
					shTransparentMSAA.add( sh );
				}
				else
				{
					shTransparentRegular.add( sh );
				}
			}
			if(sh instanceof BasicSpots )
			{
				if(BVBSettings.bMultiSampleSpots)
				{
					shTransparentMSAA.add( sh );
				}
				else
				{
					shTransparentRegular.add( sh );
				}
			}
		}
	}
	
	void initDrawState(final GL3 gl, final RenderData data)
	{
		//clear buffer with color
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		//get viewport size and transform matrices 
		screen_size = new int [] {(int)data.getScreenWidth(), (int) data.getScreenHeight()};
		pvm = new Matrix4f( data.getPv() );
		view = MatrixMath.affine( data.getRenderTransformWorldToScreen(), new Matrix4f() );
		vm = MatrixMath.screen( data.getDCam(), screen_size[0], screen_size[1], new Matrix4f() ).mul( view );
		
		nTimePoint = bvb.bvvViewer.state().getCurrentTimepoint();
	}
}
