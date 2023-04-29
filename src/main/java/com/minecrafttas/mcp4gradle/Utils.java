package com.minecrafttas.mcp4gradle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

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
		var rOut = tempFile();
		var retroguard = new RetroGuard(tempFile(MINECRAFT_URL), rOut, tempFile(RETROGUARD_CFG));
		retroguard.init(tempFile(RETROGUARD_SRG));
		retroguard.run();
		
		System.out.println("Running MCInjector...");
		var iOut = tempFile();
		var mcinjector = new MCInjector(rOut, iOut);
		mcinjector.init(tempFile(MCINJECTOR_EXC));
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
		applydiff.init(tempFile(DIFF));
		applydiff.run();
		
		System.out.println("Running Source Renamer...");
		var sourcerenamer = new SourceRenamer(new File(build, "src"));
		sourcerenamer.init(tempFile(METHODS), tempFile(FIELDS));
		sourcerenamer.run();
		
		System.out.println("Running Garbage Collector...");
        System.gc();
        
        System.out.println("Successfully decompiled minecraft");
	}
	
	/**
	 * Recursively copy a folder
	 * @param source Source folder
	 * @param target Desintation folder
	 * @throws Exception Filesystem Exception
	 */
	public static void copyFolder(Path source, Path target) throws Exception {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
	
	/**
	 * Obtains a new temporary file
	 * @return Temporary file
	 * @throws Exception Filesystem Exception
	 */
	public static File tempFile() throws Exception {
		var temp = File.createTempFile("mcp4gradle", "");
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
		var temp = File.createTempFile("mcp4gradle", "");
		Files.copy(new URL(url).openStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return temp;
	}
	
}
