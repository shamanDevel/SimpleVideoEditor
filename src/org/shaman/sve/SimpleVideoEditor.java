/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import org.shaman.sve.player.Player;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.JXMultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.shaman.sve.model.Project;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author Sebastian Weiss
 */
public class SimpleVideoEditor extends JFrame {

	private static final Logger LOG = Logger.getLogger(SimpleVideoEditor.class.getName());
	private static final String PROJECT_FILE_NAME = "project.xml";

	//project
	private Project project;
	private boolean changed;
	private Player player;
	private Selections selections;
	
	//UI
	private JToolBar toolBar;
	private PropertyPanel propertyPanel;
	private ResourcePanel resourcePanel;
	private TimelinePanel timelinePanel;
	private MainPanel mainPanel;
	
	private Action newProjectAction;
	private Action loadProjectAction;
	private Action saveProjectAction;
	private Action undoAction;
	private Action redoAction;
	private Action exportProjectAction;
	
	private UndoManager undoManager;
	private UndoableEditSupport undoSupport;
	
	public SimpleVideoEditor() throws HeadlessException {
		getContentPane().setLayout(new BorderLayout());
		
		selections = new Selections();
		undoManager = new UndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent uee) {
				UndoableEdit edit = uee.getEdit();
				undoManager.addEdit(edit);
				refreshUndoRedoUI();
				enableSave();
			}
		});
		
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		
		JPanel left = createResourceView();
		JPanel right = createPropertyView();
		JPanel center = createMainView();
		JPanel bottom = createTimelineView();
		
		String layoutDef = "(COLUMN (ROW weight=1.0 left (LEAF weight=1.0 name=center) right) bottom)";
		JXMultiSplitPane sp = new JXMultiSplitPane();
		MultiSplitLayout.Node root = MultiSplitLayout.parseModel(layoutDef);
		sp.getMultiSplitLayout().setModel(root);
		sp.add(left, "left");
		sp.add(right, "right");
		sp.add(center, "center");
		sp.add(bottom, "bottom");
		sp.setContinuousLayout(true);
		getContentPane().add(sp, BorderLayout.CENTER);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent we) {
				Settings.flush();
			}
		});
	}
	private JToolBar createToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		ClassLoader cl = SimpleVideoEditor.class.getClassLoader();
		newProjectAction = new AbstractAction("New", new ImageIcon(cl.getResource("org/shaman/sve/icons/new24.png"))) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				newProject();
			}
		};
		toolBar.add(newProjectAction);
		loadProjectAction = new AbstractAction("Load", new ImageIcon(cl.getResource("org/shaman/sve/icons/open24.png"))) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				loadProject();
			}
		};
		toolBar.add(loadProjectAction);
		saveProjectAction = new AbstractAction("Save", new ImageIcon(cl.getResource("org/shaman/sve/icons/save24.png"))) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				saveProject();
			}
		};
		saveProjectAction.setEnabled(false);
		toolBar.add(saveProjectAction);
		undoAction = new AbstractAction("Undo", new ImageIcon(cl.getResource("org/shaman/sve/icons/undo24.gif"))) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				undo();
			}
		};
		undoAction.setEnabled(false);
		toolBar.add(undoAction);
		redoAction = new AbstractAction("Redo", new ImageIcon(cl.getResource("org/shaman/sve/icons/redo24.gif"))) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				redo();
			}
		};
		redoAction.setEnabled(false);
		toolBar.add(redoAction);
		exportProjectAction = new AbstractAction("Export", new ImageIcon(cl.getResource("org/shaman/sve/icons/export24.png"))) {
			@Override
			public void actionPerformed(ActionEvent ae) {
				exportProject();
			}
		};
		exportProjectAction.setEnabled(false);
		toolBar.add(exportProjectAction);
		
		return toolBar;
	}
	private JPanel createResourceView() {
		resourcePanel = new ResourcePanel();
		resourcePanel.setUndoSupport(undoSupport);
		resourcePanel.setSelections(selections);
		return resourcePanel;
	}
	private JPanel createPropertyView() {
		propertyPanel = new PropertyPanel();
		propertyPanel.setUndoSupport(undoSupport);
		propertyPanel.setSelections(selections);
		return propertyPanel;
	}
	private JPanel createMainView() {
		mainPanel = new MainPanel();
		mainPanel.setUndoSupport(undoSupport);
		mainPanel.setSelections(selections);
		return mainPanel;
	}
	private JPanel createTimelineView() {
		timelinePanel = new TimelinePanel();
		timelinePanel.setUndoSupport(undoSupport);
		timelinePanel.setSelections(selections);
		return timelinePanel;
	}
	
	private void closeProject() {
		//close player
		if (player != null) {
			player.stop();
			player.destroy();
			player = null;
		}
		
		//disable buttons
		saveProjectAction.setEnabled(false);
		exportProjectAction.setEnabled(false);
		
		//clear undo redo
		undoManager.discardAllEdits();
		refreshUndoRedoUI();
		
		LOG.info("old project closed");
	}
	
	private void newProject() {
		LOG.info("create new project");
		project = NewProjectDialog.showDialog(this);
		if (project != null) {
			closeProject();
			LOG.info("new project created");
			project.setVersion(1);
			project.setFolder(new File(project.getFolder(), project.getName()));
			saveProjectAction.setEnabled(true);
			exportProjectAction.setEnabled(true);
			projectLoaded();
			changed = true;
		}
	}
	private boolean isProjectDir(File dir, boolean dialog) {
		File f = new File(dir, PROJECT_FILE_NAME);
		if (f.exists()) {
			return true;
		}
		if (dialog) {
			JOptionPane.showMessageDialog(this, "The selected folder does not contain a project");
		}
		return false;
	}
	private void loadProject() {
		LOG.info("load project");
		JFileChooser fc = new JFileChooser(Settings.getLastDirectory());
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (!file.isDirectory()) return false;
				if (isProjectDir(file, false)) return true;
				for (File fx : file.listFiles()) {
					if (fx.isDirectory()) return true;
				}
				return false;
			}

			@Override
			public String getDescription() {
				return "Project";
			}
		});
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			if (!isProjectDir(fc.getSelectedFile(), true)) {
				return;
			}
			Settings.setLastDirectory(fc.getCurrentDirectory());
			closeProject();
			Serializer serializer = SerializationUtils.createSerializer();
			File source = new File(fc.getSelectedFile(), PROJECT_FILE_NAME);
			try {
				project = serializer.read(Project.class, source);
				projectLoaded();
				LOG.info("project loaded");
			} catch (Exception ex) {
				LOG.log(Level.SEVERE, null, ex);
			}
		}
	}
	/**
	 * initialize project
	 */
	private void projectLoaded() {
		setTitle(project.getFolder().getAbsolutePath());
		resourcePanel.setProject(project);
		timelinePanel.setProject(project);
		mainPanel.setProject(project);
		
		//init player
		player = new Player(project);
		player.loadResources();
		player.initTimelineObjects();
		resourcePanel.setPlayer(player);
		timelinePanel.setPlayer(player);
		mainPanel.setPlayer(player);
	}
	private void saveProject() {
		LOG.info("save project");
		Serializer serializer = SerializationUtils.createSerializer();
		if (!project.getFolder().exists()) {
			project.getFolder().mkdir();
		}
		File target = new File(project.getFolder(), PROJECT_FILE_NAME);
		try {
			serializer.write(project, target);
			LOG.info("project saved");
			changed = false;
			saveProjectAction.setEnabled(false);
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	private void exportProject() {
		
	}
	
	private void undo() {
		undoManager.undo();
		refreshUndoRedoUI();
		enableSave();
	}
	private void redo() {
		undoManager.redo();
		refreshUndoRedoUI();
		enableSave();
	}
	private void refreshUndoRedoUI() {
		undoAction.setEnabled(undoManager.canUndo());
		undoAction.putValue(Action.NAME, undoManager.getUndoPresentationName());
		redoAction.setEnabled(undoManager.canRedo());
		redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());
	}
	private void enableSave() {
		changed = true;
		saveProjectAction.setEnabled(true);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		SimpleVideoEditor sve = new SimpleVideoEditor();
		sve.pack();
		sve.setVisible(true);
		sve.setLocationRelativeTo(null);
	}
	
}
