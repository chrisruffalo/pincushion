package com.github.chrisruffalo.pincushion.model.tunnel;

import java.util.Date;

public class TunnelHistoryItem implements Comparable<TunnelHistoryItem> {

	private String name;
	
	private Date lastActive;
	
	private String jsonString;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getLastActive() {
		return lastActive;
	}

	public void setLastActive(Date lastActive) {
		this.lastActive = lastActive;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	@Override
	public int compareTo(TunnelHistoryItem o) {
		// if o is null then this item comes ahead
		if(o == null) {
			return 1;
		}
				
		// compare for time, newest item first
		return this.lastActive.compareTo(o.getLastActive());
	}

}
