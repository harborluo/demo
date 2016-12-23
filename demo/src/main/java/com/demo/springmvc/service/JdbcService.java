package com.demo.springmvc.service;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.demo.springmvc.service.vo.BatchEntity;
import com.demo.springmvc.service.vo.TableColumnEntity;



public interface JdbcService {
	
	public int test();
	
	
	/**
	 * 根据参数查询,返回结果集
	 * @param sql
	 * @param args
	 * @return
	 */
	public SqlRowSet query(String sql,Object... args);

	/**
	 * 取得sql的统计数量
	 * @param sql (select count(1) from .....)
	 * @param params
	 * @return
	 */
	public int queryForInt(String sql,Object... params);
	
	/**
	 * 读数据表结构信息
	 * @param tableName
	 * @return
	 */
	public Map<String,TableColumnEntity> getTableStructreInfo(String tableName);
	
	/**
	 * 取得序列的下一键值
	 * @param sequenceName
	 * @return
	 */
	public Long getSequenceValue(String sequenceName);
	
	/**
	 * 执行sql
	 * @param sql
	 * @param params
	 * @return
	 */
	public int doExecuteSQL(String sql,Object... params);
	
	/**
	 * 读取系统参数配置
	 * @param paramName
	 * @return
	 */
	public String getSystemParamValue(String paramName);
	
	/**
	 * 执行sql
	 * @param sql
	 * @param params
	 * @param argTypes 参数数据类型
	 * @return
	 */
	public int doExecute(String sql,Object[] params,int[] argTypes);
	
	/**
	 * 批量执行sql
	 * @param list
	 */
	public void doExecuteBatch(List<BatchEntity> list) throws Exception;
	
	public List<String[]> fetchRowsByPage(String sql, int pageIndex, int pageSize, Object[] values);

	public Map<String, String> fetchOneRow(String sql, Object... args);
	
	public List<String[]> fetchRows(String sql, Object... args);
	
	public String[] getMappingLabel(String sql, Object... args);
	
	public Map<String,String> getMutilRowValues(String sql,Object... args);  
	
	public String getSingleColumnValue(String sql, Object... args);

}
