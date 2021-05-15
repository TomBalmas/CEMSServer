package entities;

import java.util.ArrayList;
import java.util.Hashtable;

public class Student extends User{
	private  Hashtable<?, ?> grades = new Hashtable(); 
	private ArrayList <Test> tests = new ArrayList<>();
	public Student(Hashtable<?, ?> grades, ArrayList<Test> tests,long SSN,String name,String surName ,String userName ,String email, String password) {
		super(SSN,name,surName,userName,email, password);
		this.grades = grades;
		this.tests = tests;
	}
	
	 
}
