package bvb.shapes;

import com.jogamp.opengl.GL3;

import org.joml.Matrix4fc;

public interface Shape
{
	public void draw( final GL3 gl, final Matrix4fc pvm,  final Matrix4fc vm, final int [] screen_size);

	//needed to reload primitive during BVV restart
	public void reload();
}
