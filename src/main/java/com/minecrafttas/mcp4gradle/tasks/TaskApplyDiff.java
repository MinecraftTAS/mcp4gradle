package com.minecrafttas.mcp4gradle.tasks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

/**
 * Task used to recreate sources after another person has committed
 * @author Pancake
 */
public class TaskApplyDiff extends DefaultTask {

	/**
	 * Apply diff file to the original source
	 * @throws Exception Filesystem Exception
	 */
	@TaskAction
	public void applyDiff() throws Exception {
		var patches = new HashMap<String, Patch<String>>();
		
		// read patch file
		var file = new File(this.getProject().getProjectDir(), "change.patch");
		if (!file.exists())
			file.createNewFile();
		var lineIterator = Files.readAllLines(file.toPath()).iterator();
		
		// parse patch files
		var currentPatch = new ArrayList<String>();
		var currentPatchFile = "";
		while (lineIterator.hasNext()) {
			var line = lineIterator.next();
			
			if (line.startsWith("==========>")) {
				
				if (!currentPatchFile.isEmpty()) {
					patches.put(currentPatchFile, UnifiedDiffUtils.parseUnifiedDiff(currentPatch));
					currentPatch = new ArrayList<>();
				}
				
				currentPatchFile = line.split(" ")[1];
				continue;
			}
			
			currentPatch.add(line);
		}
		
		// patch files
		var originalMcSource = new File(this.getProject().getBuildDir(), "src/minecraft/net/minecraft");
		var modifiedMcSource = new File(this.getProject().getProjectDir(), "src/main/java/net/minecraft");
		this.patch(originalMcSource, modifiedMcSource, patches);

		System.out.println("Successfully applied diff file");
	}
	
	/**
	 * Apply diff
	 * @param original Original file
	 * @param out Out file
	 * @param patches Patch file
	 * @throws Exception Filesystem Exception
	 */
	private void patch(File original, File out, Map<String, Patch<String>> patches) throws Exception {
		// recurse through directories
		if (original.isDirectory()) {
			for (File f : original.listFiles())
				this.patch(f, new File(out, f.getName()), patches);
			
			return;
		}

		// skip if no patch
		Patch<String> patch;
		if ((patch = patches.get(original.getName())) == null) {
			if (!out.exists()) 
				Files.copy(original.toPath(), out.toPath());
			
			return;
		}
		
		// apply diff
		System.out.println("Patching " + original.getName() + "...");
		var outText = DiffUtils.patch(Files.readAllLines(original.toPath()), patch);
		
		// write file
		out.getParentFile().mkdirs();
		Files.write(out.toPath(), outText, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
}
