package bvb.io;

@FunctionalInterface
interface ConvertLoopBVB< I, O >
{
	void apply( final I src, final O dest, final int length );
}
