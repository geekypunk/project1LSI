package com.cs5300.proj1a.dao;
import java.util.Date;

import com.cs5300.proj1a.utils.Utils;

/**
 * This class describes the structure of the session object
 * @author kt466
 *
 */
public class SessionObject {
	
	private String sessionId;
	private int version;
	private long expirationTs;
	private String message;
	public static final long DELTA= 5 * 1000;
	
	public SessionObject(String message,long expTs) throws Exception{
		
		this.sessionId = Utils.sessionNumber+"_"+Utils.SERVER_IP;
		this.version = 0;
		this.message = message;
		this.expirationTs = expTs;
		Utils.sessionNumber++;
	}
	/**
	 * @return the sessionId
	 */
	public String getSessionId() {
		return sessionId;
	}
	/**
	 * @param sessionId the sessionId to set
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	/**
	 * @return the timeStamp
	 */
	public long getExpirationTs() {
		return expirationTs;
	}
	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setExpirationTs(long timeStamp) {
		this.expirationTs = timeStamp;
	}

	/**
	 * Returns the expiration times for this session object
	 * @return Date expirationTs
	 */
	public Date getExpirationDate(){
		Date date=new Date(this.expirationTs);
		return date;
	}
	/**
	 * This method returns the sessionID along with the version number appended to it. This value is primarly used as a key 
	 * in the sessionTable
	 * @return String sessionIDWithVersion
	 */
	public String getSessionIdWithVersion(){
		
		String id = this.sessionId+this.version;
		return id;
	}
	/**
	 * Increments the version number associated with this session object
	 */
	public void incrementVersionNo(){
		this.version = this.version + 1;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString(){
		return this.getMessage()+"|"+this.getExpirationDate()+"|"+this.getSessionId();
	}
}
