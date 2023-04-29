package com.minecrafttas.mcp4gradle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Utils contains all kinds of Utils
 * @author Pancake
 */
public class Utils {
	
	/**
	 * Recursively copies a Folder to another one
	 * @param source Source folder
	 * @param target Desintation folder
	 * @param options Copy Options for the copying process
	 * @throws IOException Throws whenever IO fails
	 */
	public static void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }
	
	/**
	 * Creates a new Temp File
	 * @return Returns a created file
	 * @throws Exception Throws an Exception whenever IO is busy 
	 */
	public static File createTempFile() throws Exception {
		return File.createTempFile("mcp4gradle", "");
	}
	
	/**
	 * References a new Temp File without creating it
	 * @return Returns a temporary location
	 * @throws Exception Throws an Exception whenever IO is busy 
	 */
	public static File tempFile() throws Exception {
		File temp = File.createTempFile("mcp4gradle", "");
		temp.delete();
		return temp;
	}
	
	/**
	 * Downloads a File as a temporary file
	 * @param url Download URL
	 * @return Returns temp file
	 * @throws Exception Throws an exception whenever IO is busy
	 */
	public static File obtainTempFile(String url) throws Exception {
		File temp = File.createTempFile("mcp4gradle", "");
		Files.copy(new URL(url).openStream(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return temp;
	}

}
