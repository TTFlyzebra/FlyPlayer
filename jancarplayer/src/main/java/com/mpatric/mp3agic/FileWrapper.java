package com.mpatric.mp3agic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileWrapper {

	protected File path;
	protected long length;
	protected long lastModified;

	protected FileWrapper() {
	}

	public FileWrapper(String filename) throws IOException {
		this(new File(filename));
	}

	public FileWrapper(File file) throws IOException {
		if (file == null) throw new NullPointerException();
		this.path = file;
		init();
	}


	private void init() throws IOException {
		if (!path.exists()) throw new FileNotFoundException("File not found " + path);
		if (!path.canRead()) throw new IOException("File not readable");
		length = path.length();
		lastModified = path.lastModified();
	}

	public String getFilename() {
		return path.toString();
	}

	public long getLength() {
		return length;
	}

	public long getLastModified() {
		return lastModified;
	}
}
