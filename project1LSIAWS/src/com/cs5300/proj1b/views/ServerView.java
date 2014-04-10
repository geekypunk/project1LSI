package com.cs5300.proj1b.views;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.cs5300.proj1b.rpc.Constants;

/**
 * @author kt466
 * Class for manipulating the local server view
 *
 */
public class ServerView {

	private Set<String> svrIDList;
	public static final int viewSize = 5;
	private Random randomGenerator;
	public ServerView(){
		this.randomGenerator = new Random();
		this.svrIDList = new HashSet<String>();
	}
	public ServerView(Set<String> svrIds){
		this.randomGenerator = new Random();
		this.svrIDList = svrIds;
		
	}
	public ServerView(ServerView _view){
		this.svrIDList = _view.getView();
	}
	
	/**
	 * Returns the View of this server
	 * @return serverView
	 */
	public Set<String> getView(){
		
		return this.svrIDList;
	}
	
	/**
	 * Shrink the View of this server if its size is greater than k
	 * @param k
	 */
	public void shrink(int k){
		
		if(this.svrIDList.size()>k){
			
			this.svrIDList.remove(randomGenerator.nextInt(this.svrIDList.size()));
		}
	}
	
	/**
	 * Insert the svrID into this server's view
	 * @param svrID
	 */
	public void insert(String svrID){
		
		this.svrIDList.add(svrID);
	}
	
	
	
	/**
	 * Remove svrID from View if present
	 * @param svrID
	 */
	public void remove(String svrID){
		
		this.svrIDList.remove(svrID);
	}
	
	/**
	 * Return serverID chosen uniformly at random from View
	 * @return serverID
	 */
	public String choose(){
		if(this.svrIDList.isEmpty())
			return Constants.NULL_ADDRESS;
		int index = randomGenerator.nextInt(this.svrIDList.size());
		int i=0;
		for(String s : this.svrIDList){
			if (i == index)
		        return s;
		    i = i + 1;
		}
		return Constants.NULL_ADDRESS;
	}
	
	/**
	 * Set the View of this server to the union of its view and _view
	 * @param _view
	 */
	public void union(Set<String> _view){
		this.svrIDList.addAll(_view);
	}
	
	/**
	 * Replace view with _view 
	 * @param _view
	 */
	public void replaceWithView(ServerView _view){
		this.svrIDList.clear();
		this.svrIDList.addAll(_view.getView());
	}
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = this.svrIDList.iterator();
		String s;
		while(it.hasNext()){
			s = it.next();
			sb.append(s).append("_");
		}
		
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		if(sb.length()>0 && sb.charAt(0)=='_'){
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}
}
