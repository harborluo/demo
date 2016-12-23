package com.websystique.springmvc.service.vo;

/**
 * 批处理sql脚本实体类
 * @author Administrator
 *
 */
public class BatchEntity {
	
	public BatchEntity(String sql,Object[] args){
		this.sql = sql;
		this.args = args;
	}
	
	/** sql 命令 */
	private String sql;
	
	/** 参数 */
	private Object[] args;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
	
}
