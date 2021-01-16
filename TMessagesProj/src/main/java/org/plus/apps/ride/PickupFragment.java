package org.plus.apps.ride;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.EntityDeletionOrUpdateAdapter;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.ride.data.RideObjects;
import org.plus.apps.ride.drawroutemap.DrawMarker;
import org.plus.apps.ride.drawroutemap.DrawRouteMaps;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Adapters.LocationActivityAdapter;
import org.telegram.ui.Adapters.LocationActivitySearchAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Cells.LocationDirectionCell;
import org.telegram.ui.Cells.LocationLoadingCell;
import org.telegram.ui.Cells.LocationPoweredCell;
import org.telegram.ui.Cells.SendLocationCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.SharingLiveLocationCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MapPlaceholderDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.LocationActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static org.plus.apps.ride.RideUtils.getLocationInLatLngRad;

public class PickupFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private ImageView locationButton;
    private ImageView backButton;
    private BackupImageView profileImageView;

    private GoogleMap googleMap;
    private CameraUpdate moveToBounds;
    private MapView mapView;
    private CameraUpdate forceUpdate;
    private float yOffset;

    private FrameLayout mapViewClip;
    private LocationAdapter adapter;
    private RecyclerListView listView;
    private RecyclerListView searchListView;
    private LocationActivitySearchAdapter searchAdapter;
    private View markerImageView;
    private LinearLayoutManager layoutManager;

    private boolean userLocationMoved;
    private Location userLocation;

    private boolean currentMapStyleDark;

    private boolean checkGpsEnabled = true;

    private boolean isFirstLocation = true;

    private boolean firstFocus = true;

    private AnimatorSet animatorSet;

    private boolean checkPermission = true;
    private int askWithRadius;

    private boolean searching;
    private boolean searchWas;
    private boolean searchInProgress;

    private boolean wasResults;
    private boolean mapsInitialized;
    private boolean onResumeCalled;

    private boolean firstWas;


    private Location myLocation;

    private MapOverlayView overlayView;

    private ActionBarMenuItem searchItem;
    private ActionBarMenuItem menuItem;

    private Drawable shadowDrawable;
    private View shadow;

    private LinearLayout emptyView;
    private ImageView emptyImageView;
    private TextView emptyTitleTextView;
    private TextView emptySubtitleTextView;

    private boolean scrolling;

    private int markerTop;


    private int overScrollHeight = AndroidUtilities.displaySize.x - ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(66);

    //view on the top of map view
    public class MapOverlayView extends FrameLayout {

        private HashMap<Marker, View> views = new HashMap<>();

        public MapOverlayView(Context context) {
            super(context);
        }

        public void addInfoView(Marker marker) {

            Driver location = (Driver) marker.getTag();
            if (location == null) {
                return;
            }
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

    private void drawRoute(RideObjects.SearchLocation start, RideObjects.SearchLocation end){
        if(start == null || end == null || getParentActivity() == null){
            return;
        }
        markerImageView.setVisibility(View.GONE);
        LatLng origin = new LatLng(start._lat ,start._long);
        LatLng destination = new LatLng(end._lat ,end._long);
        RideController.getInstance(currentAccount).getDirection(origin, destination, new RideController.ResponseDelegate() {
            @Override
            public void run(Object response, APIError error) {
                if(error == null){
                    try {
                        AndroidUtilities.runOnUIThread(() -> {
                            PolylineOptions lineOptions;
                            ArrayList<LatLng> points;
                            lineOptions = null;
                            List<List<HashMap<String, String>>>  result = (List<List<HashMap<String, String>>>)response;
                            for (int i = 0; i < result.size(); i++) {
                                points = new ArrayList<>();
                                lineOptions = new PolylineOptions();

                                // Fetching i-th route
                                List<HashMap<String, String>> path = result.get(i);

                                // Fetching all the points in i-th route
                                for (int j = 0; j < path.size(); j++) {
                                    HashMap<String, String> point = path.get(j);

                                    double lat = Double.parseDouble(point.get("lat"));
                                    double lng = Double.parseDouble(point.get("lng"));
                                    LatLng position = new LatLng(lat, lng);

                                    points.add(position);
                                }

                                // Adding all the points in the route to LineOptions
                                lineOptions.addAll(points);
                                lineOptions.width(6);
                                int  routeColor = Theme.getColor(Theme.key_actionBarTabLine);
                                if (routeColor == 0)
                                    lineOptions.color(0xFF0A8F08);
                                else
                                    lineOptions.color(routeColor);
                            }

                            // Drawing polyline in the Google Map for the i-th route
                            if (lineOptions != null && googleMap != null) {
                                googleMap.addPolyline(lineOptions);
                               // LatLngBounds latLngBounds = new LatLngBounds(origin,destination);

                               // CameraUpdate position = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), googleMap.getMaxZoomLevel() - 4);

                               // googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,AndroidUtilities.displaySize.x,overScrollHeight,16));
                               //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom());
                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(origin));
                            } else {
                                Log.d("onPostExecute", "without Polylines draw");
                            }


                        });

                    }catch (Exception ignore){

                    }

                }
            }
        });
    }

    public PickupFragment() {
        super();
        AndroidUtilities.fixGoogleMapsBug();
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.locationPermissionGranted);
        return super.onFragmentCreate();
    }

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

        if (adapter != null) {
            adapter.destroy();
        }
        if (searchAdapter != null) {
            searchAdapter.destroy();
        }
        super.onFragmentDestroy();
    }


    private ActionBarMenuItem profileItem;


    private void createBackImageView(Context context){
        backButton = new ImageView(context);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_location_actionBackground), Theme.getColor(Theme.key_location_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(40), AndroidUtilities.dp(40));
            drawable = combinedDrawable;
        } else {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(backButton, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(backButton, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            backButton.setStateListAnimator(animator);
            backButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(40), AndroidUtilities.dp(40));
                }
            });
        }
        backButton.setBackgroundDrawable(drawable);
        backButton.setImageResource(R.drawable.ic_ab_back);
        backButton.setScaleType(ImageView.ScaleType.CENTER);
        backButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
        backButton.setTag(Theme.key_location_actionActiveIcon);
        //backButton.setContentDescription(LocaleController.getString("AccDescrMyLocation", R.string.AccDescrMyLocation));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishFragment();
            }
        });



    }
    private boolean searchedForCustomLocations;

    @Override
    public View createView(Context context) {
        searchWas = false;
        searching = false;
        searchInProgress = false;
        if (adapter != null) {
            adapter.destroy();
        }
        if (searchAdapter != null) {
            searchAdapter.destroy();
        }

       // actionBar.setBackButtonDrawable(ShopUtils.createDetailMenuDrawable(R.drawable.ic_ab_back));
        actionBar.setTitle("");
       // actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setBackground(null);
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText),false);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet());



        ActionBarMenu menu = actionBar.createMenu();
        profileItem = menu.addItemWithWidth(1, 0, AndroidUtilities.dp(56));
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setTextSize(AndroidUtilities.dp(12));

        BackupImageView profileImageView = new BackupImageView(context){

            @Override
            protected void onDraw(Canvas canvas) {
                canvas.drawCircle(getMeasuredWidth()/2,getMeasuredHeight()/2,AndroidUtilities.dp(21),Theme.avatar_backgroundPaint);
                super.onDraw(canvas);
            }
        };
        profileImageView.setSize(AndroidUtilities.dp(32),AndroidUtilities.dp(32));
        profileImageView.setRoundRadius(AndroidUtilities.dp(16));
        profileItem.addView(profileImageView, LayoutHelper.createFrame(42, 42, Gravity.CENTER));

        TLRPC.User user = getUserConfig().getCurrentUser();
        avatarDrawable.setInfo(user);
        profileImageView.getImageReceiver().setCurrentAccount(currentAccount);
        profileImageView.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        userLocation = new Location("network");

        overlayView = new MapOverlayView(context);

        fragmentView = new FrameLayout(context) {
            private boolean first = true;
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                if (changed) {
                    fixLayoutInternal(first);
                    first = false;
                } else {
                    updateClipView(true);
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));
        Rect padding = new Rect();
        shadowDrawable.getPadding(padding);

        FrameLayout.LayoutParams layoutParams;
        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(21) + padding.top);
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;

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

        createBackImageView(context);
        frameLayout.addView(backButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 40 : 44,Build.VERSION.SDK_INT >= 21 ? 40 : 44,Gravity.TOP|Gravity.LEFT,16,48,0,0));


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
        locationButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23) {
                Activity activity = getParentActivity();
                if (activity != null) {
                    if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        showPermissionAlert(false);
                        return;
                    }
                }
            }
            if (!checkGpsEnabled()) {
                return;
            }
            if (myLocation != null && googleMap != null) {
                locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
                locationButton.setTag(Theme.key_location_actionActiveIcon);
                adapter.setCustomLocation(null);
                userLocationMoved = false;
                //showSearchPlacesButton(false);
               // googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));

                CameraUpdate position = CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), googleMap.getMaxZoomLevel() - 4);
                googleMap.animateCamera(position);

                if (searchedForCustomLocations) {
                    if (myLocation != null) {
                        adapter.searchPlacesWithQuery(null, myLocation, true, true);
                    }
                    searchedForCustomLocations = false;
                   // showResults();
                }
            }
           // removeInfoView();
        });


        FrameLayout.LayoutParams layoutParams1 = LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 40 : 44, Build.VERSION.SDK_INT >= 21 ? 40 : 44, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 12, 12);
        layoutParams1.bottomMargin += layoutParams.height - padding.top;
        mapViewClip.addView(locationButton, layoutParams1);

        createEmptyView(context,frameLayout);
        createListView(context,frameLayout);
        frameLayout.addView(mapViewClip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        createMapView(context);

        if (markerImageView == null) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.map_pin2);
            mapViewClip.addView(imageView, LayoutHelper.createFrame(28, 48, Gravity.TOP | Gravity.CENTER_HORIZONTAL));
            markerImageView = imageView;
        }

        createSearchListView(context,frameLayout);

        shadow = new View(context) {

            private RectF rect = new RectF();

            @Override
            protected void onDraw(Canvas canvas) {
                shadowDrawable.setBounds(-padding.left, 0, getMeasuredWidth() + padding.right, getMeasuredHeight());
                shadowDrawable.draw(canvas);
                if (true) {
                    int w = AndroidUtilities.dp(36);
                    int y = padding.top + AndroidUtilities.dp(10);
                    rect.set((getMeasuredWidth() - w) / 2, y, (getMeasuredWidth() + w) / 2, y + AndroidUtilities.dp(4));
                    int color = Theme.getColor(Theme.key_sheet_scrollUp);
                    int alpha = Color.alpha(color);
                    Theme.dialogs_onlineCirclePaint.setColor(color);
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(2), AndroidUtilities.dp(2), Theme.dialogs_onlineCirclePaint);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= 21) {
            shadow.setTranslationZ(AndroidUtilities.dp(6));
        }
        mapViewClip.addView(shadow, layoutParams);

        frameLayout.addView(actionBar);
        updateEmptyView();

        return fragmentView;
    }

    private void showMenu(){
        if(fragmentView == null || getParentActivity() == null){
            return;
        }
       FrameLayout frameLayout = (FrameLayout) fragmentView;

    }

    private void showPermissionAlert(boolean byButton) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
        if (byButton) {
            builder.setMessage(LocaleController.getString("PermissionNoLocationPosition", R.string.PermissionNoLocationPosition));
        } else {
            builder.setMessage(LocaleController.getString("PermissionNoLocation", R.string.PermissionNoLocation));
        }
        builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), (dialog, which) -> {
            if (getParentActivity() == null) {
                return;
            }
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                getParentActivity().startActivity(intent);
            } catch (Exception e) {
                FileLog.e(e);
            }
        });
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }


    private void updateEmptyView() {
        if (searching) {
            if (searchInProgress) {
                searchListView.setEmptyView(null);
                emptyView.setVisibility(View.GONE);
                searchListView.setVisibility(View.GONE);
            } else {
                searchListView.setEmptyView(emptyView);
            }
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void createListView(Context context,FrameLayout frameLayout){

        listView = new RecyclerListView(context);
        listView.setAdapter(adapter = new LocationAdapter(context, 1, true));
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(position == adapter.whereRow){
                    RideObjects.SearchLocation address = new RideObjects.SearchLocation();;
                    address.location = userLocation;
                    address._lat  = userLocation.getLatitude();
                    address._long = userLocation.getLongitude();
                    address.address = "Uknown Address";
                    RideLocationSetFragment fragment = new RideLocationSetFragment( address);
                    fragment.setDelegate((pickup, dest) -> {
                        drawRoute(pickup,dest);
                        mapView.setEnabled(false);
                    });
                    presentFragment(fragment);




//                    CarsChooserAlert carsChooserAlert = new CarsChooserAlert(context,false);
//                    carsChooserAlert.setDelegate(new CarsChooserAlert.CarsAlertDelegate() {
//                        @Override
//                        public void didCarSelected(RideData.Car car) {
//
//                        }
//                    });
//                    showDialog(carsChooserAlert);
                }
            }
        });
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                scrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
                if (!scrolling && forceUpdate != null) {
                    forceUpdate = null;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                  updateClipView(false);
                if (forceUpdate != null) {
                    yOffset += dy;
                }
            }
        });
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        //

    }



    private void createEmptyView(Context context,FrameLayout frameLayout){
        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyView.setPadding(0, AndroidUtilities.dp(60 + 100), 0, 0);
        emptyView.setVisibility(View.GONE);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyView.setOnTouchListener((v, event) -> true);

        emptyImageView = new ImageView(context);
        emptyImageView.setImageResource(R.drawable.location_empty);
        emptyImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogEmptyImage), PorterDuff.Mode.MULTIPLY));
        emptyView.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        emptyTitleTextView = new TextView(context);
        emptyTitleTextView.setTextColor(Theme.getColor(Theme.key_dialogEmptyText));
        emptyTitleTextView.setGravity(Gravity.CENTER);
        emptyTitleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        emptyTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        emptyTitleTextView.setText(LocaleController.getString("NoPlacesFound", R.string.NoPlacesFound));
        emptyView.addView(emptyTitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 11, 0, 0));



    }

    private void createMapView(Context context){
        mapView = new MapView(context) {

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return super.onTouchEvent(event);
            }

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                MotionEvent eventToRecycle = null;
                if (yOffset != 0) {
                    ev = eventToRecycle = MotionEvent.obtain(ev);
                    eventToRecycle.offsetLocation(0, -yOffset / 2);
                }
                boolean result = super.dispatchTouchEvent(ev);
                if (eventToRecycle != null) {
                    eventToRecycle.recycle();
                }
                return result;
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                    }
                    animatorSet = new AnimatorSet();
                    animatorSet.setDuration(200);
                    animatorSet.playTogether(ObjectAnimator.ofFloat(markerImageView, View.TRANSLATION_Y, markerTop - AndroidUtilities.dp(10)));
                    animatorSet.start();
                } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                    if (animatorSet != null) {
                        animatorSet.cancel();
                    }
                    yOffset = 0;
                    animatorSet = new AnimatorSet();
                    animatorSet.setDuration(200);
                    animatorSet.playTogether(ObjectAnimator.ofFloat(markerImageView, View.TRANSLATION_Y, markerTop));
                    animatorSet.start();
                    adapter.fetchLocationAddress();
                }


                if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!userLocationMoved) {
                        locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionIcon), PorterDuff.Mode.MULTIPLY));
                        locationButton.setTag(Theme.key_location_actionIcon);
                        userLocationMoved = true;
                    }
                    if (googleMap != null) {
                        if (userLocation != null) {
                            userLocation.setLatitude(googleMap.getCameraPosition().target.latitude);
                            userLocation.setLongitude(googleMap.getCameraPosition().target.longitude);
                        }
                    }
                   adapter.setCustomLocation(userLocation);
                   // loadDrivers(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()));
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                AndroidUtilities.runOnUIThread(() -> {
                    if (moveToBounds != null) {
                        googleMap.moveCamera(moveToBounds);
                        moveToBounds = null;
                    }
                });
            }
        };
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
    }

    private void createSearchListView(Context context,FrameLayout frameLayout){
        searchListView = new RecyclerListView(context);
        searchListView.setVisibility(View.GONE);
        searchListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        searchAdapter = new LocationActivitySearchAdapter(context) {
            @Override
            public void notifyDataSetChanged() {
                if (searchItem != null) {
                    searchItem.setShowSearchProgress(searchAdapter.isSearching());
                }
                if (emptySubtitleTextView != null) {
                    emptySubtitleTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("NoPlacesFoundInfo", R.string.NoPlacesFoundInfo, searchAdapter.getLastSearchString())));
                }
                super.notifyDataSetChanged();
            }
        };
        searchAdapter.setDelegate(0, places -> {
            searchInProgress = false;
            updateEmptyView();
        });
        frameLayout.addView(searchListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
        searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
            }
        });
    }

    private void onMapInit(){
        if(googleMap == null){
            return;
        }
        LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        try {
            googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin2)));
        } catch (Exception e) {
            FileLog.e(e);
        }
        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 4);
        googleMap.moveCamera(position);
        firstFocus = false;

//        userLocation = new Location("network");
//        userLocation.setLatitude(20.659322);
//        userLocation.setLongitude(-11.406250);



//        if (initialLocation != null) {
//            LatLng latLng = new LatLng(initialLocation.geo_point.lat, initialLocation.geo_point._long);
//            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 4));
//            userLocation.setLatitude(initialLocation.geo_point.lat);
//            userLocation.setLongitude(initialLocation.geo_point._long);
//            adapter.setCustomLocation(userLocation);
//        } else {
//            userLocation.setLatitude(20.659322);
//            userLocation.setLongitude(-11.406250);
//        }
//
       // getRecentLocations();

        try {
            googleMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            FileLog.e(e);
        }

        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);

//        googleMap.setOnCameraMoveStartedListener(reason -> {
//            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
//               // showSearchPlacesButton(true);
//                //removeInfoView();
//
//                CameraPosition cameraPosition = googleMap.getCameraPosition();
//                forceUpdate = CameraUpdateFactory.newLatLngZoom(cameraPosition.target, cameraPosition.zoom);
//                loadDrivers(cameraPosition.target);
//            }
//        });


        googleMap.setOnMyLocationChangeListener(location -> {
            positionMarker(location);
            getLocationController().setGoogleMapLocation(location, isFirstLocation);
            isFirstLocation = false;
        });

//        googleMap.setOnMarkerClickListener(marker -> {
//            if (!(marker.getTag() instanceof Driver)) {
//                return true;
//            }
//            markerImageView.setVisibility(View.INVISIBLE);
//            if (!userLocationMoved) {
//                locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionIcon), PorterDuff.Mode.MULTIPLY));
//                locationButton.setTag(Theme.key_location_actionIcon);
//                userLocationMoved = true;
//            }
//            overlayView.addInfoView(marker);
//            return true;
//        });

        googleMap.setOnCameraMoveListener(() -> {
            if (overlayView != null) {
                overlayView.updatePositions();
            }
        });


        positionMarker(myLocation = getLastLocation());


        if (checkGpsEnabled && getParentActivity() != null) {
            checkGpsEnabled = false;
            checkGpsEnabled();
        }



      //  loadDrivers(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));

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


    private boolean loadingDriver;
    private void loadDrivers(LatLng latLng){
        if(loadingDriver){
            return;
        }
        loadingDriver  = true;
        ArrayList<Driver> drivers = new ArrayList<>();
        for (int a = 0; a < 10; a ++){
            Driver driver  = new Driver();
            driver.id = a;
            driver._lat = latLng.latitude;
            driver._long = latLng.longitude;
            drivers.add(driver);
        }
        updatePlacesMarkers(drivers);
    }


    public static class Driver {
        public int id;
        public Marker marker;
        public double _lat;
        public double _long;
    }

    private ArrayList<Driver> placeMarkers = new ArrayList<>();

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context,vectorDrawableResourceId);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }



    public void moveCars(){
        for (int a = 0, N = placeMarkers.size(); a < N; a++) {
          Driver driver  =   placeMarkers.get(a);
          if(driver != null && driver.marker != null){
              Location location = userLocation;
              location.setLatitude(driver._lat);
              location.setLongitude(driver._long);
              animateMarkerNew(location,driver.marker);
              break;
          }
        }


    }




    private void updatePlacesMarkers(ArrayList<Driver> places) {
        if (places == null) {
            return;
        }
        for (int a = 0, N = placeMarkers.size(); a < N; a++) {
            placeMarkers.get(a).marker.remove();
        }
        placeMarkers.clear();
        for (int a = 0, N = places.size(); a < N; a++) {
            Driver venue = places.get(a);

            try {
                Location location = new Location("network");
                location.setLongitude(venue._long);
                location.setLatitude(venue._lat);
                location =  getLocationInLatLngRad(200,location);
                MarkerOptions options = new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude()));
                options.icon(bitmapDescriptorFromVector(getParentActivity(),R.drawable.car));
                options.anchor(0.5f, 0.5f);
                options.flat(false);
                venue.marker = googleMap.addMarker(options);
                placeMarkers.add(venue);

            } catch (Exception e) {
                Log.i("berhanzak",e.getMessage());

                FileLog.e(e);
            }
           // googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(venue._lat + 0.1, venue._long + 0.1)));

        }
        loadingDriver = false;
        moveCars();
    }

    private interface LatLngInterpolatorNew {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolatorNew {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }
    private Map<String, Boolean> animating = new HashMap<>();
    private void animateMarkerNew(final Location destination, final Marker marker) {
        if(marker == null){
            return;
        }
        Boolean animateOnprogress = animating.get(marker.getId());
        if(animateOnprogress != null && animateOnprogress){
            return;
        }
        animating.put(marker.getId(),true);
        final LatLng startPosition = marker.getPosition();
       Location loc =  getLocationInLatLngRad(30,destination);
        final LatLng endPosition = new LatLng(loc.getLatitude(),loc.getLongitude());

        final float startRotation = marker.getRotation();
        final LatLngInterpolatorNew latLngInterpolator = new LatLngInterpolatorNew.LinearFixed();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(3000); // duration 3 second
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    marker.setPosition(newPosition);
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(newPosition)
                            .zoom(15.5f)
                            .build()));

                    marker.setRotation(getBearing(startPosition, new LatLng(destination.getLatitude(), destination.getLongitude())));
                } catch (Exception ignore) {
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);


                animating.remove(marker.getId(),true);

                // if (mMarker != null) {
                //      mMarker.remove();
                // }
                 googleMap.addMarker(new MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

            }
        });
        valueAnimator.start();
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }
    private Bitmap[] bitmapCache = new Bitmap[7];
    private Bitmap createPlaceBitmap(int num) {
        if (bitmapCache[num % 7] != null) {
            return bitmapCache[num % 7];
        }
        try {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(0xffffffff);
            Bitmap bitmap = Bitmap.createBitmap(AndroidUtilities.dp(12), AndroidUtilities.dp(12), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawCircle(AndroidUtilities.dp(6), AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);
            paint.setColor(LocationCell.getColorForIndex(num));
            canvas.drawCircle(AndroidUtilities.dp(6), AndroidUtilities.dp(6), AndroidUtilities.dp(5), paint);
            canvas.setBitmap(null);
            return bitmapCache[num % 7] = bitmap;
        } catch (Throwable e) {
            FileLog.e(e);
        }
        return null;
    }


    private void positionMarker(Location location) {
        if (location == null) {
            return;
        }
        myLocation = new Location(location);
        if ( googleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (adapter != null) {
                adapter.searchPlacesWithQuery(null, myLocation, true);
                adapter.setGpsLocation(myLocation);
            }
            if (!userLocationMoved) {
                userLocation = new Location(location);
                if (firstWas) {
                    CameraUpdate position = CameraUpdateFactory.newLatLng(latLng);
                    googleMap.animateCamera(position);
                } else {
                    firstWas = true;
                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 4);
                    googleMap.moveCamera(position);
                }
            }
        } else {
            adapter.setGpsLocation(myLocation);
        }
    }


    private Location getLastLocation() {
        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) {
                break;
            }
        }
        return l;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.locationPermissionGranted) {
            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null && mapsInitialized) {
            try {
                mapView.onPause();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        onResumeCalled = false;

    }

    private void updateClipView(boolean fromLayout) {
        int height = 0;
        int top;
        RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(0);//the firt empety cell
        if (holder != null) {
            top = (int) holder.itemView.getY();//top y postion
            height = overScrollHeight + (Math.min(top, 0));
        } else {
            top = -mapViewClip.getMeasuredHeight();
        }

        //top reprsent the top posation of the first child in list view
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mapViewClip.getLayoutParams();
        if (layoutParams != null) {
            if (height <= 0) {
                //negetive height
                if (mapView.getVisibility() == View.VISIBLE) {
                    mapView.setVisibility(View.INVISIBLE);
                    mapViewClip.setVisibility(View.INVISIBLE);
                    if (overlayView != null) {
                        overlayView.setVisibility(View.INVISIBLE);
                    }
                }
            } else {
                if (mapView.getVisibility() == View.INVISIBLE) {
                    mapView.setVisibility(View.VISIBLE);
                    mapViewClip.setVisibility(View.VISIBLE);
                    if (overlayView != null) {
                        overlayView.setVisibility(View.VISIBLE);
                    }
                }
            }

            mapViewClip.setTranslationY(Math.min(0, top));//settig negetvie transation y
            mapView.setTranslationY(Math.max(0, -top / 2));
            if (overlayView != null) {
                overlayView.setTranslationY(Math.max(0, -top / 2));
            }

            if (markerImageView != null) {
                markerImageView.setTranslationY(markerTop = -top - AndroidUtilities.dp(markerImageView.getTag() == null ? 48 : 69) + height / 2);
            }
            if (!fromLayout) {
                layoutParams = (FrameLayout.LayoutParams) mapView.getLayoutParams();
                if (layoutParams != null && layoutParams.height != overScrollHeight + AndroidUtilities.dp(10)) {
                    layoutParams.height = overScrollHeight + AndroidUtilities.dp(10);
                    if (googleMap != null) {
                        googleMap.setPadding(AndroidUtilities.dp(70), 0, AndroidUtilities.dp(70), AndroidUtilities.dp(10));
                    }
                    mapView.setLayoutParams(layoutParams);
                }
                if (overlayView != null) {
                    layoutParams = (FrameLayout.LayoutParams) overlayView.getLayoutParams();
                    if (layoutParams != null && layoutParams.height != overScrollHeight + AndroidUtilities.dp(10)) {
                        layoutParams.height = overScrollHeight + AndroidUtilities.dp(10);
                        overlayView.setLayoutParams(layoutParams);
                    }
                }
            }
        }
    }

    private void fixLayoutInternal(final boolean resume) {
        if (listView != null) {
            int height = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
            height = 0;
            int viewHeight = fragmentView.getMeasuredHeight();
            if (viewHeight == 0) {
                return;
            }
            overScrollHeight = viewHeight - AndroidUtilities.dp(60) - height - AndroidUtilities.dp(72) - AndroidUtilities.dp(34);


            //over height = the height of for the map that is valiable to play with,

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.topMargin = height;
            listView.setLayoutParams(layoutParams);//making sure list view height is bellow the action bar
            layoutParams = (FrameLayout.LayoutParams) mapViewClip.getLayoutParams();
            layoutParams.topMargin = height;
            layoutParams.height = overScrollHeight;//setting the height of the mapviewcliup
            mapViewClip.setLayoutParams(layoutParams);
            if (searchListView != null) {
                layoutParams = (FrameLayout.LayoutParams) searchListView.getLayoutParams();
                layoutParams.topMargin = height;
                searchListView.setLayoutParams(layoutParams);
            }

            adapter.setOverScrollHeight(overScrollHeight);//pass the height of the the map for the adapter  so the
            layoutParams = (FrameLayout.LayoutParams) mapView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.height = overScrollHeight + AndroidUtilities.dp(10); // playing growund  for mapview adding 10 extra space for , i am not sure what it is
                if (googleMap != null) {
                    //setting padding for google map to mek sure the google logo is seen - must requiremnt
                    googleMap.setPadding(AndroidUtilities.dp(70), 0, AndroidUtilities.dp(70), AndroidUtilities.dp(10));
                }
                mapView.setLayoutParams(layoutParams);
            }

            if (overlayView != null) {
                layoutParams = (FrameLayout.LayoutParams) overlayView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = overScrollHeight + AndroidUtilities.dp(10);
                    overlayView.setLayoutParams(layoutParams);
                }
            }
            adapter.notifyDataSetChanged();

            if (resume) {
                //either firset time or resuming this activiti
                int top;
                top = 73;

                //detting defulat scsroll postion of the list iew to 0 with offset value of -top value
                layoutManager.scrollToPositionWithOffset(0, -AndroidUtilities.dp(top));
                updateClipView(false);
                listView.post(() -> {
                    layoutManager.scrollToPositionWithOffset(0, -AndroidUtilities.dp(top));
                    updateClipView(false);
                });
            } else {
                //user has changed the postion so update it manualy
                updateClipView(false);
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
        if (mapView != null && mapsInitialized) {
            try {
                mapView.onResume();
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
        onResumeCalled = true;
        if (googleMap != null) {
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        fixLayoutInternal(true);
        if (checkPermission && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null && mapsInitialized) {
            mapView.onLowMemory();
        }
    }


    @Override
    public boolean onBackPressed() {

        return super.onBackPressed();
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
                mapViewClip.addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, overScrollHeight + AndroidUtilities.dp(10), Gravity.TOP | Gravity.LEFT));
                if (overlayView != null) {
                    try {
                        if (overlayView.getParent() instanceof ViewGroup) {
                            ViewGroup viewGroup = (ViewGroup) overlayView.getParent();
                            viewGroup.removeView(overlayView);
                        }
                    } catch (Exception ignore) {

                    }
                    mapViewClip.addView(overlayView, 1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, overScrollHeight + AndroidUtilities.dp(10), Gravity.TOP | Gravity.LEFT));
                }
                updateClipView(false);
            } else if (fragmentView != null) {
                ((FrameLayout) fragmentView).addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {

            shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));
            shadow.invalidate();

            if (googleMap != null) {
                if (isActiveThemeDark()) {
                    if (!currentMapStyleDark) {
                        currentMapStyleDark = true;
                        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(ApplicationLoader.applicationContext, R.raw.mapstyle_night);
                        googleMap.setMapStyle(style);

                    }
                } else {
                    if (currentMapStyleDark) {
                        currentMapStyleDark = false;
                        googleMap.setMapStyle(null);

                    }
                }
            }
        };



        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, cellDelegate, Theme.key_dialogBackground));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_dialogBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_dialogButtonSelector));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_chat_messagePanelHint));
        themeDescriptions.add(new ThemeDescription(searchItem != null ? searchItem.getSearchField() : null, ThemeDescription.FLAG_CURSORCOLOR, null, null, null, null, Theme.key_dialogTextBlack));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, cellDelegate, Theme.key_actionBarDefaultSubmenuBackground));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, cellDelegate, Theme.key_actionBarDefaultSubmenuItem));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM | ThemeDescription.FLAG_IMAGECOLOR, null, null, null, cellDelegate, Theme.key_actionBarDefaultSubmenuItemIcon));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(emptyImageView, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_dialogEmptyImage));
        themeDescriptions.add(new ThemeDescription(emptyTitleTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_dialogEmptyText));
        themeDescriptions.add(new ThemeDescription(emptySubtitleTextView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_dialogEmptyText));
        themeDescriptions.add(new ThemeDescription(shadow, 0, null, null, null, null, Theme.key_sheet_scrollUp));

        themeDescriptions.add(new ThemeDescription(locationButton, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_location_actionIcon));
        themeDescriptions.add(new ThemeDescription(locationButton, ThemeDescription.FLAG_IMAGECOLOR | ThemeDescription.FLAG_CHECKTAG, null, null, null, null, Theme.key_location_actionActiveIcon));
        themeDescriptions.add(new ThemeDescription(locationButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_location_actionBackground));
        themeDescriptions.add(new ThemeDescription(locationButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_location_actionPressedBackground));


        themeDescriptions.add(new ThemeDescription(null, 0, null, null, Theme.avatarDrawables, cellDelegate, Theme.key_avatar_text));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink));

        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_location_liveLocationProgress));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_location_placeLocationBackground));
        themeDescriptions.add(new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialog_liveLocationProgress));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_CHECKTAG, new Class[]{SendLocationCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_location_sendLocationIcon));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_CHECKTAG, new Class[]{SendLocationCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_location_sendLiveLocationIcon));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_CHECKTAG, new Class[]{SendLocationCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_location_sendLocationBackground));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_CHECKTAG, new Class[]{SendLocationCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_location_sendLiveLocationBackground));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{SendLocationCell.class}, new String[]{"accurateTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{SendLocationCell.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_location_sendLiveLocationText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{SendLocationCell.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_location_sendLocationText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationDirectionCell.class}, new String[]{"buttonTextView"}, null, null, null, Theme.key_featuredStickers_buttonText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE, new Class[]{LocationDirectionCell.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_featuredStickers_addButton));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{LocationDirectionCell.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_featuredStickers_addButtonPressed));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_dialogTextBlue2));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{LocationCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationCell.class}, new String[]{"addressTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));

        themeDescriptions.add(new ThemeDescription(searchListView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{LocationCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(searchListView, 0, new Class[]{LocationCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(searchListView, 0, new Class[]{LocationCell.class}, new String[]{"addressTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{SharingLiveLocationCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{SharingLiveLocationCell.class}, new String[]{"distanceTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationLoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationLoadingCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationLoadingCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationPoweredCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{LocationPoweredCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_dialogEmptyImage));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{LocationPoweredCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_dialogEmptyText));

        return themeDescriptions;
    }
}
