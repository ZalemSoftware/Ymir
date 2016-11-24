package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.widget.TextView;
import android.widget.TimePicker;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerFragment;
import com.wdullaer.materialdatetimepicker.time.TimePickerFragment.OnTimeSetListener;

import java.sql.Time;
import java.util.Calendar;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractLabeledFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeMask;

/**
 * Editor de campo referente a um atributo do tipo <code>hora</code> da entidade.
 *
 * @author Thiago Gesser
 */
public final class TimeFieldEditor extends AbstractLabeledFieldEditor<Time> implements OnClickListener, OnTimeSetListener {

	private static final String TIME_PICKER_FRAGMENT_TAG = "TIME_PICKER_FRAGMENT_TAG";
	
	private final FragmentManager fragmentManager;
	private final ITimeMask mask;
	
	private TextView textView;

	public TimeFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
                           ITimeMask mask, FragmentManager fragmentManager) {
		super(fieldName, label, editable, hidden, virtual, help);
		this.mask = mask;
		this.fragmentManager = fragmentManager;
	}

    @Override
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

		//Se o fragmento foi restaurado devido a uma mudança  de orientação, por exemplo, seta o editor novamente nele. Feito aqui ao invés de no construtor por causa do bug descrito em AndroidBugsUtils.applyWorkaroundForInvalidFragmentManagerStateBug.
		Fragment frag = fragmentManager.findFragmentByTag(TIME_PICKER_FRAGMENT_TAG);
		if (frag != null) {
			if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
				TimePickerFragmentV21 timePicker = (TimePickerFragmentV21) frag;
				timePicker.setEditor(this);
			} else {
				TimePickerFragment timePicker = (TimePickerFragment) fragmentManager.findFragmentByTag(TIME_PICKER_FRAGMENT_TAG);
				timePicker.setOnTimeSetListener(this);
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
    protected void refreshView(Time newValue) {
        super.refreshView(newValue);

        if (newValue == null) {
            textView.setText(null);
        } else {
            textView.setText(mask.formatTime(newValue));
        }
    }

    @Override
    public TextView getView() {
        return textView;
    }

    @Override
	protected Time internalLoadValue(IEntityRecord record, String fieldName) {
		return record.getTimeValue(fieldName);
	}

	@Override
	protected void internalStoreValue(IEntityRecord record, String fieldName, Time value) {
		record.setTimeValue(fieldName, value);
	}

	@Override
	protected Time internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return (Time) bundle.get(key);
	}

	@Override
	protected void internalSaveState(Bundle bundle, String key, Time value) {
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
			TimePickerFragmentV21 timePicker = new TimePickerFragmentV21();
			timePicker.setEditor(this);
			frag = timePicker;
		} else {
			Calendar calendar = getCalendarValue();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			frag = TimePickerFragment.newInstance(this, hour, minute, is24Hour());
		}
		frag.show(fragmentManager, TIME_PICKER_FRAGMENT_TAG);
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		setCalendarValue(hourOfDay, minute);
	}


	/*
	 * Métoods / Classes auxiliares
	 */

	private boolean is24Hour() {
		return mask.is24Hour();
	}

	private Calendar getCalendarValue() {
		Calendar calendar = Calendar.getInstance();
		Time time = getValue();
		//Se a hora do registro for nula, usa a hora atual.
		if (time != null) {
			calendar.setTime(time);
		}
		return calendar;
	}

	private void setCalendarValue(int hourOfDay, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		setValue(new Time(calendar.getTimeInMillis()));
	}


	/**
	 * Fragmento que exibe um Dialog para a seleção de hora.<br>
	 * Precisa ser público e estático para poder ser instânciado por fora desta classe (quando o fragmento estiver sendo
	 * recuperado, por exemplo).
	 */
	public static class TimePickerFragmentV21 extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		private TimeFieldEditor editor;

		public void setEditor(TimeFieldEditor editor) {
			this.editor = editor;
		}

		@Override
		@NonNull
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			//Se está recuperando o fragmento, pode criar o Dialog zerado que ele mesmo irá recuperar seu estado.
			if (savedInstanceState != null) {
				return AndroidBugsUtils.applyWorkaroundForBug34833(getActivity(), this, 0, 0, false);
			}

			Calendar calendar = editor.getCalendarValue();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			return AndroidBugsUtils.applyWorkaroundForBug34833(getActivity(), this, hour, minute, editor.is24Hour());
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			editor.setCalendarValue(hourOfDay, minute);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			editor = null;
		}
	}
}
