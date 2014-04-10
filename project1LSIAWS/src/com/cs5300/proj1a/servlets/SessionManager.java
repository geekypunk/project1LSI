package com.cs5300.proj1a.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
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
import com.cs5300.proj1b.rpc.RPCServer;
import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.View;

/**
 * Servlet implementation class SessionManager This class handles all requests
 * and session maintenance
 * 
 * @author kt466
 */
@WebServlet("/SessionManager")
public class SessionManager extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// Display message
	public static String defaultServerMsg = "Hello User!";

	// Global session table
	public static ConcurrentHashMap<String, SessionObject> sessionTable = new ConcurrentHashMap<String, SessionObject>();

	// Sets the expiration age for a cookie in milliseconds
	public static int cookieAge = 1000 * 60 * 5;

	// Cookie name
	public static String COOKIE_NAME = "CS5300PROJ1SESSION";
	private static String SERVER_NULL = "0.0.0.0";
	public static List<String> views = new ArrayList<String>();

	private static BootStrapView bootStrapView;
	public static View serverView;
	private Random randomGenerator;
	private static RPCClient rpcClient;
	private static RPCServer rpcServer;
	private final static Logger LOGGER = Logger.getLogger(SessionManager.class
			.getName());
	private static String new_backup;

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

		ServletContext ctx = config.getServletContext();
		bootStrapView = (BootStrapView) ctx.getAttribute("bootStrapView");
		serverView = (View) ctx.getAttribute("serverView");
		LOGGER.info("bootStrapView:" + bootStrapView.getView());
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
		rpcClient = new RPCClient();
		PrintWriter responseWriter = response.getWriter();

		// get HTTP GET request param
		String requestType = request.getParameter("param");

		String sessionIdFromCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals(COOKIE_NAME)) {
					sessionIdFromCookie = cookies[i].getValue();
					break;
				}
			}
		}

		try {
			if (requestType == null && sessionIdFromCookie == null) {

				// Create a new session object
				SessionObject sessionObj = new SessionObject(defaultServerMsg,
						Utils.getCurrentTimeInMillis() + cookieAge + SessionObject.DELTA);
				sessionObj.setMessage(defaultServerMsg);
				String sessionID = sessionObj.getSessionId();
				System.out.println("Creating session:" + sessionID);
				String tuple = sessionID + Constants.delimiter
						+ sessionObj.getVersion() + Constants.delimiter;
				// since its a new session object, primary server = local
				// server
				String[] parts = sessionID.split("_");
				String localServer = parts[1];

				new_backup = rpcClient.sessionWriteClient(
						new HashSet<String>(), sessionID,
						String.valueOf(sessionObj.getVersion()),
						sessionObj.getMessage(), sessionObj.getExpirationTs());

				String location_metadata = localServer + Constants.delimiter
						+ new_backup;
				List<String> destinationAddresses = new LinkedList<String>();
				destinationAddresses.add(localServer);

				String cookieValue = tuple + location_metadata + Constants.delimiter + localServer;
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
				String backupServer = parts[3];
				String cookieValue = sessionID + Constants.delimiter
						+ (Integer.parseInt(version) + 1) + Constants.delimiter
						+ primaryServer + Constants.delimiter + backupServer;
				String result = "";

				if (requestType != null && requestType.equals("Logout")) {

					// Handle logout button
					Cookie c = new Cookie(COOKIE_NAME, sessionIdFromCookie);
					// Causes cookie to be removed from the browser
					c.setMaxAge(0);

					System.out.println("Logging out of session:"
							+ sessionIdFromCookie);
					response.addCookie(c);
				} else {
					SessionObject sessionObj = null;
					List<String> destinationAddresses = Arrays
							.asList(new String[] { primaryServer, backupServer });
					String addressFound = Utils.SERVER_IP;

					// If the local server is same as primary or backup
					if (Utils.SERVER_IP.equals(primaryServer)
							|| Utils.SERVER_IP.equals(backupServer)) {
						sessionObj = sessionTable.get(sessionID);

					} else {

						String sessionData = rpcClient.sessionReadClient(
								destinationAddresses, sessionID, version);
						String data[] = sessionData.split(Constants.delimiter);
						addressFound = data[0];
						String foundVersion = String.valueOf(Integer
								.parseInt(data[2]));
						String message = data[3];
						sessionObj= new SessionObject(
								message, -1);
						sessionObj.setVersion(Integer.valueOf(foundVersion));
					}

					if (requestType != null && requestType.equalsIgnoreCase("replace")) {
						sessionObj.setMessage(request.getParameter("message"));
					}

					sessionObj.incrementVersionNo();
					sessionObj.setExpirationTs(Utils.getCurrentTimeInMillis()
							+ cookieAge + SessionObject.DELTA);
					if (Utils.SERVER_IP.equals(primaryServer)
							|| Utils.SERVER_IP.equals(backupServer)) {
						sessionTable.put(sessionID, sessionObj);
						String otherServer = Utils.SERVER_IP
								.equals(primaryServer) ? backupServer
								: primaryServer;
						destinationAddresses = Arrays
								.asList(new String[] { otherServer });
					}

					rpcClient.sessionWriteClient(new HashSet<String>(
							destinationAddresses), sessionID, String
							.valueOf(sessionObj.getVersion()), sessionObj
							.getMessage(), sessionObj.getExpirationTs());
					result = sessionObj.toString();
					

					System.out.println("Creating session:" + sessionID
							+ " with version " + (Integer.valueOf(version) + 1));
					// Old cookie is overwritten, all new requests will be
					// handled using this cookie.
					Cookie c = new Cookie(COOKIE_NAME, cookieValue + Constants.delimiter + addressFound);
					// This new cookie will have default expiration timeout
					c.setMaxAge(cookieAge / 1000);
					response.addCookie(c);

					responseWriter.write(result + "@" + c.getValue());
				}


			}
		} catch (Exception e) {
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
