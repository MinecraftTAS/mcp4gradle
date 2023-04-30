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

	private static final String CONFIG = """
		deob = %DEOB%
		startindex = 0
		input = null
		output = null
		log = null
		script = null
		protectedpackage = paulscode
		protectedpackage = com/jcraft
		protectedpackage = isom
		protectedpackage = ibxm
		protectedpackage = de/matthiasmann/twl
		protectedpackage = org/xmlpull
		protectedpackage = javax/xml
		""";
	
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
		var file = Utils.tempFile();
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
