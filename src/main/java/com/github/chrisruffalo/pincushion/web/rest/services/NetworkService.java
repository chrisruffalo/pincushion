package com.github.chrisruffalo.pincushion.web.rest.services;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.chrisruffalo.pincushion.util.InterfaceHelper;
import com.github.chrisruffalo.pincushion.util.PortHelper;

@Path("/network")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NetworkService {
	
	@Path("/checkInterface")
	@POST
	public Map<String, String> checkInterface(Map<String, String> values) {
		// get interface
		String interfaceName = values.get("interfaceName");
		if(interfaceName == null || interfaceName.isEmpty()) {
			interfaceName = "0.0.0.0";
		}		
		String saneInterfaceName = InterfaceHelper.INSTANCE.sanitize(interfaceName);
		
		Map<String,String> message = new HashMap<>();
		
		// send a message if the interface changes
		if(!interfaceName.equalsIgnoreCase(saneInterfaceName)) {
			message.put("message", String.format("The interface '%s' will be resolved to '%s'", interfaceName, saneInterfaceName));
		}
		
		// compose the rest of the response
		message.put("originalInterface", interfaceName);
		message.put("saneInterface", saneInterfaceName);
		
		return message;
		
	}

	@Path("/{port}/available")
	@POST
	public Map<String, String> getPortAvailable(@PathParam("port") Integer port, Map<String, String> values) {
		// get interface
		String interfaceName = values.get("interfaceName");
		if(interfaceName == null || interfaceName.isEmpty()) {
			interfaceName = "0.0.0.0";
		}		
		interfaceName = InterfaceHelper.INSTANCE.sanitize(interfaceName);
		
		// check availability
		boolean result = PortHelper.INSTANCE.available(interfaceName, port);
		Map<String,String> message = new HashMap<>();
		message.put("result", String.valueOf(result));
		return message;
	}
	
}
