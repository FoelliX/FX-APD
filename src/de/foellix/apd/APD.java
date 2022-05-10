package de.foellix.apd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class APD {
	private static final String DEFAULT_DOWNLOAD_REPOSITORY = "https://dl.google.com/android/repository/repository2-3.xml";

	public static void main(String[] args) {
		try {
			final int from = args.length > 0 ? Integer.parseInt(args[0]) : -1;
			final int to = args.length > 1 ? Integer.parseInt(args[1]) : Integer.MAX_VALUE;
			final String repository = args.length > 2 ? args[2] : DEFAULT_DOWNLOAD_REPOSITORY;
			System.out.println("Starting [Repository: " + repository + ", API-Versions:" + from + "-" + to + "]");
			new APD().start(repository, from, to);
			System.out.println("\nFinished!");
		} catch (final Exception e) {
			System.out.println(
					"Use as follows: java -jar FX-APD.jar [from] [to] [repository/updatesite]\n\t- no optional parameters: java -jar FX-APD.jar\n\t- with optional parameter: java -jar FX-APD.jar 19 32 https://dl.google.com/android/repository/repository2-3.xml");
		}
	}

	private void start(String repository, int from, int to) {
		final String repoUrl = repository.substring(0, repository.lastIndexOf('/'));

		// Step 1
		System.out.println("\n1/4) Getting repository data...\nDownloading: " + repository);
		final File xmlFile = new File("repository.xml");
		if (!xmlFile.exists()) {
			FileHelper.download(repository, xmlFile);
		}

		// Step 2
		System.out.println("\n2/4) Searching platform files...");
		final Map<Integer, String> urlMap = new HashMap<>();
		try {
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc = builder.parse(xmlFile);
			final Element root = doc.getDocumentElement();
			root.normalize();

			for (int i1 = 0; i1 < root.getElementsByTagName("remotePackage").getLength(); i1++) {
				final Node remotePackage = root.getElementsByTagName("remotePackage").item(i1);
				boolean download = false;
				for (int i2 = 0; i2 < remotePackage.getChildNodes().getLength(); i2++) {
					if (remotePackage.getAttributes().getNamedItem("path").getNodeValue()
							.startsWith("platforms;android-")) {
						final Node child = remotePackage.getChildNodes().item(i2);
						if (child.getNodeName().equals("display-name")) {
							if (child.getTextContent() != null
									&& child.getTextContent().startsWith("Android SDK Platform ")) {
								download = true;
								break;
							}
						}
					}
				}
				if (download) {
					for (int i2 = 0; i2 < remotePackage.getChildNodes().getLength(); i2++) {
						final Node archieves = remotePackage.getChildNodes().item(i2);
						if (archieves.getNodeName().equals("archives")) {
							boolean done = false;
							for (int i3 = 0; i3 < archieves.getChildNodes().getLength() && !done; i3++) {
								final Node archieve = archieves.getChildNodes().item(i3);
								if (archieve.getNodeName().equals("archive")) {
									for (int i4 = 0; i4 < archieve.getChildNodes().getLength() && !done; i4++) {
										final Node complete = archieve.getChildNodes().item(i4);
										if (complete.getNodeName().equals("complete")) {
											for (int i5 = 0; i5 < complete.getChildNodes().getLength() && !done; i5++) {
												final Node urlNode = complete.getChildNodes().item(i5);
												if (urlNode.getNodeName().equals("url")) {
													final String filename = urlNode.getTextContent();
													if (!filename.contains("macosx") && !filename.contains("windows")) {
														try {
															final int version = Integer.parseInt(filename.substring(
																	filename.indexOf('-') + 1, filename.indexOf('_')));
															urlMap.put(version, filename);
															System.out.println(
																	"Found: " + version + " (" + filename + ")");
														} catch (final NumberFormatException e) {
															// Skip
														}
														done = true;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (final ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Error while reading repository data from: " + xmlFile.getAbsolutePath() + " ("
					+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
		}

		// Step 3
		System.out.println("\n3/4) Downloading...");
		final File dir = new File("platforms");
		if (!dir.exists()) {
			dir.mkdir();
		}
		final List<Integer> sortedVersions = new LinkedList<>(urlMap.keySet());
		Collections.sort(sortedVersions);
		for (final int version : sortedVersions) {
			if (version >= from && version <= to) {
				final File platformZip = new File(dir, "android-" + version + ".zip");
				if (!platformZip.exists()) {
					System.out.print("Downloading: \"" + repoUrl + "/" + urlMap.get(version) + "\" to \""
							+ platformZip.getAbsolutePath() + "\"... ");
					FileHelper.download(repoUrl + "/" + urlMap.get(version), platformZip);
					System.out.println("done");
				} else {
					System.out.println("Downloading: \"" + platformZip.getAbsolutePath() + "\"... already available!");
				}
			}
		}

		// Step 4
		System.out.println("\n4/4) Extracting...");
		for (final int version : sortedVersions) {
			if (version >= from && version <= to) {
				final File platformZip = new File(dir, "android-" + version + ".zip");
				final File platformJar = new File(dir, "android-" + version + "/android.jar");
				if (!platformJar.exists()) {
					System.out.print("Extracting: \"" + platformJar.getAbsolutePath() + "\"... ");
					platformJar.getParentFile().mkdirs();
					FileHelper.extractAndroidJar(platformZip, platformJar);
					System.out.println("done");
				} else {
					System.out.println("Extracting: \"" + platformJar.getAbsolutePath() + "\"... already available!");
				}
			}
		}
	}
}