# SimpleVideoEditor

**This project was migrated to gitlab: https://gitlab.com/shaman42/SimpleVideoEditor.**
This repository is no longer active.

A simple video editor written in java

For a compiled version, see the release page.

Features:
 - Import any video format that ffmpeg supports
 - Import any audio and image format that Java supports
 - Pixel-wise placement of videos and images
 - Precise control of start time and duration of videos, images and audio down to milliseconds
 - Some filters
  - Fade in and out (transparency)
  - Fade in and out (audio volume)
  - cropping
  - color keying for green screens
 - Fast startup time because the frames are loaded on demand
 - Markers
 
Screenshot:
(This screenshot shows how I used this program to create the video for [How Far I’ll Go](http://www.sebastian-weiss.eu/music/sax-ensemble/how-far-ill-go-moana/))
![Editing "How Far I'll Go"](http://sebastian-weiss.eu/wp-content/uploads/2017/01/SimpleVideoEditor.png)
 
It requires at least Java 7 and ffmpeg (the binary) to run.
Libraries used:
 - Beads (http://www.beadsproject.net/)
 - Apache Commons (https://commons.apache.org/)
 - Java Image Filters (http://www.jhlabs.com/ip/filters/)
 - SwingX
 - Simple XML (http://simple.sourceforge.net/)
 - l2fprod (https://github.com/sarxos/l2fprod-common)
All libraries are bundled with this project.

I used a very naïve approach to process videos: I let ffmpeg extract all frames, process them, and let ffmpeg combine them again to a video. This leads to the following problems:
 - Very large projects: The raw video data of „How Far I’ll Go“ used up 70GB of my hard drive!
 - Slow importing: The frames of a video have to be extracted first and compressed. But after that, the preview runs in realtime!
 - Slow export: The final export of the video in the full resolution with all filters, antialising and so on, took threee hours!
In conclusion, this project works and can be used. But I still look for a better alternative to edit the videos

For more information, see http://www.sebastian-weiss.eu/programming/simple-video-editor/.
