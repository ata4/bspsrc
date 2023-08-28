package info.ata4.bspsrc.app.util.swing.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import info.ata4.bspsrc.app.src.gui.BspSourceGui;

public class Icons {
	public static final FlatSVGIcon PENDING_ICON = new FlatSVGIcon(BspSourceGui.class.getResource("svg/pending.svg"));
	public static final FlatSVGIcon SUCCESS_ICON = new FlatSVGIcon(BspSourceGui.class.getResource("svg/successDialog.svg"));
	public static final FlatSVGIcon WARNING_ICON = new FlatSVGIcon(BspSourceGui.class.getResource("svg/warningDialog.svg"));
	public static final FlatSVGIcon FAILED_ICON = new FlatSVGIcon(BspSourceGui.class.getResource("svg/errorDialog.svg"));
}
