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

import net.segoia.event.eventbus.EventContext;
import net.segoia.event.eventbus.EventContextListener;

public class SimpleStats implements EventContextListener{
    private long startTime=System.currentTimeMillis();
    private long eventsCount;
    private long lastEventTs;
    private long lastEventsTsDiff;
    private float recentFrequency;

    @Override
    public void onEvent(EventContext ec) {
	eventsCount++;
	long now = System.currentTimeMillis();
	lastEventsTsDiff = now - lastEventTs;
	lastEventTs = now;
	
	recentFrequency=recentFrequency*0.9f+getLastFrequency()*0.01f;
    }
    
    
    private long duration() {
	return 1+((System.currentTimeMillis() - startTime))/1000;
    }

    public float getAvgFrequency() {
	return ((float)eventsCount)/duration();
    }
    
    public float getLastFrequency() {
	if(lastEventsTsDiff <=0 ) {
	    return 0;
	}
	return ((float)1000)/lastEventsTsDiff;
    }
    
    public float getRecentFrequency() {
	return recentFrequency;
    }
    
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * @return the eventsCount
     */
    public long getEventsCount() {
        return eventsCount;
    }

    /**
     * @return the lastEventTs
     */
    public long getLastEventTs() {
        return lastEventTs;
    }
    
    public float getActivityIndex() {
	return recentFrequency*0.1f+0.9f*((float)eventsCount)/duration();
    }
    

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("SimpleStats [startTime=").append(startTime).append(", eventsCount=").append(eventsCount)
		.append(", lastEventTs=").append(lastEventTs).append(", lastEventsTsDiff=").append(lastEventsTsDiff)
		.append(", recentFrequency=").append(recentFrequency).append(", duration()=").append(duration())
		.append(", getAvgFrequency()=").append(getAvgFrequency()).append(", getLastFrequency()=")
		.append(getLastFrequency()).append("]");
	return builder.toString();
    }

    @Override
    public void init() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void terminate() {
	// TODO Auto-generated method stub
	
    }

}
