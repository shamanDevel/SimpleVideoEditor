/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import org.shaman.sve.FrameTime;
import org.shaman.sve.model.ImageTimelineObject;
import org.shaman.sve.model.Resource;

/**
 *
 * @author Sebastian Weiss
 */
public class PlayerImageControl {
	private static final Logger LOG = Logger.getLogger(PlayerImageControl.class.getName());
	
	private final ImageTimelineObject timelineObject;
	private final Player player;

	public PlayerImageControl(ImageTimelineObject timelineObject, Player player) {
		this.timelineObject = timelineObject;
		this.player = player;
	}
	
	public Image computeFrame(FrameTime ft, boolean thumbnail) {
		FrameTime start = ft.clone().fromMillis(timelineObject.getStart());
		FrameTime end = ft.clone().fromMillis(timelineObject.getDuration()).addLocal(start);
		if (start.compareTo(ft) > 0) {
			return null; //too early: before the video/image shows up
		} else if (end.compareTo(ft) <= 0) {
			return null; //too late: end of the video/image passed
		}
		int frame = ft.toFrames() - start.toFrames();
		
		BufferedImage img = ((Resource.ImageProvider) timelineObject.getResource()).getFrame(frame, thumbnail);
		//TODO: call filters
		
		return img;
	}
	
	public void drawFrame(Graphics2D g, Image frame, boolean thumbnail) {
		int x = timelineObject.getX();
		int y = timelineObject.getY();
		int w = timelineObject.getWidth();
		int h = timelineObject.getHeight();
		if (timelineObject.isKeepAspectRatio()) {
			//w,h defines a rect, place the image centered inside it
			float aspect = w / (float) h;
			if (aspect > timelineObject.getAspect()) {
				// w is greater
				w = (int) (h * timelineObject.getAspect());
				x = (timelineObject.getWidth() - w) / 2;
			} else if (aspect < timelineObject.getAspect()) {
				// h is greater
				h = (int) (w / timelineObject.getAspect());
				y = (timelineObject.getHeight() - h) / 2;
			}
		}
		g.drawImage(frame, x, y, w, h, null);
	}
}
