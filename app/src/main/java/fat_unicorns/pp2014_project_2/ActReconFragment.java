package fat_unicorns.pp2014_project_2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.SharedPreferences.*;


import org.w3c.dom.Text;

public class ActReconFragment extends Fragment implements OnSharedPreferenceChangeListener {
    public static final String PREFS_NAME = "ProjectPrefsFile";

    private TextView activity_display_text;

    public static ActReconFragment newInstance() {
        return new ActReconFragment();
    }
    public ActReconFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_act_recon, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        this.getActivity().getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        activity_display_text = (TextView) getActivity().findViewById(R.id.activity_display_text);
        SharedPreferences settings = this.getActivity().getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        activity_display_text.setText("Activity: " + settings.getString("Activity", "-") + "(" + settings.getString("Confidence", "-1") + ")");

    }
}
