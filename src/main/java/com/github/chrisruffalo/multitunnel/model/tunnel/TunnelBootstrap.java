package com.github.chrisruffalo.multitunnel.model.tunnel;

import java.util.Map;
import java.util.Set;

public class TunnelBootstrap {

	private TunnelConfiguration configuration;
	
	private Set<Integer> blockedPorts;
	
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

	public Set<Integer> getBlockedPorts() {
		return blockedPorts;
	}

	public void setBlockedPorts(Set<Integer> blockedPorts) {
		this.blockedPorts = blockedPorts;
	}

	public Map<String,Set<String>> getValidInterfaces() {
		return validInterfaces;
	}

	public void setValidInterfaces(Map<String,Set<String>> validInterfaces) {
		this.validInterfaces = validInterfaces;
	}	
	
}
