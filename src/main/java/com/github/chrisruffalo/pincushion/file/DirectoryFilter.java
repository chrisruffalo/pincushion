package com.github.chrisruffalo.pincushion.file;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		if(pathname.exists() && pathname.isDirectory()) {
			return true;
		}
		
		return false;
	}

}
