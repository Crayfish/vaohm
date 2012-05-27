import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_PLAIN;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;

import java.io.File;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_video.BackgroundSubtractorMOG2;

public class MotionDetection {
	private IplImage prevImg, currImg, diffImg;

	public static void main(String[] args) throws Exception {
		// Preload the opencv_objdetect module to work around a known

		CvSeq contour = new CvSeq(null);
		CvSeq ptr = new CvSeq();
		Loader.load(opencv_objdetect.class);
		CvMemStorage storage = CvMemStorage.create();
		CanvasFrame frameInput = new CanvasFrame("Original");
		CanvasFrame frameOutput = new CanvasFrame("Foregroung");
		File f = new File("lib/squash1.avi");
		FrameGrabber grabber = new OpenCVFrameGrabber(f);
		grabber.start();
		IplImage grabbedImage = grabber.grab();
		grabber.start();

		IplImage foreground = null;
		// BackgroundSubtractorMOG2 mog = null;
		BackgroundSubtractorMOG2 mog = new BackgroundSubtractorMOG2(30, 16,
				true);
		IplImage frame = grabbedImage.clone();

		while (frameInput.isVisible()
				&& (grabbedImage = grabber.grab()) != null) {

			// cvSmooth(grabbedImage, grabbedImage, CV_GAUSSIAN, 9, 9, 2, 2);

			if (foreground == null) {
				foreground = IplImage.create(frame.width(), frame.height(),
						IPL_DEPTH_8U, 1);
			}

			mog.apply(grabbedImage, foreground, -1);

			// morph. schliessen
			cvDilate(foreground, foreground, null, 5);
			cvErode(foreground, foreground, null, 5);

			cvSmooth(foreground, foreground, CV_MEDIAN, 3, 3, 2, 2);

			cvFindContours(foreground, storage, contour,
					Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);
			// cvDilate(diff, diff, null, 3);
			// cvErode(diff, diff, null, 3);

			CvRect boundbox;

			int cnt = 0;
			for (ptr = contour; ptr != null; ptr = ptr.h_next()) {

				boundbox = cvBoundingRect(ptr, 0);

				cvRectangle(
						grabbedImage,
						cvPoint(boundbox.x(), boundbox.y()),
						cvPoint(boundbox.x() + boundbox.width(), boundbox.y()
								+ boundbox.height()), CV_RGB(255, 0, 0), 1, 8,
						0);

				CvFont font = new CvFont(CV_FONT_HERSHEY_PLAIN, 1, 1);
				cvPutText(grabbedImage, " " + cnt,
						cvPoint(boundbox.x(), boundbox.y()), font, CvScalar.RED);

				// Color randomColor = new Color(rand.nextFloat(),
				// rand.nextFloat(), rand.nextFloat());
				// CvScalar color = CV_RGB(randomColor.getRed(),
				// randomColor.getGreen(), randomColor.getBlue());
				// cvDrawContours(diff, ptr, color, CV_RGB(0, 0, 0), -1,
				// CV_FILLED, 8, cvPoint(0, 0));

			}

			frameInput.showImage(grabbedImage);
			frameOutput.showImage(foreground);
		}
		grabber.stop();
		frameInput.dispose();
		frameOutput.dispose();
	}
}