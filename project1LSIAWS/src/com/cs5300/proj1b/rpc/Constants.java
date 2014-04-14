package com.cs5300.proj1b.rpc;

/**
 * Constant used during RPC communications
 *
 */
public class Constants {
	
	/**
	 * Specify the RPC operation codes
	 *
	 */
	public enum Operation {
		SESSION_READ, SESSION_WRITE, GET_VIEW
	};
	
	public final static int maxPacketSize = 512;
	public final static int port = 5300;
	public final static String delimiter = "#";
	public final static int timeout = 5000;  //5 seconds
	public final static String NULL_ADDRESS = "0.0.0.0";
}
