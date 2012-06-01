import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvLine;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;

/**
 * Class for Image Processing and Motion Detection
 * 
 * Processes every frame in order to detect the players on the squash field
 * 
 * we are using javaCV, the java wrapper for OpenCV for image manipulation
 * 
 * @author Márk Ormos, Thomas Mayr
 * @since 28.05.2012
 */
public class ImageProcessor {

	/** Sequent to store contours (blobs) */
	private CvSeq contour = new CvSeq(null);

	/** sequent used as pointer to contour */
	private CvSeq ptr = new CvSeq();

	/** memory to store sequents */
	private CvMemStorage storage;

	/** to eliminate the background */
	private BackgroundSubtractorMOG2 mog = new BackgroundSubtractorMOG2(30, 16,
			true);

	/** image to store the foreground */
	private IplImage foreground = null;

	/** to save the collide image */
	private IplImage tempImg = null;
	// CanvasFrame canves = new CanvasFrame("Blob View");
	//
	// CanvasFrame testFrame = new CanvasFrame("Collide View");

	/** position of player 1 in the current frame */
	private Point ptCurrentPlayer1 = null;

	/** position of player 2 in the current frame */
	private Point ptCurrentPlayer2 = null;

	/** position of player 1 in the previous frame */
	private Point ptPrevPlayer1 = null;

	/** position of player 2 in the previous frame */
	private Point ptPrevPlayer2 = null;

	/** counts the blobs in the image */
	private int cnt = 0;

	/** stores the current surrounding rect for a blob */
	private CvRect boundbox = null;

	/** stores the biggest blob in the image */
	private CvRect Biggestboundbox = null;

	/** stores the second biggest blob in the image */
	private CvRect Bigboundbox = null;

	/** original frame too draw on */
	private IplImage orig = null;

	/** List for collecting position data */
	private List<Data> dataCollector;

	/** to collect the images for displaying */
	private List<BufferedImage> images;

	/**
	 * Default constructor for the Image processor
	 * 
	 * @param dataCollector
	 *            List for collecting position data
	 */
	public ImageProcessor(List<Data> dataCollector) {
		// Preload the opencv_objdetect module to work around a known bug
		Loader.load(opencv_objdetect.class);
		storage = CvMemStorage.create();
		// canves.setVisible(false);
		// testFrame.setVisible(false);
		this.dataCollector = dataCollector;
	}

	/**
	 * Processes the current frame of the video in order to detect and identify
	 * the players.
	 * 
	 * We use nearly the same approach for blob detection described in the paper
	 * "A Low-Cost Real-Time Tracker of Live Sport Events"
	 * 
	 * 1. eliminate background
	 * 
	 * 2. use filters and morph. operations to reduce noise
	 * 
	 * 3. get the two biggest blobs. if there is only one blob, it is assumed
	 * that the players collided.
	 * 
	 * 4. separate the blob if players are too near to each other
	 * 
	 * 
	 * @param grabbedImage
	 *            the current frame grabbed from the video
	 * @param currentTime
	 *            the timestamp of the current frame
	 * @return input image with markers
	 */
	public synchronized List<BufferedImage> process(IplImage grabbedImage,
			double currentTime) {

		images = new LinkedList<BufferedImage>();

		if (grabbedImage != null) {

			try {

				// copy the original frame to draw on it
				orig = grabbedImage.clone();

				if (foreground == null) {
					foreground = IplImage.create(grabbedImage.width(),
							grabbedImage.height(), IPL_DEPTH_8U, 1);
				}

				// eliminate background
				mog.apply(grabbedImage, foreground, -1);

				// morph. close
				cvDilate(foreground, foreground, null, 3);
				cvErode(foreground, foreground, null, 3);

				// 9x9 Median Filter
				cvSmooth(foreground, foreground, CV_MEDIAN, 9, 9, 2, 2);

				// 9x9 Gauss Filter
				cvSmooth(foreground, foreground, CV_GAUSSIAN, 9, 9, 2, 2);

				// morph. close again
				cvDilate(foreground, foreground, null, 3);
				cvErode(foreground, foreground, null, 3);

				images.add(foreground.getBufferedImage());

				if (tempImg == null) {
					tempImg = IplImage.create(grabbedImage.width(),
							grabbedImage.height(), IPL_DEPTH_8U, 1);
					images.add(tempImg.getBufferedImage());
				}

				// find and save blobs
				getBlobs(foreground);

				if (cnt == 1) { // if there is only one blob then collosion

					ptCurrentPlayer1 = ptPrevPlayer1;
					ptCurrentPlayer2 = ptPrevPlayer2;

					CvRect temp = Biggestboundbox;

					List<Point> points = divideBlob1(ptCurrentPlayer1,
							ptCurrentPlayer2);

					if (points.size() > 0) {
						Point first = points.get(0);
						Point last = points.get(1);

						cvLine(foreground, cvPoint(first.x, first.y),
								cvPoint(last.x, last.y), CV_RGB(0, 0, 0), 5, 8,
								0);

						cvLine(orig, cvPoint(first.x, first.y),
								cvPoint(last.x, last.y), CV_RGB(255, 255, 255),
								2, 8, 0);

						getBlobs(foreground);

						cvRectangle(
								foreground,
								cvPoint(temp.x(), temp.y()),
								cvPoint(temp.x() + temp.width(), temp.y()
										+ temp.height()),
								CV_RGB(255, 255, 255), 1, 8, 0);

						cvLine(foreground, cvPoint(first.x, first.y),
								cvPoint(last.x, last.y), CV_RGB(255, 255, 255),
								1, 8, 0);
						cvCircle(
								foreground,
								cvPoint(ptCurrentPlayer2.x, ptCurrentPlayer2.y),
								2, CV_RGB(255, 255, 255), 1, 8, 0);

						cvCircle(
								foreground,
								cvPoint(ptCurrentPlayer1.x, ptCurrentPlayer1.y),
								2, CV_RGB(255, 255, 255), 1, 8, 0);

						Iterator<Point> it = points.iterator();
						while (it.hasNext()) {
							Point curr = it.next();
							cvCircle(foreground, cvPoint(curr.x, curr.y), 1,
									CV_RGB(255, 255, 255), 1, 8, 0);

						}

						tempImg = foreground.clone();
					}

				}

				if (cnt == 1) {// if blob could not be separated
					System.err.println("unresolved blob separation");
					dataCollector.add(new Data(currentTime,
							"unresolved blob separation"));
					cvRectangle(
							orig,
							cvPoint(Biggestboundbox.x(), Biggestboundbox.y()),
							cvPoint(Biggestboundbox.x()
									+ Biggestboundbox.width(),
									Biggestboundbox.y()
											+ Biggestboundbox.height()),
							CV_RGB(255, 0, 0), 1, 8, 0);

				}

				// if more than one blob, get the position of the players by the
				// center of the surrounding box
				else {

					ptCurrentPlayer1 = new Point(
							(Biggestboundbox.x() + Biggestboundbox.width() / 2),
							(Biggestboundbox.y() + Biggestboundbox.height() / 2));

					ptCurrentPlayer2 = new Point(
							(Bigboundbox.x() + Bigboundbox.width() / 2),
							(Bigboundbox.y() + Bigboundbox.height() / 2));

					// check the distance between the players
					// note: only the first player is checked to avoid overwrite
					if (ptPrevPlayer1 != null && ptPrevPlayer2 != null) {

						double distP1Prev1 = distance(ptCurrentPlayer1,
								ptPrevPlayer1);
						double distP1Prev2 = distance(ptCurrentPlayer1,
								ptPrevPlayer2);

						if (distP1Prev1 > distP1Prev2)

						{
							Point temp = ptCurrentPlayer1;
							ptCurrentPlayer1 = ptCurrentPlayer2;
							ptCurrentPlayer2 = temp;

							boundbox = Biggestboundbox;
							Biggestboundbox = Bigboundbox;
							Bigboundbox = boundbox;

						}
						// draw a line from the previous position to the current
						cvLine(orig,
								cvPoint(ptCurrentPlayer1.x, ptCurrentPlayer1.y),
								cvPoint(ptPrevPlayer1.x, ptPrevPlayer1.y),
								CV_RGB(255, 0, 0), 1, 8, 0);
						cvLine(orig,
								cvPoint(ptCurrentPlayer2.x, ptCurrentPlayer2.y),
								cvPoint(ptPrevPlayer2.x, ptPrevPlayer2.y),
								CV_RGB(0, 255, 0), 1, 8, 0);
					}

					// draw the position of the players
					cvCircle(orig,
							cvPoint(ptCurrentPlayer1.x, ptCurrentPlayer1.y), 2,
							CV_RGB(255, 0, 0), 1, 8, 0);

					cvCircle(orig,
							cvPoint(ptCurrentPlayer2.x, ptCurrentPlayer2.y), 2,
							CV_RGB(0, 255, 0), 1, 8, 0);

					// draw a rectangle around the players
					cvRectangle(
							orig,
							cvPoint(Biggestboundbox.x(), Biggestboundbox.y()),
							cvPoint(Biggestboundbox.x()
									+ Biggestboundbox.width(),
									Biggestboundbox.y()
											+ Biggestboundbox.height()),
							CV_RGB(255, 0, 0), 1, 8, 0);

					cvRectangle(
							orig,
							cvPoint(Bigboundbox.x(), Bigboundbox.y()),
							cvPoint(Bigboundbox.x() + Bigboundbox.width(),
									Bigboundbox.y() + Bigboundbox.height()),
							CV_RGB(0, 255, 0), 1, 8, 0);

					// draw the safe area
					cvRectangle(orig, cvPoint(10, 10), cvPoint(365, 270),
							CV_RGB(0, 0, 255), 1, 8, 0);

					ptPrevPlayer1 = ptCurrentPlayer1;
					ptPrevPlayer2 = ptCurrentPlayer2;
				}

			} catch (Exception e) {

				System.out.println("no frame");
				dataCollector.add(new Data(currentTime, "No Frame"));

				return null;

			}
			dataCollector.add(new Data(currentTime, ptCurrentPlayer1,
					ptCurrentPlayer2));
			images.add(tempImg.getBufferedImage());
			images.add(orig.getBufferedImage());

			return images;

		}
		return null;
	}

	/**
	 * calculate the distance between 2 Points
	 * 
	 * @param p1
	 *            position 1
	 * @param p2
	 *            position 2
	 * @return the distance
	 */
	private double distance(Point p1, Point p2) {
		int xDiff = Math.abs(p1.x - p2.x);
		int yDiff = Math.abs(p1.y - p2.y);

		return Math.sqrt(Math.exp(xDiff) + Math.exp(yDiff));

	}

	/**
	 * Finds the blobs in the processed image with the help of their contours
	 * and sorts them according to their size. Only the two biggest blobs wil be
	 * considered for the tracking. It is assumed that the two biggest blobs
	 * represent the players. Not all the noise could be reduced during the
	 * image processing, in the first line the borders are critical in this
	 * regard, so this area will not be considered in further steps. The safe
	 * area is marked by a blue rectangle.
	 * 
	 * 
	 * @param foreground
	 *            the processed image
	 * @return the amount of blobs in this frame
	 */
	private int getBlobs(IplImage foreground) {

		cvFindContours(foreground, storage, contour,
				Loader.sizeof(CvContour.class), CV_RETR_LIST,
				CV_CHAIN_APPROX_SIMPLE);

		boundbox = null;
		Biggestboundbox = null;
		Bigboundbox = null;
		// count the blobs
		cnt = 0;

		// iterate over the contours (countour = blob)
		// get the two biggest blobs (=players)
		for (ptr = contour; ptr != null; ptr = ptr.h_next()) {

			// get the bounding Box surrounding the contour area
			boundbox = cvBoundingRect(ptr, 0);

			if (boundbox.y() > 10 && boundbox.y() < 270 && boundbox.x() > 10
					&& boundbox.x() < 365) { // ignore at the borders (too
												// noisy, squash 1.avi)

				// get the two biggest blobs
				if (Biggestboundbox == null) {
					Biggestboundbox = boundbox;
				} else if (Bigboundbox == null) {
					Bigboundbox = boundbox;

				} else if ((Biggestboundbox.width() * Biggestboundbox.height()) < (boundbox
						.width() * boundbox.height())) {
					Bigboundbox = Biggestboundbox;
					Biggestboundbox = boundbox;
				} else if ((Bigboundbox.width() * Bigboundbox.height()) < (boundbox
						.width() * boundbox.height())) {
					Bigboundbox = boundbox;
				}

				cnt++;
			}

		}
		return cnt;
	}

	/**
	 * Divide the blob if collosion occures, use the previous positions of the
	 * players. Elliminate the pixels, which have the same distance to both
	 * players. Problem: the points do not form a line, so that they do not
	 * separate the blob. As an approach the first and the last points were
	 * connected for this purpose.
	 * 
	 * @param boundbox
	 *            the bounding box of the blob
	 * @param p1
	 *            position of player 1
	 * @param p2
	 *            position of player 2
	 * @return the list of points, which have the same distance to both players
	 */
	private List<Point> divideBlob(CvRect boundbox, Point p1, Point p2) {

		int width = boundbox.width();
		int height = boundbox.height();

		// make the points relative to the boundbox
		Point player1 = new Point(p1.x - boundbox.x(), p1.y - boundbox.y());
		Point player2 = new Point(p2.x - boundbox.x(), p2.y - boundbox.y());

		Point current;

		List<Point> points = new LinkedList<Point>();

		try {
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					current = new Point(j, i);
					if (Math.round(distance(current, player1)) == Math
							.round(distance(current, player2))) {
						points.add(new Point(j + boundbox.x(), i + boundbox.y()));
					}
				}

			}

		} catch (Exception e) {
			System.out.println("Divide failure");
		}
		return points;

	}

	/**
	 * Another approach for blob separation: draw a normal to the line which
	 * connects the two positions of the players and intersects the middle of
	 * this line. The two end points will be calculated. Problem: the lenght of
	 * the normal is dependent from the lenght of the line between the two
	 * players. If the players are too near to each other, the normal will be
	 * short as well and wont be able to cut the blob into two pieces.
	 * 
	 * 
	 * @param p1
	 *            position of player 1
	 * @param p2
	 *            position of player 2
	 * @return two Points, the two endpoints of the normal
	 */
	private List<Point> divideBlob1(Point p1, Point p2) {

		List<Point> points = new LinkedList<Point>();

		int x1 = p1.x;
		int x2 = p2.x;

		int y1 = p1.y;
		int y2 = p2.y;

		int dx = x2 - x1;
		int dy = y2 - y1;

		int centerx = (x1 + x2) / 2;
		int centery = (y1 + y2) / 2;

		Point point1 = new Point(-dy + centerx, dx + centery);
		Point point2 = new Point(dy + centerx, -dx + centery);

		// System.out.println("pos: (" + x1 + "/" + y1 + ") (" + x2 + "/" + y2
		// + ") normal: (" + point1.x + "/" + point1.y + ") (" + point2.x
		// + "/" + point2.y + ")");

		points.add(point1);
		points.add(point2);

		return points;

	}

}
