/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import org.shaman.sve.player.Player;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;
import org.apache.commons.io.FileUtils;
import org.shaman.sve.model.AudioResource;
import org.shaman.sve.model.Project;
import org.shaman.sve.model.Resource;

/**
 *
 * @author Sebastian
 */
public class ResourcePanel extends javax.swing.JPanel {

	private static final Logger LOG = Logger.getLogger(ResourcePanel.class.getName());
	private static final String RESOURCE_FOLDER = "resources";
	private static final String AUDIO_FOLDER = "audio";
	private static final String IMAGE_FOLDER = "images";
	private static final String VIDEO_FOLDER = "videos";
	
	private Project project;
	private UndoableEditSupport undoSupport;
	private Player player;
	
	private DefaultListModel<Resource> listModel;
	
	/**
	 * Creates new form ResourcePanel
	 */
	public ResourcePanel() {
		listModel = new DefaultListModel();
		initComponents();
	}

	public void setProject(Project project) {
		this.project = project;
		//init resource list
		listModel.clear();
		for (Resource r : project.getResources()) {
			listModel.addElement(r);
		}
		
		addAudioButton.setEnabled(true);
		addImageButton.setEnabled(true);
		addVideoButton.setEnabled(true);
	}

	public void setUndoSupport(UndoableEditSupport undoSupport) {
		this.undoSupport = undoSupport;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Resource getSelectedResource() {
		int idx = resourceList.getSelectedIndex();
		if (idx >= 0) {
			return project.getResources().get(idx);
		} else {
			return null;
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

        jLabel1 = new javax.swing.JLabel();
        addAudioButton = new javax.swing.JButton();
        addImageButton = new javax.swing.JButton();
        addVideoButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        resourceList = new javax.swing.JList<>();
        bluescreenButton = new javax.swing.JButton();
        syncButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMinimumSize(new java.awt.Dimension(192, 200));

        jLabel1.setText("Resources");

        addAudioButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/plus16.png"))); // NOI18N
        addAudioButton.setText("audio");
        addAudioButton.setEnabled(false);
        addAudioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAudioEvent(evt);
            }
        });

        addImageButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/plus16.png"))); // NOI18N
        addImageButton.setText("image");
        addImageButton.setEnabled(false);
        addImageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addImageEvent(evt);
            }
        });

        addVideoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/plus16.png"))); // NOI18N
        addVideoButton.setText("video");
        addVideoButton.setEnabled(false);
        addVideoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVideoEvent(evt);
            }
        });

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/minus16.png"))); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeEvent(evt);
            }
        });

        resourceList.setModel((javax.swing.ListModel) listModel);
        resourceList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        resourceList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                selectionChangedEvent(evt);
            }
        });
        jScrollPane1.setViewportView(resourceList);

        bluescreenButton.setText("Bluescreen");
        bluescreenButton.setToolTipText("Remove background of a video");
        bluescreenButton.setEnabled(false);
        bluescreenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bluescreenEvent(evt);
            }
        });

        syncButton.setText("Sync");
        syncButton.setToolTipText("Sync audio and video");
        syncButton.setEnabled(false);
        syncButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addAudioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addImageButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addVideoButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bluescreenButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(syncButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(11, 11, 11))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addAudioButton)
                    .addComponent(addImageButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addVideoButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bluescreenButton)
                    .addComponent(syncButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void removeEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_removeEvent

    private void addAudioEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAudioEvent
        //load file
		JFileChooser fc = new JFileChooser(new File(Settings.get("RESOURCE_AUDIO", Settings.getLastDirectory().getAbsolutePath())));
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileNameExtensionFilter("audio", "mp3", "wav", "ogg"));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			Settings.set("RESOURCE_AUDIO", fc.getCurrentDirectory().getAbsolutePath());
			final File audioFile = fc.getSelectedFile();
			//copy file to resource folder
			File resourceFolder = new File(project.getFolder(), RESOURCE_FOLDER);
			if (!resourceFolder.exists()) resourceFolder.mkdir();
			File audioFolder = new File(resourceFolder, AUDIO_FOLDER);
			if (!audioFile.exists()) audioFile.mkdir();
			final File targetFile = new File(audioFolder, audioFile.getName());
			try {
				FileUtils.copyFile(audioFile, targetFile);
			} catch (IOException ex) {
				LOG.log(Level.SEVERE, null, ex);
				return;
			}
			//create resource
			final AudioResource res = new AudioResource(RESOURCE_FOLDER + "/" + AUDIO_FOLDER + "/" + audioFile.getName());
			project.getResources().add(res);
			player.loadResource(res);
			listModel.addElement(res);
			LOG.info("audio file copied and resource added");
			//add undo support
			undoSupport.postEdit(new AbstractUndoableEdit() {
				@Override
				public void undo() throws CannotUndoException {
					super.undo();
//					targetFile.delete();
					project.getResources().remove(res);
					listModel.removeElement(res);
					LOG.info("undo: add audio");
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
//					try {
//						FileUtils.copyFile(audioFile, targetFile); //copy agaim
//					} catch (IOException ex) {
//						LOG.log(Level.SEVERE, null, ex);
//						return;
//					}
					project.getResources().add(res);
					listModel.addElement(res);
					LOG.info("redo: add audio");
				}
			
			});
		}
    }//GEN-LAST:event_addAudioEvent

    private void addImageEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addImageEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_addImageEvent

    private void addVideoEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVideoEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_addVideoEvent

    private void bluescreenEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bluescreenEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_bluescreenEvent

    private void syncEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_syncEvent

    private void selectionChangedEvent(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_selectionChangedEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_selectionChangedEvent


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAudioButton;
    private javax.swing.JButton addImageButton;
    private javax.swing.JButton addVideoButton;
    private javax.swing.JButton bluescreenButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JList<String> resourceList;
    private javax.swing.JButton syncButton;
    // End of variables declaration//GEN-END:variables
}
