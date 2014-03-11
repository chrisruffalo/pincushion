package com.github.chrisruffalo.multitunnel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		
		Logger logger = LoggerFactory.getLogger("main");
		
		// otherwise execute
		List<TunnelInstance> instances = options.getTunnels();

		// requires at least one tunnel
		if(instances == null || instances.isEmpty()) {
			System.out.println("At least one tunnel must be specified");
			return;
		}
		
		// calculate threads
		int threads = options.getThreads();
		int boss = (threads > 4) ? threads / 4 : 1;
		if(boss > 2) {
			boss = 2;
		}
		int event = threads - boss;
		
		// create shared thread pools
		EventLoopGroup bossGroup = new NioEventLoopGroup(boss);
		EventLoopGroup eventGroup = bossGroup;
		if(event > 1) {
			eventGroup = new NioEventLoopGroup(event);
			logger.info("Using {} threads [{} boss and {} event]", threads, boss, event);
		} else {
			logger.info("Using {} threads [{} shared boss and event]", threads, boss);
		}
				
		// start servers
		logger.info("Starting instances...");
		for(TunnelInstance instance : instances) {
			TunnelServer server = new TunnelServer(bossGroup, eventGroup, instance.getSourcePort(), instance.getDestHost(), instance.getDestPort());
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
