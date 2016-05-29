package org.wso2.carbon.osgi.test.util.container;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Extract zip or tar.gz archives to a target folder.
 */
public class ArchiveExtractor {

    private ArchiveExtractor() {
    }

    /**
     * Extract zip or tar.gz archives to a target folder.
     *
     * @param sourceURL    url of the archive to extract
     * @param targetFolder where to extract to
     * @throws IOException on I/O error
     */
    public static void extract(URL sourceURL, File targetFolder) throws IOException {
        if (sourceURL.getProtocol().equals("file")) {
            if (sourceURL.getFile().indexOf(".zip") > 0) {
                extractZipDistribution(sourceURL, targetFolder);
            } else if (sourceURL.getFile().indexOf(".tar.gz") > 0) {
                extractTarGzDistribution(sourceURL, targetFolder);
            } else {
                throw new IllegalStateException(
                        "Unknow packaging of distribution; only zip or tar.gz could be handled.");
            }
            return;
        }
        if (sourceURL.toExternalForm().indexOf("/zip") > 0) {
            extractZipDistribution(sourceURL, targetFolder);
        } else if (sourceURL.toExternalForm().indexOf("/tar.gz") > 0) {
            extractTarGzDistribution(sourceURL, targetFolder);
        } else {
            throw new IllegalStateException(
                    "Unknow packaging; only zip or tar.gz could be handled. URL was " + sourceURL);
        }
    }

    public static void extract(Path path, File targetFolder) throws IOException {
        if (path.toString().indexOf(".zip") > 0) {
            extract(new ZipArchiveInputStream(new FileInputStream(path.toFile())), targetFolder);
        } else {
            throw new IllegalStateException("Unknow packaging of distribution; only zip can be handled.");
        }
    }

    private static void extractTarGzDistribution(URL sourceDistribution, File _targetFolder) throws IOException {
        File uncompressedFile = File.createTempFile("uncompressedTarGz-", ".tar");
        extractGzArchive(sourceDistribution.openStream(), uncompressedFile);
        extract(new TarArchiveInputStream(new FileInputStream(uncompressedFile)), _targetFolder);
        FileUtils.forceDelete(uncompressedFile);
    }

    private static void extractZipDistribution(URL sourceDistribution, File _targetFolder) throws IOException {
        extract(new ZipArchiveInputStream(sourceDistribution.openStream()), _targetFolder);
    }

    private static void extractGzArchive(InputStream tarGz, File tar) throws IOException {
        BufferedInputStream in = new BufferedInputStream(tarGz);
        FileOutputStream out = new FileOutputStream(tar);
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
        final byte[] buffer = new byte[1000];
        int n = 0;
        while (-1 != (n = gzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        gzIn.close();
    }

    private static void extract(ArchiveInputStream is, File targetDir) throws IOException {
        try {
            if (targetDir.exists()) {
                FileUtils.forceDelete(targetDir);
            }
            targetDir.mkdirs();
            ArchiveEntry entry = is.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                name = name.substring(name.indexOf("/") + 1);
                File file = new File(targetDir, name);
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    OutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
                entry = is.getNextEntry();
            }
        } finally {
            is.close();
        }
    }
}
