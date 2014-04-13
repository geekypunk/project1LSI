package com.cs5300.proj1a.listeners;

import java.util.Properties;
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
	
	// Time intervals in which session cleanup daemon(Garbage collection) is invoked 
	private static int CLEANUP_INTERVAL = 300*1000; 
	
	// Time intervals in which BootStrapViewUpdate daemon is invoked 
	private static final int BOOT_SERVER_UPDATE_SECS = 15*1000; 
	
	// Time intervals in which ViewUpdate daemon is invoked 
	private static final int GOSSIP_SECS = 10*1000;
	 
	/**
     * Default constructor. 
     * 
     */
	public WebAppListener() {
    	
    }

	/**
	 * <p><b>Actions being done:</b></p>
	 * <p>-->Get and cache the EC2 instance public IP</p>
	 * <p>-->Initilize the server and bootstrap views and put them in ServletContext</p>
	 * <p>-->Initialization of all daemons</p>
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
    	
    	try{
    		Random generator = new Random();
    		ServletContext ctx = sce.getServletContext();
    		
    		//Load properties file. For now it has 1 parameter indicating k-resilience
    		Properties config = new Properties();
    		config.load(ctx.getResourceAsStream("/WEB-INF/config.properties"));
    		ctx.setAttribute("resilienceFactor", Integer.parseInt(config.getProperty("K_RESILIENCE_PARAM")));
			//Change for AWS/Local machine
	    	Utils.SERVER_IP = Utils.getPublicIP();
	    	
	    	//Put Bootstrap view in context
	    	BootStrapView bootStrapView = new BootStrapView();
	    	//bootStrapView.insert(Utils.SERVER_IP);
	    	ctx.setAttribute("bootStrapView", bootStrapView);
	    	
	    	//Put the server view in context
	    	//ServerView serverView = bootStrapView.getAsServerView();	
	    	ServerView serverView = new ServerView();
	    	ctx.setAttribute("serverView", serverView);
	    	
	   	    	
	    	//Garbage collection
	    	Timer time = new Timer(); // Instantiate Timer Object
	    	SessionCleanUpDaemon st = new SessionCleanUpDaemon(); // Instantiate SheduledTask class
			time.schedule(st, 0, (CLEANUP_INTERVAL/2) + generator.nextInt( CLEANUP_INTERVAL )); // Create Repetitively task for every 1 secs
			
			//Start BootStrap view update daemon
			Timer timer2 = new Timer();
			BootStrapViewUpdate bViewUp = new BootStrapViewUpdate(ctx);
			timer2.schedule(bViewUp, 0, (BOOT_SERVER_UPDATE_SECS/2) + generator.nextInt( BOOT_SERVER_UPDATE_SECS ));
			
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
