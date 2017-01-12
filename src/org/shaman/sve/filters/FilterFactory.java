/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import org.shaman.sve.model.Resource;
import org.shaman.sve.model.ResourceTimelineObject;
import org.shaman.sve.model.TimelineObject;

/**
 * factory for filters
 * @author Sebastian Weiss
 */
public interface FilterFactory {
	/**
	 * The name of the filter, separate paths by '/'.
	 * @return 
	 */
	String getName();
	
	/**
	 * Checks if the filter can be applied on the specified object
	 * @param obj the object
	 * @return {@code true} if applicable and it should show up in the UI
	 */
	boolean isApplicable(ResourceTimelineObject<Resource> obj);
	
	/**
	 * Creates the filter and sets the parent to the specified object
	 * @param obj the parent resource object
	 * @return the filter
	 */
	TimelineObject createFilter(ResourceTimelineObject<Resource> obj);
	
}
