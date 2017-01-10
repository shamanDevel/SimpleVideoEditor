/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.IOUtils;
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
	public static final int MAX_THUMBNAIL_SIZE = 200;
	
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
	
	private static BufferedImage scaleImage(BufferedImage img) {
		int maxS = Math.max(img.getWidth(), img.getHeight());
		if (maxS <= MAX_THUMBNAIL_SIZE) {
			return img;
		}
		float scale = MAX_THUMBNAIL_SIZE / (float) maxS;
		int newWidth = (int) (img.getWidth() * scale);
		int newHeight = (int) (img.getHeight()* scale);
		BufferedImage resized = new BufferedImage(newWidth, newHeight, img.getType());
		Graphics2D g = resized.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, img.getWidth(),
			img.getHeight(), null);
		g.dispose();
		return resized;
	}
	
	public static void copyVideoIntoProject(File videoFile, File directory, String baseName, int framerate) throws IOException, InterruptedException {
		String root = directory.getAbsolutePath() + File.separator + baseName;
		//call ffmpeg to extract frames
		String[] args = {
			FFMPEG_FILE.getAbsolutePath(),
			"-i", videoFile.getAbsolutePath(),
			"-r", String.valueOf(framerate),
			"-n",
			root + "%d.png"
		};
		ProcessBuilder pb = new ProcessBuilder(args).inheritIO();
		LOG.info("start ffmpeg: "+pb.command());
		Process p = pb.start();
		int exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("ffmpeg terminated with a failure");
		}
		LOG.info("frames extracted");
		
		//write files into zip files
		try (ZipOutputStream zipHigh = new ZipOutputStream(new FileOutputStream(new File(root+IMAGES_SUFFIX)));
			 ZipOutputStream zipLow = new ZipOutputStream(new FileOutputStream(new File(root+THUMBNAILS_SUFFIX)))) {
			for (int i=1; ;++i) {
				File image = new File(root+i+".png");
				if (!image.exists()) {
					break;
				}
				//read image
				BufferedImage img = ImageIO.read(image);
				//write high res
				zipHigh.putNextEntry(new ZipEntry((i-1)+".png"));
				ImageIO.write(img, "png", zipHigh);
				zipHigh.flush();
				zipHigh.closeEntry();
				//write thumbnail
				img = scaleImage(img);
				zipLow.putNextEntry(new ZipEntry((i-1)+".png"));
				ImageIO.write(img, "png", zipLow);
				zipLow.flush();
				zipLow.closeEntry();
				//delete image
				image.delete();
			}		
		}
		LOG.info("frames compressed and written to zip file");
		
		//call ffmpeg to extract audio
		args = new String[]{
			FFMPEG_FILE.getAbsolutePath(),
			"-i", videoFile.getAbsolutePath(),
			"-n",
			root + AUDIO_SUFFIX
		};
		pb = new ProcessBuilder(args).inheritIO();
		LOG.info("start ffmpeg: "+pb.command());
		p = pb.start();
		exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("ffmpeg terminated with a failure");
		}
		LOG.info("audio extracted");
	}
}
