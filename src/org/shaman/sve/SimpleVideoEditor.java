/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
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
		
		undoManager = new UndoManager();
		undoSupport = new UndoableEditSupport();
		undoSupport.addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent uee) {
				UndoableEdit edit = uee.getEdit();
				undoManager.addEdit(edit);
				refreshUndoRedoUI();
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
		
		return resourcePanel;
	}
	private JPanel createPropertyView() {
		propertyPanel = new PropertyPanel();
		
		return propertyPanel;
	}
	private JPanel createMainView() {
		mainPanel = new MainPanel();
		
		return mainPanel;
	}
	private JPanel createTimelineView() {
		timelinePanel = new TimelinePanel();
		
		return timelinePanel;
	}
	
	private void closeProject() {
		
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
		}
	}
	private void loadProject() {
		LOG.info("load project");
		JFileChooser fc = new JFileChooser("E:\\Sebastian\\Programmierung\\Java\\SimpleVideoEditorTests");
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				File f = new File(file, PROJECT_FILE_NAME);
				return f.exists();
			}

			@Override
			public String getDescription() {
				return "Project";
			}
		});
		int ret = fc.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			closeProject();
			Serializer serializer = new Persister();
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
	}
	private void saveProject() {
		LOG.info("save project");
		Serializer serializer = new Persister();
		if (!project.getFolder().exists()) {
			project.getFolder().mkdir();
		}
		File target = new File(project.getFolder(), PROJECT_FILE_NAME);
		try {
			serializer.write(project, target);
			LOG.info("project saved");
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}
	private void exportProject() {
		
	}
	
	private void undo() {
		undoManager.undo();
		refreshUndoRedoUI();
	}
	private void redo() {
		undoManager.redo();
		refreshUndoRedoUI();
	}
	private void refreshUndoRedoUI() {
		undoAction.setEnabled(undoManager.canUndo());
		undoAction.putValue(Action.NAME, undoManager.getUndoPresentationName());
		redoAction.setEnabled(undoManager.canRedo());
		redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());
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
