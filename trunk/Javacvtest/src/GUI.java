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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;

import javax.swing.JFrame;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize2D32f;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class GUI extends JFrame {

	private ImgPanel pnlOrig = new ImgPanel();
	private ImgPanel pnlMod = new ImgPanel();

	public GUI() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() throws Exception {

		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;

		c.gridy = 0;

		getContentPane().add(pnlOrig, c);
		c.gridx = 1;

		getContentPane().add(pnlMod, c);

		File f = new File("lib/squash1.avi");
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(f);

		grabber.start();

		IplImage frame = grabber.grab();
		IplImage image = null;
		IplImage prevImage = null;
		IplImage diff = null;

		// CanvasFrame originalFrame = new CanvasFrame("Original");
		// originalFrame.setCanvasSize(frame.width(), frame.height());
		//
		// CanvasFrame canvasFrame = new CanvasFrame("Edited");
		// canvasFrame.setCanvasSize(frame.width(), frame.height());

		pnlOrig.setSize(frame.width(), frame.height());
		pnlMod.setSize(frame.width(), frame.height());

		CvMemStorage storage = CvMemStorage.create();

		setPreferredSize(new Dimension(frame.width() * 2, frame.height() * 2));
		setVisible(true);
		pack();

		while ((frame = grabber.grab()) != null) {

			// originalFrame.showImage(frame);
			pnlOrig.setImg(frame.getBufferedImage());
			pnlOrig.repaint();

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
				cvThreshold(image, image, 120, 255, CV_THRESH_BINARY);
				cvSmooth(image, image, CV_MEDIAN, 3, 3, 2, 2);
				cvErode(image, image, null, 3);
				cvDilate(image, image, null, 3);

			}
			// canvasFrame.showImage(frame);

			if (diff == null) {
				diff = IplImage.create(frame.width(), frame.height(),
						IPL_DEPTH_8U, 1);
			}

			if (prevImage != null) {
				// perform ABS difference
				cvAbsDiff(image, prevImage, diff);
				// do some threshold for wipe away useless details
				cvThreshold(diff, diff, 50, 255, CV_THRESH_BINARY);

				// canvasFrame.showImage(diff);
				pnlMod.setImg(diff.getBufferedImage());
				pnlMod.repaint();

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
							 * ((Math.abs(center.x - (sa[i].offsetX +
							 * sa[i].width / 2))) < ((size.width / 2) +
							 * (sa[i].width / 2)) && (Math.abs(center.y -
							 * (sa[i].offsetY + sa[i].height / 2))) <
							 * ((size.height / 2) + (sa[i].height / 2))) {
							 * 
							 * if (!alarmedZones.containsKey(i)) {
							 * alarmedZones.put(i, true); activeAlarms.put(i,
							 * 1); } else { activeAlarms.remove(i);
							 * activeAlarms.put(i, 1); }
							 * System.out.println("Motion Detected in the area no: "
							 * + i + " Located at points: (" + sa[i].x + ", " +
							 * sa[i].y+ ") -" + " (" + (sa[i].x +sa[i].width) +
							 * ", " + (sa[i].y+sa[i].height) + ")"); } }
							 */
						}
					}
					contour = contour.h_next();
				}
			}

		}
		grabber.stop();
		// canvasFrame.dispose();

	}
}
