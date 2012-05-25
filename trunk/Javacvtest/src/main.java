import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.CvSize2D32f;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;


public class main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

		  OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
	        grabber.start();
	        
	        IplImage frame = grabber.grab();
	        IplImage image = null;
	        IplImage prevImage = null;
	        IplImage diff = null;

	        CanvasFrame canvasFrame = new CanvasFrame("Some Title");
	        canvasFrame.setCanvasSize(frame.width(), frame.height());

	        CvMemStorage storage = CvMemStorage.create();

	        while (canvasFrame.isVisible() && (frame = grabber.grab()) != null) {
	            cvSmooth(frame, frame, CV_GAUSSIAN, 9, 9, 2, 2);
	            if (image == null) {
	                image = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
	                cvCvtColor(frame, image, CV_RGB2GRAY);
	            } else {
	                prevImage = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
	                prevImage = image;
	                image = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
	                cvCvtColor(frame, image, CV_RGB2GRAY);
	            }
	        //grabber.stop();
	            if (diff == null) {
	                diff = IplImage.create(frame.width(), frame.height(), IPL_DEPTH_8U, 1);
	            }
	            
	            if (prevImage != null) {
	                // perform ABS difference
	                cvAbsDiff(image, prevImage, diff);
	                // do some threshold for wipe away useless details
	                cvThreshold(diff, diff, 64, 255, CV_THRESH_BINARY);

	                canvasFrame.showImage(diff);

	      // grabber.stop();
		
	             // recognize contours
	                CvSeq contour = new CvSeq(null);
	                cvFindContours(diff, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);

	                while (contour != null && !contour.isNull()) {
	                    if (contour.elem_size() > 0) {
	                        CvBox2D box = cvMinAreaRect2(contour, storage);
	                        // test intersection
	                        if (box != null) {
	                            CvPoint2D32f center = box.center();
	                            CvSize2D32f size = box.size();
	/*                            for (int i = 0; i < sa.length; i++) {
	                                if ((Math.abs(center.x - (sa[i].offsetX + sa[i].width / 2))) < ((size.width / 2) + (sa[i].width / 2)) &&
	                                    (Math.abs(center.y - (sa[i].offsetY + sa[i].height / 2))) < ((size.height / 2) + (sa[i].height / 2))) {

	                                    if (!alarmedZones.containsKey(i)) {
	                                        alarmedZones.put(i, true);
	                                        activeAlarms.put(i, 1);
	                                    } else {
	                                        activeAlarms.remove(i);
	                                        activeAlarms.put(i, 1);
	                                    }
	                                    System.out.println("Motion Detected in the area no: " + i +
	                                            " Located at points: (" + sa[i].x + ", " + sa[i].y+ ") -"
	                                            + " (" + (sa[i].x +sa[i].width) + ", "
	                                            + (sa[i].y+sa[i].height) + ")");
	                                }
	                            }
	*/
	                        }
	                    }
	                    contour = contour.h_next();
	                }
	            }
	        }
	        grabber.stop();
	        canvasFrame.dispose();
	    }
	}

	

