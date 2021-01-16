package org.plus.apps.ride;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MapPlaceholderDrawable;
import org.telegram.ui.LocationActivity;

import java.util.HashMap;


public class RideActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {


    private TextView setPickUpButton;

    private ImageView locationButton;
    private boolean checkGpsEnabled;

    public static class CarLocation {
        public int num;
        public Marker marker;
    }


    private Marker lastPressedMarker;
    private LocationActivity.VenueLocation lastPressedVenue;
    private FrameLayout lastPressedMarkerView;

    private GoogleMap googleMap;
    private FrameLayout mapViewClip;
    private MapView mapView;
    private MapOverlayView overlayView;


    private MenuDrawable menuDrawable;

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.locationPermissionGranted);
        return super.onFragmentCreate();
    }


    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return super.isSwipeBackEnabled(event);
    }

    private BaseFragment parentFragment;
    private boolean currentMapStyleDark;
    private boolean onResumeCalled;
    private boolean mapsInitialized;



    @Override
    public void onFragmentDestroy() {
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.locationPermissionGranted);
        try {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(false);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            if (mapView != null) {
                mapView.onDestroy();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        super.onFragmentDestroy();
    }

    public RideActivity(){
        AndroidUtilities.fixGoogleMapsBug();
    }


    public class MapOverlayView extends FrameLayout {

        private HashMap<Marker, View> views = new HashMap<>();

        public MapOverlayView(Context context) {
            super(context);
        }

        public void addInfoView(Marker marker) {
            LocationActivity.VenueLocation location = (LocationActivity.VenueLocation) marker.getTag();
            if (location == null || lastPressedVenue == location) {
                return;
            }
            //showSearchPlacesButton(false);
            if (lastPressedMarker != null) {
                removeInfoView(lastPressedMarker);
                lastPressedMarker = null;
            }
            lastPressedVenue = location;
            lastPressedMarker = marker;

            Context context = getContext();

            FrameLayout frameLayout = new FrameLayout(context);
            addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 114));

            lastPressedMarkerView = new FrameLayout(context);
            lastPressedMarkerView.setBackgroundResource(R.drawable.venue_tooltip);
            lastPressedMarkerView.getBackground().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));
            frameLayout.addView(lastPressedMarkerView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 71));
            lastPressedMarkerView.setAlpha(0.0f);
//            lastPressedMarkerView.setOnClickListener(v -> {
//                if (parentFragment != null && parentFragment.isInScheduleMode()) {
//                    AlertsCreator.createScheduleDatePickerDialog(getParentActivity(), parentFragment.getDialogId(), (notify, scheduleDate) -> {
//                        delegate.didSelectLocation(location.venue, locationType, notify, scheduleDate);
//                        finishFragment();
//                    });
//                } else {
//                    delegate.didSelectLocation(location.venue, locationType, true, 0);
//                    finishFragment();
//                }
//            });

            TextView nameTextView = new TextView(context);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            nameTextView.setMaxLines(1);
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            nameTextView.setSingleLine(true);
            nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            nameTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            lastPressedMarkerView.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 18, 10, 18, 0));

            TextView addressTextView = new TextView(context);
            addressTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            addressTextView.setMaxLines(1);
            addressTextView.setEllipsize(TextUtils.TruncateAt.END);
            addressTextView.setSingleLine(true);
            addressTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            addressTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            lastPressedMarkerView.addView(addressTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 18, 32, 18, 0));

            nameTextView.setText(location.venue.title);
            addressTextView.setText(LocaleController.getString("TapToSendLocation", R.string.TapToSendLocation));

            FrameLayout iconLayout = new FrameLayout(context);
            iconLayout.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(36), LocationCell.getColorForIndex(location.num)));
            frameLayout.addView(iconLayout, LayoutHelper.createFrame(36, 36, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 4));

            BackupImageView imageView = new BackupImageView(context);
            imageView.setImage("https://ss3.4sqi.net/img/categories_v2/" + location.venue.venue_type + "_64.png", null, null);
            iconLayout.addView(imageView, LayoutHelper.createFrame(30, 30, Gravity.CENTER));

            ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                private boolean startedInner;
                private final float[] animatorValues = new float[]{0.0f, 1.0f};

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = AndroidUtilities.lerp(animatorValues, animation.getAnimatedFraction());
                    if (value >= 0.7f && !startedInner && lastPressedMarkerView != null) {
                        AnimatorSet animatorSet1 = new AnimatorSet();
                        animatorSet1.playTogether(
                                ObjectAnimator.ofFloat(lastPressedMarkerView, View.SCALE_X, 0.0f, 1.0f),
                                ObjectAnimator.ofFloat(lastPressedMarkerView, View.SCALE_Y, 0.0f, 1.0f),
                                ObjectAnimator.ofFloat(lastPressedMarkerView, View.ALPHA, 0.0f, 1.0f));
                        animatorSet1.setInterpolator(new OvershootInterpolator(1.02f));
                        animatorSet1.setDuration(250);
                        animatorSet1.start();
                        startedInner = true;
                    }
                    float scale;
                    if (value <= 0.5f) {
                        scale = 1.1f * CubicBezierInterpolator.EASE_OUT.getInterpolation(value / 0.5f);
                    } else if (value <= 0.75f) {
                        value -= 0.5f;
                        scale = 1.1f - 0.2f * CubicBezierInterpolator.EASE_OUT.getInterpolation(value / 0.25f);
                    } else {
                        value -= 0.75f;
                        scale = 0.9f + 0.1f * CubicBezierInterpolator.EASE_OUT.getInterpolation(value / 0.25f);
                    }
                    iconLayout.setScaleX(scale);
                    iconLayout.setScaleY(scale);
                }
            });
            animator.setDuration(360);
            animator.start();

            views.put(marker, frameLayout);

            googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 300, null);
        }

        public void removeInfoView(Marker marker) {
            View view = views.get(marker);
            if (view != null) {
                removeView(view);
                views.remove(marker);
            }
        }

        public void updatePositions() {
            if (googleMap == null) {
                return;
            }
            Projection projection = googleMap.getProjection();
            for (HashMap.Entry<Marker, View> entry : views.entrySet()) {
                Marker marker = entry.getKey();
                View view = entry.getValue();
                Point point = projection.toScreenLocation(marker.getPosition());
                view.setTranslationX(point.x - view.getMeasuredWidth() / 2);
                view.setTranslationY(point.y - view.getMeasuredHeight() + AndroidUtilities.dp(22));
            }
        }
    }

    @Override
    protected ActionBar createActionBar(Context context) {
        ActionBar actionBar   = new ActionBar(context);
        actionBar.setBackButtonDrawable(RideUtils.createMenuDrawable(R.drawable.ic_ab_back));
        actionBar.setBackgroundDrawable(null);
        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet());
        actionBar.setTitle("");
        actionBar.getTitleTextView().setAlpha(0.0f);
        if (!AndroidUtilities.isTablet()) {
            actionBar.showActionModeTop();
        }
        return actionBar;
    }


    @Override
    public View createView(Context context) {

       ActionBarMenu menu =  actionBar.createMenu();
       actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
           @Override
           public void onItemClick(int id) {
               if(id == -1){
                   finishFragment();
               }
           }
       });
       menu.addItem(1,RideUtils.createMenuDrawable(menuDrawable = new MenuDrawable()));


        fragmentView = new FrameLayout(context) {
            private boolean first = true;

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateClipView(true);

            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));


        mapViewClip = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                if (overlayView != null) {
                    overlayView.updatePositions();
                }
            }
        };
        mapViewClip.setBackgroundDrawable(new MapPlaceholderDrawable());
        frameLayout.addView(mapViewClip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));


        locationButton = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_location_actionBackground), Theme.getColor(Theme.key_location_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(40), AndroidUtilities.dp(40));
            drawable = combinedDrawable;
        } else {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(locationButton, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(locationButton, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            locationButton.setStateListAnimator(animator);
            locationButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(40), AndroidUtilities.dp(40));
                }
            });
        }
        locationButton.setBackgroundDrawable(drawable);
        locationButton.setImageResource(R.drawable.location_current);
        locationButton.setScaleType(ImageView.ScaleType.CENTER);
        locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
        locationButton.setTag(Theme.key_location_actionActiveIcon);
        locationButton.setContentDescription(LocaleController.getString("AccDescrMyLocation", R.string.AccDescrMyLocation));


        FrameLayout.LayoutParams layoutParams1 = LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 40 : 44, Build.VERSION.SDK_INT >= 21 ? 40 : 44, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 12, 12);
        mapViewClip.addView(locationButton, layoutParams1);


        mapView = new MapView(context);
        final MapView map = mapView;
        new Thread(() -> {
            try {
                map.onCreate(null);
            } catch (Exception e) {
                //this will cause exception, but will preload google maps?
            }
            AndroidUtilities.runOnUIThread(() -> {
                if (mapView != null && getParentActivity() != null) {
                    try {
                        map.onCreate(null);
                        MapsInitializer.initialize(ApplicationLoader.applicationContext);
                        mapView.getMapAsync(map1 -> {
                            googleMap = map1;
                            if (isActiveThemeDark()) {
                                currentMapStyleDark = true;
                                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(ApplicationLoader.applicationContext, R.raw.mapstyle_night);
                                googleMap.setMapStyle(style);
                            }
                            googleMap.setPadding(AndroidUtilities.dp(70), 0, AndroidUtilities.dp(70), AndroidUtilities.dp(10));
                            onMapInit();
                        });
                        mapsInitialized = true;
                        if (onResumeCalled) {
                            mapView.onResume();
                        }
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            });
        }).start();


        frameLayout.addView(actionBar);

        setPickUpButton = new TextView(context);
        setPickUpButton.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        setPickUpButton.setText("Set Pickup");
        setPickUpButton.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(8),AndroidUtilities.dp(16),AndroidUtilities.dp(8));
        setPickUpButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        setPickUpButton.setGravity(Gravity.CENTER);
        setPickUpButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        mapViewClip.addView(setPickUpButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER|Gravity.BOTTOM));


        return fragmentView;
    }


    private void updateClipView(boolean fromLayout) {

//        if(mapView != null && mapView.getVisibility() == View.VISIBLE){
//            mapViewClip.setVisibility(View.INVISIBLE);
//
//        }else{
//            mapViewClip.setVisibility(View.VISIBLE);
//        }


    }

    private void onMapInit() {
        if (googleMap == null) {
            return;
        }
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            FileLog.e(e);
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.setOnCameraMoveListener(() -> {
            if (overlayView != null) {
                overlayView.updatePositions();
            }
        });

        if (checkGpsEnabled && getParentActivity() != null) {
            checkGpsEnabled = false;
            checkGpsEnabled();
        }

        updateClipView(false);
    }

    private boolean checkGpsEnabled() {
        if (!getParentActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            return true;
        }
        try {
            LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("GpsDisabledAlertTitle", R.string.GpsDisabledAlertTitle));
                builder.setMessage(LocaleController.getString("GpsDisabledAlertText", R.string.GpsDisabledAlertText));
                builder.setPositiveButton(LocaleController.getString("ConnectingToProxyEnable", R.string.ConnectingToProxyEnable), (dialog, id) -> {
                    if (getParentActivity() == null) {
                        return;
                    }
                    try {
                        getParentActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } catch (Exception ignore) {

                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
                return false;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return true;
    }


    @Override
    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
        if (isOpen && !backward) {
            try {
                if (mapView.getParent() instanceof ViewGroup) {
                    ViewGroup viewGroup = (ViewGroup) mapView.getParent();
                    viewGroup.removeView(mapView);
                }
            } catch (Exception ignore) {

            }
            if (mapViewClip != null) {
                mapViewClip.addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,  AndroidUtilities.dp(10), Gravity.TOP | Gravity.LEFT));
                if (overlayView != null) {
                    try {
                        if (overlayView.getParent() instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) overlayView.getParent();
                            viewGroup.removeView(overlayView);
                        }
                    } catch (Exception ignore) {

                    }
                    mapViewClip.addView(overlayView, 1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,  AndroidUtilities.dp(10), Gravity.TOP | Gravity.LEFT));
                }
            } else if (fragmentView != null) {
                ((FrameLayout) fragmentView).addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
            }
        }
    }



    private boolean isActiveThemeDark() {
        Theme.ThemeInfo info = Theme.getActiveTheme();
        if (info.isDark()) {
            return true;
        }
        int color = Theme.getColor(Theme.key_windowBackgroundWhite);
        return AndroidUtilities.computePerceivedBrightness(color) < 0.721f;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }
}
