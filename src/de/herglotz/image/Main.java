package de.herglotz.image;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		ImageConverter converter = new ImageConverter();
		converter.convert(args[0], args[1]);
	}

}
