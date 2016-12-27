package com.demo.component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ComponentRepository {
	
	private Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}
	
	private static final ComponentRepository instance = new ComponentRepository();
	
	private static List<String> tablePathList = new ArrayList<String>();
	
	public static ComponentRepository getInstance() {
		return instance;
	}

	private Map<String,AbstractComponent> cache = new HashMap<String, AbstractComponent>();
	
	private ComponentRepository(){
//		initialize();
	}
	
	public void initialize(){
    	
    	SAXReader saxReader = new SAXReader();

		Document document = null;

		Reader reader = null;
		
		try {
			
			reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/config.xml")));
			document = saxReader.read(reader);
			
			Element root = document.getRootElement();
			
//			String path = root.getName();
			
			List<Element> children = root.elements();
			for(Element child : children){
				createComponent("", child, null);
			}
			reader.close();

		} catch (DocumentException e) {
//			e.printStackTrace();
			getLogger().error(e.getMessage(), e);
		} catch (IOException e) {
//			e.printStackTrace();
			getLogger().error(e.getMessage(), e);
		}
		
	}

	private AbstractComponent createComponent(String parentPath, Element e, AbstractComponent parent) {
		
		String path =  parentPath==null||parentPath.length()>0 ? parentPath + "." : "";
		
    	AbstractComponent comp = null;
    	
    	try{
    		String tagName = e.getName();
    		
    		String name = e.attributeValue("name");
    		
    		switch (tagName) {
    		case "folder":{
    			comp = new Folder();
    		};
    		break;
    		case "table":{
    			comp = new Table();
    		};
    		break;
    		case "column":{
    			comp = new Column();
    		};
    		break;
    		case "table-link" : {
    			
    			comp = new TableLink();
    			String columnName = "";
        		
        		if(name==null||name.length()==0){
        			
        			if(getComponentByPath(e.attributeValue("parent"))==null || getComponentByPath(e.attributeValue("child")) == null){
        				getLogger().error("Invalidate table-link entry found: parent[" + e.attributeValue("parent")+"], table["+e.attributeValue("child")+"].");
        			} else {
        				columnName =  getComponentByPath(e.attributeValue("parent")).getName()  +"_rid";
        			}
        			
        		}else{
        			columnName =  name  +"_rid";
        		}
        		
        		name = getComponentByPath(e.attributeValue("child")).getName()+"." + columnName.toUpperCase();
        		
//        		path += name;
//            	comp.setName(name);
//            	comp.setPath(path);
        		
        		comp.setProperty("_column-name", columnName.toUpperCase());        		
        		comp.setProperty("alias", columnName.toUpperCase().replaceAll("\\_RID$", ""));
        		
        		Table targetTable = (Table) getComponentByPath(e.attributeValue("child"));
        		Column targetColumn = new Column();
        		targetColumn.setName(columnName.toUpperCase());
        		targetColumn.setLabel(e.attributeValue("label"));
        		targetColumn.setProperty("data-type", "number");
        		targetColumn.setProperty("default", "-1");
        		targetColumn.setProperty("required", e.attributeValue("required"));
        		targetColumn.setProperty("filter-widget", "text");
        		targetTable.addReferenceChildren(targetColumn, (TableLink) comp);
        		
    		}break;
    		default:{
    		  getLogger().error("Unsupport component found '{}'.", tagName);
    		}break;
    		
    		}
    		
    		path += name;
        	comp.setName(name);
        	comp.setPath(path);
        	comp.setParent(parent);
        	
        	//write other attributes into component
        	List<Attribute> attrs = e.attributes();
        	for(Attribute attr : attrs){
        		if(comp.isAdditionalProperty(attr.getName())){
        			comp.setProperty(attr.getName(), attr.getValue());
        		}
        	}
    		
        	//process child component
    		List<Element> children = e.elements();
    		for(Element c : children ){
    			comp.addChildren(createComponent(path, c, comp));
    		}
    		
    		comp.validate();
    		
    		if(cache.get(path)==null){
        		if(comp.isCache()==true){
        			getLogger().debug("Component with path '{}' added to cache.", path);
        			cache.put(path, comp);
        			
        			if(comp instanceof Table){
        				this.tablePathList.add(path);
        			}
        			
        		}
        	}else{
        		getLogger().error("Duplicate component entry found: " + path + " of type " + tagName);
        	}
    		
    	}catch(Exception ex){
    		getLogger().error("Fail to compile component with path '{}'", comp.getPath());
    		getLogger().error(ex.getMessage(), ex);
    	}
    	
    	return comp;
    	
	}
	
	

	public List<String> getTablepathlist() {
		return tablePathList;
	}

	public AbstractComponent getComponentByPath(String path) {
		
		return cache.get(path);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ComponentRepository.getInstance().initialize();
	}

	public void destroy() {
		cache.clear();		
	}

}
