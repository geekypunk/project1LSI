package com.cs5300.proj1b.rpc;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.cs5300.proj1a.dao.SessionObject;
import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1b.views.ServerView;

/**
 * RPC server thread to handle session read and write, get view requests
 * 
 * @author Deepthi
 * 
 */
public class RPCServer implements Runnable {
	
	private static ServerView serverView;
	public RPCServer(ServletContext ctx){
		serverView = (ServerView) ctx.getAttribute("serverView");
	}
	private final static Logger LOGGER = Logger.getLogger(RPCServer.class
			.getName());

	/**
	 * Handles session read, write, get view requests
	 */
	@Override
	public void run() {
		DatagramSocket rpcSocket = null;
		DatagramPacket recvPkt = null;
		try {
			rpcSocket = new DatagramSocket(Constants.port);
			while (true) {
				// Read the packet
				byte[] inBuf = new byte[Constants.maxPacketSize];
				recvPkt = new DatagramPacket(inBuf, inBuf.length);
				rpcSocket.receive(recvPkt);
				InetAddress returnAddr = recvPkt.getAddress();
				int returnPort = recvPkt.getPort();
				String data = new String(inBuf);
				String parts[] = data.split(Constants.delimiter);

				// here inBuf contains the callID and operationCode
				Constants.Operation operationCode = Constants.Operation
						.values()[Integer.parseInt(parts[1])];
				byte[] outBuf = null;

				switch (operationCode) {

				case SESSION_READ:
					// SessionRead accepts call args and returns session data
					outBuf = sessionRead(parts);
					break;

				case SESSION_WRITE:
					// SessionWrite accepts call args, and updates session
					sessionWrite(parts);
					break;

				case GET_VIEW:
					// GetView accepts call args and returns view
					outBuf = getView(parts);
					break;

				}

				// For session read, get view requests
				if (outBuf != null) {
					// here outBuf should contain the callID and results of the
					// call
					DatagramPacket sendPkt = new DatagramPacket(outBuf,
							outBuf.length, returnAddr, returnPort);
					rpcSocket.send(sendPkt);

					LOGGER.info("Response sent");
				}
			}
		} catch (Exception e) {
			recvPkt = null;
		} finally {
			rpcSocket.close();
		}

	}

	/**
	 * Returns the found version and data for given session ID
	 * 
	 * @param parts
	 *            - string array containing requested session ID and version
	 *            number
	 * @return byte array representation of version and data, along with
	 *         timestamp
	 */
	byte[] sessionRead(String[] parts) {
		// sesion id, new version, local, primary, backup
		String callId = parts[0];
		String sessionID = parts[2];
		LOGGER.info("Received SESSION_READ request for session ID " + sessionID);
		String response = "";

		// Return session object if exists
		if (SessionManager.sessionTable.containsKey(sessionID)) {
			SessionObject sessionObj = SessionManager.sessionTable
					.get(sessionID);
			response = callId + Constants.delimiter + sessionObj.getVersion()
					+ Constants.delimiter + sessionObj.getMessage()
					+ Constants.delimiter + sessionObj.getExpirationDate();
		} else {
			response = callId + Constants.delimiter + -1;
		}
		LOGGER.info("Retrieved session for ID " + sessionID);
		return response.getBytes();
	}

	/**
	 * Writes the session data, version and time stamp for the given session ID
	 * 
	 * @param parts
	 *            - string array containing session ID, version, data and time
	 *            stamp
	 */
	void sessionWrite(String[] parts) {
		try {
			// sesion id, new version, local, primary, backup
			String sessionID = parts[2];
			String message = parts[4];
			String discardTime = parts[5];
			LOGGER.info("Received SESSION_WRITE request for session ID "
					+ sessionID);

			
			// Create a new session object, with the given data
			SessionObject sessionObject = new SessionObject(message,
					Long.valueOf(discardTime));
//			sessionObject.incrementVersionNo();

			// Replace the existing session information with the new version,
			// this is same as garbage collecting the older versions
			SessionManager.sessionTable.put(sessionID, sessionObject);
			LOGGER.info("Session updated for ID " + sessionID);
		} catch (Exception e) {
		}
	}

	/**
	 * Returns the view for the given server ip address
	 * 
	 * @param parts
	 *            - string array containing caller id and operation id
	 * @return Set of IP addresses in view delimited by '$'
	 */
	byte[] getView(String[] parts) {

		// sesion id, new version, local, primary, backup
		String callId = parts[0];

		LOGGER.info("Received GET_VIEW request");

		String response = callId;
		for (String view : serverView.getView()) {
			response += Constants.delimiter + view;
		}
		LOGGER.info("Retrieved view");
		return response.getBytes();
	}

}
