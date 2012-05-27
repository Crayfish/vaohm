import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAbsDiff;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMinAreaRect2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.awt.image.BufferedImage;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize2D32f;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ImageProcessor {

	private IplImage prevImage = null;
	private IplImage image = null;
	private IplImage diff = null;
	private CvMemStorage storage = CvMemStorage.create();
	private int thres = 120;

	public ImageProcessor() {

	}

	public synchronized BufferedImage process(IplImage frame) {

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
			cvSmooth(image, image, CV_MEDIAN, 3, 3, 2, 2);
			cvErode(image, image, null, 3);
			cvDilate(image, image, null, 3);

		}
		// canvasFrame.showImage(frame);

		if (diff == null) {
			diff = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U,
					1);
		}

		if (prevImage != null) {
			// perform ABS difference
			cvAbsDiff(image, prevImage, diff);
			// do some threshold for wipe away useless details
			cvThreshold(diff, diff, 50, 255, CV_THRESH_BINARY);

			// canvasFrame.showImage(diff);

			// grabber.stop();

			// recognize contours
			CvSeq contour = new CvSeq(null);
			cvFindContours(diff, storage, contour,
					Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);

			while (contour != null && !contour.isNull()) {
				// System.out.println(contour.elem_size());
				if (contour.elem_size() > 0) {
					CvBox2D box = cvMinAreaRect2(contour, storage);
					// test intersection
					if (box != null) {
						CvPoint2D32f center = box.center();
						CvSize2D32f size = box.size();
						// System.out.println(center.x() + " " +
						// center.y());

						/*
						 * for (int i = 0; i < sa.length; i++) { if
						 * ((Math.abs(center.x - (sa[i].offsetX + sa[i].width /
						 * 2))) < ((size.width / 2) + (sa[i].width / 2)) &&
						 * (Math.abs(center.y - (sa[i].offsetY + sa[i].height /
						 * 2))) < ((size.height / 2) + (sa[i].height / 2))) {
						 * 
						 * if (!alarmedZones.containsKey(i)) {
						 * alarmedZones.put(i, true); activeAlarms.put(i, 1); }
						 * else { activeAlarms.remove(i); activeAlarms.put(i,
						 * 1); }
						 * System.out.println("Motion Detected in the area no: "
						 * + i + " Located at points: (" + sa[i].x + ", " +
						 * sa[i].y+ ") -" + " (" + (sa[i].x +sa[i].width) + ", "
						 * + (sa[i].y+sa[i].height) + ")"); } }
						 */
					}
				}
				contour = contour.h_next();
			}
		}

		return image.getBufferedImage();

	}

	public void setThreshold(int thres) {
		this.thres = thres;
	}

}
