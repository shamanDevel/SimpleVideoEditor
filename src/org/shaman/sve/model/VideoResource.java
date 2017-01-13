/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.beadsproject.beads.data.Sample;
import org.shaman.sve.player.VideoTools;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class VideoResource implements Resource, Resource.ImageProvider {
	private static final Logger LOG = Logger.getLogger(VideoResource.class.getName());

	@Element
	private String name;
	@Element
	private int framerate;

	private VideoTools videoTools;
	private Sample audio;
	private BufferedImage[] thumbnails;
	private int numFrames;
	private int duration;
	private float thumbnailScale;
	
	public VideoResource() {
	}

	public VideoResource(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}

	public int getFramerate() {
		return framerate;
	}

	@Override
	public void load(ResourceLoader loader) {
		try {
			//path
			loader.setMessage(name+":\nload audio");
			String basePath = loader.getProjectDirectory().getAbsolutePath()+"\\"+name;
			videoTools = new VideoTools(basePath);
			
			//1. load audio
			audio = videoTools.loadAudio();
			loader.setMessage(name+":\nload video");
			
			//2. load thumbnails
			numFrames = videoTools.getFrameCount();
			thumbnails = new BufferedImage[numFrames];
			for (int i=0; i<numFrames; ++i) {
				thumbnails[i] = videoTools.getThumbnail(i);
				thumbnails[i] = VideoTools.ensureFormat(thumbnails[i], BufferedImage.TYPE_INT_ARGB);
			}
			loader.setMessage(name+":\nloaded");
			
			//3. get thumbnail scale
			BufferedImage highRes = getFrame(0, false);
			thumbnailScale = thumbnails[0].getWidth() / (float) highRes.getWidth();
			
			LOG.log(Level.INFO, "video loaded, audio length: {0}, video length: {1} seconds", 
					new Object[]{audio.getLength()/1000, numFrames / (float)framerate});
			duration = (int) Math.max(audio.getLength(), numFrames / (float)framerate);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "unable to load video "+name, ex);
		}
	}

	@Override
	public boolean isLoaded() {
		return thumbnails != null;
	}

	@Override
	public void unload() {
		audio.clear();
		audio = null;
		thumbnails = null;
	}
	
	public int getDurationInMsec() {
		return duration;
	}

	@Override
	public float getThumbnailScale() {
		return thumbnailScale;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public BufferedImage getFrame(int index, boolean thumbnail) {
		if (thumbnail) {
			return thumbnails[Math.min(index, numFrames-1)];
		} else {
			try {
				return VideoTools.ensureFormat(videoTools.getFullResImage(Math.min(index, numFrames-1)), BufferedImage.TYPE_INT_ARGB);
			} catch (IOException ex) {
				Logger.getLogger(VideoResource.class.getName()).log(Level.SEVERE, 
						"unable to load high res frame with index "+index, ex);
				return null;
			}
		}
	}

	public Sample getSample() {
		return audio;
	}
}
