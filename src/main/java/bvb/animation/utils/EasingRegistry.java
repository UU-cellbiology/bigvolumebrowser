package bvb.animation.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EasingRegistry
{
    private final Map<String, Easing> byId = new HashMap<>();
    private final List<String> allEasings = new ArrayList<>();

    public EasingRegistry()
    {
    	initAll();
    }
    public void register(Easing easing) 
    {
        byId.put(easing.getId(), easing);
        allEasings.add( easing.getId() );
    }

    public Easing get(String id) {
        Easing e = byId.get(id);
        if (e == null)
            throw new IllegalArgumentException("Unknown easing: " + id);
        return e;
    }

    public Collection<Easing> getAll() {
        return byId.values();
    }
    
    public String [] getAllNames() 
    {

    	final String [] out = new String [allEasings.size()];
    	for (int i = 0; i < allEasings.size(); i++)
    	{
    		out[i] = allEasings.get( i );
    	}
    	return out;
    }
    
    void initAll()
    {
    	register(new EasingLinear());
    	register(new EasingInQuad());
    	register(new EasingOutQuad());
    	register(new EasingInOutQuad());

    }
}
