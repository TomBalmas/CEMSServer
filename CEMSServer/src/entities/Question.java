package entities;

import java.util.Hashtable;

public class Question {
	private Integer id;
	private String author;
	private String questionText;
	private  Hashtable<Question, Integer> rightAnswers = new Hashtable(); 
	private  Hashtable<String, Question> answers = new Hashtable(); 
	private String field;
	
	public Question(Integer id, String author, String questionText, Hashtable<Question, Integer> rightAnswers,
			Hashtable<String, Question> answers, String field) {
		this.id = id;
		this.author = author;
		this.questionText = questionText;
		this.rightAnswers = rightAnswers;
		this.answers = answers;
		this.field = field;
	}
} 
