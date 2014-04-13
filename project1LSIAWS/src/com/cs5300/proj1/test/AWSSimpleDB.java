package com.cs5300.proj1.test;

import java.util.ArrayList;

import com.cs5300.proj1b.views.BootStrapView;


/**
 * @author kt466
 *
 * <p>Test class for managing SimpleDB</p>
 */
public class AWSSimpleDB {
	
	public static void main(String args[]){
		
		/*String backupServer = "";
		ArrayList<String> new_backup = new ArrayList<String>();
		new_backup.add("1");new_backup.add("1");new_backup.add("1");new_backup.add("1");
		for(String t : new_backup){
			backupServer += t + "_";
		}

		System.out.println(backupServer);
		if(backupServer.endsWith("_")){
			backupServer = backupServer.substring(0, backupServer.length() - 1);
		}
		System.out.println(backupServer);*/
		clearBootStrap();
	}
	private static void clearBootStrap(){
		BootStrapView view  = new BootStrapView();
		System.out.println(view.getAsServerView());
		view.clearView();
		System.out.println(view.getAsServerView());
		System.out.println("...........");
		
	}
	/*
	private boolean addItem(String domain, String itemName) {
        try {
            PutAttributesRequest putAttributesRequest = new PutAttributesRequest();
            putAttributesRequest.setDomainName(domain);
            putAttributesRequest.setItemName(itemName);
            List<ReplaceableAttribute> list = new ArrayList<ReplaceableAttribute>();
            list.add(new ReplaceableAttribute("A", "a", true));
            list.add(new ReplaceableAttribute("B", "c", true));
            list.add(new ReplaceableAttribute("C", "b", true));
            putAttributesRequest.setAttributes(list);
            amazonSimpleDBClient.putAttributes(putAttributesRequest);
        } catch (Throwable ex) {
            System.out.println(ex.toString());
            return false;
        }
        return true;
    }

    private boolean deleteItem(String domain, String itemName) {
        try {
            DeleteAttributesRequest deleteAttributesRequest = new DeleteAttributesRequest();
            deleteAttributesRequest.setDomainName(domain);
            deleteAttributesRequest.setItemName(itemName);
            amazonSimpleDBClient.deleteAttributes(deleteAttributesRequest);
        } catch (Throwable ex) {
            System.out.println(ex.toString());
            return false;
        }
        return true;
    }*/


}
