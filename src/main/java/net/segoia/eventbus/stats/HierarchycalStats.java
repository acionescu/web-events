/**
 * web-events - An event-bus extension for events transmission over websockets
 * Copyright (C) 2016  Adrian Cristian Ionescu - https://github.com/acionescu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.segoia.eventbus.stats;

import java.util.HashMap;
import java.util.Map;

import net.segoia.event.eventbus.EventContext;

public abstract class HierarchycalStats extends SimpleStats{
    private Map<String, SimpleStats> nested = new HashMap<>();

    /* (non-Javadoc)
     * @see net.segoia.eventbus.stats.SimpleStats#onEvent(net.segoia.event.eventbus.EventContext)
     */
    @Override
    public void onEvent(EventContext ec) {
	if(!accept(ec)) {
	    return;
	}
	super.onEvent(ec);
	processNested(ec);
    }
    
    public SimpleStats getNested(String nestedKey, boolean create) {
	SimpleStats ns = nested.get(nestedKey);
	if(ns == null && create) {
	    ns = buildNestedStats();
	    nested.put(nestedKey, ns);
	}
	
	return ns;
    }
    
    public Map<String, SimpleStats> getNestedStats(){
	return nested;
    }
    
    protected boolean accept(EventContext ec) {
	return true;
    }
    
    protected abstract SimpleStats buildNestedStats();
    
    protected abstract void processNested(EventContext ec);
}
