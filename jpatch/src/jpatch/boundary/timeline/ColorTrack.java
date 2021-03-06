package jpatch.boundary.timeline;

import java.awt.*;
import java.util.Map;

import javax.swing.*;
import javax.vecmath.Color3f;

import jpatch.boundary.MainFrame;
import jpatch.entity.*;

public class ColorTrack extends Track<MotionCurve.Color3f> {

	public ColorTrack(TimelineEditor timelineEditor, MotionCurve.Color3f motionCurve) {
		super(timelineEditor, motionCurve);
	}

	@Override
	public void paint(Graphics g, int y, Map<MotionKey, TrackView.KeyData> selection, MotionKey[] hitKeys) {
		Rectangle clip = g.getClipBounds();
		int height = getHeight();
		
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		
		Color background, track;
		if (timelineEditor.getHeader().getSelectedTracks().contains(this)) {
			background = TimelineEditor.SELECTED_BACKGROUND;
			track = TimelineEditor.SHADOW;
		} else {
			background = TimelineEditor.BACKGROUND;
			track = TimelineEditor.SHADOW;
		}
		
		g.setColor(background);
		g.fillRect(clip.x, y + 0, clip.width, 3);
		g.fillRect(clip.x, y + height - 1, clip.width, 2);
		g.setColor(track);
		g.drawLine(clip.x, y + 3, clip.x + clip.width, y + 3);
		g.drawLine(clip.x, y + height - 2, clip.x + clip.width, y + height - 2);

		frame = start / fw - 1 + (int) MainFrame.getInstance().getAnimation().getStart();
		for (int x = -fw ; x <= clip.width + fw; x ++) {
			float f = (float) (start + x - fw / 2) / fw;
			Color3f color = motionCurve.getColor3fAt(f);
			color.clamp(0, 1);
			g.setColor(color.get());
			g.drawLine(x + start, y + TOP, x + start, y + TOP + 6);
		}
		
		g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.25f));
		g.drawLine(clip.x, y + 4, clip.x + clip.width, y + 4);
		
		for (int x = -fw ; x <= clip.width + fw; x += fw) {
			MotionKey.Color3f colorKey = (MotionKey.Color3f) motionCurve.getKeyAt(frame);
			if (colorKey != null) {
				//g.fill3DRect(x + start - iFrameWidth / 2, y + 2, iFrameWidth, 11, true);
				Color c = Color.BLACK;
				Color fillColor;
				if (keyHit(colorKey, hitKeys))
					fillColor = TimelineEditor.HIT_KEY;
				else if (selection.containsKey(colorKey))
					fillColor = TimelineEditor.SELECTED_KEY;
				else {
					Color3f color = colorKey.getColor3f();
					color.clamp(0, 1);
					c = (color.x + color.y + color.z > 1.5f) ? Color.BLACK : Color.WHITE;
					fillColor = color.get();
				}
//				g.fillOval(x + start - 3, y + 4, 6, 6);
//				g.setColor(c);
//				g.drawOval(x + start - 3, y + 4, 6, 6);
				drawKey(g, colorKey, x + start - 3, y + 4, fillColor, c);
			}
			frame++;
		}
	}
}
