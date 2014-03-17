package com.github.chrisruffalo.multitunnel.options;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelConfigurationConverter;

@Parameters()
public class Options {

	@Parameter(names={"--tunnel", "-t"}
			, description="Creates a tunnel using the format '<sourcePort>:<destinationHost>:<destinationPort>'.  You may have more than one tunnel but at least one is required."
			, converter=TunnelConfigurationConverter.class
	)
	private List<TunnelConfiguration> tunnels;
	
	@Parameter(names = {"--help", "-h"}, description="Prints help message.", help = true)
	private boolean help;

	@Parameter(names = {"--configuration", "-c"}, description="JSON formatted configuration file.")
	private File configurationFile;
	
	@Parameter(names = {"--example", "-e"}, description="Create an example configuration at 'multi-tunnel.configuration.example' and exit.")
	private boolean createExample;
	
	public Options() {
		// boolean options
		this.help = false;
		this.createExample = false;
		
		// default
		this.configurationFile = new File("multi-tunnel.configuration");
		
		// empty tunnel
		this.tunnels = new LinkedList<>();
	}
	
	public List<TunnelConfiguration>  getTunnels() {
		return tunnels;
	}

	public void setTunnels(List<TunnelConfiguration>  tunnels) {
		this.tunnels = tunnels;
	}

	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public File getConfigurationFile() {
		return configurationFile;
	}

	public void setConfigurationFile(File configurationFile) {
		this.configurationFile = configurationFile;
	}

	public boolean isCreateExample() {
		return createExample;
	}

	public void setCreateExample(boolean createExample) {
		this.createExample = createExample;
	}	

}
