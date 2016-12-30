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
public class ImageResource implements Resource{
	
	@Element
	private String name;

	private BufferedImage image;
	
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
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	
	
}
