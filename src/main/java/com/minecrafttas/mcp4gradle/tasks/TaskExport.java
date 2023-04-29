package com.minecrafttas.mcp4gradle.tasks;

import java.io.File;
import java.io.FilenameFilter;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.minecrafttas.mcp4gradle.Utils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

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
			ZipFile res = new ZipFile(Utils.tempFile("https://data.mgnet.work/mcp4gradle/assets.zip"));
			res.extractAll(resDir.getAbsolutePath());
			ZipFile orig = new ZipFile(jarFile);
			resDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					try {
						File fileOrFolder = new File(dir, name);
						ZipParameters parameters = new ZipParameters();
						parameters.setOverrideExistingFilesInZip(false);
						if (fileOrFolder.isDirectory()) orig.addFolder(fileOrFolder, parameters);
						else orig.addFile(fileOrFolder, parameters);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}
			});
			orig.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
