import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_PLAIN;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
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
		CanvasFrame frametest = new CanvasFrame("test");
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

		CvFont font = new CvFont(CV_FONT_HERSHEY_PLAIN, 1, 1);
		CvRect rect = new CvRect(0, 0, frame.width() - 3, frame.height() - 3);
		IplImage mask = IplImage.create(frame.width(), frame.height() - 10,
				IPL_DEPTH_8U, 1);

		cvSetImageROI(frame, rect);
		// cvCopy(frame, frame);

		cvRectangle(frame, cvPoint(rect.x(), rect.y()),
				cvPoint(rect.x() + rect.width(), rect.y() + rect.height()),
				CV_RGB(255, 0, 0), 1, 8, 0);

		frametest.showImage(frame);

		System.out.println("width:" + frame.width() + " height:"
				+ (frame.height() - 10));

		while (frameInput.isVisible()
				&& (grabbedImage = grabber.grab()) != null) {

			// cvSetImageROI(grabbedImage, rect);

			if (foreground == null) {
				foreground = IplImage.create(frame.width(), frame.height(),
						IPL_DEPTH_8U, 1);
			}

			mog.apply(grabbedImage, foreground, -1);

			// morph. schliessen
			cvDilate(foreground, foreground, null, 4);
			cvErode(foreground, foreground, null, 5);

			cvSmooth(foreground, foreground, CV_MEDIAN, 9, 9, 2, 2);

			cvSmooth(foreground, foreground, CV_GAUSSIAN, 9, 9, 2, 2);

			cvFindContours(foreground, storage, contour,
					Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);

			CvRect boundbox;

			int cnt = 0;
			for (ptr = contour; ptr != null; ptr = ptr.h_next()) {

				boundbox = cvBoundingRect(ptr, 0);

				if (boundbox.y() < 278) {
					cvPutText(grabbedImage, " " + cnt,
							cvPoint(boundbox.x(), boundbox.y()), font,
							CvScalar.RED);

					cvRectangle(
							grabbedImage,
							cvPoint(boundbox.x(), boundbox.y()),
							cvPoint(boundbox.x() + boundbox.width(),
									boundbox.y() + boundbox.height()),
							CV_RGB(255, 0, 0), 1, 8, 0);

					// Color randomColor = new Color(rand.nextFloat(),
					// rand.nextFloat(), rand.nextFloat());
					// CvScalar color = CV_RGB(randomColor.getRed(),
					// randomColor.getGreen(), randomColor.getBlue());
					// cvDrawContours(diff, ptr, color, CV_RGB(0, 0, 0), -1,
					// CV_FILLED, 8, cvPoint(0, 0));

					cnt++;
				}

			}

			frameInput.showImage(grabbedImage);
			frameOutput.showImage(foreground);
		}
		grabber.stop();
		frameInput.dispose();
		frameOutput.dispose();
	}
}