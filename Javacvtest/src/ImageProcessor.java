import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_PLAIN;
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
import java.util.LinkedList;
import java.util.List;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
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

	/** Threshold value */
	private int thres = 120;

	/** Sequent to store contours (blobs) */
	CvSeq contour = new CvSeq(null);

	/** sequent used as pointer to contour */
	CvSeq ptr = new CvSeq();

	/** memory to store sequents */
	CvMemStorage storage;

	/** Font type to write on image */
	CvFont font = new CvFont(CV_FONT_HERSHEY_PLAIN, 1, 1);

	/** to eliminate the background */
	BackgroundSubtractorMOG2 mog = new BackgroundSubtractorMOG2(30, 16, true);

	/** image to store the foreground */
	IplImage foreground = null;
	CanvasFrame canves = new CanvasFrame("ff");
	CanvasFrame testFrame = new CanvasFrame("test");

	Point ptCurrentPlayer1 = null;
	Point ptCurrentPlayer2 = null;

	Point ptPrevPlayer1 = null;
	Point ptPrevPlayer2 = null;

	/** counts the blobs in the image */
	int cnt = 0;

	/** stores the current surrounding rect for a blob */
	CvRect boundbox = null;
	/** stores the biggest blob in the image */
	CvRect Biggestboundbox = null;
	/** stores the second biggest blob in the image */
	CvRect Bigboundbox = null;

	IplImage orig = null;

	/**
	 * Default constructor
	 * 
	 */
	public ImageProcessor() {
		// Preload the opencv_objdetect module to work around a known bug
		Loader.load(opencv_objdetect.class);
		storage = CvMemStorage.create();
	}

	/**
	 * Processes the current frame of the video in order to detect and identify
	 * the players
	 * 
	 * We use nearly the same approach for blob detection described in the paper
	 * "A Low-Cost Real-Time Tracker of Live Sport Events"
	 * 
	 * 1. eliminate background 2. use filters and morph. operations to reduce
	 * noise
	 * 
	 * 
	 * @param grabbedImage
	 *            the current frame grabbed from the video
	 * @return input image with markers
	 */
	public synchronized BufferedImage process(IplImage grabbedImage) {

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
		canves.showImage(foreground);

		// find and save blobs
		getBlobs(foreground);

		// System.out.println(cnt);

		if (cnt == 1) { // if there is only one blob then collosion
			// System.out.println("collosion");

			List<Point> points = divideBlob(Biggestboundbox, ptPrevPlayer1,
					ptPrevPlayer2);
			// System.out.println(points.size());
			Point first = points.get(0);
			// System.out.println(first.x + "/" + first.y);
			Point last = points.get(points.size() - 1);
			// System.out.println(last.x + "/" + last.y);

			cvLine(foreground, cvPoint(first.x, first.y),
					cvPoint(last.x, last.y), CV_RGB(0, 0, 0), 6, 8, 0);
			cvLine(orig, cvPoint(first.x, first.y), cvPoint(last.x, last.y),
					CV_RGB(0, 0, 0), 6, 8, 0);

			System.out.println(getBlobs(foreground) + "= " + cnt + " blobs");
			// testFrame.showImage(foreground);

		}
		// if more than one blob, get the position of the players by the
		// center of the surrounding box
		ptCurrentPlayer1 = new Point(
				(Biggestboundbox.x() + Biggestboundbox.width() / 2),
				(Biggestboundbox.y() + Biggestboundbox.height() / 2));

		ptCurrentPlayer2 = new Point(
				(Bigboundbox.x() + Bigboundbox.width() / 2),
				(Bigboundbox.y() + Bigboundbox.height() / 2));

		if (ptPrevPlayer1 != null && ptPrevPlayer2 != null) {

			double distP1Prev1 = distance(ptCurrentPlayer1, ptPrevPlayer1);
			double distP1Prev2 = distance(ptCurrentPlayer1, ptPrevPlayer2);

			double distP2Prev2 = distance(ptCurrentPlayer2, ptPrevPlayer2);
			double distP2Prev1 = distance(ptCurrentPlayer2, ptPrevPlayer1);

			if (distP1Prev1 > distP1Prev2)

			{
				Point temp = ptCurrentPlayer1;
				ptCurrentPlayer1 = ptCurrentPlayer2;
				ptCurrentPlayer2 = temp;

				boundbox = Biggestboundbox;
				Biggestboundbox = Bigboundbox;
				Bigboundbox = boundbox;

			}
			cvLine(orig, cvPoint(ptCurrentPlayer1.x, ptCurrentPlayer1.y),
					cvPoint(ptPrevPlayer1.x, ptPrevPlayer1.y),
					CV_RGB(255, 0, 0), 1, 8, 0);
			cvLine(orig, cvPoint(ptCurrentPlayer2.x, ptCurrentPlayer2.y),
					cvPoint(ptPrevPlayer2.x, ptPrevPlayer2.y),
					CV_RGB(0, 255, 0), 1, 8, 0);
		}

		cvCircle(orig, cvPoint(ptCurrentPlayer1.x, ptCurrentPlayer1.y), 2,
				CV_RGB(255, 0, 0), 1, 8, 0);

		cvCircle(orig, cvPoint(ptCurrentPlayer2.x, ptCurrentPlayer2.y), 2,
				CV_RGB(0, 255, 0), 1, 8, 0);

		cvRectangle(
				orig,
				cvPoint(Biggestboundbox.x(), Biggestboundbox.y()),
				cvPoint(Biggestboundbox.x() + Biggestboundbox.width(),
						Biggestboundbox.y() + Biggestboundbox.height()),
				CV_RGB(255, 0, 0), 1, 8, 0);

		cvRectangle(
				orig,
				cvPoint(Bigboundbox.x(), Bigboundbox.y()),
				cvPoint(Bigboundbox.x() + Bigboundbox.width(), Bigboundbox.y()
						+ Bigboundbox.height()), CV_RGB(0, 255, 0), 1, 8, 0);

		cvRectangle(orig, cvPoint(10, 10), cvPoint(365, 270),
				CV_RGB(0, 0, 255), 1, 8, 0);

		ptPrevPlayer1 = ptCurrentPlayer1;
		ptPrevPlayer2 = ptCurrentPlayer2;
		return orig.getBufferedImage();
	}

	/**
	 * calculate the distance between 2 Points
	 * 
	 * @param p1
	 *            point 1
	 * @param p2
	 *            point 2
	 * @return the distance
	 */
	private double distance(Point p1, Point p2) {
		int xDiff = Math.abs(p1.x - p2.x);
		int yDiff = Math.abs(p1.y - p2.y);

		return Math.sqrt(Math.exp(xDiff) + Math.exp(yDiff));

	}

	/**
	 * finds the blobs in the processed image
	 * 
	 * @param foreground
	 *            the processed image
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

		// System.out.println(orig.width() + "x" + orig.height());

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
	 * divide the blob if collosion occures, use the previous positions of the
	 * players
	 * 
	 * @param width
	 * @param height
	 * @param p1
	 * @param p2
	 */
	private List<Point> divideBlob(CvRect boundbox, Point p1, Point p2) {

		int width = boundbox.width();
		int height = boundbox.height();

		// make the points relative to the boundbox
		Point player1 = new Point(p1.x - boundbox.x(), p1.y - boundbox.y());
		Point player2 = new Point(p2.x - boundbox.x(), p2.y - boundbox.y());

		Point current;

		List<Point> points = new LinkedList<Point>();

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				current = new Point(j, i);
				if (Math.round(distance(current, player1)) == Math
						.round(distance(current, player2))) {
					points.add(new Point(j + boundbox.x(), i + boundbox.y()));
				}
			}

		}
		return points;

	}

}
