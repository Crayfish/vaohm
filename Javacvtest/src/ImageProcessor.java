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
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.awt.image.BufferedImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;

public class ImageProcessor {

	private IplImage prevImage = null;
	private IplImage image = null;
	private IplImage diff = null;
	private IplImage foreground = null;
	private CvMemStorage storage = CvMemStorage.create();
	private int thres = 120;

	CvSeq contour = new CvSeq(null);
	CvSeq ptr = new CvSeq();
	// CanvasFrame imageCanvas = new CanvasFrame("image");
	// CanvasFrame canvas = new CanvasFrame("foreground");
	BackgroundSubtractorMOG2 mog = null;

	public ImageProcessor() {

	}

	public synchronized BufferedImage process(IplImage frame) {

		// Loader.load(opencv_objdetect.class);
		// IplImage image = frame.clone();
		// IplImage foreground = frame.clone();
		//
		// mog = new BackgroundSubtractorMOG2();
		//
		// mog.apply(image, foreground, 200);
		//
		// cvThreshold(foreground, foreground, 120, 255, CV_THRESH_BINARY);
		// medianBlur(foreground, foreground, 3);
		// cvErode(foreground, foreground, null, 10);
		// cvDilate(foreground, foreground, null, 18);
		//
		// imageCanvas.showImage(frame);
		// canvas.showImage(foreground);

		// original frame,am ende rechtecke draufzeichnen
		IplImage orig = frame.clone();

		cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
		if (image == null) {
			image = IplImage.create(frame.width(), frame.height(),
					IPL_DEPTH_8U, 1);
			cvCvtColor(frame, image, CV_RGB2GRAY);
		} else {
			prevImage = IplImage.create(frame.width(), frame.height(),
					IPL_DEPTH_8U, 1);
			prevImage = image;
			image = IplImage.create(frame.width(), frame.height(),
					IPL_DEPTH_8U, 1);
			cvCvtColor(frame, image, CV_RGB2GRAY);
			cvThreshold(image, image, thres, 255, CV_THRESH_BINARY);

			// 3x3 Median filter
			cvSmooth(image, image, CV_MEDIAN, 3, 3, 2, 2);

			// morph. schliessen
			cvDilate(image, image, null, 3);
			cvErode(image, image, null, 3);

			// cvCanny(image, image, 80, 120, 3);

		}
		// canvasFrame.showImage(frame);

		if (diff == null) {
			diff = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U,
					1);
		}

		if (prevImage != null) {
			// perform ABS difference
			// cvAbsDiff(image, prevImage, diff);
			// // do some threshold for wipe away useless details
			// cvThreshold(diff, diff, 50, 255, CV_THRESH_BINARY);
			//
			// // recognize contours
			//
			cvFindContours(image, storage, contour,
					Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);
			// cvDilate(diff, diff, null, 3);
			// cvErode(diff, diff, null, 3);

			CvRect boundbox;

			int cnt = 0;
			for (ptr = contour; ptr != null; ptr = ptr.h_next()) {

				boundbox = cvBoundingRect(ptr, 0);

				cvRectangle(
						orig,
						cvPoint(boundbox.x(), boundbox.y()),
						cvPoint(boundbox.x() + boundbox.width(), boundbox.y()
								+ boundbox.height()), CV_RGB(255, 0, 0), 1, 8,
						0);

				CvFont font = new CvFont(CV_FONT_HERSHEY_PLAIN, 1, 1);
				cvPutText(orig, " " + cnt, cvPoint(boundbox.x(), boundbox.y()),
						font, CvScalar.RED);

				// Color randomColor = new Color(rand.nextFloat(),
				// rand.nextFloat(), rand.nextFloat());
				// CvScalar color = CV_RGB(randomColor.getRed(),
				// randomColor.getGreen(), randomColor.getBlue());
				// cvDrawContours(diff, ptr, color, CV_RGB(0, 0, 0), -1,
				// CV_FILLED, 8, cvPoint(0, 0));

			}

		}
		return orig.getBufferedImage();
	}

	public void setThreshold(int thres) {
		this.thres = thres;
	}

}
