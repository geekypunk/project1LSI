package com.cs5300.proj1a.daemons;

import java.util.HashSet;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import com.cs5300.proj1a.utils.Utils;
import com.cs5300.proj1b.rpc.RPCClient;
import com.cs5300.proj1b.views.View;

/**
 * Read and update BootStrapView
 * 
 * @author dr472
 * 
 */
public class ViewUpdate extends TimerTask {

	private View serverView;
	private RPCClient client;

	public ViewUpdate(ServletContext ctx, RPCClient client) {
		this.serverView = (View) ctx.getAttribute("serverView");
		this.client = client;
	}

	@Override
	public void run() {
		HashSet<String> view = client.getView();
		if (!view.isEmpty()) {
			View temp = new View(view);
			temp.union(this.serverView.getView());
			temp.remove(Utils.SERVER_IP);
			temp.shrink(View.viewSize);
			this.serverView.replaceWithView(temp);
		}
	}

}