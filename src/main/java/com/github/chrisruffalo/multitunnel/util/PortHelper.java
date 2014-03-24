package com.github.chrisruffalo.multitunnel.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

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
		if(port < 1 || port > 65536) {
			return false;
		}
		
		ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port, 0, bind);
	        ss.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
	}
	
}
