package com.cs5300.proj1b.views;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class View {

	private Set<String> svrIDList;
	private static final int viewSize = 5;
	private Random randomGenerator;
	public View(){
		this.randomGenerator = new Random();
		this.svrIDList = new HashSet<String>();
	}
	public View(Set<String> svrIds){

		this.svrIDList = svrIds;
		
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
		int index = randomGenerator.nextInt(this.svrIDList.size());
		int i=0;
		for(String s : this.svrIDList){
			if (i == index)
		        return s;
		    i = i + 1;
		}
		return null;
	}
	
	/**
	 * Set the View of this server to the union of its view and _view
	 * @param _view
	 */
	public void union(Set<String> _view){
		this.svrIDList.addAll(_view);
	}
}
