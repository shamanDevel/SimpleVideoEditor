/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import org.shaman.sve.FrameTime;
import org.simpleframework.xml.*;

/**
 *
 * @author Sebastian Weiss
 */
@Root
public class Project {
	private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	@Attribute
	private int version = 1;
	
	@Element
	private String name;
	@Element
	private File folder;
	@Element
	private int framerate;
	@Element
	private int width;
	@Element
	private int height;
	
	@ElementList
	private ArrayList<Resource> resources = new ArrayList<>();
	
	@ElementList
	private ArrayList<TimelineObject> timelineObjects = new ArrayList<>();
	
	@Element
	private Color backgroundColor = new Color(0, 0, 0);
	public static final String PROP_BACKGROUNDCOLOR = "backgroundColor";
	
	//not persistent
	private FrameTime time;
	public static final String PROP_TIME = "time";
	private FrameTime length;
	public static final String PROP_LENGTH = "length";
	public static final String PROP_TIMELINE_OBJECT_CHANGED = "tloChanged";
	public static final String PROP_TIMELINE_OBJECTS_CHANGED = "tloxChanged";
	
	public Project() {
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
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public int getFramerate() {
		return framerate;
	}

	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public ArrayList<Resource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<Resource> resources) {
		if (resources == null) {
			resources = new ArrayList<>();
		}
		this.resources = resources;
	}

	public ArrayList<TimelineObject> getTimelineObjects() {
		return timelineObjects;
	}

	public void setTimelineObjects(ArrayList<TimelineObject> timelineObjects) {
		if (timelineObjects == null) {
			timelineObjects = new ArrayList<>();
		}
		this.timelineObjects = timelineObjects;
	}
	
	/**
	 * Fires a property changed event that the specified obj is changed
	 * @param obj 
	 */
	public void fireTimelineObjectChanged(TimelineObject obj) {
		propertyChangeSupport.firePropertyChange(PROP_TIMELINE_OBJECT_CHANGED, null, obj);
	}
	
	/**
	 * Fires a property changed event that all timeline objects are changed.
	 * This includes adding + removing objects
	 */
	public void fireTimelineObjectsChanged() {
		propertyChangeSupport.firePropertyChange(PROP_TIMELINE_OBJECTS_CHANGED, null, null);
	}
	
	/**
	 * Get the value of backgroundColor
	 *
	 * @return the value of backgroundColor
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Set the value of backgroundColor
	 *
	 * @param backgroundColor new value of backgroundColor
	 */
	public void setBackgroundColor(Color backgroundColor) {
		Color oldBackgroundColor = this.backgroundColor;
		this.backgroundColor = backgroundColor;
		propertyChangeSupport.firePropertyChange(PROP_BACKGROUNDCOLOR, oldBackgroundColor, backgroundColor);
	}

	/**
	 * Get the current playback time
	 *
	 * @return the value of time
	 */
	public FrameTime getTime() {
		return time;
	}

	/**
	 * Set the current playback time
	 *
	 * @param time new value of time
	 */
	public void setTime(FrameTime time) {
		FrameTime oldTime = this.time;
		this.time = time;
		propertyChangeSupport.firePropertyChange(PROP_TIME, oldTime, time);
	}

	/**
	 * @return the total length of the project in msec.
	 */
	public FrameTime getLength() {
		return length;
	}

	/**
	 * Sets the total length of the project in msec.
	 * @param length new value of length
	 */
	public void setLength(FrameTime length) {
		FrameTime oldLength = this.length;
		this.length = length;
		propertyChangeSupport.firePropertyChange(PROP_LENGTH, oldLength, length);
	}

	@Override
	public String toString() {
		return "Project{" + "version=" + version + ", name=" + name + ", folder=" + folder + ", framerate=" + framerate + ", width=" + width + ", height=" + height + '}';
	}
	
	
}
