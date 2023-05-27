package info.ata4.bspsrc.app.src.cli;

import info.ata4.bspsrc.app.util.log.Log4jUtil;
import info.ata4.bspsrc.common.util.AlphanumComparator;
import info.ata4.bspsrc.decompiler.BspFileEntry;
import info.ata4.bspsrc.decompiler.BspSource;
import info.ata4.bspsrc.decompiler.BspSourceConfig;
import info.ata4.bspsrc.decompiler.modules.geom.BrushMode;
import info.ata4.bspsrc.decompiler.util.SourceFormat;
import info.ata4.bspsrc.lib.app.SourceAppDB;
import info.ata4.io.util.PathUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static info.ata4.bspsrc.common.util.JavaUtil.zip;
import static picocli.CommandLine.*;

@Command(
		name = "bspsrc",
		version = {
				"BSPSource " + BspSource.VERSION,
				"",
				"Based on VMEX v0.98g by Rof <rof@mellish.org.uk>",
				"Extended and modified by Nico Bergemann <barracuda415@yahoo.de>"
		},
		parameterListHeading = "%nParameters:%n",
		optionListHeading = "%nOptions:%n",
		abbreviateSynopsis = true,
		mixinStandardHelpOptions = true,
		sortOptions = false,
		sortSynopsis = false,
		showDefaultValues = true
)
public class BspSourceCliCommand implements Callable<Void> {

	private static final Logger L = LogManager.getLogger();
	private static final BspSourceConfig INITIAL_CONFIG = new BspSourceConfig();

	@Option(names = "--appids", description = "List all available application IDs", help = true)
	private boolean listAppIds;
	@Option(names = { "-d", "--debug" }, description = "Enable debug mode. Increases verbosity and adds additional data to the VMF file.")
	private boolean debug;
	@Option(names = { "-r", "--recursive" }, description = "Recursively decompile files found in subdirectories.")
	private boolean recursive;
	@Option(names = { "-o", "--output" }, description = "Override output path for VMF file(s). Treated as directory if multiple BSP files are provided.", paramLabel = "<path>")
	private Path outputPath;
	@Option(names = { "-l", "--list" }, description = "Treat specified files as text files containing a BSP file list. BSP files are seperated by new lines.")
	private boolean useFileLists;
	@Parameters(
			description = {
					"One or more bsp files or folders.",
					"Alternatively, if --list is specified, one or more text files containing a list of bsp file or folder paths."
			},
			arity = "1"
	)
	private List<Path> paths;

	// entity options
	@ArgGroup(validate = false, heading = "%nEntity related options%n")
	private EntityOpts entityOpts = new EntityOpts();
	static class EntityOpts {
		@Option(names = "--no_point_ents", description = "Don't write any point entities.")
		private boolean noPointEnts;
		@Option(names = "--no_brush_ents", description = "Don't write any brush entities.")
		private boolean noBrushEnts;
		@Option(names = "--no_sprp", description = "Don't write prop_static entities.")
		private boolean noStaticPropEnts;
		@Option(names = "--no_overlays", description = "Don't write info_overlay entities.")
		private boolean noOverlayEnts;
		@Option(names = "--no_cubemaps", description = "Don't write env_cubemap entities.")
		private boolean noCubemapEnts;
		@Option(names = "--no_details", description = "Don't write func_detail entities.")
		private boolean noDetailEnts;
		@Option(names = "--no_areaportals", description = "Don't write func_areaportal(_window) entities.")
		private boolean noAreaportalEnts;
		@Option(names = "--no_occluders", description = "Don't write func_occluder entities.")
		private boolean noOccluderEnts;
		@Option(names = "--no_ladders", description = "Don't write func_ladder entities.")
		private boolean noLadderEnts;
		@Option(names = "--no_rotfix", description = "Don't fix instance entity brush rotations for Hammer.")
		private boolean noInstanceEntityRotationFix;
	}

	// entity mapping
	private EntityMappingOptions entityMappingOptions = new EntityMappingOptions();
	private static class EntityMappingOptions {
		@Option(names = "--force_manual_areaportal", description = "Force manual entity mapping for areaportal entities.")
		private boolean forceAreaportalManualEntMapping;
		@Option(names = "--force_manual_occluder", description = "Force manual entitiy mapping for occluder entities.")
		private boolean forceOccluderManualEntMapping;
	}

	// world brush options
	@ArgGroup(validate = false, heading = "%nBrush related options%n")
	private BrushOptions brushOptions = new BrushOptions();
	private static class BrushOptions {
		@Option(names = "--no_brushes", description = "Don't write any world brushes.")
		private boolean noBrushes;
		@Option(names = "--no_disps", description = "Don't write displacement surfaces.")
		private boolean noDisplacements;
		@Option(names = "--brushmode", description = {
				"Brush decompiling mode:",
				"${BrushMode.BRUSHPLANES.name()} - brushes and planes",
				"${BrushMode.ORIGFACE.name()} - original faces only",
				"${BrushMode.ORIGFACE_PLUS.name()} - original + split faces",
				"${BrushMode.SPLITFACE.name()} - split faces only"
		}, paramLabel = "<mode>")
		private BrushMode brushMode = INITIAL_CONFIG.brushMode;
		@Option(names = "--thickness", description = "Thickness of brushes create from flat faces in units.", paramLabel = "<value>")
		private float thickness = INITIAL_CONFIG.backfaceDepth;
	}

	// texture options
	@ArgGroup(validate = false, heading = "%nTexture related options%n")
	private TextureOptions textureOptions = new TextureOptions();
	private static class TextureOptions {
		@Option(names = "--facetex", description = "Replace all face textures with this one.", paramLabel = "<texture>")
		private String faceTex = INITIAL_CONFIG.faceTexture;
		@Option(names = "--bfacetex", description = "Replace all back-face textures with this one. Used in face-based decompiling modes only.", paramLabel = "<texture>")
		private String backFaceTex = INITIAL_CONFIG.backfaceTexture;
	}

	// miscellaneous options
	@ArgGroup(validate = false, heading = "%nMiscellaneous options%n")
	private MiscellaneousOptions miscellaneousOptions = new MiscellaneousOptions();
	private static class MiscellaneousOptions {
		@Option(names = "--no_vmf", description = "Don't write any VMF files, read BSP only.")
		private boolean noVmf;
		@Option(names = "--no_lumpfiles", description = "Don't load lump files (.lmp) associated with the BSP file.")
		private boolean noLumpFiles;
		@Option(names = "--no_prot", description = "Skip decompiling protection checking. Can increase speed when mass-decompiling unprotected maps.")
		private boolean noProt;
		@Option(names = "--no_visgroups", description = "Don't group entities from instances into visgroups.")
		private boolean noVisGroups;
		@Option(names = "--no_cams", description = "Don't create Hammer cameras above each player spawn.")
		private boolean noCams;
		@Option(names = "--appid", description = {
				"Overrides game detection by using this Steam Application ID instead",
				"Use -appids to list all known app-IDs."
		}, paramLabel = "<id>")
		private int appId;
		@Option(names = "--format", description = {
				"Sets the VMF format used for the decompiled maps:",
				"AUTO - Automatic",
				"OLD - Source 2004 to 2009",
				"NEW - Source 2010 and later",
		}, paramLabel = "<format>")
		private SourceFormat sourceFormat = INITIAL_CONFIG.sourceFormat;
		@Option(names = "--unpack_embedded", description = "Unpack embedded files in the bsp.")
		private boolean unpackEmbedded;
		@Option(names = "--no_smart_unpack", description = {
				"Disable 'smart' extracting of embedded files.",
				"Smart extracting automatically skips all files generated by vbsp, that are only relevant to running the map in the engine."
		})
		private boolean noSmartUnpack;
	}

	@Override
	public Void call() throws IOException {
		if (debug) {
			Log4jUtil.setRootLevel(Level.DEBUG);
			L.debug("Debug mode on, verbosity set to maximum");
		}

		if (listAppIds) {
			System.out.printf("%8s  %s\n", "ID", "Name");
			SourceAppDB.getInstance().getAppList().entrySet().stream()
					.sorted(Map.Entry.comparingByValue(AlphanumComparator.COMPARATOR))
					.forEachOrdered(entry -> System.out.printf("%8d  %s\n", entry.getKey(), entry.getValue()));

			return null;
		}

		BspSourceConfig config = getConfig();
		List<BspFileEntry> entries = new ArrayList<>(getEntries());
		if (entries.isEmpty()) {
			L.error("No BSP file(s) specified");
			return null;
		}

		var bspsrc = new BspSource(config, entries);

		var map = StreamSupport.stream(zip(bspsrc.getEntryUuids(), entries).spliterator(), false)
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> PathUtils.setExtension(entry.getValue().getVmfFile(), "log")));

		try (var scope = Log4jUtil.configureDecompilationLogFileAppender(map)) {
			bspsrc.run(createBspSourceListener(entries));
		}

		return null;
	}

	private BspSource.Listener createBspSourceListener(List<BspFileEntry> entries) {
		return new BspSource.Listener() {
			@Override
			public void onStarted(int entryIndex) {}

			@Override
			public void onFinished(int entryIndex, Set<BspSource.Warning> warnings) {
				Path bspFile = entries.get(entryIndex).getBspFile();
				if (warnings.isEmpty()) {
					L.info("'{}': Decompiled successfully.", bspFile);
				} else {
					L.warn(
							"'{}': Decompiled with warnings: {}. For more details see the log file.",
							bspFile,
							warnings.stream()
									.map(warning -> switch (warning) {
										case ExtractEmbedded -> "Error occurred extracting embedded files";
										case LoadNmo -> "Error occurred loading nmo file";
										case WriteNmos -> "Error occurred writing nmos file";
									})
									.collect(Collectors.joining(". "))
					);
				}
			}

			@Override
			public void onFailed(int entryIndex, Throwable exception) {
				Path bspFile = entries.get(entryIndex).getBspFile();
				L.error("'%s': Failed with exception:".formatted(bspFile), exception);
			}
		};
	}

	private BspSourceConfig getConfig() {
		var config = new BspSourceConfig();

		config.debug = debug;

		// entity options
		config.writePointEntities = !entityOpts.noPointEnts;
		config.writeBrushEntities = !entityOpts.noBrushEnts;
		config.writeStaticProps = !entityOpts.noStaticPropEnts;
		config.writeOverlays = !entityOpts.noOverlayEnts;
		config.writeCubemaps = !entityOpts.noCubemapEnts;
		config.writeDetails = !entityOpts.noDetailEnts;
		config.writeAreaportals = !entityOpts.noAreaportalEnts;
		config.writeOccluders = !entityOpts.noOccluderEnts;
		config.writeLadders = !entityOpts.noLadderEnts;
		config.fixEntityRot = !entityOpts.noInstanceEntityRotationFix;

		// entity mapping
		config.apForceManualMapping = entityMappingOptions.forceAreaportalManualEntMapping;
		config.occForceManualMapping = entityMappingOptions.forceOccluderManualEntMapping;

		// world brush options
		config.writeWorldBrushes = !brushOptions.noBrushes;
		config.writeDisp = !brushOptions.noDisplacements;
		config.brushMode = brushOptions.brushMode;
		config.backfaceDepth = brushOptions.thickness;

		// texture options
		config.faceTexture = textureOptions.faceTex;
		config.backfaceTexture = textureOptions.backFaceTex;

		// miscellaneous options
		config.nullOutput = miscellaneousOptions.noVmf;
		config.loadLumpFiles = !miscellaneousOptions.noLumpFiles;
		config.skipProt = miscellaneousOptions.noProt;
		config.writeVisgroups = !miscellaneousOptions.noVisGroups;
		config.writeCameras = !miscellaneousOptions.noCams;
		config.defaultAppId = miscellaneousOptions.appId;
		config.sourceFormat = miscellaneousOptions.sourceFormat;
		config.unpackEmbedded = miscellaneousOptions.unpackEmbedded;
		config.smartUnpack = !miscellaneousOptions.noSmartUnpack;

		return config;
	}

	private Set<BspFileEntry> getEntries() throws IOException {
		List<Path> bspPaths;
		if (useFileLists) {
			// it could be so easy...
//			bspPaths = paths.stream()
//					.flatMap(path -> Files.readAllLines(path).stream())
//					.map(Path::of)
//					.collect(Collectors.toSet());

			// but instead
			var bspPathsTmp = new ArrayList<Path>();
			for (Path path : paths)
			{
				Files.readAllLines(path).stream()
						.map(Path::of)
						.forEach(bspPathsTmp::add);
			}
			bspPaths = bspPathsTmp;
		} else {
			bspPaths = paths;
		}

		var fileSet = new HashSet<BspFileEntry>();
		for (Path path : bspPaths) {
			if (Files.isDirectory(path)) {
				PathMatcher bspPathMatcher = path.getFileSystem().getPathMatcher("glob:**.bsp");
				try (Stream<Path> pathStream = Files.walk(path, recursive ? Integer.MAX_VALUE : 0)) {
					pathStream
							.filter(Files::isRegularFile)
							.filter(bspPathMatcher::matches)
							.map(filePath -> new BspFileEntry(filePath, outputPath))
							.forEach(fileSet::add);
				}
			} else {
				fileSet.add(new BspFileEntry(path, outputPath));
			}
		}

		return fileSet;
	}
}
