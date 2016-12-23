package com.websystique.springmvc.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;

import oracle.jdbc.rowset.OracleCachedRowSet;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * 
 * @author Administrator
 *
 */
public class SqlRowSetOracleResultSetExtractor implements ResultSetExtractor {

	public Object extractData(ResultSet rs) throws SQLException {
		return createSqlRowSet(rs);
	}

	protected SqlRowSet createSqlRowSet(ResultSet rs) throws SQLException {
		javax.sql.rowset.CachedRowSet rowSet = newCachedRowSet();
		rowSet.populate(rs);
		return new ResultSetWrappingSqlRowSet(rowSet);
	}

	protected CachedRowSet newCachedRowSet() throws SQLException {
		return new OracleCachedRowSet();
	}
}
