package com.cs5300.proj1a.listeners;

import java.util.Random;
import java.util.Timer;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.cs5300.proj1a.daemons.BootStrapViewUpdate;
import com.cs5300.proj1a.daemons.SessionCleanUpDaemon;
import com.cs5300.proj1a.daemons.ViewUpdate;
import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.RPCClient;
import com.cs5300.proj1b.rpc.RPCServer;
import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.View;

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
	private static int CLEANUP_INTERVAL = 100*1000; // Time intervals in which session cleanup daemon is invoked 
	private static final int BOOT_SERVER_UPDATE_SECS = 5*1000;
	private static final int GOSSIP_SECS = 5*1000;
	public WebAppListener() {
        // TODO Auto-generated constructor stub
    	
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
    	
    	try{
    		Random generator = new Random();
    		ServletContext ctx = sce.getServletContext();
    		
    		//Change for AWS
	    	Utils.SERVER_IP = Utils.getIP();
	    	
	    	//Put the server view in context
	    	View serverView = new View();
	    	ctx.setAttribute("serverView", serverView);
	    	
	    	//Put Bootstrap view in context
	    	BootStrapView bootStrapView = new BootStrapView();
	    	bootStrapView.insert(Utils.SERVER_IP);
	    	ctx.setAttribute("bootStrapView", bootStrapView);
	    	
	    	//Garbage collection
	    	Timer time = new Timer(); // Instantiate Timer Object
	    	SessionCleanUpDaemon st = new SessionCleanUpDaemon(); // Instantiate SheduledTask class
			time.schedule(st, (CLEANUP_INTERVAL/2) + generator.nextInt( CLEANUP_INTERVAL )); // Create Repetitively task for every 1 secs
			
			//Start BootStrap view update daemon
			Timer timer2 = new Timer();
			BootStrapViewUpdate bViewUp = new BootStrapViewUpdate(ctx);
			timer2.schedule(bViewUp, (BOOT_SERVER_UPDATE_SECS/2) + generator.nextInt( BOOT_SERVER_UPDATE_SECS ));
			
			//TODO: Uncomment code below, once servlet code is ready
			//Start Gossip among servers
//			Timer timer3 = new Timer();
//			ViewUpdate viewUpdate = new ViewUpdate(ctx, new RPCClient());
//			timer3.schedule(viewUpdate, (GOSSIP_SECS/2) + generator.nextInt( GOSSIP_SECS ));
			
			
			//The RPC server thread
			//new Thread(new RPCServer()).start();
	    	
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
