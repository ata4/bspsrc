package info.ata4.bspsrc.app.util;

import java.awt.*;
import java.util.function.Consumer;

public class GridBagConstraintsBuilder {
	private final GridBagConstraints constraints;

	public GridBagConstraintsBuilder() {
		this(new GridBagConstraints());
	}

	private GridBagConstraintsBuilder(GridBagConstraints constraints) {
		this.constraints = constraints;
	}

	public GridBagConstraintsBuilder position(int x, int y) {
		return inferBuilder(constraints -> {
			constraints.gridx = x;
			constraints.gridy = y;
		});
	}

	public GridBagConstraintsBuilder anchor(Anchor anchor) {
		return inferBuilder(constraints -> {
			constraints.anchor = anchor.id;
		});
	}

	public GridBagConstraintsBuilder width(int width) {
		return inferBuilder(constraints -> {
			constraints.gridwidth = width;
		});
	}
	public GridBagConstraintsBuilder height(int height) {
		return inferBuilder(constraints -> {
			constraints.gridheight = height;
		});
	}

	public GridBagConstraintsBuilder weightX(int weight) {
		return inferBuilder(constraints -> {
			constraints.weightx = weight;
		});
	}
	public GridBagConstraintsBuilder weightY(int weight) {
		return inferBuilder(constraints -> {
			constraints.weighty = weight;
		});
	}

	public GridBagConstraintsBuilder fill(Fill fill) {
		return inferBuilder(constraints -> {
			constraints.fill = fill.id;
		});
	}

	public GridBagConstraintsBuilder insets(Insets insets) {
		return inferBuilder(constraints -> {
			constraints.insets = insets;
		});
	}

	public GridBagConstraints build() {
		return constraints;
	}

	private GridBagConstraintsBuilder inferBuilder(Consumer<GridBagConstraints> action) {
		var constraints = (GridBagConstraints) this.constraints.clone();
		action.accept(constraints);
		return new GridBagConstraintsBuilder(constraints);
	}

	public enum Anchor {
		CENTER(GridBagConstraints.CENTER),
		NORTH(GridBagConstraints.NORTH),
		NORTHEAST(GridBagConstraints.NORTHEAST),
		EAST(GridBagConstraints.EAST),
		SOUTHEAST(GridBagConstraints.SOUTHEAST),
		SOUTH(GridBagConstraints.SOUTH),
		SOUTHWEST(GridBagConstraints.SOUTHWEST),
		WEST(GridBagConstraints.WEST),
		NORTHWEST(GridBagConstraints.NORTHWEST),

		PAGE_START(GridBagConstraints.PAGE_START),
		PAGE_END(GridBagConstraints.PAGE_END),
		LINE_START(GridBagConstraints.LINE_START),
		LINE_END(GridBagConstraints.LINE_END),
		FIRST_LINE_START(GridBagConstraints.FIRST_LINE_START),
		FIRST_LINE_END(GridBagConstraints.FIRST_LINE_END),
		LAST_LINE_START(GridBagConstraints.LAST_LINE_START),
		LAST_LINE_END(GridBagConstraints.LAST_LINE_END),

		BASELINE(GridBagConstraints.BASELINE),
		BASELINE_LEADING(GridBagConstraints.BASELINE_LEADING),
		BASELINE_TRAILING(GridBagConstraints.BASELINE_TRAILING),
		ABOVE_BASELINE(GridBagConstraints.ABOVE_BASELINE),
		ABOVE_BASELINE_LEADING(GridBagConstraints.ABOVE_BASELINE_LEADING),
		ABOVE_BASELINE_TRAILING(GridBagConstraints.ABOVE_BASELINE_TRAILING),
		BELOW_BASELINE(GridBagConstraints.BELOW_BASELINE),
		BELOW_BASELINE_LEADING(GridBagConstraints.BELOW_BASELINE_LEADING),
		BELOW_BASELINE_TRAILING(GridBagConstraints.BELOW_BASELINE_TRAILING);

		public final int id;

		Anchor(int id) {
			this.id = id;
		}
	}

	public enum Fill {
		NONE(GridBagConstraints.NONE),
		HORIZONTAL(GridBagConstraints.HORIZONTAL),
		VERTICAL(GridBagConstraints.VERTICAL),
		BOTH(GridBagConstraints.BOTH);

		public final int id;

		Fill(int id) {
			this.id = id;
		}
	}
}
