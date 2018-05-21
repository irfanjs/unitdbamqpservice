package com.infy.ci.unitdbamqpservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@SpringBootApplication
@PropertySource("classpath:/application.properties")
public class RPCServer {
	
		public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		SpringApplication.run(RPCServer.class, args);
	}

	private static final String RPC_QUEUE_NAME = "rpc_queue1";


	@Value("${spring.rabbitmq.host}")
	private String rabHost;



	public RPCServer() throws IOException, TimeoutException {

	}

	@Bean
	public Queue queue() {
		return new Queue(RPC_QUEUE_NAME);
	}

	@Component
	public static class RpcListener {

		@RabbitListener(queues = RPC_QUEUE_NAME)
		public String reply() throws IOException, TimeoutException, ClassNotFoundException, SQLException {
			UnitDB db = new UnitDB(1);
			String response = db.getLatestNightlyaggregate();
		//	String response = "{" + "\n" + "" + "\"glossary\"" + ":" + "{" + "\n" + "\"title\":"
		//			+ "\"example glossary\"" + "\n" + "}" + "\n" + "}";
		//	RPCServer r = new RPCServer();
		//	List<Map<String, Object>> response = r.getUnitTestForBuildId();
			return response;
		}
	}
}

