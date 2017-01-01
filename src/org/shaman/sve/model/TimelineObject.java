/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.logging.Logger;
import org.shaman.sve.player.Player;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;

/**
 * This class represents an object in the timeline.
 * It is a compination of a {@link Resource}, settings and filters.
 * The {@link Player} is responsible for interpreting the object.
 * @author Sebastian Weiss
 */
public class TimelineObject {
	private static final Logger LOG = Logger.getLogger(TimelineObject.class.getName());
	
	private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	@Element
	private Resource resource;
	public static final String PROP_RESOURCE = "resource";
	
	@Element
	private String name;
	public static final String PROP_NAME = "name";
	
	@ElementMap
	private HashMap<String, Object> properties = new HashMap<>();
	/**
	 * Starting time in milliseconds, integer.
	 * Valid for all resources that have no parent.
	 */
	public static final String PROP_START = "start";
	/**
	 * Duration in milliseconds, integer.
	 * Valid for images.
	 */
	public static final String PROP_DURATION = "duration";
	
	/**
	 * Storage for the player
	 */
	public final HashMap<String, Object> playerProperties = new HashMap<>();

	/**
	 * Get the value of resource
	 *
	 * @return the value of resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Set the value of resource
	 *
	 * @param resource new value of resource
	 */
	public void setResource(Resource resource) {
		Resource oldResource = this.resource;
		this.resource = resource;
		propertyChangeSupport.firePropertyChange(PROP_RESOURCE, oldResource, resource);
		setName(resource.getName());
	}

	public HashMap<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(HashMap<String, Object> properties) {
		if (properties == null) {
			properties = new HashMap<>();
		}
		this.properties = properties;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProperty(String key) {
		return (T) properties.get(key);
	}
	
	public boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	public <T> void setProperty(String key, T value) {
		Object old = properties.put(key, value);
		propertyChangeSupport.firePropertyChange(key, old, value);
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
	 * @param name new value of name
	 */
	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, name);
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
