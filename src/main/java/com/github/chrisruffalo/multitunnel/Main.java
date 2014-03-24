package com.github.chrisruffalo.multitunnel;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.github.chrisruffalo.multitunnel.model.configuration.PincushionConfiguration;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.options.Options;
import com.github.chrisruffalo.multitunnel.tunnel.TunnelFileManager;
import com.github.chrisruffalo.multitunnel.tunnel.TunnelManager;
import com.github.chrisruffalo.multitunnel.util.ApplicationProperties;
import com.github.chrisruffalo.multitunnel.util.InterfaceHelper;
import com.github.chrisruffalo.multitunnel.util.PathUtil;
import com.github.chrisruffalo.multitunnel.web.ManagementServer;

public class Main {

	public static void main(String[] args) {
		
		// create commander and add converter
		Options options = new Options();
		JCommander commander = new JCommander(options);
		commander.setAllowAbbreviatedOptions(true);
		commander.setProgramName(ApplicationProperties.INSTANCE.title() + " v" + ApplicationProperties.INSTANCE.version());
	
		// parse
		commander.parse(args);
		
		// do help stuff
		if(options.isHelp()) {
			// print usage
			commander.usage();
			// done
			return;
		}
		
		// start logging
		Logger logger = LoggerFactory.getLogger("main");
		
		// do example print out
		if(options.isCreateExample()) {
			File example = new File("./multi-tunnel.configuration.example");
			if(example.exists()) {
				example.delete();
				try {
					example.createNewFile();
				} catch (IOException e) {
					System.out.println("Could not create example file: " + e.getMessage());
					return;
				}
			}
			
			// output stream
			try (FileOutputStream output = new FileOutputStream(example)) {
				PincushionConfiguration exampleConfiguration = new PincushionConfiguration();
				exampleConfiguration.write(output);
				System.out.println("Example file written to: " + PathUtil.sanitize(example));
				
				output.flush();
			} catch (FileNotFoundException e) {
				logger.error("Could not create example file: " + e.getMessage(), e);
				return;
			} catch (IOException e) {
				logger.error("Error while creating example file: " + e.getMessage(), e);
				return;
			} 
			
			// quit after creating example
			return;
		}
		
		// look for configuration file
		PincushionConfiguration config = new PincushionConfiguration();
		File configFile = options.getConfigurationFile();
		InputStream input = null;
		if(configFile.exists() && configFile.isFile()) {
			try {
				input = new FileInputStream(configFile);
				config = PincushionConfiguration.read(input, logger); 
				logger.info("Using configuration file: {}", PathUtil.sanitize(configFile));
			} catch (FileNotFoundException e) {
				logger.error("Could not load configuration file, using defaults");
			}
		} else {
			logger.info("Using default configuration");
		}
				
		// set netty stuff and various environment things
		ResourceLeakDetector.setLevel(Level.DISABLED);		
		
		// create tunnel holder
		List<TunnelConfiguration> configurations = new LinkedList<>();
		
		// add tunnels from command line, which take higher priority
		// than those configured in the home folder
		if(options.getTunnels() != null && !options.getTunnels().isEmpty()) {
			configurations.addAll(options.getTunnels());
		}
				
		// calculate threads
		int workers = config.getWorkers();
		if(workers < 1) {
			workers = 1;
		}
		EventLoopGroup eventGroup = new NioEventLoopGroup(workers);
		logger.info("Using {} workers", workers);
		
		// init interface helper since it can be slow
		InterfaceHelper.INSTANCE.init();
		
		// loading directory
		File home = new File(config.getBasePath());
		// cause an error if the home directory is already a file (not a dir)
		if(home.exists() && home.isFile()) {
			logger.error("The base directory '{}' exists and is a file, exiting", PathUtil.sanitize(home));
			return;
		}
		// if the home directory does not exist, create
		if(!home.exists()) {
			// create home
			home.mkdirs();
			// show that home was created
			logger.info("application directory '{}' created", PathUtil.sanitize(home));
			// bail if it doesn't exist
			if(!home.exists()) {
				return;
			}
		}
		
		// TODO: load modules
		//File modulesD = new File(PathUtil.sanitize(home) + File.separator + "modules.d" + File.separator);
		
		// load extant tunnels into configuration
		File tunnelsD = new File(PathUtil.sanitize(home) + File.separator + "tunnels.d" + File.separator);
		TunnelFileManager tunnelFileManager = new TunnelFileManager(tunnelsD);
		for(String interfaceAddress : tunnelFileManager.configuredInterfaces()) {
			for(TunnelConfiguration configuration : tunnelFileManager.tunnelsForInterface(interfaceAddress)) {
				configurations.add(configuration);
			}
		}

		// create tunnel manager
		TunnelManager manager = new TunnelManager(tunnelFileManager, eventGroup);

		// only start if some tunnel configuration objects exist
		if(configurations != null && !configurations.isEmpty()) {
			// filter null items
			Iterator<TunnelConfiguration> filter = configurations.iterator();
			while(filter.hasNext()) {
				TunnelConfiguration item = filter.next();
				if(item == null) {
					filter.remove();
				}
			}
						
			// start servers
			logger.info("Starting ({}) pre-configured tunnels...", configurations.size());
			for(TunnelConfiguration configuration : configurations) {
				// create an auto-name if none exists
				if(configuration.getName() == null || configuration.getName().isEmpty()) {
					// generate name for "auto" created tunnel
					String name = String.format("auto-%s:%d->%s:%d", 
													configuration.getSourceInterface(), 
													configuration.getSourcePort(), 
													configuration.getDestHost(), 
													configuration.getDestPort()
												);
					configuration.setName(name);
				}
				
				// pass to the manager to create
				manager.create(configuration);
			}
		} else {
			// empty configuration list
			configurations = new LinkedList<>();
		}
		
		ManagementServer server = new ManagementServer(manager, eventGroup, config);
		server.start();
			
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
