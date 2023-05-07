package com.minecrafttas.mcp4gradle;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.minecrafttas.mcp4gradle.tools.ApplyDiff;
import com.minecrafttas.mcp4gradle.tools.Jad;
import com.minecrafttas.mcp4gradle.tools.JadRetro;
import com.minecrafttas.mcp4gradle.tools.MCInjector;
import com.minecrafttas.mcp4gradle.tools.RetroGuard;
import com.minecrafttas.mcp4gradle.tools.SourceRenamer;

/**
 * Utils used throughout mcp4gradle
 * @author Pancake
 */
public class Utils {
	
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
		File rOut = tempFile();
		RetroGuard retroguard = new RetroGuard(tempFile(MINECRAFT_URL), rOut, tempFile(RETROGUARD_CFG));
		retroguard.init(tempFile(RETROGUARD_SRG));
		retroguard.run();
		
		System.out.println("Running MCInjector...");
		File iOut = tempFile();
		MCInjector mcinjector = new MCInjector(rOut, iOut);
		mcinjector.init(tempFile(MCINJECTOR_EXC));
		mcinjector.run();
		
		System.out.println("Running JadRetro...");
		File jOut = new File(build, "bin/minecraft");
		JadRetro jadretro = new JadRetro(iOut, jOut);
		jadretro.init();
		jadretro.run();
		
		System.out.println("Running Jad...");
		Jad jad = new Jad(build);
		jad.init();
		jad.run();
		
		System.out.println("Running ApplyDiff...");
		ApplyDiff applydiff = new ApplyDiff(build);
		applydiff.init(tempFile(DIFF));
		applydiff.run();
		
		System.out.println("Running Source Renamer...");
		SourceRenamer sourcerenamer = new SourceRenamer(new File(build, "src"));
		sourcerenamer.init(tempFile(METHODS), tempFile(FIELDS));
		sourcerenamer.run();
		
		System.out.println("Running Garbage Collector...");
        System.gc();
        
        System.out.println("Successfully decompiled minecraft");
	}
	
	/**
	 * Obtains a new temporary file
	 * @return Temporary file
	 * @throws Exception Filesystem Exception
	 */
	public static File tempFile() throws Exception {
		File temp = File.createTempFile("mcp4gradle", "");
		temp.delete();
		return temp;
	}
	
	/**
	 * Obtains a new temporary file from the internet
	 * @param url URL
	 * @return Temporary file
	 * @throws Exception Filesystem Exception
	 */
	public static File tempFile(String url) throws Exception {
		File temp = File.createTempFile("mcp4gradle", "");
		Files.copy(new URL(url).openStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return temp;
	}
	
}
