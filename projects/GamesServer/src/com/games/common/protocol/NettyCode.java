package com.games.common.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class NettyCode {
    
	private static final int msg_head_total_length 	= 16;
	
    /* package header:
     * +-----------------|-----------------|---------------+
     * | body_len(int32) | cmd_type(int32) | cmd_id(int64) |
     * +-----------------|-----------------|---------------+
     * 15----------------11----------------7---------------0
     * 
     * length (4 bytes, exclude length of header)
     * id (8 bytes, reply package with the same id)
     * flags (1 byte, digest algo, crypto algo, send or reply...)
     * sequence number(4 bytes, for thwart replay attack)
     * digest(for tampering prevention)
     * command|error code (4 bytes)
     */
	
	public static void encode(NetData msg, OutputStream os) throws IOException {		
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeLong(msg.cmdId);
		dos.writeInt(msg.cmdType);
		dos.writeInt((msg.flags << 24) | msg.dat.length);
		dos.write(msg.dat);
		dos.flush();
	}	
	
	public static class PacketHeader {
		public boolean legacy;
		public long cmdId;
		public int cmdType;
		public int bodyLen;
		public byte flags;
	}
	
    public static NetData decode(InputStream in, PacketHeader head) throws IOException {
		assert head != null;
		if (in == null)
			return null;
		DataInputStream dis = new DataInputStream(in);
		if (dis.available() < msg_head_total_length) {
			return null;
		}

		if (!head.legacy) {
			head.cmdId = dis.readLong();
			head.cmdType = dis.readInt();
			int bodyLen = dis.readInt();
			head.flags = (byte) (bodyLen >>> 24);
			head.bodyLen = bodyLen & 0x00ffffff;
			head.legacy = true;
		}
		long cmdId = head.cmdId;
		int cmdType = head.cmdType;
		int bodyLen = head.bodyLen;
		byte flags = head.flags;

//		System.out.println("body len:" + bodyLen + " cmd type:" + cmdType + " cmd id:" + cmdId + " available:" + dis.available());
		
		if (bodyLen < 0 || dis.available() < 0 || dis.available() < bodyLen) {
			return null;
		}

		byte[] decoded = new byte[bodyLen];
		dis.read(decoded);
		head.legacy = false;
		return new NetData(cmdType, cmdId, flags, decoded);
	}
    
	public static class MyEncoder extends OneToOneEncoder {
		@Override
		protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
			if (!(msg instanceof NetData)) return msg;
			
			NetData v = (NetData) msg;
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
			buf.writeLong(v.cmdId);
			buf.writeInt(v.cmdType);
			buf.writeInt((v.flags << 24) | v.dat.length);
			buf.writeBytes(v.dat);
//			System.out.println("send cmd type["+v.cmdType+"] id["+v.cmdId+"] body len["+v.dat.length+"]");
			return buf;
		}
	}

	public static class MyDecoder extends FrameDecoder {
		@Override
		protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {			
			if (buffer.readableBytes() < msg_head_total_length) {
				return null;
			}
			buffer.markReaderIndex();
			
			long cmdId = buffer.readLong();
			int cmdType = buffer.readInt();
			int bodyLen = buffer.readInt();
			byte flags = (byte) (bodyLen >>> 24);
			bodyLen &= 0x00ffffff;

			if (bodyLen < 0 || buffer.readableBytes() < 0) {
//			    System.out.println("body len:" + bodyLen + " cmd type:" + cmdType + " cmd id:" + cmdId + " readable:" + buffer.readableBytes());
				return null;
			}
			if (buffer.readableBytes() < bodyLen) {
				buffer.resetReaderIndex();
				return null;
			}
						
			byte[] decoded = new byte[bodyLen];
			buffer.readBytes(decoded);

			return new NetData(cmdType, cmdId, flags, decoded);
		}
	}	
}
