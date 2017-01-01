/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

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
	
	public static interface ResourceLoader {
		File getProjectDirectory();
		void setMessage(String message);
		AudioContext getAudioContext();
	}
}
