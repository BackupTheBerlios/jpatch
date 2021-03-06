package jpatch.boundary.laf;

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;

public class SmoothInternalFrameUI extends MetalInternalFrameUI {
    public SmoothInternalFrameUI(JInternalFrame jinternalframe) {
        super(jinternalframe);
    }

    public static ComponentUI createUI(JComponent jcomponent) {
        return new SmoothInternalFrameUI((JInternalFrame) jcomponent);
    }

    public void paint(Graphics g, JComponent c) {
        SmoothUtilities.configureGraphics(g);
        super.paint(g, c);
    }
}
