import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_PLAIN;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
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

import java.awt.image.BufferedImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
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
		cvDilate(foreground, foreground, null, 4);
		cvErode(foreground, foreground, null, 5);

		// 9x9 Median Filter
		cvSmooth(foreground, foreground, CV_MEDIAN, 9, 9, 2, 2);

		// 9x9 Gauss Filter
		cvSmooth(foreground, foreground, CV_GAUSSIAN, 9, 9, 2, 2);

		// morph. close again
		cvDilate(foreground, foreground, null, 4);
		cvErode(foreground, foreground, null, 5);

		// find and save contours
		cvFindContours(foreground, storage, contour,
				Loader.sizeof(CvContour.class), CV_RETR_LIST,
				CV_CHAIN_APPROX_SIMPLE);

		CvRect boundbox;

		int cnt = 0;

		// iterate over the contours (countour = blob)
		for (ptr = contour; ptr != null; ptr = ptr.h_next()) {

			// get the bounding Box surrounding the contour area
			boundbox = cvBoundingRect(ptr, 0);

			if (boundbox.y() < 278) {// bugfix: ignore bottom of the picture
										// (noise)

				// draw a number to identify the blob
				cvPutText(orig, " " + cnt, cvPoint(boundbox.x(), boundbox.y()),
						font, CvScalar.RED);

				// draw a rectangle around the blob
				cvRectangle(
						orig,
						cvPoint(boundbox.x(), boundbox.y()),
						cvPoint(boundbox.x() + boundbox.width(), boundbox.y()
								+ boundbox.height()), CV_RGB(255, 0, 0), 1, 8,
						0);

				cnt++;
			}

		}

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

}
