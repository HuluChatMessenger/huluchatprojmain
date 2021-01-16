package org.plus.apps.huluride;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BackupImageView;

public class PickupActivity extends BaseFragment {

    private ImageView locationButton;
    private BackupImageView avatarImageView;

    private GoogleMap googleMap;
    private MapView mapView;


    @Override
    public View createView(Context context) {
        return super.createView(context);
    }
}
