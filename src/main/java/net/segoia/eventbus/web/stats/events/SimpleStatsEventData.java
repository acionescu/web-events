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
package net.segoia.eventbus.web.stats.events;

import net.segoia.eventbus.stats.SimpleStats;

public class SimpleStatsEventData {
    private String name;
    private SimpleStats stats;
    
    
    public SimpleStatsEventData(String name, SimpleStats stats) {
	super();
	this.name = name;
	this.stats = stats;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @return the stats
     */
    public SimpleStats getStats() {
        return stats;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @param stats the stats to set
     */
    public void setStats(SimpleStats stats) {
        this.stats = stats;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SimpleStatsEventData [");
	if (name != null)
	    builder.append("name=").append(name).append(", ");
	if (stats != null)
	    builder.append("stats=").append(stats);
	builder.append("]");
	return builder.toString();
    }
    
    
}
