package com.example.demo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ServerController {
	DatabaseOperation dbo;
	ServerController (){
		this.dbo = new DatabaseOperation();
	}
	
	@GetMapping("/getusers")
	public GetUserResponse getUsers(){
		GetUserResponse gur = new GetUserResponse();
		gur.setData(new DatabaseOperation().getUserInfo());
		gur.setStatus(200);
		return gur;
	}
	
	
	@GetMapping("/")
	public GetEndpointData getLogin(){
		return new GetEndpointData();
	}
	
	@PostMapping("/login")
	public UserLogin getLogin(@RequestBody UserLogin user) throws SQLException {
		return dbo.getLogin(user);
	}
	
	@PostMapping("/upload")
	public FolderResponse upload(@RequestParam("file") MultipartFile file,@RequestParam("filedata") String fileData) {
		try {
			
			byte[] data = file.getBytes();
			JSONObject json = new JSONObject();
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(fileData);
				
			if(uploadFileHelper( data,json.get("path").toString(),json.get("filename").toString() )) {
				return getFolders(new File("D://shared/"));
			}else {
				
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@PostMapping("/addfolder")
	public FolderResponse addFolder(@RequestBody AddFolderRequest afr) {
		String name = afr.getNameOfFolder();
		String[] path = afr.getPath();
		addFolderHelper(name,path);
		File file = new File("D://shared/");
		return getFolders(file);
	}
	
	
	@PostMapping("/deletefile")
	public FolderResponse deletefile(@RequestBody AddFolderRequest afr) {
		String name = afr.getNameOfFolder();
		String[] path = afr.getPath();
		deleteFileHelper(name,path);
		File file = new File("D://shared/");
		return getFolders(file);
	}
	
	
	@PostMapping("/addfile")
	public FolderResponse addFile(@RequestBody AddFolderRequest afr) {
		String name = afr.getNameOfFolder();
		String[] path = afr.getPath();
		addFileHelper(name,path);
		File file = new File("D://shared/");
		return getFolders(file);
	}
	
	
	@GetMapping("/getfolders")
	public FolderResponse getFolders() {
		File file = new File("D://shared/");
		return getFolders(file);
	}
	
	@PostMapping("/download")
	public @ResponseBody byte[] getFile(@RequestBody String path) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject js = (JSONObject) parser.parse(path);
			File f = new File("D://"+js.get("path"));
			InputStream in = new FileInputStream(f);
			return org.apache.commons.io.IOUtils.toByteArray(in);
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException npe) {
			npe.printStackTrace();
		}
		return null;
	}
	
	@PostMapping("/updateuser")
	public UserTableData updateUser(@RequestBody UserTableData userdata) {
		if(new DatabaseOperation().updateUser(userdata)) {
			return userdata;
		}
		return new UserTableData();
	}
	
	@PostMapping("/adduser")
	public AddUserRequest addUser(@RequestBody AddUserRequest userdata) {
		if(new DatabaseOperation().addUser(userdata)) {
			return userdata;
		}
		return new AddUserRequest();
	}
	
	@GetMapping("/deleteuser")
	public String deleteUser(@RequestParam("username") String uname) {
		new DatabaseOperation().deleteUser(uname); 
		return uname;
	}
	
	private boolean deleteFileHelper(String name,String path[]) {
		String absolutePath ="D://";
		for(String s: path) {absolutePath += s +"//"; }
		File f = new File(absolutePath);
		if(f.delete()) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean addFolderHelper(String name,String path[]) {
		String absolutePath ="D://";
		for(String s: path) {absolutePath += s +"//"; }
		File f = new File(absolutePath+"//"+name);
		if(f.mkdir()) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean addFileHelper(String name , String path[])  {
		String absolutePath ="D://";
		for(String s: path) {absolutePath += s +"//"; }
		File f = new File(absolutePath+"//"+name);
		
		try {
			if(f.createNewFile()) {
				return true;
			}else {
				return false;
			}
		}catch(IOException e) {
			System.out.println(e);
		}
		return false;
	}
	
	private FolderResponse getFolders(File f){
		FolderResponse fr = new FolderResponse();
		FolderResponse files[] = new FolderResponse[f.list().length];
		int index = 0;
		
		for(String s : f.list()) {
			File file = new File(f.getAbsolutePath()+"//"+s); 
			
			if(file.isDirectory()) {
				FolderResponse temp = new FolderResponse();
				temp =  getFolders(file);
				temp.setFolder(true);
				files[index++] = temp;
			
			}else {
				FolderResponse temp = new FolderResponse();
				temp.setFolder(false);
				temp.setNameOfFolder(s);
				files[index++] = temp;				
			}
		}
		
		fr.setNameOfFolder(f.getName());
		fr.setFiles(files);
		fr.setFolder(true);
		return fr;
	}

	private boolean uploadFileHelper(byte[] data,String path,String fileName) {
		String absolutePath = "D://"+path+"/"+fileName;
		File file =new File(absolutePath);
		try {
			if(file.createNewFile()) {
				try {
					OutputStream os = new FileOutputStream(file);
					os.write(data);
					os.close();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
