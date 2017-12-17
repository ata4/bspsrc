/*//GEN-FIRST:event_buttonAddActionPerformed
 ** 2011 September 2//GEN-LAST:event_buttonAddActionPerformed
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */

package info.ata4.bspsrc.gui;

import info.ata4.bsplib.BspFileFilter;
import info.ata4.bsplib.app.SourceApp;
import info.ata4.bsplib.app.SourceAppDB;
import info.ata4.bspsrc.*;
import info.ata4.bspsrc.gui.util.EnumToolTexture;
import info.ata4.bspsrc.modules.geom.BrushMode;
import info.ata4.bspsrc.util.SourceFormat;
import info.ata4.log.LogUtils;
import info.ata4.util.gui.FileDrop;
import info.ata4.util.gui.FileExtensionFilter;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * Main window of the BSPSource GUI.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BspSourceFrame extends javax.swing.JFrame {
    
    private static final Logger L = LogUtils.getLogger();
    
    private BspSourceConfig config;
    private BspSourceLogFrame logFrame;
    private FileDrop fdrop;
    private DefaultListModel<BspFileEntry> listFilesModel = new DefaultListModel<>();
    
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
                new BspSourceFrame().setVisible(true);
            }
        });
    }

    /** Creates new form BspSourceFrame */
    public BspSourceFrame() {
        initComponents();
        initComponentsCustom();
        reset();

        // init file dropper
        fdrop = new FileDrop(listFiles, new FileDrop.Listener() {

            @Override
            public void filesDropped(File[] files) {
                java.io.FileFilter filter = new BspFileFilter();

                for (File file : files) {
                    if (file.isDirectory()) {
                        File[] subFiles = file.listFiles(filter);
                        for (File subFile : subFiles) {
                            listFilesModel.addElement(new BspFileEntry(subFile));
                        }
                    } else if (filter.accept(file)) {
                        listFilesModel.addElement(new BspFileEntry(file));
                    }
                }
                
                buttonDecompile.setEnabled(!listFilesModel.isEmpty());
            }
        });
    }

    public ComboBoxModel getFaceTextureModel() {
        return new DefaultComboBoxModel<>(EnumToolTexture.values());
    }
    
    public ComboBoxModel getAppIDModel() {
        DefaultComboBoxModel<SourceApp> cbmodel = new DefaultComboBoxModel<>();
        cbmodel.addElement(new SourceApp("Automatic", 0));
        
        List<SourceApp> apps = SourceAppDB.getInstance().getAppList();
        
        for (SourceApp app : apps) {
            cbmodel.addElement(app);
        }
        
        return cbmodel;
    }
    
    public ComboBoxModel getBrushModeModel() {
        return new DefaultComboBoxModel<>(BrushMode.values());
    }
    
    public ComboBoxModel getSourceFormatModel() {
        return new DefaultComboBoxModel<>(SourceFormat.values());
    }
    
    public ListModel getFilesModel() {
        return listFilesModel;
    }
    
    /**
     * Resets BSPSource and all form elements to their default values
     */
    public final void reset() {
        config = new BspSourceConfig();
        
        // check boxes
        checkBoxAreaportal.setSelected(config.writeAreaportals);
        checkBoxCubemap.setSelected(config.writeCubemaps);
        checkBoxDebugMode.setSelected(config.isDebug());
        checkBoxDetail.setSelected(config.writeDetails);
        checkBoxDisp.setSelected(config.writeDisp);
        checkBoxFixToolTex.setSelected(config.fixToolTextures);
        checkBoxFixCubemapTex.setSelected(config.fixCubemapTextures);
        checkBoxFixRotation.setSelected(config.fixEntityRot);
        checkBoxLoadLumpFile.setSelected(config.loadLumpFiles);
        checkBoxOccluder.setSelected(config.writeOccluders);
        checkBoxLadder.setSelected(config.writeLaddersAsEntities);
        checkBoxAddUseLandmarkAngles.setSelected(config.addUseLandmarkAngles);
        checkBoxOverlay.setSelected(config.writeOverlays);
        checkBoxPropStatic.setSelected(config.writeStaticProps);
        checkBoxVisgroups.setSelected(config.writeVisgroups);
        checkBoxCameras.setSelected(config.writeCameras);
        checkBoxExtractEmbedded.setSelected(config.unpackEmbedded);

        // linked check boxes
        checkBoxEnableEntities.setSelected(config.isWriteEntities());
        setPanelEnabled(panelEntities, checkBoxEnableEntities);
        checkBoxEnableWorldBrushes.setSelected(config.writeWorldBrushes);
        setPanelEnabled(panelWorldBrushes, checkBoxEnableWorldBrushes);

        // combo boxes
        comboBoxBackfaceTex.setSelectedIndex(0);
        comboBoxFaceTex.setSelectedIndex(0);

        // misc
        listFilesModel.removeAllElements();
        
        switch(config.brushMode) {
            case BRUSHPLANES:
                radioButtonBrushesPlanes.setSelected(true);
                break;
                
            case ORIGFACE:
                radioButtonOrigFaces.setSelected(true);
                break;
                
            case ORIGFACE_PLUS:
                radioButtonOrigSplitFaces.setSelected(true);
                break;
                
            case SPLITFACE:
                radioButtonSplitFaces.setSelected(true);
                break;
        }

        buttonDecompile.setEnabled(false);
    }
    
    public void setButtonsEnabled(boolean value) {
        buttonDecompile.setEnabled(value);
        buttonDefaults.setEnabled(value);
    }
    
    private void setPanelEnabled(JPanel panel, JCheckBox checkbox) {
        Component[] comps = panel.getComponents();
        
        for (Component comp : comps) {
            // don't touch the checkbox
            if (comp == checkbox) {
                continue;
            }
            
            // enable/disable everything in child panels
            if (comp instanceof JPanel) {
                setPanelEnabled((JPanel) comp, checkbox);
            }

            comp.setEnabled(checkbox.isSelected());
        }
    }
    
    private File[] openFileDialog(File defaultFile, FileFilter filter) {
        JFileChooser fc = new JFileChooser() {

            @Override
            public void approveSelection() {
                File file = getSelectedFile();
                if (file != null && !file.exists()) {
                    showFileNotFoundDialog();
                    return;
                }
                super.approveSelection();
            }

            private void showFileNotFoundDialog() {
                JOptionPane.showMessageDialog(this, "The selected file doesn't exist.");
            }
        };
        fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(filter);

        if (defaultFile != null) {
            fc.setSelectedFile(defaultFile);
        } else {
            // use user.dir as default directory
            try {
                fc.setSelectedFile(new File(System.getProperty("user.dir")));
            } catch (Exception ex) {
            }
        }

        // show open file dialog
        int option = fc.showOpenDialog(this);

        if (option != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return fc.getSelectedFiles();
    }

    private File saveFileDialog(File defaultFile, FileFilter filter) {
        JFileChooser fc = new JFileChooser() {

            @Override
            public void approveSelection() {
                File file = getSelectedFile();
                if (file != null && file.exists() && !askOverwrite(file)) {
                    return;
                }
                super.approveSelection();
            }

            private boolean askOverwrite(File file) {
                String title = "Overwriting " + file.getPath();
                String message = "File " + file.getName() + " already exists.\n"
                        + "Do you like to replace it?";

                int choice = JOptionPane.showConfirmDialog(this, message, title,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                return choice == JOptionPane.OK_OPTION;
            }
        };
        fc.setMultiSelectionEnabled(false);
        fc.setSelectedFile(defaultFile);
        fc.setFileFilter(filter);

        // show save file dialog
        int option = fc.showSaveDialog(this);

        if (option != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return fc.getSelectedFile();
    }
    
    private File selectDirectoryDialog(File defaultFile) {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (defaultFile != null) {
            fc.setSelectedFile(defaultFile);
        } else {
            // use user.dir as default directory
            try {
                fc.setSelectedFile(new File(System.getProperty("user.dir")));
            } catch (Exception ex) {
            }
        }

        // show dir selection dialog
        int option = fc.showOpenDialog(this);

        if (option != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return fc.getSelectedFile();
    }

    private File[] openBspFileDialog(File bspFile) {
        return openFileDialog(bspFile,
                new FileExtensionFilter("Source engine map file", "bsp"));
    }

    private File saveVmfFileDialog(File vmfFile) {
        return saveFileDialog(vmfFile,
                new FileExtensionFilter("Hammer map file", "vmf"));
    }

    /**
     * Opens the log window and starts BspSource in a new thread.
     */
    private void startBspSource() {
        new Thread() {

            @Override
            public void run() {
                // clear files in config, then add everything from the list
                Set<BspFileEntry> files = config.getFileSet();
                files.clear();
                
                Enumeration<BspFileEntry> bspListFiles = listFilesModel.elements();
                
                while (bspListFiles.hasMoreElements()) {
                    files.add(bspListFiles.nextElement());
                }
                
                // clear old output
                logFrame.clear();

                // show logging frame
                if (!logFrame.isVisible()) {
                    logFrame.setVisible(true);
                }

                logFrame.requestFocus();

                // enable logging on the output window
                logFrame.setLogging(true);

                // deactivate buttons
                setButtonsEnabled(false);

                try {
                    // start BspSource
                    BspSource bspsource = new BspSource(config);
                    bspsource.run();
                } catch (Throwable t) {
                    // "Oh this is bad!"
                    L.log(Level.SEVERE, "Fatal BSPSource error", t);
                } finally {
                    // activate buttons
                    setButtonsEnabled(true);

                    // use default logging again
                    logFrame.setLogging(false);
                }
            }
        }.start();
    }
    
    private void initComponentsCustom() {
        // add version to title
        setTitle("BSPSource " + BspSource.VERSION);

        // logging frame
        logFrame = new BspSourceLogFrame();

        // instant awesome, just add icons!
        try {
            URL iconUrl = getClass().getResource("resources/icon.png");
            Image icon = Toolkit.getDefaultToolkit().createImage(iconUrl);
            setIconImage(icon);
            logFrame.setIconImage(icon);
        } catch (Exception ex) {
            // meh, don't care
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupBrushMode = new javax.swing.ButtonGroup();
        tabbedPaneOptions = new javax.swing.JTabbedPane();
        panelFiles = new javax.swing.JPanel();
        scrollFiles = new javax.swing.JScrollPane();
        listFiles = new javax.swing.JList();
        buttonAdd = new javax.swing.JButton();
        buttonRemove = new javax.swing.JButton();
        buttonRemoveAll = new javax.swing.JButton();
        labelDnDTip = new javax.swing.JLabel();
        panelWorldBrushes = new javax.swing.JPanel();
        checkBoxDisp = new javax.swing.JCheckBox();
        checkBoxEnableWorldBrushes = new javax.swing.JCheckBox();
        panelBrushMode = new javax.swing.JPanel();
        radioButtonBrushesPlanes = new javax.swing.JRadioButton();
        radioButtonOrigFaces = new javax.swing.JRadioButton();
        radioButtonSplitFaces = new javax.swing.JRadioButton();
        radioButtonOrigSplitFaces = new javax.swing.JRadioButton();
        panelEntities = new javax.swing.JPanel();
        panelPointEnts = new javax.swing.JPanel();
        checkBoxPropStatic = new javax.swing.JCheckBox();
        checkBoxCubemap = new javax.swing.JCheckBox();
        checkBoxOverlay = new javax.swing.JCheckBox();
        panelBrushEnts = new javax.swing.JPanel();
        checkBoxDetail = new javax.swing.JCheckBox();
        checkBoxAreaportal = new javax.swing.JCheckBox();
        checkBoxOccluder = new javax.swing.JCheckBox();
        checkBoxFixRotation = new javax.swing.JCheckBox();
        checkBoxLadder = new javax.swing.JCheckBox();
        checkBoxAddUseLandmarkAngles = new javax.swing.JCheckBox();
        checkBoxEnableEntities = new javax.swing.JCheckBox();
        panelTextures = new javax.swing.JPanel();
        labelFaceTex = new javax.swing.JLabel();
        labelBackfaceTex = new javax.swing.JLabel();
        comboBoxFaceTex = new javax.swing.JComboBox();
        comboBoxBackfaceTex = new javax.swing.JComboBox();
        checkBoxFixCubemapTex = new javax.swing.JCheckBox();
        checkBoxFixToolTex = new javax.swing.JCheckBox();
        panelOther = new javax.swing.JPanel();
        checkBoxDebugMode = new javax.swing.JCheckBox();
        checkBoxLoadLumpFile = new javax.swing.JCheckBox();
        comboBoxMapFormat = new javax.swing.JComboBox();
        labelMapFormat = new javax.swing.JLabel();
        checkBoxVisgroups = new javax.swing.JCheckBox();
        checkBoxCameras = new javax.swing.JCheckBox();
        checkBoxExtractEmbedded = new javax.swing.JCheckBox();
        labelSourceFormat = new javax.swing.JLabel();
        comboBoxSourceFormat = new javax.swing.JComboBox();
        buttonDecompile = new javax.swing.JButton();
        buttonDefaults = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setResizable(false);

        listFiles.setModel(getFilesModel());
        scrollFiles.setViewportView(listFiles);

        buttonAdd.setText("Add");
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        buttonRemove.setText("Remove");
        buttonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveActionPerformed(evt);
            }
        });

        buttonRemoveAll.setText("Remove all");
        buttonRemoveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveAllActionPerformed(evt);
            }
        });

        labelDnDTip.setText("Tip: drag and drop files/folders on the box above");

        javax.swing.GroupLayout panelFilesLayout = new javax.swing.GroupLayout(panelFiles);
        panelFiles.setLayout(panelFilesLayout);
        panelFilesLayout.setHorizontalGroup(
            panelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFilesLayout.createSequentialGroup()
                        .addComponent(scrollFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(buttonRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonRemoveAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(labelDnDTip))
                .addContainerGap())
        );
        panelFilesLayout.setVerticalGroup(
            panelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFilesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFilesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollFiles, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addGroup(panelFilesLayout.createSequentialGroup()
                        .addComponent(buttonAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonRemoveAll)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelDnDTip)
                .addContainerGap())
        );

        tabbedPaneOptions.addTab("Files", panelFiles);

        checkBoxDisp.setText("Write displacements");
        checkBoxDisp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDispActionPerformed(evt);
            }
        });

        checkBoxEnableWorldBrushes.setText("Enable");
        checkBoxEnableWorldBrushes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxEnableWorldBrushesActionPerformed(evt);
            }
        });

        panelBrushMode.setBorder(javax.swing.BorderFactory.createTitledBorder("Mode"));

        buttonGroupBrushMode.add(radioButtonBrushesPlanes);
        radioButtonBrushesPlanes.setText("Brushes and planes");
        radioButtonBrushesPlanes.setToolTipText("<html>Create brushes that closely resemble those<br>\nbrushes from which the map was originally created from.</html>");
        radioButtonBrushesPlanes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonBrushesPlanesActionPerformed(evt);
            }
        });

        buttonGroupBrushMode.add(radioButtonOrigFaces);
        radioButtonOrigFaces.setText("Original faces only");
        radioButtonOrigFaces.setToolTipText("<html>Create flat brushes from the culled<br>\nbrush sides of the original brushes.<br>\n<b>Note:</b> some sides may be missing.</html>");
        radioButtonOrigFaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonOrigFacesActionPerformed(evt);
            }
        });

        buttonGroupBrushMode.add(radioButtonSplitFaces);
        radioButtonSplitFaces.setText("Split faces only");
        radioButtonSplitFaces.setToolTipText("<html>Create flat brushes from the split faces<br>\nthe engine is using for rendering.\n</html>");
        radioButtonSplitFaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonSplitFacesActionPerformed(evt);
            }
        });

        buttonGroupBrushMode.add(radioButtonOrigSplitFaces);
        radioButtonOrigSplitFaces.setText("Original/split faces");
        radioButtonOrigSplitFaces.setToolTipText("<html>Create flat brushes from the culled<br>\nbrush sides of the original brushes.<br>\nWhen a side doesn't exist, the split face<br>\nis created instead.\n</html>");
        radioButtonOrigSplitFaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonOrigSplitFacesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBrushModeLayout = new javax.swing.GroupLayout(panelBrushMode);
        panelBrushMode.setLayout(panelBrushModeLayout);
        panelBrushModeLayout.setHorizontalGroup(
            panelBrushModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBrushModeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBrushModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioButtonBrushesPlanes)
                    .addComponent(radioButtonOrigFaces)
                    .addComponent(radioButtonOrigSplitFaces)
                    .addComponent(radioButtonSplitFaces))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBrushModeLayout.setVerticalGroup(
            panelBrushModeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBrushModeLayout.createSequentialGroup()
                .addComponent(radioButtonBrushesPlanes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonOrigFaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonOrigSplitFaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioButtonSplitFaces)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelWorldBrushesLayout = new javax.swing.GroupLayout(panelWorldBrushes);
        panelWorldBrushes.setLayout(panelWorldBrushesLayout);
        panelWorldBrushesLayout.setHorizontalGroup(
            panelWorldBrushesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWorldBrushesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelWorldBrushesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelWorldBrushesLayout.createSequentialGroup()
                        .addComponent(panelBrushMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxDisp))
                    .addComponent(checkBoxEnableWorldBrushes))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        panelWorldBrushesLayout.setVerticalGroup(
            panelWorldBrushesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWorldBrushesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxEnableWorldBrushes)
                .addGap(7, 7, 7)
                .addGroup(panelWorldBrushesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxDisp)
                    .addComponent(panelBrushMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(64, Short.MAX_VALUE))
        );

        tabbedPaneOptions.addTab("World", panelWorldBrushes);

        panelPointEnts.setBorder(javax.swing.BorderFactory.createTitledBorder("Point entities"));

        checkBoxPropStatic.setText("prop_static");
        checkBoxPropStatic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxPropStaticActionPerformed(evt);
            }
        });

        checkBoxCubemap.setText("info_cubemap");
        checkBoxCubemap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCubemapActionPerformed(evt);
            }
        });

        checkBoxOverlay.setText("info_overlay");
        checkBoxOverlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxOverlayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPointEntsLayout = new javax.swing.GroupLayout(panelPointEnts);
        panelPointEnts.setLayout(panelPointEntsLayout);
        panelPointEntsLayout.setHorizontalGroup(
            panelPointEntsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPointEntsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPointEntsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxPropStatic)
                    .addComponent(checkBoxCubemap)
                    .addComponent(checkBoxOverlay))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelPointEntsLayout.setVerticalGroup(
            panelPointEntsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPointEntsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxPropStatic)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxCubemap)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxOverlay)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelBrushEnts.setBorder(javax.swing.BorderFactory.createTitledBorder("Brush entities"));

        checkBoxDetail.setText("func_detail");
        checkBoxDetail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDetailActionPerformed(evt);
            }
        });

        checkBoxAreaportal.setText("func_areaportal/_window");
        checkBoxAreaportal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxAreaportalActionPerformed(evt);
            }
        });

        checkBoxOccluder.setText("func_occluder");
        checkBoxOccluder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxOccluderActionPerformed(evt);
            }
        });

        checkBoxFixRotation.setText("Fix rotation of instances");
        checkBoxFixRotation.setToolTipText("<html>\nFixes rotation of brush entities that were compiled from rotated instances.<br>\nThe wrong rotation of these brushes is visible in Hammer only and <br>\nwon't affect re-compilation.\n</html>");
        checkBoxFixRotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFixRotationActionPerformed(evt);
            }
        });

        checkBoxLadder.setText("Ladders as func_ladder");
        checkBoxLadder.setToolTipText("<html>Writes ladders as func_ladder entities.<br>Do not use if you intend to recompile for CSGO!</html>");
        checkBoxLadder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxLadderActionPerformed(evt);
            }
        });

        checkBoxAddUseLandmarkAngles.setText("Add UseLandmarkAngles");
        checkBoxAddUseLandmarkAngles.setToolTipText("<html>\nAdds \"UseLandmarkAngles 1\" to trigger_teleport entities that have no value set<br>\nand also do not have a landmark specified. Older games do not use this key and<br>\ninstead treat it as if the value is 1, except when a landmark is set. Use this to<br>\nensure consistant behavior when recompiling maps from older games for newer ones.\n</html>");
        checkBoxAddUseLandmarkAngles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxAddUseLandmarkAnglesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelBrushEntsLayout = new javax.swing.GroupLayout(panelBrushEnts);
        panelBrushEnts.setLayout(panelBrushEntsLayout);
        panelBrushEntsLayout.setHorizontalGroup(
            panelBrushEntsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBrushEntsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelBrushEntsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxDetail)
                    .addComponent(checkBoxAreaportal)
                    .addComponent(checkBoxOccluder)
                    .addComponent(checkBoxFixRotation)
                    .addComponent(checkBoxLadder)
                    .addComponent(checkBoxAddUseLandmarkAngles))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBrushEntsLayout.setVerticalGroup(
            panelBrushEntsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBrushEntsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxDetail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxAreaportal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxOccluder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxLadder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxFixRotation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxAddUseLandmarkAngles)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        checkBoxEnableEntities.setText("Enable");
        checkBoxEnableEntities.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxEnableEntitiesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelEntitiesLayout = new javax.swing.GroupLayout(panelEntities);
        panelEntities.setLayout(panelEntitiesLayout);
        panelEntitiesLayout.setHorizontalGroup(
            panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEntitiesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxEnableEntities)
                    .addGroup(panelEntitiesLayout.createSequentialGroup()
                        .addComponent(panelPointEnts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelBrushEnts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelEntitiesLayout.setVerticalGroup(
            panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEntitiesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(checkBoxEnableEntities)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelEntitiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelBrushEnts, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPointEnts, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabbedPaneOptions.addTab("Entities", panelEntities);

        labelFaceTex.setText("Face texture");

        labelBackfaceTex.setText("Back-face texture");

        comboBoxFaceTex.setModel(getFaceTextureModel());
        comboBoxFaceTex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxFaceTexActionPerformed(evt);
            }
        });

        comboBoxBackfaceTex.setModel(getFaceTextureModel());
        comboBoxBackfaceTex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxBackfaceTexActionPerformed(evt);
            }
        });

        checkBoxFixCubemapTex.setText("Fix cubemap textures");
        checkBoxFixCubemapTex.setToolTipText("Fix textures for environment-mapped materials.");
        checkBoxFixCubemapTex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFixCubemapTexActionPerformed(evt);
            }
        });

        checkBoxFixToolTex.setText("Fix tool textures");
        checkBoxFixToolTex.setToolTipText("Fix tool textures such as toolsnodraw or toolsblocklight.");
        checkBoxFixToolTex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxFixToolTexActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelTexturesLayout = new javax.swing.GroupLayout(panelTextures);
        panelTextures.setLayout(panelTexturesLayout);
        panelTexturesLayout.setHorizontalGroup(
            panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTexturesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxFixToolTex)
                    .addComponent(checkBoxFixCubemapTex)
                    .addGroup(panelTexturesLayout.createSequentialGroup()
                        .addGroup(panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelBackfaceTex)
                            .addComponent(labelFaceTex))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(comboBoxFaceTex, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(comboBoxBackfaceTex, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(177, Short.MAX_VALUE))
        );
        panelTexturesLayout.setVerticalGroup(
            panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelTexturesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoxFaceTex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelFaceTex))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelTexturesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelBackfaceTex)
                    .addComponent(comboBoxBackfaceTex, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkBoxFixCubemapTex)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxFixToolTex)
                .addContainerGap(113, Short.MAX_VALUE))
        );

        tabbedPaneOptions.addTab("Textures", panelTextures);

        checkBoxDebugMode.setText("Debug mode");
        checkBoxDebugMode.setToolTipText("<html>\nThe debug mode produces <i>very</i> verbose output<br>\ntext and writes additional data into the VMF file.\n</html>");
        checkBoxDebugMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxDebugModeActionPerformed(evt);
            }
        });

        checkBoxLoadLumpFile.setText("Load lump files");
        checkBoxLoadLumpFile.setToolTipText("<html>\nWhen enabled, external lump files  <i>(.lmp)</i> with the same<br>\nname as the BSP file will be processed during decompilation.\n</html>");
        checkBoxLoadLumpFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxLoadLumpFileActionPerformed(evt);
            }
        });

        comboBoxMapFormat.setModel(getAppIDModel());
        comboBoxMapFormat.setToolTipText("<html>\n<p>Overrides the internal game detection for maps.</p>\n<p>Select <i>\"Unknown\"</i> for automatic detection.</p>\n<br>\n<b>Warning:</b> Change only if the game isn't detected<br>\ncorrectly, wrong values can cause program errors!\n</html>");
        comboBoxMapFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxMapFormatActionPerformed(evt);
            }
        });

        labelMapFormat.setText("BSP format");

        checkBoxVisgroups.setText("Create Hammer visgroups");
        checkBoxVisgroups.setToolTipText("<html>Automatically group instanced entities to visgroups.\n<p><b>Note:</b> World brushes created from instances can't<br>\nbe grouped because of missing information.</p>\n</html>");
        checkBoxVisgroups.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxVisgroupsActionPerformed(evt);
            }
        });

        checkBoxCameras.setText("Create Hammer cameras");
        checkBoxCameras.setToolTipText("<html>Create Hammer viewport cameras above <br>\neach player spawn to ease navigation.</html>");
        checkBoxCameras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxCamerasActionPerformed(evt);
            }
        });

        checkBoxExtractEmbedded.setText("Extract embedded files");
        checkBoxExtractEmbedded.setToolTipText("<html>\nExtract all resource files that are embedded into the BSP file.\n</html>");
        checkBoxExtractEmbedded.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxExtractEmbeddedActionPerformed(evt);
            }
        });

        labelSourceFormat.setText("VMF format");

        comboBoxSourceFormat.setModel(getSourceFormatModel());
        comboBoxSourceFormat.setToolTipText("<html>\n<p>Sets the VMF source format.</p>\n<p>On default, newer maps are decompiled to a format<br/>\nthat is incompatible with older Hammer versions. <br/>\nSelect <i>\"Source 2004-2009\"</i> if you want to make sure that<br/>\nthe decompiled map is loadable in old Hammer versions.\n</html>");
        comboBoxSourceFormat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxSourceFormatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelOtherLayout = new javax.swing.GroupLayout(panelOther);
        panelOther.setLayout(panelOtherLayout);
        panelOtherLayout.setHorizontalGroup(
            panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOtherLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkBoxExtractEmbedded)
                    .addGroup(panelOtherLayout.createSequentialGroup()
                        .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxDebugMode)
                            .addComponent(checkBoxLoadLumpFile))
                        .addGap(18, 18, 18)
                        .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkBoxCameras)
                            .addComponent(checkBoxVisgroups)))
                    .addGroup(panelOtherLayout.createSequentialGroup()
                        .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelSourceFormat)
                            .addComponent(labelMapFormat))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(comboBoxMapFormat, 0, 190, Short.MAX_VALUE)
                            .addComponent(comboBoxSourceFormat, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        panelOtherLayout.setVerticalGroup(
            panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelOtherLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelOtherLayout.createSequentialGroup()
                        .addComponent(checkBoxDebugMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxLoadLumpFile))
                    .addGroup(panelOtherLayout.createSequentialGroup()
                        .addComponent(checkBoxVisgroups)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxCameras)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(checkBoxExtractEmbedded)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelMapFormat)
                    .addComponent(comboBoxMapFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSourceFormat)
                    .addComponent(comboBoxSourceFormat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(99, Short.MAX_VALUE))
        );

        tabbedPaneOptions.addTab("Other", panelOther);

        buttonDecompile.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        buttonDecompile.setText("Decompile");
        buttonDecompile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDecompileActionPerformed(evt);
            }
        });

        buttonDefaults.setText("Defaults");
        buttonDefaults.setToolTipText("Resets all configurations to their defaults.");
        buttonDefaults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDefaultsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPaneOptions, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(buttonDefaults)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonDecompile)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPaneOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonDefaults)
                    .addComponent(buttonDecompile))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkBoxLadderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxLadderActionPerformed
        config.writeLaddersAsEntities = checkBoxLadder.isSelected();
    }//GEN-LAST:event_checkBoxLadderActionPerformed

    private void checkBoxAddUseLandmarkAnglesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkBoxAddUseLandmarkAnglesActionPerformed
        config.addUseLandmarkAngles = checkBoxAddUseLandmarkAngles.isSelected();
    }//GEN-LAST:event_checkBoxAddUseLandmarkAnglesActionPerformed

    private void checkBoxEnableEntitiesActionPerformed(java.awt.event.ActionEvent evt) {                                                       
        config.setWriteEntities(checkBoxEnableEntities.isSelected());
        setPanelEnabled(panelEntities, checkBoxEnableEntities);
    }                                                      

    private void checkBoxPropStaticActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        config.writeStaticProps = checkBoxPropStatic.isSelected();
    }                                                  

    private void checkBoxAreaportalActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        config.writeAreaportals = checkBoxAreaportal.isSelected();
    }                                                  

    private void checkBoxOccluderActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        config.writeOccluders = checkBoxOccluder.isSelected();
    }                                                

    private void checkBoxDetailActionPerformed(java.awt.event.ActionEvent evt) {                                               
        config.writeDetails = checkBoxDetail.isSelected();
    }                                              

    private void checkBoxOverlayActionPerformed(java.awt.event.ActionEvent evt) {                                                
        config.writeOverlays = checkBoxOverlay.isSelected();
    }                                               

    private void checkBoxCubemapActionPerformed(java.awt.event.ActionEvent evt) {                                                
        config.writeCubemaps = checkBoxCubemap.isSelected();
    }                                               

    private void checkBoxFixRotationActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        config.fixEntityRot = checkBoxFixRotation.isSelected();
    }                                                   

    private void buttonDecompileActionPerformed(java.awt.event.ActionEvent evt) {                                                
        if (listFilesModel.isEmpty()) {
            // how the hell has the user bypassed the buttons?
            return;
        }

        // don't show file dialog for multiple bsp files
        if (listFilesModel.size() == 1) {
            BspFileEntry entry = listFilesModel.firstElement();
            File vmfFile = saveVmfFileDialog(entry.getVmfFile());

            if (vmfFile == null) {
                // the user obviously doesn't want to decompile...
                return;
            }

            entry.setVmfFile(vmfFile);
            entry.setPakDir(new File(vmfFile.getAbsoluteFile().getParentFile(),
                    entry.getPakDir().getName()));
        } else {
            File dstDir = selectDirectoryDialog(null);

            if (dstDir == null) {
                // the user obviously doesn't want to decompile...
                return;
            }

            // update paths with new destination dir
            Enumeration<BspFileEntry> entries = listFilesModel.elements();
            while(entries.hasMoreElements()) {
                BspFileEntry entry = entries.nextElement();
                entry.setVmfFile(new File(dstDir, entry.getVmfFile().getName()));
                entry.setPakDir(new File(dstDir, entry.getPakDir().getName()));
            }
        }

        startBspSource();
    }                                               

    private void buttonDefaultsActionPerformed(java.awt.event.ActionEvent evt) {                                               
        reset();
    }                                              

    private void checkBoxDispActionPerformed(java.awt.event.ActionEvent evt) {                                             
        config.writeDisp = checkBoxDisp.isSelected();
    }                                            

    private void comboBoxFaceTexActionPerformed(java.awt.event.ActionEvent evt) {                                                
        EnumToolTexture tex = (EnumToolTexture)comboBoxFaceTex.getSelectedItem();
        config.faceTexture = tex.texPath;
    }                                               

    private void comboBoxBackfaceTexActionPerformed(java.awt.event.ActionEvent evt) {                                                    
        EnumToolTexture tex = (EnumToolTexture)comboBoxBackfaceTex.getSelectedItem();
        config.backfaceTexture = tex.texPath;
    }                                                   

    private void checkBoxFixCubemapTexActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        config.fixCubemapTextures = checkBoxFixCubemapTex.isSelected();
    }                                                     

    private void checkBoxDebugModeActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        config.setDebug(checkBoxDebugMode.isSelected());
    }                                                 

    private void checkBoxLoadLumpFileActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        config.loadLumpFiles = checkBoxLoadLumpFile.isSelected();
    }                                                    

    private void buttonRemoveAllActionPerformed(java.awt.event.ActionEvent evt) {                                                
        listFilesModel.clear();
        buttonDecompile.setEnabled(false);
    }                                               

    private void buttonRemoveActionPerformed(java.awt.event.ActionEvent evt) {                                             
        int[] selected = listFiles.getSelectedIndices();
        listFiles.clearSelection();

        for (int index : selected) {
            listFilesModel.remove(index);
        }

        buttonDecompile.setEnabled(!listFilesModel.isEmpty());
    }                                            

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {                                          
        File bspFile = null;

        if (listFilesModel.size() == 1) {
            bspFile = listFilesModel.firstElement().getBspFile();
        }

        File[] bspFiles = openBspFileDialog(bspFile);

        if (bspFiles == null) {
            // selection canceled
            return;
        }

        for (File file : bspFiles) {
            listFilesModel.addElement(new BspFileEntry(file));
        }

        buttonDecompile.setEnabled(!listFilesModel.isEmpty());
    }

    private void comboBoxMapFormatActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        config.defaultApp = (SourceApp) comboBoxMapFormat.getSelectedItem();
    }                                                 

    private void checkBoxEnableWorldBrushesActionPerformed(java.awt.event.ActionEvent evt) {                                                           
        config.writeWorldBrushes = checkBoxEnableWorldBrushes.isSelected();
        setPanelEnabled(panelWorldBrushes, checkBoxEnableWorldBrushes);
    }                                                          

    private void checkBoxVisgroupsActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        config.writeVisgroups = checkBoxVisgroups.isSelected();
    }                                                 

    private void checkBoxCamerasActionPerformed(java.awt.event.ActionEvent evt) {                                                
        config.writeCameras = checkBoxCameras.isSelected();
    }                                               

    private void radioButtonBrushesPlanesActionPerformed(java.awt.event.ActionEvent evt) {                                                         
        config.brushMode = BrushMode.BRUSHPLANES;
    }                                                        

    private void radioButtonOrigFacesActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        config.brushMode = BrushMode.ORIGFACE;
    }                                                    

    private void radioButtonOrigSplitFacesActionPerformed(java.awt.event.ActionEvent evt) {                                                          
        config.brushMode = BrushMode.ORIGFACE_PLUS;
    }                                                         

    private void radioButtonSplitFacesActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        config.brushMode = BrushMode.SPLITFACE;
    }                                                     

    private void checkBoxFixToolTexActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        config.fixToolTextures = checkBoxFixToolTex.isSelected();
    }                                                  

    private void checkBoxExtractEmbeddedActionPerformed(java.awt.event.ActionEvent evt) {                                                        
        config.unpackEmbedded = checkBoxExtractEmbedded.isSelected();
    }                                                       

    private void comboBoxSourceFormatActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        config.sourceFormat = (SourceFormat)comboBoxSourceFormat.getSelectedItem();
    }                                                    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JButton buttonDecompile;
    private javax.swing.JButton buttonDefaults;
    private javax.swing.ButtonGroup buttonGroupBrushMode;
    private javax.swing.JButton buttonRemove;
    private javax.swing.JButton buttonRemoveAll;
    private javax.swing.JCheckBox checkBoxAddUseLandmarkAngles;
    private javax.swing.JCheckBox checkBoxAreaportal;
    private javax.swing.JCheckBox checkBoxCameras;
    private javax.swing.JCheckBox checkBoxCubemap;
    private javax.swing.JCheckBox checkBoxDebugMode;
    private javax.swing.JCheckBox checkBoxDetail;
    private javax.swing.JCheckBox checkBoxDisp;
    private javax.swing.JCheckBox checkBoxEnableEntities;
    private javax.swing.JCheckBox checkBoxEnableWorldBrushes;
    private javax.swing.JCheckBox checkBoxExtractEmbedded;
    private javax.swing.JCheckBox checkBoxFixCubemapTex;
    private javax.swing.JCheckBox checkBoxFixRotation;
    private javax.swing.JCheckBox checkBoxFixToolTex;
    private javax.swing.JCheckBox checkBoxLadder;
    private javax.swing.JCheckBox checkBoxLoadLumpFile;
    private javax.swing.JCheckBox checkBoxOccluder;
    private javax.swing.JCheckBox checkBoxOverlay;
    private javax.swing.JCheckBox checkBoxPropStatic;
    private javax.swing.JCheckBox checkBoxVisgroups;
    private javax.swing.JComboBox comboBoxBackfaceTex;
    private javax.swing.JComboBox comboBoxFaceTex;
    private javax.swing.JComboBox comboBoxMapFormat;
    private javax.swing.JComboBox comboBoxSourceFormat;
    private javax.swing.JLabel labelBackfaceTex;
    private javax.swing.JLabel labelDnDTip;
    private javax.swing.JLabel labelFaceTex;
    private javax.swing.JLabel labelMapFormat;
    private javax.swing.JLabel labelSourceFormat;
    private javax.swing.JList listFiles;
    private javax.swing.JPanel panelBrushEnts;
    private javax.swing.JPanel panelBrushMode;
    private javax.swing.JPanel panelEntities;
    private javax.swing.JPanel panelFiles;
    private javax.swing.JPanel panelOther;
    private javax.swing.JPanel panelPointEnts;
    private javax.swing.JPanel panelTextures;
    private javax.swing.JPanel panelWorldBrushes;
    private javax.swing.JRadioButton radioButtonBrushesPlanes;
    private javax.swing.JRadioButton radioButtonOrigFaces;
    private javax.swing.JRadioButton radioButtonOrigSplitFaces;
    private javax.swing.JRadioButton radioButtonSplitFaces;
    private javax.swing.JScrollPane scrollFiles;
    private javax.swing.JTabbedPane tabbedPaneOptions;
    // End of variables declaration//GEN-END:variables
}
