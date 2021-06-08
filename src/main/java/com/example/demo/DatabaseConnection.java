package com.example.demo;
import java.sql.*;


public class DatabaseConnection {
	Connection conn;
	DatabaseConnection(){
		try
		{  
			Class.forName("com.mysql.jdbc.Driver");  
			this.conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/sharedfolder","root","Shyam123@@"); 
		}
		catch(Exception e)
		{ 
			System.out.println(e);
		}  
	}
	
	public Connection getConnection(){
		return this.conn;
	}
	
	public void closeConnection() throws SQLException {
		this.conn.close();
	}
	
}
