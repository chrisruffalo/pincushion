package com.github.chrisruffalo.pincushion.model.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;

import com.github.chrisruffalo.pincushion.util.ApplicationProperties;


public class PincushionConfiguration {

	private String basePath;
	
	private int managementPort;
	
	private String managementInterface;
	
	private int workers;
	
	private int acceptors;

	public PincushionConfiguration() {
		// default options
		this.managementInterface = "0.0.0.0";
		this.managementPort = 8095;

		// workers/threads
		this.workers = Runtime.getRuntime().availableProcessors() / 4; // half for workers, half for acceptors
		this.acceptors = workers;
		
		if(this.workers < 1) {
			this.workers = 1;
		}
		
		if(this.acceptors < 1) {
		    this.acceptors = 1;
		}
		
		// directory in home path as default
		this.basePath = System.getProperty("user.home") + File.separator + "." + ApplicationProperties.INSTANCE.title().toLowerCase() + File.separator;
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
	
	public int getAcceptors() {
        return acceptors;
    }

    public void setAcceptors(int acceptors) {
        this.acceptors = acceptors;
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
	
	public static PincushionConfiguration read(InputStream stream, Logger logger) {
		
		ObjectMapper instance = new ObjectMapper();
		
		PincushionConfiguration configuration;
		try {
			configuration = instance.readValue(stream, PincushionConfiguration.class);
			logger.info("Loaded configuration");
		} catch (IOException e) {
			configuration = new PincushionConfiguration();
			logger.error("Error reading configuration, falling back to defaults: " + e.getMessage(), e);
		}
		
		return configuration;
	}
}
