/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import com.jhlabs.image.PointFilter;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.util.lookup.ServiceProvider;
import org.shaman.sve.FrameTime;
import org.shaman.sve.model.ImageTimelineObject;
import org.shaman.sve.model.Resource;
import org.shaman.sve.model.ResourceTimelineObject;
import org.shaman.sve.model.TimelineObject;
import org.simpleframework.xml.Element;


public class CropImageFilter extends AbstractImageFilter implements CloneableFilter {

	@Element
	private int cropLeft;
	public static final String PROP_CROPLEFT = "cropLeft";
	
	@Element
	private int cropRight;
	public static final String PROP_CROPRIGHT = "cropRight";
	
	@Element
	private int cropTop;
	public static final String PROP_CROPTOP = "cropTop";
	
	@Element
	private int cropBottom;
	public static final String PROP_CROPBOTTOM = "cropBottom";

	public CropImageFilter() {
	}

	public CropImageFilter(TimelineObject parent) {
		setParent(parent);
	}
	
	public CropImageFilter(TimelineObject parent, int cropLeft, int cropRight, int cropTop, int cropBottom) {
		setParent(parent);
		this.cropLeft = cropLeft;
		this.cropRight = cropRight;
		this.cropTop = cropTop;
		this.cropBottom = cropBottom;
	}	
	

	/**
	 * Get the value of cropLeft
	 *
	 * @return the value of cropLeft
	 */
	public int getCropLeft() {
		return cropLeft;
	}

	/**
	 * Set the value of cropLeft
	 *
	 * @param newCropLeft new value of cropLeft
	 */
	public void setCropLeft(final int newCropLeft) {
		final int oldCropLeft = this.cropLeft;
		this.cropLeft = newCropLeft;
		propertyChangeSupport.firePropertyChange(PROP_CROPLEFT, oldCropLeft, newCropLeft);
		if (!Objects.equals(oldCropLeft, newCropLeft) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					cropLeft = oldCropLeft;
					propertyChangeSupport.firePropertyChange(PROP_CROPLEFT, newCropLeft, oldCropLeft);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					cropLeft = newCropLeft;
					propertyChangeSupport.firePropertyChange(PROP_CROPLEFT, oldCropLeft, newCropLeft);
				}
			
			});
		}
	}

	/**
	 * Get the value of cropRight
	 *
	 * @return the value of cropRight
	 */
	public int getCropRight() {
		return cropRight;
	}

	/**
	 * Set the value of cropRight
	 *
	 * @param newCropRight new value of cropRight
	 */
	public void setCropRight(final int newCropRight) {
		final int oldCropRight = this.cropRight;
		this.cropRight = newCropRight;
		propertyChangeSupport.firePropertyChange(PROP_CROPRIGHT, oldCropRight, newCropRight);
		if (!Objects.equals(oldCropRight, newCropRight) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					cropRight = oldCropRight;
					propertyChangeSupport.firePropertyChange(PROP_CROPRIGHT, newCropRight, oldCropRight);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					cropRight = newCropRight;
					propertyChangeSupport.firePropertyChange(PROP_CROPRIGHT, oldCropRight, newCropRight);
				}
			
			});
		}
	}

	/**
	 * Get the value of cropTop
	 *
	 * @return the value of cropTop
	 */
	public int getCropTop() {
		return cropTop;
	}

	/**
	 * Set the value of cropTop
	 *
	 * @param newCropTop new value of cropTop
	 */
	public void setCropTop(final int newCropTop) {
		final int oldCropTop = this.cropTop;
		this.cropTop = newCropTop;
		propertyChangeSupport.firePropertyChange(PROP_CROPTOP, oldCropTop, newCropTop);
		if (!Objects.equals(oldCropTop, newCropTop) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					cropTop = oldCropTop;
					propertyChangeSupport.firePropertyChange(PROP_CROPTOP, newCropTop, oldCropTop);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					cropTop = newCropTop;
					propertyChangeSupport.firePropertyChange(PROP_CROPTOP, oldCropTop, newCropTop);
				}
			
			});
		}
	}

	/**
	 * Get the value of cropBottom
	 *
	 * @return the value of cropBottom
	 */
	public int getCropBottom() {
		return cropBottom;
	}

	/**
	 * Set the value of cropBottom
	 *
	 * @param newCropBottom new value of cropBottom
	 */
	public void setCropBottom(final int newCropBottom) {
		final int oldCropBottom = this.cropBottom;
		this.cropBottom = newCropBottom;
		propertyChangeSupport.firePropertyChange(PROP_CROPBOTTOM, oldCropBottom, newCropBottom);
		if (!Objects.equals(oldCropBottom, newCropBottom) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					cropBottom = oldCropBottom;
					propertyChangeSupport.firePropertyChange(PROP_CROPBOTTOM, newCropBottom, oldCropBottom);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					cropBottom = newCropBottom;
					propertyChangeSupport.firePropertyChange(PROP_CROPBOTTOM, oldCropBottom, newCropBottom);
				}
			
			});
		}
	}

	@Override
	public TimelineObject cloneForParent(TimelineObject parent) {
		CropImageFilter f = new CropImageFilter(parent, cropLeft, cropRight, cropTop, cropBottom);
		f.setName(name);
		f.setDuration(parent.getDuration());
		return f;
	}

	private static class CropFilter extends PointFilter {
		private int l,r,u,d;
		
		@Override
		public int filterRGB(int x, int y, int rgba) {
			if (x<l || x>=r || y<u || y>=d) {
				return 0;
			} else {
				return rgba;
			}
		}
		
	}
	private final CropFilter filter = new CropFilter();
	@Override
	public BufferedImage process(BufferedImage image, FrameTime frameTime, boolean thumbnail, float thumbnailScale) {
		int localTime = parent.getLocalTime(frameTime.toMillis());
		if (localTime < start || localTime > start+duration) {
			return image;
		}
		filter.l = (int) Math.floor(cropLeft * thumbnailScale);
		filter.u = (int) Math.floor(cropTop * thumbnailScale);
		filter.r = (int) Math.ceil(image.getWidth() - cropRight*thumbnailScale);
		filter.d = (int) Math.ceil(image.getHeight() - cropBottom*thumbnailScale);
		return filter.filter(image, null);
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class CropFilterFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Image/Crop";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return (obj instanceof ImageTimelineObject);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			CropImageFilter f = new CropImageFilter(obj, 50, 50, 50, 50);
			f.setDuration(obj.getDuration());
			f.setName("crop");
			return f;
		}
		
	}
}
