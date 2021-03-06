package com.kartoflane.superluminal2.ui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.components.Grid;
import com.kartoflane.superluminal2.components.LayeredPainter;
import com.kartoflane.superluminal2.components.NotDeletableException;
import com.kartoflane.superluminal2.components.enums.Hotkeys;
import com.kartoflane.superluminal2.components.enums.Images;
import com.kartoflane.superluminal2.core.Cache;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.core.MouseInputDispatcher;
import com.kartoflane.superluminal2.mvc.controllers.AbstractController;
import com.kartoflane.superluminal2.mvc.controllers.CursorController;
import com.kartoflane.superluminal2.tools.CreationTool;
import com.kartoflane.superluminal2.tools.DoorTool;
import com.kartoflane.superluminal2.tools.GibTool;
import com.kartoflane.superluminal2.tools.ImagesTool;
import com.kartoflane.superluminal2.tools.ManipulationTool;
import com.kartoflane.superluminal2.tools.MountTool;
import com.kartoflane.superluminal2.tools.PropertyTool;
import com.kartoflane.superluminal2.tools.RoomTool;
import com.kartoflane.superluminal2.tools.StationTool;
import com.kartoflane.superluminal2.tools.Tool.Tools;
import com.kartoflane.superluminal2.ui.sidebar.ManipulationToolComposite;
import com.kartoflane.superluminal2.utils.ShipSaveUtils;
import com.kartoflane.superluminal2.utils.UIUtils;

public class EditorWindow {
	private static final Logger log = LogManager.getLogger(EditorWindow.class);

	public static final int SIDEBAR_MIN_WIDTH = 290;
	public static final int CANVAS_MIN_SIZE = 400;

	private static EditorWindow instance;

	private final HashMap<Tools, ToolItem> toolItemMap = new HashMap<Tools, ToolItem>();
	private final RGB canvasRGB = new RGB(164, 164, 164);

	private int sidebarWidth = SIDEBAR_MIN_WIDTH;
	private Color canvasColor = null;
	private boolean shellResizing = false;

	private File saveDestination = null;

	// UI widgets' variables
	private Shell shell;
	private ScrolledComposite sideContainer;
	private Canvas canvas;

	private ToolItem tltmPointer;
	private ToolItem tltmCreation;
	private ToolItem tltmGib;
	private ToolItem tltmProperties;
	private ToolItem tltmImages;
	private ToolItem tltmManager;

	private MenuItem mntmUndo;
	private MenuItem mntmRedo;
	private MenuItem mntmDelete;

	private MenuItem mntmShowAnchor;
	private MenuItem mntmShowMounts;
	private MenuItem mntmShowRooms;
	private MenuItem mntmShowDoors;
	private MenuItem mntmShowStations;
	private MenuItem mntmShowHull;
	private MenuItem mntmShowFloor;
	private MenuItem mntmShowShield;
	private MenuItem mntmNewShip;
	private MenuItem mntmLoadShip;
	private MenuItem mntmSettings;
	private MenuItem mntmGrid;
	private MenuItem mntmSaveShip;
	private MenuItem mntmSaveShipAs;
	private MenuItem mntmCloseShip;
	private SashForm editorContainer;
	private MenuItem mntmModMan;
	private ToolItem tltmCloak;
	private MenuItem mntmResetLinks;
	private MenuItem mntmOptimalOffset;

	public EditorWindow(Display display) {
		instance = this;

		shell = new Shell(display);
		shell.setText(String.format("%s v%s - FTL Ship Editor", Superluminal.APP_NAME, Superluminal.APP_VERSION));
		GridLayout gl_shell = new GridLayout(1, false);
		gl_shell.marginHeight = 0;
		gl_shell.marginWidth = 0;
		shell.setLayout(gl_shell);

		Monitor m = display.getPrimaryMonitor();
		Rectangle displaySize = m.getClientArea();
		displaySize.width = (displaySize.width / 5) * 4;
		displaySize.height = (displaySize.height / 5) * 4;

		// Instantiate quasi-singletons
		new MouseInputDispatcher();
		CursorController.newInstance();
		new SystemsMenu(shell);

		Manager.TOOL_MAP.put(Tools.POINTER, new ManipulationTool(this));
		Manager.TOOL_MAP.put(Tools.CREATOR, new CreationTool(this));
		Manager.TOOL_MAP.put(Tools.GIB, new GibTool(this));
		Manager.TOOL_MAP.put(Tools.IMAGES, new ImagesTool(this));
		Manager.TOOL_MAP.put(Tools.CONFIG, new PropertyTool(this));
		Manager.TOOL_MAP.put(Tools.ROOM, new RoomTool(this));
		Manager.TOOL_MAP.put(Tools.DOOR, new DoorTool(this));
		Manager.TOOL_MAP.put(Tools.WEAPON, new MountTool(this));
		Manager.TOOL_MAP.put(Tools.STATION, new StationTool(this));

		// Menu bar
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);

		// File menu
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");

		Menu menuFile = new Menu(mntmFile);
		mntmFile.setMenu(menuFile);

		mntmNewShip = new MenuItem(menuFile, SWT.NONE);
		mntmNewShip.setText("New Ship\t" + Manager.getHotkey(Hotkeys.NEW_SHIP));

		mntmLoadShip = new MenuItem(menuFile, SWT.NONE);
		mntmLoadShip.setText("Load Ship\t" + Manager.getHotkey(Hotkeys.LOAD_SHIP));

		new MenuItem(menuFile, SWT.SEPARATOR);

		mntmSaveShip = new MenuItem(menuFile, SWT.NONE);
		mntmSaveShip.setText("Save Ship\t" + Manager.getHotkey(Hotkeys.SAVE_SHIP));

		mntmSaveShipAs = new MenuItem(menuFile, SWT.NONE);
		mntmSaveShipAs.setText("Save Ship As...");

		new MenuItem(menuFile, SWT.SEPARATOR);

		mntmModMan = new MenuItem(menuFile, SWT.NONE);
		mntmModMan.setText("Mod Management\t" + Manager.getHotkey(Hotkeys.MANAGE_MOD));

		new MenuItem(menuFile, SWT.SEPARATOR);

		mntmCloseShip = new MenuItem(menuFile, SWT.NONE);
		mntmCloseShip.setText("Close Ship\t" + Manager.getHotkey(Hotkeys.CLOSE_SHIP));

		// Edit menu
		MenuItem mntmEdit = new MenuItem(menu, SWT.CASCADE);
		mntmEdit.setText("Edit");

		Menu menuEdit = new Menu(mntmEdit);
		mntmEdit.setMenu(menuEdit);

		mntmUndo = new MenuItem(menuEdit, SWT.NONE);
		mntmUndo.setText("Undo\t" + Manager.getHotkey(Hotkeys.UNDO));

		mntmRedo = new MenuItem(menuEdit, SWT.NONE);
		mntmRedo.setText("Redo\t" + Manager.getHotkey(Hotkeys.REDO));

		new MenuItem(menuEdit, SWT.SEPARATOR);

		mntmResetLinks = new MenuItem(menuEdit, SWT.NONE);
		mntmResetLinks.setText("Reset All Door Links");

		mntmOptimalOffset = new MenuItem(menuEdit, SWT.NONE);
		mntmOptimalOffset.setEnabled(false);
		mntmOptimalOffset.setText("Calculate Optimal Offset");

		new MenuItem(menuEdit, SWT.SEPARATOR);

		mntmDelete = new MenuItem(menuEdit, SWT.NONE);
		mntmDelete.setText("Delete\t" + Manager.getHotkey(Hotkeys.DELETE));

		new MenuItem(menuEdit, SWT.SEPARATOR);

		mntmSettings = new MenuItem(menuEdit, SWT.NONE);
		mntmSettings.setText("Settings\t" + Manager.getHotkey(Hotkeys.SETTINGS));

		// View menu
		MenuItem mntmView = new MenuItem(menu, SWT.CASCADE);
		mntmView.setText("View");

		Menu menuView = new Menu(mntmView);
		mntmView.setMenu(menuView);

		mntmGrid = new MenuItem(menuView, SWT.CHECK);
		mntmGrid.setText("Show Grid\t" + Manager.getHotkey(Hotkeys.TOGGLE_GRID));
		mntmGrid.setSelection(true);

		MenuItem mntmShipComponents = new MenuItem(menuView, SWT.CASCADE);
		mntmShipComponents.setText("Ship Components");

		Menu menuViewShip = new Menu(mntmShipComponents);
		mntmShipComponents.setMenu(menuViewShip);

		mntmShowAnchor = new MenuItem(menuViewShip, SWT.CHECK);
		mntmShowAnchor.setSelection(true);
		mntmShowAnchor.setText("Show Ship Origin\t" + Manager.getHotkey(Hotkeys.SHOW_ANCHOR));

		mntmShowMounts = new MenuItem(menuViewShip, SWT.CHECK);
		mntmShowMounts.setSelection(true);
		mntmShowMounts.setText("Show Mounts\t" + Manager.getHotkey(Hotkeys.SHOW_MOUNTS));

		mntmShowRooms = new MenuItem(menuViewShip, SWT.CHECK);
		mntmShowRooms.setSelection(true);
		mntmShowRooms.setText("Show Rooms\t" + Manager.getHotkey(Hotkeys.SHOW_ROOMS));

		mntmShowDoors = new MenuItem(menuViewShip, SWT.CHECK);
		mntmShowDoors.setText("Show Doors\t" + Manager.getHotkey(Hotkeys.SHOW_DOORS));
		mntmShowDoors.setSelection(true);

		mntmShowStations = new MenuItem(menuViewShip, SWT.CHECK);
		mntmShowStations.setText("Show Stations\t" + Manager.getHotkey(Hotkeys.SHOW_STATIONS));
		mntmShowStations.setSelection(true);

		MenuItem mntmShipImages = new MenuItem(menuView, SWT.CASCADE);
		mntmShipImages.setText("Ship Images");

		Menu menuViewImages = new Menu(mntmShipImages);
		mntmShipImages.setMenu(menuViewImages);

		mntmShowHull = new MenuItem(menuViewImages, SWT.CHECK);
		mntmShowHull.setText("Show Hull\t" + Manager.getHotkey(Hotkeys.SHOW_HULL));
		mntmShowHull.setSelection(true);

		mntmShowFloor = new MenuItem(menuViewImages, SWT.CHECK);
		mntmShowFloor.setText("Show Floor\t" + Manager.getHotkey(Hotkeys.SHOW_FLOOR));
		mntmShowFloor.setSelection(true);

		mntmShowShield = new MenuItem(menuViewImages, SWT.CHECK);
		mntmShowShield.setText("Show Shield\t" + Manager.getHotkey(Hotkeys.SHOW_SHIELD));
		mntmShowShield.setSelection(true);

		// Help menu
		MenuItem mntmHelp = new MenuItem(menu, SWT.CASCADE);
		mntmHelp.setText("Help");

		Menu menuHelp = new Menu(mntmView);
		mntmHelp.setMenu(menuHelp);

		MenuItem mntmUpdate = new MenuItem(menuHelp, SWT.NONE);
		mntmUpdate.setText("Check for Updates");

		MenuItem mntmAbout = new MenuItem(menuHelp, SWT.NONE);
		mntmAbout.setText("About");

		// Main container - contains everything else
		Composite mainContainer = new Composite(shell, SWT.NONE);
		mainContainer.setLayout(new GridLayout(1, false));
		mainContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Tools container - tool bar, tools
		final Composite toolContainer = new Composite(mainContainer, SWT.NONE);
		toolContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		GridLayout gl_toolContainer = new GridLayout(2, false);
		gl_toolContainer.marginHeight = 0;
		gl_toolContainer.marginWidth = 0;
		toolContainer.setLayout(gl_toolContainer);

		// Tool bar widget
		ToolBar toolBar = new ToolBar(toolContainer, SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

		SelectionAdapter toolSelectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.selectTool((Tools) ((ToolItem) e.getSource()).getData());
			}
		};

		// Pointer tool
		tltmPointer = new ToolItem(toolBar, SWT.RADIO);
		tltmPointer.setImage(Cache.checkOutImage(this, "cpath:/assets/pointer.png"));
		tltmPointer.addSelectionListener(toolSelectionAdapter);
		tltmPointer.setData(Tools.POINTER);
		tltmPointer.setToolTipText(String.format("Manipulation Tool (%s)", Manager.getHotkey(Hotkeys.POINTER_TOOL)));
		toolItemMap.put(Tools.POINTER, tltmPointer);

		// Room tool
		tltmCreation = new ToolItem(toolBar, SWT.RADIO);
		tltmCreation.setImage(Cache.checkOutImage(this, "cpath:/assets/wrench.png"));
		tltmCreation.addSelectionListener(toolSelectionAdapter);
		tltmCreation.setData(Tools.CREATOR);
		tltmCreation.setToolTipText(String.format("Layout Creation Tool (%s)", Manager.getHotkey(Hotkeys.CREATE_TOOL)));
		toolItemMap.put(Tools.CREATOR, tltmCreation);

		// Gib tool
		tltmGib = new ToolItem(toolBar, SWT.RADIO);
		tltmGib.setImage(Cache.checkOutImage(this, "cpath:/assets/gib.png"));
		tltmGib.addSelectionListener(toolSelectionAdapter);
		tltmGib.setData(Tools.GIB);
		tltmGib.setToolTipText(String.format("Gib Tool (%s)", Manager.getHotkey(Hotkeys.GIB_TOOL)));
		toolItemMap.put(Tools.GIB, tltmGib);

		// Images button
		tltmImages = new ToolItem(toolBar, SWT.RADIO);
		tltmImages.setImage(Cache.checkOutImage(this, "cpath:/assets/images.png"));
		tltmImages.addSelectionListener(toolSelectionAdapter);
		tltmImages.setData(Tools.IMAGES);
		tltmImages.setToolTipText(String.format("Ship Images (%s)", Manager.getHotkey(Hotkeys.IMAGES_TOOL)));
		toolItemMap.put(Tools.IMAGES, tltmImages);

		// Properties button
		tltmProperties = new ToolItem(toolBar, SWT.RADIO);
		tltmProperties.setImage(Cache.checkOutImage(this, "cpath:/assets/system.png"));
		tltmProperties.addSelectionListener(toolSelectionAdapter);
		tltmProperties.setData(Tools.CONFIG);
		tltmProperties.setToolTipText(String.format("Ship Loadout " + Manager.AMPERSAND + " Properties (%s)", Manager.getHotkey(Hotkeys.PROPERTIES_TOOL)));
		toolItemMap.put(Tools.CONFIG, tltmProperties);

		new ToolItem(toolBar, SWT.SEPARATOR);

		// Manager button
		tltmManager = new ToolItem(toolBar, SWT.PUSH);
		tltmManager.setImage(Cache.checkOutImage(this, "cpath:/assets/overview.png"));
		tltmManager.setToolTipText(String.format("Overview (%s)", Manager.getHotkey(Hotkeys.OVERVIEW_TOOL)));
		tltmManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OverviewWindow window = OverviewWindow.getInstance();
				if (window == null || window.isDisposed())
					window = new OverviewWindow(shell);
				window.open();
			}
		});

		tltmCloak = new ToolItem(toolBar, SWT.CHECK);
		tltmCloak.setImage(Cache.checkOutImage(this, "cpath:/assets/cloak.png"));
		tltmCloak.setToolTipText(String.format("View Cloaked Appearance (%s)", Manager.getHotkey(Hotkeys.CLOAK)));
		tltmCloak.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShipContainer container = Manager.getCurrentShip();
				container.setCloakedAppearance(tltmCloak.getSelection());
				if (!tltmCloak.getSelection() && Manager.getSelected() == container.getImageController(Images.CLOAK))
					Manager.setSelected(null);
			}
		});

		// Info container - mouse position, etc
		Composite infoContainer = new Composite(toolContainer, SWT.NONE);
		infoContainer.setLayout(new GridLayout(1, false));
		infoContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

		// Editor container - canvas, sidebar
		editorContainer = new SashForm(mainContainer, SWT.SMOOTH);
		editorContainer.setSashWidth(7);
		editorContainer.setLayout(new FormLayout());
		editorContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		canvasColor = Cache.checkOutColor(this, canvasRGB);
		canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.setBackground(canvasColor);
		canvas.addPaintListener(LayeredPainter.getInstance());

		sideContainer = new ScrolledComposite(shell, SWT.BORDER | SWT.V_SCROLL);
		sideContainer.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		sideContainer.setAlwaysShowScrollBars(true);
		sideContainer.setExpandHorizontal(true);
		sideContainer.setExpandVertical(true);
		sideContainer.getVerticalBar().setIncrement(15);

		if (Manager.sidebarOnRightSide) {
			canvas.setParent(editorContainer);
			sideContainer.setParent(editorContainer);
			editorContainer.setWeights(new int[] { displaySize.width - SIDEBAR_MIN_WIDTH, SIDEBAR_MIN_WIDTH });
		} else {
			sideContainer.setParent(editorContainer);
			canvas.setParent(editorContainer);
			editorContainer.setWeights(new int[] { SIDEBAR_MIN_WIDTH, displaySize.width - SIDEBAR_MIN_WIDTH });
		}

		shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event e) {
				// display filter is notified every time a key is pressed, regardless of focus
				// only proceed if the main window has focus
				if (!isFocusControl())
					return;

				// update modifier states for use in other places in the application
				if (e.keyCode == SWT.SHIFT || e.stateMask == SWT.SHIFT)
					Manager.modShift = true;
				if (e.keyCode == SWT.ALT || e.stateMask == SWT.ALT)
					Manager.modAlt = true;
				if (e.keyCode == SWT.CTRL || e.stateMask == SWT.CTRL)
					Manager.modCtrl = true;

				handleHotkeys(e);

				// ====== Tool-specific hotkeys

				if (Manager.getSelectedToolId() == Tools.POINTER) {
					// Arrow keys movement
					if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_RIGHT ||
							e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_LEFT) {
						AbstractController selected = Manager.getSelected();
						if (selected != null && !selected.isPinned() && selected.isLocModifiable()) {
							ManipulationToolComposite mtc = (ManipulationToolComposite) getSidebarContent();

							Point p = selected.getPresentedLocation();
							Rectangle oldBounds = null;

							oldBounds = selected.getBounds();

							int nudgeAmount = Manager.modShift && selected.getPresentedFactor() == 1 ? ShipContainer.CELL_SIZE : 1;

							selected.setPresentedLocation(p.x + (e.keyCode == SWT.ARROW_RIGHT ? nudgeAmount : e.keyCode == SWT.ARROW_LEFT ? -nudgeAmount : 0),
									p.y + (e.keyCode == SWT.ARROW_DOWN ? nudgeAmount : e.keyCode == SWT.ARROW_UP ? -nudgeAmount : 0));
							selected.updateFollowOffset();
							Manager.getCurrentShip().updateBoundingArea();
							selected.updateView();

							selected.redraw();
							canvasRedraw(oldBounds);

							mtc.updateData();

							e.doit = false;
						}
					} else if (Manager.getHotkey(Hotkeys.DELETE).passes(e.keyCode) || e.keyCode == SWT.DEL) {
						// Deletion
						mntmDelete.notifyListeners(SWT.Selection, null);
					} else if (Manager.getHotkey(Hotkeys.PIN).passes(e.keyCode)) {
						// Pin
						AbstractController selected = Manager.getSelected();
						if (selected != null) {
							selected.setPinned(!selected.isPinned());
							((ManipulationToolComposite) getSidebarContent()).updateData();
							e.doit = false;
						}
					}
				}
			}
		});

		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (e.keyCode == SWT.SHIFT || e.stateMask == SWT.SHIFT)
					Manager.modShift = false;
				if (e.keyCode == SWT.ALT || e.stateMask == SWT.ALT)
					Manager.modAlt = false;
				if (e.keyCode == SWT.CTRL || e.stateMask == SWT.CTRL)
					Manager.modCtrl = false;

				if (e.keyCode == SWT.SPACE) {
					if (Manager.getSelected() != null)
						e.doit = false;
				}
			}
		});

		shell.getDisplay().addFilter(SWT.MouseEnter, new Listener() {
			@Override
			public void handleEvent(Event e) {
				shellResizing = false;
			}
		});

		sideContainer.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				Grid.getInstance().updateBounds(canvas.getSize().x, canvas.getSize().y);

				ShipContainer ship = Manager.getCurrentShip();
				if (ship != null) {
					ship.updateBoundingArea();
					ship.updateChildBoundingAreas();
					ship.getShipController().updateView();
					ship.getShipController().redraw();
				}

				if (!shellResizing)
					sidebarWidth = Math.max(sideContainer.getSize().x, SIDEBAR_MIN_WIDTH);
			}
		});

		shell.addControlListener(new ControlListener() {
			@Override
			public void controlMoved(ControlEvent e) {
			}

			@Override
			public void controlResized(ControlEvent e) {
				shellResizing = true;

				Grid.getInstance().updateBounds(canvas.getSize().x, canvas.getSize().y);

				ShipContainer ship = Manager.getCurrentShip();
				if (ship != null) {
					ship.updateBoundingArea();
					ship.updateChildBoundingAreas();
					ship.getShipController().updateView();
					ship.getShipController().redraw();
				}

				int width = shell.getClientArea().width;
				int[] weights = editorContainer.getWeights();

				if (width >= sidebarWidth + CANVAS_MIN_SIZE) {
					weights[Manager.sidebarOnRightSide ? 1 : 0] = 1000000 * sidebarWidth / width;
					weights[Manager.sidebarOnRightSide ? 0 : 1] = 1000000 - weights[Manager.sidebarOnRightSide ? 1 : 0];
				} else {
					weights[Manager.sidebarOnRightSide ? 1 : 0] = 1000000 * sidebarWidth / (sidebarWidth + CANVAS_MIN_SIZE);
					weights[Manager.sidebarOnRightSide ? 0 : 1] = 1000000 * CANVAS_MIN_SIZE / (sidebarWidth + CANVAS_MIN_SIZE);
				}

				editorContainer.setWeights(weights);

				if (!shell.getMaximized()) {
					Manager.windowSize.x = shell.getSize().x;
					Manager.windowSize.y = shell.getSize().y;
				}
			}
		});

		mntmNewShip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				NewShipDialog dialog = new NewShipDialog(shell);
				int response = dialog.open();
				if (response != -1)
					Manager.createNewShip(response == 0);
			}
		});

		mntmLoadShip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShipLoaderDialog dialog = new ShipLoaderDialog(shell);
				dialog.open();
			}
		});

		mntmSaveShip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShipContainer container = Manager.getCurrentShip();
				// Prevent Ctrl+S spammers from overstressing the program...
				if (container.isSaved()) {
					log.trace("Ship already saved - aborting.");
					return;
				}

				File temp = saveDestination;
				// Only prompt for save directory if the user hasn't chosen any yet
				if (saveDestination == null) {
					SaveOptionsDialog dialog = new SaveOptionsDialog(shell);
					temp = dialog.open();
				}

				if (temp != null) { // User could've aborted selection, which returns null.
					saveDestination = temp;
					saveShip(container);
				}
			}
		});

		mntmSaveShipAs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShipContainer container = Manager.getCurrentShip();

				// Always prompt for save directory
				SaveOptionsDialog dialog = new SaveOptionsDialog(shell);
				File temp = dialog.open();

				if (temp != null) {
					saveDestination = temp;
					saveShip(container);
				}
			}
		});

		mntmModMan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ModManagementDialog dialog = new ModManagementDialog(shell);
				dialog.open();
			}
		});

		mntmCloseShip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.closeShip();
			}
		});

		mntmUndo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO
				if (Manager.DELETED_LIST.size() > 0)
					Manager.DELETED_LIST.removeLast().restore();
			}
		});

		mntmRedo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO
			}
		});

		mntmResetLinks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().getShipController().getGameObject().resetDoorLinks();
				if (Manager.getSelected() != null) {
					ManipulationToolComposite mtc = (ManipulationToolComposite) getSidebarContent();
					mtc.updateData();
				}
			}
		});

		mntmOptimalOffset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO
			}
		});

		mntmDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AbstractController selected = Manager.getSelected();
				if (selected != null) {
					try {
						Manager.getCurrentShip().delete(selected);
						selected.redraw();

						Manager.setSelected(null);
					} catch (NotDeletableException ex) {
						log.trace("Selected object is not deletable: " + selected.getClass().getSimpleName());
					}
				}
			}
		});

		mntmSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SettingsDialog dialog = new SettingsDialog(shell);
				dialog.open();
			}
		});

		mntmGrid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Grid.getInstance().setVisible(mntmGrid.getSelection());
			}
		});

		mntmShowAnchor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().setAnchorVisible(mntmShowAnchor.getSelection());
			}
		});

		mntmShowMounts.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().setMountsVisible(mntmShowMounts.getSelection());
			}
		});

		mntmShowRooms.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().setRoomsVisible(mntmShowRooms.getSelection());
			}
		});

		mntmShowDoors.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().setDoorsVisible(mntmShowDoors.getSelection());
			}
		});

		mntmShowStations.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().setStationsVisible(mntmShowStations.getSelection());
			}
		});

		mntmShowHull.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().getImageController(Images.HULL).setVisible(mntmShowHull.getSelection());
			}
		});

		mntmShowFloor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().getImageController(Images.FLOOR).setVisible(mntmShowFloor.getSelection());
			}
		});

		mntmShowShield.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Manager.getCurrentShip().getImageController(Images.SHIELD).setVisible(mntmShowShield.getSelection());
			}
		});

		mntmAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StringBuilder buf = new StringBuilder();
				buf.append(Superluminal.APP_NAME + " - a ship editor for FTL: Faster Than Light");
				buf.append("\nVersion " + Superluminal.APP_VERSION);
				buf.append("\n\nCreated by " + Superluminal.APP_AUTHOR);
				buf.append("\n");

				AboutDialog aboutDialog = new AboutDialog(shell);
				aboutDialog.setMessage(buf.toString());
				try {
					aboutDialog.setLink(new URL(Superluminal.APP_FORUM_URL), "Editor's thread at the official FTL forums");
				} catch (MalformedURLException ex) {
				}

				aboutDialog.open();
			}
		});

		mntmUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Superluminal.checkForUpdates(true); // Manually checking for updates
			}
		});

		canvas.addMouseListener(MouseInputDispatcher.getInstance());
		canvas.addMouseMoveListener(MouseInputDispatcher.getInstance());
		canvas.addMouseTrackListener(MouseInputDispatcher.getInstance());

		shell.setMinimumSize(SIDEBAR_MIN_WIDTH + CANVAS_MIN_SIZE, CANVAS_MIN_SIZE + toolContainer.getSize().y * 2);
		Grid.getInstance().updateBounds(canvas.getSize().x, canvas.getSize().y);

		shell.setSize(Manager.windowSize);
		shell.setMaximized(Manager.startMaximised);
		shell.setMinimized(false);

		sideContainer.setFocus();
		enableTools(false);
		enableOptions(false);
		setVisibilityOptions(true);
	}

	public static EditorWindow getInstance() {
		return instance;
	}

	public void open() {
		shell.open();
	}

	private void saveShip(ShipContainer container) {
		if (saveDestination == null)
			throw new IllegalStateException("Save destination must not be null.");

		if (saveDestination.isDirectory()) {
			log.trace("Saving ship to " + saveDestination.getAbsolutePath());

			try {
				ShipSaveUtils.saveShipXML(saveDestination, container);
				log.trace("Ship saved successfully.");
			} catch (Exception ex) {
				log.error("An error occured while saving the ship: ", ex);
				UIUtils.showWarningDialog(shell, null, "An error has occured while saving the ship:\n" + ex.getMessage() + "\n\nCheck log for details.");
			}
		} else {
			log.trace("Saving ship as " + saveDestination.getAbsolutePath());

			try {
				ShipSaveUtils.saveShipFTL(saveDestination, container);
				log.trace("Ship saved successfully.");
			} catch (Exception ex) {
				log.error("An error occured while saving the ship: ", ex);
				UIUtils.showWarningDialog(shell, null, "An error has occured while saving the ship:\n" + ex.getMessage() + "\n\nCheck log for details.");
			}
		}
	}

	/**
	 * Sets the control passed in argument as the content of the sidebar -- this is
	 * what the scrolled area will display, and will scale the scroll against.
	 */
	public void setSidebarContent(Composite c) {
		sideContainer.setContent(c);
		c.pack();
		int height = c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		sideContainer.setMinHeight(height);
	}

	/**
	 * Updates the minimum area of the sidebar, so that the scrollbar will appear when needed.
	 */
	public void updateSidebarScroll() {
		Control c = sideContainer.getContent();
		if (c != null && !c.isDisposed()) {
			int height = c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			sideContainer.setMinHeight(height);
		}
	}

	/**
	 * Do not use this method to dispose the content of the sidebar.
	 * To do this, use {@link #disposeSidebarContent()}.
	 * 
	 * @return the Composite currently held by the sidebar.
	 */
	public Composite getSidebarContent() {
		Control c = sideContainer.getContent();
		if (c instanceof Composite)
			return (Composite) sideContainer.getContent();
		else if (c != null)
			throw new IllegalStateException("Content of the sidebar is not a Composite: " + c.getClass().getSimpleName());
		else
			return null;
	}

	/**
	 * Disposes the content of the sidebar, and sets the content to null.<br>
	 * Prevents the editor from crashing when trying to change the sidebar positioning after closing a ship.
	 */
	public void disposeSidebarContent() {
		Control c = sideContainer.getContent();
		if (c != null)
			c.dispose();
		sideContainer.setContent(null);
	}

	/** @return the sidebar ScrolledComposite itself. */
	public Composite getSidebarWidget() {
		return sideContainer;
	}

	public Shell getShell() {
		return shell;
	}

	/**
	 * Checks whether the point is inside the canvas area -- eg. points non-negative coordinates
	 * with values lesser than or equal to the canvas' dimensions (width and height)
	 * 
	 * @return true if the point (relative to the canvas) is within the canvas bounds, false otherwise
	 */
	public boolean canvasContains(int x, int y) {
		Rectangle bounds = canvas.getBounds();
		bounds.x = 0;
		bounds.y = 0;
		return bounds.contains(x, y);
	}

	/** Redraws the entire canvas area. */
	public void canvasRedraw() {
		canvas.redraw();
	}

	/** Redraws the part of canvas covered by the rectangle. */
	public void canvasRedraw(Rectangle rect) {
		canvas.redraw(rect.x, rect.y, rect.width, rect.height, false);
	}

	/** Redraws the part of canvas covered by the rectangle. */
	public void canvasRedraw(int x, int y, int w, int h) {
		canvas.redraw(x, y, w, h, false);
	}

	/** Only to be used to programmatically select the tool, when the user doesn't directly click on the tool's icon. */
	public void selectTool(Tools tool) {
		if (!toolsEnabled())
			return;

		for (ToolItem it : toolItemMap.values()) {
			if (toolItemMap.containsKey(tool))
				it.setSelection(tool == (Tools) it.getData());
		}
	}

	public void enableTools(boolean enable) {
		tltmPointer.setEnabled(enable);
		tltmCreation.setEnabled(enable);
		tltmGib.setEnabled(enable);
		tltmImages.setEnabled(enable);
		tltmProperties.setEnabled(enable);
		tltmManager.setEnabled(enable);
		tltmCloak.setEnabled(enable);
		if (!enable && tltmCloak.getSelection())
			tltmCloak.setSelection(false);

		sideContainer.getVerticalBar().setEnabled(enable);
	}

	public boolean toolsEnabled() {
		return tltmPointer.isEnabled();
	}

	public void enableOptions(boolean enable) {
		// File
		mntmSaveShip.setEnabled(enable);
		mntmSaveShipAs.setEnabled(enable);
		mntmCloseShip.setEnabled(enable);

		// Edit
		mntmUndo.setEnabled(false); // TODO
		mntmRedo.setEnabled(false); // TODO
		mntmResetLinks.setEnabled(enable);
		mntmOptimalOffset.setEnabled(false); // TODO
		mntmDelete.setEnabled(enable);

		// View
		mntmShowAnchor.setEnabled(enable);
		mntmShowMounts.setEnabled(enable);
		mntmShowRooms.setEnabled(enable);
		mntmShowDoors.setEnabled(enable);
		mntmShowStations.setEnabled(enable);
		mntmShowHull.setEnabled(enable);
		mntmShowFloor.setEnabled(enable);
		mntmShowShield.setEnabled(enable);

		// Disable mod management, only available when a ship is not loaded
		mntmModMan.setEnabled(!enable);
	}

	public boolean optionsEnabled() {
		return mntmSaveShip.isEnabled();
	}

	/**
	 * Toggles all visibility-related options.
	 * 
	 * @param set
	 */
	public void setVisibilityOptions(boolean set) {
		ShipContainer container = Manager.getCurrentShip();

		mntmShowAnchor.setSelection(set);
		mntmShowMounts.setSelection(set);
		mntmShowRooms.setSelection(set);
		mntmShowDoors.setSelection(set);
		mntmShowStations.setSelection(set);
		mntmShowHull.setSelection(set);
		mntmShowFloor.setSelection(set);
		mntmShowShield.setSelection(set);

		if (container != null) {
			container.setAnchorVisible(set);
			container.setMountsVisible(set);
			container.setRoomsVisible(set);
			container.setDoorsVisible(set);
			container.setStationsVisible(set);
		}
	}

	/**
	 * @return true if the editor window controls the focus and should execute hotkey actions.
	 */
	public boolean isFocusControl() {
		boolean result =
				OverviewWindow.getInstance() == null || !OverviewWindow.getInstance().isActive();
		result &= ShipLoaderDialog.getInstance() == null || !ShipLoaderDialog.getInstance().isActive();
		result &= SettingsDialog.getInstance() == null || !SettingsDialog.getInstance().isActive();
		result &= GlowSelectionDialog.getInstance() == null || !GlowSelectionDialog.getInstance().isActive();
		result &= WeaponSelectionDialog.getInstance() == null || !WeaponSelectionDialog.getInstance().isActive();
		result &= DroneSelectionDialog.getInstance() == null || !DroneSelectionDialog.getInstance().isActive();
		result &= ModManagementDialog.getInstance() == null || !ModManagementDialog.getInstance().isActive();
		result &= AboutDialog.getInstance() == null || !AboutDialog.getInstance().isActive();
		result &= AliasDialog.getInstance() == null || !AliasDialog.getInstance().isActive();

		Composite c = (Composite) getSidebarContent();
		if (c != null && !c.isDisposed())
			result &= !c.isFocusControl();

		return result;
	}

	public boolean forceFocus() {
		return canvas.forceFocus();
	}

	public void dispose() {
		Cache.checkInColor(this, canvasRGB);
		Grid.getInstance().dispose();
		Cache.dispose();
		shell.dispose();
	}

	/**
	 * Refreshes the sidebar's layout.
	 */
	public void layoutSidebar() {
		sideContainer.setParent(shell);
		canvas.setParent(shell);
		if (Manager.sidebarOnRightSide) {
			canvas.setParent(editorContainer);
			sideContainer.setParent(editorContainer);
		} else {
			sideContainer.setParent(editorContainer);
			canvas.setParent(editorContainer);
		}

		editorContainer.layout();
	}

	private void handleHotkeys(Event e) {
		// ====== Menu hotkeys
		// File
		if (Manager.getHotkey(Hotkeys.NEW_SHIP).passes(e.keyCode) && mntmNewShip.isEnabled()) {
			mntmNewShip.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.LOAD_SHIP).passes(e.keyCode) && mntmLoadShip.isEnabled()) {
			mntmLoadShip.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SAVE_SHIP).passes(e.keyCode) && mntmSaveShip.isEnabled()) {
			mntmSaveShip.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.MANAGE_MOD).passes(e.keyCode) && mntmModMan.isEnabled()) {
			mntmModMan.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.CLOSE_SHIP).passes(e.keyCode) && mntmCloseShip.isEnabled()) {
			mntmCloseShip.notifyListeners(SWT.Selection, null);
		}

		// Edit
		else if (Manager.getHotkey(Hotkeys.UNDO).passes(e.keyCode) && mntmUndo.isEnabled()) {
			mntmUndo.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.REDO).passes(e.keyCode) && mntmRedo.isEnabled()) {
			mntmRedo.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SETTINGS).passes(e.keyCode) && mntmSettings.isEnabled()) {
			mntmSettings.notifyListeners(SWT.Selection, null);
		}

		// View
		else if (Manager.getHotkey(Hotkeys.TOGGLE_GRID).passes(e.keyCode) && mntmGrid.isEnabled()) {
			mntmGrid.setSelection(!mntmGrid.getSelection());
			mntmGrid.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_ANCHOR).passes(e.keyCode) && mntmShowAnchor.isEnabled()) {
			mntmShowAnchor.setSelection(!mntmShowAnchor.getSelection());
			mntmShowAnchor.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_MOUNTS).passes(e.keyCode) && mntmShowMounts.isEnabled()) {
			mntmShowMounts.setSelection(!mntmShowMounts.getSelection());
			mntmShowMounts.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_ROOMS).passes(e.keyCode) && mntmShowRooms.isEnabled()) {
			mntmShowRooms.setSelection(!mntmShowRooms.getSelection());
			mntmShowRooms.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_DOORS).passes(e.keyCode) && mntmShowDoors.isEnabled()) {
			mntmShowDoors.setSelection(!mntmShowDoors.getSelection());
			mntmShowDoors.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_STATIONS).passes(e.keyCode) && mntmShowStations.isEnabled()) {
			mntmShowStations.setSelection(!mntmShowStations.getSelection());
			mntmShowStations.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_HULL).passes(e.keyCode) && mntmShowHull.isEnabled()) {
			mntmShowHull.setSelection(!mntmShowHull.getSelection());
			mntmShowHull.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_FLOOR).passes(e.keyCode) && mntmShowFloor.isEnabled()) {
			mntmShowFloor.setSelection(!mntmShowFloor.getSelection());
			mntmShowFloor.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.SHOW_SHIELD).passes(e.keyCode) && mntmShowShield.isEnabled()) {
			mntmShowShield.setSelection(!mntmShowShield.getSelection());
			mntmShowShield.notifyListeners(SWT.Selection, null);
		}

		// ====== Tool hotkeys
		else if (Manager.getHotkey(Hotkeys.POINTER_TOOL).passes(e.keyCode) && tltmPointer.isEnabled()) {
			tltmPointer.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.CREATE_TOOL).passes(e.keyCode) && tltmCreation.isEnabled()) {
			tltmCreation.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.GIB_TOOL).passes(e.keyCode) && tltmGib.isEnabled()) {
			tltmGib.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.PROPERTIES_TOOL).passes(e.keyCode) && tltmProperties.isEnabled()) {
			tltmProperties.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.OVERVIEW_TOOL).passes(e.keyCode) && tltmManager.isEnabled()) {
			tltmManager.notifyListeners(SWT.Selection, null);
		} else if (Manager.getHotkey(Hotkeys.CLOAK).passes(e.keyCode) && tltmCloak.isEnabled()) {
			tltmCloak.setSelection(!tltmCloak.getSelection());
			tltmCloak.notifyListeners(SWT.Selection, null);

			// Creation Tool hotkeys
		} else if (Manager.getHotkey(Hotkeys.ROOM_TOOL).passes(e.keyCode) && tltmCreation.isEnabled()) {
			if (!tltmCreation.getSelection())
				tltmCreation.notifyListeners(SWT.Selection, null);
			CreationTool ctool = (CreationTool) Manager.getTool(Tools.CREATOR);
			ctool.selectSubtool(Tools.ROOM);
		} else if (Manager.getHotkey(Hotkeys.DOOR_TOOL).passes(e.keyCode) && tltmCreation.isEnabled()) {
			if (!tltmCreation.getSelection())
				tltmCreation.notifyListeners(SWT.Selection, null);
			CreationTool ctool = (CreationTool) Manager.getTool(Tools.CREATOR);
			ctool.selectSubtool(Tools.DOOR);
		} else if (Manager.getHotkey(Hotkeys.MOUNT_TOOL).passes(e.keyCode) && tltmCreation.isEnabled()) {
			if (!tltmCreation.getSelection())
				tltmCreation.notifyListeners(SWT.Selection, null);
			CreationTool ctool = (CreationTool) Manager.getTool(Tools.CREATOR);
			ctool.selectSubtool(Tools.WEAPON);
		} else if (Manager.getHotkey(Hotkeys.STATION_TOOL).passes(e.keyCode) && tltmCreation.isEnabled()) {
			if (!tltmCreation.getSelection())
				tltmCreation.notifyListeners(SWT.Selection, null);
			CreationTool ctool = (CreationTool) Manager.getTool(Tools.CREATOR);
			ctool.selectSubtool(Tools.STATION);
		}
	}
}
