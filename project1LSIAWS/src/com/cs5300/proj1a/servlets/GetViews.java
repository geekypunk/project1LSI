package com.cs5300.proj1a.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
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

import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.ServerView;

/**
 * @author kt466
 * <p><b>Servlet reponsible for fetching the server and Bootstrap view</b></p>
 * 
 */
@WebServlet("/GetViews")
public class GetViews extends HttpServlet {
	private static final long serialVersionUID = 2L;
	private static BootStrapView bootStrapView;
    private static ServerView serverView;
    
    //Sets the expiration age for a cookie in milliseconds
    public static int cookieAge = 1000*60*5; 
    //Cookie name
    public static String COOKIE_NAME="CS5300PROJ1SESSION";
    private final static Logger LOGGER = Logger.getLogger(GetViews.class.getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetViews() {
        super();
        // TODO Auto-generated constructor stub
    }


    /**
	 * @see Servlet#init(ServletConfig)
	 */
    public void init(ServletConfig config)
            throws ServletException{
    	
    	ServletContext ctx =config.getServletContext();
    	bootStrapView = (BootStrapView)ctx.getAttribute("bootStrapView"); 
    	serverView = (ServerView)ctx.getAttribute("serverView"); 
    	LOGGER.info("bootStrapView:"+bootStrapView.getAsServerView());
    	LOGGER.info("serverView:"+serverView.getView());
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String responseHTML="";
		Cookie responseCookie = null;
		Cookie[] cookies = request.getCookies();
	    if (cookies != null) {
	      for (int i = 0; i < cookies.length; i++) {
	        if (cookies[i].getName().equals(COOKIE_NAME)) {
	        	responseCookie = cookies[i];
	        	break;
	        }
	      }
	    }
	    if(responseCookie!=null){
			responseHTML+=bootStrapView.getAsServerView().toString();
			Set<String> view = serverView.getView();
			responseHTML+="|"+view.toString();
			responseCookie.setMaxAge(cookieAge/1000);
			response.addCookie(responseCookie);
	    }else{
	    	responseHTML = "NoCookie";
	    }
		response.setContentType("text/html");
		PrintWriter responseWriter = response.getWriter();
		responseWriter.write(responseHTML);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
