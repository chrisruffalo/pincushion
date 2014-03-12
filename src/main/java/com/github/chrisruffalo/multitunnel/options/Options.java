package com.github.chrisruffalo.multitunnel.options;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.chrisruffalo.multitunnel.model.TunnelInstance;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelConverter;

@Parameters()
public class Options {

	@Parameter(names={"--tunnel", "-t"}
			, description="Creates a tunnel using the format '<sourcePort>:<destinationHost>:<destinationPort>'.  You may have more than one tunnel but at least one is required."
			, converter=TunnelConverter.class
	)
	private List<TunnelInstance> tunnels;
	
	@Parameter(names = {"--help", "-h"}, description="Prints help message.", help = true)
	private boolean help;

	@Parameter(names = {"--workers", "-w"}, description="Number of event workers to use.  Recommended at least 2 per tunnel, for each desired concurrent connection.")
	private int workers;
	
	@Parameter(names = {"--managementPort", "-p"}, description="Management port.")
	private int managementPort;
	
	@Parameter(names = {"--managementInterface", "-i"}, description="Management interface.")
	private String managementInterface; 
	
	@Parameter(names = {"--management", "-m"}, description="Enable the management interface.")
	private boolean management;
	
	public Options() {
		this.workers = Runtime.getRuntime().availableProcessors() * 2;
		this.managementPort = 4041;
		this.managementInterface = "0.0.0.0";
		this.management = false;
	}
	
	public List<TunnelInstance>  getTunnels() {
		return tunnels;
	}

	public void setTunnels(List<TunnelInstance>  tunnels) {
		this.tunnels = tunnels;
	}

	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public int getWorkers() {
		return workers;
	}

	public void setWorkers(int threads) {
		this.workers = threads;
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

	public boolean isManagement() {
		return management;
	}

	public void setManagement(boolean management) {
		this.management = management;
	}
	
}
