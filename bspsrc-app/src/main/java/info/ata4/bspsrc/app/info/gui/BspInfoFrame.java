/*
 ** 2012 May 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspsrc.app.info.gui;

import info.ata4.bspsrc.app.info.gui.models.EmbeddedTableModel;
import info.ata4.bspsrc.app.info.gui.models.EntityTableModel;
import info.ata4.bspsrc.app.info.gui.models.GameLumpTableModel;
import info.ata4.bspsrc.app.info.gui.models.LumpTableModel;
import info.ata4.bspsrc.app.info.log.DialogHandler;
import info.ata4.bspsrc.app.util.FileDrop;
import info.ata4.bspsrc.app.util.FileExtensionFilter;
import info.ata4.bspsrc.app.util.components.ByteSizeCellRenderer;
import info.ata4.bspsrc.app.util.components.DecimalFormatCellRenderer;
import info.ata4.bspsrc.app.util.components.ProgressCellRenderer;
import info.ata4.bspsrc.decompiler.BspSource;
import info.ata4.bspsrc.decompiler.modules.BspChecksum;
import info.ata4.bspsrc.decompiler.modules.BspCompileParams;
import info.ata4.bspsrc.decompiler.modules.BspDependencies;
import info.ata4.bspsrc.decompiler.modules.BspProtection;
import info.ata4.bspsrc.decompiler.modules.texture.TextureSource;
import info.ata4.bspsrc.lib.BspFile;
import info.ata4.bspsrc.lib.BspFileFilter;
import info.ata4.bspsrc.lib.BspFileReader;
import info.ata4.bspsrc.lib.app.SourceAppDB;
import info.ata4.bspsrc.lib.entity.Entity;
import info.ata4.bspsrc.lib.lump.LumpType;
import info.ata4.bspsrc.lib.struct.BspData;
import info.ata4.log.LogUtils;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspInfoFrame extends javax.swing.JFrame {

    private static final Logger L = LogUtils.getLogger();

    public static final String NAME = "BSPInfo";
    public static final String VERSION = BspSource.VERSION;

    private File currentFile;
    private BspFile bspFile;
    private BspFileReader bspReader;
    private FileDrop fdrop;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        LogUtils.configure();

        // set the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            L.warning("Failed to set SystemLookAndFeel");
        }

        // create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BspInfoFrame().setVisible(true);
            }
        });
    }

    /**
     * Creates new form BspToolFrame
     */
    public BspInfoFrame() {
        initComponents();
        initComponentsCustom();

        // init file dropper
        fdrop = new FileDrop(this, files -> {
		    if (new BspFileFilter().accept(files[0])) {
		        loadFile(files[0]);
		    }
		});

        // add dialog log handler
        L.addHandler(new DialogHandler(this));
    }

    public final void reset() {
        // general
        textFieldName.setText(null);
        textFieldVersion.setText(null);
        textFieldRevision.setText(null);
        textFieldCompressed.setText(null);
        textFieldEndian.setText(null);
        textFieldAppID.setText(null);
        textFieldGame.setText(null);
        linkLabelAppURL.setText(null);
        textFieldFileCRC.setText(null);
        textFieldMapCRC.setText(null);

        textFieldVbspParams.setText(null);
        textFieldVvisParams.setText(null);
        textFieldVradParams.setText(null);

        // protection
        checkBoxVmexEntity.setSelected(false);
        checkBoxVmexTexture.setSelected(false);
        checkBoxVmexBrush.setSelected(false);

        checkBoxIIDObfs.setSelected(false);
        checkBoxIIDTexHack.setSelected(false);

        checkBoxBSPProtect.setSelected(false);

        // lumps
        tableLumps.setModel(new LumpTableModel());

        // entities
        textFieldTotalEnts.setText(null);
        textFieldBrushEnts.setText(null);
        textFieldPointEnts.setText(null);

        tableEntities.setModel(new EntityTableModel());

        // dependencies
        textAreaMaterials.setText(null);
        textAreaSounds.setText(null);
        textAreaSoundScripts.setText(null);
        textAreaSoundscapes.setText(null);
        textAreaModels.setText(null);
        textAreaParticles.setText(null);

        // embedded files
        tableEmbedded.setModel(new EmbeddedTableModel());

        // disable buttons
        extractLumpButton.setEnabled(false);
        extractAllLumpsButton.setEnabled(false);

        extractGameLumpButton.setEnabled(false);
        extractAllGameLumpsButton.setEnabled(false);

        extractEmbeddedButton.setEnabled(false);
        extractAllEmbeddedButton.setEnabled(false);
        extractEmbeddedZipButton.setEnabled(false);
    }

    public void loadFile(File file) {
        currentFile = file;

        setTitle(NAME + " " + VERSION + " - " + file.getName());

        new Thread(new Runnable() {
            @Override
            public void run() {
                // clear form fields
                reset();

                // set waiting cursor
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                try {
                    // load BSP file
                    bspFile = new BspFile();
                    bspFile.load(currentFile.toPath());

                    boolean compressed = bspFile.isCompressed();

                    bspReader = new BspFileReader(bspFile);
                    bspReader.loadEntities();

                    BspData data = bspReader.getData();

                    // general
                    textFieldName.setText(bspFile.getName());
                    textFieldVersion.setText(String.valueOf(bspFile.getVersion()));
                    textFieldRevision.setText(String.valueOf(bspFile.getRevision()));
                    textFieldCompressed.setText(compressed ? "Yes" : "No");
                    textFieldEndian.setText(bspFile.getByteOrder() == ByteOrder.LITTLE_ENDIAN ? "Little endian" : "Big endian");

                    if (data.entities != null && !data.entities.isEmpty()) {
                        Entity worldspawn = data.entities.get(0);
                        textFieldComment.setText(worldspawn.getValue("comment"));
                    }

                    int appId = bspFile.getAppId();

                    textFieldAppID.setText(appId > 0 ? String.valueOf(appId) : "n/a");
                    textFieldGame.setText(SourceAppDB.getInstance().getName(appId).orElse("Unknown"));

                    URI steamStoreURI = SourceAppDB.getSteamStoreURI(appId);

                    if (steamStoreURI != null) {
                        linkLabelAppURL.setURI("Steam store link", steamStoreURI);
                    }

                    BspCompileParams cparams = new BspCompileParams(bspReader);

                    textFieldVbspParams.setText(String.join(" ", cparams.getVbspParams()));

                    if (cparams.isVvisRun()) {
                        textFieldVvisParams.setText(String.join(" ", cparams.getVvisParams()));
                    } else {
                        textFieldVvisParams.setText("(not run)");
                    }

                    if (cparams.isVradRun()) {
                        textFieldVradParams.setText(String.join(" ", cparams.getVradParams()));
                    } else {
                        textFieldVradParams.setText("(not run)");
                    }

                    // protection
                    TextureSource texsrc = new TextureSource(bspReader);
                    BspProtection prot = new BspProtection(bspReader, texsrc);
                    prot.check();

                    checkBoxVmexEntity.setSelected(prot.hasEntityFlag());
                    checkBoxVmexTexture.setSelected(prot.hasTextureFlag());
                    checkBoxVmexBrush.setSelected(prot.hasBrushFlag());

                    checkBoxIIDObfs.setSelected(prot.hasObfuscatedEntities());
                    checkBoxIIDTexHack.setSelected(prot.hasModifiedTexinfo());

                    checkBoxBSPProtect.setSelected(prot.hasEncryptedEntities());

                    // lumps
                    tableLumps.setModel(new LumpTableModel(bspFile));

                    // game lumps
                    tableGameLumps.setModel(new GameLumpTableModel(bspFile));

                    // entities
                    int brushEnts = 0;
                    int pointEnts = 0;

                    List<Entity> entities = bspReader.getData().entities;

                    for (Entity ent : entities) {
                        if (ent.getModelNum() > 0) {
                            brushEnts++;
                        } else {
                            pointEnts++;
                        }
                    }

                    int totalEnts = pointEnts + brushEnts;

                    DecimalFormat df = new DecimalFormat("#,##0");

                    textFieldTotalEnts.setText(df.format(totalEnts));
                    textFieldBrushEnts.setText(df.format(brushEnts));
                    textFieldPointEnts.setText(df.format(pointEnts));
                    tableEntities.setModel(new EntityTableModel(bspReader));

                    // dependencies
                    BspDependencies bspres = new BspDependencies(bspReader);

                    fillTextArea(textAreaMaterials, bspres.getMaterials());
                    fillTextArea(textAreaSounds, bspres.getSoundFiles());
                    fillTextArea(textAreaSoundScripts, bspres.getSoundScripts());
                    fillTextArea(textAreaSoundscapes, bspres.getSoundscapes());
                    fillTextArea(textAreaModels, bspres.getModels());
                    fillTextArea(textAreaParticles, bspres.getParticles());

                    // embedded files
                    tableEmbedded.setModel(new EmbeddedTableModel(bspFile));

                    // checksum (last step, takes most time)
                    BspChecksum checksum = new BspChecksum(bspReader);

                    textFieldFileCRC.setText(String.format("%x", checksum.getFileCRC()));
                    textFieldMapCRC.setText(String.format("%x", checksum.getMapCRC()));

                    // enable buttons
                    extractLumpButton.setEnabled(true);
                    extractAllLumpsButton.setEnabled(true);

                    extractGameLumpButton.setEnabled(true);
                    extractAllGameLumpsButton.setEnabled(true);

                    extractEmbeddedButton.setEnabled(true);
                    extractAllEmbeddedButton.setEnabled(true);
                    extractEmbeddedZipButton.setEnabled(true);
                } catch (Exception ex) {
                    L.log(Level.SEVERE, "Couldn't read BSP file", ex);
                } finally {
                    // free previously opened files and resources
                    System.gc();

                    // reset cursor
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }).start();
    }

    private void fillTextArea(JTextArea textArea, Collection<String> strings) {
        for (String string : strings) {
            textArea.append(string);
            textArea.append("\n");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openFileChooser = new javax.swing.JFileChooser();
        saveDirectoryChooser = new javax.swing.JFileChooser();
        saveZipFileChooser = new javax.swing.JFileChooser();
        tabbedPane = new javax.swing.JTabbedPane();
        panelGeneral = new javax.swing.JPanel();
        panelGame = new javax.swing.JPanel();
        linkLabelAppURL = new info.ata4.bspsrc.app.util.components.URILabel();
        textFieldAppID = new javax.swing.JTextField();
        textFieldGame = new javax.swing.JTextField();
        labelAppID = new javax.swing.JLabel();
        labelGame = new javax.swing.JLabel();
        panelHeaders = new javax.swing.JPanel();
        textFieldEndian = new javax.swing.JTextField();
        textFieldCompressed = new javax.swing.JTextField();
        labelEndian = new javax.swing.JLabel();
        labelCompressed = new javax.swing.JLabel();
        textFieldVersion = new javax.swing.JTextField();
        labelVersion = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        labelName = new javax.swing.JLabel();
        textFieldRevision = new javax.swing.JTextField();
        labelRevision = new javax.swing.JLabel();
        labelComment = new javax.swing.JLabel();
        textFieldComment = new javax.swing.JTextField();
        panelChecksums = new javax.swing.JPanel();
        labelFileCRC = new javax.swing.JLabel();
        textFieldFileCRC = new javax.swing.JTextField();
        labelMapCRC = new javax.swing.JLabel();
        textFieldMapCRC = new javax.swing.JTextField();
        panelCompileParams = new javax.swing.JPanel();
        labelVbsp = new javax.swing.JLabel();
        textFieldVbspParams = new javax.swing.JTextField();
        labelVvis = new javax.swing.JLabel();
        textFieldVvisParams = new javax.swing.JTextField();
        labelVrad = new javax.swing.JLabel();
        textFieldVradParams = new javax.swing.JTextField();
        panelLumps = new javax.swing.JPanel();
        scrollPaneLumps = new javax.swing.JScrollPane();
        tableLumps = new javax.swing.JTable();
        extractLumpButton = new javax.swing.JButton();
        extractAllLumpsButton = new javax.swing.JButton();
        panelGameLumps = new javax.swing.JPanel();
        scrollPaneGameLumps = new javax.swing.JScrollPane();
        tableGameLumps = new javax.swing.JTable();
        extractGameLumpButton = new javax.swing.JButton();
        extractAllGameLumpsButton = new javax.swing.JButton();
        panelEntities = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        textFieldPointEnts = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        textFieldBrushEnts = new javax.swing.JTextField();
        textFieldTotalEnts = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableEntities = new javax.swing.JTable();
        tabbedPaneDependencies = new javax.swing.JTabbedPane();
        scrollPaneMaterials = new javax.swing.JScrollPane();
        textAreaMaterials = new javax.swing.JTextArea();
        scrollPaneSounds = new javax.swing.JScrollPane();
        textAreaSounds = new javax.swing.JTextArea();
        scrollPaneSoundScripts = new javax.swing.JScrollPane();
        textAreaSoundScripts = new javax.swing.JTextArea();
        scrollPaneSoundscapes = new javax.swing.JScrollPane();
        textAreaSoundscapes = new javax.swing.JTextArea();
        scrollPaneModels = new javax.swing.JScrollPane();
        textAreaModels = new javax.swing.JTextArea();
        scrollPaneParticles = new javax.swing.JScrollPane();
        textAreaParticles = new javax.swing.JTextArea();
        panelEmbedded = new javax.swing.JPanel();
        scrollPaneEmbedded = new javax.swing.JScrollPane();
        tableEmbedded = new javax.swing.JTable();
        extractEmbeddedButton = new javax.swing.JButton();
        extractAllEmbeddedButton = new javax.swing.JButton();
        extractEmbeddedZipButton = new javax.swing.JButton();
        panelProt = new javax.swing.JPanel();
        panelVmex = new javax.swing.JPanel();
        checkBoxVmexEntity = new info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox();
        checkBoxVmexTexture = new info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox();
        checkBoxVmexBrush = new info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox();
        panelIID = new javax.swing.JPanel();
        checkBoxIIDObfs = new info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox();
        checkBoxIIDTexHack = new info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox();
        panelOther = new javax.swing.JPanel();
        checkBoxBSPProtect = new info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        openFileMenuItem = new javax.swing.JMenuItem();

        openFileChooser.setFileFilter(new FileExtensionFilter("Source engine map file", "bsp"));

        saveDirectoryChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        saveDirectoryChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        saveZipFileChooser.setAcceptAllFileFilterUsed(false);
        saveZipFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        saveZipFileChooser.setFileFilter(new FileExtensionFilter("Zip file", "zip"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelGame.setBorder(javax.swing.BorderFactory.createTitledBorder("Game"));

        linkLabelAppURL.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        linkLabelAppURL.setText(" ");

        textFieldAppID.setEditable(false);

        textFieldGame.setEditable(false);

        labelAppID.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelAppID.setText("App-ID");

        labelGame.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelGame.setText("Name");

        javax.swing.GroupLayout panelGameLayout = new javax.swing.GroupLayout(panelGame);
        panelGame.setLayout(panelGameLayout);
        panelGameLayout.setHorizontalGroup(
            panelGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelGame)
                    .addComponent(labelAppID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGameLayout.createSequentialGroup()
                        .addComponent(textFieldAppID, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(linkLabelAppURL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(textFieldGame))
                .addContainerGap())
        );
        panelGameLayout.setVerticalGroup(
            panelGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGame)
                    .addComponent(textFieldGame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAppID)
                    .addComponent(textFieldAppID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(linkLabelAppURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelHeaders.setBorder(javax.swing.BorderFactory.createTitledBorder("Headers"));

        textFieldEndian.setEditable(false);

        textFieldCompressed.setEditable(false);

        labelEndian.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEndian.setText("Endianness");

        labelCompressed.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCompressed.setText("Compressed");

        textFieldVersion.setEditable(false);
        textFieldVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldVersionActionPerformed(evt);
            }
        });

        labelVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelVersion.setText("Version");

        textFieldName.setEditable(false);

        labelName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelName.setText("Name");

        textFieldRevision.setEditable(false);

        labelRevision.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRevision.setText("Revision");

        labelComment.setText("Comment");

        textFieldComment.setEditable(false);

        javax.swing.GroupLayout panelHeadersLayout = new javax.swing.GroupLayout(panelHeaders);
        panelHeaders.setLayout(panelHeadersLayout);
        panelHeadersLayout.setHorizontalGroup(
            panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeadersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelHeadersLayout.createSequentialGroup()
                        .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelComment)
                            .addComponent(labelCompressed))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelHeadersLayout.createSequentialGroup()
                                .addComponent(textFieldCompressed, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(22, 22, 22)
                                .addComponent(labelEndian)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldEndian))
                            .addComponent(textFieldComment)))
                    .addGroup(panelHeadersLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelName)
                            .addComponent(labelVersion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelHeadersLayout.createSequentialGroup()
                                .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(36, 36, 36)
                                .addComponent(labelRevision)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldRevision))
                            .addComponent(textFieldName))))
                .addContainerGap())
        );
        panelHeadersLayout.setVerticalGroup(
            panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeadersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelName)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelVersion)
                    .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelRevision)
                    .addComponent(textFieldRevision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCompressed)
                    .addComponent(textFieldCompressed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEndian)
                    .addComponent(textFieldEndian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelComment)
                    .addComponent(textFieldComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelChecksums.setBorder(javax.swing.BorderFactory.createTitledBorder("Checksums"));

        labelFileCRC.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelFileCRC.setText("File CRC");
        labelFileCRC.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        textFieldFileCRC.setEditable(false);

        labelMapCRC.setText("Map CRC");

        textFieldMapCRC.setEditable(false);

        javax.swing.GroupLayout panelChecksumsLayout = new javax.swing.GroupLayout(panelChecksums);
        panelChecksums.setLayout(panelChecksumsLayout);
        panelChecksumsLayout.setHorizontalGroup(
            panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChecksumsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelFileCRC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textFieldFileCRC, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMapCRC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textFieldMapCRC, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );
        panelChecksumsLayout.setVerticalGroup(
            panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChecksumsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelMapCRC)
                        .addComponent(textFieldMapCRC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelFileCRC)
                        .addComponent(textFieldFileCRC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCompileParams.setBorder(javax.swing.BorderFactory.createTitledBorder("Detected compile parameters"));

        labelVbsp.setText("vbsp");

        textFieldVbspParams.setEditable(false);

        labelVvis.setText("vvis");

        textFieldVvisParams.setEditable(false);

        labelVrad.setText("vrad");

        textFieldVradParams.setEditable(false);

        javax.swing.GroupLayout panelCompileParamsLayout = new javax.swing.GroupLayout(panelCompileParams);
        panelCompileParams.setLayout(panelCompileParamsLayout);
        panelCompileParamsLayout.setHorizontalGroup(
            panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelCompileParamsLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelVrad)
                    .addComponent(labelVbsp)
                    .addComponent(labelVvis))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(textFieldVradParams)
                    .addComponent(textFieldVvisParams)
                    .addComponent(textFieldVbspParams))
                .addContainerGap())
        );
        panelCompileParamsLayout.setVerticalGroup(
            panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCompileParamsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelVbsp)
                    .addComponent(textFieldVbspParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textFieldVvisParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelVvis))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCompileParamsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelVrad)
                    .addComponent(textFieldVradParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelHeaders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelGame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelChecksums, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCompileParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelHeaders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelGame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelChecksums, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCompileParams, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        tabbedPane.addTab("General", panelGeneral);

        tableLumps.setAutoCreateRowSorter(true);
        tableLumps.setModel(new LumpTableModel());
        tableLumps.getTableHeader().setReorderingAllowed(false);
        scrollPaneLumps.setViewportView(tableLumps);

        extractLumpButton.setText("Extract");
        extractLumpButton.setEnabled(false);
        extractLumpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractLumpButtonActionPerformed(evt);
            }
        });

        extractAllLumpsButton.setText("Extract all");
        extractAllLumpsButton.setEnabled(false);
        extractAllLumpsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractAllLumpsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLumpsLayout = new javax.swing.GroupLayout(panelLumps);
        panelLumps.setLayout(panelLumpsLayout);
        panelLumpsLayout.setHorizontalGroup(
            panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLumpsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneLumps, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addGroup(panelLumpsLayout.createSequentialGroup()
                        .addComponent(extractLumpButton)
                        .addGap(18, 18, 18)
                        .addComponent(extractAllLumpsButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelLumpsLayout.setVerticalGroup(
            panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLumpsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneLumps, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extractLumpButton)
                    .addComponent(extractAllLumpsButton))
                .addContainerGap())
        );

        tabbedPane.addTab("Lumps", panelLumps);

        tableGameLumps.setAutoCreateRowSorter(true);
        tableGameLumps.setModel(new GameLumpTableModel());
        tableGameLumps.getTableHeader().setReorderingAllowed(false);
        scrollPaneGameLumps.setViewportView(tableGameLumps);

        extractGameLumpButton.setText("Extract");
        extractGameLumpButton.setEnabled(false);
        extractGameLumpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractGameLumpButtonActionPerformed(evt);
            }
        });

        extractAllGameLumpsButton.setText("Extract all");
        extractAllGameLumpsButton.setEnabled(false);
        extractAllGameLumpsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractAllGameLumpsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelGameLumpsLayout = new javax.swing.GroupLayout(panelGameLumps);
        panelGameLumps.setLayout(panelGameLumpsLayout);
        panelGameLumpsLayout.setHorizontalGroup(
            panelGameLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGameLumpsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGameLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneGameLumps, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(panelGameLumpsLayout.createSequentialGroup()
                        .addComponent(extractGameLumpButton)
                        .addGap(18, 18, 18)
                        .addComponent(extractAllGameLumpsButton)
                        .addGap(0, 139, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelGameLumpsLayout.setVerticalGroup(
            panelGameLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGameLumpsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneGameLumps, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGameLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extractGameLumpButton)
                    .addComponent(extractAllGameLumpsButton))
                .addContainerGap())
        );

        tabbedPane.addTab("Game lumps", panelGameLumps);

        jLabel3.setText("Point");

        textFieldPointEnts.setEditable(false);

        jLabel2.setText("Brush");

        textFieldBrushEnts.setEditable(false);

        textFieldTotalEnts.setEditable(false);

        jLabel1.setText("Total");

        tableEntities.setAutoCreateRowSorter(true);
        tableEntities.setModel(new EntityTableModel());
        jScrollPane1.setViewportView(tableEntities);

        javax.swing.GroupLayout panelEntitiesLayout = new javax.swing.GroupLayout(panelEntities);
        panelEntities.setLayout(panelEntitiesLayout);
        panelEntitiesLayout.setHorizontalGroup(
            panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEntitiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelEntitiesLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFieldPointEnts, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFieldBrushEnts, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFieldTotalEnts, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelEntitiesLayout.setVerticalGroup(
            panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEntitiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(textFieldPointEnts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(textFieldBrushEnts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(textFieldTotalEnts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Entities", panelEntities);

        textAreaMaterials.setColumns(20);
        textAreaMaterials.setEditable(false);
        textAreaMaterials.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaMaterials.setRows(5);
        scrollPaneMaterials.setViewportView(textAreaMaterials);

        tabbedPaneDependencies.addTab("Materials", scrollPaneMaterials);

        textAreaSounds.setColumns(20);
        textAreaSounds.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaSounds.setRows(5);
        scrollPaneSounds.setViewportView(textAreaSounds);

        tabbedPaneDependencies.addTab("Sounds", scrollPaneSounds);

        textAreaSoundScripts.setColumns(20);
        textAreaSoundScripts.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaSoundScripts.setRows(5);
        scrollPaneSoundScripts.setViewportView(textAreaSoundScripts);

        tabbedPaneDependencies.addTab("Sound scripts", scrollPaneSoundScripts);

        textAreaSoundscapes.setColumns(20);
        textAreaSoundscapes.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaSoundscapes.setRows(5);
        scrollPaneSoundscapes.setViewportView(textAreaSoundscapes);

        tabbedPaneDependencies.addTab("Soundscapes", scrollPaneSoundscapes);

        textAreaModels.setColumns(20);
        textAreaModels.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaModels.setRows(5);
        scrollPaneModels.setViewportView(textAreaModels);

        tabbedPaneDependencies.addTab("Models", scrollPaneModels);

        textAreaParticles.setColumns(20);
        textAreaParticles.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textAreaParticles.setRows(5);
        scrollPaneParticles.setViewportView(textAreaParticles);

        tabbedPaneDependencies.addTab("Particles", scrollPaneParticles);

        tabbedPane.addTab("Dependencies", tabbedPaneDependencies);

        tableEmbedded.setAutoCreateRowSorter(true);
        tableEmbedded.setModel(new EmbeddedTableModel());
        tableEmbedded.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollPaneEmbedded.setViewportView(tableEmbedded);

        extractEmbeddedButton.setText("Extract");
        extractEmbeddedButton.setEnabled(false);
        extractEmbeddedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractEmbeddedButtonActionPerformed(evt);
            }
        });

        extractAllEmbeddedButton.setText("Extract all");
        extractAllEmbeddedButton.setEnabled(false);
        extractAllEmbeddedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractAllEmbeddedButtonActionPerformed(evt);
            }
        });

        extractEmbeddedZipButton.setText("Extract Zip file");
        extractEmbeddedZipButton.setEnabled(false);
        extractEmbeddedZipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractEmbeddedZipButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelEmbeddedLayout = new javax.swing.GroupLayout(panelEmbedded);
        panelEmbedded.setLayout(panelEmbeddedLayout);
        panelEmbeddedLayout.setHorizontalGroup(
            panelEmbeddedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEmbeddedLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEmbeddedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneEmbedded, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                    .addGroup(panelEmbeddedLayout.createSequentialGroup()
                        .addComponent(extractEmbeddedButton)
                        .addGap(18, 18, 18)
                        .addComponent(extractAllEmbeddedButton)
                        .addGap(18, 18, 18)
                        .addComponent(extractEmbeddedZipButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelEmbeddedLayout.setVerticalGroup(
            panelEmbeddedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEmbeddedLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneEmbedded, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEmbeddedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(extractEmbeddedButton)
                    .addComponent(extractAllEmbeddedButton)
                    .addComponent(extractEmbeddedZipButton))
                .addContainerGap())
        );

        tabbedPane.addTab("Embedded files", panelEmbedded);

        panelVmex.setBorder(javax.swing.BorderFactory.createTitledBorder("VMEX"));

        checkBoxVmexEntity.setText("Entity flag");
        checkBoxVmexEntity.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxVmexEntity.setIconTextGap(6);

        checkBoxVmexTexture.setText("Texture flag");
        checkBoxVmexTexture.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxVmexTexture.setIconTextGap(6);

        checkBoxVmexBrush.setText("Protector brush");
        checkBoxVmexBrush.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxVmexBrush.setIconTextGap(6);

        javax.swing.GroupLayout panelVmexLayout = new javax.swing.GroupLayout(panelVmex);
        panelVmex.setLayout(panelVmexLayout);
        panelVmexLayout.setHorizontalGroup(
            panelVmexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelVmexLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelVmexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxVmexBrush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxVmexTexture, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxVmexEntity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelVmexLayout.setVerticalGroup(
            panelVmexLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelVmexLayout.createSequentialGroup()
                .addComponent(checkBoxVmexEntity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxVmexTexture, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxVmexBrush, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelIID.setBorder(javax.swing.BorderFactory.createTitledBorder("IID"));

        checkBoxIIDObfs.setText("Entity obfuscation");
        checkBoxIIDObfs.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxIIDObfs.setIconTextGap(6);

        checkBoxIIDTexHack.setText("Nodraw texture hack");
        checkBoxIIDTexHack.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxIIDTexHack.setIconTextGap(6);

        javax.swing.GroupLayout panelIIDLayout = new javax.swing.GroupLayout(panelIID);
        panelIID.setLayout(panelIIDLayout);
        panelIIDLayout.setHorizontalGroup(
            panelIIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIIDLayout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(panelIIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxIIDTexHack, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxIIDObfs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        panelIIDLayout.setVerticalGroup(
            panelIIDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelIIDLayout.createSequentialGroup()
                .addComponent(checkBoxIIDObfs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxIIDTexHack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        panelOther.setBorder(javax.swing.BorderFactory.createTitledBorder("Other"));

        checkBoxBSPProtect.setText("BSPProtect");
        checkBoxBSPProtect.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxBSPProtect.setIconTextGap(6);

        javax.swing.GroupLayout panelOtherLayout = new javax.swing.GroupLayout(panelOther);
        panelOther.setLayout(panelOtherLayout);
        panelOtherLayout.setHorizontalGroup(
            panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOtherLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(checkBoxBSPProtect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panelOtherLayout.setVerticalGroup(
            panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(checkBoxBSPProtect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout panelProtLayout = new javax.swing.GroupLayout(panelProt);
        panelProt.setLayout(panelProtLayout);
        panelProtLayout.setHorizontalGroup(
            panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProtLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panelVmex, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelIID, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelOther, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(151, Short.MAX_VALUE))
        );
        panelProtLayout.setVerticalGroup(
            panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProtLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelVmex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelIID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelOther, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(233, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Protection", panelProt);

        menuFile.setText("File");

        openFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openFileMenuItem.setText("Open");
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileMenuItemActionPerformed(evt);
            }
        });
        menuFile.add(openFileMenuItem);

        menuBar.add(menuFile);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void initComponentsCustom() {
        // add version to title
        setTitle(NAME + " " + VERSION);

        // instant awesome, just add icons!
        try {
            URL iconUrl = getClass().getResource("resources/icon.png");
            Image icon = Toolkit.getDefaultToolkit().createImage(iconUrl);
            setIconImage(icon);
        } catch (Exception ex) {
            // meh, don't care
        }

        DecimalFormat largeFormat = new DecimalFormat("#,##0");

        // set table column widths and special renderers
        TableColumnModel tcm;

        // lump table
        tcm = tableLumps.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(30);
        tcm.getColumn(1).setPreferredWidth(150);
        tcm.getColumn(4).setPreferredWidth(40);
        tcm.getColumn(2).setCellRenderer(new ByteSizeCellRenderer());
        tcm.getColumn(3).setCellRenderer(new ProgressCellRenderer());
        tableLumps.setAutoCreateColumnsFromModel(false);

        // game lump table
        tcm = tableGameLumps.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(30);
        tcm.getColumn(3).setPreferredWidth(40);
        tcm.getColumn(1).setCellRenderer(new ByteSizeCellRenderer());
        tcm.getColumn(2).setCellRenderer(new ProgressCellRenderer());
        tableGameLumps.setAutoCreateColumnsFromModel(false);

        // entity table
        tcm = tableEntities.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(250);
        tcm.getColumn(1).setPreferredWidth(50);
        tcm.getColumn(1).setCellRenderer(new DecimalFormatCellRenderer(largeFormat));
        tableEntities.setAutoCreateColumnsFromModel(false);

        // embedded table
        tcm = tableEmbedded.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(250);
        tcm.getColumn(1).setPreferredWidth(50);
        tcm.getColumn(1).setCellRenderer(new ByteSizeCellRenderer());
        tableEmbedded.setAutoCreateColumnsFromModel(false);
    }

    private void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileMenuItemActionPerformed
        int result = openFileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        loadFile(openFileChooser.getSelectedFile());
    }//GEN-LAST:event_openFileMenuItemActionPerformed

    private void extractLumpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractLumpButtonActionPerformed
        int[] selected = tableLumps.getSelectedRows();

        if (selected.length == 0) {
            return;
        }

        saveDirectoryChooser.setCurrentDirectory(currentFile);
        int result = saveDirectoryChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path dest = saveDirectoryChooser.getSelectedFile().toPath();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            int files = 0;
            TableModel model = tableLumps.getModel();
            RowSorter sorter = tableLumps.getRowSorter();

            for (int index : selected) {
                index = sorter.convertRowIndexToModel(index);
                int lumpIndex = (Integer) model.getValueAt(index, 0);
                LumpType lumpType = LumpType.get(lumpIndex, bspFile.getVersion());

                try {
                    BspFileUtils.extractLump(bspFile, lumpType, dest);
                    files++;
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Couldn't extract lump " + lumpType, ex);
                }
            }

            JOptionPane.showMessageDialog(this, "Successfully extracted " + files + " lumps.");
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractLumpButtonActionPerformed

    private void extractAllLumpsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractAllLumpsButtonActionPerformed
        saveDirectoryChooser.setCurrentDirectory(currentFile);
        int result = saveDirectoryChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path dest = saveDirectoryChooser.getSelectedFile().toPath();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            BspFileUtils.extractLumps(bspFile, dest);
            JOptionPane.showMessageDialog(this, "Successfully extracted all lumps.");
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't extract lumps", ex);
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractAllLumpsButtonActionPerformed

    private void extractGameLumpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractGameLumpButtonActionPerformed
        int[] selected = tableGameLumps.getSelectedRows();

        if (selected.length == 0) {
            return;
        }

        saveDirectoryChooser.setCurrentDirectory(currentFile);
        int result = saveDirectoryChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path dest = saveDirectoryChooser.getSelectedFile().toPath();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            int files = 0;
            TableModel model = tableGameLumps.getModel();
            RowSorter sorter = tableGameLumps.getRowSorter();

            for (int index : selected) {
                index = sorter.convertRowIndexToModel(index);
                String id = (String) model.getValueAt(index, 0);

                try {
                    BspFileUtils.extractGameLump(bspFile, id, dest);
                    files++;
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Couldn't extract game lump " + id, ex);
                }
            }

            JOptionPane.showMessageDialog(this, "Successfully extracted " + files + " game lumps.");
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractGameLumpButtonActionPerformed

    private void extractAllGameLumpsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractAllGameLumpsButtonActionPerformed
        saveDirectoryChooser.setCurrentDirectory(currentFile);
        int result = saveDirectoryChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path dest = saveDirectoryChooser.getSelectedFile().toPath();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            BspFileUtils.extractGameLumps(bspFile, dest);
            JOptionPane.showMessageDialog(this, "Successfully extracted all game lumps.");
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't extract lumps", ex);
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractAllGameLumpsButtonActionPerformed

    private void extractEmbeddedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractEmbeddedButtonActionPerformed
        int[] selected = tableEmbedded.getSelectedRows();

        if (selected.length == 0) {
            return;
        }

        saveDirectoryChooser.setCurrentDirectory(currentFile);
        int result = saveDirectoryChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dest = saveDirectoryChooser.getSelectedFile();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        List<String> names = new ArrayList<>();
        TableModel model = tableEmbedded.getModel();
        RowSorter sorter = tableEmbedded.getRowSorter();

        for (int index : selected) {
            index = sorter.convertRowIndexToModel(index);
            names.add((String) model.getValueAt(index, 0));
        }

        try {
            bspFile.getPakFile().unpack(dest.toPath(), names::contains);

            JOptionPane.showMessageDialog(this, "Successfully extracted " + names.size() + " embedded files.");
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't extract embedded files", ex);
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractEmbeddedButtonActionPerformed

    private void extractAllEmbeddedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractAllEmbeddedButtonActionPerformed
        saveDirectoryChooser.setCurrentDirectory(currentFile);
        int result = saveDirectoryChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dest = saveDirectoryChooser.getSelectedFile();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            bspFile.getPakFile().unpack(dest.toPath(), false);
            JOptionPane.showMessageDialog(this, "Successfully extracted all embedded files.");
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't extract embedded files", ex);
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractAllEmbeddedButtonActionPerformed

    private void extractEmbeddedZipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractEmbeddedZipButtonActionPerformed
        saveZipFileChooser.setSelectedFile(new File(currentFile.getParent(), bspFile.getName() + ".zip"));
        int result = saveZipFileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dest = saveZipFileChooser.getSelectedFile();

        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            bspFile.getPakFile().unpack(dest.toPath(), true);
            JOptionPane.showMessageDialog(this, "Successfully extracted embedded Zip file.");
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't extract embedded Zip file", ex);
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_extractEmbeddedZipButtonActionPerformed

    private void textFieldVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldVersionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textFieldVersionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox checkBoxBSPProtect;
    private info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox checkBoxIIDObfs;
    private info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox checkBoxIIDTexHack;
    private info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox checkBoxVmexBrush;
    private info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox checkBoxVmexEntity;
    private info.ata4.bspsrc.app.util.components.ReadOnlyCheckBox checkBoxVmexTexture;
    private javax.swing.JButton extractAllEmbeddedButton;
    private javax.swing.JButton extractAllGameLumpsButton;
    private javax.swing.JButton extractAllLumpsButton;
    private javax.swing.JButton extractEmbeddedButton;
    private javax.swing.JButton extractEmbeddedZipButton;
    private javax.swing.JButton extractGameLumpButton;
    private javax.swing.JButton extractLumpButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelAppID;
    private javax.swing.JLabel labelComment;
    private javax.swing.JLabel labelCompressed;
    private javax.swing.JLabel labelEndian;
    private javax.swing.JLabel labelFileCRC;
    private javax.swing.JLabel labelGame;
    private javax.swing.JLabel labelMapCRC;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelRevision;
    private javax.swing.JLabel labelVbsp;
    private javax.swing.JLabel labelVersion;
    private javax.swing.JLabel labelVrad;
    private javax.swing.JLabel labelVvis;
    private info.ata4.bspsrc.app.util.components.URILabel linkLabelAppURL;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JFileChooser openFileChooser;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JPanel panelChecksums;
    private javax.swing.JPanel panelCompileParams;
    private javax.swing.JPanel panelEmbedded;
    private javax.swing.JPanel panelEntities;
    private javax.swing.JPanel panelGame;
    private javax.swing.JPanel panelGameLumps;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelHeaders;
    private javax.swing.JPanel panelIID;
    private javax.swing.JPanel panelLumps;
    private javax.swing.JPanel panelOther;
    private javax.swing.JPanel panelProt;
    private javax.swing.JPanel panelVmex;
    private javax.swing.JFileChooser saveDirectoryChooser;
    private javax.swing.JFileChooser saveZipFileChooser;
    private javax.swing.JScrollPane scrollPaneEmbedded;
    private javax.swing.JScrollPane scrollPaneGameLumps;
    private javax.swing.JScrollPane scrollPaneLumps;
    private javax.swing.JScrollPane scrollPaneMaterials;
    private javax.swing.JScrollPane scrollPaneModels;
    private javax.swing.JScrollPane scrollPaneParticles;
    private javax.swing.JScrollPane scrollPaneSoundScripts;
    private javax.swing.JScrollPane scrollPaneSounds;
    private javax.swing.JScrollPane scrollPaneSoundscapes;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTabbedPane tabbedPaneDependencies;
    private javax.swing.JTable tableEmbedded;
    private javax.swing.JTable tableEntities;
    private javax.swing.JTable tableGameLumps;
    private javax.swing.JTable tableLumps;
    private javax.swing.JTextArea textAreaMaterials;
    private javax.swing.JTextArea textAreaModels;
    private javax.swing.JTextArea textAreaParticles;
    private javax.swing.JTextArea textAreaSoundScripts;
    private javax.swing.JTextArea textAreaSounds;
    private javax.swing.JTextArea textAreaSoundscapes;
    private javax.swing.JTextField textFieldAppID;
    private javax.swing.JTextField textFieldBrushEnts;
    private javax.swing.JTextField textFieldComment;
    private javax.swing.JTextField textFieldCompressed;
    private javax.swing.JTextField textFieldEndian;
    private javax.swing.JTextField textFieldFileCRC;
    private javax.swing.JTextField textFieldGame;
    private javax.swing.JTextField textFieldMapCRC;
    private javax.swing.JTextField textFieldName;
    private javax.swing.JTextField textFieldPointEnts;
    private javax.swing.JTextField textFieldRevision;
    private javax.swing.JTextField textFieldTotalEnts;
    private javax.swing.JTextField textFieldVbspParams;
    private javax.swing.JTextField textFieldVersion;
    private javax.swing.JTextField textFieldVradParams;
    private javax.swing.JTextField textFieldVvisParams;
    // End of variables declaration//GEN-END:variables
}
