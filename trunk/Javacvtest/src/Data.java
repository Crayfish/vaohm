import java.awt.Point;

/**
 * Object for data saving and printing
 * 
 * Saves the capture time and the positions of the players or the error message
 * 
 * 
 * @author Márk Ormos, Thomas Mayr
 * 
 * 
 */
public class Data {

	/** Timestamp from the media player */
	private double time;

	/** position of player 1 */
	private Point player1;

	/** position of player 2 */
	private Point player2;

	/** failure message */
	private String failure = null;

	/** default constructor for position saving */
	public Data(double time, Point player1, Point player2) {
		this.time = roundTwoDecimals(time);
		this.player1 = player1;
		this.player2 = player2;
	}

	/** default constructor for error logging */
	public Data(double time, String failure) {
		this.time = roundTwoDecimals(time);
		this.failure = failure;
	}

	/***
	 * prints the informations
	 * 
	 * @return String containing all the informations
	 */
	public String print() {
		if (failure == null) {
			return time + " \t(" + player1.x + "/" + player1.y + ") \t ("
					+ player2.x + "/" + player2.y + ")";
		} else
			return time + " \t " + failure;
	}

	/**
	 * Round the time to two decimals
	 * 
	 * @param d
	 *            time to round
	 * @return rounded time to two decimals
	 */
	private double roundTwoDecimals(double d) {
		int ix = (int) (d * 100.0); // scale it
		return ((double) ix) / 100.0;

	}
}
