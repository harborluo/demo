package com.demo.component;



/**
 * table column definition for database, attributes supported as follow:
 * datatype : mandatory attribute; data type of the column, accepted value : {date|number|string|url|flexdate|datestring|alphanumber|regexpstring} 
 * size : length of column, can not be empty if datatype is set as {string|url|flexdate|datestring|alphanumber|regexpstring}
 * required : flag of column value is mandatory or not, should be value of {0|1}, default is 0
 * ref-select : component path that refer to public select component
 * editwidget :edit widget for create or update window, should be value of {Text|Selectlist|Checkbox|Radio|Textarea}
 * filterwidget : filter widget for main view, should be value of {Text|Selectlist}
 * values : attribute for options of Selectlist, Checkbox, Radio
 * valuelabels : attribute for options of Selectlist, Checkbox, Radio 
 * shownull : attribute for options of Selectlist, Checkbox, Radio
 * height : attribute for widget of Textarea
 * description : comment for the column
 * hidden : flag of is hidden filed or not, should be value of {0|1}, default is 0
 * default : default value of column
 * valuedataobj : options come from look-up table (component path) 
 * filter : additional filet condition for valuedataobj
 * valuecol : value column for valuedataobj
 * labelcol : label column for valuedataobj
 * format : date format 
 * readonly : flag of the column is readonly or not
 * validate-regexp : validate regular expression 
 * original : trim space before submit if value is 1,  should be value of {0|1}, default is 0
 * deprecated : indicate if a column is deprecated or not.
 *   added for Bug 28053 - Extra Spaces in Data
 * @author harbor
 *
 */
public class Column extends AbstractComponent {
	
	public static final String RID = "\"_rid\"";  
	public static final String INSTID = "\"_ver\"";
	public static final String CREATE_DATE = "\"_createdate\"";
	public static final String END_DATE = "\"_enddate\"";
	/**
	 * operate user
	 */
	public static final String OP_USER = "\"_src\"";
	public static final String IS_LIVE = "\"_live\"";
	public static final String CHANGED_COLS = "\"_srccols\"";
	
	private static final long serialVersionUID = -4987112626701096227L;
	public static final String DEFAULT_STRING_SIZE = "2000";
	public static final String DEFAULT_TEXT_SIZE = "2000";
	public static final String DEFAULT_URL_SIZE = "2000";
	public static final String DEFAULT_TYPE = "string";
	
	public static final String WIDGET_SELECT = "select";
	public static final String WIDGET_TEXT = "text";
	public static final String WIDGET_TEXTAREA = "textarea";
	public static final String WIDGET_RADIO = "radio";
	public static final String WIDGET_CHECKBOX = "radio";
	
	public static final String DATATYPE_REGEXP = "^(date|number|string|clob)$";
	
	public static final String NON_EDITABLE_REGEXP = "^(RID|LMDATE|LMUSR|CREATEDATE)$";
	
	public static final String DEFAULT_DATA_FORMAT = "MM/DD/YYYY" ;
	   
	public String getSql(){
		
		if(getParent()==null){
			return "";
		}
		
		String tableAlias = getParent().getName();
		
		String widget = getProperty("edit-widget");
		
		if(widget==null){
			return tableAlias + "." + getName();  
		}
		
		if("date".equals(getProperty("data-type"))){
			String format = getProperty("format")==null ? DEFAULT_DATA_FORMAT : getProperty("format");
			return "to_char(" + tableAlias + "." + getName() + ", '" + format + "')";
		}
		
		if(widget.matches("^(selectlist|radio)$") && getProperty("value-labels")!=null){
			String[] values = getProperty("values").split(",");
			String[] labels = getProperty("value-labels").split(",");
			
			String temp = "decode( " + tableAlias + "." + getName();
			
			for(int i=0;i<values.length;i++){
				temp += ",'" + values[i] + "' , '" + labels[i] +"'";
			}
			
			temp+=")";
			
			return temp;
			
		}
		
		return tableAlias + "." + getName();
		
	}


	@Override
	public void validate() {
		
		if(getProperty("edit-widget")==null){
			setProperty("edit-widget",WIDGET_TEXT);
		}
				
		if(getProperty("filter-widget")==null){
			if( WIDGET_SELECT.equals(getProperty("edit-widget"))){
				setProperty("filter-widget", WIDGET_SELECT);
			}else{
				setProperty("filter-widget", WIDGET_TEXT);
			}
		}
		
		String datatype = getProperty("data-type");
				
		if(getProperty("size")==null){
			
			String size = DEFAULT_STRING_SIZE;
			
			if("number".equals(datatype)){
				size = "15";
			}else if("date".equals(datatype)){
				size = "20";
			}
						
			setProperty("size", size);
			
		}
		
		if(datatype.matches(DATATYPE_REGEXP)==false){
			getLogger().error("Column with path ["+getPath()+"] has a invalidate datatype ["+datatype+"]");
		}
					
		if(getProperty("valuedataobj")!=null){
			if(getProperty("labelcol")==null){
				setProperty("labelcol", getProperty("valuecol"));
			}
			//String statement = "select " + getProperty("valuecol") + "as val, " + getProperty("labelcol") + "as lab from " + CompCache.getComponentByPath(getProperty("valuedataobj")) +" where is_live = 1 and \"_rid\" != -1";
			//setProperty("src-option-statement", statement);
		}
			
	}
	
	public String getOptionSelectStatement(){
		
		if(getProperty("valuedataobj")!=null && getProperty("src-option-statement")==null){
			String statement = "select " + getProperty("valuecol") + " as val, " + getProperty("labelcol") + " as lab from " 
			    + ComponentRepository.getInstance().getComponentByPath(getProperty("valuedataobj")).getProperty("tablename") +" where is_live = 1 and \"_rid\" != -1 ";
			
			if(getProperty("filter")!=null){
				statement += " and " + getProperty("filter");
			}
			
			statement += " order by lab";
			
			setProperty("src-option-statement", statement);
		}
		
		return getProperty("src-option-statement");
	}

	@Override
	public String getValidateAttributes() {
		return "^(datatype|size|required|ref-select|editwidget|filterwidget|values|valuelabels|shownull|height|description|hidden|default|valuedataobj|filter|valuecol|labelcol|format|readonly|validate-regexp|original|deprecated)$";
	}
	
	public String getDDL(){
		String ddl = getName();
		String datatype = getProperty("data-type");
		String stringPattern = "^(url|flexdate|string|datestring|alphanumber|regexpstring)$";
		if(datatype.matches(stringPattern)){
			ddl += " varchar2(" + getProperty("size") + ")";
		}else if("number".equals(datatype)){
			ddl += " number";
		}else if("date".equals(datatype)){
			ddl += " date";
		}else if("unicode-string".equals(datatype)){
			ddl += " nvarchar2(" + getProperty("size") + ")";
		}else if ("clob".equals(datatype)) {
			ddl += " clob";
		}else{
			getLogger().error("Component with path [" + getPath() + "] has unsupported datatype [" + datatype + "]");
		}
		
		if(getProperty("default")!=null){
			if(datatype.matches(stringPattern)){
				ddl +=" default '" + getProperty("default")+"'";
			}else{
				ddl +=" default " + getProperty("default");
			}
		}
		
		return ddl;
	}
	
	public String getHidden(){
		return getProperty("hidden");
	}
	
	/**
	 * only for mapping column
	 * @return
	 */
	public String getRefTable(){
		return getProperty("ref-table-src");
	}
	
	public boolean isCache() {
		return false;
	}
	
	public static void main(String[] args){
//		CompCache.load();
//		System.out.println(((Column) CompCache.getComponentByPath("bdna.data.content.addRemove.NREASONS")).getOptionSelectStatement());
	}

}
