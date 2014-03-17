package com.github.chrisruffalo.multitunnel.model.tunnel;

public class TunnelReference {

	private TunnelConfiguration configruation;
	
	private TunnelStatistics stats;

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

}
