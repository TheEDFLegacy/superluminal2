package com.kartoflane.superluminal2.components;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.kartoflane.superluminal2.components.interfaces.Predicate;
import com.kartoflane.superluminal2.components.interfaces.Selectable;
import com.kartoflane.superluminal2.mvc.controllers.AbstractController;

/**
 * The class governs when and in what order controllers are drawn.
 * Also implements several methods for selection.
 * 
 * The last controller in a layer is the topmost one.
 * The first view in a layer is the bottom-most one.
 * 
 * @author kartoFlane
 * 
 */
public class LayeredPainter implements PaintListener {
	public enum Layers {
		MOUNT,
		IMAGES,
		GRID,
		ROOM,
		ROOM_ZERO,
		SYSTEM,
		STATION,
		DOOR,
		OVERLAY,
		SHIP_ORIGIN,
		CURSOR
	}

	private static final LayeredPainter instance = new LayeredPainter();

	/** Specifies the layer order for selection and highlighting purposes. */
	private static final Layers[] selectionOrder = { Layers.GRID, Layers.IMAGES, Layers.MOUNT,
			Layers.ROOM, Layers.ROOM_ZERO, Layers.SYSTEM, Layers.STATION, Layers.DOOR, Layers.OVERLAY,
			Layers.SHIP_ORIGIN, Layers.CURSOR };

	/** Specifies the order in which the layers are drawn. */
	protected TreeMap<Layers, ArrayList<AbstractController>> layerMap = new TreeMap<Layers, ArrayList<AbstractController>>();

	public LayeredPainter() {
		// Add a bunch of empty lists to hold layers.
		for (Layers layer : Layers.values())
			layerMap.put(layer, new ArrayList<AbstractController>());
	}

	public static LayeredPainter getInstance() {
		return instance;
	}

	/**
	 * Places the Controller at the top of the layer. The newly added Controller
	 * will be drawn last on the layer, and will therefore appear as topmost.
	 */
	public void add(AbstractController controller, Layers layer) {
		if (controller == null)
			throw new IllegalArgumentException("Controller is null.");
		ArrayList<AbstractController> list = layerMap.get(layer);
		list.add(controller);
	}

	/**
	 * Places the Controller at the bottom of the layer. The newly added Controller
	 * will be drawn first on the layer, and will therefore appear as bottom-most.
	 */
	public void addToBottom(AbstractController controller, Layers layer) {
		if (controller == null)
			throw new IllegalArgumentException("Controller is null.");
		ArrayList<AbstractController> list = layerMap.get(layer);
		list.add(0, controller);
	}

	public void remove(AbstractController controller) {
		if (controller == null)
			throw new IllegalArgumentException("Controller is null.");
		for (Layers layer : Layers.values()) {
			if (layerMap.get(layer).remove(controller))
				break;
		}
	}

	public boolean contains(AbstractController controller) {
		if (controller == null)
			throw new IllegalArgumentException("Controller is null.");
		for (Layers layer : Layers.values()) {
			for (AbstractController c : layerMap.get(layer))
				if (c == controller)
					return true;
		}
		return false;
	}

	public TreeMap<Layers, ArrayList<AbstractController>> getLayerMap() {
		return layerMap;
	}

	/** @return sorted set of Layers in the order in which they are drawn. */
	public TreeSet<Layers> getLayers() {
		TreeSet<Layers> layers = new TreeSet<Layers>();
		for (Layers layer : Layers.values())
			layers.add(layer);
		return layers;
	}

	/** @return array of Layers, specifying the layer order for selection and highlighting purposes. */
	public Layers[] getSelectionOrder() {
		return selectionOrder.clone();
	}

	public void paintControl(PaintEvent e) {
		Rectangle dirtyRect = new Rectangle(e.x - 2, e.y - 2, e.width + 4, e.height + 4);

		for (Layers layer : Layers.values()) {
			for (AbstractController controller : layerMap.get(layer)) {
				if (controller.intersects(dirtyRect))
					controller.redraw(e);
			}
		}
		e.gc.dispose();
	}

	/** @return the topmost visible Controller in a layer at a point, or null if none was found. */
	public AbstractController getControllerAt(int x, int y, Layers layer) {
		// Layers are arranged in such a way that the bottom-most controller is the first in the layer's list.
		// Therefore, we need to iterate over the list in reverse order to get the topmost controller.
		for (int i = layerMap.get(layer).size() - 1; i >= 0; i--) {
			AbstractController controller = layerMap.get(layer).get(i);
			if (controller.isVisible() && controller.contains(x, y))
				return controller;
		}
		return null;
	}

	public AbstractController getControllerAt(Point p, Layers layer) {
		return getControllerAt(p.x, p.y, layer);
	}

	/** @return the topmost visible, selectable Controller in a layer at a point, or null if none was found. */
	public AbstractController getSelectableControllerAt(int x, int y, Layers layer) {
		// Layers are arranged in such a way that the bottom-most controller is the first in the layer's list.
		// Therefore, we need to iterate over the list in reverse order to get the topmost controller.
		for (int i = layerMap.get(layer).size() - 1; i >= 0; i--) {
			AbstractController controller = layerMap.get(layer).get(i);
			if (controller instanceof Selectable && ((Selectable) controller).isSelectable() && controller.isVisible() &&
					controller.contains(x, y)) {
				return controller;
			}
		}
		return null;
	}

	public AbstractController getSelectableControllerAt(Point p, Layers layer) {
		return getSelectableControllerAt(p.x, p.y, layer);
	}

	/** @return the topmost visible Controller, on whichever layer. */
	public AbstractController getControllerAt(int x, int y) {
		for (int i = selectionOrder.length - 1; i >= 0; i--) {
			Layers layer = selectionOrder[i];
			AbstractController controller = getControllerAt(x, y, layer);
			if (controller != null)
				return controller;
		}
		return null;
	}

	public AbstractController getControllerAt(Point p) {
		return getControllerAt(p.x, p.y);
	}

	/** @eturn the topmost visible, selectable Controller at the given coordinates, on whichever layer. */
	public AbstractController getSelectableControllerAt(int x, int y) {
		for (int i = selectionOrder.length - 1; i >= 0; i--) {
			Layers layer = selectionOrder[i];
			AbstractController controller = getSelectableControllerAt(x, y, layer);
			if (controller != null)
				return controller;
		}
		return null;
	}

	public AbstractController getSelectableControllerAt(Point p) {
		return getSelectableControllerAt(p.x, p.y);
	}

	/** @return the topmost Controller matching the conditions set by the predicate. */
	public AbstractController getControllerMatching(Predicate<AbstractController> p, Layers layer) {
		for (int i = layerMap.get(layer).size() - 1; i >= 0; i--) {
			AbstractController controller = layerMap.get(layer).get(i);
			if (p.accept(controller)) {
				return controller;
			}
		}
		return null;
	}

	/** @return the topmost Controller matching the conditions set by the predicate, on whichever layer. */
	public AbstractController getControllerMatching(Predicate<AbstractController> p) {
		for (Layers layer : selectionOrder) {
			AbstractController controller = getControllerMatching(p, layer);
			if (controller != null)
				return controller;
		}
		return null;
	}

	/**
	 * @return a list of all controllers that contain the point
	 */
	public List<AbstractController> getAllControllersAt(int x, int y) {
		ArrayList<AbstractController> list = new ArrayList<AbstractController>();

		for (Layers layer : selectionOrder) {
			for (AbstractController controller : layerMap.get(layer)) {
				if (controller != null && controller.contains(x, y))
					list.add(controller);
			}
		}

		return list;
	}

	/**
	 * @return a list of all controllers that contain the point
	 */
	public List<AbstractController> getAllControllersAt(Point p) {
		return getAllControllersAt(p.x, p.y);
	}

	/**
	 * @return a list of all selectable controllers that contain the point
	 */
	public List<AbstractController> getAllSelectableControllersAt(int x, int y) {
		ArrayList<AbstractController> list = new ArrayList<AbstractController>();

		for (Layers layer : selectionOrder) {
			for (AbstractController controller : layerMap.get(layer)) {
				if (controller != null && controller.isSelectable() && controller.contains(x, y))
					list.add(controller);
			}
		}

		return list;
	}

	/**
	 * @return a list of all selectable controllers that contain the point
	 */
	public List<AbstractController> getAllSelectableControllersAt(Point p) {
		return getAllSelectableControllersAt(p.x, p.y);
	}

	/** @return all controllers matching the conditions set by the predicate */
	public List<AbstractController> getAllControllersMatching(Predicate<AbstractController> p) {
		ArrayList<AbstractController> list = new ArrayList<AbstractController>();

		for (Layers layer : selectionOrder) {
			for (AbstractController controller : layerMap.get(layer)) {
				if (controller != null && p.accept(controller))
					list.add(controller);
			}
		}
		return list;
	}

	/** @return all controllers on the given layer matching the conditions set by the predicate */
	public List<AbstractController> getAllControllersMatching(Predicate<AbstractController> p, Layers layer) {
		ArrayList<AbstractController> list = new ArrayList<AbstractController>();

		for (AbstractController controller : layerMap.get(layer)) {
			if (controller != null && p.accept(controller))
				list.add(controller);
		}
		return list;
	}
}
