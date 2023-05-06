package info.ata4.bspsrc.app.info.gui.models;

import info.ata4.bspsrc.app.info.BspFileUtils;
import info.ata4.bspsrc.app.info.gui.data.EmbeddedInfo;
import info.ata4.bspsrc.app.info.gui.data.GameLumpInfo;
import info.ata4.bspsrc.app.info.gui.data.LumpInfo;
import info.ata4.bspsrc.decompiler.modules.BspChecksum;
import info.ata4.bspsrc.decompiler.modules.BspCompileParams;
import info.ata4.bspsrc.decompiler.modules.BspDependencies;
import info.ata4.bspsrc.decompiler.modules.BspProtection;
import info.ata4.bspsrc.decompiler.modules.texture.TextureSource;
import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.lump.AbstractLump;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.log.LogUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BspInfoModel {

	private static final Logger L = LogUtils.getLogger();

	private final List<Runnable> listeners = new ArrayList<>();

	private BspFile bspFile;
	private BspData bspData;
	private BspCompileParams cparams;
	private BspProtection prot;
	private BspDependencies bspres;

	private Long fileCrc;
	private Long mapCrc;

	private List<LumpInfo> lumps = List.of();
	private List<GameLumpInfo> gameLumps = List.of();
	private List<EmbeddedInfo> embeddedInfos = List.of();

	public void load(Path filePath) throws IOException {
		bspFile = new BspFile();
		bspFile.load(filePath);

		int lumpSizeSum = bspFile.getLumps().stream()
				.mapToInt(AbstractLump::getLength)
				.sum();

		lumps = bspFile.getLumps().stream()
				.map(lump -> new LumpInfo(
						lump.getIndex(),
						lump.getName(),
						lump.getLength(),
						(int) Math.round(lump.getLength() * 100.0 / lumpSizeSum),
						lump.getVersion()
				))
				.toList();

		int gameLumpSizeSum = bspFile.getGameLumps().stream()
				.mapToInt(AbstractLump::getLength)
				.sum();

		gameLumps = bspFile.getGameLumps().stream()
				.map(lump -> new GameLumpInfo(
						lump.getName(),
						lump.getLength(),
						(int) Math.round(lump.getLength() * 100.0 / gameLumpSizeSum),
						lump.getVersion()
				))
				.toList();

		var bspReader = new BspFileReader(bspFile);
		bspReader.loadEntities();

		bspData = bspReader.getData();
		cparams = new BspCompileParams(bspReader);

		var texsrc = new TextureSource(bspReader);
		prot = new BspProtection(bspReader, texsrc);
		prot.check();

		bspres = new BspDependencies(bspReader);

		var checksum = new BspChecksum(bspReader);
		fileCrc = checksum.getFileCRC();
		mapCrc = checksum.getMapCRC();

		try (ZipFile zip = bspFile.getPakFile().getZipFile()) {
			var files = new ArrayList<EmbeddedInfo>();

			Enumeration<ZipArchiveEntry> enumeration = zip.getEntries();
			while (enumeration.hasMoreElements()) {
				ZipArchiveEntry ze = enumeration.nextElement();
				files.add(new EmbeddedInfo(ze.getName(), ze.getSize()));
			}

			embeddedInfos = files;
		} catch (IOException ex) {
			L.log(Level.WARNING, "Can't read pak");
		}

		listeners.forEach(Runnable::run);
	}

	public void extractLumps(Set<Integer> lumpIndices, Path lumpsDst) throws IOException {
		for (int lumpIndex : lumpIndices) {
			var lump = bspFile.getLumps().get(lumpIndex);
			BspFileUtils.extractLump(lump, lumpsDst);
		}
	}

	public void extractGameLumps(Set<Integer> lumpIndices, Path lumpsDst) throws IOException {
		for (int lumpIndex : lumpIndices) {
			var lump = bspFile.getGameLumps().get(lumpIndex);
			BspFileUtils.extractGameLump(lump, lumpsDst);
		}
	}

	public void extractEmbeddedFiles(Set<Integer> fileIndices, Path filesDst) throws IOException {

		// this is maybe a little bit weird of doing this, but i can't be bothered
		// to change the PakFile api

		var fileNames = new ArrayList<String>();
		try (ZipFile zip = bspFile.getPakFile().getZipFile()) {
			Enumeration<ZipArchiveEntry> enumeration = zip.getEntries();
			for (int i = 0; enumeration.hasMoreElements(); i++) {
				ZipArchiveEntry ze = enumeration.nextElement();

				if (fileIndices.contains(i))
					fileNames.add(ze.getName());
			}
		}

		bspFile.getPakFile().unpack(filesDst, fileNames::contains);
	}

	public void extractEmbeddedFilesRaw(Path filesDst) throws IOException {
		bspFile.getPakFile().unpack(filesDst, true);
	}

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}

	public Optional<BspFile> getBspFile() {
		return Optional.ofNullable(bspFile);
	}
	public Optional<BspData> getBspData() {
		return Optional.ofNullable(bspData);
	}
	public Optional<BspCompileParams> getCparams() {
		return Optional.ofNullable(cparams);
	}
	public Optional<BspProtection> getProt() {
		return Optional.ofNullable(prot);
	}
	public Optional<BspDependencies> getBspres() {
		return Optional.ofNullable(bspres);
	}
	public Optional<Long> getFileCrc() {
		return Optional.ofNullable(fileCrc);
	}
	public Optional<Long> getMapCrc() {
		return Optional.ofNullable(mapCrc);
	}
	public List<LumpInfo> getLumps() {
		return lumps;
	}
	public List<GameLumpInfo> getGameLumps() {
		return gameLumps;
	}
	public List<EmbeddedInfo> getEmbeddedInfos() {
		return embeddedInfos;
	}
}
