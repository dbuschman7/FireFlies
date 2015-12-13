package co.davidbuschman.fireflies;

import org.junit.Test;

public class ProcessFilesTest {

	@Test
	public void session1() throws Exception {
		for (int i = 2; i < 17; ++i) {
			try {
				System.out.println("Processing file : " + i);
				new FrameListener(String.format("/Users/dave/Desktop/Fireflies/Session3/%05d.mts", i));
			} catch (Exception e) {
				System.out.println("Error thrown " + e.getMessage());
			}
		}
		System.out.println("End Processing");
	}
}
