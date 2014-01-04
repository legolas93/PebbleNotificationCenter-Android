package com.matejdro.pebblenotificationcenter.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.matejdro.pebblenotificationcenter.PebbleNotificationCenter;
import com.matejdro.pebblenotificationcenter.R;
import com.matejdro.pebblenotificationcenter.util.ListSerialization;

/**
 * Created by jbergler on 25/11/2013.
 */
public class PerAppRegexFragment extends Fragment {
	private static SharedPreferences preferences;
	private static SharedPreferences.Editor editor;

	private ListView regexListView;
	private View regexListViewHeader;
	private RegexListAdapterIn regexAdapterIn;
	private RegexListAdapterEx regexAdapterEx;
	private List<String> regexListIn;
	private List<String> regexListEx;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		editor = preferences.edit();

		regexListIn = new ArrayList<String>();
		regexListEx = new ArrayList<String>();
		ListSerialization.loadCollection(preferences, regexListIn, PebbleNotificationCenter.REGEX_LIST+"_IN"+PerAppActivity.appName);
		ListSerialization.loadCollection(preferences, regexListEx, PebbleNotificationCenter.REGEX_LIST+"_EX"+PerAppActivity.appName);

		return inflater.inflate(R.layout.fragment_per_app_regex_list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {


		TextView appNameTextView = (TextView) getActivity().findViewById(R.id.perAppAppName);
		appNameTextView.setText(PerAppActivity.appName);

		//Header for the Regex List, this is what we use to have an 'Add new entry' item
		regexListViewHeader = View.inflate(getActivity(), R.layout.fragment_regex_list_header, null);
		regexListViewHeader.setOnClickListener(new AdapterView.OnClickListener() {
			@Override
			public void onClick(View view) {
				add(true);
			}
		});

		//Setup the List<String> adapter, map it to the ListView and add the header
		regexAdapterIn = new RegexListAdapterIn();
		regexListView  = (ListView) getView().findViewById(R.id.regexListIn);
		regexListView.addHeaderView(regexListViewHeader);
		regexListView.setAdapter(regexAdapterIn);

		//Click handler for list items.
		regexListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int headerCount = regexListView.getHeaderViewsCount(); //Make sure to take header into account.
				if (position >= headerCount) edit(position - headerCount, true);
			}
		});

		//Header for the Regex List, this is what we use to have an 'Add new entry' item
		regexListViewHeader = View.inflate(getActivity(), R.layout.fragment_regex_list_header, null);
		regexListViewHeader.setOnClickListener(new AdapterView.OnClickListener() {
			@Override
			public void onClick(View view) {
				add(false);
			}
		});

		//Setup the List<String> adapter, map it to the ListView and add the header
		regexAdapterEx = new RegexListAdapterEx();
		regexListView  = (ListView) getView().findViewById(R.id.regexListEx);
		regexListView.addHeaderView(regexListViewHeader);
		regexListView.setAdapter(regexAdapterEx);

		//Click handler for list items.
		regexListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int headerCount = regexListView.getHeaderViewsCount(); //Make sure to take header into account.
				if (position >= headerCount) edit(position - headerCount, false);
			}
		});

		Spinner filterModeSpinnger = (Spinner) getView().findViewById(R.id.perAppFilterModeSpinner);
		filterModeSpinnger.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				/* Logic
				 * <string-array name="per_app_strongest_spinner">
        		 * <item>Both</item>
        		 * <item>Include ONLY of the Excluded</item>
        		 * <item>Exclude ONLY of the Included</item>
    <			 * </string-array>
		         */
				editor.putInt(PebbleNotificationCenter.REGEX_LIST+"_MODE"+PerAppActivity.appName, pos);
				editor.apply();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
		filterModeSpinnger.setSelection(preferences.getInt(PebbleNotificationCenter.REGEX_LIST+"_MODE"+PerAppActivity.appName, 0));

		super.onActivityCreated(savedInstanceState);
	}

	private void saveList()
	{
		ListSerialization.saveCollection(editor, regexListIn, PebbleNotificationCenter.REGEX_LIST+"_IN"+PerAppActivity.appName);
		ListSerialization.saveCollection(editor, regexListEx, PebbleNotificationCenter.REGEX_LIST+"_EX"+PerAppActivity.appName);
		PebbleNotificationCenter.getInMemorySettings().markDirty();
		regexAdapterIn.notifyDataSetChanged();
		regexAdapterEx.notifyDataSetChanged();
	}

	/**
	 * Build a dialog to handle adding a new regex
	 * TODO: this should do some error checking
	 */
	private void add(final boolean include) //true include false exclude
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final EditText editField = new EditText(getActivity());

		builder.setTitle("Add Filter");
		builder.setView(editField);

		builder.setMessage("Enter keyword/regex for this filter");

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(include){
					regexListIn.add(editField.getText().toString());
				} else {
					regexListEx.add(editField.getText().toString());
				}
				saveList();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	/**
	 * Build a dialog to handle editing/deleting a regex already in the list
	 * TODO: this should do some error checking (like add())
	 * @param position index in regexList to edit
	 */
	private void edit(final int position, final boolean include) //true include false exclude
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final EditText editField = new EditText(getActivity());
		if(include){
			editField.setText(regexListIn.get(position));
		} else {
			editField.setText(regexListEx.get(position));
		}
		builder.setTitle("Editing Filter");
		builder.setView(editField);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(include){
					regexListIn.set(position, editField.getText().toString());
				} else {
					regexListEx.set(position, editField.getText().toString());
				}
				saveList();
			}
		});

		builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(include){
					regexListIn.remove(position);
				} else {
					regexListEx.remove(position);
				}
				saveList();
			}
		});

		builder.setNegativeButton("Cancel", null);
		builder.show();
	}


	private class RegexListAdapterIn extends BaseAdapter
	{

		@Override
		public int getCount() {
			return regexListIn.size();
		}

		@Override
		public Object getItem(int position) {
			return regexListIn.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_regex_list_item, null);

			TextView text = (TextView) convertView.findViewById(R.id.regex);
			text.setText(regexListIn.get(position));

			return convertView;
		}
	}
	private class RegexListAdapterEx extends BaseAdapter
	{

		@Override
		public int getCount() {
			return regexListEx.size();
		}

		@Override
		public Object getItem(int position) {
			return regexListEx.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_regex_list_item, null);

			TextView text = (TextView) convertView.findViewById(R.id.regex);
			text.setText(regexListEx.get(position));

			return convertView;
		}
	}
}
