package com.embedsky.httpUtils;

import java.util.HashMap;

public class picUpload{

	private String file;
	private String filetype;


	public picUpload(){
		super();
	}

	public picUpload(String file, String filetype){
		super();
		this.file = file;
		this.filetype = filetype;
	}

	public HashMap<String, String> picUploadGet(){
		HashMap <String, String> picuploadval = new HashMap <String, String> ();
		picuploadval.put("file", file);
		picuploadval.put("filetype", filetype);

		return picuploadval;
	}
}