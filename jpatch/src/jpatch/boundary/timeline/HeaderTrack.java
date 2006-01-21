package jpatch.boundary.timeline;

import java.awt.*;

import javax.swing.UIManager;

import jpatch.entity.MotionCurve;

public class HeaderTrack extends Track {
	private String name;
	private int iIndent;
	
	public HeaderTrack(TimelineEditor timelineEditor, String name, int indent) {
		super(timelineEditor, null);
		this.name = name;
		iIndent = indent;
	}

	public String getName() {
		return name;
	}
	
	public int getIndent() {
		return iIndent;
	}
	
	public int getHeight() {
		return 12;
	}
	
	public void paint(Graphics g, int y) {	
		Rectangle clip = g.getClipBounds();
		int fw = timelineEditor.getFrameWidth();
		int start = clip.x - clip.x % fw + fw / 2;
		int frame = start / fw - 1;
		
		g.setColor(UIManager.getColor("ScrollBar.darkShadow"));
		g.drawLine(clip.x, y + getHeight() - 1, clip.x + clip.width, y + getHeight() - 1);
		g.setColor(UIManager.getColor("ScrollBar.shadow"));
		g.fillRect(clip.x, y, clip.width, getHeight() - 1);
	}
}