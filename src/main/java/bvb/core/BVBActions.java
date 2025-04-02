package bvb.core;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import bvb.gui.Rotate3DViewerStyle;
import bvvpg.vistools.BvvHandle;

public class BVBActions
{
	final BigVolumeBrowser bvb;
	public BVBActions(final BigVolumeBrowser bvb_) 
	{
		bvb = bvb_;
		installBehaviors();
	}
	/** install smoother rotation **/
	void installBehaviors()
	{
		final BvvHandle handle = bvb.bvv.getBvvHandle();
		//change drag rotation for navigation "3D Viewer" style
		final Rotate3DViewerStyle dragRotate = new Rotate3DViewerStyle( 0.75, handle);
		final Rotate3DViewerStyle dragRotateFast = new Rotate3DViewerStyle( 2.0, handle);
		final Rotate3DViewerStyle dragRotateSlow = new Rotate3DViewerStyle( 0.1, handle);
		
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig() );
		behaviours.behaviour( dragRotate, "drag rotate", "button1" );
		behaviours.behaviour( dragRotateFast, "drag rotate fast", "shift button1" );
		behaviours.behaviour( dragRotateSlow, "drag rotate slow", "ctrl button1" );
		behaviours.install( handle.getTriggerbindings(), "BigTrace Behaviours" );
	}
}
