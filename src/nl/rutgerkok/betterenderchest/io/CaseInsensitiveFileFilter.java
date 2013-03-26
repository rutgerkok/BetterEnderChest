package nl.rutgerkok.betterenderchest.io;

import java.io.File;
import java.io.FilenameFilter;

public class CaseInsensitiveFileFilter implements FilenameFilter {
	private final String fileName;

	public CaseInsensitiveFileFilter(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean accept(File dir, String fileName) {
		return this.fileName.equalsIgnoreCase(fileName);
	}
}