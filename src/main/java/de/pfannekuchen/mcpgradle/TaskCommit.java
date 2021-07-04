package de.pfannekuchen.mcpgradle;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Gradle Task that automatically creates patch files, and commits with a 
 * user defined commit message
 * @author Pancake
 */
public class TaskCommit extends DefaultTask {
	
	@TaskAction
	public void commit() {
		try {
			/* Obtain Commit Info */
			Scanner scanner = new Scanner(System.in);
			System.out.println("Please enter a commit message:");
			String commitText = scanner.nextLine();
			System.out.println("Please enter a commit description");
			String commitDescription = scanner.nextLine();
			scanner.close();
			/* Create Commit Files */
			if (!new File("C:\\Windows\\System32\\wsl.exe").exists()) {
				getProject().getLogger().error("Please install WSL, the packages diffutils and git");
				return;
			}
			System.err.println("Converting src/ to linux file ending");
			Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "find", "./src/main/java/", "-type", "f", "-exec", "touch", "-a", "-m", "-t", "202101010000.00", "{}", "\\;"), getProject().getProjectDir(), false);
			System.err.println("Fixing last modified date for build/src/");
			Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "find", "./build/src/", "-type", "f", "-exec", "touch", "-a", "-m", "-t", "202101010000.00", "{}", "\\;"), getProject().getProjectDir(), false);
			System.err.println("Creating patch file");
			Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "diff", "-ruN", "build/src/minecraft/net/minecraft", "src/main/java/net/minecraft", ">", "change.patch"), getProject().getProjectDir(), false);
			/* Commit */
			System.err.println("Committing...");
			Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "git", "add", "."), getProject().getProjectDir(), true);
			Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "git", "commit", "-m", "\"" + commitText + "\"", "-m", "\"" + commitDescription + "\""), getProject().getProjectDir(), true);
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
