/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.io.File;
import javax.sound.sampled.Clip;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class AudioResource implements Resource {
	@Element
	private String name;
	
	private Clip clip;

	public AudioResource() {
	}

	public AudioResource(String name) {
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
		//TODO
		
	}
	
	@Override
	public String toString() {
		return new File(getName()).getName();
	}
}
