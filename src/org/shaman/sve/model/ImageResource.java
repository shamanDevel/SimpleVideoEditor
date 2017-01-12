/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.shaman.sve.player.VideoTools;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class ImageResource implements Resource, Resource.ImageProvider {
	
	@Element
	private String name;

	private BufferedImage image;
	private BufferedImage thumbnail;
	private float thumbnailScale;
	
	public ImageResource() {
	}

	public ImageResource(String name) {
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
	
	@Override
	public void load(ResourceLoader loader) {
		if (isLoaded()) {
			return;
		}
		try {
			//load sample
			loader.setMessage(name+":\nloading");
			String path = loader.getProjectDirectory().getAbsolutePath()+"\\"+name;
			image = ImageIO.read(new File(path));
			thumbnailScale = VideoTools.getThumbnailScale(image);
			thumbnail = VideoTools.scaleImage(image, thumbnailScale);
			loader.setMessage(name+":\nloaded");
		} catch (IOException ex) {
			Logger.getLogger(AudioResource.class.getName()).log(Level.SEVERE, null, ex);
			loader.setMessage(ex.getMessage());
		}
	}

	@Override
	public boolean isLoaded() {
		return image != null;
	}

	@Override
	public void unload() {
		image = null;
	}

	public BufferedImage getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public BufferedImage getFrame(int index, boolean thumbnail) {
		if (thumbnail) {
			return this.thumbnail;
		} else {
			return this.image;
		}
	}

	@Override
	public float getThumbnailScale() {
		return thumbnailScale;
	}

}
