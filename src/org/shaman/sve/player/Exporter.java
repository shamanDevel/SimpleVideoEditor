/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
	private JDialog dialog;
	private JLabel message;
	private JProgressBar progress;

	public Exporter(Player player, File targetFile) {
		this.player = player;
		this.targetFile = targetFile;
		this.project = player.getProject();
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
	
	public static void exportProject(Player player, File targetFile) {
		Exporter e = new Exporter(player, targetFile);
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
		LOG.info("output folder cleaned");
		
		FrameTime ft = new FrameTime(project.getFramerate());
		
		//write images
		setMessage("write frames");
		player.setRecording(true);
		setProgress(1, 0);
		BufferedImage frame = new BufferedImage(project.getWidth(), project.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int i=1;
		while (ft.compareTo(project.getLength()) < 0) {
			player.setTime(ft);
			Graphics2D g = frame.createGraphics();
			player.draw(g);
			g.dispose();
			ImageIO.write(frame, "png", new File(outputFolder, "frame"+i+".png"));
			ft.incrementLocal();
			setProgress(1, i / (float)project.getLength().toFrames());
			i++;
		}
		LOG.info("frames written");
		
		//write audio
		setMessage("write audio");
		setProgress(2, 0);
		Sample targetSample = new Sample(project.getLength().toMillis(), 2, 96000);
		RecordToSample rts = new RecordToSample(player.getAudioContext(), targetSample, RecordToSample.Mode.FINITE);
		ft.fromMillis(0);
		player.setTime(ft);
		player.getAudioContext().out.addDependent(rts);
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
						setProgress(2, project.getTime().toFrames() / (float)project.getLength().toFrames());
						break;
				}
			}
		};
		player.addPropertyChangeListener(pcl);
		player.start(true);
		synchronized (barrier) {
			barrier.wait();
		}
		player.removePropertyChangeListener(pcl);
		rts.pause(true);
		player.getAudioContext().out.removeDependent(rts);
		rts.clip();
		SampleAudioFormat af = new SampleAudioFormat(96000.0f, 16, 2, true, true);
		targetSample.write(outputFolder.getAbsolutePath()+File.separator+"audio.wav", AudioFileType.WAV, af);
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
