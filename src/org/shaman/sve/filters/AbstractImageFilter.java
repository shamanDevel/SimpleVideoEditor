/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import java.awt.Image;
import java.awt.image.BufferedImage;
import org.shaman.sve.FrameTime;
import org.shaman.sve.model.TimelineObject;

/**
 *
 * @author Sebastian Weiss
 */
public abstract class AbstractImageFilter extends TimelineObject {
	
	/**
	 * Processes a frame
	 * @param image the current frame (from the previous filter or the source)
	 * @param frameTime the current time
	 * @param thumbnail {@code true} if it is only a thumbnail
	 * @param thumbnailScale if it is a thumbnail, this contains the scaling factor
	 * @return the new image
	 */
	public abstract BufferedImage process(BufferedImage image, FrameTime frameTime, boolean thumbnail, float thumbnailScale);
	
}
