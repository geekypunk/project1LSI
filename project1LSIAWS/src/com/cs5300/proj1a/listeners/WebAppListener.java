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
import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.RPCClient;
import com.cs5300.proj1b.rpc.RPCServer;
import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.ServerView;

/**
 * This class is used for setting up the daemon tasks, like view updates,garbage collection etc.
 *
 */
@WebListener
public class WebAppListener implements ServletContextListener {

   
	private final static Logger LOGGER = Logger.getLogger(WebAppListener.class.getName());
	private static int CLEANUP_INTERVAL = 100*1000; // Time intervals in which session cleanup daemon is invoked 
	private static final int BOOT_SERVER_UPDATE_SECS = 5*1000;
	private static final int GOSSIP_SECS = 5*1000;
	 /**
     * Default constructor. 
     * 
     */
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
	    	
	    	//Put Bootstrap view in context
	    	BootStrapView bootStrapView = new BootStrapView();
	    	//bootStrapView.insert(Utils.SERVER_IP);
	    	ctx.setAttribute("bootStrapView", bootStrapView);
	    	
	    	//Put the server view in context
	    	//ServerView serverView = bootStrapView.getAsServerView();	
	    	ServerView serverView = new ServerView();
	    	ctx.setAttribute("serverView", serverView);
	    	
	    	//Deepthi, why are you doing this? As I have already put the object in the servlet context
	    	//SessionManager.serverView = serverView;
	    	
	    	//Garbage collection
	    	Timer time = new Timer(); // Instantiate Timer Object
	    	SessionCleanUpDaemon st = new SessionCleanUpDaemon(); // Instantiate SheduledTask class
			time.schedule(st, 0, (CLEANUP_INTERVAL/2) + generator.nextInt( CLEANUP_INTERVAL )); // Create Repetitively task for every 1 secs
			
			//Start BootStrap view update daemon
			Timer timer2 = new Timer();
			BootStrapViewUpdate bViewUp = new BootStrapViewUpdate(ctx);
			timer2.schedule(bViewUp, 0, (BOOT_SERVER_UPDATE_SECS/2) + generator.nextInt( BOOT_SERVER_UPDATE_SECS ));
			
			//TODO: Uncomment code below, once servlet code is ready
			//Start Gossip among servers
			Timer timer3 = new Timer();
			ViewUpdate viewUpdate = new ViewUpdate(ctx, new RPCClient(ctx));
			timer3.schedule(viewUpdate, 0, (GOSSIP_SECS/2) + generator.nextInt( GOSSIP_SECS ));
			
			
			//The RPC server thread
			new Thread(new RPCServer(ctx)).start();
	    	
    	}catch(Exception e){
    		
    		LOGGER.info("Error in Initiliazation");
    		LOGGER.info(Utils.getStackTrace(e));
    		
    	}
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    	
    }
	
}
