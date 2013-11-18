/**
 * This file is protected by Copyright.
 * Please refer to the COPYRIGHT file distributed with this source distribution.
 *
 * This file is part of REDHAWK IDE.
 *
 * All rights reserved.  This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 *
 */
package gov.redhawk.ui.port.nxmblocks;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Adjust/override FFT settings user entry dialog.
 * @noreference This class is provisional/beta and is subject to API changes
 * @since 4.3
 */
public class FftNxmBlockControls extends Composite {

	private static final Integer[] COMMON_FFT_SIZES = { 1024, 2048, 4096, 8192, 16384 };

	private DataBindingContext dataBindingCtx;
	private final FftNxmBlockSettings settings;
	private Text        transformSizeField;
	private Text        overlapField;
	private Text        numAveragesField;
	private ComboViewer fftType;
	private ComboViewer fftWindow;

	/**
	 * Instantiates a new entry dialog.
	 *
	 * @param parentShell the parent shell
	 */
	public FftNxmBlockControls(final Composite parent, int style, final FftNxmBlockSettings settings,
			final DataBindingContext dataBindingsCtx) {
		super(parent, style);
		this.settings = settings;
		this.dataBindingCtx = dataBindingsCtx;
		createControls(this);
	}

	protected void createControls(final Composite container) {
		final GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);

		// === FFT transform size ===
		final Label transformSizeLabel = new Label(container, SWT.NONE);
		transformSizeLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		transformSizeLabel.setText("Transform Size:");
		this.transformSizeField = new Text(container, SWT.BORDER); // writable
		this.transformSizeField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		this.transformSizeField.setText(Integer.toString(this.settings.getTransformSize()));
		this.transformSizeField.setToolTipText("Performance is best if factorable by 2, 3, 4 and 5.");

		// === percent overlap ===
		final Label percentOverlapLabel = new Label(container, SWT.NONE);
		percentOverlapLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		percentOverlapLabel.setText("Percent Overlap:");
		this.overlapField = new Text(container, SWT.BORDER);
		this.overlapField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		this.overlapField.setText(Integer.toString(this.settings.getOverlap()));

		// === num averages (/nexp) ===
		final Label numAveragesLabel = new Label(container, SWT.NONE);
		numAveragesLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		numAveragesLabel.setText("Num Averages:");
		this.numAveragesField = new Text(container, SWT.BORDER);
		this.numAveragesField.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.numAveragesField.setText(Integer.toString(this.settings.getNumAverages()));
		this.numAveragesField.setToolTipText("Avoid using large value as it will cause highlighted energy to remain longer.");

		// === FFT output type ===
		final Label typeLabel = new Label(container, SWT.NONE);
		typeLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		typeLabel.setText("Output Type:");
		this.fftType = new ComboViewer(container, SWT.READ_ONLY);
		fftType.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		fftType.setContentProvider(new ArrayContentProvider());
		fftType.setLabelProvider(new LabelProvider());
		fftType.setInput(FftNxmBlockSettings.OutputType.values());
		fftType.setSelection(new StructuredSelection(FftNxmBlockSettings.OutputType.PSD));

		// === FFT Window ===
		final Label windowLabel = new Label(container, SWT.NONE);
		windowLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false));
		windowLabel.setText("Window:");

		fftWindow = new ComboViewer(container, SWT.READ_ONLY);
		fftWindow.getCombo().setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		fftWindow.setContentProvider(new ArrayContentProvider());
		fftWindow.setLabelProvider(new LabelProvider());
		fftWindow.setInput(FftNxmBlockSettings.WindowType.values());
		fftWindow.setSelection(new StructuredSelection(FftNxmBlockSettings.WindowType.HANNING));
	}

	protected void addBindings() {
		IObservableValue fsTargetObservableVal = WidgetProperties.text(SWT.Modify).observeDelayed(500, this.transformSizeField);
		IObservableValue fsModelObservableVal  = BeansObservables.observeValue(settings, "transformSize");
		dataBindingCtx.bindValue(fsTargetObservableVal, fsModelObservableVal, createTargetToModelForFftSize(), null);

		IObservableValue srTargetObservableVal = WidgetProperties.text(SWT.Modify).observeDelayed(500, this.overlapField);
		IObservableValue srModelObservableVal  = BeansObservables.observeValue(settings, "overlap");
		dataBindingCtx.bindValue(srTargetObservableVal, srModelObservableVal, createTargetToModelForOverlap(), null);

		IObservableValue numAvgTargetObservableValue = WidgetProperties.text(SWT.Modify).observeDelayed(500, this.numAveragesField);
		IObservableValue numAvgModelObservableValue = BeansObservables.observeValue(settings, "numAverages");
		dataBindingCtx.bindValue(numAvgTargetObservableValue, numAvgModelObservableValue, createTargetToModelForNumAverages(), null);

		IObservableValue windowTargetObservableValue = WidgetProperties.text(SWT.Modify).observeDelayed(500, this.fftWindow.getCombo());
		IObservableValue windowModelObservableValue = BeansObservables.observeValue(settings, "numAverages");
		dataBindingCtx.bindValue(windowTargetObservableValue, windowModelObservableValue, createTargetToModelForWindow(), null);
	}

	private UpdateValueStrategy createTargetToModelForOverlap() {
		UpdateValueStrategy updateValueStrategy = new UpdateValueStrategy();
		updateValueStrategy.setAfterGetValidator(new IValidator() {

			@Override
			public IStatus validate(Object value) {
				if ("default".equalsIgnoreCase((String) value)) {
					return ValidationStatus.ok();
				} else {
					try {
						Double.valueOf((String) value);
						return ValidationStatus.ok();
					} catch (NumberFormatException nfe) {
						return ValidationStatus.error("Overlap percent must a number between 0 - 100.");
					}
				}
			}
		});

		updateValueStrategy.setConverter(new Converter(String.class, Double.class) {

			@Override
			public Object convert(Object fromObject) {
				if ("default".equalsIgnoreCase((String) fromObject)) {
					return -1.0;
				}
				return Double.valueOf((String) fromObject);
			}
		});

		updateValueStrategy.setAfterConvertValidator(new IValidator() {

			@Override
			public IStatus validate(Object value) {
				Double val = (Double) value;
				if (val > 0) {
					return ValidationStatus.ok();
				}
				return ValidationStatus.error("Sample rate must be greater than 0.");
			}
		});
		return updateValueStrategy;
	}

	private UpdateValueStrategy createTargetToModelForFftSize() {
		UpdateValueStrategy updateValueStrategy = new UpdateValueStrategy();
		updateValueStrategy.setAfterGetValidator(new IValidator() {

			@Override
			public IStatus validate(Object value) {
				if ("default".equalsIgnoreCase((String) value)) {
					return ValidationStatus.ok();
				} else {
					try {
						Integer.valueOf((String) value);
						return ValidationStatus.ok();
					} catch (NumberFormatException nfe) {
						return ValidationStatus.error("Frame size must a number greater than 0.");
					}
				}
			}
		});

		updateValueStrategy.setConverter(new Converter(String.class, Integer.class) {

			@Override
			public Object convert(Object fromObject) {
				if ("default".equalsIgnoreCase((String) fromObject)) {
					return -1;
				}
				return Integer.valueOf((String) fromObject);
			}
		});
		updateValueStrategy.setAfterConvertValidator(new IValidator() {

			@Override
			public IStatus validate(Object value) {
				Integer val = (Integer) value;
				if (val > 0) {
					return ValidationStatus.ok();
				}
				return ValidationStatus.error("Frame size must be greater than 0.");
			}
		});
		return updateValueStrategy;
	}

	private UpdateValueStrategy createTargetToModelForWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	private UpdateValueStrategy createTargetToModelForNumAverages() {
		// TODO Auto-generated method stub
		return null;
	}


	static class SelectAllTextComboTextListener implements ISelectionChangedListener {
		private final Combo combo;

		SelectAllTextComboTextListener(Combo combo) {
			this.combo = combo;
		}

		public void selectionChanged(final SelectionChangedEvent event) {
			final String text = this.combo.getText();
			final int textLen = (text == null) ? 0 : text.length();
			this.combo.setSelection(new Point(0, textLen)); // select text from combo selection
		}
	}

}