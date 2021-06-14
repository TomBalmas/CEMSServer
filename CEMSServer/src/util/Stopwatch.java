package util;

import java.util.Timer;
import java.util.TimerTask;

public class Stopwatch {

	private static final int MINUTE = 1000 * 60;
	private static final int SECOND = 1000;
	private Timer minuteTimer;
	private int minutes;
	private boolean startFlag = true;

	public Stopwatch(int duration) {
		this.minutes = duration;
	}

	public int getMinutes() {
		return minutes;
	}

	/**
	 * The function start the timer for ending test.
	 * @param tt get the time for test.
	 */
	public synchronized void startTimer(TimerTask tt) {
		minuteTimer = new Timer();
		minuteTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (startFlag == true) {
					minutes++;
					startFlag = false;
				}
				minutes--;
				if (minutes <= 0) {
					tt.run();
					minuteTimer.cancel();
				}
			}
		}, 0, MINUTE);
	}

	/**
	 * The function add minutes to timer.
	 * @param minutesToAdd add time for timer.
	 */
	public synchronized void addMinutes(int minutesToAdd) {
		minutes += minutesToAdd;
	}

	/**
	 * The function stop the timer when reaches 0.
	 */
	public synchronized void stopTimer() {
		minuteTimer.cancel();
	}

}