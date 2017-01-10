/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

/**
 * Timeline object for both images and videos
 * @author Sebastian Weiss
 * @param <T> 
 */
public class ImageTimelineObject<T extends Resource & Resource.ImageProvider> extends ResourceTimelineObject<T> {

	public ImageTimelineObject() {
	}

	public ImageTimelineObject(T resource) {
		super(resource);
	}
	
}
