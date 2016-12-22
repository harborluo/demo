package com.websystique.springmvc.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Service("jdbcService")
public class JdbcServiceImpl implements JdbcService {

	@Autowired
	JdbcTemplate template;

	@Override
	public int test() {
		SqlRowSet set  = template.queryForRowSet("select * from user_tables");
		int cnt = 0;
		while(set.next()){
			cnt++;
		}
		return cnt;
	}
	
}
