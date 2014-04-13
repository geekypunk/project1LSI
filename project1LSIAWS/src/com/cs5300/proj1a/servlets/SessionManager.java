package com.cs5300.proj1a.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cs5300.proj1a.dao.SessionObject;
import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.Constants;
import com.cs5300.proj1b.rpc.RPCClient;
import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.ServerView;

/**
 * Servlet implementation class SessionManager This class handles all requests
 * and session maintenance
 * 
 * @author kt466
 */
@WebServlet("/SessionManager")
public class SessionManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final int k = 2;

	// Display message
	public static String DEFAULT_MSG = "Hello User!";

	// Global session table
	public static ConcurrentHashMap<String, SessionObject> sessionTable = new ConcurrentHashMap<String, SessionObject>();

	// Sets the expiration age for a cookie in milliseconds
	public static int cookieAge = 1000 * 60 * 5;

	// Cookie name
	public static String COOKIE_NAME = "CS5300PROJ1SESSION";
	public static List<String> views = new ArrayList<String>();

	private ServletContext servletContext;
	private static BootStrapView bootStrapView;
	//public static ServerView serverView;
	private static ServerView serverView;
	private static RPCClient rpcClient;
	private final static Logger LOGGER = Logger.getLogger(SessionManager.class
			.getName());
	private static HashSet<String> new_backup;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SessionManager() {

		super();

	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {

		servletContext = config.getServletContext();
		bootStrapView = (BootStrapView) servletContext.getAttribute("bootStrapView");
		serverView = (ServerView) servletContext.getAttribute("serverView");
		LOGGER.info("bootStrapView:" + bootStrapView.getAsServerView());
		LOGGER.info("serverView:" + serverView.getView());
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {

		// Enable garbage collection
		sessionTable.clear();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// When the servlet is accessed by itself, HTTP GET happens
		response.setContentType("text/html");
		rpcClient = new RPCClient(servletContext);
		PrintWriter responseWriter = response.getWriter();

		// get HTTP GET request param
		String requestType = request.getParameter("param");

		String sessionIdFromCookie = null;
		Cookie requestCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(COOKIE_NAME)) {
					requestCookie = cookies[i];
					sessionIdFromCookie = cookies[i].getValue();
					break;
				}
			}
		}

		try {
			if (requestType == null && sessionIdFromCookie == null) {

				// Create a new session object
				SessionObject sessionObj = new SessionObject(DEFAULT_MSG,
						Utils.getCurrentTimeInMillis() + cookieAge);
				sessionObj.setMessage(DEFAULT_MSG);
				String sessionID = sessionObj.getSessionId();
				System.out.println("Creating session:" + sessionID);
				String tuple = sessionID + Constants.delimiter
						+ sessionObj.getVersion() + Constants.delimiter;
				// since its a new session object, primary server = local
				// server
				String[] parts = sessionID.split("_");
				String localServer = parts[1];
				String backupServer = "";

				
					new_backup = rpcClient.sessionWriteClient(
							new HashSet<String>(), sessionID,
							String.valueOf(sessionObj.getVersion()),
							sessionObj.getMessage(), sessionObj.getExpirationTs(), k);
					for(String t : new_backup){
						backupServer += t + '_';
					}
					
				

				String location_metadata = localServer + Constants.delimiter
						+ backupServer;
				List<String> destinationAddresses = new LinkedList<String>();
				destinationAddresses.add(localServer);

				String cookieValue = tuple + location_metadata + Constants.delimiter + localServer + Constants.delimiter;
				Cookie c = new Cookie(COOKIE_NAME, cookieValue);
				c.setMaxAge(cookieAge / 1000);
				sessionTable.put(sessionID, sessionObj);

				response.addCookie(c);
				responseWriter.write(sessionObj.toString() + "@" + c.getValue());

			} else {

				// A GET request(i.e with no params, or landing page
				// request)
				// should return a new session object with incremented
				// version number.
				// Hitting F5 will also invoke the below code

				String parts[] = sessionIdFromCookie.split(Constants.delimiter);
				String sessionID = parts[0];
				String version = parts[1];
				String primaryServer = parts[2];
				String[] backups = parts[3].split("_");
				
				new_backup = new HashSet<String>();
				for(int x = 0; x < backups.length; x ++){
					if(!backups[x].isEmpty())
						new_backup.add(backups[x]);
				}
			
				
	
				String result = "";

				if (requestType != null && requestType.equals("logout")) {

					// Handle logout button
					Cookie c = new Cookie(COOKIE_NAME, sessionIdFromCookie);
					// Causes cookie to be removed from the browser
					c.setMaxAge(0);

					System.out.println("Logging out of session:"
							+ sessionIdFromCookie);
					response.addCookie(c);
				} else {
					SessionObject sessionObj = null;
					List<String> destinationAddresses = new ArrayList<String>();
					destinationAddresses.add(primaryServer);
					
					String addressFound = Utils.SERVER_IP;

					// If the local server is same as primary or backup
					if (Utils.SERVER_IP.equals(primaryServer)
							|| new_backup.contains(Utils.SERVER_IP)) {
						sessionObj = sessionTable.remove(sessionID);

						if(sessionObj == null){
							// Create a new session object
							sessionObj = new SessionObject(DEFAULT_MSG,
									Utils.getCurrentTimeInMillis() + cookieAge);
							sessionObj.setMessage(DEFAULT_MSG);
						}
					} else {

						String sessionData = rpcClient.sessionReadClient(
								destinationAddresses, sessionID, version);
						String data[] = sessionData.split(Constants.delimiter);
						if(data.length < 3){
							/*sessionObj = new SessionObject(DEFAULT_MSG,
									Utils.getCurrentTimeInMillis() + cookieAge);
							sessionObj.setMessage(DEFAULT_MSG);
							new_backup.clear();*/
							responseWriter.write("Both primary and backup servers failed to respond before time out. Please logout to create a new session");
							requestCookie.setMaxAge(0);
							response.addCookie(requestCookie);
							return;
						}
						else{
						addressFound = data[0];
						String foundVersion = String.valueOf(Integer
								.parseInt(data[2]));
						String message = data[3];
						sessionObj= new SessionObject(
								message, -1);
						sessionObj.setVersion(Integer.valueOf(foundVersion));
						}
					}
					
					if(new_backup.size() < k){
						new_backup.addAll(rpcClient.sessionWriteClient(
								new HashSet<String>(), sessionID,
								String.valueOf(sessionObj.getVersion()),
								sessionObj.getMessage(), sessionObj.getExpirationTs(), (k - new_backup.size())));			
						
					}
					
					String backupServer = "";
					for(String t : new_backup){
						backupServer += t + "_";
						destinationAddresses.add(t);
					}
					String cookieValue = sessionID + Constants.delimiter
							+ (Integer.parseInt(version) + 1) + Constants.delimiter
							+ primaryServer + Constants.delimiter + backupServer;

					if (requestType != null && requestType.equalsIgnoreCase("replace")) {
						sessionObj.setMessage(request.getParameter("message"));
					}

					sessionObj.incrementVersionNo();
					sessionObj.setExpirationTs(Utils.getCurrentTimeInMillis()
							+ cookieAge + SessionObject.DELTA);
					if (Utils.SERVER_IP.equals(primaryServer)
							|| new_backup.contains(Utils.SERVER_IP)) {
						sessionTable.put(sessionID, sessionObj);
						destinationAddresses.remove(Utils.SERVER_IP);
					}

					rpcClient.sessionWriteClient(new HashSet<String>(
							destinationAddresses), sessionID, String
							.valueOf(sessionObj.getVersion()), sessionObj
							.getMessage(), sessionObj.getExpirationTs(), k);
					result = sessionObj.toString();
					

					System.out.println("Creating session:" + sessionID
							+ " with version " + (Integer.valueOf(version) + 1));
					// Old cookie is overwritten, all new requests will be
					// handled using this cookie.
					Cookie c = new Cookie(COOKIE_NAME, cookieValue + Constants.delimiter + addressFound + Constants.delimiter);
					// This new cookie will have default expiration timeout
					c.setMaxAge(cookieAge / 1000);
					response.addCookie(c);

					responseWriter.write(result + "@" + c.getValue());
				}


			}
		} catch (Exception e) {
			if(requestCookie!=null){
				requestCookie.setMaxAge(0);
				response.addCookie(requestCookie);
			}
			responseWriter.write("You are using an invalid cookie.");
			LOGGER.info(Utils.getStackTrace(e));
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

}
