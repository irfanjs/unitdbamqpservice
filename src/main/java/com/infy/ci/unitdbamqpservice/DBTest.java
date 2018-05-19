package com.infy.ci.unitdbamqpservice;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/application.properties")
public class DBTest {
	
	static private  DBTest cihelper;
	
	private static DataSource dataSource;
	
	 @Value("${spring.mysql.host}")
	    private String mysqlHost;
	 
	 @Value("${spring.mysql.user}")
	    private String mysqlUser;
	
	 @Value("${spring.mysql.password}")
	    private String mysqlpassword;

	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub

		PoolProperties p = new PoolProperties();
		Properties prop = new Properties();
		InputStream input = null;
		
		p.setUrl("jdbc:mysql://35.154.27.134/ci");
		p.setDriverClassName("com.mysql.jdbc.Driver");
		
		//p.setDriverClassName("java.sql.DriverManager");
		
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
		
		prop.setProperty("useSSL", "false");
		prop.setProperty("autoReconnect", "true");
		prop.setProperty("verifyServerCertificate", "false");
		
		

		dataSource = new DataSource(p);
		
		DBTest d = new DBTest();
		try {
			List<Map<String, Object>> a;
			d.getConnection();
			System.out.println("connection successful");
			a = d.getUnitTestForBuildId();
			System.out.println(a);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	
	public List<Map<String, Object>> executeQuery(String sql)
			throws SQLException {
		Connection conn = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			conn = DBTest.getInstance().getConnection();
			statement = conn.createStatement();
			resultSet = statement.executeQuery(sql);

			return DBTest.getInstance().getEntitiesFromResultSet(resultSet);
		}

		finally {
			DBTest.close(conn, statement, resultSet);
		}

	}
	
	public static synchronized DBTest getInstance() {
		if (cihelper == null) {
			cihelper = new DBTest();
		}
		return cihelper;
	}
	
	
	public static void close(Connection c, Statement s, ResultSet r) {
		try {
			if(r != null){
				r.close();
			}
			if(s != null){
				s.close();
			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			// ignore
		}
	}
	
	protected Map<String, Object> getEntityFromResultSet(ResultSet resultSet)
			throws SQLException {
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
	
	
	public List<Map<String, Object>> getEntitiesFromResultSet(
			ResultSet resultSet) throws SQLException {
		ArrayList<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		while (resultSet.next()) {
			entities.add(getEntityFromResultSet(resultSet));
		}
		return entities;
	}
	
	
public List<Map<String, Object>> getUnitTestForBuildId() throws SQLException, ClassNotFoundException{
		
		//String sql = "select count(*) from buildinfo";
	String sql = "select * from (select max(id) id from buildinfo where project_id = 1 and nightlybuild_id != 'NULL') tempbi inner join unittest ut on ut.buildinfo_id = tempbi.id;";
		return executeQuery(sql);
	}	
}
