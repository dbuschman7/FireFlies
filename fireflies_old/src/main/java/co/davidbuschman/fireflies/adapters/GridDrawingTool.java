package co.davidbuschman.fireflies.adapters;

import java.awt.Color;
import java.awt.Graphics2D;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

/**
 * Create a tool which adds a grid to a video image.
 */
public class GridDrawingTool extends MediaToolAdapter {
	/** {@inheritDoc} */

	int frameCount = 0;

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		++frameCount;

		// get the graphics for the image
		Graphics2D g = event.getImage().createGraphics();

		// draw reference marks on the image
		int height = event.getImage().getHeight();
		int width = event.getImage().getWidth();
		int size = 20;
		int offset = 2;

		g.setColor(Color.DARK_GRAY);
		for (int xPoint = size; xPoint < width; xPoint += size) {
			for (int yPoint = size; yPoint < height; yPoint += size) {
				// System.out.println(String.format("Grid point(x,y) : %d, %d", xPoint, yPoint));
				// g.translate(xPoint, yPoint);
				g.drawLine(xPoint, yPoint - offset, xPoint, yPoint + offset);
				g.drawLine(xPoint - offset, yPoint, xPoint + offset, yPoint);
			}
		}

		// call parent which will pass the video onto next tool in chain
		super.onVideoPicture(event);
	}
}