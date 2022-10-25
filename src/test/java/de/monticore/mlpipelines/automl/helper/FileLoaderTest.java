package de.monticore.mlpipelines.automl.helper;

import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileLoaderTest extends TestCase {

    public void testLoadResourceFile() {
        FileLoader ressourceLoader = new FileLoader();
        String modelName = "models/adanet/resourceloadertest.txt";
        List<String> lines = ressourceLoader.loadResourceFile(modelName);
        assertEquals(2, lines.size());
        assertEquals("test", lines.get(0));
        assertEquals("test2", lines.get(1));
    }

    public void testLoadFile() {
        FileLoader ressourceLoader = new FileLoader();
        String modelName = "ResourceLoaderTest2.txt";
        String pathString = getTempModelPathString(modelName);
        createDummyTempFile(ressourceLoader, pathString);

        List<String> lines = ressourceLoader.loadFile(pathString);
        assertEquals(3, lines.size());
        assertEquals("test", lines.get(0));
        assertEquals("test2", lines.get(1));
        assertEquals("test3", lines.get(2));
    }

    private void createDummyTempFile(FileLoader ressourceLoader, String pathString) {
        deleteFileIfExists(pathString);
        createFileIfNotExists(pathString);
        List<String> lines = createFileContent();
        ressourceLoader.writeToFile(lines, pathString);
    }

    private static String getTempModelPathString(String modelName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String pathString = tempDir + modelName;
        return pathString;
    }

    public void testWriteToFile() {
        FileLoader ressourceLoader = new FileLoader();
        String modelName = "ResourceLoaderTest2.txt";
        String pathString = getTempModelPathString(modelName);

        createDummyTempFile(ressourceLoader, pathString);

        List<String> lines2 = ressourceLoader.loadFile(pathString);
        assertEquals(3, lines2.size());
        assertEquals("test", lines2.get(0));
        assertEquals("test2", lines2.get(1));
        assertEquals("test3", lines2.get(2));
    }

    private void deleteFileIfExists(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createFileIfNotExists(String pathString) {
        Path path = Paths.get(pathString);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> createFileContent() {
        List<String> lines = new ArrayList<>();
        lines.add("test");
        lines.add("test2");
        lines.add("test3");
        return lines;
    }
}