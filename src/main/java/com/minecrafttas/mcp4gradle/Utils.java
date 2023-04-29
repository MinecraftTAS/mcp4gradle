package com.minecrafttas.mcp4gradle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utils used throughout mcp4gradle
 * @author Pancake
 */
public class Utils {
	
	/**
	 * Recursively copy a folder
	 * @param source Source folder
	 * @param target Desintation folder
	 * @throws Exception Filesystem Exception
	 */
	public static void copyFolder(Path source, Path target) throws Exception {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
	
	/**
	 * Obtains a new temporary file
	 * @return Temporary file
	 * @throws Exception Filesystem Exception
	 */
	public static File tempFile() throws Exception {
		var temp = File.createTempFile("mcp4gradle", "");
		temp.delete();
		return temp;
	}
	
	/**
	 * Obtains a new temporary file from the internet
	 * @param url URL
	 * @return Temporary file
	 * @throws Exception Filesystem Exception
	 */
	public static File tempFile(String url) throws Exception {
		var temp = File.createTempFile("mcp4gradle", "");
		Files.copy(new URL(url).openStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return temp;
	}

}
