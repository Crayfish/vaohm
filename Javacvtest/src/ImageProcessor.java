import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_PLAIN;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
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

	Point ptCurrentPlayer1 = null;
	Point ptCurrentPlayer2 = null;

	Point ptPrevPlayer1 = null;
	Point ptPrevPlayer2 = null;

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
		IplImage orig = grabbedImage.clone();

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
		// find and save contours
		cvFindContours(foreground, storage, contour,
				Loader.sizeof(CvContour.class), CV_RETR_LIST,
				CV_CHAIN_APPROX_SIMPLE);

		CvRect boundbox;
		CvRect Biggestboundbox = null;
		CvRect Bigboundbox = null;
		int cnt = 0;

		// iterate over the contours (countour = blob)
		// get the two biggest blobs (=players)
		for (ptr = contour; ptr != null; ptr = ptr.h_next()) {

			// get the bounding Box surrounding the contour area

			boundbox = cvBoundingRect(ptr, 0);
			if (boundbox.y() < 278
					|| boundbox.x() + boundbox.width() == foreground.width()) {

				if (Biggestboundbox == null) {
					Biggestboundbox = boundbox;

				} else if (Bigboundbox == null) {
					Bigboundbox = boundbox;
				} else if ((Biggestboundbox.width() * Biggestboundbox.height()) < (boundbox
						.width() * boundbox.height())) {
					Biggestboundbox = boundbox;
				} else if ((Bigboundbox.width() * Bigboundbox.height()) < (boundbox
						.width() * boundbox.height())) {
					Bigboundbox = boundbox;
				}
				// draw a rectangle around the blob

				cnt++;
			}

		}

		ptCurrentPlayer1 = new Point(
				(Biggestboundbox.x() + Biggestboundbox.width() / 2),
				(Biggestboundbox.y() + Biggestboundbox.height() / 2));

		ptCurrentPlayer2 = new Point(
				(Bigboundbox.x() + Bigboundbox.width() / 2),
				(Bigboundbox.y() + Bigboundbox.height() / 2));

		if (ptPrevPlayer1 != null || ptPrevPlayer2 != null) {
			if (distance(ptCurrentPlayer1, ptPrevPlayer1) > distance(
					ptCurrentPlayer1, ptPrevPlayer2)) {
				Point temp = ptCurrentPlayer1;
				ptCurrentPlayer1 = ptCurrentPlayer2;
				ptCurrentPlayer2 = temp;
			}
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

		ptPrevPlayer1 = ptCurrentPlayer1;
		ptPrevPlayer2 = ptCurrentPlayer2;
		return orig.getBufferedImage();
	}

	/**
	 * set threshold value
	 * 
	 * @param thres
	 */
	public void setThreshold(int thres) {
		this.thres = thres;
	}

	private double distance(Point p1, Point p2) {
		int xDiff = Math.abs(p1.x - p2.x);
		int yDiff = Math.abs(p1.y - p2.y);

		return Math.sqrt(Math.exp(xDiff) + Math.exp(yDiff));

	}

}
