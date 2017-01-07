/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.shaman.sve.model.Resource;
import org.shaman.sve.model.TimelineObject;

/**
 * Helper class for storing all types of selections for communication
 * @author Sebastian
 */
public class Selections {
	
	private Resource selectedResource;

	public static final String PROP_SELECTED_RESOURCE = "selectedResource";

	/**
	 * Get the value of selectedResource
	 *
	 * @return the value of selectedResource
	 */
	public Resource getSelectedResource() {
		return selectedResource;
	}

	/**
	 * Set the value of selectedResource
	 *
	 * @param selectedResource new value of selectedResource
	 */
	public void setSelectedResource(Resource selectedResource) {
		Resource oldSelectedResource = this.selectedResource;
		this.selectedResource = selectedResource;
		propertyChangeSupport.firePropertyChange(PROP_SELECTED_RESOURCE, oldSelectedResource, selectedResource);
	}

		private TimelineObject selectedTimelineObject;

	public static final String PROP_SELECTED_TIMELINE_OBJECT = "selectedTimelineObject";

	/**
	 * Get the value of selectedTimelineObject
	 *
	 * @return the value of selectedTimelineObject
	 */
	public TimelineObject getSelectedTimelineObject() {
		return selectedTimelineObject;
	}

	/**
	 * Set the value of selectedTimelineObject
	 *
	 * @param selectedTimelineObject new value of selectedTimelineObject
	 */
	public void setSelectedTimelineObject(TimelineObject selectedTimelineObject) {
		TimelineObject oldSelectedTimelineObject = this.selectedTimelineObject;
		this.selectedTimelineObject = selectedTimelineObject;
		propertyChangeSupport.firePropertyChange(PROP_SELECTED_TIMELINE_OBJECT, oldSelectedTimelineObject, selectedTimelineObject);
	}

	
	private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

}
