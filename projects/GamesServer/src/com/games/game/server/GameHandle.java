package com.games.game.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;

import com.games.common.protocol.NetData;
import com.games.common.protocol.ProtoBas;
import com.games.common.protocol.ProtoBas.eErrorCode;
import com.games.common.protocol.ProtoMsg.StartGameAns;
import com.games.common.protocol.ProtoMsg.StartGameReq;
import com.games.common.util.SchedulerService;
import com.google.protobuf.InvalidProtocolBufferException;

public class GameHandle {
    private static final Logger logger = Logger.getLogger(GameHandle.class);
    private static GameHandle gameHandle = new GameHandle();
    
    public GameServer server = null;
    
	private ExecutorService onDemandLoader = Executors.newCachedThreadPool();
	private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(2);	
	private SchedulerService scheduler = new SchedulerService(scheduleService);
	
    public static GameHandle getInstance() {
        return gameHandle;
    }
    
    private GameHandle() {
    }
    
    public boolean init() {    	
    		return true;
    }
        
    public boolean setupVigorScheduler(){
        return true;
    }
    
    public void close() {
	    	scheduleService.shutdown();
	    	onDemandLoader.shutdown();
    }
    
    public boolean load() {
   
        return true;
    }
    
	//处理所有的请求
	public void handleStartGame(Channel ch, NetData netData) throws InvalidProtocolBufferException {
		StartGameReq req = StartGameReq.parseFrom(netData.dat);
		
//		logger.debug(" ====&&&&&&==== " + req.getName() + req.getId() );
		StartGameAns.Builder ans = StartGameAns.newBuilder().setName(req.getName())
    			.setErrCode(eErrorCode.OK);
		
		logger.debug("[" + ans.getName() + "]: send[" + ans.getErrCode() + "] to [" + ch.getRemoteAddress() + "]");
	    	NetData wrapper = new NetData(netData.cmdType, netData.cmdId, (byte) 0, ans.build().toByteArray());
	    	server.fly(wrapper, ch, false);
	}
	
	public void handleQuitServer(Channel ch, NetData netData) {
		server.serverStop();
		logger.info("request server exit.");
	}	
}

