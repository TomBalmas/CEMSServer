package entities;

public class Teacher extends User {

	public Teacher(int sSN, String name, String surName, String email, String userName, String password) {
		super(sSN, name, surName, userName, email, password);
	}
	
	public String toString() {
		return String.format("Teacher:%d,%s,%s,%s,%s,%s", getSSN(), getName(), getSurName(), getEmail(),
				getUserName(), getPassword());
	}

}
 