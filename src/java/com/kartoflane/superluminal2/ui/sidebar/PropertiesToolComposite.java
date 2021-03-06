package com.kartoflane.superluminal2.ui.sidebar;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.kartoflane.superluminal2.components.enums.PlayerShipBlueprints;
import com.kartoflane.superluminal2.components.enums.Races;
import com.kartoflane.superluminal2.core.Database;
import com.kartoflane.superluminal2.core.Manager;
import com.kartoflane.superluminal2.ftl.AugmentObject;
import com.kartoflane.superluminal2.ftl.DroneList;
import com.kartoflane.superluminal2.ftl.DroneObject;
import com.kartoflane.superluminal2.ftl.ShipObject;
import com.kartoflane.superluminal2.ftl.WeaponList;
import com.kartoflane.superluminal2.ftl.WeaponObject;
import com.kartoflane.superluminal2.mvc.controllers.ShipController;
import com.kartoflane.superluminal2.ui.AugmentSelectionDialog;
import com.kartoflane.superluminal2.ui.CrewMenu;
import com.kartoflane.superluminal2.ui.DroneSelectionDialog;
import com.kartoflane.superluminal2.ui.EditorWindow;
import com.kartoflane.superluminal2.ui.ShipContainer;
import com.kartoflane.superluminal2.ui.WeaponSelectionDialog;
import com.kartoflane.superluminal2.utils.UIUtils;

public class PropertiesToolComposite extends Composite {

	private static int selectedTab = 0;
	private ShipContainer container;

	private Text txtName;
	private Text txtClass;
	private Text txtDesc;
	private Spinner spHealth;
	private Spinner spPower;
	private TabItem tbtmCrew;
	private Composite compCrew;
	private Label lblDesc;
	private Spinner spMinSec;
	private Spinner spMaxSec;
	private TabFolder tabFolder;
	private ArrayList<Button> btnWeapons = new ArrayList<Button>();
	private ArrayList<Button> btnDrones = new ArrayList<Button>();
	private ArrayList<Button> btnAugments = new ArrayList<Button>();
	private ArrayList<Button> btnCrewMembers = new ArrayList<Button>();
	private Spinner spMissiles;
	private Spinner spWeaponSlots;
	private Button btnWeaponList;
	private Button btnDroneList;
	private Group grpWeapons;
	private Composite compArm;
	private Group grpDrones;
	private Spinner spDrones;
	private Spinner spDroneSlots;
	private Label lblBlueprint;
	private Text txtBlueprint;
	private Combo cmbShips;
	private Text txtLayout;
	private Text txtImage;
	private Group grpAugments;
	private TabItem tbtmArtillery;
	private Composite compArtillery;
	private Label label;
	private CrewMenu crewMenu;

	public PropertiesToolComposite(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));

		container = Manager.getCurrentShip();
		final ShipObject ship = container.getShipController().getGameObject();
		final boolean[] created = { false };

		Label lblPropertiesTool = new Label(this, SWT.NONE);
		lblPropertiesTool.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false, 1, 1));
		lblPropertiesTool.setText("Ship Loadout " + Manager.AMPERSAND + " Properties");

		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (created[0])
					selectedTab = tabFolder.getSelectionIndex();
			}
		});

		/*
		 * =========================================================================
		 * XXX: General tab
		 * =========================================================================
		 */

		TabItem tbtmGeneral = new TabItem(tabFolder, SWT.NONE);
		tbtmGeneral.setText("General");

		Composite compGeneral = new Composite(tabFolder, SWT.NONE);
		tbtmGeneral.setControl(compGeneral);
		compGeneral.setLayout(new GridLayout(2, false));

		lblBlueprint = new Label(compGeneral, SWT.NONE);
		lblBlueprint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblBlueprint.setText("Blueprint:");

		if (ship.isPlayerShip()) {
			lblBlueprint.setText("Replaced Ship:");

			cmbShips = new Combo(compGeneral, SWT.READ_ONLY);
			cmbShips.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			for (PlayerShipBlueprints blueprint : PlayerShipBlueprints.values()) {
				cmbShips.add(blueprint.toString());
			}

			cmbShips.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ship.setBlueprintName(cmbShips.getText());
				}
			});

			Label lblName = new Label(compGeneral, SWT.NONE);
			lblName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
			lblName.setText("Name:");

			txtName = new Text(compGeneral, SWT.BORDER);
			txtName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			txtName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ship.setShipName(txtName.getText());
				}
			});
		} else {
			lblBlueprint.setText("Blueprint Name:");

			txtBlueprint = new Text(compGeneral, SWT.BORDER);
			txtBlueprint.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

			txtBlueprint.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					ship.setBlueprintName(txtBlueprint.getText());
				}
			});
		}

		Label lblClass = new Label(compGeneral, SWT.NONE);
		lblClass.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblClass.setText("Class:");

		txtClass = new Text(compGeneral, SWT.BORDER);
		txtClass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		txtClass.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ship.setShipClass(txtClass.getText());
			}
		});

		if (ship.isPlayerShip()) {
			lblDesc = new Label(compGeneral, SWT.NONE);
			lblDesc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			lblDesc.setText("Description: (0/255)");

			txtDesc = new Text(compGeneral, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			GridData gd_txtDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			gd_txtDesc.heightHint = 80;
			txtDesc.setLayoutData(gd_txtDesc);

			txtDesc.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					lblDesc.setText("Description: (" + txtDesc.getText().length() + "/255)");
					ship.setShipDescription(txtDesc.getText());
				}
			});
		}

		Label lblLayout = new Label(compGeneral, SWT.NONE);
		lblLayout.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblLayout.setText("Layout Filename:");

		txtLayout = new Text(compGeneral, SWT.BORDER);
		txtLayout.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		txtLayout.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ship.setLayout(txtLayout.getText());
			}
		});

		Label lblImageNamespace = new Label(compGeneral, SWT.NONE);
		lblImageNamespace.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblImageNamespace.setText("Image Namespace:");

		txtImage = new Text(compGeneral, SWT.BORDER);
		txtImage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		txtImage.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				ship.setImageNamespace(txtImage.getText());
			}
		});

		Label lblHealth = new Label(compGeneral, SWT.NONE);
		lblHealth.setText("Hull Health:");

		spHealth = new Spinner(compGeneral, SWT.BORDER);
		spHealth.setTextLimit(3);
		spHealth.setMinimum(0);
		spHealth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		spHealth.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ship.setHealth(spHealth.getSelection());
			}
		});

		Label lblReactor = new Label(compGeneral, SWT.NONE);
		lblReactor.setText("Reactor Power:");

		spPower = new Spinner(compGeneral, SWT.BORDER);
		spPower.setTextLimit(3);
		spPower.setMinimum(0);
		spPower.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		spPower.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ship.setPower(spPower.getSelection());
			}
		});

		if (!ship.isPlayerShip()) {
			Label lblMinSector = new Label(compGeneral, SWT.NONE);
			lblMinSector.setText("Min Sector:");

			spMinSec = new Spinner(compGeneral, SWT.BORDER);
			spMinSec.setTextLimit(1);
			spMinSec.setMaximum(8);
			spMinSec.setMinimum(1);
			spMinSec.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

			Label lblMaxSector = new Label(compGeneral, SWT.NONE);
			lblMaxSector.setText("Max Sector:");

			spMaxSec = new Spinner(compGeneral, SWT.BORDER);
			spMaxSec.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			spMaxSec.setTextLimit(1);
			spMaxSec.setMaximum(8);
			spMaxSec.setMinimum(1);
		}

		/*
		 * =========================================================================
		 * XXX: Armaments tab
		 * =========================================================================
		 */

		TabItem tbtmArmaments = new TabItem(tabFolder, 0);
		tbtmArmaments.setText("Armaments");

		compArm = new Composite(tabFolder, SWT.NONE);
		tbtmArmaments.setControl(compArm);
		compArm.setLayout(new GridLayout(1, false));

		grpWeapons = new Group(compArm, SWT.NONE);
		grpWeapons.setLayout(new GridLayout(2, false));
		grpWeapons.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpWeapons.setText("Weapons");

		Label lblMissiles = new Label(grpWeapons, SWT.NONE);
		lblMissiles.setText("Starting Missiles");

		spMissiles = new Spinner(grpWeapons, SWT.BORDER);
		spMissiles.setMaximum(999);
		spMissiles.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

		Label lblWeaponSlots = new Label(grpWeapons, SWT.NONE);
		lblWeaponSlots.setText("Slots");

		spWeaponSlots = new Spinner(grpWeapons, SWT.BORDER);
		spWeaponSlots.setMaximum(8);
		spWeaponSlots.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		spWeaponSlots.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ship.isPlayerShip()) {
					if (spWeaponSlots.getSelection() > 4 && ship.getWeaponSlots() <= 4 && !Manager.shownSlotWarning) {
						StringBuilder buf = new StringBuilder();
						buf.append("Giving a ship more than 4 weapon slots will cause ingame UI to break.\n");
						buf.append("While it's possible for a ship to have any number of weapon slots, this\n");
						buf.append("option should only be used by experienced modders.");
						UIUtils.showWarningDialog(EditorWindow.getInstance().getShell(), null, buf.toString());
						Manager.shownSlotWarning = true;
					}

					int slots = spWeaponSlots.getSelection();
					ship.setWeaponSlots(slots);
					clearWeaponSlots();
					createWeaponSlots(slots);
					updateData();
					container.updateMounts();

					EditorWindow.getInstance().updateSidebarScroll();
				}
			}
		});

		if (ship.isPlayerShip()) {
			createWeaponSlots(ship.getWeaponSlots());
		} else {
			btnWeaponList = new Button(grpWeapons, SWT.NONE);
			btnWeaponList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			btnWeaponList.setText("<weapon list>");

			btnWeaponList.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					WeaponList current = ship.getWeaponList();
					WeaponSelectionDialog dialog = new WeaponSelectionDialog(EditorWindow.getInstance().getShell());
					WeaponList neu = dialog.open(current);

					if (neu != null) {
						ship.setWeaponList(neu);
						updateData();
					}
				}
			});
		}

		grpDrones = new Group(compArm, SWT.NONE);
		grpDrones.setLayout(new GridLayout(2, false));
		grpDrones.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpDrones.setText("Drones");

		Label lblDrones = new Label(grpDrones, SWT.NONE);
		lblDrones.setText("Starting Drone Parts");

		spDrones = new Spinner(grpDrones, SWT.BORDER);
		spDrones.setMaximum(999);
		spDrones.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));

		Label lblDroneSlots = new Label(grpDrones, SWT.NONE);
		lblDroneSlots.setText("Slots");

		spDroneSlots = new Spinner(grpDrones, SWT.BORDER);
		spDroneSlots.setMaximum(8);
		spDroneSlots.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		spDroneSlots.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ship.isPlayerShip()) {
					if (spDroneSlots.getSelection() > 4 && ship.getDroneSlots() <= 4 && !Manager.shownSlotWarning) {
						StringBuilder buf = new StringBuilder();
						buf.append("Giving a ship more than 4 drone slots will cause ingame UI to break.\n");
						buf.append("While it's possible for a ship to have any number of drone slots, this\n");
						buf.append("option should only be used by experienced modders.");
						UIUtils.showWarningDialog(EditorWindow.getInstance().getShell(), null, buf.toString());
						Manager.shownSlotWarning = true;
					}

					int slots = spDroneSlots.getSelection();
					ship.setDroneSlots(slots);
					clearDroneSlots();
					createDroneSlots(slots);
					updateData();

					EditorWindow.getInstance().updateSidebarScroll();
				}
			}
		});

		if (ship.isPlayerShip()) {
			createDroneSlots(ship.getDroneSlots());
		} else {
			btnDroneList = new Button(grpDrones, SWT.NONE);
			btnDroneList.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			btnDroneList.setText("<drone list>");

			btnDroneList.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					DroneList current = ship.getDroneList();
					DroneSelectionDialog dialog = new DroneSelectionDialog(EditorWindow.getInstance().getShell());
					DroneList neu = dialog.open(current);

					if (neu != null) {
						ship.setDroneList(neu);
						updateData();
					}
				}
			});
		}

		grpAugments = new Group(compArm, SWT.NONE);
		grpAugments.setLayout(new GridLayout(1, false));
		grpAugments.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpAugments.setText("Augments");

		SelectionAdapter augmentListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = btnAugments.indexOf(e.getSource());

				if (i != -1) {
					ShipObject ship = container.getShipController().getGameObject();
					AugmentObject current = ship.getAugments()[i];

					AugmentSelectionDialog dialog = new AugmentSelectionDialog(EditorWindow.getInstance().getShell());
					AugmentObject neu = dialog.open(current);

					if (neu != null) {
						// If the augment is the default dummy, then replace the first occurence of
						// the dummy aug, so that there are no gaps
						if (current == Database.DEFAULT_AUGMENT_OBJ)
							ship.changeAugment(current, neu);
						else
							ship.changeAugment(i, neu);
						updateData();
					}
				}
			}
		};

		for (int i = 0; i < 3; i++) {
			Button btn = new Button(grpAugments, SWT.NONE);
			btn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			btn.setText("<augment slot>");
			btn.addSelectionListener(augmentListener);
			btnAugments.add(btn);
		}

		/*
		 * =========================================================================
		 * XXX: Artillery tab
		 * =========================================================================
		 */

		tbtmArtillery = new TabItem(tabFolder, SWT.NONE);
		tbtmArtillery.setText("Artillery");

		compArtillery = new Composite(tabFolder, SWT.NONE);
		tbtmArtillery.setControl(compArtillery);
		compArtillery.setLayout(new GridLayout(1, false));

		label = new Label(compArtillery, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		label.setText("(not yet implemented)");

		/*
		 * =========================================================================
		 * XXX: Crew tab
		 * =========================================================================
		 */

		tbtmCrew = new TabItem(tabFolder, SWT.NONE);
		tbtmCrew.setText("Crew");

		compCrew = new Composite(tabFolder, SWT.NONE);
		tbtmCrew.setControl(compCrew);
		compCrew.setLayout(new GridLayout(1, false));

		if (ship.isPlayerShip()) {
			SelectionAdapter listener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int i = btnCrewMembers.indexOf(e.getSource());

					if (i != -1) {
						ShipObject ship = container.getShipController().getGameObject();
						Races current = ship.getCrew()[i];

						crewMenu = new CrewMenu(compCrew);
						Races neu = crewMenu.open();

						if (neu != null) {
							if (current == Races.NO_CREW)
								ship.changeCrew(current, neu);
							else
								ship.changeCrew(i, neu);
							updateData();
						}
					}
				}
			};

			for (int i = 0; i < 8; i++) {
				Button btn = new Button(compCrew, SWT.NONE);
				btn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				btn.setText("<crew slot>");
				btn.addSelectionListener(listener);
				btnCrewMembers.add(btn);
			}
		} else {
		}
		Label lblNYI = new Label(compCrew, SWT.NONE);
		lblNYI.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		lblNYI.setText("(not yet implemented)");

		pack();
		updateData();
		created[0] = true;
		tabFolder.setSelection(selectedTab);
	}

	public void updateData() {
		ShipController controller = container.getShipController();
		ShipObject ship = controller.getGameObject();

		String content = null;
		// General tab

		content = ship.getShipClass();
		txtClass.setText(content == null ? "Ship Class" : content);

		content = ship.getLayout();
		txtLayout.setText(content == null ? "myship" : content);

		content = ship.getImageNamespace();
		txtImage.setText(content == null ? "myship" : content);

		spHealth.setSelection(ship.getHealth());
		spPower.setSelection(ship.getPower());

		if (ship.isPlayerShip()) {
			int index = cmbShips.indexOf(ship.getBlueprintName());
			cmbShips.select(index == -1 ? 0 : index);

			content = ship.getShipName();
			txtName.setText(ship.isPlayerShip() && content == null ? "The Nameless One" : content);

			content = ship.getShipDescription();
			txtDesc.setText(ship.isPlayerShip() && content == null ? "" : content);
			lblDesc.setText("Description: (" + txtDesc.getText().length() + "/255)");
		} else {
			txtBlueprint.setText(ship.getBlueprintName());

			spMinSec.setSelection(ship.getMinSector());
			spMaxSec.setSelection(ship.getMaxSector());
			spMinSec.setEnabled(!ship.isPlayerShip());
			spMaxSec.setEnabled(!ship.isPlayerShip());
		}

		// Armaments tab

		spMissiles.setSelection(ship.getMissilesAmount());
		spWeaponSlots.setSelection(ship.getWeaponSlots());
		spDrones.setSelection(ship.getDronePartsAmount());
		spDroneSlots.setSelection(ship.getDroneSlots());

		if (ship.isPlayerShip()) {
			int count = 0;
			for (WeaponObject weapon : ship.getWeapons()) {
				if (count < ship.getWeaponSlots()) {
					btnWeapons.get(count).setText(weapon.toString());
					count++;
				}
			}

			count = 0;
			for (DroneObject drone : ship.getDrones()) {
				if (count < ship.getDroneSlots()) {
					btnDrones.get(count).setText(drone.toString());
					count++;
				}
			}
		} else {
			WeaponList wList = ship.getWeaponList();
			btnWeaponList.setText(wList.getBlueprintName());

			DroneList dList = ship.getDroneList();
			btnDroneList.setText(dList.getBlueprintName());
		}

		int count = 0;
		for (AugmentObject augment : ship.getAugments()) {
			btnAugments.get(count).setText(augment.toString());
			count++;
		}

		// Crew tab

		if (ship.isPlayerShip()) {
			count = 0;
			for (Races race : ship.getCrew()) {
				btnCrewMembers.get(count).setText(race.toString());
				btnCrewMembers.get(count).setEnabled(false);
				count++;
			}
		} else {
			// TODO
		}
	}

	private void clearWeaponSlots() {
		for (Button b : btnWeapons)
			b.dispose();
		btnWeapons.clear();
		compArm.layout();
	}

	private void clearDroneSlots() {
		for (Button b : btnDrones)
			b.dispose();
		btnDrones.clear();
		compArm.layout();
	}

	private void createWeaponSlots(int n) {
		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = btnWeapons.indexOf(e.getSource());

				if (i != -1) {
					ShipObject ship = container.getShipController().getGameObject();
					WeaponObject current = ship.getWeapons()[i];

					WeaponSelectionDialog dialog = new WeaponSelectionDialog(EditorWindow.getInstance().getShell());
					WeaponObject neu = dialog.open(current);

					if (neu != null) {
						// If the weapon is the default dummy, then replace the first occurence of
						// the dummy weapon, so that there are no gaps
						if (current == Database.DEFAULT_WEAPON_OBJ)
							container.changeWeapon(current, neu);
						else
							container.changeWeapon(i, neu);
						updateData();
					}
				}
			}
		};

		for (int i = 0; i < n; i++) {
			Button b = new Button(grpWeapons, SWT.NONE);
			b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			b.setText("<weapon slot>");
			b.addSelectionListener(listener);
			btnWeapons.add(b);
		}

		compArm.layout();
	}

	private void createDroneSlots(int n) {
		SelectionAdapter listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = btnDrones.indexOf(e.getSource());

				if (i != -1) {
					ShipObject ship = container.getShipController().getGameObject();
					DroneObject current = ship.getDrones()[i];

					DroneSelectionDialog dialog = new DroneSelectionDialog(EditorWindow.getInstance().getShell());
					DroneObject neu = dialog.open(current);

					if (neu != null) {
						// If the drone is the default dummy, then replace the first occurence of
						// the dummy drone, so that there are no gaps
						if (current == Database.DEFAULT_DRONE_OBJ)
							ship.changeDrone(current, neu);
						else
							ship.changeDrone(i, neu);
						updateData();
					}
				}
			}
		};

		for (int i = 0; i < n; i++) {
			Button b = new Button(grpDrones, SWT.NONE);
			b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			b.setText("<drone slot>");
			b.addSelectionListener(listener);
			btnDrones.add(b);
		}

		compArm.layout();
	}

	@Override
	public boolean isFocusControl() {
		boolean result = false;
		result |= txtClass.isFocusControl() || spHealth.isFocusControl() || spPower.isFocusControl() ||
				spMissiles.isFocusControl() || spWeaponSlots.isFocusControl() ||
				spDrones.isFocusControl() || spDroneSlots.isFocusControl() ||
				txtLayout.isFocusControl() || txtImage.isFocusControl();
		if (container.getShipController().isPlayerShip()) {
			result |= txtName.isFocusControl() || txtDesc.isFocusControl();
		} else {
			result |= spMinSec.isFocusControl() || spMaxSec.isFocusControl();
		}
		return result;
	}
}
