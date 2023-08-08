package info.ata4.bspsrc.lib.app;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class SourceAppBuilder {

	private String name;
	private int appId;
	private int versionMin = -1;
	private int versionMax = -1;
	private Pattern filePattern = null;
	private Set<String> entities = Collections.emptySet();
	private float pointsEntities = 20;
	private float pointsFilePattern = 3;

	public SourceAppBuilder setName(String name) {
		this.name = requireNonNull(name);
		return this;
	}

	public SourceAppBuilder setAppId(int appId) {
		this.appId = appId;
		return this;
	}

	public SourceAppBuilder setVersion(int version) {
		this.versionMin = version;
		this.versionMax = version;
		return this;
	}

	public SourceAppBuilder setVersionRange(int versionMin, int versionMax) {
		this.versionMin = versionMin;
		this.versionMax = versionMax;
		return this;
	}

	public SourceAppBuilder setFilePattern(Pattern filePattern) {
		this.filePattern = filePattern;
		return this;
	}

	public SourceAppBuilder setEntities(String... entities) {
		this.entities = new HashSet<>(Arrays.asList(entities));
		return this;
	}

	public SourceAppBuilder setPointsEntities(float pointsEntities) {
		this.pointsEntities = pointsEntities;
		return this;
	}

	public SourceAppBuilder setPointsFilePattern(float pointsFilePattern) {
		this.pointsFilePattern = pointsFilePattern;
		return this;
	}

	public SourceApp build() {
		return new SourceApp(
				name,
				appId,
				versionMin,
				versionMax,
				filePattern,
				entities,
				pointsEntities,
				pointsFilePattern
		);
	}
}