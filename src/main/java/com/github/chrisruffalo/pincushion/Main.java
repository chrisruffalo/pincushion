package com.github.chrisruffalo.pincushion;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.github.chrisruffalo.pincushion.model.configuration.PincushionConfiguration;
import com.github.chrisruffalo.pincushion.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.pincushion.options.Options;
import com.github.chrisruffalo.pincushion.tunnel.TunnelFileManager;
import com.github.chrisruffalo.pincushion.tunnel.TunnelManager;
import com.github.chrisruffalo.pincushion.util.ApplicationProperties;
import com.github.chrisruffalo.pincushion.util.InterfaceHelper;
import com.github.chrisruffalo.pincushion.util.PathUtil;
import com.github.chrisruffalo.pincushion.web.ManagementInterface;

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
			File example = new File("./pincushion.configuration.example");
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
				logger.info("Example file written to: " + PathUtil.sanitize(example));
				
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

		if(configFile.exists() && configFile.isFile()) {
			try(InputStream input = new FileInputStream(configFile)) {
				config = PincushionConfiguration.read(input, logger);
				logger.info("Using configuration file: {}", PathUtil.sanitize(configFile));
			} catch (FileNotFoundException e) {
				logger.error("Could not load configuration file, using defaults");
			} catch (IOException e) {
                logger.error("Could not close Pincushion configuration file: " + e.getMessage());
            }
        } else {
			logger.info("Using default configuration");
		}
				
		// set netty stuff and various environment things
		ResourceLeakDetector.setLevel(Level.DISABLED);		
		
		// create tunnel holder
		int commandLineTunnels = 0;
		final List<TunnelConfiguration> configurations = new LinkedList<>();
		
		// add tunnels from command line, which take higher priority
		// than those configured in the home folder
		if(options.getTunnels() != null && !options.getTunnels().isEmpty()) {
			configurations.addAll(options.getTunnels());
			commandLineTunnels = options.getTunnels().size();
		}
				
		// calculate threads
		int acceptors = config.getAcceptors();
		if(acceptors < 1) {
		    acceptors = 1;
		}
		
		int workers = config.getWorkers();
		if(workers < 1) {
			workers = 1;
		}
		
		// get executor
        final DefaultThreadFactory threadFactory = new DefaultThreadFactory("pincushion");
		final Executor executor = Executors.newCachedThreadPool(threadFactory);

		// create event groups
		final EventLoopGroup acceptorEventGroup = new NioEventLoopGroup(acceptors, executor);
		logger.info("Using {} acceptor threads", acceptors);
		
		final EventLoopGroup workerEventGroup = new NioEventLoopGroup(workers, executor);
		logger.info("Using {} child workers", workers);
		
		// init interface helper since it can be slow
		InterfaceHelper.INSTANCE.init();
		
		// loading directory
		final File home = new File(config.getBasePath());
		// cause an error if the home directory is already a file (not a dir)
		if(home.exists() && home.isFile()) {
			logger.error("The base directory '{}' exists and is a file, please delete this file so a directory can be created.", PathUtil.sanitize(home));
			return;
		}

		// if the Pincushion home directory does not exist, create
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
		
		// TODO: load/init/check modules
		//File modulesD = new File(PathUtil.sanitize(home) + File.separator + "modules.d" + File.separator);
		
		// load extant tunnels into configuration
		int configuredTunnels = 0;
		File tunnelsD = new File(PathUtil.sanitize(home) + File.separator + "tunnels.d" + File.separator);
		final TunnelFileManager tunnelFileManager = new TunnelFileManager(tunnelsD);
		for(String interfaceAddress : tunnelFileManager.configuredInterfaces()) {
			for(TunnelConfiguration configuration : tunnelFileManager.tunnelsForInterface(interfaceAddress)) {
				configurations.add(configuration);
				configuredTunnels++;
			}
		}

		// create tunnel manager
		TunnelManager tunnelManager = new TunnelManager(tunnelFileManager, acceptorEventGroup, workerEventGroup);

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
			logger.info("Starting {} tunnels from command line and {} saved configurations ({} total)...", commandLineTunnels, configuredTunnels, configurations.size());
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
				tunnelManager.create(configuration);
			}
		} else {
			// empty configuration list
			configurations.clear();
		}
		
		// start management interface
		ManagementInterface server = new ManagementInterface(tunnelManager, config);
		server.start();
			
		// and wait
		while(true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
