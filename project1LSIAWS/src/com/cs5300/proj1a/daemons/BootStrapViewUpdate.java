package com.cs5300.proj1a.daemons;

import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.views.BootStrapView;
import com.cs5300.proj1b.views.View;

/**
 * Read and update BootStrapView
 * @author kt466
 *
 */
public class BootStrapViewUpdate  extends TimerTask{

	private BootStrapView bootStrapView;
	private View serverView;
	private ServletContext ctx;
	private final static Logger LOGGER = Logger.getLogger(BootStrapViewUpdate.class.getName());
	public BootStrapViewUpdate(ServletContext ctx){
		this.ctx = ctx;
		this.bootStrapView = (BootStrapView)ctx.getAttribute("bootStrapView"); ;
		this.serverView = (View)ctx.getAttribute("serverView");;
	}
	@Override
	public void run() {
		
		Set<String> bootStrapViewIDs = new HashSet<String>();
		bootStrapViewIDs.addAll(this.bootStrapView.getView());
		View _bootStrapView = new View(bootStrapViewIDs);
		System.out.println(_bootStrapView);
		_bootStrapView.remove(Utils.SERVER_IP);
		_bootStrapView.union(this.serverView.getView());		
		_bootStrapView.shrink(5);	
		this.serverView.replaceWithView(_bootStrapView);
		_bootStrapView.insert(Utils.SERVER_IP);		
		_bootStrapView.shrink(5);			
		this.bootStrapView.replaceView(_bootStrapView);
			
	}

}
