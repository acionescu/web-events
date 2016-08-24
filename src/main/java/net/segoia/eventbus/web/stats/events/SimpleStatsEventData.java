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
