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

import static org.shaman.sve.model.ResourceTimelineObject.PROP_RESOURCE;

/**
 * Timeline object for both images and videos
 * @author Sebastian Weiss
 * @param <T> 
 */
public class ImageTimelineObject<T extends Resource & Resource.ImageProvider> extends ResourceTimelineObject<T> {

	@Element
	private int x;
	public static final String PROP_X = "x";
	
	@Element
	private int y;
	public static final String PROP_Y = "y";
	
	@Element
	private int width;
	public static final String PROP_WIDTH = "width";
	
	@Element
	private int height;
	public static final String PROP_HEIGHT = "height";
	
	public ImageTimelineObject() {
	}

	public ImageTimelineObject(T resource) {
		super(resource);
	}

	/**
	 * Returns the x position
	 * @return 
	 */
	public int getX() {
		return x;
	}

	/**
	 * Sets the x position
	 * @param newX 
	 */
	public void setX(final int newX) {
		final int oldX = this.x;
		this.x = newX;
		propertyChangeSupport.firePropertyChange(PROP_X, oldX, newX);
		if (!Objects.equals(oldX, newX) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					x = oldX;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_X, newX, oldX);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					x = newX;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_Y, oldX, newX);
				}
			
			});
		}
	}
	
	/**
	 * Returns the y position
	 * @return 
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the y position
	 * @param y
	 */
	public void setY(final int newY) {
		final int oldY = this.y;
		this.y = newY;
		propertyChangeSupport.firePropertyChange(PROP_Y, oldY, newY);
		if (!Objects.equals(oldY, newY) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					y = oldY;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_Y, newY, oldY);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					y = newY;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_Y, oldY, newY);
				}
			
			});
		}
	}
	
	/**
	 * Returns the width
	 * @return 
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width
	 * @param newWidth 
	 */
	public void setWidth(final int newWidth) {
		final int oldWidth = this.width;
		this.width = newWidth;
		propertyChangeSupport.firePropertyChange(PROP_WIDTH, oldWidth, newWidth);
		if (!Objects.equals(oldWidth, newWidth) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					width = oldWidth;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_WIDTH, newWidth, oldWidth);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					height = newWidth;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_WIDTH, oldWidth, newWidth);
				}
			
			});
		}
	}
	
	/**
	 * Returns the height
	 * @return 
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height
	 * @param newHeight 
	 */
	public void setHeight(final int newHeight) {
		final int oldHeight = this.height;
		this.height = newHeight;
		propertyChangeSupport.firePropertyChange(PROP_HEIGHT, oldHeight, newHeight);
		if (!Objects.equals(oldHeight, newHeight) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					height = oldHeight;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_HEIGHT, newHeight, oldHeight);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					height = newHeight;
					name = resource.getName();
					propertyChangeSupport.firePropertyChange(PROP_HEIGHT, oldHeight, newHeight);
				}
			
			});
		}
	}
}
