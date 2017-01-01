/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.awt.image.BufferedImage;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class VideoResource implements Resource{

	@Element
	private String name;
	@Element
	private int framerate;

	private AudioResource audio;
	private BufferedImage[] thumbnails;
	
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

	@Override
	public void load(ResourceLoader loader) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isLoaded() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
