/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleAudioFormat;
import net.beadsproject.beads.data.audiofile.AudioFileType;
import net.beadsproject.beads.ugens.RecordToSample;
import org.apache.commons.io.FileUtils;
import org.shaman.sve.FrameTime;
import org.shaman.sve.SimpleVideoEditor;
import org.shaman.sve.model.Project;

import static org.shaman.sve.player.VideoTools.FFMPEG_FILE;

/**
 *
 * @author Sebastian Weiss
 */
public class Exporter extends SwingWorker<Void, Void> {
	private static final Logger LOG = Logger.getLogger(Exporter.class.getName());
	
	private final Player player;
	private final File targetFile;
	private final Project project;
	private final FrameTime start;
	private final FrameTime end;
	private JDialog dialog;
	private JLabel message;
	private JProgressBar progress;

	public Exporter(Player player, File targetFile, FrameTime start, FrameTime end) {
		this.player = player;
		this.targetFile = targetFile;
		this.project = player.getProject();
		this.start = start;
		this.end = end;
	}
	
	private void createDialog() {
		//create dialog
		dialog = new JDialog(SimpleVideoEditor.MAIN_FRAME, "Exporting", true);
		message = new JLabel("Load project");
		message.setPreferredSize(new Dimension(300, 50));
		progress = new JProgressBar(0, 3000);
		dialog.setLayout(new BorderLayout());
		dialog.add(message, BorderLayout.NORTH);
		dialog.add(progress, BorderLayout.CENTER);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setResizable(false);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
	}
	
	public static void exportProject(Player player, File targetFile, FrameTime start, FrameTime end) {
		Exporter e = new Exporter(player, targetFile, start, end);
		e.createDialog();
		e.execute();
		e.dialog.setVisible(true);
	}

	@Override
	protected Void doInBackground() throws Exception {
		//clean output folder
		File outputFolder = new File(project.getFolder(), "output");
		if (!outputFolder.exists()) {
			outputFolder.mkdir();
		}
//		FileUtils.cleanDirectory(outputFolder);
//		LOG.info("output folder cleaned");
		
		FrameTime ft = new FrameTime(project.getFramerate());
		final float lengthFrames = end.toFrames() - start.toFrames();
		
		//write images
		setMessage("write frames");
		player.setRecording(true);
		setProgress(1, 0);
		BufferedImage frame = new BufferedImage(project.getWidth(), project.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int i=1;
		ft.set(start);
		Map<Object, Object> hints = new HashMap<>();
		hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		while (ft.compareTo(end) < 0) {
			File f = new File(outputFolder, "frame"+i+".png");
			if (f.exists()) {
				LOG.info(f.getName()+" already exists, skip it");
			} else {
				player.setTime(ft);
				Graphics2D g = frame.createGraphics();
				g.setRenderingHints(hints);
				player.draw(g);
				g.dispose();
				ImageIO.write(frame, "png", f);
				LOG.info("frame "+f.getName()+" processed");
			}
			ft.incrementLocal();
			setProgress(1, i / lengthFrames);
			i++;
		}
		LOG.info("frames written");
		
		//write audio
		setMessage("write audio");
		setProgress(2, 0);
		File audioFile = new File(outputFolder.getAbsolutePath()+File.separator+"audio.wav");
		if (audioFile.exists()) {
			LOG.info("audio file already exists, skip it");
		} else {
			Sample targetSample = new Sample(project.getLength().toMillis(), 2, 44100);
			RecordToSample rts = new RecordToSample(player.getAudioContext(), targetSample, RecordToSample.Mode.INFINITE);
			ft.set(start);
			FrameTime oldLength = project.getLength();
			project.setLength(end.clone());
			player.setTime(ft);
			player.getAudioContext().out.addDependent(rts);
			rts.addInput(player.getAudioContext().out);
			final Object barrier = new Object();
			PropertyChangeListener pcl = new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					switch (evt.getPropertyName()) {
						case Player.PROP_PLAYING:
							if ((boolean) evt.getNewValue() == false) {
								synchronized(barrier) {
									barrier.notifyAll();
								}
							}
							break;
						case Project.PROP_TIME:
							setProgress(2, (project.getTime().toFrames() - start.toFrames()) / lengthFrames);
							break;
					}
				}
			};
			player.addPropertyChangeListener(pcl);
			project.addPropertyChangeListener(pcl);
			player.start(true);
			synchronized (barrier) {
				barrier.wait();
			}
			player.removePropertyChangeListener(pcl);
			project.removePropertyChangeListener(pcl);
			player.getAudioContext().out.removeDependent(rts);
			project.setLength(oldLength);
			rts.pause(true);
			rts.kill();
			rts.clip();
			targetSample = rts.getSample();
			SampleAudioFormat af = new SampleAudioFormat(44100.0f, 16, 2, true, true);
			targetSample.write(audioFile.getAbsolutePath(), AudioFileType.WAV, af);
		}
		LOG.info("audio created");
		
		//assemble video
		setMessage("write video file");
		setProgress(3, -1);
		String[] args = {
			FFMPEG_FILE.getAbsolutePath(),
			"-y", "-framerate", String.valueOf(project.getFramerate()),
			"-start_number", "1",
			"-i", outputFolder.getAbsolutePath()+File.separator+"frame%d.png",
			"-i", outputFolder.getAbsolutePath()+File.separator+"audio.wav",
			"-c:v", "libx264",
			"-r", String.valueOf(project.getFramerate()),
			"-shortest",
			targetFile.getAbsolutePath()
		};
		ProcessBuilder pb = new ProcessBuilder(args).inheritIO();
		LOG.info("start ffmpeg: "+pb.command());
		Process p = pb.start();
		int exit = p.waitFor();
		if (exit != 0) {
			throw new IOException("ffmpeg terminated with a failure");
		}
		LOG.info("video file written to "+targetFile);
		
		player.setRecording(false);
		
		return null;
	}

	void setMessage(String message) {
		this.message.setText(message);
	}
	void setProgress(int step, float progress) {
		if (progress < 0) {
			this.progress.setIndeterminate(true);
		} else {
			this.progress.setIndeterminate(false);
		}
		int p = (step-1)*1000 + (int)(progress * 1000);
		this.progress.setValue(p);
	}

	@Override
	protected void done() {
		super.done();
		dialog.setVisible(false);
		dialog.dispose();
	}
}
