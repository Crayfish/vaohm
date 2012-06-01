import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Project work for the course "Visual analysis of Human Motion" at the Vienna
 * University of Technology. Aim of this project to to detect motion in a given
 * (squash)video file and track the players positions.
 * 
 * We are using the JMF Framework for video playing and frame grabbing, and
 * OpenCV (JavaCv wrapper) for image processing.
 * 
 * @author Márk Ormos, Thomas Mayr
 * @since 28.05.2012
 * 
 */
public class Main {

	/**
	 * Start the GUI, set the look if windows
	 * 
	 * @param args
	 *            are ignored
	 */
	public static void main(String[] args) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.contains("Windows")) {
				UIManager
						.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} else
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());

		} catch (UnsupportedLookAndFeelException e) {
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		new GUI1();

	}

}
