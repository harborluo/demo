package com.demo.component;


public class QueryField extends Column {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6718285820817505821L;
	
	private String tableAlias, columnAlias, headerLabel,label;
	private Column column;
	private String editable = "false";
	private String srcColumn = null;
//	private boolean isMapping = false;
	private String widget = "text";
	private String datatype = "string";
	private String values;
	private String labels;
	private String tableName;
	
	public QueryField(Column col, String tableName, String tableAlias, String columnAlias, String headerLabel,String editable){
		this.column = col;
		this.tableAlias = tableAlias;
		this.tableName = tableName;
		this.columnAlias = columnAlias;
		this.headerLabel = headerLabel;
		this.editable = editable;
		this.label = col.getLabel();
		this.widget = col.getProperty("filterwidget");
		this.datatype = col.getProperty("datatype");
//		if(column!=null){
//			this.isMapping = ((Table) column.getParent()).isMappingColumn(column.getName());
//		}
		this.labels = col.getProperty("valuelabels");
		this.values = col.getProperty("values");
		
	}
	
	
	
	public void setHeaderLabel(String headerLabel) {
		this.headerLabel = headerLabel;
	}

	public QueryField(QueryFieldSrc virtualColumn){
		//this.column = virtualColumn;
		this.label = virtualColumn.getLabel();
		this.srcColumn = virtualColumn.getProperty("src");
		this.columnAlias = virtualColumn.getName();
		this.datatype = virtualColumn.getProperty("datatype");
		this.headerLabel = virtualColumn.getProperty("header-label");
		this.widget = virtualColumn.getProperty("filter-widget");
		this.labels = virtualColumn.getProperty("valuelabels");
		this.values = virtualColumn.getProperty("values");
		
		if(this.widget==null){
			this.widget = "text";
		}
	}
	
    public void setSrcColumn(String srcColumn) {
		this.srcColumn = srcColumn;
	}

	public String getColumnNameForSelect() {
		
		String temp = "";
		
		if(this.srcColumn!=null){
			temp = this.srcColumn;
		}else{
			temp = tableAlias + "." + (column.getName().equals("RID") ? Column.RID  : column.getName());
		}
    	
		if(this.values!=null && this.labels!=null){
			String[] values = this.values.split(",");
			String[] labels = this.labels.split(",");
			
			temp = "decode(" + temp;
			
			for(int i=0;i<values.length;i++){
				temp += ", '" + values[i] + "', '" + labels[i] +"'";
			}
			temp+=")";   
			   
		}
		
		if(column!=null){
			
			if("RID".equals(column.getName())){
				return "decode(" + tableAlias + "." + Column.RID + ",'-1',null, " + tableAlias + "." + Column.RID + ")";
			}
			
			if("LMDATE".equals(column.getName())){
				return "to_char(" + tableAlias + "." + Column.CREATE_DATE + ",'MM/DD/YYYY HH24:MI')";
			}
			
			if("LMUSR".equals(column.getName())){
				return  tableAlias + "." + Column.OP_USER ;
			}
			if("CREATEDATE".equals(column.getName())){
				return "to_char( (select min("+ Column.CREATE_DATE +") from  " +this.tableName+" t where t."+ Column.RID +"=" +tableAlias+ "." +Column.RID+" group by t."+ Column.RID +"),'MM/DD/YYYY HH24:MI')";
			}
			if(column.getProperty("ref-table-src")!=null){
				return "decode(" + tableAlias + "." + column.getName() + ",'-1',null, " + tableAlias + "." + column.getName() + ")";
			}
			
			if("date".equals(column.getProperty("datatype"))){
				String format = column.getProperty("format")==null ? DEFAULT_DATA_FORMAT : column.getProperty("format");
				return "to_char(" + tableAlias + "." + column.getName() + ", '" + format + "')";
			}
			
			if("PASSWORD".equals(column.getName())){
				return "'***'";
			}
			
		}
		
		return temp;
	}
    
    public String getSrcColumn(){
    	return tableAlias + "." + column.getName();
    }

//	public String getTableName() {
//		return tableName;
//	}

//	public String getTableAlias() {
//		return tableAlias;
//	}

	public String getColumnAlias() {
		return columnAlias;
	}
	
	public String getHtml(){
	 
      String widget = this.widget;
    
      if(widget==null||widget.length()==0){
    	  widget = "text";
      }
		
      String html = "";
		
		try{
			
			if("text".equals(widget)){
				html = "<input class='formInput' type='text' size='15' name=" + columnAlias + " value='' maxlength='500' datatype='" + this.datatype + "'>";
			}else if("Selectlist".equals(widget)){
				
				if(this.values!=null){
					html = "<select class='formSelect' name='" + columnAlias + "' datatype='" + this.datatype + "'>";
					html += "<option value='' label=''></option>";
					
					html += "<option value='is null' label='is null'>is null</option>";
					html += "<option value='is not null' label='is not null'>is not null</option>";
					
					String[] vals = this.values.split(",");
					String[] labs = this.labels==null ? vals : this.labels.split(",");
					for(int i=0;i<vals.length;i++){
						html += "<option value='" + vals[i] + "' label='" + labs[i] + "'>" + labs[i] + "</option>";
					}
				}else if(column!=null && column.getOptionSelectStatement()!=null){
					html = "<select class='formSelect' name='" + columnAlias + "' source='" + column.getPath() + "'>";
					html += "<option value='' label=''></option>";
					html += "<option value='is null' label='is null'>is null</option>";
					html += "<option value='is not null' label='is not null'>is not null</option>";
				}else{
					getLogger().warn("Component with path ["+(column!=null ? column.getPath():"")+"] has an empty values attribute.");
				}
				
				html += "</select>";
				
			}else if("hidden".equals(widget)){
				
			}else{
				getLogger().error("invalidate filter widget type ["+this.widget+"]");
			}
			
		}catch(Exception e){
			
			e.printStackTrace();
		}
				
		return html;
	}

    public String getWidget(){
    	return this.widget;
    }
    
    public String getAlias(){
    	return columnAlias;
    }
    
    public String getLabel(){
    	//return column.getLabel();
    	return this.label;
    }
    
    public void setLabel(String label) {
		this.label = label;
	}

	public String getColumn() {
		
		if(this.srcColumn!=null){
			return this.srcColumn;
		}
		
		if("RID".equals(column.getName())){
			return tableAlias + "." + Column.RID;
		}else if("LMDATE".equals(column.getName())){
			return tableAlias + "." + Column.CREATE_DATE;
		}else if("LMUSR".equals(column.getName())){
			return tableAlias + "." + Column.OP_USER;
		}else if("CREATEDATE".equals(column.getName())){
			return "to_char( (select min("+ Column.CREATE_DATE +") from  " +this.tableName+" t where t."+ Column.RID +"=" +tableAlias+ "." +Column.RID+" group by t."+ Column.RID +"),'MM/DD/YYYY HH24:MI')";
		}else{
			return tableAlias + "." + column.getName() ;
		}
		
	}

	public Object getDatatype() {
		return this.datatype;
	}

	public String getHeaderLabel() {
		return headerLabel;
	}

	public String getEditable() {
		return editable;
	}

	public boolean isCache() {
		return false;
	}
	
	public boolean isMappingColumn(){
		return column.getName().endsWith("RID");
	}
	
}
