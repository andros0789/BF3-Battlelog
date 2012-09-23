/*
    This file is part of BF3 Battlelog

    BF3 Battlelog is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BF3 Battlelog is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 */

package com.ninetwozero.battlelog.activity.profile.soldier;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import com.google.gson.Gson;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.activity.Bf3Fragment;
import com.ninetwozero.battlelog.activity.profile.unlocks.UnlockActivity;
import com.ninetwozero.battlelog.datatype.DefaultFragment;
import com.ninetwozero.battlelog.datatype.PersonaStats;
import com.ninetwozero.battlelog.datatype.ProfileData;
import com.ninetwozero.battlelog.datatype.Statistics;
import com.ninetwozero.battlelog.dialog.ListDialogFragment;
import com.ninetwozero.battlelog.dialog.OnCloseListDialogListener;
import com.ninetwozero.battlelog.jsonmodel.PersonaInfo;
import com.ninetwozero.battlelog.loader.Bf3Loader;
import com.ninetwozero.battlelog.loader.CompletedTask;
import com.ninetwozero.battlelog.misc.Constants;
import com.ninetwozero.battlelog.misc.SessionKeeper;
import com.ninetwozero.battlelog.provider.UriFactory;
import com.ninetwozero.battlelog.provider.table.PersonaStatistics;
import com.ninetwozero.battlelog.provider.table.RankProgress;
import com.ninetwozero.battlelog.provider.table.ScoreStatistics;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.ninetwozero.battlelog.dao.PersonaStatisticsDAO.*;
import static com.ninetwozero.battlelog.dao.RankProgressDAO.*;
import static com.ninetwozero.battlelog.dao.ScoreStatisticsDAO.*;

public class ProfileStatsFragment extends Bf3Fragment implements DefaultFragment,
        OnCloseListDialogListener {

    // Attributes
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private SharedPreferences mSharedPreferences;

    // Elements
    private RelativeLayout mWrapPersona;
    private ProgressBar mProgressBar;

    // Misc
    private ProfileData mProfileData;
    private Map<Long, PersonaStats> mPersonaStats;
    private long mSelectedPersona;
    private int mSelectedPosition;
    private int mSelectedPlatformId;
    private String mSelectedPersonaName;
    private boolean mComparing;

    private URI callURI;
    private final String DIALOG = "dialog";
    private TextView personaName, rankTitle, rankId, currentLevelPoints, nextLevelPoints,
            pointsToMake;

    private PersonaStats ps;
    private Bundle bundle;
    private ProgressDialog progressDialog;
    private boolean hasDBData = false;
    private RankProgress rankProgress;
    private TableLayout personaStatisticsTable;
    private TableLayout scoreStatisticsTable;
    private List<Statistics> listPersonaStatistics;
    private List<Statistics> listScoreStatistics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Set our attributes
        mContext = getActivity();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLayoutInflater = inflater;

        View view = mLayoutInflater.inflate(R.layout.tab_content_profile_stats,
                container, false);

        initFragment(view);
        getData();
        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.bundle = savedInstanceState;
        if (hasDBData) {
            findViews();
            populateView();
        }
    }

    public void initFragment(View view) {

        // Progressbar
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_level);

        // Let's try something out
        if (mProfileData.getId() == SessionKeeper.getProfileData().getId()) {
            mSelectedPosition = mSharedPreferences.getInt(Constants.SP_BL_PERSONA_CURRENT_POS, 0);
            mSelectedPersona = getSelectedPersonaId(mSelectedPosition);
            mSelectedPlatformId = getPlatformIdFor(mSelectedPosition);
            mSelectedPersonaName = getSelectedPersonaName(mSelectedPosition);
            callURI = UriFactory.personaOverview(mSelectedPersona, mSelectedPlatformId);
        }

        // Click on the wrap
        mWrapPersona = (RelativeLayout) view.findViewById(R.id.wrap_persona);
        mWrapPersona.setOnClickListener(

                new OnClickListener() {

                    @Override
                    public void onClick(View sv) {

                        if (personaArrayLength() > 1) {
                            FragmentManager manager = getFragmentManager();
                            ListDialogFragment dialog = ListDialogFragment.newInstance(
                                    mProfileData.getPersonaArray(), getTag());
                            dialog.show(manager, DIALOG);
                        }
                    }
                });
    }

    private long getSelectedPersonaId(int position) {
        String idsString = mSharedPreferences.getString(Constants.SP_BL_PERSONA_ID, "");
        String[] ids = idsString.split(":");
        return Long.parseLong(ids[position]);
    }

    private int getPlatformIdFor(int position) {
        String idsString = mSharedPreferences.getString(Constants.SP_BL_PLATFORM_ID, "");
        String[] ids = idsString.split(":");
        return Integer.parseInt(ids[position]);
    }

    private String getSelectedPersonaName(int position) {
        String names = mSharedPreferences.getString(Constants.SP_BL_PERSONA_NAME, "");
        String[] namesArray = names.split(":");
        return namesArray[position];
    }

    @Override
    public void onDialogListSelection() {
        Log.e("ProfileStatsFragment", "I AM BACK ! ! !");
    }

    private void getData() {
        if(dbHasData()){
            findViews();
            hasDBData = true;
        }else {
            getLoaderManager().initLoader(0, bundle, this);
        }
    }

    private boolean dbHasData(){
        return hasRankData() && hasPersonaStatistics() && hasScoreStatistics();
    }

    private boolean hasRankData(){
        Cursor cursor = getContext().getContentResolver()
                .query(RankProgress.URI, RankProgress.RANK_PROGRESS_PROJECTION,
                        RankProgress.Columns.PERSONA_ID + "=?", new String[]{String.valueOf(mSelectedPersona)}, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            rankProgress = rankProgressFromCursor(cursor);
            cursor.close();
            return true;
        }
        return false;
    }

    private boolean  hasPersonaStatistics(){
        Cursor cursor = getContext().getContentResolver()
                .query(PersonaStatistics.URI, PersonaStatistics.PERSONA_STATS_PROJECTION,
                        PersonaStatistics.Columns.PERSONA_ID + "=?", new String[]{String.valueOf(mSelectedPersona)}, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            listPersonaStatistics = personaStaticsFromCursor(cursor);
            cursor.close();
            return true;
        }
        return false;
    }

    private boolean  hasScoreStatistics(){
        Cursor cursor = getContext().getContentResolver()
                .query(ScoreStatistics.URI, ScoreStatistics.SCORE_STATISTICS_PROJECTION,
                        ScoreStatistics.Columns.PERSONA_ID + "=?", new String[]{String.valueOf(mSelectedPersona)}, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            listScoreStatistics = scoreStatisticsFromCursor(cursor);
            cursor.close();
            return true;
        }
        return false;
    }

    public void findViews() {

        // Let's find it
        View view = getView();
        if (view == null) {
            return;
        }

        if (personaArrayLength() == 1) {
            view.findViewById(R.id.img_persona_list).setVisibility(View.INVISIBLE);
        }

        // Persona & rank
        personaName = (TextView) view.findViewById(R.id.string_persona);
        rankTitle = (TextView) view.findViewById(R.id.string_rank_title);
        rankId = (TextView) view.findViewById(R.id.string_rank_short);

        // Progress
        currentLevelPoints = (TextView) view.findViewById(R.id.string_progress_curr);
        nextLevelPoints = (TextView) view.findViewById(R.id.string_progress_max);
        pointsToMake = (TextView) view.findViewById(R.id.string_progress_left);

        personaStatisticsTable = (TableLayout) view.findViewById(R.id.persona_statistics);
        scoreStatisticsTable = (TableLayout) view.findViewById(R.id.score_statistics);

        // Are we going to compare?
        /*
         * if (comparing) { ((CompareActivity)
         * getActivity()).sendToCompare(profileData,
         * personaStats,selectedPersona,toggle); }
         */
    }

    private void populateView(){
        populateRankProgress();
        populateStatistics(listPersonaStatistics, personaStatisticsTable);
        populateStatistics(listScoreStatistics, scoreStatisticsTable);
    }

    private void populateRankProgress() {

        if (rankProgress == null) {
            return;
        }
        Log.e("STATS", "Populating view");
        personaName.setText(rankProgress.getPersonaName() + " " + rankProgress.getPlatform());
        rankTitle.setText(fromResource(rankProgress.getRank()));
        rankId.setText(String.valueOf(rankProgress.getRank()));

        // Progress
        mProgressBar.setMax((int) (rankProgress.getNextRankScore() - rankProgress.getCurrentRankScore()));
        mProgressBar.setProgress((int) (rankProgress.getScore() - rankProgress.getCurrentRankScore()));
        currentLevelPoints.setText(String.valueOf(rankProgress.getScore() - rankProgress.getCurrentRankScore()));
        nextLevelPoints.setText(String.valueOf(rankProgress.getNextRankScore() - rankProgress.getCurrentRankScore()));
        pointsToMake.setText(String.valueOf(rankProgress.getNextRankScore() - rankProgress.getScore()));
    }

    private void populateStatistics(List<Statistics> statistics, TableLayout layout){
        for (Statistics ps : statistics) {
            TableRow tr = new TableRow(getContext());
            tr.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (ps.getStyle() == R.style.InfoSubHeading) {
                tr.setBackgroundColor(Color.parseColor("#EEEEEE"));
            }

            TextView title = new TextView(getContext());
            title.setText(ps.getTitle());
            title.setTextColor(Color.parseColor("#000000"));
            title.setPadding(5, 5, 5, 5);
            tr.addView(title);

            TextView value = new TextView(getContext());
            value.setText(ps.getValue());
            value.setTextColor(Color.parseColor("#000000"));
            value.setPadding(5, 5, 5, 5);
            tr.addView(value);

            layout.addView(tr);
        }
    }

    private int personaArrayLength() {
        return mProfileData.getPersonaArray().length;
    }

    @Override
    protected Loader<CompletedTask> createLoader(int id, Bundle bundle) {
        startLoadingDialog();
        return new Bf3Loader(getContext(), callURI);
    }

    private Context getContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void loadFinished(Loader<CompletedTask> loader, CompletedTask task) {
        if (task.result.equals(CompletedTask.Result.SUCCESS)) {
            Log.e("STATS", "Load finished");
            findViews();
            PersonaInfo pi = personaStatsFrom(task);
            updateDatabase(pi);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            populateView();
        }
    }

    private PersonaInfo personaStatsFrom(CompletedTask task) {
        Gson gson = new Gson();
        PersonaInfo data = gson.fromJson(task.jsonObject, PersonaInfo.class);
        return data;
    }

    private String fromResource(int rank) {
        return getResources().getStringArray(R.array.rank)[rank];
    }

    private void updateDatabase(PersonaInfo pi) {
        updateRankProgressDB(pi);
        updatePersonaStats(pi);
        updateScoreStatistics(pi);
    }

    private void updateRankProgressDB(PersonaInfo pi) {
        rankProgress = rankProgressFromJSON(pi);
        getContext().getContentResolver().insert(RankProgress.URI, rankProgressForDB(pi, mSelectedPersona));
    }

    private void updatePersonaStats(PersonaInfo pi) {
        listPersonaStatistics = personaStatisticsFromJSON(pi);
        getContext().getContentResolver().insert(PersonaStatistics.URI, personaStatisticsForDB(pi, mSelectedPersona));
    }

    private void updateScoreStatistics(PersonaInfo pi){
        listScoreStatistics = scoreStatisticsFromJSON(pi);
        getContext().getContentResolver().insert(ScoreStatistics.URI, scoreStatisticsForDB(pi, mSelectedPersona));
    }

    public void setProfileData(ProfileData p) {
        mProfileData = p;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public Menu prepareOptionsMenu(Menu menu) {

        ((MenuItem) menu.findItem(R.id.option_friendadd))
                .setVisible(false);
        ((MenuItem) menu.findItem(R.id.option_frienddel))
                .setVisible(false);
        ((MenuItem) menu.findItem(R.id.option_compare))
                .setVisible(true);
        ((MenuItem) menu.findItem(R.id.option_unlocks))
                .setVisible(true);
        return menu;

    }

    public boolean handleSelectedOption(MenuItem item) {

        if (item.getItemId() == R.id.option_compare) {

            startActivity(new Intent(mContext, CompareActivity.class)
                    .putExtra("profile1", SessionKeeper.getProfileData())
                    .putExtra("profile2", mProfileData)
                    .putExtra("selectedPosition", mSelectedPosition));

        } else if (item.getItemId() == R.id.option_unlocks) {

            int position = 0;
            for (long key : mPersonaStats.keySet()) {

                if (key == mSelectedPersona) {
                    break;
                } else {
                    position++;
                }
            }

            startActivity(

                    new Intent(mContext, UnlockActivity.class)
                            .putExtra("profile", mProfileData)
                            .putExtra("selectedPosition", position));
        }
        return true;
    }

    public void setComparing(boolean c) {
        mComparing = c;
    }

    @Override
    public void reload() {
    }

    private void startLoadingDialog() {   //TODO extract multiple duplicates of same code
        this.progressDialog = new ProgressDialog(mContext);
        this.progressDialog.setTitle(mContext
                .getString(R.string.general_wait));
        this.progressDialog.setMessage(mContext
                .getString(R.string.general_downloading));
        this.progressDialog.show();
    }
}