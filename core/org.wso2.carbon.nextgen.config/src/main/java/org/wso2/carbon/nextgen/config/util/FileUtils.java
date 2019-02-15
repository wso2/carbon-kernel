package org.wso2.carbon.nextgen.config.util;

import org.wso2.carbon.nextgen.config.ConfigParserException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Contains the util methods related to file.
 */
public class FileUtils {

    private FileUtils() {

    }

    /**
     * Read a given file and returns the content of the file as a string.
     *
     * @param file File to be read
     * @return The content of the file
     * @throws ConfigParserException If file doesn't exist or is not a file or if some IO error occurs.
     */
    public static String readFile(File file) throws ConfigParserException {

        if (file.exists() && file.isFile()) {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                    StandardCharsets.UTF_8))) {
                String s;
                while ((s = br.readLine()) != null) {
                    stringBuilder.append(s).append("\n");
                }
            } catch (IOException e) {
                throw new ConfigParserException("Error reading file " + file.getName(), e);
            }
            return stringBuilder.toString();
        } else {
            throw new ConfigParserException(file.getName() + " does not exist or is not a file");
        }
    }

    private static void writeFile(File file, String input) throws ConfigParserException {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),
                StandardCharsets.UTF_8))) {
            bufferedWriter.write(input);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new ConfigParserException("Error while writing file into " + file.getAbsolutePath(), e);
        }
    }

    public static void writeDirectory(File configurations, File backupFile) throws ConfigParserException {

        backupFile = Paths.get(backupFile.getAbsolutePath(), configurations.getName()).toFile();
        if (configurations.isDirectory()) {
            boolean status = backupFile.mkdirs();
            if (!status) {
                throw new ConfigParserException("Error while creating directory " + configurations.getName());
            }
            File[] files = configurations.listFiles();
            if (files != null) {
                for (File file : files) {
                    writeDirectory(file, backupFile);
                }
            }
        } else {
            String configuration = FileUtils.readFile(configurations);
            FileUtils.writeFile(backupFile, configuration);
        }
    }

    public static void deleteDirectroy(File file) throws ConfigParserException {

        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    if (files.length > 0) {
                        for (File file1 : files) {
                            deleteDirectroy(file1);
                        }
                    } else {
                        boolean status = file.delete();
                        if (!status) {
                            throw new ConfigParserException("Error while deleting " + file.getName());
                        }
                    }
                }
            } else {
                boolean status = file.delete();
                if (!status) {
                    throw new ConfigParserException("Error while deleting " + file.getName());
                }
            }
        }

    }
}
