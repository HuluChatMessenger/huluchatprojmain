package org.plus.apps.ride;

import android.widget.ImageView;

import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.BaseFragment;

public class RideFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {


    private ImageView locationButton;


    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }
}
