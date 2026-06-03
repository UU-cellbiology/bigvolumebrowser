package bvb.io;

import com.github.benmanes.caffeine.cache.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;

import bvb.core.BVVSettings;


public class CaffeineLoaderCache < V>
{
	final ConcurrentHashMap< String, CacheLoader< Long, V> > map = new ConcurrentHashMap<>();
	
	public final LoadingCache<TimeLevelCellKey, V> caffeine;
	public CaffeineLoaderCache()
	{
		long maxHeap = Runtime.getRuntime().maxMemory();
		long cacheBudget = (long) (maxHeap * 0.1);
		caffeine =
                Caffeine.newBuilder()
                        .maximumWeight( cacheBudget )
                        .weigher( (k, cell)-> {
                        	
                        	return BVVSettings.cacheBlockSize *
                        			BVVSettings.cacheBlockSize *
                        			BVVSettings.cacheBlockSize * 2;
                        	} )
                		//.maximumSize(10_000) // simple safe default
                        .build(key -> {
                            try
                            {
                            	return map.get(key.getTimePointLevel()).get( key.getCellKey() );                          
                            }
                            catch (Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                        });
	}
	
	public Cache<Long, V> withLoader(final CacheLoader< Long, V> loader, final int timepoint, final int level)
	{
		final TimeLevelCellKey curV = new TimeLevelCellKey(timepoint, level, 0);
		map.put( curV.getTimePointLevel(), loader );
		return new Cache<Long, V>() {

            @Override
            public V get(Long key) throws ExecutionException
            {
        		final TimeLevelCellKey curr = new TimeLevelCellKey(timepoint, level, key);

                return caffeine.get(curr);
            }

            @Override
            public void invalidate(Long key)
            {
            	final TimeLevelCellKey curr = new TimeLevelCellKey(timepoint, level, key);
                caffeine.invalidate(curr);
            }

			@Override
			public V getIfPresent( Long key )
			{	
				final TimeLevelCellKey curr = new TimeLevelCellKey(timepoint, level, key);
				return caffeine.getIfPresent( curr );
			}

			@Override
			public void persist( Long key )
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void persistIf( Predicate< Long > condition )
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void persistAll()
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void invalidateIf( long parallelismThreshold, Predicate< Long > condition )
			{
				caffeine.asMap().keySet().stream()
	                .filter(k ->               	
	                	condition.test(k.getCellKey())
	                )
	                .forEach(caffeine::invalidate);
			}

			@Override
			public void invalidateAll( long parallelismThreshold )
			{
				caffeine.invalidateAll();				
			}
        };
	}
	
    public static <K, V> Cache<K, V> withLoader(CacheLoader< K, V> loader)
    {
        LoadingCache<K, V> caffeine =
                Caffeine.newBuilder()
                        .maximumSize(10_000) // simple safe default
                        .build(key -> {
                            try
                            {
                                return loader.get(key);
                            }
                            catch (Exception e)
                            {
                                throw new RuntimeException(e);
                            }
                        });

        return new Cache<K, V>() {

            @Override
            public V get(K key) throws ExecutionException
            {
                return caffeine.get(key);
            }

            @Override
            public void invalidate(K key)
            {
                caffeine.invalidate(key);
            }

			@Override
			public V getIfPresent( K key )
			{				
				return caffeine.getIfPresent( key );
			}

			@Override
			public void persist( K key )
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void persistIf( Predicate< K > condition )
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void persistAll()
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void invalidateIf( long parallelismThreshold, Predicate< K > condition )
			{
				caffeine.asMap().keySet().stream()
	                .filter(k -> condition.test(k))
	                .forEach(caffeine::invalidate);
			}

			@Override
			public void invalidateAll( long parallelismThreshold )
			{
				caffeine.invalidateAll();				
			}
        };
    }

}