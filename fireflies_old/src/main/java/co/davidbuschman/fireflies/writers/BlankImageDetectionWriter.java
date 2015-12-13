package co.davidbuschman.fireflies.writers;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class BlankImageDetectionWriter extends MediaToolAdapter {

	int frameCount = 0;

	private PrintWriter writer;

	public BlankImageDetectionWriter(PrintWriter logFile) throws Exception {
		writer = logFile;
	}

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {

		++frameCount;
		boolean nonBlackFrame = false;
		// System.out.println(String.format("Frame : %04d", frameCount));
		BufferedImage javaData = event.getJavaData();

		int height = event.getImage().getHeight();
		int width = event.getImage().getHeight();

		for (int xPoint = 1; xPoint < width; xPoint += 1) {
			for (int yPoint = 1; yPoint < height; yPoint += 1) {

				int rgb = javaData.getRGB(xPoint, yPoint);
				Color c = new Color(rgb);
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();
				if ((red > 10) || (green > 10) || (blue > 10)) {
					nonBlackFrame = true;
					break;
				}
				// System.out.println(//
				// String.format("X= %4d Y = %3d Red = %3d Green = %3d Blue = %3d ", //
				// xPoint, yPoint, red, green, blue)//
				// );
			}
		}

		if (nonBlackFrame) {
			writer.println(String.format("Flash : %5d", frameCount));
		}

		super.onVideoPicture(event);
	}
}
