package com.github.chrisruffalo.multitunnel.util;

import java.io.IOException;
import java.util.Properties;

public enum MultiTunnelProperties {

	INSTANCE;
	
	private Properties props;
	
	private MultiTunnelProperties() {
		this.props = new Properties();
		// load
		try {
			this.props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("multi-tunnel.properties"));
		} catch (IOException e) {
			// set some defaults...
			this.props.put("version", "UNKNOWN");
		}
	}
	
	public String version() {
		return this.props.getProperty("version");
	}
	
}
