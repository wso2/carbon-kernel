package org.wso2.carbon.nextgen.config.util;

import org.wso2.carbon.nextgen.config.ConfigParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Contains the util methods related to file reading.
 */
public class FileReaderUtils {

    private FileReaderUtils() {

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
}
