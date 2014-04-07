package com.cs5300.proj1a.servlets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cs5300.proj1a.dao.SessionObject;
import com.cs5300.proj1a.utils.Utils;

/**
 * Servlet implementation class SessionManager
 * This class handles all requests and session maintenance
 * @author kt466
 */
@WebServlet("/SessionManager")
public class SessionManager extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//Display message 
	public static String defaultServerMsg = "Hello User!"; 
   
    //Global session table
    public static ConcurrentHashMap<String,SessionObject> sessionTable
    			= new ConcurrentHashMap<String,SessionObject>();
   
    //Sets the expiration age for a cookie in milliseconds
    public static int cookieAge = 1000*60*5; 
    
    //Cookie name
    public static String COOKIE_NAME="CS5300PROJ1SESSION";

	public static List<String> views = new ArrayList<String>();
    
    private final static Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SessionManager() {
       
    	super();
       
    }

    /**
	 * @see Servlet#init(ServletConfig)
	 */
    public void init(ServletConfig config)
            throws ServletException{
    	
    	
    }
    /**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		
		//Enable garbage collection
		sessionTable.clear();
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// When the servlet is accessed by itself, HTTP GET happens
		response.setContentType("text/html");
		
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
	    
	    try{
	    if(requestType ==null){
		    if (sessionIdFromCookie == null) {
		   
		    	//Create a new session object
		    	SessionObject sessionObj = new SessionObject(defaultServerMsg,Utils.getCurrentTimeInMillis()+cookieAge);
		    	sessionObj.setMessage(defaultServerMsg);
		    	String sessionID = sessionObj.getSessionId();
		    	System.out.println("Creating session:"+sessionID);
		        Cookie c = new Cookie(COOKIE_NAME, sessionID);
		        c.setMaxAge(cookieAge/1000);
		        sessionTable.put(sessionID, sessionObj);
		        response.addCookie(c);
		        responseWriter.write(sessionObj.toString());
		   
		    }else{
		    
		    	//A GET request(i.e with no params, or landing page request) 
		    	//should return a new session object with incremented version number.
		    	//Hitting F5 will also invoke the below code
		    	SessionObject sessionObj = sessionTable.get(sessionIdFromCookie);
		    	sessionObj.incrementVersionNo();
		    	sessionObj.setExpirationTs(Utils.getCurrentTimeInMillis()+cookieAge);
		    	String sessionID = sessionObj.getSessionId();
		    	//Create a new session object with incremented version
		    	//SessionObject newSessionObj = generateNewSessionObjectWithIncrementedVersion(sessionObj);
		    	//For new object
		    	//String sessionIDWithVersion = newSessionObj.getSessionIdWithVersion();
		    	//Put the new session object with incremented session version in the session table
		    	//sessionTable.put(sessionIDWithVersion, newSessionObj);
		    	
		    	System.out.println("Creating session:"+sessionID+" with version :"+sessionObj.getVersion());
		    	//Old cookie is overwritten, all new requests will be handled using this cookie.
		    	Cookie c = new Cookie(COOKIE_NAME, sessionID);
		    	//This new cookie will have default expiration timeout
		    	c.setMaxAge(cookieAge/1000);
		    	response.addCookie(c);
		    	responseWriter.write(sessionObj.toString());
		    }
	    
	    }else if(requestType.equalsIgnoreCase("refresh")){

	    	//Handle refresh button
	    	SessionObject sessionObj = sessionTable.get(sessionIdFromCookie);
	    	sessionObj.incrementVersionNo();
	    	sessionObj.setExpirationTs(Utils.getCurrentTimeInMillis()+cookieAge);
	    	String sessionID = sessionObj.getSessionId();
	    	//Create a new session object with incremented version
	    	//SessionObject newSessionObj = generateNewSessionObjectWithIncrementedVersion(sessionObj);
	    	//For new object
	    	//String sessionIDWithVersion = newSessionObj.getSessionIdWithVersion();
	    	//Put the new session object with incremented session version in the session table
	    	//sessionTable.put(sessionIDWithVersion, sessionObj);
	    	Cookie c = new Cookie(COOKIE_NAME, sessionID);
	    	c.setMaxAge(cookieAge/1000);
	    	response.addCookie(c);
	    	responseWriter.write(sessionObj.toString());
	    }
	    
	    else if( requestType.equalsIgnoreCase("replace")){
	    	
	    	String newMessage = request.getParameter("message");
	    	//Handle replace button
	    	SessionObject sessionObj = sessionTable.get(sessionIdFromCookie);
	    	sessionObj.setMessage(newMessage);
	    	sessionObj.incrementVersionNo();
	    	sessionObj.setExpirationTs(Utils.getCurrentTimeInMillis()+cookieAge);
	    	String sessionID = sessionObj.getSessionId();
	    	//Create a new session object with incremented version
	    	//SessionObject newSessionObj = generateNewSessionObjectWithIncrementedVersion(sessionObj,newMessage);
	    	//For new object
	    	//String sessionIDWithVersion = newSessionObj.getSessionIdWithVersion();
	    	//Put the new session object with incremented session version in the session table
	    	//sessionTable.put(sessionIDWithVersion, sessionObj);
	    	Cookie c = new Cookie(COOKIE_NAME, sessionID);
	    	c.setMaxAge(cookieAge/1000);
	    	response.addCookie(c);
	    	responseWriter.write(sessionObj.toString());
	    }else{
	    	
	    	//Handle logout button
	    	
	    	Cookie c = new Cookie(COOKIE_NAME, sessionIdFromCookie);
	    	//Causes cookie to be removed from the browser
	    	c.setMaxAge(0);
	    	//Delete the session object from the table
	    	sessionTable.remove(sessionIdFromCookie);
	    	System.out.println("Logging out of session:"+sessionIdFromCookie);
	       	response.addCookie(c);
	    	
	    }
	    }catch(Exception e){
	    	responseWriter.write("You are using an invalid cookie.");
	    	LOGGER.info(Utils.getStackTrace(e));
	    }
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
	
	
	
	

}
