import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

public class Videoplayer extends JPanel implements ActionListener {

	private DataSource ds;
	private Player p = null;
	private FrameGrabbingControl frameGrabber;
	private BufferedImage buffImg = null;
	private ImgPanel ip = null;

	public Videoplayer(ImgPanel imagePanel) {
		ip = imagePanel;
	}

	public boolean open(MediaLocator ml) {

		new Timer(800, this).start();
		try {
			ds = Manager.createDataSource(ml);
			p = Manager.createRealizedPlayer(ds);
		} catch (NoDataSourceException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoPlayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotRealizeException e) {
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

		frameGrabber = (FrameGrabbingControl) p
				.getControl("javax.media.control.FrameGrabbingControl");

		return true;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (buffImg != null) {
			g.drawImage(buffImg, 0, 0, this);
			ip.setImg(buffImg);
		}
	}

	private void grab() {

		try {
			Buffer buf = frameGrabber.grabFrame();

			// Convert frame to an buffered image so it can be processed and
			// saved
			Image img = (new BufferToImage((VideoFormat) buf.getFormat())
					.createImage(buf));
			buffImg = new BufferedImage(img.getWidth(this),
					img.getHeight(this), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = buffImg.createGraphics();
			g.drawImage(img, null, null);
			g.setColor(Color.red);
			g.setFont(new Font("Tahoma", Font.PLAIN, 12)
					.deriveFont(AffineTransform.getRotateInstance(1.57)));
			g.drawString((new Date()).toString(), 5, 5);
		} catch (Exception ex) {
			System.err.println("FrameGrabbing failed..");
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

}