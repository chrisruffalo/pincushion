package com.github.chrisruffalo.pincushion.file;

import java.io.File;
import java.io.FileFilter;

/**
 * Accepts all files (through the filter) that both exist and
 * that are a directory.
 *
 */
public class DirectoryFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		return pathname.exists() && pathname.isDirectory();
	}

}
