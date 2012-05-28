import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Custom Panel to display the processed images
 * 
 * @author Márk Ormos, Thomas Mayr
 * @since 28.05.2012
 * 
 */
public class ImgPanel extends JPanel {

	private BufferedImage buffImg = null;

	public ImgPanel() {
		super();
	}

	/**
	 * Set the current image
	 * 
	 * @param buff
	 *            image to display
	 */
	public void setImg(BufferedImage buff) {
		buffImg = buff;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		if (buffImg != null) {
			g.drawImage(buffImg, 0, 0, this);
		}
	}

}
