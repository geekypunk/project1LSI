package com.cs5300.proj1a.daemons;

import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.ServerView;

/**
 * Runs a daemon which periodically updates bootstrap view
 * @author kt466
 *
 */
public class BootStrapViewUpdate  extends TimerTask{

	private BootStrapView bootStrapView;
	private ServerView serverView;
	private final static Logger LOGGER = Logger.getLogger(BootStrapViewUpdate.class.getName());
	public BootStrapViewUpdate(ServletContext ctx){
		this.bootStrapView = (BootStrapView)ctx.getAttribute("bootStrapView"); ;
		this.serverView = (ServerView)ctx.getAttribute("serverView");;
	}
	@Override
	public void run() {
		LOGGER.info("Running BootStrapViewUpdate");
		try{
			ServerView _bootStrapView = this.bootStrapView.getAsServerView();
			LOGGER.info("Old BootStrapView-->"+_bootStrapView);
			System.out.println(_bootStrapView);
			_bootStrapView.remove(Utils.SERVER_IP);
			_bootStrapView.union(this.serverView.getView());		
			_bootStrapView.shrink(5);	
			this.serverView.replaceWithView(_bootStrapView);
			_bootStrapView.insert(Utils.SERVER_IP);		
			_bootStrapView.shrink(5);			
			this.bootStrapView.replaceView(_bootStrapView);
			LOGGER.info("BootStrapViewUpdate -->"+this.bootStrapView.getAsServerView());	
		}catch(Exception e){
			LOGGER.info("BootStrapViewUpdate failed");
			LOGGER.log(Level.WARNING, Utils.getStackTrace(e));
		}
	}

}
