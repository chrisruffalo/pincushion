package com.github.chrisruffalo.multitunnel.options.tunnel;

import com.beust.jcommander.IStringConverter;
import com.github.chrisruffalo.multitunnel.model.tunnel.TunnelConfiguration;

public class TunnelConfigurationConverter implements IStringConverter<TunnelConfiguration> {

	@Override
	public TunnelConfiguration convert(String arg0) {
		TunnelConfiguration instance = new TunnelConfiguration();
		
		String[] split = arg0.split(":");
		
		if(split.length < 3) {
			throw new IllegalArgumentException("A tunnel must be in the format \"<sourcePort>:<destinationHost>:<destinationPort>\" or \"<sourceInterface>:<sourcePort>:<destinationHost>:<destinationPort>\"");
		}
		
		int offset = 0;
		if(split.length == 4) {
			instance.setSourceInterface(split[0]);
			offset = 1;
		}
		instance.setSourcePort(Integer.parseInt(split[0 + offset]));
		instance.setDestHost(split[1 + offset]);
		instance.setDestPort(Integer.parseInt(split[2 + offset]));
		
		return instance;
	}

}
