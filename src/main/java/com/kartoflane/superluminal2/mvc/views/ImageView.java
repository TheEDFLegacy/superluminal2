package com.kartoflane.superluminal2.mvc.views;

import org.eclipse.swt.events.PaintEvent;

public class ImageView extends BaseView {

	@Override
	public void paintControl(PaintEvent e) {
		if (alpha > 0) {
			paintImage(e, image, cachedImageBounds, alpha);
			paintBorder(e, borderColor, borderThickness, 255);
		}
	}

	@Override
	public void updateView() {
		if (controller.isSelected()) {
			setBorderColor(controller.isPinned() ? PIN_RGB : SELECT_RGB);
			setBackgroundColor(null);
			setBorderThickness(2);
		} else if (controller.isHighlighted()) {
			setBorderColor(HIGHLIGHT_RGB);
			setBackgroundColor(null);
			setBorderThickness(3);
		} else {
			setBorderColor(null);
			setBackgroundColor(null);
			setBorderThickness(2);
		}
	}
}
