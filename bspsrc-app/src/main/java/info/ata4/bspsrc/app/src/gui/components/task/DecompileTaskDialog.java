package info.ata4.bspsrc.app.src.gui.components.task;

import com.formdev.flatlaf.FlatClientProperties;
import info.ata4.bspsrc.app.src.gui.data.ErrorNotification;
import info.ata4.bspsrc.app.src.gui.data.Task;
import info.ata4.bspsrc.app.src.gui.models.DecompileTaskModel;
import info.ata4.bspsrc.app.util.swing.GuiUtil;
import info.ata4.bspsrc.app.util.swing.model.ReadonlyListTableModel;
import info.ata4.bspsrc.app.util.swing.renderer.ErrorNotificationCellRenderer;
import info.ata4.bspsrc.app.util.swing.renderer.NoFocusProxyCellRenderer;
import info.ata4.bspsrc.app.util.swing.renderer.PathCellRenderer;
import info.ata4.bspsrc.app.util.swing.renderer.StateCellRender;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static javax.swing.ScrollPaneConstants.*;

public class DecompileTaskDialog extends JDialog {

	private final DecompileTaskModel model;

	private final ReadonlyListTableModel<Task> tableModel = new ReadonlyListTableModel<>(List.of(
			new ReadonlyListTableModel.Column<>("", Task.State.class, Task::state),
			new ReadonlyListTableModel.Column<>("Bsp", Path.class, Task::bspFile)
	));
	private final JTable tblTasks = new JTable() {{
		setModel(tableModel);
		setAutoCreateRowSorter(true);
		getTableHeader().setReorderingAllowed(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		setRowHeight(25); // make rows a little bigger
		setPreferredScrollableViewportSize(new Dimension(150, 0));

		setDefaultRenderer(Task.State.class, new NoFocusProxyCellRenderer(new StateCellRender()));
		setDefaultRenderer(Path.class, new NoFocusProxyCellRenderer(new PathCellRenderer()));

		GuiUtil.setColumnWidth(this, 0, Task.State.FINISHED, true, true);

		getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting())
				return;

			int row = convertRowIndexToModel(getSelectedRow());
			selectTask(row);
		});
	}};

	private final JTextArea txtaLog = new JTextArea(30, 100) {{
		putClientProperty(FlatClientProperties.STYLE, "inactiveBackground: @componentBackground");
		setFont(new Font("Consolas", Font.PLAIN, 12));
		setEditable(false);
	}};

	private final DefaultListModel<ErrorNotification> lstNotificationsModel = new DefaultListModel<>();
	private final JList<ErrorNotification> lstNotifications = new JList<>() {{
		setModel(lstNotificationsModel);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setCellRenderer(new ErrorNotificationCellRenderer());

		getSelectionModel().addListSelectionListener(e -> {
			if (e.getValueIsAdjusting())
				return;

			selectTask(getSelectedValue().taskIndex());
		});
	}};

	private final JButton btnDialog = new JButton() {{
		addActionListener(e -> {
			DecompileTaskDialog.this.model.close();
			DecompileTaskDialog.this.dispose();
		});
	}};

	public DecompileTaskDialog(Window owner, DecompileTaskModel model) {
		super(owner);

		setTitle("BSPSource output");

		this.model = requireNonNull(model);
		model.addStateListener(this::onStateChange);
		model.addTaskUpdateListener(i -> onDataChange());
		model.addNotificationListener(lstNotificationsModel::addElement);

		onStateChange(model.getState());
		onDataChange();

		JPanel pnl = new JPanel(new MigLayout(
				"",
				"[grow]",
				"[grow||]"
		));

		var scrlTasks = new JScrollPane(tblTasks, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		var scrlLog = new JScrollPane(txtaLog, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		var spltUpper = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrlTasks, scrlLog);
		spltUpper.setResizeWeight(0.2);

		var scrlInfos = new JScrollPane(lstNotifications, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		var splt = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT,
				model.getTasks().size() == 1 ? scrlLog : spltUpper, // hide scrlTask if there is only one task
				scrlInfos
		);
		splt.setDividerLocation(Integer.MAX_VALUE);
		splt.setResizeWeight(0.8);

		pnl.add(splt, "grow, wrap");
		pnl.add(new JSeparator(), "grow, wrap");
		pnl.add(btnDialog, "right");
		setContentPane(pnl);

		lstNotificationsModel.addListDataListener(new ListDataListener() {
			private void updateSplitPane() {
				if (lstNotificationsModel.isEmpty())
					splt.setDividerLocation(Integer.MAX_VALUE);
				else
					splt.setDividerLocation(-1);
			}

			@Override public void intervalAdded(ListDataEvent e) { updateSplitPane(); }
			@Override public void intervalRemoved(ListDataEvent e) { updateSplitPane(); }
			@Override public void contentsChanged(ListDataEvent e) {}
		});

		if (tblTasks.getRowCount() > 0)
			tblTasks.setRowSelectionInterval(0, 0); // default select first task

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				model.close();
			}
		});

		pack();
		setMinimumSize(getSize());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	private void onStateChange(DecompileTaskModel.State state) {
		String text;
		if (state instanceof DecompileTaskModel.State.Running)
			text = "Cancel";
		else if (state instanceof DecompileTaskModel.State.Finished)
			text = "Close";
		else
			throw new RuntimeException("Not reachable");

		btnDialog.setText(text);
	}

	private void onDataChange() {
		if (tableModel.getRowCount() == 0)
			tableModel.setData(model.getTasks());
		else // only update if data is already there to preserve row selection
			tableModel.updateData(model.getTasks());
	}

	private void selectTask(int index) {
		tblTasks.setRowSelectionInterval(index, index);
		Document logDoc = model.getTaskLog(index);
		txtaLog.setDocument(logDoc);
	}
}
