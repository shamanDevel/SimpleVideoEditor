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
import org.shaman.sve.filters.GainAudioFilter;
import org.shaman.sve.model.*;

/**
 * controls an audio input: TimelineObject linked to a audio resource or video resource
 * @author Sebastian
 */
public class PlayerAudioControl {

	private static final Logger LOG = Logger.getLogger(PlayerAudioControl.class.getName());
	
	private final ResourceTimelineObject timelineObject;
	private final Player player;

	private Sample sample;
	private SamplePlayer samplePlayer;
	private Gain gain;
	private float pos;
	private int start;
	private boolean running;
	
	public PlayerAudioControl(ResourceTimelineObject<? extends Resource> timelineObject, Player player) {
		this.timelineObject = timelineObject;
		this.player = player;

		Resource res = timelineObject.getResource();
		if (res instanceof AudioResource) {
			sample = ((AudioResource) res).getSample();
		} else if (res instanceof VideoResource) {
			sample = ((VideoResource) res).getSample();
		} else {
			throw new IllegalArgumentException("unknown resource: "+res);
		}
		
		samplePlayer = new SamplePlayer(player.getAudioContext(), sample);
		samplePlayer.setKillOnEnd(false);
		samplePlayer.setToEnd();
		gain = new Gain(player.getAudioContext(), 2);
		gain.addInput(samplePlayer);
		player.getMasterGain().addInput(gain);
	}
	
	public void setTime(int msec) {
		start = timelineObject.getStart();
		pos = msec - start;
	}
	
	public void start() {
		if (!timelineObject.isEnabled()) return;
		start = timelineObject.getStart();
		if (pos >= 0) {
			samplePlayer.start(pos);
			LOG.info("length: "+samplePlayer.getSample().getLength());
			running = true;
			LOG.log(Level.INFO, "{0} started", timelineObject);
		}
	}
	
	public void updateAudio(float timeMsec) {
		if (!timelineObject.isEnabled()) return;
		float npos = timeMsec - start;
		if (npos >= 0 && !running && npos<=sample.getLength()) {
			samplePlayer.start(pos);
			running = true;
			LOG.log(Level.INFO, "{0} started", timelineObject);
		} else if (npos > sample.getLength() && running) {
			running = false;
			LOG.log(Level.INFO, "{0} reached the end", timelineObject);
			return;
		}
		//update filters
		float gainValue = 1;
		for (TimelineObject child : timelineObject.getChildren()) {
			if (child instanceof GainAudioFilter) {
				gainValue = ((GainAudioFilter) child).getGain(gainValue, (int) timeMsec);
			}
		}
		gain.setGain(gainValue);
	}
	
	public void stop() {
		samplePlayer.setToEnd();
		running = false;
	}
}
