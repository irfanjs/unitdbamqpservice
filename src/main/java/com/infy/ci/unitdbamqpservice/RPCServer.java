package com.infy.ci.unitdbamqpservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

@Configuration
@SpringBootApplication
@PropertySource("classpath:/application.properties")
public class RPCServer {
	
	static RPCServer cihelper;
	
	DataSource dataSource;
	

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		SpringApplication.run(RPCServer.class, args);
	}

	Environment env;
	private static final String RPC_QUEUE_NAME = "rpc_queue1";
	// commenting today
	// private static final String routing_key = "rpc_queue1";

	@Value("${spring.rabbitmq.host}")
	private String rabHost;

	private Connection connection;
	private Channel channel;

	public RPCServer() throws IOException, TimeoutException {

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("35.154.27.134");

		connection = factory.newConnection();
		channel = connection.createChannel();
	
		
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
		

		dataSource = new DataSource(p);

	}

	@Bean
	public Queue queue() {
		return new Queue(RPC_QUEUE_NAME);
	}

	@Component
	public static class RpcListener {

		@RabbitListener(queues = RPC_QUEUE_NAME)
		public String reply() throws IOException, TimeoutException, ClassNotFoundException, SQLException {
			UnitDB db = new UnitDB();
			String response = db.getLatestNightlyaggregate();
//			String response = "{" + "\n" + "" + "\"glossary\"" + ":" + "{" + "\n" + "\"title\":"
//					+ "\"example glossary\"" + "\n" + "}" + "\n" + "}";
		//	RPCServer r = new RPCServer();
		//	List<Map<String, Object>> response = r.getUnitTestForBuildId();
			return response;
		}

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
	
	public List<Map<String, Object>> getEntitiesFromResultSet(
			ResultSet resultSet) throws SQLException {
		ArrayList<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		while (resultSet.next()) {
			entities.add(getEntityFromResultSet(resultSet));
		}
		return entities;
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
	
	public java.sql.Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}
	
	public static synchronized RPCServer getInstance() throws IOException, TimeoutException {
		if (cihelper == null) {
			cihelper = new RPCServer();
		}
		return cihelper;
	}
	
public List<Map<String, Object>> getUnitTestForBuildId() throws SQLException, ClassNotFoundException, IOException, TimeoutException{
		
		String sql = "select count(*) from buildinfo";
		return executeQuery(sql);
	}	


public List<Map<String, Object>> executeQuery(String sql)
		throws SQLException, IOException, TimeoutException {
	java.sql.Connection conn = null;
	Statement statement = null;
	ResultSet resultSet = null;

	try {
		conn = RPCServer.getInstance().getConnection();
		statement = conn.createStatement();
		resultSet = statement.executeQuery(sql);

		return RPCServer.getInstance().getEntitiesFromResultSet(resultSet);
	}

	finally {
		RPCServer.close(conn, statement, resultSet);
	}

}

public static void close(java.sql.Connection c, Statement s, ResultSet r) {
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


}