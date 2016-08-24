package net.segoia.eventbus.web.stats.events;

import net.segoia.event.eventbus.CustomEvent;
import net.segoia.event.eventbus.EventType;
import net.segoia.eventbus.stats.SimpleStats;

@EventType("STATS:STATS:SIMPLE")
public class SimpleStatsEvent extends CustomEvent<SimpleStatsEventData>{

    public SimpleStatsEvent(String name, SimpleStats stats) {
	super(SimpleStatsEvent.class);
	this.data = new SimpleStatsEventData(name, stats);
    }

}
