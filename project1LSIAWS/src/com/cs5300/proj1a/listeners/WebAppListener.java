package com.cs5300.proj1a.listeners;

import java.util.Timer;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.cs5300.proj1a.daemons.SessionCleanUpDaemon;
import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.RPCServer;

/**
 * This class is used for setting up the session clean up daemon task.
 * The class which implements the daemon is SessionCleanUpDaemon
 *
 */
@WebListener
public class WebAppListener implements ServletContextListener {

    /**
     * Default constructor. 
     * 
     */
	private final static Logger LOGGER = Logger.getLogger(WebAppListener.class.getName());
	SessionCleanUpDaemon clsTask;
	private static int deamonStartPeriod = 100*1000; // Time intervals in which session cleanup daemon is invoked 
    public WebAppListener() {
        // TODO Auto-generated constructor stub
    	
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
    	
    	try{
	    	
    		//Change for AWS
	    	Utils.SERVER_IP = Utils.getIP();
	    	//Bootstrap mechanism
	    	
	    	
	    	//Garbage collection
	    	Timer time = new Timer(); // Instantiate Timer Object
	    	SessionCleanUpDaemon st = new SessionCleanUpDaemon(); // Instantiate SheduledTask class
			time.schedule(st, 0, deamonStartPeriod); // Create Repetitively task for every 1 secs
			
			//The RPC server thread
			new Thread(new RPCServer()).start();
	    	
    	}catch(Exception e){
    		
    		LOGGER.info("Error in Initiliazation");
    		LOGGER.info(Utils.getStackTrace(e));
    		
    	}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    	clsTask = null;
    }
	
}
