package com.cs5300.proj1a.daemons;

import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cs5300.proj1a.dao.SessionObject;
import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1a.utils.Utils;

/**
 * <p>This class implements the daemon for cleaning up expired sessions.</p>
 * <p>Sessions are cleaned up by looking at their discard timstamp and comparing them with the local server time</p>
 * @author kt466
 *
 */
public class SessionCleanUpDaemon extends TimerTask{
	
	private final static Logger LOGGER = Logger.getLogger(SessionCleanUpDaemon.class.getName());
	@Override
	public void run() {
		
		LOGGER.info("Invoking Session Cleanup");
		try{
		ConcurrentHashMap<String, SessionObject> sessionTable = SessionManager.sessionTable;
		Iterator<Map.Entry<String,SessionObject>> it = sessionTable.entrySet().iterator();
		long currentTimeInMillis = Utils.getCurrentTimeInMillis();
		SessionObject sObj;
		while (it.hasNext()) {
		  Map.Entry<String,SessionObject> entry = it.next();
		  sObj = entry.getValue();
		   if(currentTimeInMillis>=sObj.discardTs()){
			   LOGGER.info("Cleaning up:"+sObj.getSessionId());
		   it.remove();
		  }
		}
		}catch(Exception e){
			LOGGER.info("Garbage collection failed");
			LOGGER.log(Level.WARNING, Utils.getStackTrace(e));
		}
		
	}
}
