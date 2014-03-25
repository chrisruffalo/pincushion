package com.github.chrisruffalo.multitunnel.tunnel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.github.chrisruffalo.multitunnel.file.DirectoryFilter;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;
import com.github.chrisruffalo.multitunnel.util.PathUtil;

public class TunnelFileManager {

	private final File tunnelDir;

	private final SimpleDateFormat archiveDateFormatter;
	
	public TunnelFileManager(File tunnelDir) {
		this.tunnelDir = tunnelDir;
		
		this.archiveDateFormatter = new SimpleDateFormat("yyyy.MM.dd_hh.mm.ss");
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
		// if the file already exists: move to archive dir
		if(tunnelFile.exists()) {
			this.archiveTunnel(toSave, tunnelFile);
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
	
	private void archiveTunnel(TunnelConfiguration toArchive, File oldFile) {
		// create archive path if it exists
		File archivePath = this.archivePathForConfiguration(toArchive);
		if(archivePath.isFile()) {
			archivePath.delete();
		}
		if(!archivePath.exists()) {
			archivePath.mkdirs();
		}
		
		// move only if it exists
		if(oldFile.exists() && oldFile.isFile()) {
			Date now = new Date();
			String timestamp = this.archiveDateFormatter.format(now);
			String archiveNameString = timestamp + "-tunnel.json";
			
			File archiveFile = new File(PathUtil.sanitize(archivePath) + File.separator + archiveNameString);
			
			try {
				FileUtils.moveFile(oldFile, archiveFile);
			} catch (IOException e) {
				// nothing to do?
			}
		}
	}
	
	public void deleteTunnel(TunnelConfiguration toDelete) {
		File tunnelFile = this.tunnelFileForConfiguration(toDelete);
		// this really just means move it to the archive
		if(tunnelFile.exists() && tunnelFile.isFile()) {
			this.archiveTunnel(toDelete, tunnelFile);
			tunnelFile.delete();
		}
	}
	
	private File basePathForConfiguration(TunnelConfiguration forPath) {
		// get interface name
		String iface = forPath.getSourceInterface();
		iface = this.filter(iface);
		
		// create file direct to tunnel
		File tunnelDir = new File(PathUtil.sanitize(this.tunnelDir) + File.separator + iface + File.separator + forPath.getSourcePort() + File.separator);
		
		return tunnelDir;
	}
	
	private File archivePathForConfiguration(TunnelConfiguration forPath) {
		File tunnelDir = this.basePathForConfiguration(forPath);
		
		File tunnelFile = new File(PathUtil.sanitize(tunnelDir) + File.separator + "archive" + File.separator);
		
		return tunnelFile;
	}
	
	
	private File tunnelFileForConfiguration(TunnelConfiguration forPath) {
		File tunnelDir = this.basePathForConfiguration(forPath);
		
		File tunnelFile = new File(PathUtil.sanitize(tunnelDir) + File.separator + "tunnel.json");
		
		return tunnelFile;		
	}
	
	private String filter(String iface) {
		// filter out unwanted characters
		iface = iface.replaceAll(":", "_");
		iface = iface.replaceAll("%", "-");
		
		return iface;
	}
	
}
