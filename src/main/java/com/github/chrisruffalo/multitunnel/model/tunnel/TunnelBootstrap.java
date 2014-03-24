package com.github.chrisruffalo.multitunnel.model.tunnel;

import java.util.Map;
import java.util.Set;

public class TunnelBootstrap {

	private TunnelConfiguration configuration;
	
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
	
}
