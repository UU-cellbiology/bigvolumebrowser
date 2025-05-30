package bvb.scene;

import com.jogamp.opengl.GL3;

import org.joml.Matrix4fc;

public interface BasicVis
{
	/** reloads shaders in case of BVV/BVB restart **/
	public void reload();
	
	/** draws OpenGL primitive **/
	public void draw(final GL3 gl, final Matrix4fc pvm, final Matrix4fc vm, final int [] screen_size );
	
}
