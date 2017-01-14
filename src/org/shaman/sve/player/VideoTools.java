/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Enumeration;
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
	public static File FFMPEG_FILE;
	private static final String AUDIO_SUFFIX = "audio.mp3";
	private static final String IMAGES_SUFFIX = ".png";
	private static final String THUMBNAILS_ZIP_SUFFIX = "thumbs.zip";
	private static final int MAX_THUMBNAIL_SIZE = 200;
	
	private final String basePath;
	private int startFrame = -1;
	private int frameCount = -1;
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
		return basePath + File.separator + AUDIO_SUFFIX;
	}
	
	public Sample loadAudio() throws IOException {
		return new Sample(getAudioPath());
	}
	
	private ZipFile openZipFile(String path) throws IOException {
		return new ZipFile(path);
	}
	
	public String getFullResImagesPath() {
		return basePath;
	}
	
	public String getThumbnailImagesPath() {
		return basePath + File.separator + THUMBNAILS_ZIP_SUFFIX;
	}
	
	public int getFrameCount() throws IOException {
		if (frameCount >= 0) {
			return frameCount;
		}
		if (thumbsZip == null) {
			thumbsZip = openZipFile(getThumbnailImagesPath());
		}
		frameCount = thumbsZip.size();
		return frameCount;
	}
	
	private int getStartFrame() throws IOException {
		if (startFrame >= 0) {
			return startFrame;
		}
		if (thumbsZip == null) {
			thumbsZip = openZipFile(getThumbnailImagesPath());
		}
		int minIndex = Integer.MAX_VALUE;
		for (Enumeration<? extends ZipEntry> e = thumbsZip.entries(); e.hasMoreElements(); ) {
			ZipEntry entry = e.nextElement();
			int index = Integer.parseInt(entry.getName().substring(0, entry.getName().indexOf('.')));
			minIndex = Math.min(index, minIndex);
		} 
		startFrame = minIndex;
		return minIndex;
	}
	
	public BufferedImage getThumbnail(int frame) throws IOException {
		frame += getStartFrame();
		if (thumbsZip == null) {
			thumbsZip = openZipFile(getThumbnailImagesPath());
		}
		try (InputStream in = thumbsZip.getInputStream(thumbsZip.getEntry(frame+IMAGES_SUFFIX))) {
			return ImageIO.read(in);
		}
	}
	
	public BufferedImage getFullResImage(int frame) throws IOException {
		return ImageIO.read(new File(basePath + File.separator + (frame+getStartFrame()) + IMAGES_SUFFIX));
	}
	
	public static float getThumbnailScale(BufferedImage img) {
		int maxS = Math.max(img.getWidth(), img.getHeight());
		if (maxS <= MAX_THUMBNAIL_SIZE) {
			return 1;
		}
		float scale = MAX_THUMBNAIL_SIZE / (float) maxS;
		return scale;
	}
	
	public static BufferedImage scaleImage(BufferedImage img, float scale) {
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
	
	public static BufferedImage ensureFormat(BufferedImage img, int format) {
		if (img.getType() == format) {
			return img;
		} else {
			BufferedImage i = new BufferedImage(img.getWidth(), img.getHeight(), format);
			Graphics g = i.getGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();
			return i;
		}
	}
	
	public static void copyVideoIntoProject(File videoFile, File directory, String baseName, int framerate, String startTime) throws IOException, InterruptedException {
		String root = directory.getAbsolutePath() + File.separator + baseName;
		File rootDir = new File(root);
		if (!rootDir.mkdir()) {
			throw new IOException("unable to create output folder");
		}
		//call ffmpeg to extract frames
		String[] args = {
			FFMPEG_FILE.getAbsolutePath(),
			"-ss", startTime,
			"-i", videoFile.getAbsolutePath(),
			"-r", String.valueOf(framerate),
			"-n",
			root + File.separator + "%d.png"
		};
		ProcessBuilder pb = new ProcessBuilder(args).inheritIO();
		LOG.log(Level.INFO, "start ffmpeg: {0}", pb.command());
		Process p = pb.start();
		int exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("ffmpeg terminated with a failure");
		}
		LOG.info("frames extracted");
		
		//write files into zip files
		try (ZipOutputStream zipLow = new ZipOutputStream(new FileOutputStream(new File(root+File.separator+THUMBNAILS_ZIP_SUFFIX)))) {
			for (File image : rootDir.listFiles()) {
				if (!image.getName().endsWith(IMAGES_SUFFIX)) {
					continue;
				}
				//read image
				BufferedImage img = ImageIO.read(image);
				img = ensureFormat(img, BufferedImage.TYPE_4BYTE_ABGR);
				//write thumbnail
				img = scaleImage(img, getThumbnailScale(img));
				zipLow.putNextEntry(new ZipEntry(image.getName()));
				ImageIO.write(img, "png", zipLow);
				zipLow.flush();
				zipLow.closeEntry();
			}		
		}
		LOG.info("frames compressed and written to zip file");
		
		//call ffmpeg to extract audio
		args = new String[]{
			FFMPEG_FILE.getAbsolutePath(),
			"-ss", startTime,
			"-i", videoFile.getAbsolutePath(),
			"-n",
			root + File.separator + AUDIO_SUFFIX
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
