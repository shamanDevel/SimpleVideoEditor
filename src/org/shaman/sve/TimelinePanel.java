/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.shaman.sve.player.Player;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.openide.util.Lookup;
import org.shaman.sve.filters.CloneableFilter;
import org.shaman.sve.filters.FilterFactory;
import org.shaman.sve.model.*;

/**
 *
 * @author Sebastian
 */
public class TimelinePanel extends javax.swing.JPanel implements PropertyChangeListener {
	private static final Logger LOG = Logger.getLogger(TimelinePanel.class.getName());

	private Project project;
	private UndoableEditSupport undoSupport;
	private Selections selections;
	private Player player;
	
	private JXTreeTable table;
	private TimelineTreeTableModel tableModel;
	private CloneableFilter selectedFilter;
	
	/**
	 * Creates new form TimelinePanel
	 */
	public TimelinePanel() {
		initComponents();
		
		tableModel = new TimelineTreeTableModel();
		table = new JXTreeTable(tableModel);
//		table.setPreferredSize(new Dimension(400, 160));
		table.setRootVisible(false);
		table.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent tse) {
				timelineObjectSelected(tse.getNewLeadSelectionPath());
			}
		});
		table.addMouseListener(new TableMouseListener(table));
		timelinePanel.setLayout(new BorderLayout());
		timelinePanel.add(new JScrollPane(table));
		timelinePanel.setPreferredSize(new Dimension(400, 158));
		table.setDefaultRenderer(Object.class, new TimelineCellRenderer());
		table.getColumn(1).setCellRenderer(new TimelineCellRenderer());
	}
	
	public void setProject(Project project) {
		this.project = project;
		project.addPropertyChangeListener(this);
		
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

	public void setSelections(Selections selections) {
		this.selections = selections;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		if (pce.getSource() == project) {
			if (player.isRecording()) {
				return;
			}
			switch (pce.getPropertyName()) {
				case Project.PROP_LENGTH:
				case Project.PROP_TIME:
					table.repaint();
					break;
				case Project.PROP_TIMELINE_OBJECTS_CHANGED:
					tableModel.fireUpdate();
					break;
				case Project.PROP_TIMELINE_OBJECT_CHANGED:
					tableModel.fireCurrentUpdate();
					break;
			}
		}
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
		
		public void fireCurrentUpdate() {
			for (TreeModelListener l : getTreeModelListeners()) {
				l.treeNodesChanged(new TreeModelEvent(this, table.getTreeSelectionModel().getLeadSelectionPath()));
			}
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public boolean isCellEditable(Object node, int column) {
//			if ((node instanceof TimelineObject) && (column == 0)) {
//				return true; //edit name of the object
//			}
			return false;
		}

		@Override
		public void setValueAt(Object value, Object node, int column) {
//			if ((node instanceof TimelineObject) && (column == 0)) {
//				((TimelineObject) node).setName((String) value);
//			}
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
			} else if (parent instanceof TimelineObject) {
				return ((TimelineObject) parent).getChildren().get(index);
			} else {
				return null;
			}
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent == root && project != null) {
				return project.getTimelineObjects().size();
			} else if (parent instanceof TimelineObject) {
				return ((TimelineObject) parent).getChildren().size();
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent == root && project != null) {
				return project.getTimelineObjects().indexOf(child);
			} else if (parent instanceof TimelineObject) {
				return ((TimelineObject) parent).getChildren().indexOf(child);
			}
			return -1;
		}
		
	}
	
	/**
	 * Adds the specified timeline object and deals with undo redo.
	 * @param obj 
	 */
	private void addTimelineObject(final TimelineObject obj) {
		project.addTimelineObject(obj);
		tableModel.fireUpdate();
		LOG.log(Level.INFO, "timeline object added: {0}", obj);
		undoSupport.postEdit(new AbstractUndoableEdit() {
			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				project.removeTimelineObject(obj);
				tableModel.fireUpdate();
				LOG.info("undo: add timeline object");
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				project.addTimelineObject(obj);
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
		TimelineObject obj = null;
		if (res instanceof AudioResource) {
			AudioTimelineObject o = new AudioTimelineObject((AudioResource) res);
			o.setDuration((int)Math.ceil(((AudioResource)res).getSample().getLength()));
			obj = o;
		} else if (res instanceof Resource.ImageProvider) {
			ImageTimelineObject o = new ImageTimelineObject(res);
			BufferedImage i = ((Resource.ImageProvider) res).getFrame(0, false);
			o.setWidth(i.getWidth());
			o.setHeight(i.getHeight());
			o.setAspect(i.getWidth() / (float) i.getHeight());
			if (res instanceof VideoResource) {
				o.setDuration(((VideoResource) res).getDurationInMsec());
			} else { //image
				o.setDuration(10000); //10sec
			}
			obj = o;
		} else {
			LOG.log(Level.WARNING, "unknown resource type: {0}", res);
			return;
		}
		//TODO: video + image
		addTimelineObject(obj);
	}
	
	private class TimelineCellRenderer extends JPanel implements TableCellRenderer {
		private int start;
		private int duration;
		private int length;
		private int time;
		private Color color;
		
		public TimelineCellRenderer() {
			setOpaque(true);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object obj, boolean isSelected, boolean hasFocus, int row, int column) {
			start = 0;
			duration = 0;
			length = (project.getLength()==null) ? 0 : project.getLength().toMillis();
			color = Color.BLACK;
			time = (project.getTime()==null) ? 0 : project.getTime().toMillis();
			
			if (obj instanceof ResourceTimelineObject) {
				@SuppressWarnings("unchecked")
				ResourceTimelineObject<Resource> to = (ResourceTimelineObject<Resource>) obj;
				start = to.getStart();
				duration = to.getDuration();
				if (to.getResource() instanceof AudioResource) {
					color = Color.BLUE;
				} else if (to.getResource() instanceof ImageResource) {
					color = Color.ORANGE;
				} else { //video
					color = Color.GREEN;
				}
			} else if (obj instanceof TimelineObject) {
				//filter
				start = ((TimelineObject) obj).getGlobalStart();
				duration = ((TimelineObject) obj).getGlobalDuration();
				color = Color.GRAY;
			}
			
			if (isSelected) {
				setBackground(table.getSelectionBackground());
			} else {
				setBackground(table.getBackground());
			}
			
			return this;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			if (length == 0) return;
			int w = getWidth();
			int h = getHeight();
			int intent = 3;
			int sx = w * start / length;
			int ex = w * (start+duration) / length;
			int sy = intent;
			int ey = getHeight() - sy;
			g.setColor(color);
			g.fillRect(sx, sy, ex-sx, ey-sy);
			g.setColor(color.darker());
			g.drawRect(sx, sy, ex-sx, ey-sy);
			int tx = w * time / length;
			g.setColor(Color.RED);
			g.fillRect(tx, 0, 2, h);
		}

	}
	
	private void addFilter(final ResourceTimelineObject<Resource> obj, FilterFactory factory) {
		final TimelineObject child = factory.createFilter(obj);
		obj.addChild(child);
		addFilter(obj, child);
	}
	private void addFilter(final TimelineObject obj, final TimelineObject child) {
		tableModel.fireUpdate();
		LOG.log(Level.INFO, "filter {0} added to {1}", new Object[]{child, obj});
		undoSupport.postEdit(new AbstractUndoableEdit(){

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				obj.removeChild(child);
				tableModel.fireUpdate();
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				obj.addChild(child);
				tableModel.fireUpdate();
			}
		});
	}
	
	private void triggerPopup(TimelineObject tobj, Point mouse) {
		LOG.info("trigger popup on "+tobj);
		//until now, filters can only be applied to resources
		if (tobj instanceof ResourceTimelineObject) {
			@SuppressWarnings("unchecked")
			final ResourceTimelineObject<Resource> obj = (ResourceTimelineObject<Resource>) tobj;
			//test which filters can be applied
			ArrayList<FilterFactory> factories = new ArrayList<>();
			for (FilterFactory f : Lookup.getDefault().lookupAll(FilterFactory.class)) {
				if (f.isApplicable(obj)) {
					factories.add(f);
				}
			}
			Collections.sort(factories, new Comparator<FilterFactory>() {
				@Override
				public int compare(FilterFactory o1, FilterFactory o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			//build menu
			JPopupMenu popup = new JPopupMenu();
			if (selectedFilter != null) {
				JMenuItem pasteItem = new JMenuItem("paste: "+selectedFilter.toString());
				pasteItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TimelineObject child = selectedFilter.cloneForParent(obj);
						obj.addChild(child);
						addFilter(obj, child);
					}
				});
				popup.add(pasteItem);
			}
			Map<String, JMenu> items = new HashMap<>();
			for (final FilterFactory f : factories) {
				JMenu parent = null;
				int index = 0;
				while (index != -1) {
					index = f.getName().indexOf('/', index);
					if (index != -1) {
						String prefix = f.getName().substring(0, index);
						index++;
						JMenu item = items.get(prefix);
						if (item == null) {
							item = new JMenu(prefix.substring(prefix.lastIndexOf('/')+1));
							items.put(prefix, item);
							if (parent != null) {
								parent.add(item);
							} else {
								popup.add(item);
							}
						}
						parent = item;
					}
				}
				String name = f.getName().substring(f.getName().lastIndexOf('/')+1);
				JMenuItem item = new JMenuItem(name);
				parent.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						addFilter(obj, f);
					}
				});
			}
			popup.show(table, mouse.x, mouse.y);
		}
		else if (tobj instanceof CloneableFilter) {
			selectedFilter = (CloneableFilter) tobj;
			LOG.info(tobj+" copied");
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

        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        timelinePanel = new javax.swing.JPanel();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

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

        upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/up16.png"))); // NOI18N
        upButton.setToolTipText("moves the object up");
        upButton.setEnabled(false);
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonEvent(evt);
            }
        });

        downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/shaman/sve/icons/down16.png"))); // NOI18N
        downButton.setToolTipText("moves the object down");
        downButton.setEnabled(false);
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonEvent(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton)
                    .addComponent(removeButton)
                    .addComponent(upButton)
                    .addComponent(downButton))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downButton)
                .addContainerGap(26, Short.MAX_VALUE))
            .addComponent(timelinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonEvent
        Resource res = selections.getSelectedResource();
		if (res != null) {
			addResource(res);
		}
    }//GEN-LAST:event_addButtonEvent

    private void removeButtonEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonEvent
        final TimelineObject obj = selections.getSelectedTimelineObject();
		assert (obj != null);
		project.removeTimelineObject(obj);
		tableModel.fireUpdate();
		table.clearSelection();
		LOG.log(Level.INFO, "timeline object {0} removed", obj);
		undoSupport.postEdit(new AbstractUndoableEdit() {

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				project.addTimelineObject(obj);
				tableModel.fireUpdate();
				LOG.info("undo: remove timeline object");
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				project.removeTimelineObject(obj);
				tableModel.fireUpdate();
				LOG.info("redo: remove timeline object");
			}
		});
    }//GEN-LAST:event_removeButtonEvent

	private void move(TimelineObject obj, int dir) {
		int index = project.getTimelineObjects().indexOf(obj);
		int newIndex = index + dir;
		if (newIndex >= 0 && newIndex < project.getTimelineObjects().size()) {
			project.getTimelineObjects().remove(index);
			project.getTimelineObjects().add(newIndex, obj);
			project.fireTimelineObjectsChanged();
			tableModel.fireUpdate();
		}
	}
	
    private void upButtonEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonEvent
        final TimelineObject obj = selections.getSelectedTimelineObject();
		assert (obj != null);
		move(obj, -1);
		undoSupport.postEdit(new AbstractUndoableEdit(){

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				move(obj, 1);
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				move(obj, -1);
			}
			
		});
    }//GEN-LAST:event_upButtonEvent

    private void downButtonEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonEvent
        final TimelineObject obj = selections.getSelectedTimelineObject();
		assert (obj != null);
		move(obj, 1);
		undoSupport.postEdit(new AbstractUndoableEdit(){

			@Override
			public void undo() throws CannotUndoException {
				super.undo();
				move(obj, -1);
			}

			@Override
			public void redo() throws CannotRedoException {
				super.redo();
				move(obj, 1);
			}
			
		});
    }//GEN-LAST:event_downButtonEvent

	private void timelineObjectSelected(TreePath path) {
		if (path == null) {
			return;
		}
		Object leaf = path.getLastPathComponent();
		if (leaf instanceof TimelineObject) {
			selections.setSelectedTimelineObject((TimelineObject) leaf);
		} else {
			selections.setSelectedTimelineObject(null);
		}
		removeButton.setEnabled(selections.getSelectedTimelineObject() != null);
		upButton.setEnabled(selections.getSelectedTimelineObject() != null);
		downButton.setEnabled(selections.getSelectedTimelineObject() != null);
	}
	
	public class TableMouseListener extends MouseAdapter {

		private JTable table;

		public TableMouseListener(JTable table) {
			this.table = table;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			if (event.getButton() == MouseEvent.BUTTON3) {
				// selects the row at which point the mouse is clicked
				Point point = event.getPoint();
				int currentRow = table.rowAtPoint(point);
				table.setRowSelectionInterval(currentRow, currentRow);
				//trigger popup
				if (selections.getSelectedTimelineObject() != null) {
					triggerPopup(selections.getSelectedTimelineObject(), event.getPoint());
				}
			}
		}
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton downButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel timelinePanel;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
}
