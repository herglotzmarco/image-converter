package de.herglotz.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.CubicCurve2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

public class ImageConverter {

	private static final int OFFSET_DEFAULT = 30;
	private static final int THRESHOLD_DEFAULT = 8000;

	private int offset;
	private int threshold;

	public ImageConverter(int offset, int threshold) {
		this.offset = offset;
		this.threshold = threshold;
	}

	public ImageConverter() {
		this.offset = OFFSET_DEFAULT;
		this.threshold = THRESHOLD_DEFAULT;
	}

	public void convert(String inputFile, String outputFile) throws IOException {
		try (FileInputStream input = new FileInputStream(inputFile);
				FileOutputStream output = new FileOutputStream(outputFile)) {
			BufferedImage image = ImageIO.read(input);
			BufferedImage curved = toCurved(image);
			ImageIO.write(curved, "jpg", output);
		}
	}

	private BufferedImage toCurved(BufferedImage image) {
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D graphics = getAndPrepareGraphics(result);

		int lineY = offset / 2;
		while (lineY < image.getHeight()) {
			drawSingleLine(graphics, image.getRaster(), lineY);
			lineY += 3 * offset / 4;
		}
		return result;
	}

	private Graphics2D getAndPrepareGraphics(BufferedImage result) {
		Graphics2D graphics = (Graphics2D) result.getGraphics();
		graphics.setBackground(Color.WHITE);
		graphics.setColor(Color.BLACK);
		graphics.clearRect(0, 0, result.getWidth(), result.getHeight());
		return graphics;
	}

	private void drawSingleLine(Graphics2D graphics, WritableRaster raster, int y) {
		List<Integer> controlPointsX = new ArrayList<>();
		controlPointsX.add(0);
		int sum = 0;
		for (int x = 0; x < raster.getWidth(); x++) {
			int finalX = x;
			sum += IntStream//
					.range(-offset / 2, offset / 2)//
					.map(i -> getInvertedGreyvalue(raster, y, finalX, i))//
					.sum();
			if (sum > threshold) {
				controlPointsX.add(x);
				sum = 0;
			}
			if (controlPointsX.size() == 4) {
				drawCurveAndReset(graphics, y, controlPointsX);
			}
		}

	}

	private void drawCurveAndReset(Graphics2D graphics, int y, List<Integer> controlPointsX) {
		Double curve = new CubicCurve2D.Double();
		curve.setCurve(//
				new Point(controlPointsX.get(0), y), //
				new Point(controlPointsX.get(1), y + offset), //
				new Point(controlPointsX.get(2), y - offset), //
				new Point(controlPointsX.get(3), y));
		graphics.draw(curve);
		controlPointsX.remove(0);
		controlPointsX.remove(0);
		controlPointsX.remove(0);
	}

	private int getInvertedGreyvalue(WritableRaster raster, int y, int x, int offsetY) {
		try {
			return 255 - greyscalePixel(raster.getPixel(x, y + offsetY, (int[]) null));
		} catch (ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	private int greyscalePixel(int[] pixel) {
		return Arrays.stream(pixel).sum() / pixel.length;
	}

}
