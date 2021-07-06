package de.pfannekuchen.mcpgradle;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import net.lingala.zip4j.ZipFile;
import retrogradle.NameProvider;
import retrogradle.obf.RetroGuardImpl;

/**
 * Gradle Task that automatically exports and obfuscates a File ready to be ran as minecraft.jar
 * @author Pancake
 */
public class TaskExport extends DefaultTask {

	@TaskAction
	public void export() {
		try {
			System.err.println("Repacking...");
			File jarFile = new File(getProject().getBuildDir(), "libs/").listFiles()[0];
			// download resources
			File resDir = new File(getProject().getBuildDir(), "resources1.0");
			resDir.mkdirs();
			ZipFile res = new ZipFile(Utils.obtainTempFile(new URL("https://mgnet.work/cfg/resources1.0.zip")));
			res.extractAll(resDir.getAbsolutePath());
			ZipFile orig = new ZipFile(jarFile);
			orig.removeFile("me.class");
			resDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					try {
						File fileOrFolder = new File(dir, name);
						if (fileOrFolder.isDirectory()) orig.addFolder(fileOrFolder);
						else orig.addFile(fileOrFolder);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}
			});
			orig.close();
			System.err.println("Reobfuscating...");
			File deobfFile = new File(getProject().getBuildDir(), "minecraft.jar");
			File config = Utils.tempFile();
			Files.write(config.toPath(), Arrays.asList(
					"startindex = 0",
					"reobinput = " + jarFile.getAbsolutePath(),
					"reoboutput = " + deobfFile.getAbsolutePath(),
					"script = " + Utils.obtainTempFile(new URL("https://mgnet.work/cfg/retroguard_ro1.0.cfg")).getAbsolutePath(),
					"reob = " + Utils.obtainTempFile(new URL("https://mgnet.work/cfg/client_ro1.0.srg")).getAbsolutePath(),
					"log = " + Utils.tempFile().getAbsolutePath(),
					"rolog = " + Utils.tempFile().getAbsolutePath(),
					"protectedpackage = paulscode",	
					"protectedpackage = com/jcraft",
					"protectedpackage = isom",
					"protectedpackage = ibxm",
					"protectedpackage = de/matthiasmann/twl",
					"protectedpackage = org/xmlpull",
					"protectedpackage = javax/xml"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
			// run retroguard the bad way
			String[] args = NameProvider.parseCommandLine(new String[] {"-notch", config.getAbsolutePath()});
			RetroGuardImpl.obfuscate((args.length < 1 ? null : args[0]), (args.length < 2 ? null : args[1]), (args.length < 3 ? null : args[2]), (args.length < 4 ? null : args[3]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
