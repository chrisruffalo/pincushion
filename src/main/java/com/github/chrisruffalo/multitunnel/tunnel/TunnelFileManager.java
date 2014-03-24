package com.github.chrisruffalo.multitunnel.tunnel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

import com.github.chrisruffalo.multitunnel.file.DirectoryFilter;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.util.PathUtil;

public class TunnelFileManager {

	private final File tunnelDir;
	
	public TunnelFileManager(File tunnelDir) {
		this.tunnelDir = tunnelDir;
	}	
	
	public List<String> configuredInterfaces() {
		if(!tunnelDir.exists() || !tunnelDir.isDirectory()) {
			return Collections.emptyList();
		}
		
		List<String> interfaces = new LinkedList<>();
		
		// for each directory, return.  make sure 0.0.0.0 and ::0 are first.
		File[] ifaceDirs = this.tunnelDir.listFiles(new DirectoryFilter());
		
		// add all interfaces
		for(File f : ifaceDirs) {
			interfaces.add(f.getName());
		}
		
		return interfaces;
	}
	
	public List<TunnelConfiguration> tunnelsForInterface(String iface) {
		if(!tunnelDir.exists() || !tunnelDir.isDirectory()) {
			return Collections.emptyList();
		}
		
		// filter
		iface = this.filter(iface);
		
		File portsDir = new File(PathUtil.sanitize(this.tunnelDir) + File.separator + iface + File.separator);
		
		if(!portsDir.exists() || !portsDir.isDirectory()) {
			return Collections.emptyList();
		}

		List<TunnelConfiguration> tunnels = new LinkedList<>();		
		
		// for each port directory, open tunnel.json and return tunnel configuration
		File[] portDirs = portsDir.listFiles(new DirectoryFilter());
		
		// for each port
		for(File portDir : portDirs) {
			// look for tunnel.json
			File tunnelFile = new File(PathUtil.sanitize(portDir) + File.separator + "tunnel.json");
			
			// if the file isn't availbale or isn't a file, don't bother
			if(!tunnelFile.exists() || !tunnelFile.isFile()) {
				continue;
			}
			
			// load tunnel configuration from file
			try (FileInputStream stream = new FileInputStream(tunnelFile)) {
				ObjectMapper instance = new ObjectMapper();
				TunnelConfiguration config = instance.readValue(stream, TunnelConfiguration.class);
				tunnels.add(config);
			} catch (FileNotFoundException e) {
				// do nothing?				
			} catch (IOException e) {
				// do nothing?
			} 
		}		
		
		return tunnels;
	}
	
	public void saveTunnel(TunnelConfiguration toSave) {
		String iface = toSave.getSourceInterface();
		iface = this.filter(iface);
		
		// create file direct to tunnel
		File tunnelFile = new File(PathUtil.sanitize(this.tunnelDir) + File.separator + iface + File.separator + toSave.getSourcePort() + File.separator + "tunnel.json");
		if(!tunnelFile.getParentFile().exists()) {
			tunnelFile.getParentFile().mkdirs();
		}
		// delete if it exists
		// in future: move to archive dir
		if(tunnelFile.exists()) {
			tunnelFile.delete();
		} 
		try {
			tunnelFile.createNewFile();
		} catch (IOException e) {
			// could not create
		}
		// write out
		try (FileOutputStream stream = new FileOutputStream(tunnelFile)) {
			ObjectMapper instance = new ObjectMapper();
			// print pretty to file (for humans!)
			instance.writerWithDefaultPrettyPrinter().writeValue(stream, toSave);
		} catch (FileNotFoundException e) {
			// do nothing?				
		} catch (IOException e) {
			// do nothing?
		}		
	}
	
	private String filter(String iface) {
		// filter out unwanted characters
		iface = iface.replaceAll(":", "_");
		iface = iface.replaceAll("%", "-");
		
		return iface;
	}
	
}
