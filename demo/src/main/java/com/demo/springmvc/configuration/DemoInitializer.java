package com.demo.springmvc.configuration;

import javax.servlet.Filter;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class DemoInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
 
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
    
    
 
}