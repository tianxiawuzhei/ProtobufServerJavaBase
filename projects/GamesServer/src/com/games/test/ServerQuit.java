package com.games.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import redis.clients.jedis.Protocol.Command;

import com.games.common.protocol.NetData;
import com.games.common.protocol.NettyCode;
import com.games.common.protocol.ProtoBas.eCommand;
import com.games.common.protocol.ProtoMsg.CommonReq;

public class ServerQuit {
	private static final Logger logger = Logger.getLogger(ServerQuit.class);
	
	public static final String INBORN_LOG_CONFIG = "config/log4j.properties";	
	
	public static void quitSafely(String ipv4, int port) throws IOException {		
		final int req_timeout_duration = 60 * 1000; // by millisecond

		Socket socket = new Socket();
		socket.setTcpNoDelay(true);
		socket.setSoTimeout(req_timeout_duration);
		socket.connect(new InetSocketAddress(ipv4, port));
		
		CommonReq.Builder builder = CommonReq.newBuilder();
				
		NetData cocoon = new NetData(eCommand.QUIT_SERVER.getNumber(), 0, (byte)0, builder.build().toByteArray());		
		NettyCode.encode(cocoon, socket.getOutputStream());
		
		socket.close();
	}

	public static void main(String[] args) {		
		PropertyConfigurator.configure(INBORN_LOG_CONFIG);
		String ipv4 = "localhost";
//		int port = Integer.parseInt(args[0]);
		int port = 9755;
	
		try {          
			quitSafely(ipv4, port);
			String fingerprint = "send the 'EXIT' command at " + (new Date()) + ".";
			logger.info(fingerprint);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
	}

}
