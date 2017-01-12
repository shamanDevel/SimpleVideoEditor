/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import com.jhlabs.image.PointFilter;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
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
public class GreenScreenImageFilter extends AbstractImageFilter {
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

	/**
	 * Set the value of keyColor
	 *
	 * @param keyColor new value of keyColor
	 */
	public void setKeyColor(Color keyColor) {
		Color oldKeyColor = this.keyColor;
		this.keyColor = keyColor;
		propertyChangeSupport.firePropertyChange(PROP_KEYCOLOR, oldKeyColor, keyColor);
		keyCb = rgb2cb(keyColor.getRed(), keyColor.getGreen(), keyColor.getBlue());
		keyCr = rgb2cr(keyColor.getRed(), keyColor.getGreen(), keyColor.getBlue());
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
	 * @param tolA new value of tolA
	 */
	public void setTolA(int tolA) {
		int oldTolA = this.tolA;
		this.tolA = tolA;
		propertyChangeSupport.firePropertyChange(PROP_TOL_A, oldTolA, tolA);
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
	 * @param tolB new value of tolB
	 */
	public void setTolB(int tolB) {
		int oldTolB = this.tolB;
		this.tolB = tolB;
		propertyChangeSupport.firePropertyChange(PROP_TOL_B, oldTolB, tolB);
	}

	@Override
	public BufferedImage process(BufferedImage image, FrameTime frameTime, boolean thumbnail, float thumbnailScale) {
		return filter.filter(image, null);
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
