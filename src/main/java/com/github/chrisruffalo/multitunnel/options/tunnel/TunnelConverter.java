package com.github.chrisruffalo.multitunnel.options.tunnel;

import com.beust.jcommander.IStringConverter;

public class TunnelConverter implements IStringConverter<TunnelInstance> {

	@Override
	public TunnelInstance convert(String arg0) {
		TunnelInstance instance = new TunnelInstance();
		
		String[] split = arg0.split(":");
		
		if(split.length != 3) {
			throw new IllegalArgumentException("A tunnel must be in the format \"<sourcePort>:<destinationHost>:<destinationPort>\"");
		}
		
		instance.setSourcePort(Integer.parseInt(split[0]));
		instance.setDestHost(split[1]);
		instance.setDestPort(Integer.parseInt(split[2]));
		
		return instance;
	}

}
