package com.minecrafttas.mcp4gradle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ResolvableDependencies;

import com.minecrafttas.mcp4gradle.tasks.TaskCommit;
import com.minecrafttas.mcp4gradle.tasks.TaskDecommit;
import com.minecrafttas.mcp4gradle.tasks.TaskExport;

import jadretro.Main;
import mcinjector.MCInjectorImpl;
import net.lingala.zip4j.ZipFile;
import retrogradle.NameProvider;
import retrogradle.obf.RetroGuardImpl;

/**
 * Gradle plugin. 
 * @author Pancake	
 */
public class MCP4Gradle implements Plugin<Project> {
	
	/**
	 * Called when the plugin is being loaded
	 */
	@Override
	public void apply(Project project) {
		project.getPlugins().apply("java-library");
		project.getAllTasks(true).forEach((p, tasks) -> {
			tasks.forEach((task) -> {
				task.setGroup(null);
			});
		});
		project.getTasksByName("javadoc", true).iterator().next().setGroup("mcpgradle");		/* Register Tasks, Repositories and Dependencies */ 
		final DependencySet deps = project.getConfigurations().getByName("implementation").getDependencies();
		project.getTasks().register("commit", TaskCommit.class).get().setGroup("mcpgradle");
		project.getTasks().register("decommit", TaskDecommit.class).get().setGroup("mcpgradle");
		TaskExport export = project.getTasks().register("export", TaskExport.class).get();
		export.setGroup("mcpgradle");
		export.dependsOn("jar");
		project.getRepositories().mavenCentral();
		project.getRepositories().maven((in) -> { try { in.setUrl(new URI("https://maven.mgnet.work")); } catch (Exception e) {} } );
		project.getRepositories().maven((in) -> { try { in.setUrl(new URI("https://libraries.minecraft.net/")); } catch (Exception e) {} } );
		project.afterEvaluate((p) -> {
			// download natives if it doesn't exist
			if (!(new File(project.getBuildDir(), "natives/").exists())) {
				try {
					new File(project.getBuildDir(), "natives").mkdirs();
					new File(project.getProjectDir(), "src/main/java").mkdirs();
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/jinput-dx8.dll").openStream(), new File(project.getBuildDir(), "natives/jinput-dx8.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/jinput-dx8_64.dll").openStream(), new File(project.getBuildDir(), "natives/jinput-dx8_64.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/jinput-raw.dll").openStream(), new File(project.getBuildDir(), "natives/jinput-raw.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/jinput-raw_64.dll").openStream(), new File(project.getBuildDir(), "natives/jinput-raw_64.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/lwjgl.dll").openStream(), new File(project.getBuildDir(), "natives/lwjgl.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/lwjgl64.dll").openStream(), new File(project.getBuildDir(), "natives/lwjgl64.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/OpenAL32.dll").openStream(), new File(project.getBuildDir(), "natives/OpenAL32.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
					Files.copy(new URL("https://data.mgnet.work/mcp4gradle/natives/OpenAL64.dll").openStream(), new File(project.getBuildDir(), "natives/OpenAL64.dll").toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
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
		/* Recreate Build Files when they don't exist yet */
		if (!new File(project.getProjectDir(), "build/src/minecraft/net/minecraft/client/Minecraft.java").exists()) {
			try {
				Decompiler.decompile(project.getProjectDir());
				
		        /* Copy back source */
				System.out.println("Deleting sources...");
		        new File(project.getProjectDir(), "src/main/java").mkdirs();
				new File(project.getProjectDir(), "src/main/java/net/minecraft").delete();
				System.out.println("Copying sources...");
				System.gc();
				Utils.copyFolder(new File(project.getProjectDir(), "build/src/minecraft").toPath(), new File(project.getProjectDir(), "src/main/java").toPath(), StandardCopyOption.REPLACE_EXISTING);
		        /* Patch source */
				if (!new File("C:\\Windows\\System32\\wsl.exe").exists()) {
					project.getLogger().error("Please install WSL, the packages diffutils and git");
		        	return;
		        }
				System.out.println("Patching sources...");
		        Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "patch", "-s", "-p0", "<", "change.patch"), project.getProjectDir(), false);
		        System.gc();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
