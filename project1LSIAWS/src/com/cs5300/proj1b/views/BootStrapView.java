/**
 * 
 */
package com.cs5300.proj1b.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;


/**
 * @author kt466
 * <p>Class for manipulating the BootStrap View stored in Amazon SimpleDB</p>
 *
 */
public class BootStrapView {
	
	private AmazonSimpleDBClient simpleDBClient;
	
	private static final String DOMAIN = "BootStrapView";
	private static final String ITEM_NAME = "svrIDs";
	private static final String ATTR_NAME = "IPs";
	/**
	 * Default constructor. Region is Oregon
	 */
	public BootStrapView(){
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
		simpleDBClient = new AmazonSimpleDBClient(credentialsProvider);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2); //Oregon
		simpleDBClient.setRegion(usWest2);
	}

	public BootStrapView(AWSCredentialsProvider credentialsProvider,Region usWest2){
		simpleDBClient = new AmazonSimpleDBClient(credentialsProvider);
		simpleDBClient.setRegion(usWest2);
	}
	
	/**
	 * Get the BootStrap view in SimpleDB by specifying the region code
	 * @param regionCode
	 */
	public BootStrapView(String regionCode){
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
		simpleDBClient = new AmazonSimpleDBClient(credentialsProvider);
		Regions s = Regions.fromName(regionCode);
		Region region = Region.getRegion(s);
		simpleDBClient.setRegion(region);
	}
	
	/**
	 * <p>Get the BootStrap view from SimpleDB as a {@link com.cs5300.proj1b.views.ServerView ServerView} object</p>
	 * @return ServerView
	 */
	public ServerView getAsServerView(){
		
		GetAttributesRequest getAttributesRequest = new GetAttributesRequest(DOMAIN,ITEM_NAME);
		GetAttributesResult result = simpleDBClient.getAttributes(getAttributesRequest);
		String[]ips = result.getAttributes().get(0).getValue().split("_");
		Set<String> bootStrapViewIDs = new HashSet<String>();
		bootStrapViewIDs.addAll(Arrays.asList(ips));
		
		bootStrapViewIDs.remove("");
		ServerView _bootStrapView = new ServerView(bootStrapViewIDs);
		return _bootStrapView; 
	}
	
	/**
	 * Insert the serverID(svrID) into the BootStrap View 
	 * @param svrID
	 */
	public void insert(String svrID){
		GetAttributesRequest getAttributesRequest = new GetAttributesRequest(DOMAIN,ITEM_NAME);
		GetAttributesResult result = simpleDBClient.getAttributes(getAttributesRequest);
		String currentView = result.getAttributes().get(0).getValue();
		
		if(currentView.trim().length()== 0)
			currentView = svrID;
		else	
			currentView += "_"+svrID;
		
		//Add if not present
		if(currentView.contains("_")){
			List<String> svrIDs = Arrays.asList(currentView.split("_"));
			Set<String> uniqIds = new HashSet<String>();
			uniqIds.addAll(svrIDs);
			currentView = StringUtils.join(uniqIds,"_");
			
		}
		PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
		putAttributesRequest.setDomainName(DOMAIN);
		putAttributesRequest.setItemName(ITEM_NAME);
		
		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
		list.add(new ReplaceableAttribute(ATTR_NAME, currentView, true));
		putAttributesRequest.setAttributes(list);
        simpleDBClient.putAttributes(putAttributesRequest);
	}
	
	/**
	 * Clear the BootStrap view. Set to an empty string 
	 */
	public void clearView(){
		PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
		putAttributesRequest.setDomainName(DOMAIN);
		putAttributesRequest.setItemName(ITEM_NAME);
		
		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
		list.add(new ReplaceableAttribute(ATTR_NAME, "", true));
		putAttributesRequest.setAttributes(list);
        simpleDBClient.putAttributes(putAttributesRequest);
	}
	
	/**
	 * Replace the BootStrap view with the given {@link com.cs5300.proj1b.views.ServerView ServerView} object
	 * @param _view
	 */
	public void replaceView(ServerView _view){
		
		String currentView = _view.toString();
		PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
		putAttributesRequest.setDomainName(DOMAIN);
		putAttributesRequest.setItemName(ITEM_NAME);
		
		List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
		list.add(new ReplaceableAttribute(ATTR_NAME, currentView, true));
		putAttributesRequest.setAttributes(list);
        simpleDBClient.putAttributes(putAttributesRequest);
		
	}
		
}
