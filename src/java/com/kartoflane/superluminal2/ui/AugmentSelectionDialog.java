package com.kartoflane.superluminal2.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.kartoflane.superluminal2.Superluminal;
import com.kartoflane.superluminal2.core.Database;
import com.kartoflane.superluminal2.ftl.AugmentObject;

public class AugmentSelectionDialog {

	private static AugmentSelectionDialog instance = null;

	private static final int defaultBlueTabWidth = 200;
	private static final int defaultNameTabWidth = 150;
	private static final int minTreeWidth = defaultBlueTabWidth + defaultNameTabWidth + 5;
	private static final int defaultDataWidth = 200;

	private AugmentObject result = null;
	private int response = SWT.NO;

	private boolean sortByBlueprint = true;

	private Shell shell = null;
	private Text txtDesc;
	private Button btnCancel;
	private Button btnConfirm;
	private Tree tree;
	private TreeColumn trclmnBlueprint;
	private TreeColumn trclmnName;

	public AugmentSelectionDialog(Shell parent) {
		if (instance != null)
			throw new IllegalStateException("Previous instance has not been disposed!");
		instance = this;

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText(Superluminal.APP_NAME + " - Augment Selection");
		shell.setLayout(new GridLayout(1, false));

		SashForm sashForm = new SashForm(shell, SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		tree = new Tree(sashForm, SWT.BORDER | SWT.FULL_SELECTION);
		tree.setHeaderVisible(true);

		trclmnBlueprint = new TreeColumn(tree, SWT.LEFT);
		trclmnBlueprint.setWidth(defaultBlueTabWidth);
		trclmnBlueprint.setText("Blueprint");

		trclmnName = new TreeColumn(tree, SWT.RIGHT);
		trclmnName.setWidth(defaultNameTabWidth);
		trclmnName.setText("Name");

		ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite compData = new Composite(scrolledComposite, SWT.NONE);
		GridLayout gl_compData = new GridLayout(1, false);
		gl_compData.marginHeight = 0;
		gl_compData.marginWidth = 0;
		compData.setLayout(gl_compData);

		Label lblDescription = new Label(compData, SWT.NONE);
		lblDescription.setText("Description:");

		txtDesc = new Text(compData, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_txtDesc = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_txtDesc.heightHint = 60;
		txtDesc.setLayoutData(gd_txtDesc);
		scrolledComposite.setContent(compData);
		scrolledComposite.setMinSize(compData.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm.setWeights(new int[] { minTreeWidth, defaultDataWidth });

		Composite compButtons = new Composite(shell, SWT.NONE);
		GridLayout gl_compButtons = new GridLayout(2, false);
		gl_compButtons.marginWidth = 0;
		gl_compButtons.marginHeight = 0;
		compButtons.setLayout(gl_compButtons);
		compButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		btnConfirm = new Button(compButtons, SWT.NONE);
		GridData gd_btnConfirm = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_btnConfirm.widthHint = 80;
		btnConfirm.setLayoutData(gd_btnConfirm);
		btnConfirm.setText("Confirm");

		btnCancel = new Button(compButtons, SWT.NONE);
		GridData gd_btnCancel = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_btnCancel.widthHint = 80;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setText("Cancel");

		tree.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 1 && tree.getSelectionCount() != 0) {
					TreeItem selectedItem = tree.getSelection()[0];
					if (selectedItem.getItemCount() == 0 && btnConfirm.isEnabled())
						btnConfirm.notifyListeners(SWT.Selection, null);
					else if (selectedItem.getBounds().contains(e.x, e.y))
						selectedItem.setExpanded(!selectedItem.getExpanded());
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tree.getSelectionCount() != 0) {
					TreeItem selectedItem = tree.getSelection()[0];
					Object o = selectedItem.getData();

					if (o instanceof AugmentObject) {
						result = (AugmentObject) o;
						btnConfirm.setEnabled(result != null);
					} else {
						result = null;
						btnConfirm.setEnabled(false);
					}
				} else {
					result = null;
					btnConfirm.setEnabled(false);
				}
				updateData();
			}
		});

		trclmnBlueprint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Sort by blueprint name
				if (!sortByBlueprint) {
					sortByBlueprint = true;
					updateTree();
				}
			}
		});

		trclmnName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Sort by title
				if (sortByBlueprint) {
					sortByBlueprint = false;
					updateTree();
				}
			}
		});

		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				response = SWT.NO;
				dispose();
			}
		});

		btnConfirm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				response = SWT.YES;
				dispose();
			}
		});

		shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event e) {
				btnCancel.notifyListeners(SWT.Selection, null);
				e.doit = false;
			}
		});

		shell.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && tree.getSelectionCount() != 0) {
					TreeItem selectedItem = tree.getSelection()[0];
					if (selectedItem.getItemCount() == 0 && btnConfirm.isEnabled())
						btnConfirm.notifyListeners(SWT.Selection, null);
					else
						selectedItem.setExpanded(!selectedItem.getExpanded());
				}
			}
		});

		ControlAdapter resizer = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				final int BORDER_OFFSET = 5;
				trclmnName.setWidth(tree.getClientArea().width - trclmnBlueprint.getWidth() - BORDER_OFFSET);
			}
		};
		tree.addControlListener(resizer);
		trclmnBlueprint.addControlListener(resizer);

		shell.setMinimumSize(minTreeWidth + defaultDataWidth, 300);
		shell.pack();
		Point size = shell.getSize();
		shell.setSize(size.x + 5, size.y);
		Point parSize = parent.getSize();
		Point parLoc = parent.getLocation();
		shell.setLocation(parLoc.x + parSize.x / 3 - size.x / 2, parLoc.y + parSize.y / 3 - size.y / 2);
	}

	private void open() {
		response = SWT.NO;

		updateTree();

		shell.open();

		updateData();

		Display display = Display.getCurrent();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public AugmentObject open(AugmentObject current) {
		result = current;

		open();

		if (response == SWT.YES)
			return result;
		else
			return null;
	}

	private void updateTree() {
		for (TreeItem trtm : tree.getItems())
			trtm.dispose();

		TreeItem trtm = new TreeItem(tree, SWT.NONE);
		trtm.setText("No Augment");
		trtm.setData(Database.DEFAULT_AUGMENT_OBJ);

		TreeItem selection = null;

		AugmentIterator it = new AugmentIterator(Database.getInstance().getAugments(), sortByBlueprint);
		for (it.first(); it.hasNext(); it.next()) {
			AugmentObject Augment = it.current();

			trtm = new TreeItem(tree, SWT.NONE);
			trtm.setText(0, Augment.getBlueprintName());
			trtm.setText(1, Augment.getTitle());
			trtm.setData(Augment);

			if (result == Augment)
				selection = trtm;
		}

		tree.layout();

		if (selection != null)
			tree.select(selection);
		else
			tree.select(tree.getItem(0));
	}

	private void updateData() {
		txtDesc.setText(result == null ? "" : result.getDescription());
	}

	public static AugmentSelectionDialog getInstance() {
		return instance;
	}

	public void dispose() {
		shell.dispose();
		instance = null;
	}

	public boolean isActive() {
		return !shell.isDisposed() && shell.isVisible();
	}

	private class AugmentIterator implements Iterator<AugmentObject> {
		private final ArrayList<AugmentObject> list;
		private final AugmentComparator comparator;

		private AugmentObject current = null;

		public AugmentIterator(ArrayList<AugmentObject> list, boolean byBlueprint) {
			comparator = new AugmentComparator(byBlueprint);
			this.list = list;
		}

		private AugmentObject getSmallestElement() {
			AugmentObject result = null;
			for (AugmentObject Augment : list) {
				if (result == null || comparator.compare(Augment, result) < 0)
					result = Augment;
			}

			return result;
		}

		public void first() {
			current = getSmallestElement();
		}

		public AugmentObject current() {
			return current;
		}

		@Override
		public boolean hasNext() {
			return !list.isEmpty();
		}

		@Override
		public AugmentObject next() {
			remove();
			current = getSmallestElement();
			return current;
		}

		@Override
		public void remove() {
			list.remove(current);
		}
	}

	private class AugmentComparator implements Comparator<AugmentObject> {
		private final boolean byBlueprint;

		public AugmentComparator(boolean byBlueprint) {
			this.byBlueprint = byBlueprint;
		}

		@Override
		public int compare(AugmentObject o1, AugmentObject o2) {
			if (byBlueprint) {
				// Just compare the two blueprints together for alphanumerical ordering
				return o1.getBlueprintName().compareTo(o2.getBlueprintName());
			} else {
				int result = o1.getTitle().compareTo(o2.getTitle());
				if (result == 0) // If titles are the same, fall back to sorting by blueprint
					result = o1.getBlueprintName().compareTo(o2.getBlueprintName());
				return result;
			}
		}
	}
}
