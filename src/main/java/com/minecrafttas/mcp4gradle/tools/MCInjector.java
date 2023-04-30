package com.minecrafttas.mcp4gradle.tools;

import java.io.File;

import com.minecrafttas.mcp4gradle.Utils;

import mcinjector.MCInjectorImpl;

/**
 * MCInjector wrapper class
 */
public class MCInjector {

	private File in, out, exc;

	/**
	 * Initializes the MCInjector
	 * @param in Input file
	 * @param out Output file
	 */
	public MCInjector(File in, File out) {
		this.in = in;
		this.out = out;
	}
	
	/**
	 * Initialize the processor
	 * @param exc Mappings
	 */
	public void init(File exc) {
		this.exc = exc;
	}
	
	/**
	 * Runs the injector
	 * @throws Exception
	 */
	public void run() throws Exception {
		MCInjectorImpl.process(this.in.getAbsolutePath(), this.out.getAbsolutePath(), this.exc.getAbsolutePath(), Utils.tempFile().getAbsolutePath(), null, 0);
	}
	
}
