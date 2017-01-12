/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.util.Objects;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 * @param <R> the type of resource
 */
public abstract class ResourceTimelineObject<R extends Resource> extends TimelineObject {
	
	@Element
	protected R resource;
	public static final String PROP_RESOURCE = "resource";
	
	
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
		if (newResource == resource) return;
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


}
