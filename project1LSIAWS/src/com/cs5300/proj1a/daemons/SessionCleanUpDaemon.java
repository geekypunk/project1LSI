package com.cs5300.proj1a.daemons;

import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.cs5300.proj1a.dao.SessionObject;
import com.cs5300.proj1a.servlets.SessionManager;
import com.cs5300.proj1a.utils.Utils;

/**
 * This class implements the daemon for cleaning up expired sessions
 * @author kt466
 *
 */
public class SessionCleanUpDaemon extends TimerTask{
	
	
	@Override
	public void run() {
		System.out.println("Invoking Session Cleanup");
		
		 ConcurrentHashMap<String, SessionObject> sessionTable = SessionManager.sessionTable;
		 Iterator<Map.Entry<String,SessionObject>> it = sessionTable.entrySet().iterator();
		 long currentTimeInMillis = Utils.getCurrentTimeInMillis();
		 SessionObject sObj;
		 while (it.hasNext()) {
		   Map.Entry<String,SessionObject> entry = it.next();
		
		   // Remove entry if session has expired
		   sObj = entry.getValue();
		   if(currentTimeInMillis>=sObj.getExpirationTs()){
			   System.out.println("Cleaning up:"+sObj.getSessionId());
			   it.remove();
		   }
		 }
		
	}
}
