package com.games.common.protocol;

public class NetData {
	static public final byte FLAG_ENCRYPTION = 0x1;
	static public final byte FLAG_COMPRESSION = 0x2;
	static public final byte FLAG_DIGESTION = 0x4;
	
	public NetData(int cmdType, long cmdId, byte flags, byte[] dat) {
		this.cmdType = cmdType;
		this.cmdId = cmdId;
		this.flags = flags;
		this.dat = dat;
	}

	public int cmdType;
	public long cmdId;
	public byte flags;
	public byte[] dat;
	public byte[] key;
	
	public boolean hasFlag(byte flag) {
		return (flags & flag) != 0;		
	}
	
	public void clearFlag(byte flag) {
		flags &= ~flag;
	}
	
	public void setFlag(byte flag) {
		flags |= flag;
	}

}
