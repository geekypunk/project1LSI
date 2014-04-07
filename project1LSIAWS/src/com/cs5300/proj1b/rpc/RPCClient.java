package com.cs5300.proj1b.rpc;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1b.rpc.Constants.Operation;

/**
 * RPC client to handle requests for session read and write, and get view.
 * @author Deepthi
 *
 */
public class RPCClient {
	static int callId = -1;

	/**
	 * Reads from primary and backup server, and returns found version and
	 * data(version number, expiration timestamp) for given session ID,
	 * delimited with '$'. Returns version number as -1, if the session is not
	 * found.
	 * 
	 * @param destinationAddresses
	 * @param sessionID
	 * @param sessionVersionNo
	 * @return String containing found version and data delimited by '$'
	 */
	public String sessionReadClient(List<String> destinationAddresses,
			String sessionID, String sessionVersionNo) {

		callId++;
		DatagramPacket recvPkt = null;
		DatagramSocket rpcSocket = null;
		try {
			rpcSocket = new DatagramSocket();

			// Unique call Id, operation ID, session ID, session version No
			String outBufString = callId + Constants.delimiter
					+ Operation.SESSION_READ + Constants.delimiter + sessionID
					+ Constants.delimiter + sessionVersionNo;
			byte[] outBuf = outBufString.getBytes();

			// sending to multiple [destAddr, destPort] pairs
			// using a single pre-existing DatagramSocket object rpcSocket
			for (String ipAddress : destinationAddresses) {
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, InetAddress.getByName(ipAddress),
						Constants.port);
				rpcSocket.send(sendPkt);
			}
			byte[] inBuf = new byte[Constants.maxPacketSize];
			String parts[] = null;
			recvPkt = new DatagramPacket(inBuf, inBuf.length);

			// Get the first response for the corresponding callID
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.setSoTimeout(Constants.timeout);
				rpcSocket.receive(recvPkt);
				String data = new String(recvPkt.getData());
				parts = data.split(Constants.delimiter);

			} while (!parts[0].equals(callId));

			// Add to views on getting response
			SessionManager.views.add(recvPkt.getAddress().getHostAddress());

		} catch (InterruptedIOException iioe) {

			// Remove from views on time out
			if (SessionManager.views.contains(recvPkt.getAddress().getHostAddress())) {
				SessionManager.views.remove(recvPkt.getAddress().getHostAddress());
			}
			recvPkt = null;

		} catch (IOException ioe) {
		} finally {
			rpcSocket.close();
		}
		return new String(recvPkt.getData());
	}

	/**
	 * Sends the session data, version and discard time to primary and backup
	 * server
	 * 
	 * @param destinationAddresses
	 * @param sessionID
	 * @param version
	 * @param data
	 * @param discardTime
	 */
	public void sessionWriteClient(List<String> destinationAddresses,
			String sessionID, String version, String data, long discardTime) {
		callId++;
		DatagramSocket rpcSocket = null;
		try {
			rpcSocket = new DatagramSocket();

			// Unique call Id, operation Id, session ID, version number, data,
			// discard time
			String outBufString = callId + Constants.delimiter
					+ Operation.SESSION_WRITE + Constants.delimiter + sessionID
					+ Constants.delimiter + version + Constants.delimiter
					+ data + Constants.delimiter + discardTime;
			byte[] outBuf = outBufString.getBytes();

			// sending to multiple [destAddr, destPort] pairs
			// using a single pre-existing DatagramSocket object rpcSocket
			for (String ipAddress : destinationAddresses) {
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, InetAddress.getByName(ipAddress),
						Constants.port);
				rpcSocket.send(sendPkt);
			}
		} catch (IOException e) {
		} finally {
			rpcSocket.close();
		}
	}

	/**
	 * Returns view of the given server
	 * 
	 * @param - ipAddress - Server's ip address, for which the view is requested
	 * @return - Requested view
	 */
	public List<String> getView(String ipAddress) {
		callId++;

		List<String> view = new ArrayList<String>();
		DatagramSocket rpcSocket = null;
		DatagramPacket recvPkt = null;
		try {
			
			// Unique call Id, operation ID, session ID, session version No
			String outBufString = callId + Constants.delimiter
					+ Operation.GET_VIEW;
			byte[] outBuf = outBufString.getBytes();
			
			//Send request to get view
			rpcSocket = new DatagramSocket();
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length,
					InetAddress.getByName(ipAddress), Constants.port);
			rpcSocket.send(sendPkt);

			byte[] inBuf = new byte[Constants.maxPacketSize];
			recvPkt = new DatagramPacket(inBuf, inBuf.length);

			String parts[] = null;

			// Get the first response for the corresponding callID
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.setSoTimeout(Constants.timeout);
				rpcSocket.receive(recvPkt);
				String data = new String(recvPkt.getData());
				parts = data.split(Constants.delimiter);
			} while (!parts[0].equals(callId));
			
			// Add to views on getting response
			SessionManager.views.add(recvPkt.getAddress().getHostAddress());
			
			//Add to this server's view
			for(int i=1 ; i < parts.length; i ++){
				view.add(parts[i]);
			}

		} catch (InterruptedIOException iioe) {

			// Remove from view on time out
			if (SessionManager.views.contains(recvPkt.getAddress().getHostAddress())) {
				SessionManager.views.remove(recvPkt.getAddress().getHostAddress());
			}
			recvPkt = null;

		} catch (IOException ioe) {
		} finally {
			rpcSocket.close();
		}
		
		
		return view;
	}

}
