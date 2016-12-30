/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.model;

/**
 *
 * @author Sebastian Weiss
 */
public interface Resource {
	
	String getName();
	void setName(String name);
	
	void load();
	
}
