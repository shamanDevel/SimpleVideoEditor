/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.Graphics2D;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.UndoableEditSupport;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import org.shaman.sve.FrameTime;
import org.shaman.sve.Selections;
import org.shaman.sve.model.*;

/**
 *
 * @author Sebastian Weiss
 */
public class Player implements PropertyChangeListener {

	private static final Logger LOG = Logger.getLogger(Player.class.getName());
	
	private static final int AUDIO_CLOCK_RESOLUTION = 10; //every 10 msec
	public static final String AUDIO_CONTROL = "audioControl";
	public static final String IMAGE_CONTROL = "imageControl";
	
	public static final String PROP_PLAYING = "playing";
	
	private final Project project;
	private final UndoableEditSupport undoSupport;
	private final Selections selections;
	private final AudioContext ac;
	private final Gain masterGain;
	private final ResourceLoaderImpl resourceLoader;
	private ResourceLoadingCallback resourceLoadingCallback;
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	private boolean playing;
	private boolean recording;
	private long playingStartTime;
	private FrameTime playingStartFrame;
	private FrameTime currentTime;
	private ClockBead clockBead;
	private ArrayList<PlayerAudioControl> audioControls = new ArrayList<>();

	public Player(Project project, UndoableEditSupport undoSupport, Selections selections) {
		this.project = project;
		this.undoSupport = undoSupport;
		this.selections = selections;
		project.addPropertyChangeListener(this);
		
		ac = new AudioContext(10000);
		ac.start();
		masterGain = new Gain(ac, 2);
		ac.out.addInput(masterGain);
		
		Clock clock = new Clock(ac, AUDIO_CLOCK_RESOLUTION);
		clockBead = new ClockBead();
		clock.addMessageListener(clockBead);
		ac.out.addDependent(clock);

		resourceLoader = new ResourceLoaderImpl();
		currentTime = new FrameTime(project.getFramerate());
	}
	
	public AudioContext getAudioContext() {
		return ac;
	}

	public Gain getMasterGain() {
		return masterGain;
	}

	public Project getProject() {
		return project;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.addPropertyChangeListener(l);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener l) {
		propertyChangeSupport.removePropertyChangeListener(l);
	}
	
	/**
	 * Sets the selected time of playback in msec.
	 * @param msec 
	 */
	public void setTime(FrameTime msec) {
		currentTime = msec;
	}
	
	public boolean isPlaying() {
		return playing;
	}

	public boolean isRecording() {
		return recording;
	}
	void setRecording(boolean recording) {
		this.recording = recording;
	}
	
	/**
	 * Starts the playback
	 */
	public void start(boolean recording) {
		if (playing) {
			LOG.warning("player is already playing");
			return;
		}
		this.recording = recording;
		audioControls.clear();
		for (TimelineObject obj : project.getTimelineObjects()) {
			//todo: check if deactivated
			PlayerAudioControl pac = (PlayerAudioControl) obj.playerProperties.get(AUDIO_CONTROL);
			if (pac != null) {
				pac.setTime(currentTime.toMillis());
				pac.start();
				audioControls.add(pac);
			}
		}
		clockBead.start(currentTime.toMillis());
		playing = true;
		propertyChangeSupport.firePropertyChange(PROP_PLAYING, false, true);
		playingStartTime = System.currentTimeMillis();
		playingStartFrame = currentTime.clone();
	}
	
	/**
	 * Stops the audio playback
	 */
	public void stop() {
		if (!playing) {
			LOG.warning("player was already stopped");
			return;
		}
		for (PlayerAudioControl pac : audioControls) {
			pac.stop();
		}
		playing = false;
		propertyChangeSupport.firePropertyChange(PROP_PLAYING, true, false);
	}
	
	/**
	 * Draws the images. Should be called on a regular basis, optimally with the project frame rate
	 * @param g 
	 */
	public void draw(Graphics2D g) {
		//update time
		if (playing) {
			long elapsedTime = System.currentTimeMillis() - playingStartTime;
			currentTime.fromMillis((int) elapsedTime);
			currentTime.addLocal(playingStartFrame);
			project.setTime(currentTime.clone());
		}
		
		Graphics2D g2d = (Graphics2D) g.create();
		
		//draw background
		g2d.setColor(project.getBackgroundColor());
		g2d.fillRect(0, 0, project.getWidth(), project.getHeight());
		
		//first test, no parallelism
		for (TimelineObject to : project.getTimelineObjects()) {
			PlayerImageControl pic = (PlayerImageControl) to.playerProperties.get(IMAGE_CONTROL);
			if (pic != null) {
				if (recording) {
					Image img = pic.computeFrame(currentTime, false);
					pic.drawFrame(g2d, img, false, false);
				} else {
					Image img = pic.computeFrame(currentTime, true);
					pic.drawFrame(g2d, img, true, selections.getSelectedTimelineObject()==to);
				}
			}
		}
		
		g2d.dispose();
	}
	
	/**
	 * Destroys all allocated resources
	 */
	public void destroy() {
		ac.stop();
	}
	
	/**
	 * Exports the project
	 * @param target the target file
	 * @param start start time
	 * @param end end time
	 */
	public void export(File target, FrameTime start, FrameTime end) {
		Exporter.exportProject(this, target, start, end);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == project) {
			switch (evt.getPropertyName()) {
				case Project.PROP_RESOURCE_ADDED:
					loadResource((Resource) evt.getNewValue());
					break;
				case Project.PROP_RESOURCE_REMOVED:
					deleteResource((Resource) evt.getOldValue());
					break;
				case Project.PROP_TIMELINE_OBJECT_ADDED:
					initTimelineObject((TimelineObject) evt.getNewValue());
					computeTotalLength();
					break;
				case Project.PROP_TIMELINE_OBJECT_REMOVED:
					deleteTimelineObject((TimelineObject) evt.getOldValue());
					computeTotalLength();
					break;
			}
		}
	}
	
	public static interface ResourceLoadingCallback {
		void onMessage(String message);
		void onProgress(float progress);
	}
	
	public void loadResources(ResourceLoadingCallback callback) {
		this.resourceLoadingCallback = callback;
		if (callback != null) {callback.onProgress(0);}
		int i = 0;
		for (Resource r : project.getResources()) {
			loadResource(r);
			i++;
			if (callback != null) {callback.onProgress(i / (float)project.getResources().size());}
		}
		this.resourceLoadingCallback = null;
	}
	public void loadResource(Resource res) {
		if (!res.isLoaded()) {
			res.load(resourceLoader);
		}
	}
	
	public void deleteResource(Resource res) {
		assert(res.isLoaded());
		res.unload();
	}
	
	public void initTimelineObjects() {
		for (TimelineObject obj : project.getTimelineObjects()) {
			initTimelineObject(obj);
		}
		computeTotalLength();
	}
	public void initTimelineObject(TimelineObject to) {
		to.playerProperties.clear();
		to.setUndoSupport(undoSupport);
		if (to instanceof ResourceTimelineObject) {
			@SuppressWarnings("unchecked")
			ResourceTimelineObject<Resource> obj = (ResourceTimelineObject<Resource>) to;
			Resource res = obj.getResource();
			if ((res instanceof AudioResource) || (res instanceof VideoResource)) {
				PlayerAudioControl pac = new PlayerAudioControl(obj, this);
				obj.playerProperties.put(AUDIO_CONTROL, pac);
			}
			if ((res instanceof VideoResource) || (res instanceof ImageResource)) {
				PlayerImageControl pic = new PlayerImageControl((ImageTimelineObject) obj, this);
				obj.playerProperties.put(IMAGE_CONTROL, pic);
			}
		}
		for (TimelineObject child : to.getChildren()) {
			initTimelineObject(child);
		}
	}
	
	public void deleteTimelineObject(TimelineObject to) {
		to.playerProperties.clear();
	}
	
	private void updateAudio(int msec) {
		if (msec > project.getLength().toMillis() && recording) {
			stop();
			return;
		}
		for (PlayerAudioControl pac : audioControls) {
			pac.updateAudio(msec);
		}
		if (recording) {
			project.setTime(project.getTime().fromMillis(msec));
		}
	}
	
	/**
	 * Computes the total length of the playback
	 */
	private void computeTotalLength() {
		int length = 0;
		for (TimelineObject obj : project.getTimelineObjects()) {
			if (obj instanceof ResourceTimelineObject) {
				int len = ((ResourceTimelineObject)obj).getStart() + ((ResourceTimelineObject)obj).getDuration();
				length = Math.max(length, len);
			}
		}
		LOG.log(Level.INFO, "length of the project: {0} msec", length);
		project.setLength(new FrameTime(project.getFramerate()).fromMillis(length));
	}
	
	private class ResourceLoaderImpl implements Resource.ResourceLoader {

		@Override
		public File getProjectDirectory() {
			return project.getFolder();
		}

		@Override
		public void setMessage(String message) {
			LOG.info("loading: "+message);
			if (resourceLoadingCallback != null) {
				resourceLoadingCallback.onMessage(message);
			}
		}

		@Override
		public AudioContext getAudioContext() {
			return ac;
		}
		
	}
		
	private class ClockBead extends Bead {
		private int startTime;
		private long lastTime;
		
		private void start(int msec) {
			startTime = msec;
			lastTime = System.currentTimeMillis();
		}
		
		@Override
		protected void messageReceived(Bead bead) {
			if (Player.this.playing) {
				int elapsed = (int) (System.currentTimeMillis() - lastTime);
				Player.this.updateAudio(startTime + elapsed);
			}
		}
		
	}
}
