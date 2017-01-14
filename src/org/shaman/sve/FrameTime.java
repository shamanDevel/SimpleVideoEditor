/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shaman.sve;

import org.simpleframework.xml.Element;

/**
 * Represents a time in the project: seconds + frames
 * @author Sebastian Weiss
 */
public final class FrameTime implements Comparable<FrameTime>, Cloneable {
	
	//persistent variables
	
	/**
	 * The seconds
	 */
	@Element
	private int seconds;
	
	/**
	 * The frame within the second.
	 * Between 0 (inclusive) and framesPerSecond (exclusive) 
	 */
	@Element
	private int frame;
	
	@Element
	private int framesPerSecond;

	@Deprecated //Marker, this constructor should only be used by the serializer
	public FrameTime() {
	}
	public FrameTime(FrameTime toClone) {
		seconds = toClone.seconds;
		frame = toClone.frame;
		framesPerSecond = toClone.framesPerSecond;
	}
	public FrameTime(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int seconds) {
		this.seconds = seconds;
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public int getFramesPerSecond() {
		return framesPerSecond;
	}

	public void setFramesPerSecond(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}
	
	/**
	 * Returns the second-part of {@link #seconds}, without minutes
	 * @return the seconds
	 */
	public int getSecond() {
		return seconds % 60;
	}
	
	/**
	 * Returns the minute-part of {@link #seconds}
	 * @return 
	 */
	public int getMinute() {
		return seconds / 60;
	}
	
	/**
	 * Tests if the instance is valid: no negative values and frames
	 * is within the allowed range
	 * @return {@code true} iff it is valid
	 */
	public boolean isValid() {
		return (seconds >= 0) && (frame >= 0) && (frame < framesPerSecond);
	}

	/**
	 * Converts this frame time into milliseconds
	 * @return the time in milliseconds
	 */
	public int toMillis() {
		return seconds*1000 + frame * 1000 / framesPerSecond;
	}
	
	/**
	 * Sets this instance to the closest possible frame time (round down) 
	 * as specified by the milliseconds from the beginning.
	 * @param millis 
	 */
	public FrameTime fromMillis(int millis) {
		seconds = millis / 1000;
		frame = (millis % 1000) * framesPerSecond / 1000;
		frame = Math.min(frame, framesPerSecond);
		return this;
	}
	
	/**
	 * Converts it into frames from the beginning
	 * @return the frames from beginning
	 */
	public int toFrames() {
		return frame + seconds * framesPerSecond;
	}
	
	public FrameTime incrementLocal() {
		frame++;
		if (frame >= framesPerSecond) {
			frame = 0;
			seconds++;
		}
		return this;
	}
	public FrameTime decrementLocal() {
		if (frame==0 && seconds==0) {
			return this;
		}
		frame--;
		if (frame < 0) {
			frame = framesPerSecond - 1;
			seconds--;
		}
		return this;
	}
	
	private void checkCompatibility(FrameTime other) {
		if (other.framesPerSecond != this.framesPerSecond) {
			throw new IllegalArgumentException("frames per second do not match");
		}
	}
	
	/**
	 * Adds the specified frame time to this instance
	 * @param other
	 * @return 
	 */
	public FrameTime addLocal(FrameTime other) {
		checkCompatibility(other);
		seconds += other.seconds;
		frame += other.frame;
		if (frame >= framesPerSecond) {
			frame -= framesPerSecond;
			seconds++;
		}
		return this;
	}
	
	/**
	 * Computes the maximum of this and other and stores it in this.
	 * @param other
	 * @return 
	 */
	public FrameTime maxLocal(FrameTime other) {
		checkCompatibility(other);
		if (other.seconds > this.seconds) {
			this.seconds = other.seconds;
			this.frame = other.frame;
		} else if (this.seconds > other.seconds) {
			//do nothing
		} else { //this.seconds == other.seconds
			this.frame = Math.max(this.frame, other.frame);
		}
		return this;
	}
	
	/**
	 * Computes the minimum of this and other and stores it in this.
	 * @param other
	 * @return 
	 */
	public FrameTime minLocal(FrameTime other) {
		checkCompatibility(other);
		if (other.seconds < this.seconds) {
			this.seconds = other.seconds;
			this.frame = other.frame;
		} else if (this.seconds < other.seconds) {
			//do nothing
		} else { //this.seconds == other.seconds
			this.frame = Math.min(this.frame, other.frame);
		}
		return this;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 71 * hash + this.seconds;
		hash = 71 * hash + this.frame;
		hash = 71 * hash + this.framesPerSecond;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FrameTime other = (FrameTime) obj;
		if (this.seconds != other.seconds) {
			return false;
		}
		if (this.frame != other.frame) {
			return false;
		}
		if (this.framesPerSecond != other.framesPerSecond) {
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings("CloneDoesntCallSuperClone")
	public FrameTime clone() {
		return new FrameTime(this);
	}

	@Override
	public String toString() {
		return getMinute()+","+getSecond()+":"+getFrame();
	}

	@Override
	public int compareTo(FrameTime o) {
		if (this.seconds < o.seconds) {
			return -1;
		} else if (this.seconds > o.seconds) {
			return 1;
		} else {
			return Integer.compare(frame, o.frame);
		}
	}
	
	public javax.swing.SpinnerModel getSpinnerModel() {
		return new SpinnerModel(this);
	}
	
	public javax.swing.SpinnerModel getSpinnerModel(FrameTime min, FrameTime max) {
		return new SpinnerModel(this, min, max);
	}
	
	private static class SpinnerModel extends javax.swing.AbstractSpinnerModel {

		private FrameTime time;
		private final FrameTime min;
		private final FrameTime max;
		
		public SpinnerModel() {
			min = null;
			max = null;
		}

		public SpinnerModel(FrameTime time) {
			this.time = time;
			min = null;
			max = null;
		}

		public SpinnerModel(FrameTime time, FrameTime min, FrameTime max) {
			this.time = time;
			this.min = min;
			this.max = max;
		}
	
		@Override
		public Object getValue() {
			return time;
		}

		@Override
		public void setValue(Object value) {
			if (!(value instanceof FrameTime)) return;
			time = (FrameTime) value;
			fireStateChanged();
		}

		@Override
		public Object getNextValue() {
			time.incrementLocal();
			fireStateChanged();
			return time;
		}

		@Override
		public Object getPreviousValue() {
			time.decrementLocal();
			fireStateChanged();
			return time;
		}
		
	}
	
	//TODO
	//private class SpinnerEditor extends javax.swing.JSpi
}
