/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import com.sun.org.apache.xalan.internal.utils.Objects;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.swing.undo.*;
import org.shaman.sve.player.Player;
import org.simpleframework.xml.Element;

/**
 * This class represents an object in the timeline.
 * It is a compination of a {@link Resource}, settings and filters.
 * The {@link Player} is responsible for interpreting the object.
 * @author Sebastian Weiss
 */
public class TimelineObject {
	private static final Logger LOG = Logger.getLogger(TimelineObject.class.getName());
	
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

	@Override
	public String toString() {
		return getName();
	}

}
