package com.minecrafttas.mcp4gradle.tasks;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;

/**
 * Gradle Task for creating the patch files
 * @author Pancake
 */
public class TaskCreateDiff extends DefaultTask {
	
	/**
	 * Create diff file between original mc source and 
	 * @throws Exception Filesystem Exception
	 */
	@TaskAction
	public void createDiff() throws Exception {
		var file = new File(this.getProject().getProjectDir(), "change.patch");
		if (!file.exists()) file.createNewFile();
		FileWriter patch = new FileWriter(file, false);
		
		var originalMcSource = new File(this.getProject().getBuildDir(), "src/minecraft/net/minecraft");
		var modifiedMcSource = new File(this.getProject().getProjectDir(), "src/main/java/net/minecraft");
		this.diff(originalMcSource, modifiedMcSource, patch);

		patch.close();
		System.out.println("Successfully created diff file");
	}

	/**
	 * Compute diff between two files and recurse directories
	 * @param original Original file
	 * @param modified Modified file
	 * @param writer Patch writer
	 * @throws Exception Filesystem Exception
	 */
	private void diff(File original, File modified, FileWriter writer) throws Exception {
		// recurse through directories
		if (original.isDirectory()) {
			for (File f : original.listFiles())
				this.diff(f, new File(modified, f.getName()), writer);
			
			return;
		}
		
		// read files
		var originalText = original.exists() ? Files.readAllLines(original.toPath()) : new ArrayList<String>();
		var modifiedText = modified.exists() ? Files.readAllLines(modified.toPath()) : new ArrayList<String>();
		
		// create diff
		System.out.println("Comparing " + original.getName() + "...");
		var diff = DiffUtils.diff(originalText, modifiedText);
		var unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(original.getName(), modified.getName(), originalText, diff, 0);
		
		// write diff
		if (unifiedDiff.size() != 0) 
			writer.write("==========> " + original.getName() + "\n");
		for (String line : unifiedDiff) 
			writer.write(line + "\n");
	}
	
}
