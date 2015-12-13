package co.davidbuschman.fireflies.tracking;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Slicer {

	private int x;

	private List<Rectangle> slices = new ArrayList<Rectangle>();

	private int startY = 0;
	private int endY = 0;

	public Slicer(int x) {
		this.x = x;
	}

	public void addY(int y) {
		if (startY == 0 && endY == 0) {
			startY = y;
			endY = y;
		} else {
			if (endY + 1 == y) {
				endY = y;
			} else {
				pushSlice();
				this.addY(y); // recurse myself again
			}

		}

	}

	private void pushSlice() {
		if (startY != 0 && endY != 0) {
			slices.add(new Rectangle(x, startY, 1, endY - startY));
			startY = 0;
			endY = 0;
		}
	}

	public boolean hasRectangles() {
		pushSlice();
		return !this.slices.isEmpty();
	}

	public List<Rectangle> getRectangles() {
		pushSlice();
		return this.slices;
	}
}
