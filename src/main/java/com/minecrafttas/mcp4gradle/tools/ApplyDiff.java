package com.minecrafttas.mcp4gradle.tools;

import java.io.File;

import com.minecrafttas.mcp4gradle.Utils;

/**
 * Apply diff wrapper class
 */
public class ApplyDiff {

	private static final String APPLYDIFF_EXE = "https://data.mgnet.work/mcp4gradle/tools/applydiff.exe";
	
	private File dir;
	private ProcessBuilder p;
	
	/**
	 * Initializes Apply Diff
	 * @param dir Directory
	 */
	public ApplyDiff(File dir) {
		this.dir = dir;
	}
	
	/**
	 * Initialize the patcher
	 * @param diff Diff file
	 * @throws Exception Filesystem Exception
	 */
	public void init(File diff) throws Exception {
		File applydiff = Utils.tempFile(APPLYDIFF_EXE);
		
		this.p = new ProcessBuilder(applydiff.getAbsolutePath(), "--binary", "-p1", "-u", "-i", diff.getAbsolutePath(), "-d", "src/minecraft");
		this.p.directory(this.dir);
		this.p.redirectErrorStream(true);
	}
	
	/**
	 * Runs the patcher
	 * @throws Exception
	 */
	public void run() throws Exception {
		Process pr = this.p.start(); 
		pr.getOutputStream().close();
		Utils.deleteLarge(pr.getInputStream());
		pr.waitFor();
	}

}
