package com.example.phoneit;

import android.graphics.Bitmap;

public class TransferItem {
	private String device_name, title, note, type, direction, filename,
			device_user, date;
	private int transfer_id;
	private Bitmap map_picture=null, file_picture=null;

	public TransferItem(int id, String type, String title, String note,
			String device_name, String device_user, String direction, String date,
			String filename) {
		this.transfer_id = id;
		this.date = date;
		this.type = type;
		this.title = title;
		this.note = note;
		this.device_name = device_name;
		this.device_user = device_user;
		this.direction = direction;
		this.filename = filename;
	}
	
	public void addMapBitmap(Bitmap picture) {
		this.map_picture = picture;
	}
	public Bitmap getMapBitmap() {
		return map_picture;
	}
	
	public void addFileBitmap(Bitmap picture) {
		this.file_picture = picture;
	}
	public Bitmap getFileBitmap() {
		return file_picture;
	}
	
	public String getDeviceName() {
		return device_name;
	}
	
	public String getUser() {
		return device_user;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getNote() {
		return note;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDirection(){
		return direction;		
	}
	
	public String getFilename() {
		return filename;
	}
	
	public int getTransferId() {
		return transfer_id;
	}
	
	public String getDate() {
		return date;
	}

}