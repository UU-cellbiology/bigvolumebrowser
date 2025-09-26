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
import bvb.shapes.BasicShape;
import bvb.shapes.MeshColor;
import bvb.shapes.MeshTexture;

/** A primitive Gltf importer based on de.javagl.jgltf library. 
 * Written in a very inefficient way, but should work ok on small meshes. **/
public class GltfImporter
{
	
	public static List<BasicShape> loadGLTF(String sFilename)
	{
		
		final ArrayList<BasicShape> out = new ArrayList<>();
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
			
			// Entry point:
			for (SceneModel scene : gltfModel.getSceneModels()) 
			{
			    System.out.println("Scene: " + scene.getName());
			    for (NodeModel root : scene.getNodeModels()) 
			    {
			        traverseNode(root, 0);
			    }
			}  
			String meshName = "";
			for (NodeModel nodeModel : gltfModel.getNodeModels())
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
						for(int d=0;d<3;d++)
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
				
				for(final MeshModel meshModel: nodeModel.getMeshModels())
				{
					//System.out.println(meshModel.getName());
					int nMeshCount = 0;
					for(final MeshPrimitiveModel meshPrimitiveModel:meshModel.getMeshPrimitiveModels())
					{
						nMeshCount ++;
						meshName = nodeModel.getName() +"("+ meshModel.getName()+Integer.toString( nMeshCount ) +")";
						
						final Mesh currMesh = new NaiveFloatMesh();				
						final float [][] vert = readAttributeFloatArray(meshPrimitiveModel, "POSITION");
						final float [][] uvmap = readAttributeFloatArray(meshPrimitiveModel, "TEXCOORD_0");
						boolean bTexture = uvmap == null ? false : true;

						final MaterialModelV2 material = ( MaterialModelV2 ) meshPrimitiveModel.getMaterialModel();

						final TextureModel baseColorTexture = material.getBaseColorTexture();
						if (baseColorTexture == null) 
						{
							bTexture = false;
							//System.out.println("Gltf loader: no base color texture found, skipping mesh.");
							// break;
						}
						for(int i = 0; i < vert.length; i++)
						{
							currMesh.vertices().addf(vert[i][0], vert[i][1], vert[i][2] );
							if(bTexture)
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
						if(bTexture)
						{
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
							final MeshTexture meshTexture = new MeshTexture(currMesh, image);
							meshTexture.setTransform( nodeTransform );
							out.add( new MeshTexture(currMesh, image) );
						}
						//no texture, only color
						else
						{
							final float[] rgba = material.getBaseColorFactor();
							final Color colorMesh = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
							final MeshColor meshShape = new MeshColor(currMesh);
							meshShape.setColor( colorMesh );
							meshShape.setName( meshName );
							meshShape.setTransform(nodeTransform);
							out.add(meshShape );
						}
					}
				}
			}
		}
		return out;
	}
	
	public static void traverseNode(NodeModel node, int depth) 
	{
	    String indent = "";
	    for(int i=0;i<depth;i++)
	    {
	    	indent = indent + "  ";
	    }
		for(final MeshModel mesh: node.getMeshModels())
		{
		    if (mesh != null) {
		        System.out.printf("%sNode '%s' → Mesh '%s'%n",
		                          indent,
		                          node.getName(),
		                          mesh.getName());
		    } else {
		        System.out.printf("%sNode '%s' → no mesh%n",
		                          indent,
		                          node.getName());
		    }
		}
	    for (NodeModel child : node.getChildren()) {
	        traverseNode(child, depth + 1);
	    }
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
