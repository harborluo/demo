package com.demo.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractComponent implements Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -1288115737975505702L;
	
	private AbstractComponent parent;
	
	public AbstractComponent getParent() {
		return parent;
	}

	public void setParent(AbstractComponent parent) {
		
		if(parent == null || parent instanceof Folder){
			return;
		}
		this.parent = parent;
	}
	
	private List<AbstractComponent> children = new ArrayList<AbstractComponent>();

	public List<AbstractComponent> getChildren() {
		return children;
	}

	public void addChildren(AbstractComponent child) {
		this.children.add(child);
	}

	private String name, label, path;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private Map<String,String> props = new HashMap<String, String>();
	
	public String getProperty(String key){
		return props.get(key);
	}
	
	public void setProperty(String key, String value){
		
		getLogger().debug("Component with path '{}' set property '{}' to '{}'.",getPath(), key, value);
		
		props.put(key, value);
	}
	
	public boolean isAdditionalProperty(String propertyName){
		return !propertyName.matches("^(name|label)$");
	}
	
	public abstract void validate();
	
	/**
	 * 
	 * @return
	 */
	public abstract String getValidateAttributes();
	
	public Logger getLogger(){
		return LoggerFactory.getLogger(this.getClass());
	}
	
	public void validateAttribute(){
		Iterator<String> keys = this.props.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			if(key.matches(getValidateAttributes())==false){
				getLogger().warn("Component with path '{}' does not support attribute '{}'.", getPath(), key);
			}
		}
	}
	
	public boolean isCache(){
		return true;
	}
   
}
