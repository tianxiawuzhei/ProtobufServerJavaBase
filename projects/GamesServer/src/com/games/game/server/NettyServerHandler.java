package com.games.game.server;


import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.games.common.protocol.NetData;
import com.games.common.protocol.ProtoBas;
import com.games.common.protocol.ProtoBas.eCommand;

public class NettyServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger logger = Logger.getLogger(NettyServerHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		logger.debug("some message received by thread id["+Thread.currentThread().getId()+"]");
		
		if (!(e.getMessage() instanceof NetData)) {
		    logger.debug("it isnt my type");
			ctx.sendUpstream(e);
			return;
		}
			
		NetData netData = (NetData) e.getMessage();
		try {
			if (netData.hasFlag(NetData.FLAG_ENCRYPTION)) {
				netData.clearFlag(NetData.FLAG_ENCRYPTION);
			}
			
			GameServer.getInst().addMsg(new GameServer.ReqDat(e.getChannel(), netData));
			logger.debug("queue size["+GameServer.getInst().getReqQueueSize()+"]");
		} catch (InterruptedException e1) {			
		} catch (Exception ex) {			
		} 	
		
	}

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        GameServer.getInst().getNettyEnter().addChannel(e.getChannel());
    }
	
}
