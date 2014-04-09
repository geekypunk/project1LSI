package com.cs5300.proj1b.rpc;

public class Constants {
	public enum Operation {
		SESSION_READ, SESSION_WRITE, GET_VIEW
	};
	
	public final static int maxPacketSize = 512;
	public final static int port = 5300;
	public final static String delimiter = "$";
	public final static int timeout = 30000;  //30 seconds
	public final static String NULL_ADDRESS = "0.0.0.0";
}
