/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve.filters;

import java.util.Objects;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.openide.util.lookup.ServiceProvider;
import org.shaman.sve.model.Resource;
import org.shaman.sve.model.ResourceTimelineObject;
import org.shaman.sve.model.TimelineObject;
import org.shaman.sve.player.Player;
import org.shaman.sve.player.PlayerAudioControl;
import org.simpleframework.xml.Element;

/**
 *
 * @author Sebastian Weiss
 */
public class GainAudioFilter extends TimelineObject implements CloneableFilter {
	
	@Element
	private float startGain;
	public static final String PROP_START_GAIN = "startGain";
	
	@Element
	private float endGain;
	public static final String PROP_END_GAIN = "endGain";
	
	@Element
	private boolean applyOverBorders;
	public static final String PROP_APPLY_OVER_BORDERS = "applyOverBorders";

	public GainAudioFilter() {
	}

	public GainAudioFilter(TimelineObject parent) {
		setParent(parent);
	}
	
	public GainAudioFilter(TimelineObject parent, float startGain, float endGain, boolean applyOverBorders) {
		this(parent);
		this.startGain = startGain;
		this.endGain = endGain;
		this.applyOverBorders = applyOverBorders;
	}

	/**
	 * Get the value of startGain
	 *
	 * @return the value of startGain
	 */
	public float getStartGain() {
		return startGain;
	}

	/**
	 * Set the value of startGain
	 *
	 * @param newStartGain new value of startGain
	 */
	public void setStartGain(final float newStartGain) {
		final float oldStartGain = this.startGain;
		this.startGain = newStartGain;
		propertyChangeSupport.firePropertyChange(PROP_START_GAIN, oldStartGain, newStartGain);
		if (!Objects.equals(oldStartGain, newStartGain) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					startGain = oldStartGain;
					propertyChangeSupport.firePropertyChange(PROP_START_GAIN, newStartGain, oldStartGain);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					startGain = newStartGain;
					propertyChangeSupport.firePropertyChange(PROP_START_GAIN, oldStartGain, newStartGain);
				}
			
			});
		}
	}

	/**
	 * Get the value of endGain
	 *
	 * @return the value of endGain
	 */
	public float getEndGain() {
		return endGain;
	}

	/**
	 * Set the value of endGain
	 *
	 * @param newEndGain new value of endGain
	 */
	public void setEndGain(final float newEndGain) {
		final float oldEndGain = this.endGain;
		this.endGain = newEndGain;
		propertyChangeSupport.firePropertyChange(PROP_END_GAIN, oldEndGain, newEndGain);
		if (!Objects.equals(oldEndGain, newEndGain) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					endGain = oldEndGain;
					propertyChangeSupport.firePropertyChange(PROP_END_GAIN, newEndGain, oldEndGain);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					endGain = newEndGain;
					propertyChangeSupport.firePropertyChange(PROP_END_GAIN, oldEndGain, newEndGain);
				}
			
			});
		}
	}

	/**
	 * Get the value of applyOverBorders
	 *
	 * @return the value of applyOverBorders
	 */
	public boolean isApplyOverBorders() {
		return applyOverBorders;
	}

	/**
	 * Set the value of applyOverBorders
	 *
	 * @param newApplyOverBorders new value of applyOverBorders
	 */
	public void setApplyOverBorders(final boolean newApplyOverBorders) {
		final boolean oldApplyOverBorders = this.applyOverBorders;
		this.applyOverBorders = newApplyOverBorders;
		propertyChangeSupport.firePropertyChange(PROP_APPLY_OVER_BORDERS, oldApplyOverBorders, newApplyOverBorders);
		if (!Objects.equals(oldApplyOverBorders, newApplyOverBorders) && undoSupport != null) {
			undoSupport.postEdit(new AbstractUndoableEdit() {

				@Override
				public void undo() throws CannotUndoException {
					super.undo();
					applyOverBorders = oldApplyOverBorders;
					propertyChangeSupport.firePropertyChange(PROP_APPLY_OVER_BORDERS, newApplyOverBorders, oldApplyOverBorders);
				}

				@Override
				public void redo() throws CannotRedoException {
					super.redo();
					applyOverBorders = newApplyOverBorders;
					propertyChangeSupport.firePropertyChange(PROP_APPLY_OVER_BORDERS, oldApplyOverBorders, newApplyOverBorders);
				}
			
			});
		}
	}

	/**
	 * Called by {@link PlayerAudioControl} to compute the gain
	 * @param gain the old gain
	 * @param millis the current global time
	 * @return the new gain
	 */
	public float getGain(float gain, int millis) {
		if (!isEnabled()) {
			return gain;
		}
		int localTime = parent.getLocalTime(millis);
		float alpha;
		if (localTime < start) {
			if (applyOverBorders) {
				alpha = startGain;
			} else {
				return gain;
			}
		} else if (localTime > start + duration) {
			if (applyOverBorders) {
				alpha = endGain;
			} else {
				return gain;
			}
		} else {
			alpha = startGain + ((localTime - start) / (float)duration) * (endGain - startGain);
		}
		return gain * alpha;
	}

	@Override
	public TimelineObject cloneForParent(TimelineObject parent) {
		GainAudioFilter f = new GainAudioFilter(parent, startGain, endGain, applyOverBorders);
		f.setStart(start);
		f.setDuration(duration);
		f.setName(name);
		return f;
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class SilenceFilterFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Audio/Silence";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return obj.playerProperties.containsKey(Player.AUDIO_CONTROL);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			GainAudioFilter f = new GainAudioFilter(obj, 0.0f, 0.0f, false);
			f.setDuration(obj.getDuration());
			f.setName("Silence");
			return f;
		}
		
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class FadeInFilterFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Audio/Fade In";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return obj.playerProperties.containsKey(Player.AUDIO_CONTROL);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			GainAudioFilter f = new GainAudioFilter(obj, 0.0f, 1.0f, false);
			int duration = Math.min(obj.getDuration(), 1000);
			f.setDuration(duration);
			f.setName("Fade in");
			return f;
		}
		
	}
	
	@ServiceProvider(service = FilterFactory.class)
	public static class FadeOutFilterFactory implements FilterFactory {

		@Override
		public String getName() {
			return "Audio/Fade Out";
		}

		@Override
		public boolean isApplicable(ResourceTimelineObject<Resource> obj) {
			return obj.playerProperties.containsKey(Player.AUDIO_CONTROL);
		}

		@Override
		public TimelineObject createFilter(ResourceTimelineObject<Resource> obj) {
			GainAudioFilter f = new GainAudioFilter(obj, 1.0f, 0.0f, false);
			int duration = Math.min(obj.getDuration(), 1000);
			f.setDuration(duration);
			f.setStart(obj.getDuration() - duration);
			f.setName("Fade out");
			return f;
		}
		
	}
}
