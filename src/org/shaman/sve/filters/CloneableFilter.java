/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import org.shaman.sve.model.TimelineObject;

/**
 *
 * @author Sebastian Weiss
 */
public interface CloneableFilter {
	
	TimelineObject cloneForParent(TimelineObject parent);
	
}
