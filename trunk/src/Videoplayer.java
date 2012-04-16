import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.CannotRealizeException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.protocol.DataSource;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class Videoplayer extends JPanel {

	private DataSource ds;
	private Player p = null;

	public Videoplayer() {
	}

	public boolean open(MediaLocator ml) {

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

		// Start the processor.
		p.start();// by thomas
		repaint();
		setVisible(true);

		return true;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public void actionPerformed(ActionEvent e) {

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