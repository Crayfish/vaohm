
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.util.BufferToImage;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

public class Videoplayer extends JPanel implements ActionListener {

	private DataSource ds;
	private Player p = null;
	private FrameGrabbingControl frameGrabber;
	private BufferedImage buffImg = null;
	private ImgPanel ip = null;
	private short[] threshold = new short[256];
	private IplImage image;

	public Videoplayer(ImgPanel imagePanel) {
		ip = imagePanel;
		for (int i = 0; i < 256; i++)
			threshold[i] = (i < 200) ? (short)0 : (short)255;
	}

	public boolean open(MediaLocator ml) {

		
		try {
			ds = Manager.createDataSource(ml);
			p = Manager.createRealizedPlayer(ds);
		} catch (NoDataSourceException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotRealizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		setLayout(new BorderLayout());

		Component cc;
		Component vc;
		if ((vc = p.getVisualComponent()) != null) {
			add("Center", vc);
		}

		if ((cc = p.getControlPanelComponent()) != null) {
			add("South", cc);
		}

		p.start();

		setVisible(true);

		new Timer(400, this).start();
		frameGrabber = (FrameGrabbingControl) p
				.getControl("javax.media.control.FrameGrabbingControl");

		return true;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buffImg != null) {
//			g.drawImage(buffImg, 0, 0, this);
			ip.setImg(buffImg);
		}
	}

	private void grab() {

		try {
			Buffer buf = frameGrabber.grabFrame();

			// Convert frame to an buffered image so it can be processed and
			// saved
			Image img = (new BufferToImage((VideoFormat) buf.getFormat()).createImage(buf));
			BufferedImage buffImg1 = new BufferedImage(img.getWidth(this),img.getHeight(this), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = buffImg1.createGraphics();
			g.drawImage(img, 0,0, null);
			g.dispose();
//			BufferedImageOp grayscale = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
//			buffImg1 = grayscale.filter(buffImg1, null);
			BufferedImageOp thresholdOp =new LookupOp(new ShortLookupTable(0, threshold), null);
			buffImg = thresholdOp.filter(buffImg1, null);
			
//			image = IplImage.createFrom(buffImg1);
//			IplImage imgThreshold = cvCreateImage(cvGetSize(image), 8, 1);
//	        cvInRangeS(image, cvScalar(100, 100, 100, 0), cvScalar(180, 255, 255, 0), imgThreshold);
//	        cvReleaseImage(image);
//	        cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
//	        buffImg = imgThreshold.getBufferedImage();
			
		} catch (Exception ex) {
			System.err.println("FrameGrabbing failed..");
			ex.printStackTrace();
		}

	}

	public void actionPerformed(ActionEvent e) {
		grab();
		repaint();
		ip.repaint();
	}

	@SuppressWarnings("unused")
	public boolean openURL() {
		URL url = null;

		JFileChooser fc = new JFileChooser();
		int ret = fc.showDialog(null, "Open file");

		if (ret == JFileChooser.APPROVE_OPTION) {
			try {
				url = fc.getSelectedFile().toURL();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		MediaLocator mediaLocator;

		if ((mediaLocator = new MediaLocator(url)) == null) {
			System.err.println("Cannot build media locator from: " + url);
			System.exit(0);
		}

		if (!open(mediaLocator)) {
			return false;
		}
		return true;
	}
	
	public void setThreshold(int value){
		
		for (int i = 0; i < 256; i++)
			threshold[i] = (i < value) ? (short)0 : (short)255;
	}

}