package com.cs5300.proj1b.rpc;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.Constants.Operation;
import com.cs5300.proj1b.views.ServerView;

/**
 * RPC client to handle requests for session read and write, and get view.
 * 
 * @author Deepthi
 * 
 */
public class RPCClient {
	private static ServerView serverView;

	public RPCClient(ServletContext ctx) {
		serverView = (ServerView) ctx.getAttribute("serverView");
	}

	static int callId = -1;
	private final static Logger LOGGER = Logger.getLogger(RPCClient.class
			.getName());

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
		int tempCallId = callId;
		DatagramPacket recvPkt = null;
		DatagramSocket rpcSocket = null;
		try {
			rpcSocket = new DatagramSocket();

			// Unique call Id, operation ID, session ID, session version No
			String outBufString = tempCallId + Constants.delimiter
					+ Operation.SESSION_READ + Constants.delimiter + sessionID
					+ Constants.delimiter + sessionVersionNo
					+ Constants.delimiter;
			byte[] outBuf = outBufString.getBytes();

			// sending to multiple [destAddr, destPort] pairs
			// using a single pre-existing DatagramSocket object rpcSocket
			for (String ipAddress : destinationAddresses) {

				// Do not send to NULL server
				if (Constants.NULL_ADDRESS.equals(ipAddress))
					continue;

				LOGGER.info("Sending SESSION_READ request to server "
						+ ipAddress);
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, InetAddress.getByName(ipAddress),
						Constants.port);
				rpcSocket.send(sendPkt);
			}
			byte[] inBuf = new byte[Constants.maxPacketSize];
			String parts[] = null;
			recvPkt = new DatagramPacket(inBuf, inBuf.length);

			String receiverIpAddress = "";
			// Get the first response for the corresponding callID
			do {
				recvPkt.setLength(inBuf.length);
				rpcSocket.setSoTimeout(Constants.timeout);
				rpcSocket.receive(recvPkt);
				receiverIpAddress = recvPkt.getAddress().getHostAddress();
				LOGGER.info("Received response from server "
						+ receiverIpAddress);
				String data = new String(recvPkt.getData());
				parts = data.split(Constants.delimiter);

			} while (!parts[0].equals(String.valueOf(tempCallId))
					&& !parts[1].equals("-1"));

			// Add to views on getting response
			serverView.insert(recvPkt.getAddress().getHostAddress());
			LOGGER.info("Inserted " + recvPkt.getAddress().getHostAddress()
					+ " to views");

		} catch (SocketTimeoutException ste) {

			for (String ipAddress : destinationAddresses) {
				serverView.remove(ipAddress);
				LOGGER.warning("Timeout occurred. Removed server " + ipAddress
						+ " from views");

			}

			recvPkt = null;

		} catch (InterruptedIOException iioe) {

			// Remove from views on time out
			for (String ipAddress : destinationAddresses) {
				serverView.remove(ipAddress);
				LOGGER.warning("InterruptedIOException occurred. Removed server "
						+ ipAddress + " from views");

			}

			recvPkt = null;

		} catch (IOException ioe) {
			LOGGER.warning("An IO error occurred attempting to read the session for session ID "
					+ sessionID);
			LOGGER.warning(Utils.getStackTrace(ioe));
		} finally {
			rpcSocket.close();
		}
		return new String(recvPkt.getAddress().getHostAddress()
				+ Constants.delimiter + recvPkt.getData());
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
	 * 
	 * @return IP of the server responded
	 */
	public String sessionWriteClient(Set<String> destinationAddresses,
			String sessionID, String version, String data, long discardTime) {
		callId++;
		int tempCallId = callId;
		boolean FIND_BACKUP = false;
		DatagramSocket rpcSocket = null;
		try {
			rpcSocket = new DatagramSocket();

			if (destinationAddresses.isEmpty()) {
				FIND_BACKUP = true;
				destinationAddresses = new HashSet<String>(serverView.getView());
				if (destinationAddresses.isEmpty()) {
					return Constants.NULL_ADDRESS;
				}
			}

			// Unique call Id, operation Id, session ID, version number, data,
			// discard time
			String outBufString = tempCallId + Constants.delimiter
					+ Operation.SESSION_WRITE + Constants.delimiter + sessionID
					+ Constants.delimiter + version + Constants.delimiter
					+ data + Constants.delimiter + discardTime
					+ Constants.delimiter;
			byte[] outBuf = outBufString.getBytes();

			// sending to multiple [destAddr, destPort] pairs
			// using a single pre-existing DatagramSocket object rpcSocket
			for (String ipAddress : destinationAddresses) {

				// Do not send to NULL server
				if (Constants.NULL_ADDRESS.equals(ipAddress))
					continue;

				LOGGER.info("Sending SESSION_WRITE request to server "
						+ ipAddress);
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, InetAddress.getByName(ipAddress),
						Constants.port);
				rpcSocket.send(sendPkt);

				// wait for response
				String parts[] = null;
				byte[] inBuf = new byte[10];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String receiverIpAddress = "";
				try {
					// Get the first response for the corresponding callID
					do {
						recvPkt.setLength(inBuf.length);
						rpcSocket.setSoTimeout(Constants.timeout);
						rpcSocket.receive(recvPkt);
						receiverIpAddress = recvPkt.getAddress()
								.getHostAddress();
						LOGGER.info("Received response from server "
								+ receiverIpAddress);
						data = new String(recvPkt.getData());
						parts = data.split(Constants.delimiter);

					} while (!parts[0].equals(String.valueOf(tempCallId)));
				} catch (InterruptedIOException ie) {
					recvPkt = null;

					// Remove from views on time out

					serverView.remove(ipAddress);
					LOGGER.warning("Timeout occurred. Removed server "
							+ ipAddress + " from views");
					return Constants.NULL_ADDRESS;

				}

				if (recvPkt != null) {
					// Add to views on getting response
					serverView.insert(recvPkt.getAddress().getHostAddress());
					LOGGER.info("Inserted "
							+ recvPkt.getAddress().getHostAddress()
							+ " to views");

					if (FIND_BACKUP)
						return recvPkt.getAddress().getHostAddress();
				}

			}
		} catch (IOException e) {
			LOGGER.warning("An IO error occurred attempting to write session with ID "
					+ sessionID);
			LOGGER.warning(Utils.getStackTrace(e));
		} finally {
			rpcSocket.close();
		}

		// return null address, if no backup is found
		return Constants.NULL_ADDRESS;
	}

	/**
	 * Returns view of the given server. Returns empty list if there is no
	 * successful response
	 * 
	 * @param - ipAddress - Server's ip address, for which the view is requested
	 * @return - Requested view
	 */
	public HashSet<String> getView() {
		callId++;
		int tempCallId = callId;

		HashSet<String> view = new HashSet<String>();
		DatagramSocket rpcSocket = null;
		DatagramPacket recvPkt = null;

		// Choose a random server
		String ipAddress = serverView.choose();
		if (ipAddress.equals(Constants.NULL_ADDRESS))
			return view;

		int attempt = 0;
		while (true) {

			try {
				// Unique call Id, operation ID, session ID, session version No
				String outBufString = tempCallId + Constants.delimiter
						+ Operation.GET_VIEW + Constants.delimiter;
				byte[] outBuf = outBufString.getBytes();

				// Send request to get view
				LOGGER.info("Sending GET_VIEW request to server " + ipAddress);
				rpcSocket = new DatagramSocket();
				DatagramPacket sendPkt = new DatagramPacket(outBuf,
						outBuf.length, InetAddress.getByName(ipAddress),
						Constants.port);
				rpcSocket.send(sendPkt);

				byte[] inBuf = new byte[Constants.maxPacketSize];
				recvPkt = new DatagramPacket(inBuf, inBuf.length);

				String parts[] = null;

				// Get the first response for the corresponding callID
				do {
					recvPkt.setLength(inBuf.length);
					rpcSocket.setSoTimeout(Constants.timeout);
					rpcSocket.receive(recvPkt);
					LOGGER.info("Received response from server "
							+ recvPkt.getAddress().getHostAddress());
					String data = new String(recvPkt.getData());
					parts = data.split(Constants.delimiter);
				} while (!parts[0].equals(String.valueOf(tempCallId)));

				// Add to this server's view
				for (int i = 1; i < parts.length - 1; i++) {
					view.add(parts[i]);
				}

				// Add to views on getting response
				serverView.insert(recvPkt.getAddress().getHostAddress());
				LOGGER.info("Inserted " + recvPkt.getAddress().getHostAddress()
						+ " to views");

				return view;

			} catch (InterruptedIOException iioe) {

				// Remove from views on time out
				serverView.remove(ipAddress);
				LOGGER.warning("Timeout occurred. Removed server " + ipAddress
						+ " from views");

				recvPkt = null;

			} catch (IOException ioe) {
				LOGGER.warning("An IO error occurred attempting to get view for server "
						+ ipAddress);
				LOGGER.warning(Utils.getStackTrace(ioe));
			} finally {
				attempt++;

				// Do not try for more than view size
				if (attempt == ServerView.viewSize) {
					return view;
				}

				ipAddress = serverView.choose();
				if (ipAddress.equals(Constants.NULL_ADDRESS))
					return view;
				rpcSocket.close();

			}
		}

	}
}
