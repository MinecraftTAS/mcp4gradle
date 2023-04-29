package com.minecrafttas.mcp4gradle.tasks;

import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.minecrafttas.mcp4gradle.Utils;

/**
 * Task used to recreate sources after another person has committed
 * @author Pancake
 */
public class TaskDecommit extends DefaultTask {

	@TaskAction
	public void decommit() {
		try {
			/* Copy back source */
			System.err.println("Deleting sources...");
	        new File(getProject().getProjectDir(), "src/main/java").mkdirs();
			new File(getProject().getProjectDir(), "src/main/java/net/minecraft").delete();
			System.err.println("Copying sources...");
			Utils.copyFolder(new File(getProject().getProjectDir(), "build/src/minecraft").toPath(), new File(getProject().getProjectDir(), "src/main/java").toPath(), StandardCopyOption.REPLACE_EXISTING);
	        /* Patch source */
			if (!new File("C:\\Windows\\System32\\wsl.exe").exists()) {
	        	getProject().getLogger().error("Please install WSL, the packages diffutils and git");
	        	return;
	        }
			System.err.println("Patching sources...");
	        Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "patch", "-s", "-p0", "<", "change.patch"), getProject().getProjectDir(), false);
	        System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
