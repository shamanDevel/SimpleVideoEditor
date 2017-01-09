/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import com.sun.org.apache.xalan.internal.utils.Objects;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.simpleframework.xml.Element;

import static org.shaman.sve.model.TimelineObject.PROP_NAME;

/**
 *
 * @author Sebastian Weiss
 * @param <R> the type of resource
 */
public abstract class ResourceTimelineObject<R extends Resource> extends TimelineObject {
	
	@Element
	protected R resource;
	public static final String PROP_RESOURCE = "resource";
	
	@Element
	protected int start;
	public static final String PROP_START = "start";
	
	@Element
	protected int duration;
	public static final String PROP_DURATION = "duration";
	
	public ResourceTimelineObject() {
	}

	public ResourceTimelineObject(R resource) {
		setResource(resource);
	}
	
	/**
	 * Get the value of resource
	 *
	 * @return the value of resource
	 */
	public R getResource() {
		return resource;
	}

	/**
	 * Set the value of resource
	 *
	 * @param newResource new value of resource
	 */
	public void setResource(final R newResource) {
		final R oldResource = this.resource;
		this.resource = newResource;
		propertyChangeSupport.firePropertyChange(PROP_RESOURCE, oldResource, newResource);
		setName(newResource.getName());
		if (!Objects.equals(oldResource, newResource) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					resource = oldResource;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_RESOURCE, newResource, oldResource);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					resource = newResource;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_RESOURCE, oldResource, newResource);
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
	 * Set the start time in msec
	 *
	 * @param newStart new value of start
	 */
	public void setStart(final int newStart) {
		final int oldStart = this.start;
		this.start = newStart;
		propertyChangeSupport.firePropertyChange(PROP_START, oldStart, newStart);
		
		if (!Objects.equals(oldStart, newStart) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					start = oldStart;
					propertyChangeSupport.firePropertyChange(PROP_START, newStart, oldStart);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					start = newStart;
					propertyChangeSupport.firePropertyChange(PROP_START, oldStart, newStart);
				}
			
			});
		}
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
		propertyChangeSupport.firePropertyChange(PROP_DURATION, oldDuration, newDuration);
		
		if (!Objects.equals(oldDuration, newDuration) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					duration = oldDuration;
					propertyChangeSupport.firePropertyChange(PROP_DURATION, newDuration, oldDuration);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					duration = newDuration;
					propertyChangeSupport.firePropertyChange(PROP_DURATION, oldDuration, newDuration);
				}
			
			});
		}
	}

}
