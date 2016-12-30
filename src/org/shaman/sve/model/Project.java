/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
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
	
}
