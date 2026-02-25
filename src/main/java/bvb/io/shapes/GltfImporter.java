/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
package bvb.io.shapes;

import com.jogamp.opengl.GL;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.impl.naive.NaiveFloatMesh;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.BufferViewModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import ij.IJ;
import bvb.shapes.BasicShape;
import bvb.shapes.MeshShape;
//import bvb.shapes.MeshTexture;

/** A primitive Gltf importer based on de.javagl.jgltf library. 
 * Written in a very inefficient way, but should work ok on small meshes. **/
public class GltfImporter
{
	final ArrayList<BasicShape> shapesOut = new ArrayList<>();
	int nTotNodes = 0;
	int nCurrNodeN = 0;
	
	public List<BasicShape> loadGLTF(String sFilename)
	{	
		shapesOut.clear();
		
		final GltfModelReader gltfModelReader = new GltfModelReader();
		
		GltfModel gltfModel = null;
		
		try
		{
			gltfModel = gltfModelReader.read(Paths.get(sFilename));
		}
		catch ( IOException exc )
		{
			exc.printStackTrace();
			return null;
		}
		
		if(gltfModel != null)
		{       
			nTotNodes = gltfModel.getNodeModels().size();
			// Entry point:
			for (SceneModel scene : gltfModel.getSceneModels()) 
			{
			    //System.out.println("Scene: " + scene.getName());
			    for (NodeModel root : scene.getNodeModels()) 
			    {
			    	final AffineTransform3D nodeTransformZero = new AffineTransform3D ();
			        traverseNode(root, 0, nodeTransformZero);
			    }
			}  
		}
		IJ.log( "Loaded " + Integer.toString( shapesOut.size() ) + " shapes from " + sFilename );
		return shapesOut;
	}
	
	/** returns pre-concatenated transform: first applied local and then provided  nodeTransformUpstream **/
	public AffineTransform3D loadNodeMeshes(final NodeModel nodeModel, final AffineTransform3D nodeTransformUpstream)
	{
		String meshName = "";
		
		final AffineTransform3D nodeTransform = getNodeTransform(nodeModel);
		nodeTransform.preConcatenate( nodeTransformUpstream );
		
		for(final MeshModel meshModel: nodeModel.getMeshModels())
		{					
			//System.out.println(meshModel.getName());
			//int nMeshCount = 0;
			for(final MeshPrimitiveModel meshPrimitiveModel:meshModel.getMeshPrimitiveModels())
			{
				//nMeshCount ++;
				meshName = nodeModel.getName() +"["+meshModel.getName()+"]("+ meshModel.getName() +")";
				
				final Mesh currMesh = new NaiveFloatMesh();				
				final float [][] vert = readAttributeFloatArray(meshPrimitiveModel, "POSITION");
				final float [][] uvmap = readAttributeFloatArray(meshPrimitiveModel, "TEXCOORD_0");

				for(int i = 0; i < vert.length; i++)
				{
					currMesh.vertices().addf(vert[i][0], vert[i][1], vert[i][2] );
					if(uvmap != null)
					{
						currMesh.vertices().setTexturef( i, uvmap[i][0], uvmap[i][1] ); 
					}
				}

				final int [] indices = readIndices(meshPrimitiveModel);
				for(int i = 0; i < indices.length; i++)
				{
					if((i+1)%3 == 0)
					{
						currMesh.triangles().add( indices[i-2], indices[i-1], indices[i] );
					}
				}	
				
				final MaterialModelV2 material = ( MaterialModelV2 ) meshPrimitiveModel.getMaterialModel();
				boolean bTextureLoaded = false;
				if(material != null)
				{
					final TextureModel baseColorTexture = material.getBaseColorTexture();
					//see if there is a texture
					if(baseColorTexture != null && uvmap != null)
					{
						bTextureLoaded = true;
						// 3. Get the image model
						final ImageModel imageModel = baseColorTexture.getImageModel();
						final BufferViewModel bufferViewModel = imageModel.getBufferViewModel();
						ByteBuffer byteBuffer = null;
	
						int nByteLength = 0 ;
						if(bufferViewModel != null)
						{
							byteBuffer = bufferViewModel.getBufferModel().getBufferData();
							nByteLength = bufferViewModel.getByteLength();				        
							byteBuffer.position(bufferViewModel.getByteOffset());
	
						}
						else
						{
							byteBuffer = imageModel.getImageData();
							nByteLength = byteBuffer.remaining();
						}
	
						final byte[] imageBytes = new byte[nByteLength];
						byteBuffer.get(imageBytes);
	
						BufferedImage image = null;		
	
						try
						{
							image = ImageIO.read(new ByteArrayInputStream(imageBytes));
						}
						catch ( IOException exc )
						{
							exc.printStackTrace();
							break;
						}
						final MeshShape meshTexture = new MeshShape(currMesh, image);
						meshTexture.setTransform( nodeTransform );
						meshTexture.setName( meshName );
						shapesOut.add( meshTexture );
						
					}
				}
				//no texture, only color
				if(!bTextureLoaded)
				{
					Color colorMesh;
					if(material != null)
					{
						final float[] rgba = material.getBaseColorFactor();
						final float[] rgbaEM = material.getEmissiveFactor();
						//for now, we are going to add them
						for(int d = 0; d < 3; d++)
						{
							rgba[d] += rgbaEM[d];
							rgba[d] = ( float ) Math.min(rgba[d], 1.0);
						}
						colorMesh = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
					}
					else
					{
						colorMesh = Color.WHITE;
					}
					final MeshShape meshShape = new MeshShape(currMesh);
					meshShape.setColor( colorMesh );
					meshShape.setName( meshName );
					meshShape.setTransform(nodeTransform);
					shapesOut.add(meshShape );
				}
			}
		}
		return nodeTransform;
	}
	
	AffineTransform3D getNodeTransform(final NodeModel nodeModel)
	{
		final AffineTransform3D nodeTransform = new AffineTransform3D();
		
		final float[] matTransform = nodeModel.getMatrix();
		
		if(matTransform == null)
		{
			final float[] scalef = nodeModel.getScale();
			
			if(scalef != null)
			{
				nodeTransform.scale( scalef[0], scalef[1], scalef[2] );
			}
			
			final float[] rotationQ = nodeModel.getRotation();
			if(rotationQ != null)
			{
				final double [] q = new double[4];
				//x,y,z,w to w,x,y,z
				for(int d = 0; d < 3; d++)
				{
					q[d+1] = rotationQ[d];
				}
				q[0] = rotationQ[3];
				
				final double [][] rotMatrix = new double [3][4];
				LinAlgHelpers.quaternionToR( q, rotMatrix );
				final AffineTransform3D rotationAf = new AffineTransform3D();
				rotationAf.set( rotMatrix );
				nodeTransform.preConcatenate( rotationAf );
			}
			
			final float[] translatef = nodeModel.getTranslation();
			if(translatef != null)
			{
				final double [] translated = new double [3];
				for(int d = 0; d < 3; d++)
				{
					translated[d] = translatef[d];
				}
				nodeTransform.translate( translated );
			}
		}
		else
		{
			final double [][] trMatrix = new double [3][4];
			for(int j = 0; j < 4; j++)
			{	
				for(int d = 0; d < 3; d++)
				{
					trMatrix[d][j] = matTransform[j*4 + d];
				}
			}
			nodeTransform.set( trMatrix );
		}
		return nodeTransform;
	}
	
	public void traverseNode(final NodeModel node, int depth, final AffineTransform3D nodeTransformParent) 
	{
	    
	    final AffineTransform3D nodeTransformCurrent = loadNodeMeshes(node, nodeTransformParent);
	    
	    nCurrNodeN++;
	    
	    IJ.showProgress( nCurrNodeN, nTotNodes  );
	    
	    for (NodeModel child : node.getChildren()) 
	    {
	        traverseNode(child, depth + 1, nodeTransformCurrent);
	    }
	    
//		//printout scene tree 	    
//	    String indent = "";
//	    for(int i=0;i<depth;i++)
//	    {
//	    	indent = indent + "  ";
//	    }
//		for(final MeshModel mesh: node.getMeshModels())
//		{
//		    if (mesh != null) {
//		        System.out.printf("%sNode '%s' → Mesh '%s'%n",
//		                          indent,
//		                          node.getName(),
//		                          mesh.getName());
//		    } else {
//		        System.out.printf("%sNode '%s' → no mesh%n",
//		                          indent,
//		                          node.getName());
//		    }
//		}
	}
	
	public static float [][] readAttributeFloatArray(final MeshPrimitiveModel meshPrimitiveModel, String key)
	{
		
		float [][] out = null;
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();

		// POSITION is the key for vertex positions
		AccessorModel positionAccessor = attributes.get(key);
		
		if (positionAccessor != null) 
		{
			AccessorData accessorData = positionAccessor.getAccessorData();
			ByteBuffer byteBuffer = accessorData.createByteBuffer();
			FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
			int numComponents = accessorData.getNumComponentsPerElement();
			int count = accessorData.getNumElements(); // Number of vertices
			out = new float [count][numComponents];
			for (int i = 0; i < count; i++) 
			{
				for(int j = 0; j < numComponents; j++ )
				{
					out[i][j] = floatBuffer.get();
				}
			}
		}
		return out;
	}
	
	public static int [] readIndices(MeshPrimitiveModel meshPrimitiveModel) 
	{
	    
		final AccessorModel indexAccessor = meshPrimitiveModel.getIndices();
	    
		if (indexAccessor == null) 
	    {
	        System.out.println("No indices found (may be non-indexed geometry).");
	        return null;
	    }

		final AccessorData accessorData = indexAccessor.getAccessorData();
		final int indexCount = accessorData.getNumElements();
	    final int[] indices = new int[indexCount];
	    final ByteBuffer byteBuffer = accessorData.createByteBuffer();
	    
	    // Depending on component type, read the data appropriately
	    switch (indexAccessor.getComponentType()) {
	        case GL.GL_UNSIGNED_BYTE:
	        	final byte [] indicesB = new byte[indexCount];
	        	byteBuffer.get(indicesB);
	        	for(int i = 0; i< indexCount;i++)
	        	{
	        		 indices[i] = indicesB[i] & 0xFF;
	        	}
	        	return indices;
	        case GL.GL_UNSIGNED_SHORT:
	        	ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
	        	final short[] indicesS = new short[indexCount];
	        	shortBuffer.get(indicesS);
	        	for(int i = 0; i < indexCount;i++)
	        	{
	        		 indices[i] = Short.toUnsignedInt( indicesS[i] );
	        	}
	        	return indices;
	        case GL.GL_UNSIGNED_INT:	        	
	            final IntBuffer intBuffer = byteBuffer.asIntBuffer();
	            intBuffer.get(indices);
	            return indices;

	        default:
	            System.out.println("Unsupported component type for indices: " + indexAccessor.getComponentType());
	            return null;
	    }
	}
}
