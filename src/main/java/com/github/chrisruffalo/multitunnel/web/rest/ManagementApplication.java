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

	private final Set<Class<? extends Annotation>> MARKERS;

	private final Reflections REFLECTIONS;
	
	public ManagementApplication() {
		// create markers
		this.MARKERS = new HashSet<>();
		
		this.MARKERS.add(GET.class);
		this.MARKERS.add(PUT.class);
		this.MARKERS.add(POST.class);
		this.MARKERS.add(DELETE.class);
		this.MARKERS.add(Path.class);
		this.MARKERS.add(Produces.class);
		this.MARKERS.add(Consumes.class);
		
		// scan local package for REST services
		String packageName = ManagementServer.class.getName();
		packageName = packageName.substring(0, packageName.lastIndexOf(".") - 1);
		
		this.REFLECTIONS = new Reflections(packageName);

	}
	
	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();
		classes.addAll(super.getClasses());

		// get types by annotation at the class
		// and method level
		classes.addAll(this.types());	
		classes.addAll(this.methods());
		
		return classes;
	}
	
	private Set<Class<?>> types() {
		
		Set<Class<?>> found = new HashSet<>();
		
		for(Class<? extends Annotation> type : this.MARKERS) {
			found.addAll(this.REFLECTIONS.getTypesAnnotatedWith(type, true));
		}
		
		return found;
	}
	
	private Set<Class<?>> methods() {

		Set<Class<?>> found = new HashSet<>();
		
		for(Class<? extends Annotation> type : this.MARKERS) {
			Set<Method> methods = this.REFLECTIONS.getMethodsAnnotatedWith(type);
			for(Method method : methods) {
				found.add(method.getDeclaringClass());
			}
		}
		
		return found;
	}

}
