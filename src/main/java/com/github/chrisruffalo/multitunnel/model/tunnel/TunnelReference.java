package com.github.chrisruffalo.multitunnel.model.tunnel;

import com.github.chrisruffalo.multitunnel.tunnel.impl.TunnelStatus;

/**
 * A view object used to represent the values in the 
 * actual, saved, tunnel object.
 * 
 * @author cruffalo
 *
 */
public class TunnelReference {

	private String id;
	
	private String bind;
	
	private TunnelConfiguration configruation;
	
	private TunnelStatistics stats;
	
	private TunnelStatus status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getBind() {
		return this.bind;
	}
	
	public void setBind(String bind) {
		this.bind = bind;
	}

	public TunnelConfiguration getConfigruation() {
		return configruation;
	}

	public void setConfigruation(TunnelConfiguration configruation) {
		this.configruation = configruation;
	}

	public TunnelStatistics getStats() {
		return stats;
	}

	public void setStats(TunnelStatistics stats) {
		this.stats = stats;
	}

	public TunnelStatus getStatus() {
		return status;
	}

	public void setStatus(TunnelStatus status) {
		this.status = status;
	}
}
