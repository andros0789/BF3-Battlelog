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

package com.ninetwozero.battlelog.activity;

import java.util.ArrayList;
import java.util.List;

import net.peterkuterna.android.apps.swipeytabs.SwipeyTabs;
import net.peterkuterna.android.apps.swipeytabs.SwipeyTabsPagerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.ninetwozero.battlelog.MainActivity;
import com.ninetwozero.battlelog.R;
import com.ninetwozero.battlelog.activity.aboutapp.AboutActivity;
import com.ninetwozero.battlelog.activity.aboutapp.FeedbackActivity;
import com.ninetwozero.battlelog.activity.feed.FeedFragment;
import com.ninetwozero.battlelog.activity.forum.MenuForumFragment;
import com.ninetwozero.battlelog.activity.news.NewsListFragment;
import com.ninetwozero.battlelog.activity.platoon.MenuPlatoonFragment;
import com.ninetwozero.battlelog.activity.profile.MenuProfileFragment;
import com.ninetwozero.battlelog.activity.social.ComFriendFragment;
import com.ninetwozero.battlelog.activity.social.ComNotificationFragment;
import com.ninetwozero.battlelog.asynctask.AsyncLogout;
import com.ninetwozero.battlelog.datatype.DefaultFragmentActivity;
import com.ninetwozero.battlelog.datatype.PlatoonData;
import com.ninetwozero.battlelog.datatype.ProfileData;
import com.ninetwozero.battlelog.http.FeedClient;
import com.ninetwozero.battlelog.misc.SessionKeeper;

public class DashboardActivity extends CustomFragmentActivity implements DefaultFragmentActivity {

    // COM-related
    private SlidingDrawer mSlidingDrawer;
    private TextView mSlidingDrawerHandle;

    // Fragment related
    private SwipeyTabs mTabsCom;
    private SwipeyTabsPagerAdapter mPagerAdapterCom;
    private List<Fragment> mListFragmentsCom;
    private MenuPlatoonFragment mFragmentMenuPlatoon;
    private FeedFragment mFragmentFeed;
    private ComFriendFragment mFragmentComFriends;
    private ComNotificationFragment mFragmentComNotifications;
    private ViewPager mViewPager;
    private ViewPager mViewPagerCom;
    private final int VIEWPAGER_POSITION_FEED = 4;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.viewpager_dashboard);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        validateSession();
        
        init();
        setup();
        handleIfOpenedViaNotification();
    }

    private void handleIfOpenedViaNotification() {
		if( getIntent().getBooleanExtra("openCOMCenter", false) ) {
			mSlidingDrawer.open();
			mViewPagerCom.setCurrentItem(1);
		}
	}

	public final void init() {
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.com_slider);
        mSlidingDrawerHandle = (TextView) findViewById(R.id.com_slide_handle_text);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setup() {
        if (mListFragments == null) {
            mListFragments = new ArrayList<Fragment>();
            mListFragments.add(Fragment.instantiate(this, NewsListFragment.class.getName()));
            mListFragments.add(Fragment.instantiate(this, MenuProfileFragment.class.getName()));
            mListFragments.add(mFragmentMenuPlatoon = (MenuPlatoonFragment) Fragment.instantiate(this, MenuPlatoonFragment.class.getName()));
            mListFragments.add(Fragment.instantiate(this, MenuForumFragment.class.getName()));
            mListFragments.add(mFragmentFeed = (FeedFragment) Fragment.instantiate(this, FeedFragment.class.getName()));

            mFragmentMenuPlatoon.setPlatoonData(SessionKeeper.getPlatoonData());

            mFragmentFeed.setType(FeedClient.TYPE_GLOBAL);
            mFragmentFeed.setCanWrite(true);

            mViewPager = (ViewPager) findViewById(R.id.viewpager);
            mTabs = (SwipeyTabs) findViewById(R.id.swipeytabs);

            mPagerAdapter = new SwipeyTabsPagerAdapter(
                    mFragmentManager, new String[]{"NEWS", "PROFILE", "PLATOON",
                    "FORUM", "FEED"}, mListFragments, mViewPager,
                    mLayoutInflater);
            mViewPager.setAdapter(mPagerAdapter);
            mTabs.setAdapter(mPagerAdapter);

            mViewPager.setOnPageChangeListener(mTabs);
            mViewPager.setOffscreenPageLimit(4);
            mViewPager.setCurrentItem(1);
        }

        if (mListFragmentsCom == null) {
            mListFragmentsCom = new ArrayList<Fragment>();
            mListFragmentsCom.add(mFragmentComFriends = (ComFriendFragment) Fragment.instantiate(this, ComFriendFragment.class.getName()));
            mListFragmentsCom.add(mFragmentComNotifications = (ComNotificationFragment) Fragment.instantiate(this, ComNotificationFragment.class.getName()));

            mViewPagerCom = (ViewPager) findViewById(R.id.viewpager_sub);
            mTabsCom = (SwipeyTabs) findViewById(R.id.swipeytabs_sub);

            mPagerAdapterCom = new SwipeyTabsPagerAdapter(
                    mFragmentManager, 
                    new String[]{"FRIENDS", "NOTIFICATIONS"},
                    mListFragmentsCom, 
                    mViewPagerCom, 
                    mLayoutInflater
            );
            mViewPagerCom.setAdapter(mPagerAdapterCom);
            mTabsCom.setAdapter(mPagerAdapterCom);

            mViewPagerCom.setOnPageChangeListener(mTabsCom);
            mViewPagerCom.setOffscreenPageLimit(1);
            mViewPagerCom.setCurrentItem(0);
        }
    }

    public void validateSession() {
        if (SessionKeeper.getProfileData() == null) {
            if (getIntent().hasExtra("myProfile")) {

                ProfileData profileData = getIntent().getParcelableExtra("myProfile");
                List<PlatoonData> platoonArray = getIntent().getParcelableArrayListExtra("myPlatoon");

                if( profileData == null || platoonArray == null ) {
                    startActivity(new Intent(this, MainActivity.class));
                	finish();
                } else {
                	SessionKeeper.setProfileData(profileData);
                	SessionKeeper.setPlatoonData(platoonArray);
                }
            } else {
                Toast.makeText(this, R.string.info_txt_session_lost, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    public void reload() {
        mFragmentComFriends.reload();
        mFragmentComNotifications.reload();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        if (mSlidingDrawer.isOpened()) {
            switch (mViewPagerCom.getCurrentItem()) {
                case 0:
                    mFragmentComFriends.createContextMenu(menu, view, menuInfo);
                    break;
                default:
                    break;
            }
        } else {
            switch (mViewPager.getCurrentItem()) {
                case VIEWPAGER_POSITION_FEED:
                    mFragmentFeed.createContextMenu(menu, view, menuInfo);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return false;
        }
        if (mSlidingDrawer.isOpened()) {
            switch (mViewPagerCom.getCurrentItem()) {
                case 0:
                    mFragmentComFriends.handleSelectedContextItem(info, item);
                    break;
                default:
                    break;
            }
        } else {
            switch (mViewPager.getCurrentItem()) {
                case VIEWPAGER_POSITION_FEED:
                    return mFragmentFeed.handleSelectedContextItem(info, item);
                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.option_search) {
        	startActivity(new Intent(this, SearchActivity.class) );
        } else if (item.getItemId() == R.id.option_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        } else if(item.getItemId() == R.id.option_feedback) {
        	startActivity(new Intent(this, FeedbackActivity.class));
        } else if (item.getItemId() == R.id.option_logout) {
            new AsyncLogout(this).execute();
        } else if (item.getItemId() == R.id.option_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        return true;
    }

    public void setComLabel(String str) {
        mSlidingDrawerHandle.setText(str);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSlidingDrawer.isOpened()) {
                mSlidingDrawer.animateClose();
                return true;
            } else if (mViewPager.getCurrentItem() != 1) {
                mViewPager.setCurrentItem(1, true);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            startActivity(new Intent(this, SearchActivity.class));
        }
        return super.onKeyDown(keyCode, event);
    }
}
