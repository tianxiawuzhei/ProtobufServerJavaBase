package com.games.common.net;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.games.common.protocol.NettyCode;
import com.games.common.util.Utils;
import com.games.game.server.NettyServerHandler;

public class NettyEnter {
	private static final Logger logger = Logger.getLogger(NettyEnter.class);

	private ChannelGroup allChannels = new DefaultChannelGroup();
	private ServerBootstrap bootstrap;
	
	public NettyEnter() throws IOException {
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline p = pipeline();
                p.addLast("decoder", new NettyCode.MyDecoder());
                p.addLast("encoder", new NettyCode.MyEncoder());
                p.addLast("handler", new NettyServerHandler());
                return p;
            }
        };
	    
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(pipelineFactory);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
								
		Channel serverChannel = bootstrap.bind(new InetSocketAddress(9755));
		allChannels.add(serverChannel);      
				
		System.out.println("network init done. listen at "+serverChannel.getLocalAddress()+"");
	}
	
	public void close() { 
		allChannels.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();			
	}
	
	public void addChannel(Channel ch) {
	    allChannels.add(ch);
	}
		
}
