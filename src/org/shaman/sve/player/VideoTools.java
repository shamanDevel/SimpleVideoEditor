/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.beadsproject.beads.data.Sample;
import org.shaman.sve.Settings;

/**
 *
 * @author Sebastian Weiss
 */
public class VideoTools {
	private static final Logger LOG = Logger.getLogger(VideoTools.class.getName());
	
	private static final String FFMPEG_KEY = "ffmpeg";
	private static File FFMPEG_FILE;
	public static final String AUDIO_SUFFIX = ".mp3";
	public static final String IMAGES_SUFFIX = ".zip";
	public static final String THUMBNAILS_SUFFIX = "_thumbs.zip";
	
	private final String basePath;
	private ZipFile imagesZip;
	private ZipFile thumbsZip;
	
	public static void checkFfmpeg(Component parent) {
		if (FFMPEG_FILE != null && FFMPEG_FILE.exists()) {
			return;
		}
		String path = Settings.get(FFMPEG_KEY, null);
		if (path == null || !new File(path).exists() || !new File(path).canExecute()) {
			LOG.info("no ffmpeg stored in settings, open dialog");
			JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new FileNameExtensionFilter("executables", "exe"));
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle("Select ffmpeg.exe");
			int ret = fc.showOpenDialog(parent);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File selected = fc.getSelectedFile();
				if (selected.canExecute()) {
					FFMPEG_FILE = selected;
					Settings.set(FFMPEG_KEY, selected.getAbsolutePath());
					LOG.log(Level.INFO, "ffmpeg found: {0}", selected);
				}
			}
		} else {
			FFMPEG_FILE = new File(path);
			LOG.log(Level.INFO, "ffmpeg found in settings: {0}", path);
		}
	}

	public VideoTools(String basePath) {
		this.basePath = basePath;
	}

	public String getAudioPath() {
		return basePath + AUDIO_SUFFIX;
	}
	
	public Sample loadAudio() throws IOException {
		return new Sample(getAudioPath());
	}
	
	private ZipFile openZipFile(String path) throws IOException {
		return new ZipFile(path);
	}
	
	public String getFullResImagesPath() {
		return basePath + IMAGES_SUFFIX;
	}
	
	public String getThumbnailImagesPath() {
		return basePath + THUMBNAILS_SUFFIX;
	}
	
	public int getFrameCount() throws IOException {
		if (thumbsZip == null) {
			thumbsZip = openZipFile(getThumbnailImagesPath());
		}
		return thumbsZip.size();
	}
	
	public BufferedImage getThumbnail(int frame) throws IOException {
		if (thumbsZip == null) {
			thumbsZip = openZipFile(getThumbnailImagesPath());
		}
		try (InputStream in = thumbsZip.getInputStream(thumbsZip.getEntry(frame+".png"))) {
			return ImageIO.read(in);
		}
	}
	
	public BufferedImage getFullResImage(int frame) throws IOException {
		if (imagesZip == null) {
			imagesZip = openZipFile(getFullResImagesPath());
		}
		try (InputStream in = imagesZip.getInputStream(imagesZip.getEntry(frame+".png"))) {
			return ImageIO.read(in);
		}
	}
}
