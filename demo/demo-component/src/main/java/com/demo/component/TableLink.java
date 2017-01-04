package com.demo.component;

import java.util.List;


public class TableLink extends Page {

	/**
	 * 
	 */
//	private static final long serialVersionUID = -177431807399270248L;

	@Override
	public Table getTargetTable() {
		return (Table) ComponentRepository.getInstance().getComponentByPath(getProperty("child"));
	}
	
	public Table getChildTable() {
		return (Table) ComponentRepository.getInstance().getComponentByPath(getProperty("child"));
	}
	
	public String getSelectLabelSql(){
		String sql  = getProperty("sql-select-no-filter");//getProperty("sql-select-statement");
		String srcTableAlias =  getTargetTable().getName();
		return sql += " and " + srcTableAlias + "." + Column.RID + " = ?";
	}

	@Override
	public String getValidateAttributes() {
		return "^(src|target|cols|filter|collabels|ondelete|required|column-name|alias|allow-insert|showid|srcrowfilter|clearcols|child-columns|parent-columns|srcfilter|readonly|target-column-name|orderby|pop-win-width|add-row-window|orderdir|self-link|column-labels|ignore-fixed-param|onchange|custom-column|ignore-empty-parent|depend-column|show-red-major|showIgnoreCase|perSrcRowFilter|pubSrcRowFilter|per-add-row-window|pub-add-row-window)$";
	}

	@Override
	public void validate() {
		
		if(getProperty("on-delete")==null){
			setProperty("on-delete", "clear");
		}
		
		if(getProperty("on-delete").matches("^(clear|delete)$")==false){
			getLogger().warn("Invalidate ondelete attribute specified for table src with path ["+getPath()+"].");
		}
		
        if(getProperty("columns") == null){
    		
    		StringBuffer cols = new StringBuffer();
    		//set default columns if "cols" attribute is empty
    		
    		AbstractComponent comp = ComponentRepository.getInstance().getComponentByPath(getProperty("table"));
    		
    		List<AbstractComponent> columns = comp.getChildren();
    		for(AbstractComponent column : columns){
    			if("1".equals(column.getProperty("hidden")) || column.getName().matches("^(RID|LMDATE|LMUSR|CREATEDATE)$")){
    				continue;
    			}
    			cols.append(column.getName()).append(",");
    		}
    		
    		List<TableLink> sources = getTargetTable().getSource();
    		for(TableLink source : sources){
    			if(source.getProperty("table").equals(getTargetTable().getPath())&& !source.getProperty("table").equals(source.getProperty("parent"))){
    				
    				if(source.getProperty("columns")!=null){
    					String[] temp = source.getProperty("columns").split(",");
    					for(String t:temp){
    						cols.append(source.getTargetTable().getName().toUpperCase()).append("_").append(t).append(",");
    					}
    				}
    			}
    		}
    		
    		if(cols.length()>0){
    			setProperty("columns", cols.toString().replaceAll(",$", ""));
    		}
    		
    	}

        super.validate();

	}

	public static void main(String[] args){
//		CompCache.load();
//		System.out.println(CompCache.getComponentByPath("bdna.data.relation.rels_support").getProperty("srcrowfilter"));
	}
	
}
