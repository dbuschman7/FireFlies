package co.davidbuschman.fireflies.adapters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;

/**
 * Create a tool which adds a grid to a video image.
 */
public class GridDrawingTool extends MediaToolAdapter {
	/** {@inheritDoc} */

	int frameCount = 0;

	@Override
	public void onAudioSamples(IAudioSamplesEvent event) {
		super.onAudioSamples(event);
	}

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		++frameCount;
		convert(event.getImage());
		super.onVideoPicture(event);
	}

	public static BufferedImage convert(BufferedImage image) {
		// get the graphics for the image
		Graphics2D g = image.createGraphics();

		// draw reference marks on the image
		int height = image.getHeight();
		int width = image.getWidth();
		int xSize = width / 10;
		int ySize = height / 10;
		int offset = 10;

		g.setColor(Color.GRAY);
		for (int xPoint = xSize; xPoint < width; xPoint += xSize) {
			for (int yPoint = ySize; yPoint < height; yPoint += ySize) {
				// System.out.println(String.format("Grid point(x,y) : %d, %d",
				// xPoint, yPoint));
				// g.translate(xPoint, yPoint);
				g.drawLine(xPoint, yPoint - offset, xPoint, yPoint + offset);
				g.drawLine(xPoint - offset, yPoint, xPoint + offset, yPoint);
			}
		}
		return image;
	}
}