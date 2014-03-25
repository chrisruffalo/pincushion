package com.github.chrisruffalo.pincushion.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Help determine active ports
 * 
 * @author cruffalo
 *
 */
public enum PortHelper {

	INSTANCE;
	
	// wrapper to use string
	public boolean available(String bind, int port) {
		try {
			InetAddress address = InetAddress.getByName(bind);
			return this.available(address, port);
		} catch (UnknownHostException e) {
			return false;
		}
	}
	
	// from: http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
	public boolean available(InetAddress bind, int port) {
		// make sure port is in a good range
		if(port < 1 || port > 65536) {
			return false;
		}
		
		// check both bind and based on talking to it
		boolean response = this.checkCanBind(bind, port) && this.checkCanTalk(bind, port);
		
		// return response
	    return response;
	}
	
	// the assumption here is that if we can bind to it, it must be available
	// from: http://stackoverflow.com/a/435579/298389
	private boolean checkCanBind(InetAddress bind, int port) {
		try (ServerSocket tester = new ServerSocket(port, 0, bind)) {
			tester.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			return false;
		}		
	}
	
	// the assumption here is that if we can talk to the port 
	// it is NOT open to be bound to
	// from: http://stackoverflow.com/a/15340291/128339
	private boolean checkCanTalk(InetAddress bind, int port) {
	    try (Socket ignored = new Socket(bind, port)) {
	        return false;
	    } catch (IOException ignored) {
	        return true;
	    }
	}
	
}
