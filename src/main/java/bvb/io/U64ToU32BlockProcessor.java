package bvb.io;

import net.imglib2.algorithm.blocks.AbstractDimensionlessBlockProcessor;
import net.imglib2.algorithm.blocks.BlockProcessor;
import net.imglib2.type.NativeType;
import net.imglib2.util.Cast;

/**
 * Convert primitive arrays between standard ImgLib2 {@code Type}s.
 * Provides rounding, optional clamping, and handling unsigned types.
 * @param <I>
 * 		input primitive array type, e.g., float[]
 * @param <O>
 * 		output primitive array type, e.g., float[]
 */
class U64ToU32BlockProcessor<I, O> extends AbstractDimensionlessBlockProcessor< I, O >
{
	private final ConvertLoopBVB< I, O > loop;

	public < S extends NativeType< S >, T extends NativeType< T > > U64ToU32BlockProcessor( final S sourceType)
	{
		super( sourceType.getNativeTypeFactory().getPrimitiveType() );
		loop =  Cast.unchecked(Convert_u64_to_u16.INSTANCE);//ConvertLoops.get( UnaryOperatorType.of( sourceType, targetType ), clamp );
	}

	private U64ToU32BlockProcessor( U64ToU32BlockProcessor< I, O > convert )
	{
		super( convert );
		loop = convert.loop;
	}

	@Override
	public BlockProcessor< I, O > independentCopy()
	{
		return new U64ToU32BlockProcessor<>( this );
	}

	@Override
	public void compute( final I src, final O dest )
	{
		loop.apply( src, dest, sourceLength() );
	}
	
	static class Convert_u64_to_u16 implements ConvertLoopBVB< long[], short[] >
	{
		static final Convert_u64_to_u16 INSTANCE = new Convert_u64_to_u16();

		@Override
		public void apply( final long[] src, final short[] dest, final int length )
		{
			for ( int i = 0; i < length; ++i )
				dest[ i ] = (short)Math.min(  src[ i ], 65535L );
		}
	}
}

