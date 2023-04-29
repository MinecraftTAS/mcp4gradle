package com.minecrafttas.mcp4gradle.tasks;

import java.io.File;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.minecrafttas.mcp4gradle.Utils;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Gradle Task for exporting the project
 * @author Pancake
 */
public class TaskExport extends DefaultTask {

	private final String ASSETS_ZIP = "https://data.mgnet.work/mcp4gradle/assets.zip";
	
	/**
	 * Update existing compiled jar with required sources to launch minecraft
	 * @throws Exception
	 */
	@TaskAction
	public void export() throws Exception {
		var buildDir = this.getProject().getBuildDir();
		
		// download assets
		var assets = new File(buildDir, "assets");
		if (!assets.exists()) {
			System.out.println("Downloading assets...");
			assets.mkdirs();
			var assetsZip = new ZipFile(Utils.tempFile(ASSETS_ZIP));
			assetsZip.extractAll(assets.getAbsolutePath());
		}
		
		// setup zip params
		var params = new ZipParameters();
		params.setOverrideExistingFilesInZip(false);
		
		// combine compiled jar and assets
		System.out.println("Merging jar...");
		var outputJar = new ZipFile(new File(buildDir, "libs/").listFiles()[0]);
		for (File f : assets.listFiles())
			if (f.isDirectory())
				outputJar.addFolder(f, params);
			else
				outputJar.addFile(f, params);
		
		// close jar
		outputJar.close();
	}

}
