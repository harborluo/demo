package com.demo.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.demo.utils.Utils;

public class Table extends AbstractComponent{
	
	/**
	 * 
	 */
//	private static final long serialVersionUID = -4411594206043443332L;

	private List<TableLink> source = new ArrayList<TableLink>();
	
	public static final String[] adminColList = new String[] { Column.RID, Column.INSTID, Column.CREATE_DATE, Column.END_DATE, Column.IS_LIVE, Column.OP_USER, Column.CHANGED_COLS };
	
	public static final String adminCols = Column.RID+", " + Column.INSTID + ", " + Column.CREATE_DATE + ", " + Column.END_DATE + ", " + Column.IS_LIVE + ", " + Column.OP_USER + ", " + Column.CHANGED_COLS;
	
	public static final String adminColSQL = Column.RID + " number not null, " + 
	                                         Column.INSTID + " number, " + 
	                                         Column.CREATE_DATE + " date default sysdate, "+
	                                         Column.END_DATE + " date, " + 
	                                         Column.IS_LIVE + " integer default 1, " + 
	                                         Column.OP_USER + " varchar2(60), " +
	                                         Column.CHANGED_COLS + " varchar2(4000)";
	
	public static int DELETE_FLAG_MONTHS = 6;
	
	public Table(){
		AbstractComponent idColumn = new Column();
		idColumn.setLabel("ID");
		idColumn.setName("RID");
		idColumn.setProperty("filter-widget", "text");
		idColumn.setProperty("edit-widget", "Text");
		idColumn.setProperty("data-type", "number");
		
		this.addChildren(idColumn);
		
		//bug 29148 - Add 'Last Modified by' Column in Customize Columns for Privileged users 
		AbstractComponent lastModifiedUsrColumn = new Column();
		lastModifiedUsrColumn.setLabel("Last Modified By");
		lastModifiedUsrColumn.setName("LMUSR");
		lastModifiedUsrColumn.setProperty("filter-widget", "text");
		lastModifiedUsrColumn.setProperty("data-type", "string");
		this.addChildren(lastModifiedUsrColumn);
		
		//Bug 27425 - Add a new column 'Last Update Time' to all tables
		AbstractComponent lastModifiedColumn = new Column();
		lastModifiedColumn.setLabel("Last Modified Date");
		lastModifiedColumn.setName("LMDATE");
		lastModifiedColumn.setProperty("filter-widget", "text");
//		lastModifiedColumn.setProperty("editwidget", "Text");
		lastModifiedColumn.setProperty("data-type", "date");
		this.addChildren(lastModifiedColumn);

		//BDNA-13807 Please add "Create date" column in Content DB
		AbstractComponent createDateColumn = new Column();
		createDateColumn.setLabel("Created Date");
		createDateColumn.setName("CREATEDATE");
		createDateColumn.setProperty("filter-widget", "text");
//		createDateColumn.setProperty("editwidget", "Text");
		createDateColumn.setProperty("data-type", "date");
		this.addChildren(createDateColumn);
		
	}
	
	public String getColumnSql(String columnName){
		
		String result = null;
		
		List<AbstractComponent> cols = this.getChildren();
		
		for(AbstractComponent column : cols){
			
			if(columnName.equals(column.getName())){
				return ((Column) column).getSql();
			}
		}
		
		if(result==null){
			//find the column from parent table
			
		}
		
		return result;
	}
	
    public Column getColumnByName(String columnName){
		
		List<AbstractComponent> cols = this.getChildren();
		
		for(AbstractComponent column : cols){
			
			if(columnName.equals(column.getName())){
				return (Column) column;
			}
		}
		
		return null;
	}
    
   public Column getReferenceColumnByName(String columnName){
		
		List<AbstractComponent> cols = this.getReferenceChildren();
		
		for(AbstractComponent column : cols){
			
			if(columnName.equals(column.getName())){
				return (Column) column;
			}
		}
		
		return null;
	}
    
   public TableLink getTableSrcByName(String columnName){
		
	   for(TableLink tableSrc : source){
			if(tableSrc.getProperty("target").equals(this.getPath())){
				if(tableSrc.getProperty("target-column-name").equals(columnName)){
					return tableSrc;
				}
			}
		}
		
		return null;
	}
	
	public void addSourceTable(TableLink src){
		this.source.add(src);
	}
	
	
	
	public String getAdminColumn(){
		String sql = "table_name." + Column.RID + " " + Column.RID + ", \n" +
				"  table_name." + Column.INSTID + " " + Column.INSTID + ", \n" +
				"  to_char(table_name." + Column.CREATE_DATE + ", 'YYYY-MM-DD HH24:MI:SS') " + Column.CREATE_DATE + ", \n" +
				//"  to_char(table_name.\"_enddate\", 'YYYYMMDDHH24MISS') \"_enddate\", \n" +
				"  table_name." + Column.OP_USER + " " + Column.OP_USER + ", \n" +
				"  table_name." + Column.CHANGED_COLS + " " + Column.CHANGED_COLS;
		
		return sql.replaceAll("table_name", this.getName());
	}
	
	public String getPrivateColumn() {
		String sql = "";
		if (this.hasColumn("PRIVATE_FLAG")) {
			sql = " ,\n table_name." + "PRIVATE_FLAG SHOW_PRIVATE_FLAG" ;
		} 
		return sql.replaceAll("table_name", this.getName());		
	}

	public String getMajorColumn() {
		String sql = "";
		if (this.hasColumn("ISMAJOR")) {
			sql = " ,\n table_name." + "ISMAJOR SHOW_ISMAJOR_FLAG" ;
		} 
		return sql.replaceAll("table_name", this.getName());		
	}
	
	public List<Table> getParentTable(){
		
		List<Table> parents = new ArrayList<Table>();
		
		for(TableLink tableSrc : source){
			if(tableSrc.getProperty("child").equals(this.getPath())){
				parents.add((Table) ComponentRepository.getInstance().getComponentByPath(tableSrc.getProperty("parent")));
			}
		}
		
		return parents;
	}

	@Override
	public void validate() {
		// TODO place your XML attribute validate logic here
		
	}
	
	public String getLoadSQL(String cols){
		String sql = "select " + Column.INSTID;
		
		String col_regxp = "^" + (Utils.isEmpty(cols) ? ".*" : cols.replaceAll(",", "|") ) + "$";
		
		for(TableLink src: source){
			if(src.getProperty("target").equals(this.getPath()) && src.getProperty("column-name").matches(col_regxp)){
				sql += ", "+ src.getProperty("column-name");
			}
		}
		
		for(AbstractComponent column : this.getChildren()){
			
			if(column.getName().matches(Column.NON_EDITABLE_REGEXP) || column.getName().matches(col_regxp) == false){
				continue;
			}
			
			if("date".equals(column.getProperty("datatype"))){
				String format = column.getProperty("format")==null ? Column.DEFAULT_DATA_FORMAT : column.getProperty("format");
				sql += ", to_char(" + column.getName() + ", '" + format + "') " + column.getName();
				continue;
			}
			
			sql +=", " + column.getName();
		}
		
		sql += " from " + this.getProperty("tablename") + " where " + Column.IS_LIVE + " = ? and " + Column.RID + " = ? ";
		
		return sql;
	}
	
	public String getAllColumns(){
		String cols = "";
		for(TableLink src: source){
			if(src.getProperty("target").equals(this.getPath()) ){
				cols += src.getProperty("column-name") + ",";
			}
		}
		
		for(AbstractComponent column : this.getChildren()){
			
			if(column.getName().matches(Column.NON_EDITABLE_REGEXP)){
				continue;
			}
			cols += column.getName() + ",";
		}
		return cols.replaceAll(",$", "");
	}
	
	public String getSelectSQL(String cols, String orderColumns){
		String sql = "select " + Column.RID;
		
		String col_regxp = "^" + (Utils.isEmpty(cols) ? ".*" : cols.replaceAll(",", "|") ) + "$";
		
		for(TableLink src: source){
			if(src.getProperty("target").equals(this.getPath()) && src.getProperty("column-name").matches(col_regxp)){
				sql += ", "+ src.getProperty("column-name");
			}
		}
		
		for(AbstractComponent column : this.getChildren()){
			
			if(column.getName().matches(Column.NON_EDITABLE_REGEXP) || column.getName().matches(col_regxp) == false){
				continue;
			}
			
			if("date".equals(column.getProperty("datatype"))){
				String format = column.getProperty("format")==null ? Column.DEFAULT_DATA_FORMAT : column.getProperty("format");
				sql += ", to_char(" + column.getName() + ", '" + format + "') " + column.getName();
				continue;
			}
			
			sql +=", " + column.getName();
		}
		
		sql += " from " + this.getProperty("tablename") + " where " + Column.RID + " > 0 and is_live = 1 ";
		
		if(orderColumns!=null){
			sql += "order by " + orderColumns;
		}
		
		return sql;
	}

	/**
	 * @param cols : specified column name for insert statement
	 * @return
	 */
	public String getInsertSQL(String cols) {
		
		String sql = "insert into " + getProperty("tablename") + "( " + Column.RID + ", " + Column.INSTID + ", " + Column.CREATE_DATE + ", " + Column.OP_USER ;
		String valueString = "values ( ?, dataobj_inst.nextval, sysdate, ?";
		
		String regexp = "^.*$";
		
		if(cols!=null){
			regexp = "^(" + cols.replaceAll(",", "|") +")$";
		}
			
		for(TableLink src: source){
			if(src.getProperty("target").equals(this.getPath())&& src.getProperty("column-name").matches(regexp)){
				sql += ", "+ src.getProperty("column-name");
				valueString += ", ?";
			}
		}
		
		for(AbstractComponent column : this.getChildren()){
			
			if(column.getName().matches(Column.NON_EDITABLE_REGEXP)||column.getName().matches(regexp)==false){
				continue;
			}
			
			sql +=", " + column.getName();
			
			if("date".equals(column.getProperty("datatype"))){
				String format = column.getProperty("format")==null ? Column.DEFAULT_DATA_FORMAT : column.getProperty("format");
//					   valueString += ", to_date(" + column.getName() + ", '" + format + "') " + column.getName();
				valueString += ", to_date(?, '" + format +  "')";
				continue;
			}
			
			valueString += ", ?";
		}
		
		sql = sql + ", \"_srccols\") " + valueString +", ?)";
		
		return sql;
	}

	/**
	 * return two sql for update action,
	 * first one for backup up the current record in content table space 
	 * second one for create a new record in content_live table space
	 * @param cols, format as 'cols1|cols2,cols3'
	 * @return
	 */
	public String[] getUpdateSQL(String cols) {
		//fixed for Bug 29284 – Duplicate rows found when update a row on content DB UI
		//String sql = "update " + getProperty("tablename") + " set " + Column.IS_LIVE + " = 0, " + Column.END_DATE + " = sysdate, " + Column.OP_USER + " = ? where " + Column.RID + " = ? and " + Column.INSTID + " = ?";
		String sql = "update " + getProperty("tablename") + " set " + Column.IS_LIVE + " = 0, " + Column.END_DATE + " = sysdate where " + Column.RID + " = ? and " + Column.IS_LIVE + " = ? ";
		String new_sql = "insert into " + getProperty("tablename") +" ( " + Column.RID + ", " + Column.INSTID + ", " + Column.CREATE_DATE + ", " + Column.OP_USER + ", " + Column.CHANGED_COLS ;
		String select_sql = "select " + Column.RID + ", dataobj_inst.nextval, sysdate, ?, ?";
		String skipColsRegxp = "^$";
		if(Utils.isEmpty(cols) == false){
			skipColsRegxp = "^(" + cols + ")$";
		}
		
		for(TableLink src: source){
			if(src.getProperty("target").equals(this.getPath())){
				new_sql += ", "+ src.getProperty("column-name");
				if(src.getProperty("column-name").matches(skipColsRegxp)){
					select_sql += ", ?";
				}else{
					select_sql += ", " + src.getProperty("column-name");
				}
			}
		}
		
		for(AbstractComponent column : this.getChildren()){
			
			if(column.getName().matches("^(RID|LMDATE|LMUSR|CREATEDATE)$")) continue;
			
			new_sql += ", " + column.getName();
			
			if(!column.getName().matches(skipColsRegxp)||column.getName().matches(Column.NON_EDITABLE_REGEXP)){
				select_sql += ", " + column.getName();
			}else{
				
				if("date".equals(column.getProperty("datatype"))){
					   String format = column.getProperty("format")==null ? Column.DEFAULT_DATA_FORMAT : column.getProperty("format");
					   select_sql += ", to_date(?, '" + format +  "')";
					   continue;
				}
				
				select_sql += ", ?";
			}
			
		}
		
		String sql_combine = new_sql + ")\n  " + select_sql + " from " + getProperty("tablename") + " where " + Column.RID + " = ? and " + Column.INSTID + " = ?"; 
		
		return new String[]{sql, sql_combine};
	}
	
	
	
	public List<String> getDeleteSQL(){
		
		List<String> sqls = new ArrayList<String>();
		
		sqls.add(0, "update " + getProperty("tablename") + " set " + Column.IS_LIVE + " = 0, " + Column.END_DATE + " = sysdate, " + Column.OP_USER + " = ? where " + Column.IS_LIVE + " = 1 and " + Column.RID + " = ?");
		
		//private List<TableSrc> source = new ArrayList<TableSrc>();
		for(TableLink src : source){
			
			if(!src.getProperty("src").equals(this.getPath())){
				continue;
			}
			
			if("delete".equals(src.getProperty("ondelete"))){
				getLogger().debug("Cascade delete children table [" + src.getProperty("target") +"] for table [" + src.getProperty("src") + "].");
				cascadeDelete(this, src, sqls, " ? " );
			}else{
				getLogger().debug("Cascade clear mapping of children table [" + src.getProperty("target") +"] for table [" + src.getProperty("src") + "].");
				Table childTable = (Table) ComponentRepository.getInstance().getComponentByPath(src.getProperty("target"));
				//first level of children tables need to clear mapping columns
				String clearcols = src.getProperty("clearcols") == null ? src.getProperty("target-column-name") : src.getProperty("clearcols");
				
				if(clearcols.indexOf(",")==-1){
					clearcols += " = -1, " + Column.OP_USER + " = ? ";
				}else{
					clearcols = clearcols.replaceAll(",", " = -1, ") + " = -1, " + Column.OP_USER + " = ? ";
				}
				
				sqls.add(0, "update " + childTable.getProperty("tablename") + " set " + clearcols + " where is_live = 1 and " + src.getProperty("target-column-name") + " = ?");
				
			}
			
		}
		
		return sqls;
	}
	
	private void cascadeDelete(Table parent, TableLink src, List<String> sqls, String conds){
		
		Table childTable = (Table) ComponentRepository.getInstance().getComponentByPath(src.getProperty("target"));
		
//		if(childTable.hasColumn(src.getProperty("target-column-name"))==false){
//			return;
//		}
		
		conds = src.getProperty("target-column-name") + " in ( " + conds + " )"; 
		
		getLogger().debug("--Cascade delete children table [" + src.getProperty("target") +"] for table [" + src.getProperty("src") + "].");
//		getLogger().info("update " + childTable.getProperty("tablename") + " set is_live = 0, \"_enddate\" = sysdate, \"_src\" = ? where is_live = 1 and " + conds);
		
		sqls.add(0, "update " + childTable.getProperty("tablename") + " set " + Column.IS_LIVE + " = 0, " + Column.END_DATE + " = sysdate, " + Column.OP_USER + " = ? where " + Column.IS_LIVE + " = 1 and " + conds);
		
		for(TableLink source : childTable.getSource()){
			
			if(!source.getProperty("src").equals(childTable.getPath())){
				continue;
			}
			
			if("delete".equals(source.getProperty("ondelete"))){
				
				if(!childTable.getPath().equals(parent.getPath())){
//					getLogger().info("Cascade delete children table [" + source.getProperty("target") +"] for table [" + source.getProperty("src") + "].");
					cascadeDelete(childTable, source, sqls, "select " + Column.RID + " from " + childTable.getProperty("tablename") + " where " + Column.IS_LIVE + " = 1 and " + conds );
				}
				
			}else{
				
				Table grandChildTable = (Table) ComponentRepository.getInstance().getComponentByPath(source.getProperty("target"));
				
				String clearcols = source.getProperty("clearcols") == null ? source.getProperty("target-column-name") : source.getProperty("clearcols");
				
				if(clearcols.indexOf(",")==-1){
					clearcols += " = -1, " + Column.OP_USER + " = ? ";
				}else{
					clearcols = clearcols.replaceAll(",", " = -1, ") + " = -1, " + Column.OP_USER + " = ? ";
				}
				
				getLogger().debug("Cascade clear mapping of children table [" + source.getProperty("target") +"] for table [" + source.getProperty("src") + "].");
				
				String sql = "update " + grandChildTable.getProperty("tablename") + " set " + clearcols + " where is_live = 1 and " 
				             + source.getProperty("target-column-name") + " in ( " + "select \"_rid\" from " + childTable.getProperty("tablename") + " where " + Column.IS_LIVE + " = 1 and " + conds + " )";
				
//				getLogger().info(sql);
				
				sqls.add(0, sql);
				
			}
			
		}
	}


	public List<TableLink> getSource() {
		return source;
	}

	@Override
	public String getValidateAttributes() {
		return "^(tablename|sequece)$";
	}
	
	public String generateDDL(){
		
		StringBuffer ddl = new StringBuffer();
		ddl.append("create table ").append(getProperty("table-name")).append(" (\n  ");
		ddl.append(adminColSQL);
		
		/**
		for(TableSrc tableSrc : source){
			if(tableSrc.getProperty("target").equals(this.getPath())){
				ddl.append(",\n  ").append(tableSrc.getProperty("target-column-name")).append(" number default -1");
			}
		}
		**/
		List<AbstractComponent> ref_columns = getReferenceChildren();
		for(AbstractComponent column : ref_columns){
			ddl.append(",\n  ").append(((Column) column).getDDL());
		}
		
		List<AbstractComponent> columns = getChildren();
		for(AbstractComponent column : columns){
			if(column.getName().matches("^(RID|LMDATE|LMUSR|CREATEDATE)$")) continue;
			ddl.append(",\n  ").append(((Column) column).getDDL());
		}
		
		String tableName = getProperty("table-name");
		
		ddl.append(",\n constraint pk_"+(tableName.length()>25?tableName.substring(0,20):tableName)+" primary key ("+Column.RID+","+Column.INSTID+")");
		
		ddl.append("\n) partition by list (" + Column.IS_LIVE + ") ( partition live values (1) tablespace content_live,  partition snap values (0) tablespace content, partition archive values (-1) tablespace content_archive ) enable row movement");
		
		return ddl.toString();
	}
		
	public boolean hasColumn(String columnName){
		
		for(AbstractComponent col : getChildren()){
			if(col.getName().equals(columnName)) return true;
		}
		
		for(TableLink src : source){
			
			if(src.getProperty("child").equals(this.getPath())){
				if(columnName.equals(src.getProperty("target-column-name"))) return true;
			}
			
		}
		
		return false;
	}
	
    public boolean isMappingColumn(String columnName){
    	
    	if("RID".equals(columnName)){
    		return true;
    	}
		
		for(TableLink src : source){
			
			if(src.getProperty("target").equals(this.getPath())){
				if(columnName.equals(src.getProperty("target-column-name"))) return true;
			}
			
		}
		
		return false;
	}
	
	private List<AbstractComponent> referenceChildren = new ArrayList<AbstractComponent>();
	
	public List<AbstractComponent> getReferenceChildren() {
		return referenceChildren;
	}
	
	public void addReferenceChildren(Column c, TableLink source){
		this.referenceChildren.add(c);
		this.source.add(source);
	}
	
	
	
	@Override
	public boolean isCache() {
		return true;
	}

	public static void main(String[] args){
		
	}
	
}
