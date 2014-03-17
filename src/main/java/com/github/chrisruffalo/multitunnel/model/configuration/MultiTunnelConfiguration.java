package com.github.chrisruffalo.multitunnel.model.configuration;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;


public class MultiTunnelConfiguration {

	private String basePath;
	
	private int managementPort;
	
	private String managementInterface;
	
	private int workers;

	public MultiTunnelConfiguration() {
		// default options
		this.managementInterface = "0.0.0.0";
		this.managementPort = 8095;

		// workers/threads
		this.workers = Runtime.getRuntime().availableProcessors() / 2;
		if(this.workers < 1) {
			this.workers = 1;
		}
		
		// local directory
		this.basePath = "./";
	}
	
	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public int getManagementPort() {
		return managementPort;
	}

	public void setManagementPort(int managementPort) {
		this.managementPort = managementPort;
	}

	public String getManagementInterface() {
		return managementInterface;
	}

	public void setManagementInterface(String managementInterface) {
		this.managementInterface = managementInterface;
	}

	public int getWorkers() {
		return workers;
	}

	public void setWorkers(int workers) {
		this.workers = workers;
	}	
	
	public void write(OutputStream output) {
		ObjectMapper instance = new ObjectMapper();
		
		try {
			instance.writerWithDefaultPrettyPrinter().writeValue(output, this);
		} catch (IOException e) {
			// just write empty json
			try {
				output.write("{}".getBytes());
			} catch (IOException e1) {
				// do nothing
			}
		}
	}	
}
