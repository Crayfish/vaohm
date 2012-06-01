import java.awt.Color;
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
	private String title;

	public ImgPanel(String title) {
		super();
		this.title = title;
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

	/***
	 * Draw the image and write the title on it
	 */
	public void paint(Graphics g) {
		if (buffImg != null) {
			g.drawImage(buffImg, 0, 0, this);
			g.setColor(Color.white);
			g.drawString(title, 10, 15);
		}
	}

}
