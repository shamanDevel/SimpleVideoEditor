/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.awt.image.BufferedImage;
import java.io.File;
import net.beadsproject.beads.core.AudioContext;

/**
 *
 * @author Sebastian Weiss
 */
public interface Resource {
	
	String getName();
	void setName(String name);
	
	void load(ResourceLoader loader);
	boolean isLoaded();
	void unload();
	
	public static interface ResourceLoader {
		File getProjectDirectory();
		void setMessage(String message);
		AudioContext getAudioContext();
	}
	
	public static interface ImageProvider {
		/**
		 * Retrieves the frame with the specified index from the beginning
		 * of the resource
		 * @param index the frame index
		 * @param thumbnail {@code true} if only a preview thumbnail should be returned
		 * @return the frame
		 */
		BufferedImage getFrame(int index, boolean thumbnail);
		/**
		 * Returns the scale of the thumbnail
		 * @return 
		 */
		float getThumbnailScale();
	}
}
