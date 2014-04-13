package com.cs5300.proj1a.daemons;

import java.util.HashSet;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.RPCClient;
import com.cs5300.proj1b.views.ServerView;

/**
 * Read and update ServerView 
 * 
 * @author dr472
 * 
 */
public class ViewUpdate extends TimerTask {

	private ServerView serverView;
	private RPCClient client;
	private final static Logger LOGGER = Logger.getLogger(ViewUpdate.class.getName());
	public ViewUpdate(ServletContext ctx, RPCClient client) {
		this.serverView = (ServerView) ctx.getAttribute("serverView");
		this.client = client;
	}

	@Override
	public void run() {
		LOGGER.info("Running ViewUpdate");
		try{
			HashSet<String> view = client.getView();
			LOGGER.info("Old Server View-->"+view);
			if (!view.isEmpty()) {
				ServerView temp = new ServerView(view);
				temp.union(this.serverView.getView());
				temp.remove(Utils.SERVER_IP);
				temp.shrink(ServerView.viewSize);
				this.serverView.replaceWithView(temp);
				LOGGER.info("New Server View-->"+this.serverView);
			}
		}catch(Exception e){
			LOGGER.info("Server ViewUpdate failed");
			LOGGER.log(Level.WARNING, Utils.getStackTrace(e));
		}
	}

}