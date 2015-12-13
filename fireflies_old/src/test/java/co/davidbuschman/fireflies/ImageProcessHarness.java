package co.davidbuschman.fireflies;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import co.davidbuschman.fireflies.tracking.Slicer;
import co.davidbuschman.fireflies.tracking.SpotTracking;

public class ImageProcessHarness {

	@Test
	public void test() throws IOException {
		// processFiles("/Users/dave/Desktop/AVCHD/Converted/00004/00004-0758.png");
		processFiles("/Users/dave/Desktop/AVCHD/Converted/00004/00004-0752.png");
	}

	private void processFiles(String filename) throws IOException {
		String outputFilename = filename.replace(".png", "-out.png");

		BufferedImage img = ImageIO.read(new File(filename));

		int height = img.getHeight();
		int width = img.getWidth();

		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		SpotTracking tracking = new SpotTracking(height, width);

		for (int x = 0; x < width; ++x) {

			Slicer slicer = new Slicer(1);
			for (int y = 0; y < height; ++y) {
				int rgb = img.getRGB(x, y);
				int a = (0xff000000 & rgb) >>> 24;
				int r = (0x00ff0000 & rgb) >> 16;
				int g = (0x0000ff00 & rgb) >> 8;
				int b = (0x000000ff & rgb);

				double gray = (0.2126 * r) + (0.7152 * g) + (0.0722 * g);

				if (gray == 0) {
					out.setRGB(x, y, 0xffffffff);
					continue;
				}

				slicer.addY(y);
				out.setRGB(x, y, 0x00000000);

				//
				System.out.println(//
						String.format("X:%4d/Y:%3d R:%3d G:%3d B:%3d Gray:%3d",//
								x, y, r, g, b, (int) gray));

			}

			if (slicer.hasRectangles()) {
				tracking.addSlices(slicer.getRectangles());
			}
		}

		//
		ImageIO.write(out, "PNG", new File(outputFilename));
	}

}
