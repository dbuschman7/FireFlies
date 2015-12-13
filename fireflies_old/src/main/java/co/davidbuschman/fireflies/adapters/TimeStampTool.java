package co.davidbuschman.fireflies.adapters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

/**
 * Create a tool which adds a time stamp to a video image.
 */
public class TimeStampTool extends MediaToolAdapter {
	/** {@inheritDoc} */

	int frameCount = 0;

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {
		++frameCount;

		// get the graphics for the image
		Graphics2D g = event.getImage().createGraphics();

		// establish the timestamp and how much space it will take

		String timeStampStr = String.format("Frame :%04d : %s", frameCount,
				event.getPicture().getFormattedTimeStamp());
		Rectangle2D bounds = g.getFont().getStringBounds(timeStampStr,
				g.getFontRenderContext());

		// compute the amount to inset the time stamp and translate the
		// image to that position

		double inset = bounds.getHeight() / 2;
		g.translate(inset, event.getImage().getHeight() - inset);

		// draw a white background and black timestamp text
		g.setColor(Color.WHITE);
		g.fill(bounds);
		g.setColor(Color.BLACK);
		g.drawString(timeStampStr, 0, 0);

		// call parent which will pass the video onto next tool in chain
		super.onVideoPicture(event);
	}
}
