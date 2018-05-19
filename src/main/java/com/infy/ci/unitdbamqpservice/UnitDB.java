package com.infy.ci.unitdbamqpservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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

import com.google.gson.Gson;

public class UnitDB {
	
	int projectid;
	
	static private UnitDB cihelper;
	private DataSource dataSource;
		
	public UnitDB(int projectid) {
		PoolProperties p = new PoolProperties();
		Properties prop = new Properties();
		InputStream input = null;
		
		this.projectid = projectid;
		
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

	
	public UnitDB() {
		// TODO Auto-generated constructor stub
	}


	public String getLatestNightlyaggregate() throws IOException {
		List<Map<String, Object>> data;
		ChartData d = new ChartData();
		Gson gson = new Gson();
		List<String> arrayList = new ArrayList<String>();
		arrayList.add("Pass");
		arrayList.add("Fail");
		arrayList.add("Skip");

		int pas = 0;
		int fail = 0;
		int skip = 0;
		String json;

		ArrayList<Integer> singleList = new ArrayList<Integer>();

		try {
			data = getAggregatedUnitTestDataForLatestNightlyBuild();
			if (data.size() != 0) {
				for (Map<String, Object> data1 : data) {
					for (Map.Entry<String, Object> entry : data1.entrySet()) {
						System.out.println(entry.getKey() + ": "
								+ entry.getValue());

						if (entry.getKey().equals("pass")) {
							pas = Integer.parseInt(entry.getValue().toString());
						} else if (entry.getKey().equals("fail")) {
							fail = Integer
									.parseInt(entry.getValue().toString());
						} else if (entry.getKey().equals("skip")) {
							skip = Integer
									.parseInt(entry.getValue().toString());
						}

					}

					singleList.add(pas);
					singleList.add(fail);
					singleList.add(skip);

					Map<String, Object> map = new HashMap<>();
					map.put("Data", singleList);

					ArrayList<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
					dataList.add(map);

					d.setCategories(arrayList);
					d.setData(dataList);

					json = gson.toJson(d);
					return json;

				}
			} else {
				return null;
			}
		} catch (SQLException | ClassNotFoundException e) {
			throw new IOException("Failed to fetch data for unit test", e);
		}
		return null;

	}
	
	
	public List<Map<String, Object>> getAggregatedUnitTestDataForLatestNightlyBuild() throws SQLException, ClassNotFoundException{

	//	LOGGER.debug("called getAggregatedUnitTestDataForNightlyBuildId");
		
	//	String sql = "select sum(subut.total) total,sum(subut.pass) pass,sum(subut.fail) fail, sum(subut.skip) skip from (select modulename, datetime as dt,max(id) id from  buildinfo  subbi  where subbi.nightlybuild_id in (select id from nightlybuild where datetime in (select max(datetime) from nightlybuild where status = 1)) and project_id = " + this.projectid + " group by modulename) suborig LEFT JOIN unittest subut ON suborig.id = subut.buildinfo_id;";
		// query to get data from so e2e snapshot
		
		String sql = "select * from (select max(id) id from buildinfo where project_id = " + this.projectid + " and nightlybuild_id != 'NULL') tempbi inner join unittest ut on ut.buildinfo_id = tempbi.id;";	
		return executeQuery(sql);
	}
	
	public List<Map<String, Object>> executeQuery(String sql)
			throws SQLException {
		Connection conn = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			
			conn = getInstance().getConnection();
			statement = conn.createStatement();
			resultSet = statement.executeQuery(sql);

			return UnitDB.getInstance().getEntitiesFromResultSet(resultSet);
		}

		finally {
			UnitDB.close(conn, statement, resultSet);
		}

	}
	
	public static synchronized UnitDB getInstance() {
		if (cihelper == null) {
			cihelper = new UnitDB();
		}
		return cihelper;
	}
	
	public Connection getConnection() throws SQLException {
		Connection connection = null;
		return connection = DriverManager.getConnection("jdbc:mysql://35.154.27.134/ci","root", "root");
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


}
