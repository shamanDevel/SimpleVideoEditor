/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.undo.*;
import org.shaman.sve.player.Player;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * This class represents an object in the timeline.
 * The {@link Player} is responsible for interpreting the object.
 * @author Sebastian Weiss
 */
public class TimelineObject {
	private static final Logger LOG = Logger.getLogger(TimelineObject.class.getName());
	public static final String PROP_DURATION = "duration";
	public static final String PROP_START = "start";
	
	protected transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	@Element
	protected String name;
	public static final String PROP_NAME = "name";
	
	@Element
	protected boolean enabled = true;
	public static final String PROP_ENABLED = "enabled";
	
	protected UndoableEditSupport undoSupport;
	
	/**
	 * Storage for the player
	 */
	public final HashMap<String, Object> playerProperties = new HashMap<>();
	
	@ElementList(required = false)
	protected ArrayList<TimelineObject> children = new ArrayList<>();
	public static final String PROP_CHILD_ADDED = "child+";
	public static final String PROP_CHILD_REMOVED = "child-";
	public static final String PROP_CHILD_MODIFIED = "childMod";
	
	@Element(required = false)
	protected TimelineObject parent;
	public static final String PROP_PARENT_CHANGED = "parent";
	@Element
	protected int duration;
	@Element
	protected int start;

	public void setUndoSupport(UndoableEditSupport undoSupport) {
		this.undoSupport = undoSupport;
	}

	/**
	 * Add PropertyChangeListener.
	 *
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Remove PropertyChangeListener.
	 *
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Get the value of name
	 *
	 * @return the value of name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name
	 *
	 * @param newName new value of name
	 */
	public void setName(final String newName) {
		final String oldName = this.name;
		this.name = newName;
		propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, newName);
		if (!Objects.equals(oldName, newName) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					name = oldName;
					propertyChangeSupport.firePropertyChange(PROP_NAME, newName, oldName);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					name = newName;
					propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, newName);
				}
			
			});
		}
	}

	/**
	 * Get the value of enabled
	 *
	 * @return the value of enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set the value of enabled
	 *
	 * @param newEnabled new value of enabled
	 */
	public void setEnabled(final boolean newEnabled) {
		final boolean oldEnabled = this.enabled;
		this.enabled = newEnabled;
		propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, newEnabled);
		if (!Objects.equals(oldEnabled, newEnabled) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					enabled = oldEnabled;
					propertyChangeSupport.firePropertyChange(PROP_ENABLED, newEnabled, oldEnabled);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					enabled = newEnabled;
					propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, newEnabled);
				}
			
			});
		}
	}

	public ArrayList<TimelineObject> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<TimelineObject> children) {
		this.children = children;
	}
	
	public void addChild(TimelineObject child) {
		children.add(child);
		propertyChangeSupport.firePropertyChange(PROP_CHILD_ADDED, null, child);
	}
	
	public void removeChild(TimelineObject child) {
		children.remove(child);
		propertyChangeSupport.firePropertyChange(PROP_CHILD_REMOVED, child, null);
	}
	
	public void fireChildChanged(TimelineObject child) {
		propertyChangeSupport.firePropertyChange(PROP_CHILD_MODIFIED, null, child);
	}

	public TimelineObject getParent() {
		return parent;
	}

	public void setParent(TimelineObject parent) {
		TimelineObject oldParent = this.parent;
		this.parent = parent;
		propertyChangeSupport.firePropertyChange(PROP_PARENT_CHANGED, oldParent, parent);
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Get the duration in msec
	 *
	 * @return the value of duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Set the duration in msec
	 *
	 * @param newDuration new value of duration
	 */
	public void setDuration(final int newDuration) {
		final int oldDuration = this.duration;
		this.duration = newDuration;
		propertyChangeSupport.firePropertyChange(ResourceTimelineObject.PROP_DURATION, oldDuration, newDuration);
		if (!Objects.equals(oldDuration, newDuration) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {
				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					duration = oldDuration;
					propertyChangeSupport.firePropertyChange(ResourceTimelineObject.PROP_DURATION, newDuration, oldDuration);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					duration = newDuration;
					propertyChangeSupport.firePropertyChange(ResourceTimelineObject.PROP_DURATION, oldDuration, newDuration);
				}
			});
		}
	}

	/**
	 * Get the start time in msec
	 *
	 * @return the value of start
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Converts the specified global time to a local time from the beginning
	 * of this object.
	 * @param globalTime the global time in msec
	 * @return the local time in msec
	 */
	public int getLocalTime(int globalTime) {
		int localTime;
		if (parent != null) {
			localTime = parent.getLocalTime(globalTime);
		} else {
			localTime = globalTime;
		}
		return localTime - getStart();
	}
	
	/**
	 * Set the start time in msec
	 *
	 * @param newStart new value of start
	 */
	public void setStart(final int newStart) {
		final int oldStart = this.start;
		this.start = newStart;
		propertyChangeSupport.firePropertyChange(ResourceTimelineObject.PROP_START, oldStart, newStart);
		if (!Objects.equals(oldStart, newStart) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {
				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					start = oldStart;
					propertyChangeSupport.firePropertyChange(ResourceTimelineObject.PROP_START, newStart, oldStart);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					start = newStart;
					propertyChangeSupport.firePropertyChange(ResourceTimelineObject.PROP_START, oldStart, newStart);
				}
			});
		}
	}

	public int getGlobalStart() {
		return start + ((parent==null)?0:parent.getGlobalStart());
	}
	
	public int getGlobalDuration() {
		if (parent != null) {
			return Math.min(duration, parent.getGlobalDuration());
		} else {
			return duration;
		}
	}
}
