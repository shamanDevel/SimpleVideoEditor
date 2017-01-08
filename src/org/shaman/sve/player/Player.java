/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.UndoManager;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import org.shaman.sve.model.*;

/**
 *
 * @author Sebastian Weiss
 */
public class Player {

	private static final Logger LOG = Logger.getLogger(Player.class.getName());
	
	private static final int AUDIO_CLOCK_RESOLUTION = 10; //every 10 msec
	private static final String AUDIO_CONTROL = "audioControl";
	
	private final Project project;
	private final UndoManager undoManager;
	private final AudioContext ac;
	private final Gain masterGain;
	private final ResourceLoaderImpl resourceLoader;
	
	private boolean playing;
	private int time;
	private ClockBead clockBead;
	private ArrayList<PlayerAudioControl> audioControls = new ArrayList<>();

	public Player(Project project, UndoManager undoManager) {
		this.project = project;
		this.undoManager = undoManager;
		
		ac = new AudioContext(10000);
		ac.start();
		masterGain = new Gain(ac, 2);
		ac.out.addInput(masterGain);
		
		Clock clock = new Clock(ac, AUDIO_CLOCK_RESOLUTION);
		clockBead = new ClockBead();
		clock.addMessageListener(clockBead);
		ac.out.addDependent(clock);

		resourceLoader = new ResourceLoaderImpl();
	}
	
	public AudioContext getAudioContext() {
		return ac;
	}

	public Gain getMasterGain() {
		return masterGain;
	}
	
	/**
	 * Sets the selected time of playback in msec.
	 * @param msec 
	 */
	public void setTime(int msec) {
		time = msec;
	}
	
	/**
	 * Starts the playback
	 */
	public void start() {
		if (playing) {
			LOG.warning("player is already playing");
			return;
		}
		audioControls.clear();
		for (TimelineObject obj : project.getTimelineObjects()) {
			//todo: check if deactivated
			PlayerAudioControl pac = (PlayerAudioControl) obj.playerProperties.get(AUDIO_CONTROL);
			if (pac != null) {
				pac.setTime(time);
				pac.start();
				audioControls.add(pac);
			}
		}
//		clockBead.time = time;
		playing = true;
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
	}
	
	public void destroy() {
		ac.stop();
	}
	
	public void loadResources() {
		for (Resource r : project.getResources()) {
			loadResource(r);
		}
	}
	public void loadResource(Resource res) {
		res.load(resourceLoader);
	}
	
	public void initTimelineObjects() {
		for (TimelineObject obj : project.getTimelineObjects()) {
			initTimelineObject(obj);
		}
		computeTotalLength();
	}
	public void initTimelineObject(TimelineObject to) {
		to.playerProperties.clear();
		to.setUndoManager(undoManager);
		if (to instanceof ResourceTimelineObject) {
			@SuppressWarnings("unchecked")
			ResourceTimelineObject<Resource> obj = (ResourceTimelineObject<Resource>) to;
			Resource res = obj.getResource();
			if (res instanceof AudioResource) {
				PlayerAudioControl pac = new PlayerAudioControl(obj, this);
				obj.playerProperties.put(AUDIO_CONTROL, pac);
			}
		}
	}
	
	private void updateAudio(int time) {
		for (PlayerAudioControl pac : audioControls) {
			pac.updateAudio(time);
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
		project.setLength(length);
	}
	
	private class ResourceLoaderImpl implements Resource.ResourceLoader {

		@Override
		public File getProjectDirectory() {
			return project.getFolder();
		}

		@Override
		public void setMessage(String message) {
			LOG.info("loading: "+message);
		}

		@Override
		public AudioContext getAudioContext() {
			return ac;
		}
		
	}
		
	private class ClockBead extends Bead {
		private int time;
		
		@Override
		protected void messageReceived(Bead bead) {
			if (Player.this.playing) {
				time += AUDIO_CLOCK_RESOLUTION;
				Player.this.updateAudio(time);
			}
		}
		
	}
}
