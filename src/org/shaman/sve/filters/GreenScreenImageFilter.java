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
public class GreenScreenImageFilter extends AbstractImageFilter implements CloneableFilter {
	private static final Logger LOG = Logger.getLogger(GreenScreenImageFilter.class.getName());

	/**
	 * a utility function to convert colors in RGB into YCbCr
	 */
	private static int rgb2y(int r, int g, int b) {
		int y;
		y = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
		return (y);
	}

	/**
	 * a utility function to convert colors in RGB into YCbCr
	 */
	private static int rgb2cb(int r, int g, int b) {
		int cb;
		cb = (int) Math.round(128 + -0.168736 * r - 0.331264 * g + 0.5 * b);
		return (cb);
	}

	/**
	 * a utility function to convert colors in RGB into YCbCr
	 */
	private static int rgb2cr(int r, int g, int b) {
		int cr;
		cr = (int) Math.round(128 + 0.5 * r - 0.418688 * g - 0.081312 * b);
		return (cr);
	}
	
	@Element
	private Color keyColor;
	public static final String PROP_KEYCOLOR = "keyColor";
	
	@Element
	private int keyCb;
	@Element
	private int keyCr;
	
	@Element
	private int tolA;
	public static final String PROP_TOL_A = "tolA";
	
	@Element
	private int tolB;
	public static final String PROP_TOL_B = "tolB";

	private class GreenFilter extends PointFilter {		
		@Override
		public int filterRGB(int x, int y, int rgba) {
			int a = (rgba >>> 24) & 0xff;
			int r = (rgba >> 16) & 0xff;
			int g = (rgba >> 8) & 0xff;
			int b = rgba & 0xff;
			int cb = rgb2cb(r, g, b);
			int cr = rgb2cr(r, g, b);
			
			double factor;
			double temp = Math.sqrt((keyCb - cb) * (keyCb - cb) + (keyCr - cr) * (keyCr - cr));
			if (temp < tolA) {
				factor = 0;
			} else if (temp < tolB) {
				factor = ((temp - tolA) / (tolB - tolA));
			} else {
				factor = 1;
			}
			a = (int)Math.min(255, (256 * (a * factor / 256.0)));
			return (a << 24) | (rgba & 0x00ffffff);
		}
		
	}
	private final GreenFilter filter = new GreenFilter();
	
	public GreenScreenImageFilter() {
	}
	
	public GreenScreenImageFilter(TimelineObject parent) {
		setParent(parent);
	}
	
	public GreenScreenImageFilter(TimelineObject parent, Color keyColor, int tolA, int tolB) {
		setParent(parent);
		setKeyColor(keyColor);
		setTolA(tolA);
		setTolB(tolB);
	}

	/**
	 * Get the value of keyColor
	 *
	 * @return the value of keyColor
	 */
	public Color getKeyColor() {
		return keyColor;
	}

	private void updateCbCr() {
		keyCb = rgb2cb(keyColor.getRed(), keyColor.getGreen(), keyColor.getBlue());
		keyCr = rgb2cr(keyColor.getRed(), keyColor.getGreen(), keyColor.getBlue());
	}
	
	/**
	 * Set the value of keyColor
	 *
	 * @param newKeyColor new value of keyColor
	 */
	public void setKeyColor(final Color newKeyColor) {
		final Color oldKeyColor = this.keyColor;
		this.keyColor = newKeyColor;
		propertyChangeSupport.firePropertyChange(PROP_KEYCOLOR, oldKeyColor, newKeyColor);
		updateCbCr();
		if (!Objects.equals(oldKeyColor, newKeyColor) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					keyColor = oldKeyColor;
					updateCbCr();
					propertyChangeSupport.firePropertyChange(PROP_KEYCOLOR, newKeyColor, oldKeyColor);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					keyColor = newKeyColor;
					updateCbCr();
					propertyChangeSupport.firePropertyChange(PROP_KEYCOLOR, oldKeyColor, newKeyColor);
				}
			
			});
		}
	}

	/**
	 * Get the value of tolA
	 *
	 * @return the value of tolA
	 */
	public int getTolA() {
		return tolA;
	}

	/**
	 * Set the value of tolA
	 *
	 * @param newTolA new value of tolA
	 */
	public void setTolA(final int newTolA) {
		final int oldTolA = this.tolA;
		this.tolA = newTolA;
		propertyChangeSupport.firePropertyChange(PROP_TOL_A, oldTolA, newTolA);
		if (!Objects.equals(oldTolA, newTolA) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					tolA = oldTolA;
					propertyChangeSupport.firePropertyChange(PROP_TOL_A, newTolA, oldTolA);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					tolA = newTolA;
					propertyChangeSupport.firePropertyChange(PROP_TOL_A, oldTolA, newTolA);
				}
			
			});
		}
	}

	/**
	 * Get the value of tolB
	 *
	 * @return the value of tolB
	 */
	public int getTolB() {
		return tolB;
	}

	/**
	 * Set the value of tolB
	 *
	 * @param newTolB new value of tolB
	 */
	public void setTolB(final int newTolB) {
		final int oldTolB = this.tolB;
		this.tolB = newTolB;
		propertyChangeSupport.firePropertyChange(PROP_TOL_B, oldTolB, newTolB);
		if (!Objects.equals(oldTolB, newTolB) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					tolB = oldTolB;
					propertyChangeSupport.firePropertyChange(PROP_TOL_B, newTolB, oldTolB);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					tolB = newTolB;
					propertyChangeSupport.firePropertyChange(PROP_TOL_B, oldTolB, newTolB);
				}
			
			});
		}
	}

	@Override
	public BufferedImage process(BufferedImage image, FrameTime frameTime, boolean thumbnail, float thumbnailScale) {
		return filter.filter(image, null);
	}
	
	@Override
	public TimelineObject cloneForParent(TimelineObject parent) {
		GreenScreenImageFilter f = new GreenScreenImageFilter(parent, keyColor, tolA, tolB);
		f.setName(name);
		f.setDuration(parent.getDuration());
		return f;
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class GreenScreenFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Image/Green Screen";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return (obj instanceof ImageTimelineObject);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			GreenScreenImageFilter filter = new GreenScreenImageFilter(obj, new Color(100, 255, 100), 20, 30);
			filter.setDuration(obj.getDuration());
			filter.setName("Green Screen");
			return filter;
		}
		
	}
}
