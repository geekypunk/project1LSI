package com.cs5300.proj1b.rpc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.cs5300.proj1a.dao.SessionObject;
import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1b.rpc.Constants.Operation;

/** 
 * RPC server thread to handle session read and write, get view requests
 * @author Deepthi
 *
 */
public class RPCServer implements Runnable {

	/**
	 * Handles session read, write, get view requests
	 */
	@Override
	public void run() {
		DatagramSocket rpcSocket = null;
		DatagramPacket recvPkt  = null;
		try{
		rpcSocket = new DatagramSocket(Constants.port);
		int serverPort = rpcSocket.getLocalPort();
		while (true) {
			
			//Read the packet
			byte[] inBuf = new byte[Constants.maxPacketSize];
			recvPkt = new DatagramPacket(inBuf, inBuf.length);
			rpcSocket.receive(recvPkt);
			InetAddress returnAddr = recvPkt.getAddress();
			int returnPort = recvPkt.getPort();
			String data = new String(inBuf);
			String parts[] = data.split(Constants.delimiter);

			// here inBuf contains the callID and operationCode
			Constants.Operation operationCode = Constants.Operation.values()[Integer
					.parseInt(parts[1])];
			byte[] outBuf = null;

			switch (operationCode) {

			case SESSION_READ:
				// SessionRead accepts call args and returns call results
				outBuf = sessionRead(parts);
				break;
			
			case SESSION_WRITE:
				sessionWrite(parts);
				break;
				
			case GET_VIEW:
				getView(parts);
				break;

			}
			
			//For session read, get view requests
			if(outBuf != null){
				// here outBuf should contain the callID and results of the call
				DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,
						returnAddr, returnPort);
				rpcSocket.send(sendPkt);
			}
		}
		}
		catch(Exception e){
			recvPkt = null;
		}
		finally{
			rpcSocket.close();
		}
		
	}

	/**
	 * Returns the found version and data for given session ID
	 * @param parts - string array containing requested session ID and version number
	 * @return byte array representation of version and data, along with timestamp
	 */
	byte[] sessionRead(String[] parts) {
		//sesion id, new version, local, primary, backup
			String callId = parts[0];
			String sessionID = parts[2];
			String versionNo = parts[3];
			String response = "";
			if( SessionManager.sessionTable.containsKey(sessionID)){
				SessionObject sessionObj = SessionManager.sessionTable.get(sessionID);
				response = callId + Constants.delimiter + 
						sessionObj.getVersion() + Constants.delimiter + sessionObj.getMessage() + Constants.delimiter + 
						sessionObj.getExpirationDate();
			}
			else{
				response = callId + Constants.delimiter + -1;
			}
	
			return response.getBytes();
	}
	
	/**
	 * Writes the session data, version and time stamp for the given session ID
	 * @param parts - string array containing session ID, version, data and time stamp
	 */
	void sessionWrite(String[] parts) {
		try{
		//sesion id, new version, local, primary, backup
			String sessionID = parts[2];
			String message = parts[4];
			String discardTime = parts[5];
			
			//Create a new session object, with the given data
			SessionObject sessionObject = new SessionObject(message, Long.valueOf(discardTime));
			sessionObject.incrementVersionNo();
			
			//Replace the existing session information with the new version, this is same as garbage collecting the older versions
			
			SessionManager.sessionTable.put(sessionID, sessionObject);	
		}
		catch(Exception e){}
	}
	
	/**
	 * Returns the view for the given server ip address
	 * @param parts - string array containing caller id and operation id
	 * @return Set of IP addresses in view delimited by '$'
	 */
	byte[] getView(String[] parts) {
		
			//sesion id, new version, local, primary, backup
			String callId = parts[0];
			
			String response = callId;
			for(String view : SessionManager.views){
				response += Constants.delimiter + view;
			}
	
			return response.getBytes();
	}
	

}
