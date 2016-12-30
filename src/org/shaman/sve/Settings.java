/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Sebastian
 */
public class Settings {
	private Settings() {}
	
	private static final Preferences PREFS = Preferences.userNodeForPackage(Settings.class);
	private static final String KEY_DIR = "dir";
	
	public static File getLastDirectory() {
		return new File(PREFS.get(KEY_DIR, "E:\\Sebastian\\Programmierung\\Java\\SimpleVideoEditorTests"));
	} 
	public static void setLastDirectory(File f) {
		PREFS.put(KEY_DIR, f.getAbsolutePath());
	}
	
	public static String get(String key, String def) {
		return PREFS.get(key, def);
	}
	public static void set(String key, String value) {
		PREFS.put(key, value);
	}
	
	public static void flush() {
		try {
			PREFS.flush();
		} catch (BackingStoreException ex) {
			Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
