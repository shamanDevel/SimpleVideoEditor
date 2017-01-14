/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import com.jhlabs.image.PointFilter;
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
public class TransparencyImageFilter extends AbstractImageFilter implements CloneableFilter {
	private static final Logger LOG = Logger.getLogger(TransparencyImageFilter.class.getName());

	@Element
	private float startTransparency;
	public static final String PROP_START_TRANSPARENCY = "startTransparency";
	
	@Element
	private float endTransparency;
	public static final String PROP_END_TRANSPARENCY = "endTransparency";
	
	@Element
	private boolean applyOverBorders;
	public static final String PROP_APPLYOVERBORDERS = "applyOverBorders";
	
	private static class SetAlphaFilter extends PointFilter {
		private float alpha;
		
		@Override
		public int filterRGB(int x, int y, int rgba) {
			int a = rgba >>> 24;
			int rgb = rgba & 0x00ffffff;
			a = (int)Math.min(255, (256 * (a * alpha / 256.0)));
			return (a << 24) | rgb;
		}
		
	}
	private final SetAlphaFilter filter = new SetAlphaFilter();
	
	public TransparencyImageFilter() {
	}

	public TransparencyImageFilter(TimelineObject parent) {
		setParent(parent);
	}

	public TransparencyImageFilter(TimelineObject parent, float startTransparency, float endTransparency, boolean applyOverBorders) {
		this(parent);
		this.startTransparency = startTransparency;
		this.endTransparency = endTransparency;
		this.applyOverBorders = applyOverBorders;
	}

	@Override
	public BufferedImage process(BufferedImage image, FrameTime frameTime, boolean thumbnail, float thumbnailScale) {
		int localTime = parent.getLocalTime(frameTime.toMillis());
		float alpha;
		if (localTime < start) {
			if (applyOverBorders) {
				alpha = startTransparency;
			} else {
				return image;
			}
		} else if (localTime > start + duration) {
			if (applyOverBorders) {
				alpha = endTransparency;
			} else {
				return image;
			}
		} else {
			alpha = startTransparency + ((localTime - start) / (float)duration) * (endTransparency - startTransparency);
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
	public float getStartTransparency() {
		return startTransparency;
	}

	/**
	 * Set the value of startTransparency
	 *
	 * @param newStartTransparency new value of startTransparency
	 */
	public void setStartTransparency(final float newStartTransparency) {
		final float oldStartTransparency = this.startTransparency;
		this.startTransparency = newStartTransparency;
		propertyChangeSupport.firePropertyChange(PROP_START_TRANSPARENCY, oldStartTransparency, newStartTransparency);
		if (!Objects.equals(oldStartTransparency, newStartTransparency) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					startTransparency = oldStartTransparency;
					propertyChangeSupport.firePropertyChange(PROP_START_TRANSPARENCY, newStartTransparency, oldStartTransparency);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					startTransparency = newStartTransparency;
					propertyChangeSupport.firePropertyChange(PROP_START_TRANSPARENCY, oldStartTransparency, newStartTransparency);
				}
			
			});
		}
	}

	/**
	 * Get the value of endTransparency
	 *
	 * @return the value of endTransparency
	 */
	public float getEndTransparency() {
		return endTransparency;
	}

	/**
	 * Set the value of endTransparency
	 *
	 * @param newEndTransparency new value of endTransparency
	 */
	public void setEndTransparency(final float newEndTransparency) {
		final float oldEndTransparency = this.endTransparency;
		this.endTransparency = newEndTransparency;
		propertyChangeSupport.firePropertyChange(PROP_END_TRANSPARENCY, oldEndTransparency, newEndTransparency);
		if (!Objects.equals(oldEndTransparency, newEndTransparency) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					endTransparency = oldEndTransparency;
					propertyChangeSupport.firePropertyChange(PROP_END_TRANSPARENCY, newEndTransparency, oldEndTransparency);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					endTransparency = newEndTransparency;
					propertyChangeSupport.firePropertyChange(PROP_END_TRANSPARENCY, oldEndTransparency, newEndTransparency);
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
		TransparencyImageFilter f = new TransparencyImageFilter(parent, startTransparency, endTransparency, applyOverBorders);
		f.setStart(start);
		f.setDuration(duration);
		f.setName(name);
		return f;
	}

	@ServiceProvider(service = FilterFactory.class)
	public static class FadeInFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Image/Fade In";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return (obj instanceof ImageTimelineObject);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			int duration = Math.min(1000, obj.getDuration());
			TransparencyImageFilter filter = new TransparencyImageFilter(obj, 0, 1, false);
			filter.setDuration(duration);
			filter.setStart(0);
			filter.setName("Fade In");
			return filter;
		}
		
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class FadeOutFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Image/Fade Out";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return (obj instanceof ImageTimelineObject);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			int duration = Math.min(1000, obj.getDuration());
			TransparencyImageFilter filter = new TransparencyImageFilter(obj, 1, 0, false);
			filter.setDuration(duration);
			filter.setStart(obj.getDuration() - duration);
			filter.setName("Fade Out");
			return filter;
		}
		
	}
}
