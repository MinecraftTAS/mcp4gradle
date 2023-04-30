package com.minecrafttas.mcp4gradle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.JavaPluginExtension;

import com.minecrafttas.mcp4gradle.tasks.TaskApplyDiff;
import com.minecrafttas.mcp4gradle.tasks.TaskCreateDiff;

/**
 * Gradle plugin. 
 * @author Pancake	
 */
public class MCP4Gradle implements Plugin<Project> {
	
	@Override
	public void apply(Project project) {
		// add required plugins
		project.getPlugins().apply("java-library");
		project.getPlugins().apply("com.github.johnrengelman.shadow");
		
		// setup java plugin
		project.getExtensions().getByType(JavaPluginExtension.class).setSourceCompatibility(JavaVersion.VERSION_1_8);
		project.getExtensions().getByType(JavaPluginExtension.class).setTargetCompatibility(JavaVersion.VERSION_1_8);
		
		// add tasks
		var createDiffTask = project.getTasks().register("createDiff", TaskCreateDiff.class).get();
		createDiffTask.setGroup("mcpgradle");
		var applyDiffTask = project.getTasks().register("applyDiff", TaskApplyDiff.class).get();
		applyDiffTask.setGroup("mcpgradle");
		
		// add repositories
		project.getRepositories().mavenCentral();
		project.getRepositories().maven(in -> in.setUrl(URI.create("https://maven.mgnet.work/main")));
		project.getRepositories().maven(in -> in.setUrl(URI.create("https://libraries.minecraft.net/")));
		
		// add dependencies
		project.getDependencies().add("implementation", "com.mojang:soundsystem:1.0");
		project.getDependencies().add("implementation", "com.mojang:minecraftassets:1.0");
		project.getDependencies().add("implementation", "net.java.jutils:jutils:1.0.0");
		((ModuleDependency) project.getDependencies().add("implementation", "net.java.jinput:jinput:2.0.5")).setTransitive(false);
		((ModuleDependency) project.getDependencies().add("implementation", "org.lwjgl.lwjgl:lwjgl:2.9.0")).setTransitive(false);
		((ModuleDependency) project.getDependencies().add("implementation", "org.lwjgl.lwjgl:lwjgl_util:2.9.0")).setTransitive(false);
		project.getDependencies().add("runtimeOnly", "org.lwjgl.lwjgl:lwjgl-platform:2.9.0");
		project.getDependencies().add("runtimeOnly", "net.java.jinput:jinput-platform:2.0.5");
		
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
				Utils.decompile(project.getBuildDir(), new File(project.getProjectDir(), "src/main/mc"));
				applyDiffTask.applyDiff();
			} catch (Exception e) {
				System.err.println("Unable to decompile the game");
				e.printStackTrace();
			}
		}
		
		// create source sets
		var s = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().getByName("main").getJava();
		s.srcDir("src/main/mc");
	}
	
}
