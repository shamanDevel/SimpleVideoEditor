/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import com.jhlabs.image.PointFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.logging.Logger;
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

/**
 *
 * @author Sebastian Weiss
 */
public class ColorBlendImageFilter extends AbstractImageFilter implements CloneableFilter {
	private static final Logger LOG = Logger.getLogger(ColorBlendImageFilter.class.getName());

	@Element
	private float startBlending;
	public static final String PROP_START_BLENDING = "startTransparency";
	
	@Element
	private float endBlending;
	public static final String PROP_END_BLENDING = "endTransparency";
	
	@Element
	private Color color;
	public static final String PROP_COLOR = "color";
	
	@Element
	private boolean applyOverBorders;
	public static final String PROP_APPLYOVERBORDERS = "applyOverBorders";
	
	private class BlendFilter extends PointFilter {
		private float alpha;
		
		@Override
		public int filterRGB(int x, int y, int rgba) {
			int a = (rgba >>> 24) & 0xff;
			int r = (rgba >> 16) & 0xff;
			int g = (rgba >> 8) & 0xff;
			int b = rgba & 0xff;
			
			float nalpha = 1-alpha;
			a = (int)(alpha * a + nalpha * color.getAlpha());
			r = (int)(alpha * r + nalpha * color.getRed());
			g = (int)(alpha * g + nalpha * color.getGreen());
			b = (int)(alpha * b + nalpha * color.getBlue());
			
			int nrgba = (a<<24) | (r<<16) | (g<<8) | b;
			return nrgba;
		}
		
	}
	private final BlendFilter filter = new BlendFilter();
	
	public ColorBlendImageFilter() {
	}

	public ColorBlendImageFilter(TimelineObject parent) {
		setParent(parent);
	}

	public ColorBlendImageFilter(TimelineObject parent, float startBlending, float endBlending, Color color, boolean applyOverBorders) {
		this(parent);
		this.startBlending = startBlending;
		this.endBlending = endBlending;
		this.color = color;
		this.applyOverBorders = applyOverBorders;
	}

	@Override
	public BufferedImage process(BufferedImage image, FrameTime frameTime, boolean thumbnail, float thumbnailScale) {
		int localTime = parent.getLocalTime(frameTime.toMillis());
		localTime = Math.max(0, localTime);
		float alpha;
		if (localTime < start) {
			if (applyOverBorders) {
				alpha = startBlending;
			} else {
				return image;
			}
		} else if (localTime > start + duration) {
			if (applyOverBorders) {
				alpha = endBlending;
			} else {
				return image;
			}
		} else {
			alpha = startBlending + ((localTime - start) / (float)duration) * (endBlending - startBlending);
		}
		filter.alpha = alpha;
//		LOG.info("apply transparency filter with alpha="+alpha);
		return filter.filter(image, null);
	}

	/**
	 * Get the value of startTransparency
	 *
	 * @return the value of startTransparency
	 */
	public float getStartBlending() {
		return startBlending;
	}

	/**
	 * Set the value of startTransparency
	 *
	 * @param newStartTransparency new value of startTransparency
	 */
	public void setStartBlending(final float newStartTransparency) {
		final float oldStartTransparency = this.startBlending;
		this.startBlending = newStartTransparency;
		propertyChangeSupport.firePropertyChange(PROP_START_BLENDING, oldStartTransparency, newStartTransparency);
		if (!Objects.equals(oldStartTransparency, newStartTransparency) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					startBlending = oldStartTransparency;
					propertyChangeSupport.firePropertyChange(PROP_START_BLENDING, newStartTransparency, oldStartTransparency);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					startBlending = newStartTransparency;
					propertyChangeSupport.firePropertyChange(PROP_START_BLENDING, oldStartTransparency, newStartTransparency);
				}
			
			});
		}
	}

	/**
	 * Get the value of endTransparency
	 *
	 * @return the value of endTransparency
	 */
	public float getEndBlending() {
		return endBlending;
	}

	/**
	 * Set the value of endTransparency
	 *
	 * @param newEndTransparency new value of endTransparency
	 */
	public void setEndBlending(final float newEndTransparency) {
		final float oldEndTransparency = this.endBlending;
		this.endBlending = newEndTransparency;
		propertyChangeSupport.firePropertyChange(PROP_END_BLENDING, oldEndTransparency, newEndTransparency);
		if (!Objects.equals(oldEndTransparency, newEndTransparency) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					endBlending = oldEndTransparency;
					propertyChangeSupport.firePropertyChange(PROP_END_BLENDING, newEndTransparency, oldEndTransparency);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					endBlending = newEndTransparency;
					propertyChangeSupport.firePropertyChange(PROP_END_BLENDING, oldEndTransparency, newEndTransparency);
				}
			
			});
		}
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(final Color newColor) {
		final Color oldColor = this.color;
		this.color = newColor;
		propertyChangeSupport.firePropertyChange(PROP_COLOR, oldColor, newColor);
		if (!Objects.equals(oldColor, newColor) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					color = oldColor;
					propertyChangeSupport.firePropertyChange(PROP_COLOR, newColor, oldColor);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					color = newColor;
					propertyChangeSupport.firePropertyChange(PROP_COLOR, oldColor, newColor);
				}
			
			});
		}
	}

	/**
	 * @return {@code true} if the filter is also applied after the bounds.
	 */
	public boolean isApplyOverBorders() {
		return applyOverBorders;
	}

	/**
	 * Sets if the filter should also be applied beyond the bounds.
	 * If the current time is before the beginning of the filter and after the
	 * end of the filter (start+duration) and this flag is true, then
	 * the filter is also applied beyond these bounds.
	 * This works as if the current time is clipped to the bounds of the filter.
	 * @param newApplyOverBorders 
	 */
	public void setApplyOverBorders(final boolean newApplyOverBorders) {
		final boolean oldApplyOverBorders = this.applyOverBorders;
		this.applyOverBorders = newApplyOverBorders;
		propertyChangeSupport.firePropertyChange(PROP_APPLYOVERBORDERS, oldApplyOverBorders, newApplyOverBorders);
		if (!Objects.equals(oldApplyOverBorders, newApplyOverBorders) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					applyOverBorders = oldApplyOverBorders;
					propertyChangeSupport.firePropertyChange(PROP_APPLYOVERBORDERS, newApplyOverBorders, oldApplyOverBorders);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					applyOverBorders = newApplyOverBorders;
					propertyChangeSupport.firePropertyChange(PROP_APPLYOVERBORDERS, oldApplyOverBorders, newApplyOverBorders);
				}
			
			});
		}
	}

	@Override
	public TimelineObject cloneForParent(TimelineObject parent) {
		ColorBlendImageFilter f = new ColorBlendImageFilter(parent, startBlending, endBlending, color, applyOverBorders);
		f.setStart(start);
		f.setDuration(duration);
		f.setName(name);
		return f;
	}

	@ServiceProvider(service = FilterFactory.class)
	public static class FadeFromBlackFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Image/Fade From Black";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return (obj instanceof ImageTimelineObject);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			int duration = Math.min(1000, obj.getDuration());
			ColorBlendImageFilter filter = new ColorBlendImageFilter(obj, 0, 1, Color.BLACK, false);
			filter.setDuration(duration);
			filter.setStart(0);
			filter.setName("Fade from black");
			return filter;
		}
		
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class FadeToBlackFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Image/Fade To Black";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return (obj instanceof ImageTimelineObject);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			int duration = Math.min(1000, obj.getDuration());
			ColorBlendImageFilter filter = new ColorBlendImageFilter(obj, 1, 0, Color.BLACK, false);
			filter.setDuration(duration);
			filter.setStart(obj.getDuration() - duration);
			filter.setName("Fade to black");
			return filter;
		}
		
	}
}
