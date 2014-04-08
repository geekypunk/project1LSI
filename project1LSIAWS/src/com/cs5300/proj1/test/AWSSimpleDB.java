package com.cs5300.proj1.test;

import com.cs5300.proj1b.views.BootStrapView;


public class AWSSimpleDB {
	
	public static void main(String args[]){
		BootStrapView view  = new BootStrapView();
		view.clearView();
		System.out.println(view.getView());
		//view.insert("");
		
	}/*
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
