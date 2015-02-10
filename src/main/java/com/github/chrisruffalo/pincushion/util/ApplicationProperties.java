package com.github.chrisruffalo.pincushion.util;

import java.io.IOException;
import java.util.Properties;

public enum ApplicationProperties {

	INSTANCE;
	
	private final Properties props;
	
	private ApplicationProperties() {
		this.props = new Properties();

		// set some defaults...
		this.props.put("version", "UNKNOWN");
		this.props.put("title", "Pincushion");
		this.props.put("shortName", "pincushion");
		
		// load
		try {
			this.props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
		} catch (IOException e) {
			// no-op
		}
	}
	
	public String version() {
		return this.props.getProperty("version");
	}
	
	public String title() {
		return this.props.getProperty("title");
	}
	
	public String shortName() {
		return this.props.getProperty("shortName");
	}
	
}
