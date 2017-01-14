/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.beadsproject.beads.data.Sample;
import org.shaman.sve.LRUCache;
import org.shaman.sve.player.VideoTools;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class VideoResource implements Resource, Resource.ImageProvider {
	private static final Logger LOG = Logger.getLogger(VideoResource.class.getName());
	
	public static class FrameKey {
		private final VideoResource resource;
		private final int frame;
		public FrameKey(VideoResource resource, int frame) {
			this.resource = resource;
			this.frame = frame;
		}
		@Override
		public int hashCode() {
			int hash = 5;
			hash = 89 * hash + Objects.hashCode(this.resource);
			hash = 89 * hash + this.frame;
			return hash;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final FrameKey other = (FrameKey) obj;
			if (!Objects.equals(this.resource, other.resource)) {
				return false;
			}
			if (this.frame != other.frame) {
				return false;
			}
			return true;
		}
	}
	public static final int MAX_CACHE_SIZE = 1000;
	/**
	 * Cache for thumbnails
	 */
	public static final LRUCache<FrameKey, BufferedImage> VIDEO_CACHE = new LRUCache<>(MAX_CACHE_SIZE);

	@Element
	private String name;
	@Element
	private int framerate;

	private VideoTools videoTools;
	private Sample audio;
	private int numFrames;
	private int duration;
	private float thumbnailScale;
	
	private class FrameLoader implements LRUCache.Factory<FrameKey, BufferedImage> {
		@Override
		public BufferedImage create(FrameKey key) {
			assert (key.resource == VideoResource.this);
			assert (videoTools != null);
			try {
				return videoTools.getThumbnail(key.frame);
			} catch (IOException ex) {
				Logger.getLogger(VideoResource.class.getName()).log(Level.SEVERE, "unable to load thumbnail "+key.frame, ex);
				return null;
			}
		}
	}
	private final FrameLoader factory = new FrameLoader();
	
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
			BufferedImage firstThumbnail = VIDEO_CACHE.getCreate(new FrameKey(this, 0), factory);
			loader.setMessage(name+":\nloaded");
			
			//3. get thumbnail scale
			BufferedImage highRes = getFrame(0, false);
			thumbnailScale = firstThumbnail.getWidth() / (float) highRes.getWidth();
			
			LOG.log(Level.INFO, "video loaded, audio length: {0}, video length: {1} seconds", 
					new Object[]{audio.getLength()/1000, numFrames / (float)framerate});
			duration = (int) Math.max(audio.getLength(), numFrames / (float)framerate);
		} catch (IOException ex) {
			LOG.log(Level.SEVERE, "unable to load video "+name, ex);
		}
	}

	@Override
	public boolean isLoaded() {
		return audio != null;
	}

	@Override
	public void unload() {
		audio.clear();
		audio = null;
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
		return new File(getName()).getName();
	}

	@Override
	public BufferedImage getFrame(int index, boolean thumbnail) {
		if (thumbnail) {
			FrameKey key = new FrameKey(this, Math.min(index, numFrames-1));
			return VIDEO_CACHE.getCreate(key, factory);
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
