package com.github.amr.mimetypes;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility registry of mime types, with lookups by mime type and by file
 * extensions.
 *
 * The constructors, factory methods and load methods are not thread safe,
 * the exception to this is the {@link #getInstance()} method. BLookup methods
 * ({@link #getByType(String)} and {@link #getByExtension(String)}) are
 * thread-safe. Therefore, once initialized, instances may be used concurrently
 * by multiple threads.
 */
public class MimeTypes {
	private static final String COMMENT_PREFIX = "#";

	private final Map<String, MimeType> mimeTypes = new HashMap<>();
	private final Map<String, MimeType> extensions = new HashMap<>();

	private static MimeTypes singleton = null;
	private final static Object singletoneMonitor = new Object();

	public MimeTypes() {
		this(getDefaultMimeTypesDefinition());
	}

	/**
	 * Get path to the default included mime types definition file.
	 *
	 * @return Standard path to the included mime types definitions
	 */
	public static Path getDefaultMimeTypesDefinition() {
		URL defaultDefinition = MimeTypes.class.getClassLoader().getResource("mime.types");
		if (defaultDefinition == null) {
			throw new IllegalStateException("Could not find the built-in mime.types definition file");
		}

		try {
			return Paths.get(defaultDefinition.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error occurred while initializing from the default mime types definitions, this is a bug", e);
		}
	}

	/**
	 * Create a new instance not initialized with any mime types definitions.
	 *
	 * @return New blank instance
	 */
	public static MimeTypes blank() {
		return new MimeTypes(new Path[0]);
	}

	/**
	 * Initialize the mime types definitions with given one or more mime
	 * types definition files in standard /etc/mime.types format.
	 *
	 * @param mimeTypesDefinitions Paths to mime types definition files
	 */
	public MimeTypes(Path... mimeTypesDefinitions) {
		for (Path f : mimeTypesDefinitions) {
			load(f);
		}
	}

	/**
	 * Get the default instance which is initialized with the built-in mime
	 * types definitions on the first access to this method.
	 *
	 * This is thread-safe.
	 *
	 * @return default singleton instance with built-in mime types definitions
	 */
	public static MimeTypes getInstance() {
		if (singleton == null) {
			synchronized (singletoneMonitor) {
				if (singleton == null) {
					singleton = new MimeTypes();
				}
			}
		}

		return singleton;
	}

	/**
	 * Parse and register mime type definitions from given path.
	 *
	 * @param def Path of mime type definitions file to load and register
	 */
	public void load(Path def) {
		try {
			for (String line : Files.readAllLines(def, StandardCharsets.US_ASCII)) {
				loadOne(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Load and register a single line that starts with the mime type proceeded
	 * by any number of whitespaces, then a whitespace separated list of
	 * valid extensions for that mime type.
	 *
	 * @param def Single mime type definition to load and register
	 */
	public void loadOne(String def) {
		if (def.startsWith(COMMENT_PREFIX)) {
			return;
		}

		String[] halfs = def.toLowerCase().split("\\s", 2);

		MimeType mimeType = new MimeType(halfs[0], halfs[1].trim().split("\\s"));
		register(mimeType);
	}

	/**
	 * Register the given {@link MimeType} so it can be looked up later by mime
	 * type and/or extension.
	 *
	 * @param mimeType MimeType instance to register
	 */
	public void register(MimeType mimeType) {
		mimeTypes.put(mimeType.getMimeType(), mimeType);
		for (String ext : mimeType.getExtensions()) {
			extensions.put(ext, mimeType);
		}
	}

	/**
	 * Get a @{link MimeType} instance for the given mime type identifier from
	 * the loaded mime type definitions.
	 *
	 * @param mimeType lower-case mime type identifier string
	 * @return Instance of MimeType for the given mime type identifier or null
	 * if none was found
	 */
	public MimeType getByType(String mimeType) {
		return mimeTypes.get(mimeType);
	}

	/**
	 * Get a @{link MimeType} instance for the given extension from the loaded
	 * mime type definitions.
	 *
	 * @param extension lower-case extension
	 * @return Instance of MimeType for the given ext or null if none was found
	 */
	public MimeType getByExtension(String extension) {
		return extensions.get(extension);
	}
}
