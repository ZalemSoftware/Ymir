package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerFragment;
import com.wdullaer.materialdatetimepicker.date.DatePickerFragment.OnDateSetListener;

import java.sql.Date;
import java.util.Calendar;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractLabeledFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDateMask;

/**
 * Editor de campo referente a um atributo do tipo <code>data</code> da entidade.
 *
 * @author Thiago Gesser
 */
public final class DateFieldEditor extends AbstractLabeledFieldEditor<Date> implements OnClickListener, OnDateSetListener {

	private static final String DATE_PICKER_FRAGMENT_TAG = "DATE_PICKER_FRAGMENT_TAG";

	private final FragmentManager fragmentManager;
	private final IDateMask mask;

	private TextView textView;

	public DateFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
                           IDateMask mask, FragmentManager fragmentManager) {
		super(fieldName, label, editable, hidden, virtual, help);
		this.mask = mask;
		this.fragmentManager = fragmentManager;
	}

    @Override
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

		//Se o fragmento foi restaurado devido a uma mudança  de orientação, por exemplo, seta o editor novamente nele. Feito aqui ao invés de no construtor por causa do bug descrito em AndroidBugsUtils.applyWorkaroundForInvalidFragmentManagerStateBug.
		Fragment frag = fragmentManager.findFragmentByTag(DATE_PICKER_FRAGMENT_TAG);
		if (frag != null) {
			if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
				DatePickerFragmentV21 datePicker = (DatePickerFragmentV21) frag;
				datePicker.setEditor(this);
			} else {
				DatePickerFragment datePicker = (DatePickerFragment) frag;
				datePicker.setOnDateSetListener(this);
			}
		}
		textView = (TextView) inflater.inflate(R.layout.entity_field_editor_date_time, parent, false);
		textView.setHint(label);
		textView.setEnabled(editable);
		textView.setOnClickListener(this);

        rootView.addView(textView, 1);
		return rootView;
	}

	@Override
	protected void destroyView() {
        super.destroyView();

        textView = null;
	}

    @Override
    protected void refreshView(Date newValue) {
        super.refreshView(newValue);

        if (newValue == null) {
            textView.setText(null);
        } else {
            textView.setText(mask.formatDate(newValue));
        }
    }

    @Override
    public TextView getView() {
        return textView;
    }

    @Override
	protected Date internalLoadValue(IEntityRecord record, String fieldName) {
		return record.getDateValue(fieldName);
	}

	@Override
	protected void internalStoreValue(IEntityRecord record, String fieldName, Date value) {
		record.setDateValue(fieldName, value);
	}

	@Override
	protected Date internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return (Date) bundle.get(key);
	}

	@Override
	protected void internalSaveState(Bundle bundle, String key, Date value) {
        super.internalSaveState(bundle, key, value);

		bundle.putSerializable(key, value);
	}

	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public void onClick(View v) {
		DialogFragment frag;
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			DatePickerFragmentV21 datePicker = new DatePickerFragmentV21();
			datePicker.setEditor(this);
			frag = datePicker;
		} else {
			Calendar calendar = getCalendarValue();
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			frag = DatePickerFragment.newInstance(this, year, month, day);
		}

		frag.show(fragmentManager, DATE_PICKER_FRAGMENT_TAG);
	}

	@Override
	public void onDateSet(DatePickerFragment datePickerDialog, int year, int month, int day) {
		setCalendarValue(year, month, day);
	}


    /*
	 * Métodos / Classes auxiliares
	 */

	private Calendar getCalendarValue() {
		Calendar calendar = Calendar.getInstance();
		Date date = getValue();
		//Se a data do registro for nula, usa a data atual.
		if (date != null) {
			calendar.setTime(date);
		}
		return calendar;
	}

	private void setCalendarValue(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		setValue(new Date(calendar.getTimeInMillis()));
	}


	/**
	 * Fragmento que exibe um Dialog para a seleção de data.<br>
	 * Precisa ser público e estático para poder ser instânciado por fora desta classe (quando o fragmento estiver sendo
	 * recuperado, por exemplo).
	 */
	public static class DatePickerFragmentV21 extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		private DateFieldEditor editor;

		void setEditor(DateFieldEditor editor) {
			this.editor = editor;
		}

		@Override
		@NonNull
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			//Se está recuperando o fragmento, pode criar o Dialog zerado que ele mesmo irá recuperar seu estado.
			if (savedInstanceState != null) {
				return AndroidBugsUtils.applyWorkaroundForBug34833(AndroidBugsUtils.applyWorkaroundForSamsung5DatePickerBug(getActivity()), this, 0, 0, 0);
			}

			Calendar calendar = editor.getCalendarValue();

			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			return AndroidBugsUtils.applyWorkaroundForBug34833(AndroidBugsUtils.applyWorkaroundForSamsung5DatePickerBug(getActivity()), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			editor.setCalendarValue(year, month, day);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			editor = null;
		}
	}
}
