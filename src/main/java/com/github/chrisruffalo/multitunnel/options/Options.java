package com.github.chrisruffalo.multitunnel.options;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelConverter;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelInstance;

@Parameters()
public class Options {

	@Parameter(names={"--tunnel", "-t"}
			, description="Creates a tunnel using the format '<sourcePort>:<destinationHost>:<destinationPort>'.  You may have more than one tunnel but at least one is required."
			, converter=TunnelConverter.class
			, required = true
	)
	private List<TunnelInstance> tunnels;
	
	@Parameter(names = {"--help", "-h"}, description="Prints help message.", help = true)
	private boolean help;

	@Parameter(names = {"--workers", "-w"}, description="Number of event workers to use.  Recommended at least 2 per tunnel, for each desired concurrent connection.")
	private int workers;
	
	public Options() {
		this.workers = Runtime.getRuntime().availableProcessors();
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
	
}
