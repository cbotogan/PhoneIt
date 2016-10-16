package com.example.phoneit;

public class DrawerItem {
	private String device_name;
	private String device_owner;	
	private int device_id;
	
    public DrawerItem(String name, String owner, int id){
        device_name = name;
        device_owner = owner;  
        device_id = id;
    }
    
    public String getDeviceName(){
        return device_name;
    }
     
    public String getDeviceOwner(){
        return device_owner;
    }    
    public int getDeviceId() {
    	return device_id;
    }
    
}