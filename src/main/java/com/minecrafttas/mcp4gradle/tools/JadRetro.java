package com.minecrafttas.mcp4gradle.tools;

import java.io.File;

import jadretro.Main;
import net.lingala.zip4j.ZipFile;

/**
 * JadRetro wrapper class
 */
public class JadRetro {

	private File in, out;

	/**
	 * Initializes JadRetro
	 * @param in Input file
	 * @param out Output file
	 */
	public JadRetro(File in, File out) {
		this.in = in;
		this.out = out;
	}
	
	/**
	 * Initialize the processor
	 * @throws Exception Filesystem Exception
	 */
	public void init() throws Exception {
		this.out.mkdirs();

		ZipFile f = new ZipFile(this.in);
		f.extractAll(this.out.getAbsolutePath()); 
		f.close();
	}
	
	/**
	 * Runs the processor
	 * @throws Exception
	 */
	public void run() throws Exception {
		Main.main(new String[] { new File(this.out, "net/minecraft/client").getAbsolutePath()});
        Main.main(new String[] { new File(this.out, "net/minecraft/src").getAbsolutePath()});
	}
	
}
