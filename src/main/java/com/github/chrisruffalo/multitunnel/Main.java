package com.github.chrisruffalo.multitunnel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.github.chrisruffalo.multitunnel.options.Options;
import com.github.chrisruffalo.multitunnel.options.tunnel.TunnelInstance;
import com.github.chrisruffalo.multitunnel.tunnel.TunnelServer;
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
		
		Logger logger = LoggerFactory.getLogger("main");
		
		// otherwise execute
		List<TunnelInstance> instances = options.getTunnels();

		// requires at least one tunnel
		if(instances == null || instances.isEmpty()) {
			System.out.println("At least one tunnel must be specified");
			return;
		}
		
		// create executor group
		Executor pool = Executors.newCachedThreadPool();
				
		// calculate threads
		int workers = options.getWorkers();
		if(workers < 1) {
			workers = 1;
		}
		EventLoopGroup eventGroup = new NioEventLoopGroup(workers, pool);
		logger.info("Using {} workers", workers);
						
		// start servers
		logger.info("Starting ({}) tunnels...", instances.size());
		for(TunnelInstance instance : instances) {
			TunnelServer server = new TunnelServer(new NioEventLoopGroup(1, pool), eventGroup, instance.getSourcePort(), instance.getDestHost(), instance.getDestPort());
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
