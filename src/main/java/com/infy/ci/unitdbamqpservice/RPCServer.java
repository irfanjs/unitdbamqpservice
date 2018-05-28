package com.infy.ci.unitdbamqpservice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@SpringBootApplication
public class RPCServer {

	public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
		SpringApplication.run(RPCServer.class, args);
	}

	private final static Logger logger = LoggerFactory.getLogger(RPCServer.class);

	private static final String RPC_QUEUE_NAME = "rpc_queue_unit";

	public RPCServer() throws IOException, TimeoutException {

	}

	@Bean
	public Queue queue() {
		return new Queue(RPC_QUEUE_NAME);
	}

	@Component
	public static class RpcListener {

		@RabbitListener(queues = RPC_QUEUE_NAME)
		public String reply(String request) throws IOException, TimeoutException, ClassNotFoundException, SQLException {
			logger.info("Sent Message was: " + request);
			String response;
			String[] output = request.split("-");
			String projectid = output[1];

			UnitDB db = new UnitDB(Integer.parseInt(projectid));

			if (output[0].equals("aggregate")) {
				String build = output[2];
				String buildtype = output[3];

				if (build.toLowerCase().equals("latest") && buildtype.equals("nightly")) {
					response = db.getLatestNightlyaggregate();
				} else {
					response = db.getAggregatedDataForNightlyBuild(Integer.parseInt(build));
				}
			} else if (output[0].equals("modulewise")) {
				String build = output[2];
				String buildtype = output[3];
				if (build.toLowerCase().equals("latest") && buildtype.equals("ci")) {
					response = db.getLatestCiModulewise();
				} else {

					response = db.getAggregatedDataForNightlyBuild(Integer.parseInt(build));
				}
			}

			else if (output[0].equals("week")) {
				response = db.getTrendWeekData();
			} else if (output[0].equals("month")) {
				response = db.getTrendMonthData();
			} else {
				throw new RuntimeException("failed to return a response. please check the message sent");
			}
			return response;

		}

	}
}