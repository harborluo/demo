package com.demo.springmvc.configuration;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.demo.component.ComponentRepository;
import com.demo.component.Table;
import com.demo.springmvc.service.JdbcService;

@Component
public class CustomizeApplicationListenser
/* implements ApplicationListener<ContextRefreshedEvent> */{

	private Logger getLogger() {
		Logger log = LoggerFactory.getLogger(this.getClass());
		return log;
	}

	// @Override
	// public void onApplicationEvent(ApplicationEvent event) {
	//
	// getLogger().info("----------------------------------------------"+event.getClass().getName());
	//
	// if(event instanceof ContextStartedEvent){
	// getLogger().info("Web application started.");
	// }else if (event instanceof ContextStoppedEvent){
	// getLogger().info("Web application stopped.");
	// }
	// }

	// @Override
	// public void onApplicationEvent(ContextRefreshedEvent event) {
	// // TODO Auto-generated method stub
	// getLogger().info("------------------------- Web application started.");
	//
	// }
	
	@Autowired
	JdbcService jdbcService;

	private boolean startedEventFired = false;
	
	private boolean stoppedEventFired = false;

	@EventListener({ ContextStartedEvent.class, ContextRefreshedEvent.class })
	void ContextStartedEvent(ApplicationEvent event) {

		if (startedEventFired == true) {
			return;
		}
		
	    ComponentRepository.getInstance().initialize();

	    Set<String> createdTables = jdbcService.getAllTableNames();
		getLogger().debug("{} tables found.", createdTables.size());
	    List<String> tablePathList = ComponentRepository.getInstance().getTablepathlist();
	    for(String path : tablePathList){
	    	getLogger().debug(path);
	    	Table tab = (Table) ComponentRepository.getInstance().getComponentByPath(path);
//	    	getLogger().debug("table is empty? " + (tab==null));
	    	String tableName = tab.getProperty("table-name").toUpperCase();
	    	if(createdTables.contains(tableName)==false){
	    		String ddl = tab.generateDDL();
	    		jdbcService.doExecuteSQL(ddl);
	    		getLogger().info("Table '{}' created: \n{}", tableName, ddl);
	    		continue;
	    	}
	    	
	    }
		
		getLogger().info("Web application started.");
		
		startedEventFired = true;
	}

	@EventListener({ ContextStoppedEvent.class })
	void ContextStoppedEvent() {
		
		if(stoppedEventFired){
			return;
		}
		 ComponentRepository.getInstance().destroy();
		getLogger().info("Web application stopped.");
		
		stoppedEventFired=true;
	}

}
