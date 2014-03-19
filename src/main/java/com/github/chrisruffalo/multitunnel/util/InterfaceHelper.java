package com.github.chrisruffalo.multitunnel.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum InterfaceHelper {

	INSTANCE;

	private static final String PHYSICAL = "Physical";
	private static final String HOST_NAME = "Hostname";
	private static final String IPV4 = "IPV4";
	private static final String IPV6 = "IPV6";
	
	private static final String FALLBACK = "127.0.0.1";

	private Logger logger;
	
	private Map<String, Set<String>> interfaceCache;
	
	private InterfaceHelper() {
		this.logger = LoggerFactory.getLogger("interfaces");
	}
	
	/**
	 * takes an interface (that could be a physical interface or host name)
	 * and tries to map it back to something sane (the bind address);
	 * 
	 * @return
	 */
	public String sanitize(String iface) {
		
		Set<String> allValidInterfaces = this.validInterfaceSet();

		Set<String> physical = this.validInterfaces().get(InterfaceHelper.PHYSICAL);
		
		// already known, resolve
		if(allValidInterfaces.contains(iface)) {
			if(physical.contains(iface)) {
				// look up physical address and use value for it
				try {
					NetworkInterface phys = NetworkInterface.getByName(iface);
					Enumeration<InetAddress> addresses = phys.getInetAddresses();
					InetAddress found = null;
					while(addresses.hasMoreElements()) {
						found = addresses.nextElement();
						if(found instanceof Inet4Address) {
							return found.getHostAddress();
						}
					}
					if(found != null) {
						return found.getHostAddress();
					}
					return this.fallback(iface);
				} catch (SocketException e) {
					return this.fallback(iface);
				}				
			}
			return iface;
		} else {
			// look up by host name because it is not "known"
			try {
				InetAddress address = InetAddress.getByName(iface);
				iface = address.getHostAddress();
				// check if the new address is in the set of valid interfaces
				if(allValidInterfaces.contains(iface)) {
					// if it is, but is physical, continue to sanitize
					if(physical.contains(iface)) {
						return this.sanitize(iface);
					}
					// otherwise just return
					return iface;
				} else {
					// in case it is not, fallback
					return this.fallback(iface);
				}
			} catch (UnknownHostException e) {
				// fallback when the host cannot be found
				return this.fallback(iface);
			}
		}		
	}
	
	private String fallback(String iface) {
		this.logger.warn("Could not resolve address for given bind interface '{}', falling back to '{}'", iface, InterfaceHelper.FALLBACK);
		return InterfaceHelper.FALLBACK;
	}
	
	/**
	 * Dummy method for init
	 * 
	 */
	public void init() {
		this.logger.info("enumerating interfaces...");
		this.validInterfaces();
	}
	
	public Map<String, Set<String>> validInterfaces() {
		
		if(this.interfaceCache != null) {
			return this.interfaceCache;
		}
		
		Map<String,Set<String>> interfaces = new TreeMap<String, Set<String>>();
		
		Set<String> physInterfaces = new TreeSet<String>();
		Set<String> hostInterfaces = new TreeSet<String>();
		Set<String> ip4Interfaces = new TreeSet<String>();
		Set<String> ip6Interfaces = new TreeSet<String>();
		
		// enumerate interfaces
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
			for(NetworkInterface iface : Collections.list(nets)) {
				// physical interfaces
				physInterfaces.add(iface.getDisplayName());
				// addresses 
				Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
		        	// check address
		        	String address = inetAddress.getHostAddress();
		        	if(address == null || address.isEmpty()) {
		        		continue;
		        	}

					// check host (especially against address)
					String host = inetAddress.getHostName();
					if(host != null && !host.isEmpty() && !host.equals(address)) {
						hostInterfaces.add(host);
					}
					
		        	// ipv6 address
		        	if(inetAddress instanceof Inet6Address) {
		        		ip6Interfaces.add(address);
		        	} else if(inetAddress instanceof Inet4Address) {
		        		// otherwise ipv4 address
		            	ip4Interfaces.add(address);
		        	}
		        }
			}
		} catch (SocketException e) {
			this.logger.error("could not enumerate network interfaces, only listing loopback and bind all");
		}
		
		// make sure that global bind is included
		if(!ip4Interfaces.contains("0.0.0.0")) {
			ip4Interfaces.add("0.0.0.0");
		}
		
		// make sure that localhost is included
		if(!ip4Interfaces.contains("127.0.0.1")) {
			ip4Interfaces.add("127.0.0.1");
		}
		
		// make sure that ipv6 localhost is included
		if(!ip6Interfaces.contains("::1")) {
			ip6Interfaces.add("::1");
		}
		
		// make sure that global bind ipv6 is included
		if(!ip6Interfaces.contains("::0")) {
			ip6Interfaces.add("::0");
		}
		
		// combine all
		interfaces.put(InterfaceHelper.PHYSICAL, physInterfaces);
		interfaces.put(InterfaceHelper.HOST_NAME, hostInterfaces);
		interfaces.put(InterfaceHelper.IPV4, ip4Interfaces);
		interfaces.put(InterfaceHelper.IPV6, ip6Interfaces);
		
		// cache
		this.interfaceCache = interfaces;

		return interfaces;
	}
	
	public Set<String> validInterfaceSet() {
		Map<String,Set<String>> interfaces = this.validInterfaces();
		
		Set<String> interfacesSet = new HashSet<String>();
		
		for(Set<String> subSet : interfaces.values()) {
			if(subSet != null && !subSet.isEmpty()) {
				interfacesSet.addAll(subSet);				
			}
		}
		
		return interfacesSet;
	}
	
}
