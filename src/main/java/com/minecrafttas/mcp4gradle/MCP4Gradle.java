package com.minecrafttas.mcp4gradle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;

import com.minecrafttas.mcp4gradle.tasks.TaskCreateDiff;
import com.minecrafttas.mcp4gradle.tasks.TaskApplyDiff;
import com.minecrafttas.mcp4gradle.tasks.TaskExport;

/**
 * Gradle plugin. 
 * @author Pancake	
 */
public class MCP4Gradle implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		// add required plugins
		project.getPlugins().apply("java-library");
		
		// add tasks
		var commitTask = project.getTasks().register("createDiff", TaskCreateDiff.class).get();
		commitTask.setGroup("mcpgradle");
		var decommitTask = project.getTasks().register("applyDiff", TaskApplyDiff.class).get();
		decommitTask.setGroup("mcpgradle");
		var exportTask = project.getTasks().register("export", TaskExport.class).get();
		exportTask.setGroup("mcpgradle");
		exportTask.dependsOn("jar");
		
		// add repositories
		project.getRepositories().mavenCentral();
		project.getRepositories().maven(in -> in.setUrl(URI.create("https://maven.mgnet.work/main")));
		project.getRepositories().maven(in -> in.setUrl(URI.create("https://libraries.minecraft.net/")));

		// add dependencies
		var deps = project.getConfigurations().getByName("implementation").getDependencies();
		project.getGradle().addListener(new DependencyResolutionListener() { // add dependencies, before they are being resolved
			@Override 	
			public void beforeResolve(ResolvableDependencies resDeps) {
				deps.add(project.getDependencies().create("com.mojang:minecraft:1.0"));
				deps.add(project.getDependencies().create("net.java.jinput:jinput:2.0.5"));
				deps.add(project.getDependencies().create("org.lwjgl.lwjgl:lwjgl:2.9.0"));
				deps.add(project.getDependencies().create("org.lwjgl.lwjgl:lwjgl_util:2.9.0"));
				project.getGradle().removeListener(this);
			}
			@Override public void afterResolve(ResolvableDependencies arg0) {}
		});
		
		// download natives
		project.afterEvaluate((p) -> {
			var natives = new File(project.getBuildDir(), "natives");
			if (natives.exists())
				return;
			
			try {
				natives.mkdirs();
				
				var url = "https://data.mgnet.work/mcp4gradle/natives/";
				Files.copy(new URL(url + "jinput-dx8_64.dll").openStream(), new File(natives, "jinput-dx8_64.dll").toPath());
				Files.copy(new URL(url + "jinput-raw_64.dll").openStream(), new File(natives, "jinput-raw_64.dll").toPath());
				Files.copy(new URL(url + "lwjgl64.dll").openStream(), new File(natives, "lwjgl64.dll").toPath());
				Files.copy(new URL(url + "OpenAL64.dll").openStream(), new File(natives, "OpenAL64.dll").toPath());
			} catch (IOException e) {
				natives.delete();
				
				System.err.println("Unable to download natives");
				e.printStackTrace();
			}
		});
		
		// create project if minecraft isn't decompiled yet
		if (!new File(project.getBuildDir(), "src/minecraft/net/minecraft/client/Minecraft.java").exists()) {
			
			// fully decompile the game
			try {
				Utils.decompile(project.getBuildDir());
			} catch (Exception e) {
				System.err.println("Unable to decompile the game");
				e.printStackTrace();
			}
			
			// being moved somewhere else
//			System.out.println("Deleting sources...");
//	        new File(project.getProjectDir(), "src/main/java").mkdirs();
//			new File(project.getProjectDir(), "src/main/java/net/minecraft").delete();
//			System.out.println("Copying sources...");
//			System.gc();
//			Utils.copyFolder(new File(project.getProjectDir(), "build/src/minecraft").toPath(), new File(project.getProjectDir(), "src/main/java").toPath(), StandardCopyOption.REPLACE_EXISTING);
//	        /* Patch source */
//			if (!new File("C:\\Windows\\System32\\wsl.exe").exists()) {
//				project.getLogger().error("Please install WSL, the packages diffutils and git");
//	        	return;
//	        }
//			System.out.println("Patching sources...");
//	        Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "patch", "-s", "-p0", "<", "change.patch"), project.getProjectDir(), false);
//	        System.gc();
		}
	}
	
}
