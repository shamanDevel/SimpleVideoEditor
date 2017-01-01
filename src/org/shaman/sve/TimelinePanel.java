/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import org.shaman.sve.player.Player;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableColumn;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.shaman.sve.model.Project;
import org.shaman.sve.model.Resource;
import org.shaman.sve.model.TimelineObject;

/**
 *
 * @author Sebastian
 */
public class TimelinePanel extends javax.swing.JPanel {
	private static final Logger LOG = Logger.getLogger(TimelinePanel.class.getName());

	private Project project;
	private UndoableEditSupport undoSupport;
	private ResourcePanel resourcePanel;
	private Player player;
	
	private JXTreeTable table;
	private TimelineTreeTableModel tableModel;
	
	/**
	 * Creates new form TimelinePanel
	 */
	public TimelinePanel() {
		initComponents();
		
		tableModel = new TimelineTreeTableModel();
		table = new JXTreeTable(tableModel);
//		table.setPreferredSize(new Dimension(400, 160));
		table.setRootVisible(false);
		timelinePanel.setLayout(new BorderLayout());
		timelinePanel.add(new JScrollPane(table));
		timelinePanel.setPreferredSize(new Dimension(400, 158));
	}
	
	public void setProject(Project project) {
		this.project = project;
		//init timeline
		tableModel.fireUpdate();
		TableColumn c = table.getColumn(0);
		c.setResizable(false);
		int w = 100;
		c.setMaxWidth(w);
		c.setMinWidth(w);
		c.setPreferredWidth(w);
		c.setWidth(w);
		
		addButton.setEnabled(true);
	}

	public void setUndoSupport(UndoableEditSupport undoSupport) {
		this.undoSupport = undoSupport;
	}

	public void setResourcePanel(ResourcePanel resourcePanel) {
		this.resourcePanel = resourcePanel;
		assert (resourcePanel != null);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private class TimelineTreeTableModel extends AbstractTreeTableModel {

		public TimelineTreeTableModel() {
			super(new Object());
		}
		
		public void fireUpdate() {
			for (TreeModelListener l : getTreeModelListeners()) {
				l.treeStructureChanged(new TreeModelEvent(this, new Object[]{project}));
			}
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public boolean isCellEditable(Object node, int column) {
			if ((node instanceof TimelineObject) && (column == 0)) {
				return true; //edit name of the object
			}
			return false;
		}

		@Override
		public void setValueAt(Object value, Object node, int column) {
			if ((node instanceof TimelineObject) && (column == 0)) {
				((TimelineObject) node).setName((String) value);
			}
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 0) {
				return String.class;
			} else if (column == 1) {
				return Object.class;
			} else {
				return Object.class;
			}
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Object";
			} else if (column == 1) {
				return "Timeline";
			} else {
				return "";
			}
		}

		@Override
		public Object getValueAt(Object node, int column) {
			if (node instanceof TimelineObject) {
				TimelineObject to = (TimelineObject) node;
				if (column == 0) {
					return to.toString();
				} else if (column == 1) {
					return to; //timeline display
				}
			}
			return null;
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent == root && project != null) {
				return project.getTimelineObjects().get(index);
			}
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent == root && project != null) {
				return project.getTimelineObjects().size();
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent == root && project != null) {
				return project.getTimelineObjects().indexOf(child);
			}
			return -1;
		}
		
	}
	
	/**
	 * Adds the specified timeline object and deals with undo redo.
	 * @param obj 
	 */
	private void addTimelineObject(final TimelineObject obj) {
		project.getTimelineObjects().add(obj);
		tableModel.fireUpdate();
		player.initTimelineObject(obj);
		LOG.log(Level.INFO, "timeline object added: {0}", obj);
		undoSupport.postEdit(new AbstractUndoableEdit() {
			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				project.getTimelineObjects().remove(obj);
				tableModel.fireUpdate();
				LOG.info("undo: add timeline object");
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				project.getTimelineObjects().add(obj);
				tableModel.fireUpdate();
				LOG.info("redo: add timeline object");
			}
		
		});
	}
	
	/**
	 * Adds the specified resource as timeline object
	 * @param res 
	 */
	private void addResource(Resource res) {
		TimelineObject obj = new TimelineObject();
		obj.setResource(res);
		//initialize object
		//todo: where is the right place to do this?
		obj.setProperty(TimelineObject.PROP_START, (int) 0);
		addTimelineObject(obj);
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        timelinePanel = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setMinimumSize(new java.awt.Dimension(400, 100));

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/plus16.png"))); // NOI18N
        addButton.setToolTipText("add selected resource to the timeline");
        addButton.setEnabled(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonEvent(evt);
            }
        });

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/minus16.png"))); // NOI18N
        removeButton.setToolTipText("Remove the selected object");
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonEvent(evt);
            }
        });

        javax.swing.GroupLayout timelinePanelLayout = new javax.swing.GroupLayout(timelinePanel);
        timelinePanel.setLayout(timelinePanelLayout);
        timelinePanelLayout.setHorizontalGroup(
            timelinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 580, Short.MAX_VALUE)
        );
        timelinePanelLayout.setVerticalGroup(
            timelinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timelinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton)
                .addContainerGap(88, Short.MAX_VALUE))
            .addComponent(timelinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonEvent
        Resource res = resourcePanel.getSelectedResource();
		if (res != null) {
			addResource(res);
		}
    }//GEN-LAST:event_addButtonEvent

    private void removeButtonEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonEvent
        // TODO add your handling code here:
    }//GEN-LAST:event_removeButtonEvent


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel timelinePanel;
    // End of variables declaration//GEN-END:variables
}
