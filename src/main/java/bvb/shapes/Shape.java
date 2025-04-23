package bvb.shapes;

import com.jogamp.opengl.GL3;

import org.joml.Matrix4fc;

public interface Shape
{
	/** method to draw GPU primitives **/
	public void draw( final GL3 gl, final Matrix4fc pvm,  final Matrix4fc vm, final int [] screen_size);

	/** method required to reload GPU shader/primitives 
	 * during BVV restart **/
	public void reload();
}
