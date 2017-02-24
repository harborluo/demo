package com.demo.springmvc.configuration;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import org.apache.commons.dbcp.BasicDataSource;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.demo.springmvc")
@PropertySource("classpath:jdbc.properties")
public class DemoConfiguration extends WebMvcConfigurerAdapter{
	
	@Value("${jdbc.url}")
	private String jdbcUrl;
	
	@Value("${jdbc.username}")
	private String jdbcUsername;
	
	@Value("${jdbc.password}")
	private String jdbcPassword;
	
	@Value("${jdbc.driverClassName}")
	private String driverClassName;
	
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".jsp");
		registry.viewResolver(viewResolver);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("/static/");
	}
	
	@Bean
	public DataSource getDataSource() {
		
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(jdbcUrl);
		dataSource.setUsername(jdbcUsername);
		dataSource.setPassword(jdbcPassword);
		dataSource.setDefaultAutoCommit(false);
		dataSource.setInitialSize(10);
		dataSource.setMaxActive(100);
		dataSource.setMaxIdle(30);
		dataSource.setMinIdle(15);
		dataSource.setMinEvictableIdleTimeMillis(1000);
		dataSource.setTimeBetweenEvictionRunsMillis(5000);
		dataSource.setTestOnBorrow(true);
		dataSource.setTestWhileIdle(true);
		dataSource.setValidationQuery("select 1 from dual");
		return dataSource;
	}
	     	 
	@Bean
	public JdbcTemplate getJdbcTemplate() {
		return new JdbcTemplate(getDataSource());
	}

}