package co.davidbuschman.fireflies.writers;

import java.io.File;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;

public class FileImageWriter extends MediaToolAdapter {

	int frameCount = 0;

	private String baseFilename;

	private String fileType;

	public FileImageWriter(String baseFilename, String fileType) {
		this.baseFilename = baseFilename;
		this.fileType = fileType;
	}

	@Override
	public void onVideoPicture(IVideoPictureEvent event) {

		++frameCount;

		try {
			File file = new File(String.format("%s-%04d.%s", this.baseFilename,
					frameCount, this.fileType));
			file.mkdirs();
			ImageIO.write(event.getImage(), this.fileType, file);

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
}
