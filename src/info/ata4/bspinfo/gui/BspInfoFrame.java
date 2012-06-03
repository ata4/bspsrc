/*
 ** 2012 May 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bspinfo.gui;

import info.ata4.bsplib.BspFile;
import info.ata4.bsplib.BspFileFilter;
import info.ata4.bsplib.BspFileReader;
import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.entity.Entity;
import info.ata4.bsplib.lump.GameLump;
import info.ata4.bsplib.lump.Lump;
import info.ata4.bsplib.lump.LumpType;
import info.ata4.bspsrc.modules.BspChecksum;
import info.ata4.bspsrc.modules.BspProtection;
import info.ata4.bspsrc.modules.CompileParameters;
import info.ata4.bspsrc.modules.TextureSource;
import info.ata4.util.gui.FileDrop;
import info.ata4.util.gui.FileExtensionFilter;
import info.ata4.util.gui.components.DecimalFormatCellRenderer;
import info.ata4.util.gui.components.ProgressCellRenderer;
import info.ata4.util.log.ConsoleFormatter;
import info.ata4.util.log.DialogHandler;
import info.ata4.util.log.LogUtils;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.TableColumnModel;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspInfoFrame extends javax.swing.JFrame {

    private static final Logger L = Logger.getLogger(BspInfoFrame.class.getName());
    
    public static final String VERSION = "1.0";
    
    private File currentFile;
    private BspFile bspFile;
    private BspFileReader bspReader;
    private FileDrop fdrop;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        LogUtils.configure();
        ConsoleFormatter.setPrintStackTrace(true);
        
        // set the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            L.warning("Failed to set SystemLookAndFeel");
        }

        // create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
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
        fdrop = new FileDrop(this, new FileDrop.Listener() {

            @Override
            public void filesDropped(File[] files) {
                java.io.FileFilter filter = new BspFileFilter();

                if (filter.accept(files[0])) {
                    loadFile(files[0]);
                }
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
    }
    
    public void loadFile(File file) {
        currentFile = file;

        new Thread(new Runnable() {
            public void run() {
                // clear form fields
                reset();
                
                // set waiting cursor
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                // enable menu
                menuTools.setEnabled(true);
                
                try {
                    // init fields that use info from the BspFile only
                    bspFile = new BspFile();
                    bspFile.load(currentFile);

                    textFieldName.setText(bspFile.getName());
                    textFieldVersion.setText(String.valueOf(bspFile.getVersion()));
                    textFieldRevision.setText(String.valueOf(bspFile.getRevision()));
                    textFieldCompressed.setText(bspFile.isCompressed() ? "Yes" : "No");
                    textFieldEndian.setText(bspFile.getByteOrder() == ByteOrder.LITTLE_ENDIAN ? "Little endian" : "Big endian");
                    
                    // init fields that require further BSP loading
                    bspReader = new BspFileReader(bspFile);
                    bspReader.loadEntities();
                    
                    SourceApp app = bspFile.getSourceApp();
                    
                    textFieldAppID.setText(String.valueOf(app.getAppID()));
                    textFieldGame.setText(app.getName());
                    
                    URI steamStoreURI = app.getSteamStoreURI();
                    
                    if (steamStoreURI != null) {
                        linkLabelAppURL.setURI("Steam store link", steamStoreURI);
                    }
                    
                    // init fields that require analysis modules
                    TextureSource texsrc = new TextureSource(bspReader);
                    BspProtection prot = new BspProtection(bspReader, texsrc);
                    prot.check();
                    
                    checkBoxVmexEntity.setSelected(prot.hasEntityFlag());
                    checkBoxVmexTexture.setSelected(prot.hasTextureFlag());
                    checkBoxVmexBrush.setSelected(prot.hasBrushFlag());
                    
                    checkBoxIIDObfs.setSelected(prot.hasObfuscatedEntities());
                    checkBoxIIDTexHack.setSelected(prot.hasModifiedTexinfo());
                    
                    checkBoxBSPProtect.setSelected(prot.hasEncryptedEntities());
                    
                    CompileParameters cparams = new CompileParameters(bspReader);
                    
                    textFieldVbspParams.setText(StringUtils.join(cparams.getVbspParams(), ' '));
                    
                    if (cparams.isVvisRun()) {
                        textFieldVvisParams.setText(StringUtils.join(cparams.getVvisParams(), ' '));
                    } else {
                        textFieldVvisParams.setText("(not run)");
                    }
                    
                    if (cparams.isVradRun()) {
                        textFieldVradParams.setText(StringUtils.join(cparams.getVradParams(), ' '));
                    } else {
                        textFieldVradParams.setText("(not run)");
                    }
                   
                    // init entity stats                    
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
                    
                    EntityTableModel etm = new EntityTableModel();
                    etm.update(bspReader);
                    
                    tableEntities.setModel(etm);
                    
                    // init checksum fields
                    BspChecksum checksum = new BspChecksum(bspReader);
                    
                    textFieldFileCRC.setText(String.format("%x", checksum.getFileCRC()));
                    textFieldMapCRC.setText(String.format("%x", checksum.getMapCRC()));
                    
                    // init lump data table
                    LumpTableModel ltm = new LumpTableModel();
                    ltm.update(bspFile);
                    
                    tableLumps.setModel(ltm);
                } catch (Exception ex) {
                    L.log(Level.SEVERE, "Couldn't read BSP file", ex);
                    
                    // disable menu
                    menuTools.setEnabled(false);
                } finally {
                    // free previously opened files and resources
                    System.gc();
                    
                    // reset cursor
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }).start();
    }
    
    private void extractEmbedded(File destination) {
       try {
            FileUtils.forceMkdir(destination);
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't create directory", ex);
        }
       
        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        ZipArchiveInputStream zis = null;

        try {
            Lump pakLump = bspFile.getLump(LumpType.LUMP_PAKFILE);
            zis = new ZipArchiveInputStream(pakLump.getInputStream());
            int files = 0;
            
            for (ZipArchiveEntry ze; (ze = zis.getNextZipEntry()) != null; files++) {
                File entryFile = new File(destination, ze.getName());
                L.log(Level.INFO, "Extracting {0}", ze.getName());

                try {
                    InputStream cszis = new CloseShieldInputStream(zis);
                    FileUtils.copyInputStreamToFile(cszis, entryFile);
                } catch (IOException ex) {
                    L.log(Level.WARNING, "Couldn't extract file", ex);
                }
            }
            
            JOptionPane.showMessageDialog(this, "Successfully extracted " + files + " embedded files.");
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't read pakfile", ex);
        } finally {
            IOUtils.closeQuietly(zis);
            
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void extractLumps(File destination) {
        try {
            FileUtils.forceMkdir(destination);
        } catch (IOException ex) {
            L.log(Level.WARNING, "Couldn't create directory", ex);
        }
        
        // set waiting cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            List<Lump> lumps = bspFile.getLumps();
            int files = 0;

            for (Lump lump : lumps) {
                try {
                    if (lump.getType() == LumpType.LUMP_UNKNOWN) {
                        continue;
                    }

                    String fileName = String.format("%02d_%s.bin", lump.getIndex(),
                            lump.getName());
                    File lumpFile = new File(destination, fileName);

                    L.log(Level.INFO, "Extracting {0}", lump);

                    InputStream is = lump.getInputStream();
                    FileUtils.copyInputStreamToFile(is, lumpFile);
                    files++;
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't extract lump", ex);
                }
            }

            File gameLumpsDir = new File(destination, "game");
            gameLumpsDir.mkdir();

            List<GameLump> gameLumps = bspFile.getGameLumps();

            for (GameLump lump : gameLumps) {
                try {
                    String fileName = String.format("%s_v%d.bin", lump.getName(), lump.getVersion());
                    File lumpFile = new File(gameLumpsDir, fileName);

                    L.log(Level.INFO, "Extracting {0}", lump);

                    InputStream is = lump.getInputStream();
                    FileUtils.copyInputStreamToFile(is, lumpFile);
                    files++;
                } catch (IOException ex) {
                    L.log(Level.SEVERE, "Can't extract lump", ex);
                }
            }

            JOptionPane.showMessageDialog(this, "Successfully extracted " + files + " lump files.");
        } finally {
            // reset cursor
            setCursor(Cursor.getDefaultCursor());
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
        saveFileChooser = new javax.swing.JFileChooser();
        tabbedPane = new javax.swing.JTabbedPane();
        panelGeneral = new javax.swing.JPanel();
        panelGame = new javax.swing.JPanel();
        linkLabelAppURL = new info.ata4.util.gui.components.URILabel();
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
        panelEntities = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        textFieldPointEnts = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        textFieldBrushEnts = new javax.swing.JTextField();
        textFieldTotalEnts = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableEntities = new javax.swing.JTable();
        panelProt = new javax.swing.JPanel();
        panelVmex = new javax.swing.JPanel();
        checkBoxVmexEntity = new info.ata4.util.gui.components.ReadOnlyCheckBox();
        checkBoxVmexTexture = new info.ata4.util.gui.components.ReadOnlyCheckBox();
        checkBoxVmexBrush = new info.ata4.util.gui.components.ReadOnlyCheckBox();
        panelIID = new javax.swing.JPanel();
        checkBoxIIDObfs = new info.ata4.util.gui.components.ReadOnlyCheckBox();
        checkBoxIIDTexHack = new info.ata4.util.gui.components.ReadOnlyCheckBox();
        panelOther = new javax.swing.JPanel();
        checkBoxBSPProtect = new info.ata4.util.gui.components.ReadOnlyCheckBox();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        openFileMenuItem = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        menuItemExtractFiles = new javax.swing.JMenuItem();
        menuItemExtractLumps = new javax.swing.JMenuItem();

        openFileChooser.setFileFilter(new FileExtensionFilter("Source engine map file", "bsp"));

        saveFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        saveFileChooser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BSPInfo");

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

        labelVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelVersion.setText("Version");

        textFieldName.setEditable(false);

        labelName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelName.setText("Name");

        textFieldRevision.setEditable(false);

        labelRevision.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRevision.setText("Revision");

        javax.swing.GroupLayout panelHeadersLayout = new javax.swing.GroupLayout(panelHeaders);
        panelHeaders.setLayout(panelHeadersLayout);
        panelHeadersLayout.setHorizontalGroup(
            panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelHeadersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelHeadersLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelName)
                            .addComponent(labelVersion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelHeadersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelHeadersLayout.createSequentialGroup()
                                .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(labelRevision)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldRevision, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelHeadersLayout.createSequentialGroup()
                        .addComponent(labelCompressed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFieldCompressed, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(labelEndian, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(textFieldEndian, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labelMapCRC)
                    .addComponent(labelFileCRC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textFieldFileCRC, javax.swing.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE)
                    .addComponent(textFieldMapCRC))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelChecksumsLayout.setVerticalGroup(
            panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChecksumsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFileCRC)
                    .addComponent(textFieldFileCRC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelChecksumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMapCRC)
                    .addComponent(textFieldMapCRC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCompileParams.setBorder(javax.swing.BorderFactory.createTitledBorder("Compile parameters"));

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
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelHeaders, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelGame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelChecksums, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCompileParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(173, Short.MAX_VALUE))
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
                .addContainerGap(42, Short.MAX_VALUE))
        );

        tabbedPane.addTab("General", panelGeneral);

        tableLumps.setAutoCreateRowSorter(true);
        tableLumps.setModel(new LumpTableModel());
        tableLumps.getTableHeader().setReorderingAllowed(false);
        scrollPaneLumps.setViewportView(tableLumps);

        javax.swing.GroupLayout panelLumpsLayout = new javax.swing.GroupLayout(panelLumps);
        panelLumps.setLayout(panelLumpsLayout);
        panelLumpsLayout.setHorizontalGroup(
            panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLumpsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneLumps)
                .addContainerGap())
        );
        panelLumpsLayout.setVerticalGroup(
            panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLumpsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPaneLumps, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Lumps", panelLumps);

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
                    .addComponent(jScrollPane1))
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Entities", panelEntities);

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

        checkBoxBSPProtect.setText("BSPProtect entity encryption");
        checkBoxBSPProtect.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        checkBoxBSPProtect.setIconTextGap(6);

        javax.swing.GroupLayout panelOtherLayout = new javax.swing.GroupLayout(panelOther);
        panelOther.setLayout(panelOtherLayout);
        panelOtherLayout.setHorizontalGroup(
            panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelOtherLayout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
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
                .addGroup(panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelOther, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(panelVmex, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelIID, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(251, Short.MAX_VALUE))
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
                .addContainerGap(257, Short.MAX_VALUE))
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

        menuTools.setText("Tools");
        menuTools.setEnabled(false);

        menuItemExtractFiles.setText("Extract embedded files");
        menuItemExtractFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExtractFilesActionPerformed(evt);
            }
        });
        menuTools.add(menuItemExtractFiles);

        menuItemExtractLumps.setText("Extract lumps");
        menuItemExtractLumps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExtractLumpsActionPerformed(evt);
            }
        });
        menuTools.add(menuItemExtractLumps);

        menuBar.add(menuTools);

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
        setTitle(getTitle() + " " + VERSION);
        
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
        TableColumnModel tcm = tableLumps.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(30);
        tcm.getColumn(1).setPreferredWidth(150);
        tcm.getColumn(4).setPreferredWidth(40);
        tcm.getColumn(2).setCellRenderer(new DecimalFormatCellRenderer(largeFormat));
        tcm.getColumn(3).setCellRenderer(new ProgressCellRenderer());
        
        tcm = tableEntities.getColumnModel();
        tcm.getColumn(1).setPreferredWidth(50);
        tcm.getColumn(1).setCellRenderer(new DecimalFormatCellRenderer(largeFormat));
        
        // don't rebuild columns when replacing the model from now on to keep
        // the preferred width set above
        tableLumps.setAutoCreateColumnsFromModel(false);
        tableEntities.setAutoCreateColumnsFromModel(false);
    }
    
    private void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileMenuItemActionPerformed
        int result = openFileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        loadFile(openFileChooser.getSelectedFile());
    }//GEN-LAST:event_openFileMenuItemActionPerformed

    private void menuItemExtractFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExtractFilesActionPerformed
        saveFileChooser.setCurrentDirectory(currentFile);
        int result = saveFileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        extractEmbedded(saveFileChooser.getSelectedFile());
    }//GEN-LAST:event_menuItemExtractFilesActionPerformed

    private void menuItemExtractLumpsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExtractLumpsActionPerformed
        saveFileChooser.setCurrentDirectory(currentFile);
        int result = saveFileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        extractLumps(saveFileChooser.getSelectedFile());
    }//GEN-LAST:event_menuItemExtractLumpsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private info.ata4.util.gui.components.ReadOnlyCheckBox checkBoxBSPProtect;
    private info.ata4.util.gui.components.ReadOnlyCheckBox checkBoxIIDObfs;
    private info.ata4.util.gui.components.ReadOnlyCheckBox checkBoxIIDTexHack;
    private info.ata4.util.gui.components.ReadOnlyCheckBox checkBoxVmexBrush;
    private info.ata4.util.gui.components.ReadOnlyCheckBox checkBoxVmexEntity;
    private info.ata4.util.gui.components.ReadOnlyCheckBox checkBoxVmexTexture;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelAppID;
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
    private info.ata4.util.gui.components.URILabel linkLabelAppURL;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemExtractFiles;
    private javax.swing.JMenuItem menuItemExtractLumps;
    private javax.swing.JMenu menuTools;
    private javax.swing.JFileChooser openFileChooser;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JPanel panelChecksums;
    private javax.swing.JPanel panelCompileParams;
    private javax.swing.JPanel panelEntities;
    private javax.swing.JPanel panelGame;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelHeaders;
    private javax.swing.JPanel panelIID;
    private javax.swing.JPanel panelLumps;
    private javax.swing.JPanel panelOther;
    private javax.swing.JPanel panelProt;
    private javax.swing.JPanel panelVmex;
    private javax.swing.JFileChooser saveFileChooser;
    private javax.swing.JScrollPane scrollPaneLumps;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTable tableEntities;
    private javax.swing.JTable tableLumps;
    private javax.swing.JTextField textFieldAppID;
    private javax.swing.JTextField textFieldBrushEnts;
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
