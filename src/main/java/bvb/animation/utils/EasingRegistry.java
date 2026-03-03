/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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
