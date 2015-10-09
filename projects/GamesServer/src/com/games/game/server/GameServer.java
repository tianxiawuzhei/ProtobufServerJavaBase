package com.games.game.server;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jboss.netty.channel.Channel;

import com.games.common.net.NettyEnter;
import com.games.common.protocol.NetData;
import com.games.common.protocol.ProtoBas.eCommand;
import com.games.common.util.Utils;
import com.google.protobuf.InvalidProtocolBufferException;

public class GameServer {
	private static final Logger logger = Logger.getLogger(GameServer.class);

	private NettyEnter nettyEnter; // network
	private LinkedBlockingQueue<ReqDat> reqQ; // request queue
	private GameHandle gameHandle;

	private ExecutorService requestExecutor = Executors
			.newSingleThreadExecutor();
	private Future<?> requestFuture;

	public static final ReqDat SERVER_QUIT = new ReqDat(null, null);

	public static class ReqDat {
		public ReqDat(Channel channel, NetData cocoon) {
			this.channel = channel;
			this.cocoon = cocoon;
		}

		public Channel channel;
		public NetData cocoon;
	}

	private static class LazyHolder {
		public static final GameServer INSTANCE = new GameServer();
	}

	public static GameServer getInst() {
		return LazyHolder.INSTANCE;
	}

	private GameServer() {
		gameHandle = GameHandle.getInstance();
		gameHandle.server = this;
	}

	public boolean init() {
		if (!gameHandle.init()) {
			logger.error("init server failed.");
			return false;
		}

		reqQ = new LinkedBlockingQueue<ReqDat>(100);

		gameHandle.load();

		try {
			nettyEnter = new NettyEnter();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	public void startWork() {
		requestFuture = requestExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					handle();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		});

	}

	public void exit() {
		logger.debug("exiting...");
		logger.debug("stop server...");

		if (nettyEnter != null)
			nettyEnter.close();

		if (reqQ != null)
			reqQ.clear();

		requestExecutor.shutdown();
		gameHandle.close();

		logger.debug("server over.");
	}

	public void serverStop() {
		boolean succ = false;
		do {
			try {
				succ = addMsg(SERVER_QUIT);
			} catch (InterruptedException e) {
			}
		} while (!succ);
	}

	public void fly(NetData cocoon, Channel channel, boolean needEncrypt) {
		channel.write(cocoon);
	}

	public void dispatch(Channel ch, eCommand cmd, NetData netData)
			throws InvalidProtocolBufferException {
		assert ch != null;
		assert cmd != null;
		assert netData != null;

		switch (cmd) {
		case START_GAME:
			gameHandle.handleStartGame(ch, netData);
			break;

		case QUIT_SERVER:
			gameHandle.handleQuitServer(ch, netData);
			break;

		default:
			// no impl yet
			break;
		}
	}

	private void handle() throws InterruptedException {
		StringBuffer sb = new StringBuffer(128);
		while (true) {
			ReqDat reqdat = reqQ.take();
			if (null == reqdat)
				continue;
			if (reqdat == SERVER_QUIT)
				break;
			Channel ch = reqdat.channel;
			NetData cocoon = reqdat.cocoon;
			eCommand cmd = eCommand.valueOf(cocoon.cmdType);
			if (cmd == null)
				continue;
			if (ch != null) {
				sb.delete(0, sb.length()).append("accept cmd[").append(cmd)
						.append("] from [").append(ch.getRemoteAddress())
						.append("]");
				logger.debug(sb.toString());
			}

			long startTime = System.currentTimeMillis();

			try {
				dispatch(ch, cmd, cocoon);
			} catch (InvalidProtocolBufferException e) {
				logger.error(e.getMessage(), e);
				continue;
			}

			long offTime = System.currentTimeMillis() - startTime;
			if (offTime > 1000) {
				sb.delete(0, sb.length()).append("***slow cmd[").append(cmd)
						.append("] costs[").append(ch.getRemoteAddress())
						.append("]ms***");
				logger.debug(sb.toString());
			}
		}

	}

	public NettyEnter getNettyEnter() {
		return this.nettyEnter;
	}

	public void awaitTerm() {
		try {
			requestFuture.get();
		} catch (CancellationException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} catch (ExecutionException e) {
			logger.error(e.getMessage(), e);
			if (e.getMessage().equals("shutdown request received!")) {
			}
		}
	}

	// 主入口
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure(Utils.INBORN_LOG_CONFIG);

		final GameServer server = getInst();
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		boolean isOk = server.init();
		if (!isOk) {
			logger.error("init failed.");
			return;
		}
		logger.info("init completed, wait your command...");

		logger.info("server ready.");
		server.startWork();

		server.awaitTerm();

		server.exit();
		logger.info("server exit.");
	}

	public boolean addMsg(ReqDat req) throws InterruptedException {
		return reqQ.offer(req, 500L, TimeUnit.MILLISECONDS);
	}

	public int getReqQueueSize() {
		return reqQ.size();
	}
}
