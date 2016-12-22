package com.websystique.springmvc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractController {

	public Logger getLogger(){
		Logger log = LoggerFactory.getLogger(this.getClass());
		return log;
	} 
}
