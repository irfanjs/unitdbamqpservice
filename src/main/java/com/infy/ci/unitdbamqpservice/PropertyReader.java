package com.infy.ci.unitdbamqpservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.util.ResourceUtils;

public class PropertyReader {
	
	String host;

	public String getHost() {
		
		Properties properties = new Properties();
		try {
			File file = ResourceUtils.getFile("classpath:application.properties");
			InputStream in = new FileInputStream(file);
			properties.load(in);
		} catch (IOException e) {

		}
		return host = properties.getProperty("spring.mysql.host");
	}

	public void setHost(String host) {
		this.host = host;
	}

}
