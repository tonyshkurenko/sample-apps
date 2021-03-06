/**
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.photoframe.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.kaaproject.kaa.demo.photoframe.MainActivity;
import org.kaaproject.kaa.demo.photoframe.R;
import org.kaaproject.kaa.demo.photoframe.communication.Events;
import org.kaaproject.kaa.demo.photoframe.kaa.KaaManager;

/**
 * The implementation of the {@link Fragment} class. Used as a superclass for most application fragments.
 * Implements common fragment lifecycle functions. Stores references to common application resources.
 * Provides functions for switching between views representing busy progress, an error message, and content.
 */
public abstract class BaseFragment extends Fragment {

    private ActionBar mActionBar;

    public BaseFragment() {
        super();
    }

    public static Fragment getCurrentFragment(MainActivity activity) {

        final FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            final FragmentManager.BackStackEntry entry = fm.
                    getBackStackEntryAt(fm.getBackStackEntryCount() - 1);
            return fm.findFragmentByTag(entry.getName());
        }
        return null;
    }

    public void move(Activity activity) {

        final String tag = getFragmentTag();
        ((MainActivity) activity).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(tag)
                .commit();
    }

    public static void popBackStack(FragmentActivity activity) {

        final FragmentManager fm = activity.getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context != null) {
            mActionBar = ((MainActivity) context).getSupportActionBar();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (updateActionBar() && mActionBar != null) {
            int options = ActionBar.DISPLAY_SHOW_TITLE;
            if (displayHomeAsUp()) {
                options |= ActionBar.DISPLAY_HOME_AS_UP;
            }

            mActionBar.setDisplayOptions(options, ActionBar.DISPLAY_HOME_AS_UP
                    | ActionBar.DISPLAY_SHOW_TITLE);
            mActionBar.setTitle(getTitle());

            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setHomeButtonEnabled(displayHomeAsUp());
        }

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEvent(final Events.PlayAlbumEvent playAlbumEvent) {
        final MainActivity activity = (MainActivity) getActivity();
        final Fragment fragment = BaseFragment.getCurrentFragment(activity);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (getKaaManager().isUserAttached()) {
                    loadSlideshow(playAlbumEvent, fragment);
                } else {
                    // If you logout, but get this event
                    Toast.makeText(getActivity(), R.string.fragment_base_interaction_event_text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSlideshow(Events.PlayAlbumEvent playAlbumEvent, Fragment fragment) {
        if (fragment != null && fragment instanceof SlideshowFragment) {
            ((SlideshowFragment) fragment).updateBucketId(playAlbumEvent.getBucketId());
        } else {
            SlideshowFragment.newInstance(playAlbumEvent.getBucketId()).move(getActivity());
        }
    }

    @Subscribe
    public void onEvent(Events.UserDetachEvent userDetachEvent) {
        final String errorMessage = userDetachEvent.getErrorMessage();
        if (errorMessage != null) {
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            return;
        }
        new LoginFragment().move(getActivity());
    }

    protected KaaManager getKaaManager() {
        return ((MainActivity) getActivity()).getKaaManager();
    }

    public abstract String getTitle();

    public abstract String getFragmentTag();

    protected abstract boolean displayHomeAsUp();

    protected boolean updateActionBar() {
        return true;
    }
}
