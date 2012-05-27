import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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

public class Videoplayer extends JPanel implements ActionListener {

	private DataSource ds;
	private Player p = null;
	private FrameGrabbingControl frameGrabber;
	private BufferedImage buffImg = null;
	private ImgPanel ip = null;
	private ImageProcessor imgProcessor = new ImageProcessor();
	private BufferedImage procImage = null;

	public Videoplayer(ImgPanel imagePanel) {
		ip = imagePanel;

	}

	public boolean open(MediaLocator ml) {

		try {
			ds = Manager.createDataSource(ml);
			p = Manager.createRealizedPlayer(ds);
		} catch (NoDataSourceException e) {
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

		new Timer(100, this).start();
		frameGrabber = (FrameGrabbingControl) p
				.getControl("javax.media.control.FrameGrabbingControl");

		return true;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buffImg != null) {
			// g.drawImage(buffImg, 0, 0, this);
			ip.setImg(procImage);
		}
	}

	private void grab() {

		try {
			Buffer buf = frameGrabber.grabFrame();
			Image img = (new BufferToImage((VideoFormat) buf.getFormat())
					.createImage(buf));
			buffImg = new BufferedImage(img.getWidth(this),
					img.getHeight(this), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = buffImg.createGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();

			procImage = imgProcessor.process(IplImage.createFrom(buffImg));

			// // Convert frame to an buffered image so it can be processed and
			// // saved
			// Image img = (new BufferToImage((VideoFormat)
			// buf.getFormat()).createImage(buf));
			// BufferedImage buffImg1 = new
			// BufferedImage(img.getWidth(this),img.getHeight(this),
			// BufferedImage.TYPE_INT_RGB);
			// Graphics2D g = buffImg1.createGraphics();
			// g.drawImage(img, 0,0, null);
			// g.dispose();
			// // BufferedImageOp grayscale = new
			// ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
			// // buffImg1 = grayscale.filter(buffImg1, null);
			// BufferedImageOp thresholdOp =new LookupOp(new ShortLookupTable(0,
			// threshold), null);
			// buffImg = thresholdOp.filter(buffImg1, null);
			//
			// // image = IplImage.createFrom(buffImg1);
			// // IplImage imgThreshold = cvCreateImage(cvGetSize(image), 8, 1);
			// // cvInRangeS(image, cvScalar(100, 100, 100, 0), cvScalar(180,
			// 255, 255, 0), imgThreshold);
			// // cvReleaseImage(image);
			// // cvSmooth(imgThreshold, imgThreshold, CV_MEDIAN, 13);
			// // buffImg = imgThreshold.getBufferedImage();
			//
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
	public boolean openURL(URL url) {
		if (url == null) {
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
		}

		System.out.println(url);

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

	public void setThreshold(int value) {

		imgProcessor.setThreshold(value);

	}

}