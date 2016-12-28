package com.demo.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.demo.utils.Utils;

/**
 * main view component, attributes accepted as follow:
 * cols - define the column of main view
 * filter - implicit filter for main view
 * orderby - define default order by columns, split by comma, such as NAME,TIER ...
 * orderdir - define default order by columns' direction, split by comma, such as DESC,ASC ... 
 * showheaders - switch for display column header or not, validate values is {1|0}, default is 1
 * showcount - switch for display page count or not, validate values is {1|0}, default is 1
 * dataobj - define source table of main view
 * numrows - page size of main view
 * caninsert - switch for "Add New Row" button, validate values is {1|0}, default is 0
 * canedit - switch for row edit function, validate values is {1|0}, default is 0
 * canupdate - switch for "Edit Row" button, validate values is {1|0}, default is 0
 * candelete - switch for "Delete Selected Row" button, validate values is {1|0}, default is 0
 * canEnablePrivs - switch for "Enable Privilege for Role Row" button, validate values is {1|0}, default is 0 
 * canfilter - switch for "Filter" button, validate values is {1|0}, default is 1
 * cansort - switch for data sorting, validate values is {1|0}, default is 1
 * cansearch - switch for "Chart Data" button, validate values is {1|0}, default is 0
 * confirmdelete - prompt message before delete action.
 * showgroupheaders - switch to show group headers
 * canmultirowedit - switch for "Edit Multiple Row" button, validate values is {1|0}, default is 0
 * showid - switch to show row id of source table, validate values is {1|0}, default is 0
 * multiroweditcols - define multiple edit columns, split by comma
 * editcols - define row edit columns, split by comma
 * myRecords - present entries for users who is a "newFingerprintDeveloper" role
 * add-row-window
 * edit-row-window
 * pop-window-width
 * column-labels
 * group-header-labels
 * column-labels 
 * @author harbor
 *
 */
public class Page extends AbstractComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6025431593202137659L;


	
	private Table table = null;
	
	private List<QueryField> fields = new ArrayList<QueryField>();
	
	private Set<String> condiction = new HashSet<String>(); 
	
	private Set<String> refTables = new HashSet<String>();
	
	private List<String[]> header = new ArrayList<String[]>();
	
    
    public void validate(){
    	    	
    	table = getTargetTable();
    	    	
    	if(table==null){
    		getLogger().error("Table with path: " + getProperty("table") + " has not been defined yet!");
    		return;
    	}
    	
    	if(getProperty("columns") == null){
    		
    		StringBuffer cols = new StringBuffer();
    		//set default columns if "columns" attribute is empty
    		List<AbstractComponent> columns = table.getChildren();
    		for(AbstractComponent column : columns){
    			if("1".equals(column.getProperty("hidden")) || column.getName().matches("^(RID|LMDATE|LMUSR|CREATEDATE)$")){
    				continue;
    			}
    			cols.append(column.getName()).append(",");
    		}
    		
    		List<TableLink> sources = table.getSource();
    		for(TableLink source : sources){
    			if(source.getProperty("table").equals(table.getPath())&& !source.getProperty("parent").equals(source.getProperty("table"))){
    				//Table parentTable = (Table) ComponentCache.getComponentByPath(soruce.getProperty("src"));
    				//String prefix = source.getProperty(key)
    				if(source.getProperty("columns")!=null){
    					String[] temp = source.getProperty("columns").split(",");
    					for(String t:temp){
    						cols.append(source.getTargetTable().getName().toUpperCase()).append("_").append(t).append(",");
    					}
    				}else{
    					
    				}
    			}
    		}
    		
    		getLogger().debug("Auto fill up columns attribute '{}'",cols.toString());
    		
    		if(cols.length()>0){
    			setProperty("columns", cols.toString().replaceAll(",$", ""));
    		}
    		
    	}
    	
    		
    	String[] cols = getProperty("columns").split(",");
    	boolean editable = "1".equals(getProperty("can-edit"));
    	
    	String editCols = getProperty("edit-columns");
    	if(editCols==null){

    		StringBuffer sb = new StringBuffer("^(");
    		
    		if (table != null) {
    			// Get columns of object data table
    			List<AbstractComponent> columns = table.getChildren();
        		for(AbstractComponent column : columns){
        			if("1".equals(column.getProperty("hidden")) || column.getName().matches("^(RID|LMDATE|LMUSR|CREATEDATE)$")){
        				continue;
        			}
        			sb.append(column.getName()).append("|");
        		}
        		// Get correlate column of parent Tables
        		List<TableLink> sources = table.getSource();
        		for(TableLink source : sources){
        			if(source.getProperty("child").equals(table.getPath())){
        				if(source.getProperty("column-name")!=null){
        					sb.append(source.getProperty("column-name")).append("|");
        				}
        			}
        		}
    		}
    		
            editCols = sb.toString().replaceAll("\\|$", "") + ")$";
    	}else{
    		editCols = "^(" + editCols.replaceAll(",", "|") + ")$";
    	}
    	
    	for(String col : cols){
    		
    		Column c = table.getColumnByName(col);
    		
    		
    		if( c!=null ){
    			
    			fields.add( new QueryField(c, table.getProperty("table-name"), table.getName() , col, table.getLabel(), (editable && c.getName().matches(editCols)) ? "true" : "false") );
    			continue;
    			
    		}
    			
    		Column refCol = table.getReferenceColumnByName(col);
    		
    		if(refCol!=null){
    			fields.add( new QueryField(refCol, table.getProperty("table-name"), table.getName() , col, table.getLabel(), (editable && refCol.getName().matches(editCols)) ? "true" : "false" ) );
    			continue;
    		}
    		
    		List<TableLink> steps = new ArrayList<TableLink>();
    		
    		if(fetchColumnFromParent(null, table, col, steps, (editable && col.matches(editCols)) ? "true" : "false")){
    			
    			for(TableLink src : steps){
    				Table srcTable = (Table) ComponentRepository.getInstance().getComponentByPath(src.getProperty("parent")) ;
    				Table targetTable = (Table) ComponentRepository.getInstance().getComponentByPath(src.getProperty("child")) ;
    				
    				refTables.add(srcTable.getProperty("table-name")+ " " + src.getProperty("parent-alias"));
    				
    				if(!targetTable.getPath().equals(this.table.getPath())){
    					refTables.add(targetTable.getProperty("table-name")+ " " + src.getProperty("child-alias"));
    				}
    				
    				condiction.add(src.getProperty("parent-alias")+"." + Column.IS_LIVE + " = 1 and " 
    						+ src.getProperty("parent-alias") + "." + Column.RID + " = " + src.getProperty("child-alias") + "." + src.getProperty("_column-name"));
    				
    			}
    			
    			continue;
    		}
    		
    		boolean found = false;
    		for(AbstractComponent src : getChildren()){
    			if(src.getName().equals(col) && src instanceof QueryFieldSrc ){
    				fields.add(new QueryField((QueryFieldSrc)src));
    				found = true;
    				continue;
    			}
    		}
    		
    		if(found) continue;
    		
    		getLogger().warn("Can't find column [" + col + "] in table [" + table.getPath() + "] for "+this.getClass().getName().replaceAll("com.bdna.components.", "")+" with path [" + getPath() + "].");
    		
    	}
    	
    	//fixed for Bug 25505 - QA View: Mapped Hardware Lifecycle
    	for(AbstractComponent src : getChildren()){
    		if(src instanceof QueryFieldSrc){
    			if(getFieldByAlias(src.getName())!=null)
    			  getFieldByAlias(src.getName()).setSrcColumn(src.getProperty("src"));
    		}
    	}
    	
        
        String sql = "select ";
      //Bug 27528 - 'To-Be-Deleted' Flag is Not Cascaded from Products to Releases
//        String select_statement = table.getAdminColumn();
        String select_statement = "";
        
        int aliasOverLimitedCnt = 0;
        //select_statement += ",\n  " + f.getColumnNameForSelect() + " " + (f.getColumnAlias().length() < 30 ? f.getColumnAlias() : f.getColumnAlias().substring(0, 28)+ Integer.toString(aliasOverLimitedCnt));
        for(QueryField f :fields){        
        	if (f.getColumnAlias().length() < 30) {
        		select_statement += ",\n  " + f.getColumnNameForSelect() + " " + f.getColumnAlias();
        	} else {
        		select_statement += ",\n  " + f.getColumnNameForSelect() + " " + f.getColumnAlias().substring(0, 28)+ Integer.toString(aliasOverLimitedCnt);
            	aliasOverLimitedCnt ++;
        	}
        }
        
        sql += "\n from " + table.getProperty("table-name") + " " + table.getName();
        
        Iterator<String> it = refTables.iterator();
        while(it.hasNext()){
        	sql += ",\n  " + it.next();
        }
        
        sql += "\n where " + table.getName()+"." + Column.IS_LIVE + " = 1 and " + table.getName() + "." + Column.RID + " != -1 ";
        
        Iterator<String> it2 = condiction.iterator();
        while(it2.hasNext()){
        	sql += "\n  and " + it2.next();
        }
        
        if(getProperty("filter")!=null){
        	sql += "\n  and " + getProperty("filter");
        }
        
        if(getProperty("srcfilter")!=null){
        	sql += "\n and " + getProperty("srcfilter");
        }

        setProperty("sql-select-statement", sql.replaceAll("^select", "select " + table.getAdminColumn() +  select_statement));

        setProperty("chart-sql-select-statement", sql.replaceAll("^select", "SELECT_STATEMENT"));     
    	
        if("1".equals(getProperty("showgroupheaders"))){
            if(getProperty("group-header-labels")==null){
            	String current = null;
            	String previous = null;
            	int colspan = 0;
            	
            	for(int i=0; i<fields.size();i++){
            		
            		QueryField f = fields.get(i);
            		
            		if(i > 0){
            			previous = fields.get(i-1).getHeaderLabel();
            		}
            		
            		current = f.getHeaderLabel();
            		
            		if(current.equals(previous)){
            			colspan = colspan + 1;
            		}else{
            			if(previous!=null){
            				this.header.add(new String[]{previous, "" + colspan});
            			}
            			//reset counter
            			colspan = 1;
            			previous = current;
            		}
            		
            	}
            	
            	this.header.add(new String[]{previous, "" + (colspan )});
            }else{
            	String[] labels = getProperty("group-header-labels").split(",");
            	for(String label : labels){
            		this.header.add(label.split(":"));
            	}
            	
            	List<String> list = new ArrayList<String>();
            	for(String [] headStr:header){
            		for(int i=0;i<Integer.parseInt(headStr[1]);i++){
            			list.add(headStr[0]);
            		}
            	}
            	
            	int i=0;
            	for(QueryField f: fields){
            		if("hidden".equals(f.getWidget())){
            			continue;
            		}
            		f.setHeaderLabel(list.get(i));
            		i++;
            	}
            }
            
        }
        
        String orderby = getProperty("orderby");
        
        if(Utils.isEmpty(orderby)==false){

        	String[] temp = orderby.split(",");
        	String new_order_by = "";
        	String[] direction = new String[temp.length];
        	if(getProperty("orderdir")!=null){
        		direction = getProperty("orderdir").split(",");
        	}else{
        		for(int i=0;i<direction.length;i++){
        			direction[i] = "ASC";
        		}
        	}
        	
        	for(int i=0;i<temp.length;i++){
        		boolean orderByColumnFound = false;
        		for(QueryField f :fields){
        			
        			sql += ",\n  " + f.getColumnNameForSelect() + " " + f.getColumnAlias();
        			if(f.getColumnAlias().equals(temp[i])){
        				new_order_by += f.getColumn() + " " + direction[i] + ", ";
        				orderByColumnFound = true;
        				break;
        			}
        			
        		}
        		
        		if(table.hasColumn(temp[i])){
        			//convert RID to "_rid"
        			temp[i] = temp[i].equals("RID") ? "\"_rid\"":temp[i];
        			new_order_by += table.getName() + "." + temp[i] + " " + direction[i] + " nulls last, ";
        			orderByColumnFound = true;
        		}
        		
        		if(orderByColumnFound==false){
        			getLogger().warn("Page with path [" + getPath()+"] has invalidate order by column definition [" + temp[i] + "].");
        		}
        		
        	}
        	
        	new_order_by = new_order_by.replaceAll(", $", "");
        	if(Utils.isEmpty(new_order_by)==false){
        		setProperty("new-orderby", new_order_by);
        	}else{
        		//clear invalidate order by property
        		setProperty("new-orderby", null);
        	}
        }
        
        String columnLabels = getProperty("column-labels");
        if(columnLabels!=null){
        	String[] labels = columnLabels.split(",");
        	for(int i=0;i<labels.length;i++){
        		fields.get(i).setLabel(labels[i]);
        	}
        }
        
        
        //force to disable page row number count 
       //this.setProperty("showcount", "0"); 
    }
    
    public Table getTargetTable(){
    	
//    	String path = getProperty("table");
    	
//    	getLogger().debug("target table path is '{}'.", path);
//    	AbstractComponent comp = ComponentRepository.getInstance().getComponentByPath(path);
    	return (Table) ComponentRepository.getInstance().getComponentByPath(getProperty("table"));
    }
    
    private int srcAliasIndex = 0;
    
    private boolean fetchColumnFromParent(String prefix, Table table, String col, List<TableLink> steps, String editable){
    	
       	List<TableLink> sources = table.getSource();
    	
       for(TableLink src : sources){
    	   
//    	   System.out.println(src.getProperty("column-name"));
    	   
    	   String alias = src.getProperty("_column-name").replaceAll("_RID$", "");
    	   
    	   if(col.startsWith(alias + "_")){
    		   Table parent = (Table) ComponentRepository.getInstance().getComponentByPath(src.getProperty("parent"));
    		   Column column = (parent.getColumnByName(col.replaceAll(alias + "_", "")));
    		   
    		   String srcAlias = (prefix==null ? "" : prefix + "_" ) + alias.toLowerCase();
    		   
    		   //ORA-00972: identifier is too long
    		   if(srcAlias.length()>30){
    			   srcAlias = srcAlias.substring(0,20)+"_"+ srcAliasIndex;
    			   srcAliasIndex++;
    		   }
    		   
    		   src.setProperty("parent-alias", srcAlias);
    		   src.setProperty("child-alias", prefix==null ? table.getName() : prefix);
    		   
    		   steps.add(src);
    		   
    		   if(column!=null){
    			   fields.add(new QueryField(column, 
    					      parent.getProperty("tablename"),
    					      srcAlias,
    					      (prefix==null ? "" : prefix + "_" ).toUpperCase() + col, 
    					      // steps.get(0).getLabel(),  editable)); only first level of parent table's column is allow cell edit
    					      steps.get(0).getLabel(), steps.size()>1 ? "false" : editable));
    			   
    			   return true;
    		   }
    		       		   
    		   String target_col_name = col.replaceAll("^" + alias + "_", "");
    		   
    		   return fetchColumnFromParent(srcAlias, parent, target_col_name, steps, editable);
    		   
    	   }
       }
    	
    	return false;
    	
    }

	public List<String[]> getHeader() {
		return header;
	}

	@Override
	public String getValidateAttributes() {
		return "^()$";
	}
	
	/**
	 * 
	 * @param cols
	 * @param columnLables
	 * @param headerLabels
	 * @param settingRID
	 */
	public void reset(String cols, String columnLables, String headerLabels, String settingRID){
		setProperty("comp.id", settingRID);
		setProperty("cols", cols);
		setProperty("column-labels", columnLables);
		setProperty("group-header-labels", headerLabels);
//		this.fields.clear();
		this.header.clear();
		this.refTables.clear();
		this.condiction.clear();
		validate();
	}
	
	public QueryField getFieldByAlias(String alias){
		
		for(QueryField f:fields){
			if(f.getColumnAlias().equals(alias)){
				return f;
			}
		}
		
		return null;
	}
	 
	public static void main(String[] args){
//		try{
//			
//			CompCache.load();
//			Page page = (Page) CompCache.getComponentByPath("bdna.data.relation.addRemove_orgs");
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
		
		//Page page = (Page) CompCache.getComponentByPath("bdna.data.relation.vendorPriceLev_priceLev");
//		Page page = (Page) CompCache.getComponentByPath("bdna.content.prod");
		
		
		//for(QueryField f : page.getFields()){
		//	System.out.println(f.getHeaderLabel());
		//}
//		System.out.println(page.getProperty("sql-select-statement"));
		//System.out.println("---------------------------------------");
		//System.out.println(page.getProperty("orderby"));
		
//		for(String[] head : page.getHeader()){
//			System.out.println(head[0] + " = " + head[1]);
//		}
		
		
	}
	
}
