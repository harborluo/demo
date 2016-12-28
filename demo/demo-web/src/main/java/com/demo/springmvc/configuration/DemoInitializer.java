package com.demo.springmvc.configuration;

import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class DemoInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
	
	private Logger getLogger() {
		Logger log = LoggerFactory.getLogger(this.getClass());
		return log;
	}
 
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { DemoConfiguration.class };
    }
  
    @Override
    protected Class<?>[] getServletConfigClasses() {
    	return new Class[] {};
    }
  
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
    
    @Override
    protected Filter[] getServletFilters() {
    	Filter [] singleton = { new CORSFilter() };
    	return singleton;
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		// TODO Auto-generated method stub
		getLogger().debug("Root application context created.");
		return super.createRootApplicationContext();
	}
    
    
 
}