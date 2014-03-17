package com.github.chrisruffalo.multitunnel.model.tunnel;

import com.github.chrisruffalo.multitunnel.tunnel.impl.TunnelStatus;

public class TunnelReference {

	private String id;
	
	private TunnelConfiguration configruation;
	
	private TunnelStatistics stats;
	
	private TunnelStatus status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
