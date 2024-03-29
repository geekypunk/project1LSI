package com.cs5300.proj1a.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * @author kt466
 * This class contains the utility methods
 *
 */
public class Utils {
	
	
	public static String SERVER_IP="";
	public static int sessionNumber = 0;
	private final static Logger LOGGER = Logger.getLogger(Utils.class.getName());
	
	/**
	 * Returns the current server time
	 * @return Date
	 */
	public static long getCurrentTimeInMillis(){
		Date date= new Date();
        Timestamp currentTimestamp= new Timestamp(date.getTime());
        return currentTimestamp.getTime();
	}
	
	/** 
	 * Get the local server IP. For debugging purposes
	 * @return String
	 * @throws Exception
	 */
	public static String getIP() throws Exception{
		
		String ip = null;
		try{
		
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()){
			    NetworkInterface current = interfaces.nextElement();
			    if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
			    Enumeration<InetAddress> addresses = current.getInetAddresses();
			    while (addresses.hasMoreElements()){
			        InetAddress current_addr = addresses.nextElement();
			        if (current_addr.isLoopbackAddress()) continue;
			        if (current_addr instanceof Inet4Address){
			        	  ip =  (current_addr.getHostAddress());
			        	  return ip;
			        }
			        
			    }
			}
			
		}
		catch (Exception e) {
			LOGGER.info(e.getLocalizedMessage());
			throw e;
		}
		return ip;
	}
	
	public static String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}
	
	/**
	 * Returns the EC2 public IP
	 * @return String
	 */
	public static String getPublicIP(){
		String output = null;
	    try {  
            Process p = Runtime.getRuntime().exec("/opt/aws/bin/ec2-metadata --public-ipv4");  
            p.waitFor();
            BufferedReader in = new BufferedReader(  
                                new InputStreamReader(p.getInputStream()));  
            output = in.readLine(); 
            output = output.split(" ")[1];
        } catch (IOException e) {  
            LOGGER.info(Utils.getStackTrace(e)); 
        } catch (InterruptedException e) {
        	LOGGER.info(Utils.getStackTrace(e)); 
		}
	    return output;
    } 
	
	
}
