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

	private DataSource ds;
	private Player p = null;
	private FrameGrabbingControl frameGrabber;
	private BufferedImage buffImg = null;
	/** Imagepanel to draw the processed image on */
	private ImgPanel ip = null;
	private ImageProcessor imgProcessor;
	private BufferedImage procImage = null;

	public Videoplayer(ImgPanel imagePanel, ImageProcessor processor) {
		ip = imagePanel;
		imgProcessor = processor;

	}

	/**
	 * Opens and starts the video file
	 * 
	 * @param ml
	 * @return
	 */
	public boolean open(MediaLocator ml) throws Exception {

		ds = Manager.createDataSource(ml);
		p = Manager.createRealizedPlayer(ds);

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

		// Timer triggers the frame grabber
		new Timer(100, this).start();
		frameGrabber = (FrameGrabbingControl) p
				.getControl("javax.media.control.FrameGrabbingControl");

		return true;
	}

	/**
	 * 
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buffImg != null) {
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

			procImage = imgProcessor.process(IplImage.createFrom(buffImg), p
					.getMediaTime().getSeconds());

		} catch (Exception ex) {
			System.err.println("FrameGrabbing failed..");
			// ex.printStackTrace();
		}

	}

	public void actionPerformed(ActionEvent e) {
		grab();
		repaint();
		ip.repaint();
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
					// TODO Auto-generated catch block
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