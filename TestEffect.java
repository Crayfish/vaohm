//by andre und thomas
//bzw code aus link der folien von sun!

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.CannotRealizeException;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSourceException;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.protocol.DataSource;
import javax.swing.JFileChooser;

/**
 * Sample program to test the Vogelschatten Effect.
 * 
 * (based on Rotation Effect by Sun Microsystems)
 * 
 * 
 */
public class TestEffect extends Frame implements ControllerListener {

	private static final long serialVersionUID = 1L;
	Processor processor;
	Object waitSync = new Object();
	boolean stateTransitionOK = true;

	public TestEffect() {
		super("Test Vogelschatten Effect");
	}

	/**
	 * Given a media locator, create a processor and use that processor as a
	 * player to playback the media.
	 * 
	 * During the processor's Configured state, the Vogelschatten Effect is
	 * inserted into the video track.
	 * 
	 */
	public boolean open(MediaLocator ml) {

		DataSource ds;
		Player p = null;
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

		// try {
		// Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, new Boolean(true));
		// processor = Manager.createProcessor(sourceURL);
		// } catch (Exception e) {
		// System.err
		// .println("Failed to create a processor from the given url ");
		// e.printStackTrace();
		// return false;
		// }
		//
		// processor.addControllerListener(this);
		//
		// // Put the Processor into configured state.
		// processor.configure();
		//
		// if (!waitForState(processor.Configured)) {
		// System.err.println("Failed to configure the processor.");
		// return false;
		// }
		//
		// processor.setContentDescriptor(null);
		//
		// // Obtain the track controls.
		// TrackControl tc[] = processor.getTrackControls();
		//
		// if (tc == null) {
		// System.err
		// .println("Failed to obtain track controls from the processor.");
		// return false;
		// }
		//
		// // Search for the track control for the video track.
		// TrackControl videoTrack = null;
		//
		// for (int i = 0; i < tc.length; i++) {
		// if (tc[i].getFormat() instanceof VideoFormat) {
		// videoTrack = tc[i];
		// break;
		// }
		// }
		//
		// if (videoTrack == null) {
		// System.err
		// .println("The input media does not contain a video track.");
		// return false;
		// }
		//
		// System.err.println("Video format: " + videoTrack.getFormat());

		// Instantiate and set the frame access codec to the data flow path.
		// try {
		// Codec codec[] = { new VogelschattenEffect() };
		// videoTrack.setCodecChain(codec);
		// } catch (UnsupportedPlugInException e) {
		// System.err.println("The processor does not support effects.");
		// }

		// Realize the processor.
		// processor.prefetch();
		// if (!waitForState(processor.Prefetched)) {
		// System.err.println("Failed to realize the processor.");
		// return false;
		// }

		// Display the visual & control component if there's one.

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

		setVisible(true);

		// addWindowListener(new WindowAdapter() {
		// public void windowClosing(WindowEvent we) {
		// p.close();
		// System.exit(0);
		// }
		// });

		return true;
	}

	public void addNotify() {
		super.addNotify();
		pack();
	}

	/**
	 * Block until the processor has transitioned to the given state. Return
	 * false if the transition failed.
	 */
	boolean waitForState(int state) { // by andre
		synchronized (waitSync) {
			try {
				while (processor.getState() != state && stateTransitionOK)
					waitSync.wait();
			} catch (Exception e) {
			}
		}
		return stateTransitionOK;
	}

	/**
	 * Controller Listener.
	 */
	public void controllerUpdate(ControllerEvent evt) {

		if (evt instanceof ConfigureCompleteEvent
				|| evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof EndOfMediaEvent) {
			processor.close();
			System.exit(0);
		}
	}

	/**
	 * Main program by thomas & andre
	 */
	public static void main(String[] args) {
		//
		// if (args.length == 0) {
		// prUsage();
		// System.exit(0);
		// }

		// String url = args[0];
		//
		// if (url.indexOf(":") < 0) {
		// prUsage();
		// System.exit(0);
		// }

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

		TestEffect testEffect = new TestEffect();

		if (!testEffect.open(mediaLocator)) {
			System.exit(0);
		}

	}

	static void prUsage() {
		System.err.println("Usage: java TestEffect <url>");
	}
}