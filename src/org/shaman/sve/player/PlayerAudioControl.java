/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.player;

import java.awt.image.SampleModel;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import org.shaman.sve.model.AudioResource;
import org.shaman.sve.model.TimelineObject;

/**
 * controls an audio input: TimelineObject linked to a audio resource or video resource
 * @author Sebastian
 */
public class PlayerAudioControl {

	private static final Logger LOG = Logger.getLogger(PlayerAudioControl.class.getName());
	
	private final TimelineObject timelineObject;
	private final Player player;

	private SamplePlayer samplePlayer;
	private Gain gain;
	private float pos;
	private int start;
	private boolean running;
	
	public PlayerAudioControl(TimelineObject timelineObject, Player player) {
		this.timelineObject = timelineObject;
		this.player = player;
		
		Sample sample = null;
		if (timelineObject.getResource() instanceof AudioResource) {
			sample = ((AudioResource) timelineObject.getResource()).getSample();
		} //todo: video
		timelineObject.setProperty(TimelineObject.PROP_DURATION, (int)Math.ceil(sample.getLength()));
		samplePlayer = new SamplePlayer(player.getAudioContext(), sample);
		samplePlayer.setKillOnEnd(false);
		samplePlayer.setToEnd();
		gain = new Gain(player.getAudioContext(), 2);
		gain.addInput(samplePlayer);
		player.getMasterGain().addInput(gain);
	}
	
	public void setTime(int msec) {
		start = timelineObject.getProperty(TimelineObject.PROP_START);
		pos = msec - start;
	}
	
	public void start() {
		start = timelineObject.getProperty(TimelineObject.PROP_START);
		if (pos >= 0) {
			samplePlayer.start(pos);
			running = true;
			LOG.log(Level.INFO, "{0} started", timelineObject);
		}
	}
	
	public void updateAudio(float timeMsec) {
		float npos = timeMsec - start;
		if (npos >= 0 && !running) {
			samplePlayer.start(pos);
			running = true;
			LOG.log(Level.INFO, "{0} started", timelineObject);
		}
	}
	
	public void stop() {
		samplePlayer.setToEnd();
		running = false;
	}
}
