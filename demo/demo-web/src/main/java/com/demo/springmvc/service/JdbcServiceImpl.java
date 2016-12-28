package com.demo.springmvc.service;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.demo.springmvc.service.vo.BatchEntity;
import com.demo.springmvc.service.vo.ClobString;
import com.demo.springmvc.service.vo.TableColumnEntity;
import com.demo.utils.Utils;

@Service("jdbcService")
public class JdbcServiceImpl implements JdbcService {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	JdbcTemplate template;

	@Override
	public Set<String> getAllTableNames() {
		Set<String> tableNameSet = new HashSet<>();
		SqlRowSet set = query("select table_name from user_tables");
		while(set.next()){
			tableNameSet.add(set.getString(1));
		}
		return tableNameSet;
	}

	@Override
	public int test() {
		SqlRowSet set  = template.queryForRowSet("select * from user_tables");
		int cnt = 0;
		while(set.next()){
			cnt++;
		}
		return cnt;
	}
	
//	private static final Logger logger = Logger.getLogger(JdbcServiceImpl.class);

	private org.springframework.jdbc.support.lob.OracleLobHandler lobHandler ;
	
	public void setLobHandler(
			org.springframework.jdbc.support.lob.OracleLobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}
	
	@Override
	public SqlRowSet query(String sql, Object... args){
		return(SqlRowSet) template.query(sql,args, new SqlRowSetOracleResultSetExtractor());  
	}

	/**
	 * 生成分页查询语句
	 * @param sql
	 * @return
	 */
	private String getPageSQL(String sql){
		
		sql = sql.toUpperCase();
		
		int whereIndex = sql.lastIndexOf("WHERE");
		int subQueryIndex = sql.lastIndexOf(")");
		int funInex = sql.lastIndexOf("(");
		int fromIndex = sql.indexOf("FROM");
		int orderIndex = sql.lastIndexOf("ORDER");
		
		if(orderIndex>-1){
			logger.debug("SQL 有排序");
			return "SELECT * FROM (SELECT TT.*, ROWNUM AS ROWNO FROM ("+sql+") TT WHERE ROWNUM <= ? ) TABLE_ALIAS where TABLE_ALIAS.ROWNO > ? ";
		}else{
			
			logger.debug("SQL 没有排序");
			
			String new_sql = "";
			boolean hasCondiction = false;
			
			/**
			if(sql.indexOf("where")==-1){
				hasCondiction = false;
			}else{
				if(sql.indexOf(")")==-1){
					//不带子查询
					hasCondiction = true;
				}else{
					//带子询
					String temp = sql.toLowerCase().substring(sql.lastIndexOf(")"), sql.length()-1);
					hasCondiction = temp.indexOf("where")!=-1;
				}
			}
			**/
			
			if(subQueryIndex==-1||subQueryIndex<fromIndex||funInex>whereIndex){
				//无子查询
				if(whereIndex==-1){
					//无查询条件
				}else{
					//有查询条件
					hasCondiction = true;
				}
				
			}else{
				//有子查询
				if(whereIndex==-1||subQueryIndex>whereIndex){
					//无查询条件
				}else{
					//有查询条件
					hasCondiction = true;
				}
			}
			
			logger.debug("hasCondiction:"+hasCondiction);
			
			new_sql = "SELECT * FROM ( "+sql.replaceFirst("SELECT","SELECT ROWNUM ROWNO,") + (hasCondiction?" and ":" where ")+" ROWNUM <= ? ) TABLE_ALIAS WHERE TABLE_ALIAS.ROWNO > ? ";
			
			logger.debug("new_sql:\n"+new_sql);
			
			return new_sql;
		}
	}
	
	public static String getPaginateSql(String sql,int pageIndex,int pageSize,boolean hasOrderBy){

	       String resultSql = "";
	       	       
	       if(hasOrderBy==false){
	           	    	   
	    	   resultSql = "select * from (\n" + sql.replaceFirst("select", "select ROWNUM as RN,") + " and ROWNUM <= ?\n) TABLE_ALIAS where TABLE_ALIAS.RN > ?";
	           
	       }else{
	           
	           resultSql = "SELECT * FROM ( SELECT ROWNUM AS RN, TT.* FROM ( \n"
	        	   + sql +       
	           " \n) TT WHERE ROWNUM <= ? ) TABLE_ALIAS where TABLE_ALIAS.RN > ? "  ; 
	           
	       }      
	       
	       return resultSql;
	       
	   }

	@Override
	public Map<String,TableColumnEntity> getTableStructreInfo(String tableName) {
		
		//tableName = "MW_WORKITEM";
		
		String sql="select  column_name,data_type,data_length,data_precision,data_scale,data_default  from USER_TAB_COLUMNS "+
				   "where table_name not in (select view_name from USER_VIEWS) and table_name = upper( ? ) ";
		SqlRowSet set = query(sql, new Object[]{tableName});
		Map<String,TableColumnEntity> map = new HashMap<String, TableColumnEntity>();
		while(set.next()){
			TableColumnEntity entity = new TableColumnEntity();
			entity.setColumnName(set.getString("COLUMN_NAME"));
			entity.setDataLength(set.getString("DATA_LENGTH"));
			entity.setDataPrecision(set.getString("DATA_PRECISION"));
			entity.setDataType(set.getString("DATA_TYPE"));
			entity.setDataScale(set.getString("DATA_SCALE"));
			entity.setDefaultValue(set.getString("DATA_DEFAULT"));
			
			map.put(entity.getColumnName(), entity);
		}
		return map;
	}

	public Long getSequenceValue(String sequenceName) {
		String sql = "select  "+sequenceName+".nextval from dual ";
		return template.queryForObject(sql, new Object[]{}, Long.class);
	}
	
	@Override
	public int queryForInt(String sql, Object... params) {
		return template.queryForObject(sql, params, int.class);
	}

	@Override
	public int doExecuteSQL(String sql, Object... params) {
		return template.update(sql, params);
	}

	@Override
	public String getSystemParamValue(String paramName) {
		String sql = " SELECT paras FROM mw_paras WHERE name= ? ";
		String value ="";
		SqlRowSet set = query(sql, new Object[]{paramName});
		if(set.next()){
			value = set.getString("paras");
		}
		return value;
	}

	@Transactional
	public int doExecute(String sql, Object[] params, int[] argTypes) {
		return template.update(sql, params,argTypes);
	}

	@Override
	@Transactional
	public void doExecuteBatch(List<BatchEntity> list) throws Exception{
		
		try {

			for(final BatchEntity item:list){
		
				
				template.update(item.getSql(), new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						
						Object[] args = item.getArgs();
						for(int i=0; i< args.length; i++){
							if (args[i] instanceof ClobString) {
								String toBeUpdated = ((ClobString) args[i]).getContent();
								lobHandler.getLobCreator().setClobAsString(ps, i+1, toBeUpdated);

							}else{
								ps.setObject(i+1, args[i]);
							}
						}
						
					}
				});
				
			}
			

		} catch (Exception e) {
			throw e;
		}
		
	}


	@Override
	public List<String[]> fetchRowsByPage(String sql, int pageIndex, int pageSize, Object[] args) {
		
		List<String[]> result = new ArrayList<String[]>();	
		
//		String sql_temp = sql;
		
		int startRow = (pageIndex-1)*pageSize;
		int endRow = pageIndex*pageSize;
		
		String sql_temp = getPaginateSql(sql, pageIndex, pageSize, sql.indexOf("order by")>-1);
		
		Object[] new_args ;
		if(args==null || args.length==0){
			new_args = new Object[]{endRow,startRow};
		}else{
			new_args = new Object[args.length+2];
			for(int i=0;i<args.length;i++){
				new_args[i]=args[i];
			}
			new_args[new_args.length-2] = endRow;
			new_args[new_args.length-1] = startRow;
		}
		
//		JdbcTemplate template = template;
		
		SqlRowSet dataSet = (SqlRowSet)  template.query(sql_temp, new_args, new SqlRowSetOracleResultSetExtractor());
		
		SqlRowSetMetaData meta = dataSet.getMetaData();
		
		String[] temp = meta.getColumnNames();
		
//		List<Map<String, String>> dataList = new ArrayList<Map<String,String>> ();
		while(dataSet.next()){
			String[] row = new String[temp.length];
			for(int i=0;i<temp.length;i++){
				row[i] = dataSet.getString(i+1);
			}
			result.add(row);
		}
		
		return result;
		
	}

	@Override
	public Map<String, String> fetchOneRow(String sql, Object... args) {
//		JdbcTemplate template = template;
		SqlRowSet dataSet = (SqlRowSet) template.query(sql,args, new SqlRowSetOracleResultSetExtractor());
		Map<String, String> result = new HashMap<String, String>();
        SqlRowSetMetaData meta = dataSet.getMetaData();
		
		String[] temp = meta.getColumnNames();
		while(dataSet.next()){
			for(String colName:temp){
				result.put(colName, dataSet.getString(colName));
			}
		}
		return result;
	}

	@Override
	public List<String[]> fetchRows(String sql, Object... args) {
		
		List<String[]> result = new ArrayList<String[]>();	
//		JdbcTemplate template = template;
		SqlRowSet dataSet = (SqlRowSet)  template.query(sql, args, new SqlRowSetOracleResultSetExtractor());
		SqlRowSetMetaData meta = dataSet.getMetaData();
		
		String[] temp = meta.getColumnNames();
		
		while(dataSet.next()){
			String[] row = new String[temp.length];
			for(int i=0;i<temp.length;i++){
				row[i] = dataSet.getString(i+1);
			}
			result.add(row);
		}
		
		return result;
	}

	@Override
	public String[] getMappingLabel(String sql, Object... args) {
		
		String label = "";
		String delete_flag = "";
		SqlRowSet set = query(sql, args);
		
		while(set.next()){
			SqlRowSetMetaData meta = set.getMetaData();
			delete_flag = set.getString(1);
			for(int i = 2; i <= meta.getColumnCount(); i++){
//				if(meta.getColumnName(i).startsWith("_")) continue;
				label += set.getString(i)==null ? ":" : set.getString(i) + ":";
			}
		}		
		
		label = label.replaceAll(":$", "");
		
		return new String[]{delete_flag, label};
	}

//	@Override
//	public Map<String, String> getMutilRowValues(String sql, Object... args) {
//		Map<String, String> resultMap = new HashMap<String, String>();
//		
//		List<String[]> result = new ArrayList<String[]>();	
////		JdbcTemplate template = template;
//		SqlRowSet dataSet = (SqlRowSet)  template.query(sql, args, new SqlRowSetOracleResultSetExtractor());
//		SqlRowSetMetaData meta = dataSet.getMetaData();
//		
//		String[] temp = meta.getColumnNames();
//		
//		while(dataSet.next()){
//			String[] row = new String[temp.length];
//			for(int i=0;i<temp.length;i++){
//				row[i] = dataSet.getString(i+1);
//			}
//			result.add(row);
//		}
//		
//		String[] vals = Utils.mergeSameValues(result);
//		String[] cols = new String[vals.length];
//		for(int i=0;i<vals.length;i++){
//			cols[i] = dataSet.getMetaData().getColumnName(i+1);
//		}
//		
//		for(int i=0;i<cols.length;i++){
//			resultMap.put(cols[i], vals[i]);
//		}
//		
//		return resultMap;
//	}

	@Override
	public String getSingleColumnValue(String sql, Object... args) {
		String result = null;
		
		SqlRowSet set = query(sql, args);
		
		while(set.next()){
			result = set.getString(1);
		}	
		
		return result;
	}
	
}
