package com.github.chrisruffalo.multitunnel.options.tunnel;

import com.beust.jcommander.IStringConverter;
import com.github.chrisruffalo.multitunnel.model.TunnelConfiguration;

public class TunnelConfigurationConverter implements IStringConverter<TunnelConfiguration> {

	@Override
	public TunnelConfiguration convert(String arg0) {
		TunnelConfiguration instance = new TunnelConfiguration();
		
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
