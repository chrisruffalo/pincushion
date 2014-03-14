package com.github.chrisruffalo.multitunnel.web.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

import org.reflections.Reflections;

import com.github.chrisruffalo.multitunnel.web.ManagementServer;

public class ManagementApplication extends Application {

	private final Set<Class<?>> types;
	
	private final Set<Object> singletons;
	
	public ManagementApplication() {
		// create markers
		final Set<Class<? extends Annotation>> markers = new HashSet<>();
		
		markers.add(GET.class);
		markers.add(PUT.class);
		markers.add(POST.class);
		markers.add(DELETE.class);
		markers.add(Path.class);
		markers.add(Produces.class);
		markers.add(Consumes.class);
		
		// scan local package for REST services
		String packageName = ManagementServer.class.getName();
		packageName = packageName.substring(0, packageName.lastIndexOf(".") - 1);
		
		// create reflective scanner for given package
		Reflections reflections = new Reflections(packageName);
		
		// scan and add types
		this.types = new HashSet<>();
		this.types.addAll(ManagementApplication.types(reflections, markers));
		this.types.addAll(ManagementApplication.methods(reflections, markers));

		// create singletons
		this.singletons = new HashSet<>();
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		return this.types;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return this.singletons;
	}

	private static Set<Class<?>> types(Reflections reflections, Set<Class<? extends Annotation>> markers) {
		
		Set<Class<?>> found = new HashSet<>();
		
		for(Class<? extends Annotation> type : markers) {
			found.addAll(reflections.getTypesAnnotatedWith(type, true));
		}
		
		return found;
	}
	
	private static Set<Class<?>> methods(Reflections reflections, Set<Class<? extends Annotation>> markers) {

		Set<Class<?>> found = new HashSet<>();
		
		for(Class<? extends Annotation> type : markers) {
			Set<Method> methods = reflections.getMethodsAnnotatedWith(type);
			for(Method method : methods) {
				found.add(method.getDeclaringClass());
			}
		}
		
		return found;
	}

}
