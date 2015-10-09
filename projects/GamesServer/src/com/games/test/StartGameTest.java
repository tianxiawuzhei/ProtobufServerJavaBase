package com.games.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.games.common.protocol.NetData;
import com.games.common.protocol.NettyCode;
import com.games.common.protocol.ProtoBas;
import com.games.common.protocol.ProtoMsg.StartGameAns;
import com.games.common.protocol.ProtoMsg.StartGameReq;

public class StartGameTest {
	private static final Logger logger = Logger.getLogger(StartGameTest.class);
	
	public static final String INBORN_LOG_CONFIG = "config/log4j.properties";	

	public static void startGame(String ipv4, int port, String name, int id)
			throws IOException {
		final int req_timeout_duration = 60 * 1000; // by millisecond

		Socket socket = new Socket();
		socket.setTcpNoDelay(true);
		socket.setSoTimeout(req_timeout_duration);
		socket.connect(new InetSocketAddress(ipv4, port));

		long now = System.currentTimeMillis();
		StartGameReq.Builder builder = StartGameReq.newBuilder();
		builder.setName(name).setId(id);

		NetData cocoon = new NetData(ProtoBas.eCommand.START_GAME.getNumber(),
				0, (byte) 0, builder.build().toByteArray());
		NettyCode.encode(cocoon, socket.getOutputStream());
		socket.close();
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure(INBORN_LOG_CONFIG);
		
		String ipv4 = "192.168.10.51";
		try {
			startGame(ipv4, 9755, "dddddd", 15);
		} catch (IOException e) {
			e.printStackTrace();
			// logger.error(e.getMessage(), e);
		}

	}

}
