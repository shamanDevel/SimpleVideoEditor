/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Clip;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.SamplePlayer;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class AudioResource implements Resource {
	@Element
	private String name;
	
	private Sample sample;

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
		if (isLoaded()) {
			return;
		}
		try {
			//load sample
			loader.setMessage(name+":\nloading");
			sample = new Sample(loader.getProjectDirectory().getAbsolutePath()+"\\"+name);
			loader.setMessage(name+":\nloaded");
		} catch (IOException ex) {
			Logger.getLogger(AudioResource.class.getName()).log(Level.SEVERE, null, ex);
			loader.setMessage(ex.getMessage());
		}
	}

	@Override
	public boolean isLoaded() {
		return sample != null;
	}

	public Sample getSample() {
		return sample;
	}
	
	@Override
	public String toString() {
		return new File(getName()).getName();
	}
}
