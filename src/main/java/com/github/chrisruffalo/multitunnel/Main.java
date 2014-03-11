package com.github.chrisruffalo.multitunnel;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.github.chrisruffalo.multitunnel.options.Options;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelInstance;
import com.github.chrisruffalo.multitunnel.util.MultiTunnelProperties;

public class Main {

	public static void main(String[] args) {
		
		// create commander and add converter
		Options options = new Options();
		JCommander commander = new JCommander(options);
		commander.setAllowAbbreviatedOptions(true);
		commander.setProgramName("multi-tunnel v" + MultiTunnelProperties.INSTANCE.version());
	
		// parse
		commander.parse(args);
		
		// do help stuff
		if(options.isHelp()) {
			// print usage
			commander.usage();
			// done
			return;
		}
		
		// otherwise execute
		List<TunnelInstance> instances = options.getTunnels();

		// requires at least one tunnel
		if(instances == null || instances.isEmpty()) {
			System.out.println("At least one tunnel must be specified");
			return;
		}
		
		// start servers
		for(TunnelInstance instance : instances) {
			TunnelServer server = new TunnelServer(instance.getSourcePort(), instance.getDestHost(), instance.getDestPort());
			server.start();
		}
		
		// and wait
		while(true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
