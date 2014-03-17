package com.github.chrisruffalo.multitunnel.model.tunnel;

public class TunnelConfiguration {
	
	private String name;
	
	private String sourceInterface;
	
	private int sourcePort;
	
	private String destHost;
	
	private int destPort;

	public TunnelConfiguration() {
		this.sourceInterface = "0.0.0.0";
	}
	
	public String getName() {
		if(name == null || name.isEmpty()) {
			return String.format("auto-%s:%d->%s:%d", this.sourceInterface, this.sourcePort, this.destHost, this.destPort);
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSourceInterface() {
		return sourceInterface;
	}

	public void setSourceInterface(String sourceInterface) {
		this.sourceInterface = sourceInterface;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	public String getDestHost() {
		return destHost;
	}

	public void setDestHost(String destHost) {
		this.destHost = destHost;
	}

	public int getDestPort() {
		return destPort;
	}

	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}
	
}
