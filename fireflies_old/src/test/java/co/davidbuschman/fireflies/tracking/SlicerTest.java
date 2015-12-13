package co.davidbuschman.fireflies.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.List;

import org.junit.Test;

public class SlicerTest {

	@Test
	public void testEmpty() {
		Slicer slicer = new Slicer(1);

		assertFalse(slicer.hasRectangles());
		assertTrue(slicer.getRectangles().isEmpty());
	}

	@Test
	public void testSingleSlice() {
		Slicer slicer = new Slicer(1);

		slicer.addY(1);
		slicer.addY(2);

		assertTrue(slicer.hasRectangles());
		List<Rectangle> rectangles = slicer.getRectangles();
		assertFalse(rectangles.isEmpty());
		assertEquals(1, rectangles.size());

		Rectangle rectangle = rectangles.get(0);
		assertEquals(1, rectangle.x);
		assertEquals(1, rectangle.width);

		assertEquals(1.0, rectangle.getMinY(), 0.01);
		assertEquals(2.0, rectangle.getMaxY(), 0.01);
	}

	@Test
	public void testMultipleSlices() {
		Slicer slicer = new Slicer(1);

		slicer.addY(1);
		slicer.addY(2);
		slicer.addY(4);
		slicer.addY(5);
		slicer.addY(6);

		assertTrue(slicer.hasRectangles());
		List<Rectangle> rectangles = slicer.getRectangles();
		assertFalse(rectangles.isEmpty());
		assertEquals(2, rectangles.size());

		Rectangle rectangle = rectangles.get(0);
		assertEquals(1, rectangle.x);
		assertEquals(1, rectangle.width);

		assertEquals(1.0, rectangle.getMinY(), 0.01);
		assertEquals(2.0, rectangle.getMaxY(), 0.01);

		rectangle = rectangles.get(1);
		assertEquals(1, rectangle.x);
		assertEquals(1, rectangle.width);

		assertEquals(4.0, rectangle.getMinY(), 0.01);
		assertEquals(6.0, rectangle.getMaxY(), 0.01);

	}
}
