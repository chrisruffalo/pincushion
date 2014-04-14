package com.github.chrisruffalo.pincushion.model.tunnel;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TunnelBootstrap {

	private TunnelConfiguration configuration;
	
	private List<TunnelHistoryItem> history;
	
	private Map<String,Set<String>> validInterfaces;

	public TunnelBootstrap() {
		this.configuration = new TunnelConfiguration();
	}
	
	public TunnelConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(TunnelConfiguration configuration) {
		this.configuration = configuration;
	}

	public Map<String,Set<String>> getValidInterfaces() {
		return validInterfaces;
	}

	public void setValidInterfaces(Map<String,Set<String>> validInterfaces) {
		this.validInterfaces = validInterfaces;
	}

	public List<TunnelHistoryItem> getHistory() {
		return history;
	}

	public void setHistory(List<TunnelHistoryItem> history) {
		this.history = history;
	}	
	
}
