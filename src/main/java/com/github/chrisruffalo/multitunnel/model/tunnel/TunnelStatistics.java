package com.github.chrisruffalo.multitunnel.model.tunnel;

public class TunnelStatistics {

	private int activeConnections;
	
	private int totalConnections;
	
	private long returned;
	
	private long read;

	public int getActiveConnections() {
		return activeConnections;
	}

	public void setActiveConnections(int activeConnections) {
		this.activeConnections = activeConnections;
	}

	public int getTotalConnections() {
		return totalConnections;
	}

	public void setTotalConnections(int totalConnections) {
		this.totalConnections = totalConnections;
	}

	public long getReturned() {
		return returned;
	}

	public void setReturned(long sent) {
		this.returned = sent;
	}

	public long getRead() {
		return read;
	}

	public void setRead(long received) {
		this.read = received;
	}
	
}
