package util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import common.ActiveTest;
import common.Course;
import common.FinishedTest;
import common.Question;
import common.Report;
import common.ScheduledTest;
import common.Student;
import common.StudentGrade;
import common.Teacher;
import common.Test;
import common.TimeExtensionRequest;
import javafx.util.Pair;

public class GeneralQueryMethods {

	/**
	 * creates a question
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new question
	 * @throws SQLException
	 */
	public static Question createQuestion(ResultSet rs) throws SQLException {
		ArrayList<String> answers = new ArrayList<>();
		answers.add(rs.getString("answer1"));
		answers.add(rs.getString("answer2"));
		answers.add(rs.getString("answer3"));
		answers.add(rs.getString("answer4"));
		return new Question(rs.getString("questionId"), rs.getString("authorId"), rs.getString("questionContent"),
				rs.getInt("correctAnswer"), rs.getString("field"), answers);
	}

	/**
	 * creates a test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return - new test
	 * @throws SQLException
	 */
	public static Test createTest(ResultSet rs) throws SQLException {
		return new Test(rs.getString("testId"), Queries.getNameById(rs.getString("authorId")), rs.getString("title"),
				rs.getString("course"), rs.getInt("duration"), rs.getInt("pointsPerQuestion"),
				rs.getString("studentInstructions"), rs.getString("teacherInstructions"),
				rs.getString("questionsInTest"), rs.getString("field"));
	}

	/**
	 * creates a finished test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new finished test
	 * @throws SQLException
	 */
	public static FinishedTest createFinishedTest(ResultSet rs) throws SQLException {
		return new FinishedTest(rs.getString("testId"), Queries.getNameById(rs.getString("authorId")),
				rs.getString("Title"), rs.getString("Course"), rs.getString("scheduler"), rs.getString("studentSSN"),
				rs.getString("date"), rs.getString("startingTime"), rs.getInt("timeTaken"),
				rs.getString("presentationMethod"), rs.getInt("grade"), rs.getString("status"));
	}

	/**
	 * creates an active test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new active test
	 * @throws SQLException
	 */
	public static ActiveTest createActiveTest(ResultSet rs) throws SQLException {
		return new ActiveTest(rs.getString("testId"), rs.getString("author"), rs.getString("title"),
				rs.getString("course"), rs.getString("field"), rs.getString("startingTime"),
				GeneralQueryMethods.calculateFinishTime(rs.getString("startingTime"), rs.getInt("duration")),
				rs.getString("beginTestCode"));
	}

	/**
	 * creates a scheduled test
	 * 
	 * @param rs - result set that has all the arguments for test constructor
	 * @return new scheduled test
	 * @throws SQLException
	 */
	public static ScheduledTest createScheduledTest(ResultSet rs) throws SQLException {
		return new ScheduledTest(rs.getString("testId"), Queries.getAuthorNameByTestId(rs.getString("testId")),
				rs.getString("title"), rs.getString("course"), rs.getString("date"), rs.getString("startingTime"),
				rs.getInt("duration"), rs.getString("scheduledByTeacher"), rs.getString("beginTestCode"));
	}

	/**
	 * 
	 * creates time extension request
	 * 
	 * @param rs - result set that has all the arguments for time extension request
	 *           constructor
	 * @return new time extension request
	 * @throws SQLException
	 */
	public static TimeExtensionRequest createTimeExtensionRequest(ResultSet rs) throws SQLException {
		return new TimeExtensionRequest(rs.getString("teacherSSN"), rs.getString("content"), rs.getString("testCode"),
				rs.getInt("minutes"));
	}

	/**
	 * calculates starting time + duration
	 * 
	 * @param startingTime - test's starting time
	 * @param duration     - test's duration
	 * @return - finish time as string
	 */
	public static String calculateFinishTime(String startingTime, int duration) {
		String[] time = startingTime.split(":");
		String hours = time[0];
		String minutes = time[1];
		int h, m;
		h = Integer.parseInt(hours);
		m = Integer.parseInt(minutes);
		m += duration;
		while (m >= 60) {
			if (h == 24)
				h = 0;
			h += 1;
			m -= 60;
		}
		hours = Integer.toString(h);
		minutes = Integer.toString(m);
		if (h < 10)
			hours = "0" + hours;
		if (m < 10)
			minutes = "0" + minutes;
		return hours + ":" + minutes;
	}

	/**
	 * creates a report
	 * 
	 * @param rs - result set that has all the arguments for report constructor
	 * @return new report
	 * @throws SQLException
	 */
	public static Report createTestReport(ResultSet rs) throws SQLException {
		return new Report(rs.getString("reportId"), rs.getString("testId"), rs.getInt("numOfStudents"),
				rs.getDouble("average"), rs.getDouble("median"), rs.getInt("0-54.9"), rs.getInt("55-64"),
				rs.getInt("65-69"), rs.getInt("70-74"), rs.getInt("75-79"), rs.getInt("80-84"), rs.getInt("85-89"),
				rs.getInt("90-94"), rs.getInt("95-100"));
	}

	/**
	 * creates a student
	 * 
	 * @param rs - result set that has all the arguments for student constructor
	 * @return new student
	 * @throws SQLException
	 */
	public static Student createStudent(ResultSet rs) throws SQLException {
		return new Student(rs.getString("ssn"), rs.getString("name"), rs.getString("surname"), rs.getString("email"),
				rs.getString("username"), rs.getString("password"));
	}

	/**
	 * creates a course
	 * 
	 * @param rs - result set that has all the arguments for course constructor
	 * @return new course
	 * @throws SQLException
	 */
	public static Course createCourse(ResultSet rs) throws SQLException {
		return new Course(rs.getString("courseId"), rs.getString("courseName"), rs.getString("field"));
	}

	/**
	 * creates a teacher
	 * 
	 * @param rs - result set that has all the arguments for teacher constructor
	 * @return new teacher
	 * @throws SQLException
	 */
	public static Teacher createTeacher(ResultSet rs) throws SQLException {
		return new Teacher(rs.getString("ssn"), rs.getString("name"), rs.getString("surname"), rs.getString("email"),
				rs.getString("username"), rs.getString("password"), rs.getString("fields"));
	}

	/**
	 * creates a student report
	 * 
	 * @param testsAndGrades - array list of pairs of tests and grades
	 * @return report with student constructor
	 */
	public static Report createStudentReport(ArrayList<Pair<String, Integer>> testsAndGrades) {
		ArrayList<Integer> grades = new ArrayList<>();
		Double average;
		Double median;
		for (Pair<String, Integer> testAndGrade : testsAndGrades)
			grades.add(testAndGrade.getValue());
		average = getAverage(grades);
		median = getMedian(grades);
		return new Report(testsAndGrades, average, median);
	}

	/**
	 * gets the median of grades array list
	 * 
	 * @param grades - integer array list
	 * @return median of the grades as double
	 */
	public static double getMedian(ArrayList<Integer> grades) {
		Collections.sort(grades);
		return grades.get(grades.size() / 2);
	}

	/**
	 * gets the average of grades array list
	 * 
	 * @param grades - integer array list
	 * @return average of the grades as double
	 */
	public static Double getAverage(ArrayList<Integer> grades) {
		Double average = 0.0;
		for (Integer grade : grades)
			average += grade;
		return average / grades.size();
	}

	/**
	 * creates a teacher report
	 * 
	 * @param testsAveragesMedians - array list of pairs of tests and pairs of
	 *                             averages and medians
	 * @return report with teacher constructor
	 */
	public static Report createTeacherOrCourseReport(
			ArrayList<Pair<String, Pair<Double, Double>>> testsAveragesMedians) {
		ArrayList<Pair<Double, Double>> averagesMedians = new ArrayList<>();
		ArrayList<Double> averages = new ArrayList<>();
		ArrayList<Double> medians = new ArrayList<>();
		Double average = null;
		Double median = null;
		for (Pair<String, Pair<Double, Double>> testAverageMedian : testsAveragesMedians)
			averagesMedians.add(testAverageMedian.getValue());
		for (Pair<Double, Double> averageMedian : averagesMedians) {
			averages.add(averageMedian.getKey());
			medians.add(averageMedian.getValue());
		}
		average = getAverageOfAverages(averages);
		median = getMedianOfMedians(medians);
		return new Report(average, median, testsAveragesMedians);
	}

	/**
	 * gets the median of medians array list
	 * 
	 * @param medians - double array list
	 * @return median of the medians as double
	 */
	public static double getMedianOfMedians(ArrayList<Double> medians) {
		Collections.sort(medians);
		return medians.get(medians.size() / 2);
	}

	/**
	 * gets the average of averages array list
	 * 
	 * @param averages - double array list
	 * @return average of the averages as double
	 */
	public static Double getAverageOfAverages(ArrayList<Double> averages) {
		Double averageOfAverages = 0.0;
		for (Double average : averages)
			averageOfAverages += average;
		if (averages.size() == 0)
			return 0.0;
		return averageOfAverages / averages.size();
	}

	/**
	 * creates student's grade information
	 * 
	 * @param rs - result set that has all the arguments for student grade
	 *           constructor
	 * @return student grade
	 * @throws SQLException
	 */
	public static StudentGrade createStudentGrade(ResultSet rs) throws SQLException {
		return new StudentGrade(rs.getString("testId"), rs.getString("course"), rs.getString("title"),
				rs.getInt("grade"));
	}

	/**
	 * checks if argument time is between given start and finish times
	 * 
	 * @param startingTime - String 00:00
	 * @param finishTime   - String 00:00
	 * @param argumentTime - String 00:00
	 * @return true if the argument is between the given times
	 */
	public static boolean isArgTimeBetweenStartAndFinishTimes(String startingTime, String finishTime,
			String argumentTime) {
		String[] details;
		int startHours;
		int startMinutes;
		int finishHours;
		int finishMinutes;
		int argumentHours;
		int argumentMinutes;
		details = startingTime.split(":");
		startHours = Integer.parseInt(details[0]);
		startMinutes = Integer.parseInt(details[1]);
		details = finishTime.split(":");
		finishHours = Integer.parseInt(details[0]);
		finishMinutes = Integer.parseInt(details[1]);
		details = argumentTime.split(":");
		argumentHours = Integer.parseInt(details[0]);
		argumentMinutes = Integer.parseInt(details[1]);
		if (argumentHours < startHours || argumentHours > finishHours)
			return false;
		else if (argumentHours == finishHours && argumentMinutes >= finishMinutes)
			return false;
		else if (argumentHours == startHours && argumentMinutes < startMinutes)
			return false;
		return true;
	}

	/**
	 * calculates the difference between now and a given starting time
	 * 
	 * @param startingTime example: 00:00
	 * @return time taken as integer
	 */
	public static int calculateTimeTaken(String startingTime) {
		LocalTime now = LocalTime.now();
		String nowString = now.toString();
		String[] times = nowString.split(":");
		int nowHours = Integer.parseInt(times[0]);
		int nowMinutes = Integer.parseInt(times[1]);
		times = startingTime.split(":");
		int startHours = Integer.parseInt(times[0]);
		int startMinutes = Integer.parseInt(times[1]);
		int takenHours = nowHours - startHours;
		int takenMinutes = nowMinutes - startMinutes;
		return takenHours * 60 + takenMinutes;
	}

	/**
	 * checks if the argument date is between the range of dates given
	 * 
	 * @param startDate    - 00/00/0000
	 * @param finishDate   - 00/00/0000
	 * @param argumentDate - 00/00/0000
	 * @return
	 */
	public static boolean isDateInRange(String startDate, String finishDate, String argumentDate) {
		String[] dates;
		dates = startDate.split("/");
		int startDay = Integer.parseInt(dates[0]);
		int startMonth = Integer.parseInt(dates[1]);
		int startYear = Integer.parseInt(dates[2]);
		dates = finishDate.split("/");
		int finishDay = Integer.parseInt(dates[0]);
		int finishMonth = Integer.parseInt(dates[1]);
		int finishYear = Integer.parseInt(dates[2]);
		dates = argumentDate.split("/");
		int argumentDay = Integer.parseInt(dates[0]);
		int argumentMonth = Integer.parseInt(dates[1]);
		int argumentYear = Integer.parseInt(dates[2]);
		// argumentYear out of range
		if (argumentYear < startYear || argumentYear > finishYear)
			return false;
		// same as startYear smaller than finishYear
		if (argumentYear == startYear && argumentYear < finishYear) {
			if (argumentMonth < startMonth)
				return false;
			else if (argumentMonth == startMonth && argumentDay < startDay)
				return false;
			return true;
		}
		// same as finishYear bigger than startYear
		if (argumentYear > startYear && argumentYear == finishYear) {
			if (argumentMonth > finishMonth)
				return false;
			else if (argumentMonth == finishMonth && argumentDay > finishDay)
				return false;
			return true;
		}
		// same as startYear and finishYear
		// argumentMonth out of range
		if (argumentMonth < startMonth || argumentMonth > finishMonth)
			return false;
		// same as startMonth smaller than finishMonth
		if (argumentMonth == startMonth && argumentMonth < finishMonth) {
			if (argumentDay < startDay)
				return false;
			return true;
		}
		// same as finishMonth bigger than startMonth
		if (argumentMonth == finishMonth && argumentMonth > startMonth) {
			if (argumentDay > finishDay)
				return false;
			return true;
		}
		// same as startMonth and finishMonth
		// argumentDay out of range
		if (argumentDay < startDay || argumentDay > finishDay)
			return false;
		return true;
	}
}
