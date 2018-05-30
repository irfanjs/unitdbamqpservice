package com.infy.ci.unitdbamqpservice;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class UnitDBHelper implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(UnitDBHelper.class);
	static private UnitDBHelper cihelper;

	private final static Object cihelperLock = new Object();

	private DataSource dataSource;
	
	@Autowired
    private Environment env;

	/*public UnitDBHelper() {
		
		
	}*/

	public static synchronized UnitDBHelper getInstance() {
		if (cihelper == null) {
			cihelper = new UnitDBHelper();
		}
		return cihelper;
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public List<Map<String, Object>> getEntitiesFromResultSet(ResultSet resultSet) throws SQLException {
		ArrayList<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		while (resultSet.next()) {
			entities.add(getEntityFromResultSet(resultSet));
		}
		return entities;
	}

	protected Map<String, Object> getEntityFromResultSet(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		Map<String, Object> resultsMap = new HashMap<String, Object>();
		for (int i = 1; i <= columnCount; ++i) {
			String columnName = metaData.getColumnName(i).toLowerCase();
			Object object = resultSet.getObject(i);
			resultsMap.put(columnName, object);
		}
		return resultsMap;
	}

	public Statement createExecuteStatement() throws SQLException, ClassNotFoundException {

		Connection c = getConnection();
		return c.createStatement();
	}

	public PreparedStatement createInsertStatement(String sqlQuery) throws SQLException, ClassNotFoundException {
		// Statements allow to issue SQL queries to the database
		Connection c = getConnection();
		return c.prepareStatement(sqlQuery);
	}

	public static void close(Connection c, Statement s, ResultSet r) {
		try {
			if (r != null) {
				r.close();
			}
			if (s != null) {
				s.close();
			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
        String host = env.getProperty("spring.mysql.host");
		
		PoolProperties p = new PoolProperties();
		// Properties prop = new Properties();
		InputStream input = null;
		
		p.setUrl("jdbc:mysql://" + host + "/ci");
		p.setDriverClassName("com.mysql.jdbc.Driver");

		p.setUsername("root");
		p.setPassword("root");

		p.setJmxEnabled(true);
		p.setTestWhileIdle(true);
		p.setTestOnBorrow(true);
		p.setValidationQuery("SELECT 1");
		p.setTestOnReturn(false);
		p.setValidationInterval(30000);
		p.setTimeBetweenEvictionRunsMillis(30000);
		p.setMaxActive(100);
		p.setInitialSize(10);
		p.setMaxWait(10000);
		p.setRemoveAbandonedTimeout(60);
		p.setMinEvictableIdleTimeMillis(30000);
		p.setMinIdle(10);
		p.setLogAbandoned(true);
		p.setRemoveAbandoned(true);
		p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");

		/*
		 * prop.setProperty("useSSL", "false");
		 * prop.setProperty("autoReconnect", "true");
		 */

		dataSource = new DataSource(p);
		
	}
}