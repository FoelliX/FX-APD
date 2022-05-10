package de.foellix.apd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHelper {
	public static void download(String url, File downloadedFile) {
		try (InputStream in = new URL(url).openStream()) {
			Files.copy(in, downloadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			System.err.println("Could not download from URL \"" + url + "\" to \"" + downloadedFile.getAbsolutePath()
					+ "\". (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
		}
	}

	public static void extractAndroidJar(File zipFile, File destinationFile) {
		final byte[] buffer = new byte[1024];
		try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				if (zipEntry.getName() == null || zipEntry.getName().isBlank()
						|| !zipEntry.getName().endsWith("android.jar")) {
					zipEntry = zis.getNextEntry();
					continue;
				}
				final FileOutputStream fos = new FileOutputStream(destinationFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				break;
			}
			zis.closeEntry();
		} catch (final IOException e) {
			System.err.println("Error while unzipping \"" + zipFile.getAbsolutePath() + "\" to \""
					+ destinationFile.getAbsolutePath() + "\". (" + e.getClass().getSimpleName() + ": " + e.getMessage()
					+ ")");
		}
	}
}