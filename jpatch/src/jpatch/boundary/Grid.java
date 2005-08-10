package jpatch.boundary;

import javax.vecmath.*;

public class Grid {
	
	public static final int NONE = 0;
	public static final int XY = 1;
	public static final int XZ = 2;
	public static final int YZ = 3;
	
	private int iPlane = XZ;
	private float fSpacing = JPatchSettings.getInstance().fGridSpacing;
	private int iSize = 25;
	private boolean bSnap = true;
	private JPatchSettings settings = JPatchSettings.getInstance();
	
	public Grid() {
	}
	
	public void setPlane(int plane) {
		iPlane = plane;
	}
	
	public boolean snap() {
		return bSnap;
	}
	
	public void snap(boolean enable) {
		bSnap = enable;
	}
	
	public float getSpacing() {
		return fSpacing;
	}
	
	public void setSpacing(float spacing) {
		fSpacing = spacing;
	}
	
	public boolean isSnapping() {
		return bSnap && (iPlane != NONE);
	}
	
	public void paint(JPatchCanvas canvas) {
		JPatchDrawable drawable = canvas.getDrawable();
		ViewDefinition viewDefinition = canvas.getViewDefinition();
		Matrix4f m4View = viewDefinition.getMatrix();
		if (viewDefinition.getView() != ViewDefinition.BIRDS_EYE) {
			drawPlanarGrid(drawable, m4View, canvas.getWidth(), canvas.getHeight());
		} else {
			//drawBirdsEyeGrid(drawable, m4View);
		}
	}

	private void drawPlanarGrid(JPatchDrawable drawable, Matrix4f m4View, int width, int height) {
		float gridScreenSpacing = fSpacing * m4View.getScale();
		float dx = width / 2;
		float dy = height / 2;
		float xcenter = m4View.m03;
		float ycenter = m4View.m13;
		if (gridScreenSpacing >= 4)
		{
			int start = Math.round((- xcenter)/gridScreenSpacing);
			int end = Math.round((2 * dx - xcenter)/gridScreenSpacing);
			for (int x = start; x <= end; x++)
			{
				if (x % 5 == 0) drawable.setColor(settings.iGrid);
				else drawable.setColor(settings.iGridMin);
				drawable.drawLine((int)(xcenter + x*gridScreenSpacing),0,(int)(xcenter + x*gridScreenSpacing),(int)height);
			}
			start = Math.round((- ycenter)/gridScreenSpacing);
			end = Math.round((2 * dy - ycenter)/gridScreenSpacing);
			for (int y = start; y <= end; y++)
			{
				if (y % 5 == 0) drawable.setColor(settings.iGrid);
				else drawable.setColor(settings.iGridMin);
				drawable.drawLine(0,(int)(ycenter + y*gridScreenSpacing),(int)width,(int)(ycenter + y*gridScreenSpacing));
			}
			drawable.setColor(settings.iGridMin);
			drawable.drawLine((int)(xcenter + 0*dx - 1),0,(int)(xcenter + 0*dx - 1),(int)(height));
			drawable.drawLine((int)(xcenter + 0*dx + 1),0,(int)(xcenter + 0*dx + 1),(int)(height));
			drawable.drawLine(0,(int)(ycenter + 0*dy - 1),(int)width,(int)(ycenter + 0*dy - 1));
			drawable.drawLine(0,(int)(ycenter + 0*dy + 1),(int)width,(int)(ycenter + 0*dy + 1));
		}
		drawable.setColor(settings.iGrid);
		drawable.drawLine((int)(xcenter + 0*dx),0,(int)(xcenter + 0*dx),(int)(height));
		drawable.drawLine(0,(int)(ycenter + 0*dy),(int)width,(int)(ycenter + 0*dy));
	}
	
	private void drawBirdsEyeGrid(JPatchDrawable drawable, Matrix4f m4View) {
		float max = fSpacing * iSize;
		float f;
		Point3f a = new Point3f();
		Point3f b = new Point3f();
		for (int n = -iSize; n <= iSize; n++) {
			f = fSpacing * n;
			if (n % 5 == 0) {
				drawable.setColor(settings.cGrid);
			} else {
				drawable.setColor(settings.cGridMin);
			}
			switch(iPlane) {
				case XZ:
					a.set(-max,0,f);
					b.set(max,0,f);
					break;
				case XY:
					a.set(-max,f,0);
					b.set(max,f,0);
					break;
				case YZ:
					a.set(0,-max,f);
					b.set(0,max,f);
			}
			m4View.transform(a);
			m4View.transform(b);
			drawable.drawLine3D(a,b);
			switch(iPlane) {
				case XZ:
					a.set(f,0,-max);
					b.set(f,0,max);
					break;
				case XY:
					a.set(f,-max,0);
					b.set(f,max,0);
					break;
				case YZ:
					a.set(0,f,-max);
					b.set(0,f,max);
			}
			m4View.transform(a);
			m4View.transform(b);
			drawable.drawLine3D(a,b);
		}
	}
	
	public boolean correctPosition(Tuple3f from, Tuple3f to) {
		//System.out.println("correctPosition");
		if (bSnap && iPlane != NONE) {
			to.x = (iPlane == YZ) ? from.x : Math.round(to.x / fSpacing) * fSpacing;
			to.y = (iPlane == XZ) ? from.y : Math.round(to.y / fSpacing) * fSpacing;
			to.z = (iPlane == XY) ? from.z : Math.round(to.z / fSpacing) * fSpacing;
		}
		return (!from.equals(to));
	}
	
	public boolean correctZPosition(Tuple3f from, Tuple3f to) {
		//System.out.println("correctZPosition");
		if (bSnap && iPlane != NONE) {
			to.x = (iPlane != YZ) ? from.x : Math.round(to.x / fSpacing) * fSpacing;
			to.y = (iPlane != XZ) ? from.y : Math.round(to.y / fSpacing) * fSpacing;
			to.z = (iPlane != XY) ? from.z : Math.round(to.z / fSpacing) * fSpacing;
		}
		return (!from.equals(to));
	}
	
	public void correctVector(Tuple3f t) {
		//System.out.println("correctVector");
		if (bSnap && iPlane != NONE) {
			t.x = (iPlane == YZ) ? 0 : Math.round(t.x / fSpacing) * fSpacing;
			t.y = (iPlane == XZ) ? 0 : Math.round(t.y / fSpacing) * fSpacing;
			t.z = (iPlane == XY) ? 0 : Math.round(t.z / fSpacing) * fSpacing;
		}
	}
	
	public void correctZVector(Tuple3f t) {
		//System.out.println("correctZVector");
		if (bSnap && iPlane != NONE) {
			t.x = (iPlane != YZ) ? 0 : Math.round(t.x / fSpacing) * fSpacing;
			t.y = (iPlane != XZ) ? 0 : Math.round(t.y / fSpacing) * fSpacing;
			t.z = (iPlane != XY) ? 0 : Math.round(t.z / fSpacing) * fSpacing;
		}
	}
	
	public Vector3f getCorrectionVector(Tuple3f t3) {
		//System.out.println("getCorrectionVector");
		Vector3f v3 = new Vector3f();
		if (bSnap && iPlane != NONE) {
			v3.set(t3);
			v3.x = (iPlane == YZ) ? v3.x : Math.round(v3.x / fSpacing) * fSpacing;
			v3.y = (iPlane == XZ) ? v3.y : Math.round(v3.y / fSpacing) * fSpacing;
			v3.z = (iPlane == XY) ? v3.z : Math.round(v3.z / fSpacing) * fSpacing;
			v3.sub(t3);
		}
		return v3;
	}
	
	public Vector3f getZCorrectionVector(Tuple3f t3) {
		//System.out.println("getZCorrectionVector");
		Vector3f v3 = new Vector3f();
		if (bSnap && iPlane != NONE) {
			v3.set(t3);
			v3.x = (iPlane != YZ) ? v3.x : Math.round(v3.x / fSpacing) * fSpacing;
			v3.y = (iPlane != XZ) ? v3.y : Math.round(v3.y / fSpacing) * fSpacing;
			v3.z = (iPlane != XY) ? v3.z : Math.round(v3.z / fSpacing) * fSpacing;
			v3.sub(t3);
		}
		return v3;
	}
}

