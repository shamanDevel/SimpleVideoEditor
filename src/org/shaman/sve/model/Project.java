/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import java.io.File;
import java.util.ArrayList;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 *
 * @author Sebastian Weiss
 */
@Root
public class Project {
	@Attribute
	private int version = 1;
	
	@Element
	private String name;
	@Element
	private File folder;
	@Element
	private int framerate;
	@Element
	private int width;
	@Element
	private int height;
	
	@ElementList
	private ArrayList<Resource> resources = new ArrayList<>();

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFolder() {
		return folder;
	}

	public void setFolder(File folder) {
		this.folder = folder;
	}

	public int getFramerate() {
		return framerate;
	}

	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public ArrayList<Resource> getResources() {
		return resources;
	}

	public void setResources(ArrayList<Resource> resources) {
		if (resources == null) {
			resources = new ArrayList<>();
		}
		this.resources = resources;
	}

	@Override
	public String toString() {
		return "Project{" + "version=" + version + ", name=" + name + ", folder=" + folder + ", framerate=" + framerate + ", width=" + width + ", height=" + height + '}';
	}
	
	
}
