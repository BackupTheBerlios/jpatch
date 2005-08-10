package jpatch;

import jpatch.boundary.*;
import jpatch.entity.*;

public final class Modeler {
	public static void main(String[] args) {
		if (args.length == 1 && args[0].equals("animator")) {
			Animator.getInstance();
		} else {
			Model model = new Model();
			new MainFrame(model);
		}
		//if (args.length > 0 && args[0].equals("--test")) {
		//	Tester tester = new CylinderTest();
		//}
		//PluginManager pluginManager = new PluginManager();
		//pluginManager.loadPlugins();
		//((MeshToolBar)mainFrame.getMeshToolBar()).dumpComponents();
	}
}
