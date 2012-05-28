/*
 ** 2012 Mai 27
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
import info.ata4.bspsrc.modules.BspChecksum;
import info.ata4.bspsrc.modules.BspProtection;
import info.ata4.bspsrc.modules.TextureSource;
import info.ata4.util.gui.FileDrop;
import info.ata4.util.gui.FileExtensionFilter;
import info.ata4.util.log.ConsoleFormatter;
import info.ata4.util.log.LogUtils;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspInfoFrame extends javax.swing.JFrame {

    private static final Logger L = Logger.getLogger(BspInfoFrame.class.getName());
    
    public static final String VERSION = "1.0";
    
    private File currentFile;
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
    }
    
    public final void reset() {
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
        
        checkBoxVmexEntity.setSelected(false);
        checkBoxVmexTexture.setSelected(false);
        checkBoxVmexBrush.setSelected(false);

        checkBoxIIDObfs.setSelected(false);
        checkBoxIIDTexHack.setSelected(false);

        checkBoxBSPProtect.setSelected(false);
    }
    
    public void loadFile(File file) {
        currentFile = file;

        new Thread(new Runnable() {
            public void run() {
                // clear form fields
                reset();
                
                // set waiting cursor
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                try {
                    // init fields that use info from the BspFile only
                    BspFile bspFile = new BspFile();
                    bspFile.load(currentFile);

                    textFieldName.setText(bspFile.getName());
                    textFieldVersion.setText(String.valueOf(bspFile.getVersion()));
                    textFieldRevision.setText(String.valueOf(bspFile.getRevision()));
                    textFieldCompressed.setText(bspFile.isCompressed() ? "Yes" : "No");
                    textFieldEndian.setText(bspFile.getByteOrder() == ByteOrder.LITTLE_ENDIAN ? "Little endian" : "Big endian");
                    
                    // init fields that require further BSP loading
                    BspFileReader bspReader = new BspFileReader(bspFile);
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
                    
                    // init checksum fields
                    BspChecksum checksum = new BspChecksum(bspReader);
                    
                    textFieldFileCRC.setText(String.format("%x", checksum.getFileCRC()));
                    textFieldMapCRC.setText(String.format("%x", checksum.getMapCRC()));
                } catch (Exception ex) {
                    L.log(Level.SEVERE, null, ex);
                } finally {
                    // free previously opened files
                    System.gc();
                    
                    // reset cursor
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }).start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        tabbedPane = new javax.swing.JTabbedPane();
        panelGeneral = new javax.swing.JPanel();
        labelName = new javax.swing.JLabel();
        textFieldName = new javax.swing.JTextField();
        labelVersion = new javax.swing.JLabel();
        textFieldVersion = new javax.swing.JTextField();
        labelRevision = new javax.swing.JLabel();
        textFieldRevision = new javax.swing.JTextField();
        labelCompressed = new javax.swing.JLabel();
        textFieldCompressed = new javax.swing.JTextField();
        labelEndian = new javax.swing.JLabel();
        textFieldEndian = new javax.swing.JTextField();
        labelGame = new javax.swing.JLabel();
        textFieldGame = new javax.swing.JTextField();
        labelAppID = new javax.swing.JLabel();
        textFieldAppID = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textFieldMapCRC = new javax.swing.JTextField();
        textFieldFileCRC = new javax.swing.JTextField();
        linkLabelAppURL = new info.ata4.util.gui.URILabel();
        panelLumps = new javax.swing.JPanel();
        panelEntities = new javax.swing.JPanel();
        panelProt = new javax.swing.JPanel();
        panelVmex = new javax.swing.JPanel();
        checkBoxVmexEntity = new info.ata4.util.gui.ReadOnlyCheckBox();
        checkBoxVmexTexture = new info.ata4.util.gui.ReadOnlyCheckBox();
        checkBoxVmexBrush = new info.ata4.util.gui.ReadOnlyCheckBox();
        panelIID = new javax.swing.JPanel();
        checkBoxIIDObfs = new info.ata4.util.gui.ReadOnlyCheckBox();
        checkBoxIIDTexHack = new info.ata4.util.gui.ReadOnlyCheckBox();
        panelOther = new javax.swing.JPanel();
        checkBoxBSPProtect = new info.ata4.util.gui.ReadOnlyCheckBox();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        openFileMenuItem = new javax.swing.JMenuItem();
        menuTools = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        fileChooser.setFileFilter(new FileExtensionFilter("Source engine map file", "bsp"));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BSPInfo");

        labelName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelName.setText("Name");

        textFieldName.setEditable(false);

        labelVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelVersion.setText("Version");

        textFieldVersion.setEditable(false);

        labelRevision.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelRevision.setText("Revision");

        textFieldRevision.setEditable(false);

        labelCompressed.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCompressed.setText("Compressed");

        textFieldCompressed.setEditable(false);

        labelEndian.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelEndian.setText("Endianness");

        textFieldEndian.setEditable(false);

        labelGame.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelGame.setText("Game");

        textFieldGame.setEditable(false);

        labelAppID.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelAppID.setText("App-ID");

        textFieldAppID.setEditable(false);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("File CRC");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        jLabel2.setText("Map CRC");

        textFieldMapCRC.setEditable(false);

        textFieldFileCRC.setEditable(false);

        linkLabelAppURL.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        linkLabelAppURL.setText(" ");

        javax.swing.GroupLayout panelGeneralLayout = new javax.swing.GroupLayout(panelGeneral);
        panelGeneral.setLayout(panelGeneralLayout);
        panelGeneralLayout.setHorizontalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelName)
                            .addComponent(labelVersion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldName)
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(labelRevision)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldRevision, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(panelGeneralLayout.createSequentialGroup()
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelGame)
                            .addComponent(labelCompressed)
                            .addComponent(labelAppID)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textFieldGame)
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addComponent(textFieldCompressed, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(12, 12, 12)
                                .addComponent(labelEndian, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(textFieldEndian, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE))
                            .addGroup(panelGeneralLayout.createSequentialGroup()
                                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(textFieldAppID, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                                    .addComponent(textFieldFileCRC))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelGeneralLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(textFieldMapCRC, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(linkLabelAppURL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                .addContainerGap())
        );
        panelGeneralLayout.setVerticalGroup(
            panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelName)
                    .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelVersion)
                    .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelRevision)
                    .addComponent(textFieldRevision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCompressed)
                    .addComponent(textFieldCompressed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelEndian)
                    .addComponent(textFieldEndian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGame)
                    .addComponent(textFieldGame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAppID)
                    .addComponent(textFieldAppID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(linkLabelAppURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(textFieldFileCRC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textFieldMapCRC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab("General", panelGeneral);

        javax.swing.GroupLayout panelLumpsLayout = new javax.swing.GroupLayout(panelLumps);
        panelLumps.setLayout(panelLumpsLayout);
        panelLumpsLayout.setHorizontalGroup(
            panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 313, Short.MAX_VALUE)
        );
        panelLumpsLayout.setVerticalGroup(
            panelLumpsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
        );

        tabbedPane.addTab("Lumps", panelLumps);

        javax.swing.GroupLayout panelEntitiesLayout = new javax.swing.GroupLayout(panelEntities);
        panelEntities.setLayout(panelEntitiesLayout);
        panelEntitiesLayout.setHorizontalGroup(
            panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 313, Short.MAX_VALUE)
        );
        panelEntitiesLayout.setVerticalGroup(
            panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
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
                .addContainerGap(13, Short.MAX_VALUE)
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
                .addContainerGap(15, Short.MAX_VALUE)
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
                    .addGroup(panelProtLayout.createSequentialGroup()
                        .addComponent(panelVmex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelIID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelOther, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelProtLayout.setVerticalGroup(
            panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProtLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProtLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelVmex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelIID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelOther, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
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

        jMenuItem1.setText("Extract embedded files");
        menuTools.add(jMenuItem1);

        jMenuItem2.setText("Extract lumps");
        menuTools.add(jMenuItem2);

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

    private void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileMenuItemActionPerformed
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        loadFile(fileChooser.getSelectedFile());
    }//GEN-LAST:event_openFileMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private info.ata4.util.gui.ReadOnlyCheckBox checkBoxBSPProtect;
    private info.ata4.util.gui.ReadOnlyCheckBox checkBoxIIDObfs;
    private info.ata4.util.gui.ReadOnlyCheckBox checkBoxIIDTexHack;
    private info.ata4.util.gui.ReadOnlyCheckBox checkBoxVmexBrush;
    private info.ata4.util.gui.ReadOnlyCheckBox checkBoxVmexEntity;
    private info.ata4.util.gui.ReadOnlyCheckBox checkBoxVmexTexture;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JLabel labelAppID;
    private javax.swing.JLabel labelCompressed;
    private javax.swing.JLabel labelEndian;
    private javax.swing.JLabel labelGame;
    private javax.swing.JLabel labelName;
    private javax.swing.JLabel labelRevision;
    private javax.swing.JLabel labelVersion;
    private info.ata4.util.gui.URILabel linkLabelAppURL;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuTools;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JPanel panelEntities;
    private javax.swing.JPanel panelGeneral;
    private javax.swing.JPanel panelIID;
    private javax.swing.JPanel panelLumps;
    private javax.swing.JPanel panelOther;
    private javax.swing.JPanel panelProt;
    private javax.swing.JPanel panelVmex;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextField textFieldAppID;
    private javax.swing.JTextField textFieldCompressed;
    private javax.swing.JTextField textFieldEndian;
    private javax.swing.JTextField textFieldFileCRC;
    private javax.swing.JTextField textFieldGame;
    private javax.swing.JTextField textFieldMapCRC;
    private javax.swing.JTextField textFieldName;
    private javax.swing.JTextField textFieldRevision;
    private javax.swing.JTextField textFieldVersion;
    // End of variables declaration//GEN-END:variables
}
