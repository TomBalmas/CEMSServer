package entities;

import java.util.ArrayList;

public class Teacher extends User {
	private ArrayList<String> fields = new ArrayList<>();
	public Teacher(int sSN, String name, String surName, String email, String userName, String password,ArrayList<String> fields) {
		super(sSN, name, surName, userName, email, password);
		this.fields=fields;
	}
	
	public String toString() {
		return String.format("Teacher:%d,%s,%s,%s,%s,%s", getSSN(), getName(), getSurName(), getEmail(),
				getUserName(), getPassword());
	}

}
 