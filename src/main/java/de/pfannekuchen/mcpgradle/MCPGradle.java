package de.pfannekuchen.mcpgradle;

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

import jadretro.Main;
import mcinjector.MCInjectorImpl;
import net.lingala.zip4j.ZipFile;
import retrogradle.NameProvider;
import retrogradle.obf.RetroGuardImpl;

/**
 * Gradle plugin. 
 * @author Pancake	
 */
public class MCPGradle implements Plugin<Project> {
	
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
				/* RetroGuard */
				System.out.println("Running Retroguard [De]obfuscator");
				File minecraft_rg_jar = Utils.tempFile();
				File client_rg_cfg = Utils.tempFile();
				// create new config file with above paths
				Files.write(client_rg_cfg.toPath(), Arrays.asList(
						"startindex = 0",
						"input = " + Utils.obtainTempFile(new URL("https://maven.mgnet.work/com/mojang/minecraft/1.0/minecraft-1.0.jar")).getAbsolutePath(),
						"output = " + minecraft_rg_jar.getAbsolutePath(),
						"reobinput = " + Utils.tempFile().getAbsolutePath(),
						"reoboutput = " + Utils.tempFile().getAbsolutePath(),
						"script = " + Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/mappings/retroguard.cfg")).getAbsolutePath(),
						"log = " + Utils.tempFile().getAbsolutePath(),
						"deob = " + Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/mappings/client_rg.srg")).getAbsolutePath(),
						"protectedpackage = paulscode",	
						"protectedpackage = com/jcraft",
						"protectedpackage = isom",
						"protectedpackage = ibxm",
						"protectedpackage = de/matthiasmann/twl",
						"protectedpackage = org/xmlpull",
						"protectedpackage = javax/xml"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				// run retroguard the bad way
				String[] args = NameProvider.parseCommandLine(new String[] {"-searge", client_rg_cfg.getAbsolutePath()});
				RetroGuardImpl.obfuscate((args.length < 1 ? null : args[0]), (args.length < 2 ? null : args[1]), (args.length < 3 ? null : args[2]), (args.length < 4 ? null : args[3]));
				System.out.println("Running MCInjector...");
				/* MCInjector */
				File minecraft_exc_jar = Utils.tempFile();
				// run mcinjector the actual right way
				MCInjectorImpl.process(minecraft_rg_jar.getAbsolutePath(), minecraft_exc_jar.getAbsolutePath(), Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/mappings/client.exc")).getAbsolutePath(), Utils.tempFile().getAbsolutePath(), null, 0);
				System.out.println("Applying Jad Retro to sources...");
				/* Jadretro */
		        new File(project.getBuildDir(), "src/minecraft").mkdirs(); // dumb.. create the build folder first
		        System.out.println("Unzipping Sources..");
		        // unzip files
		        ZipFile f = new ZipFile(minecraft_exc_jar); f.extractAll(new File(project.getBuildDir(), "bin/minecraft").getAbsolutePath()); f.close();
		        // run jadretro the cli way
		        Main.main(new String[] { new File(project.getBuildDir(), "bin/minecraft/net/minecraft/client").getAbsolutePath()});
		        Main.main(new String[] { new File(project.getBuildDir(), "bin/minecraft/net/minecraft/src").getAbsolutePath()});
		        System.out.println("Decompiling using Jad..");
		        /* Jad */
		        File jad_exe = Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/tools/jad.exe"));
		        // jad is not a java program (???) so we run it via cli
		        Utils.run(Arrays.asList(jad_exe.getAbsolutePath(), "-b", "-d", "src/minecraft", "-dead", "-o", "-r", "-s", ".java", "-stat", "-v", "-ff", "bin/minecraft\\net\\minecraft\\client\\*.class"), project.getBuildDir(), false);
		        Utils.run(Arrays.asList(jad_exe.getAbsolutePath(), "-b", "-d", "src/minecraft", "-dead", "-o", "-r", "-s", ".java", "-stat", "-v", "-ff", "bin/minecraft\\net\\minecraft\\src\\*.class"), project.getBuildDir(), false);
		        System.out.println("Fixing up Sources..");
		        /* Applydiff - few diffs so mcp works. */
		        Utils.run(Arrays.asList(Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/tools/applydiff.exe")).getAbsolutePath(), "--binary", "-p1", "-u", "-i", Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/mappings/temp.patch")).getAbsolutePath(), "-d", "src/minecraft"), project.getBuildDir(), false);
		        System.out.println("Renaming Sources..");
		        /* Rename Sources */
		        // read csv files
		        CSVParser functionParser = CSVFormat.DEFAULT.parse(new FileReader(Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/mappings/methods.csv"))));
		        CSVParser methodParser = CSVFormat.DEFAULT.parse(new FileReader(Utils.obtainTempFile(new URL("https://data.mgnet.work/mcp4gradle/mappings/fields.csv"))));
		        HashMap<String, String> functionRefmap = new HashMap<>();
		        for (CSVRecord csvRecord : functionParser) {
		        	if (csvRecord.get(8).equals("0")) functionRefmap.put(csvRecord.get(0), csvRecord.get(1));
				}
		        HashMap<String, String> methodRefmap = new HashMap<>();
		        for (CSVRecord csvRecord : methodParser) {
		        	if (csvRecord.get(8).equals("0")) methodRefmap.put(csvRecord.get(0), csvRecord.get(1));
				}
		        // modify all files
		        new File(project.getBuildDir(), "src").listFiles(new FilenameFilter() { @Override public boolean accept(File dir, String name) {
		        	try {
						File theFile = new File(dir, name);
						if (theFile.isDirectory()) theFile.listFiles(this);
						else if (theFile.getName().toLowerCase().endsWith(".java")) {
							// read file
							FileReader reader = new FileReader(theFile);
							String fileContent = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
							String orig = fileContent + "";
							reader.close();
							// replace funcs
							Matcher func = Pattern.compile("func_[0-9]+_[a-zA-Z]+_?").matcher(fileContent);
							while (func.find()) {
								String match = func.group();
								if (functionRefmap.containsKey(match)) fileContent = fileContent.replaceAll(match, functionRefmap.get(match));
							}
							// replace methods
							Matcher method = Pattern.compile("field_[0-9]+_[a-zA-Z]+_?").matcher(fileContent);
							while (method.find()) {
								String match = method.group();
								if (methodRefmap.containsKey(match)) fileContent = fileContent.replaceAll(match, methodRefmap.get(match));
							}
							// replace opengl
							if (fileContent.contains("import org.lwjgl.opengl.")) {
								Matcher opengl = Pattern.compile("(?<![.\\w])\\d+(?![.\\w])(?! /\\*GL_)").matcher(fileContent);
								while (opengl.find()) {
									String match = opengl.group(0);
									if (Utils.map.containsKey(Integer.parseInt(match))) fileContent = fileContent.replaceAll(match + "\\)", match + " /*" + Utils.map.get(Integer.parseInt(match)) + "*/)").replaceAll(match + ",", match + " /*" + Utils.map.get(Integer.parseInt(match)) + "*/,");
									
								}
							}
							// write file again
							if (!orig.equals(fileContent)) {
								FileWriter writer = new FileWriter(theFile, false);
								writer.write(fileContent + "\n");
								writer.flush();
								writer.close();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				return true; } });
		        System.out.println("Linuxifying Sources..");
		        /* Convert to unix format forever.. */
		        Utils.run(Arrays.asList("C:\\Windows\\System32\\wsl.exe", "find", "./build/src", "-type", "f", "-exec", "dos2unix", "{}", "\\;"), project.getProjectDir(), false); // linuxify
		        System.out.println("Garbage Collecting...");
		        System.gc();
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
