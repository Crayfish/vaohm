import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImgPanel extends JPanel {

	private BufferedImage buffImg = null;

	public ImgPanel() {
		super();

	}

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
