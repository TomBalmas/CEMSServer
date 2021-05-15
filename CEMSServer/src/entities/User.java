package entities;

public abstract class User {
	private long SSN;
	private String name;
	private String surName;
	private String userName;
	private String email;
	private String password;
	
	public User(long sSN, String name, String surName, String userName, String email, String password) {
		SSN = sSN;
		this.name = name;
		this.surName = surName;
		this.userName = userName;
		this.email = email;
		this.password = password;
	}
	 
	
}
