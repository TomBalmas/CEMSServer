package entities;

public class Teacher extends User {

	public Teacher(long sSN, String name, String surName, String email, String userName, String password) {
		super(sSN, name, surName, userName, email, password);
	}
	
	public String toString() {
		return String.format("Teacher: %ld, %s, %s, %s, %s, %s", getSSN(), getName(), getSurName(), getEmail(),
				getUserName(), getPassword());
	}

}
 