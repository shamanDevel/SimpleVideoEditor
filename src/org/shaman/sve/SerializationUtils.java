/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import java.awt.Color;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.TreeStrategy;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

/**
 *
 * @author Sebastian Weiss
 */
public class SerializationUtils {
	private SerializationUtils() {}
	
	private static class ColorTransform implements Transform<Color> {
		private static final ColorTransform INSTANCE = new ColorTransform();

		@Override
		public Color read(String value) throws Exception {
			String[] vals = value.split(":");
			return new Color(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]), Integer.parseInt(vals[3]));
		}

		@Override
		public String write(Color value) throws Exception {
			return value.getRed() + ":" + value.getGreen() + ":" + value.getBlue() + ":" + value.getAlpha();
		}
	
	}
	
	private static class MyMatcher implements Matcher {
		private static final MyMatcher INSTANCE = new MyMatcher();
		
		@Override
		public Transform match(Class type) throws Exception {
			if (Color.class.isAssignableFrom(type)) {
				return ColorTransform.INSTANCE;
			}
			return null;
		}
		
	}
	
	public static Serializer createSerializer() {
		return new Persister(new TreeStrategy(), MyMatcher.INSTANCE);
	}
}
