import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.media.Buffer;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;
import javax.media.util.BufferToImage;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Java Media Framework (JMF with FFMpeg plugin) plays the given video file,
 * grabs the fames ,triggered by a timer, and hands it to the ImageProcessor.
 * 
 * 
 * 
 * @author Márk Ormos, Thomas Mayr
 * @since 28.05.2012
 * 
 */
public class Videoplayer extends JPanel implements ActionListener {

	/** datasource for video */
	private DataSource ds;

	/** the video player, plays the video */
	private Player p = null;

	/** framegrabber grabs the frames of the video, triggered by the timer */
	private FrameGrabbingControl frameGrabber;

	/** temp image for the frame grabber */
	private BufferedImage buffImg = null;

	/** Imagepanel to draw the processed image on */
	private ImgPanel ip = null;

	/** Imagepanel for blob view */
	private ImgPanel ipBlobs = null;

	/** Image Panel for the collide view */
	private ImgPanel ipCollide = null;

	/** Imageprocessor processes the frames */
	private ImageProcessor imgProcessor;

	/** List of images returning from the Image processor */
	private List<BufferedImage> images;

	/***
	 * Default Constructor
	 * 
	 * @param imagePanel
	 *            for processed image output
	 * @param ipBlobs
	 *            for blob view
	 * @param ipCollide
	 *            for collide view
	 * @param processor
	 *            for image processing
	 */
	public Videoplayer(ImgPanel imagePanel, ImgPanel ipBlobs,
			ImgPanel ipCollide, ImageProcessor processor) {
		ip = imagePanel;
		this.ipBlobs = ipBlobs;
		this.ipCollide = ipCollide;
		imgProcessor = processor;

	}

	/**
	 * Opens and starts the video file
	 * 
	 * @param ml
	 *            standard Media Locator
	 * @return true if media could be opened
	 */
	public boolean open(MediaLocator ml) throws Exception {

		ds = Manager.createDataSource(ml);
		p = Manager.createRealizedPlayer(ds);

		setLayout(new BorderLayout());

		Component vc;
		if ((vc = p.getVisualComponent()) != null) {
			add("Center", vc);
		}

		p.start();

		setVisible(true);

		// Timer triggers the frame grabber
		new Timer(100, this).start();

		frameGrabber = (FrameGrabbingControl) p
				.getControl("javax.media.control.FrameGrabbingControl");

		return true;
	}

	/**
	 * set the images for all views at every repaint();
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buffImg != null) {
			if (images != null) {
				ip.setImg(images.get(2));
				ipBlobs.setImg(images.get(0));
				ipCollide.setImg(images.get(1));
			}

		}
	}

	/***
	 * grabs the image from the video
	 * 
	 * triggered by the Timer
	 */
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

			images = imgProcessor.process(IplImage.createFrom(buffImg), p
					.getMediaTime().getSeconds());

		} catch (Exception ex) {
			System.err.println("FrameGrabbing failed..");
			ex.printStackTrace();
		}

	}

	/***
	 * refresh all imagepanels at every imagegrab
	 */
	public void actionPerformed(ActionEvent e) {
		grab();
		repaint();
		ip.repaint();
		ipBlobs.repaint();
		ipCollide.repaint();
	}

	/**
	 * gets the URL from the video file either by manual input or by searching
	 * 
	 * @param url
	 *            URL of the video file, if null: File chooser dialog appears
	 * @return true if URL is valid
	 */
	@SuppressWarnings("unused")
	public boolean openURL(URL url) throws Exception {
		if (url == null) {
			JFileChooser fc = new JFileChooser();
			int ret = fc.showDialog(null, "Open file");

			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					url = fc.getSelectedFile().toURL();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
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

}