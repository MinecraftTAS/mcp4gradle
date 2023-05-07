package com.minecrafttas.mcp4gradle.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import com.minecrafttas.mcp4gradle.Utils;

import retrogradle.NameProvider;
import retrogradle.obf.RetroGuardImpl;

/**
 * RetroGuard wrapper class
 */
public class RetroGuard {

	private static final String CONFIG = "" +
		"deob = %DEOB%\n" +
		"startindex = 0\n" +
		"input = null\n" +
		"output = null\n" +
		"log = null\n" +
		"script = null\n" +
		"protectedpackage = paulscode\n" +
		"protectedpackage = com/jcraft\n" +
		"protectedpackage = isom\n" +
		"protectedpackage = ibxm\n" +
		"protectedpackage = de/matthiasmann/twl\n" +
		"protectedpackage = org/xmlpull\n" +
		"protectedpackage = javax/xml\n";
	
	private File in, out, cfg;

	/**
	 * Initializes the Retro Guard
	 * @param in Input file
	 * @param out Output file
	 */
	public RetroGuard(File in, File out, File cfg) {
		this.in = in;
		this.out = out;
		this.cfg = cfg;
	}
	
	/**
	 * Initialize the obfuscator
	 * @param srg Mappings
	 * @throws Exception Filesystem Exception
	 */
	public void init(File srg) throws Exception {
		File file = Utils.tempFile();
		Files.write(file.toPath(), CONFIG.replace("%DEOB%", srg.getAbsolutePath()).getBytes(), StandardOpenOption.CREATE);
		NameProvider.parseCommandLine(new String[] { "-searge", file.getAbsolutePath() });
	}
	
	/**
	 * Runs the obfuscator
	 * @throws Exception
	 */
	public void run() throws Exception {
		RetroGuardImpl.obfuscate(this.in, this.out, this.cfg, Utils.tempFile());
	}
	
}
