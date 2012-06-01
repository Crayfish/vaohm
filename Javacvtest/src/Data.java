import java.awt.Point;

public class Data {

	private double time;
	private Point player1;
	private Point player2;
	private String failure = null;

	public Data(double time, Point player1, Point player2) {
		this.time = roundTwoDecimals(time);
		this.player1 = player1;
		this.player2 = player2;
	}

	public Data(double time, String failure) {
		this.time = roundTwoDecimals(time);
		this.failure = failure;
	}

	public String print() {
		if (failure == null) {
			return time + " \t(" + player1.x + "/" + player1.y + ") \t ("
					+ player2.x + "/" + player2.y + ")";
		} else
			return time + " \t " + failure;
	}

	double roundTwoDecimals(double d) {
		int ix = (int) (d * 100.0); // scale it
		return ((double) ix) / 100.0;

	}
}
