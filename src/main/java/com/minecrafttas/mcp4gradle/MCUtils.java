package com.minecrafttas.mcp4gradle;

import java.io.File;

import com.minecrafttas.mcp4gradle.tools.ApplyDiff;
import com.minecrafttas.mcp4gradle.tools.Jad;
import com.minecrafttas.mcp4gradle.tools.JadRetro;
import com.minecrafttas.mcp4gradle.tools.MCInjector;
import com.minecrafttas.mcp4gradle.tools.RetroGuard;
import com.minecrafttas.mcp4gradle.tools.SourceRenamer;

/**
 * Decompilation/Patching class
 */
public class MCUtils {
	
	private static String MINECRAFT_URL = "https://maven.mgnet.work/main/com/mojang/minecraft/1.0/minecraft-1.0.jar";
	private static String RETROGUARD_CFG = "https://data.mgnet.work/mcp4gradle/mappings/retroguard.cfg";
	private static String RETROGUARD_SRG = "https://data.mgnet.work/mcp4gradle/mappings/client_rg.srg";
	private static String MCINJECTOR_EXC = "https://data.mgnet.work/mcp4gradle/mappings/client.exc";
	private static String DIFF = "https://data.mgnet.work/mcp4gradle/mappings/temp.patch";
	private static String METHODS = "https://data.mgnet.work/mcp4gradle/mappings/methods.csv";
	private static String FIELDS = "https://data.mgnet.work/mcp4gradle/mappings/fields.csv";
	
	/**
	 * Decompile the game
	 * @param build Build/Output directory
	 * @throws Exception Exception during decompilation process
	 */
	public static void decompile(File build) throws Exception {
		System.out.println("Running RetroGuard...");
		var rOut = Utils.tempFile();
		var retroguard = new RetroGuard(Utils.tempFile(MINECRAFT_URL), rOut, Utils.tempFile(RETROGUARD_CFG));
		retroguard.init(Utils.tempFile(RETROGUARD_SRG));
		retroguard.run();
		
		System.out.println("Running MCInjector...");
		var iOut = Utils.tempFile();
		var mcinjector = new MCInjector(rOut, iOut);
		mcinjector.init(Utils.tempFile(MCINJECTOR_EXC));
		mcinjector.run();
		
		System.out.println("Running JadRetro...");
		var jOut = new File(build, "bin/minecraft");
		var jadretro = new JadRetro(iOut, jOut);
		jadretro.init();
		jadretro.run();
		
		System.out.println("Running Jad...");
		var jad = new Jad(build);
		jad.init();
		jad.run();
		
		System.out.println("Running ApplyDiff...");
		var applydiff = new ApplyDiff(build);
		applydiff.init(Utils.tempFile(DIFF));
		applydiff.run();
		
		System.out.println("Running Source Renamer...");
		var sourcerenamer = new SourceRenamer(new File(build, "src"));
		sourcerenamer.init(Utils.tempFile(METHODS), Utils.tempFile(FIELDS));
		sourcerenamer.run();
		
		System.out.println("Running Garbage Collector...");
        System.gc();
        
        System.out.println("Successfully decompiled minecraft");
	}
	
}
