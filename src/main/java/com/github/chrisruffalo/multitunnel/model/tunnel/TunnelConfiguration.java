package com.github.chrisruffalo.multitunnel.model.tunnel;

public class TunnelConfiguration {
	
	private String name;
	
	private String sourceInterface;
	
	private Integer sourcePort;
	
	private String destHost;
	
	private Integer destPort;

	public TunnelConfiguration() {
		this.sourceInterface = "0.0.0.0";
	}
	
	public String getName() {
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

	public Integer getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(Integer sourcePort) {
		this.sourcePort = sourcePort;
	}

	public String getDestHost() {
		return destHost;
	}

	public void setDestHost(String destHost) {
		this.destHost = destHost;
	}

	public Integer getDestPort() {
		return destPort;
	}

	public void setDestPort(Integer destPort) {
		this.destPort = destPort;
	}
	
}
