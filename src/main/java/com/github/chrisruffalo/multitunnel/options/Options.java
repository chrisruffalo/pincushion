package com.github.chrisruffalo.multitunnel.options;

import java.util.List;

import com.beust.jcommander.Parameter;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelConverter;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelInstance;

public class Options {

	@Parameter(names={"--tunnel"}, converter=TunnelConverter.class)
	private List<TunnelInstance> tunnels;
	
	@Parameter(names = "--help", help = true)
	private boolean help;

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
	
}
