//package org.plus.apps.huluride;
//
//import android.Manifest;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.animation.StateListAnimator;
//import android.animation.ValueAnimator;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Outline;
//import android.graphics.Paint;
//import android.graphics.Point;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffColorFilter;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.drawable.Drawable;
//import android.location.Location;
//import android.location.LocationManager;
//import android.net.Uri;
//import android.os.Build;
//import android.text.TextUtils;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewOutlineProvider;
//import android.view.animation.OvershootInterpolator;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.MapsInitializer;
//import com.google.android.gms.maps.Projection;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.CameraPosition;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MapStyleOptions;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ApplicationLoader;
//import org.telegram.messenger.FileLog;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.LocationController;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.AlertDialog;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Adapters.LocationActivitySearchAdapter;
//import org.telegram.ui.Cells.LocationCell;
//import org.telegram.ui.Cells.SendLocationCell;
//import org.telegram.ui.Components.AlertsCreator;
//import org.telegram.ui.Components.BackupImageView;
//import org.telegram.ui.Components.CombinedDrawable;
//import org.telegram.ui.Components.CubicBezierInterpolator;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.MapPlaceholderDrawable;
//import org.telegram.ui.Components.RecyclerListView;
//import org.telegram.ui.LocationActivity;
//
//import java.util.HashMap;
//import java.util.List;
//
//public class PickUpFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
//
//
//
//    private LocationActivitySearchAdapter searchAdapter;
//
//    private ImageView locationButton;
//
//    private LinearLayout emptyView;
//    private ImageView emptyImageView;
//    private TextView emptyTitleTextView;
//    private TextView emptySubtitleTextView;
//    private ActionBarMenuItem searchItem;
//
//    private MapOverlayView overlayView;
//
//    private GoogleMap googleMap;
//    private CameraUpdate moveToBounds;
//    private MapView mapView;
//    private CameraUpdate forceUpdate;
//
//    private FrameLayout mapViewClip;
//    private RecyclerListView searchListView;
//
//
//    private SendLocationCell sendLocationCell;
//    private View markerImageView;
//    private LinearLayoutManager layoutManager;
//
//    private boolean currentMapStyleDark;
//
//    private boolean checkGpsEnabled = true;
//
//    private boolean checkPermission = true;
//    private boolean checkBackgroundPermission = true;
//
//    private boolean searching;
//    private boolean searchWas;
//    private boolean searchInProgress;
//
//
//    private boolean wasResults;
//    private boolean mapsInitialized;
//    private boolean onResumeCalled;
//
//    private Location myLocation;
//    private Location userLocation;
//    private int markerTop;
//
//    private boolean userLocationMoved;
//    private boolean searchedForCustomLocations;
//    private boolean firstWas;
//
//    public PickUpFragment() {
//        super();
//        AndroidUtilities.fixGoogleMapsBug();
//    }
//
//
//    public class MapOverlayView extends FrameLayout {
//
//        private HashMap<Marker, View> views = new HashMap<>();
//
//        public MapOverlayView(Context context) {
//            super(context);
//        }
//
//        public void addInfoView(Marker marker){
//
//        }
//
//
//        public void removeInfoView(Marker marker) {
//            View view = views.get(marker);
//            if (view != null) {
//                removeView(view);
//                views.remove(marker);
//            }
//        }
//
//        public void updatePositions() {
//            if (googleMap == null) {
//                return;
//            }
//            Projection projection = googleMap.getProjection();
//            for (HashMap.Entry<Marker, View> entry : views.entrySet()) {
//                Marker marker = entry.getKey();
//                View view = entry.getValue();
//                Point point = projection.toScreenLocation(marker.getPosition());
//                view.setTranslationX(point.x - view.getMeasuredWidth() / 2);
//                view.setTranslationY(point.y - view.getMeasuredHeight() + AndroidUtilities.dp(22));
//            }
//        }
//    }
//
//
//
//
//    @Override
//    public boolean onFragmentCreate() {
//        super.onFragmentCreate();
//        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.locationPermissionGranted);
//        return true;
//    }
//
//
//    @Override
//    public void onFragmentDestroy() {
//        super.onFragmentDestroy();
//        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.locationPermissionGranted);
//        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.liveLocationsChanged);
//        getNotificationCenter().removeObserver(this, NotificationCenter.closeChats);
//        getNotificationCenter().removeObserver(this, NotificationCenter.didReceiveNewMessages);
//        getNotificationCenter().removeObserver(this, NotificationCenter.replaceMessagesObjects);
//        try {
//            if (googleMap != null) {
//                googleMap.setMyLocationEnabled(false);
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        try {
//            if (mapView != null) {
//                mapView.onDestroy();
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//
//        if (searchAdapter != null) {
//            searchAdapter.destroy();
//        }
//    }
//
//
//    @Override
//    public boolean isSwipeBackEnabled(MotionEvent event) {
//        return false;
//    }
//
//
//    private Drawable shadowDrawable;
//    private float yOffset  = AndroidUtilities.dp(66);
//
//
//    @Override
//    public View createView(Context context) {
//        searchWas = false;
//        searching = false;
//        searchInProgress = false;
//
//        if (searchAdapter != null) {
//            searchAdapter.destroy();
//        }
//
//        userLocation = new Location("network");
//
//        actionBar.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
//        actionBar.setTitle("Set Pickup");
//        actionBar.setTitleColor(Theme.getColor(Theme.key_dialogTextBlack));
//        actionBar.setItemsColor(Theme.getColor(Theme.key_dialogTextBlack), false);
//        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_dialogButtonSelector), false);
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        actionBar.setAllowOverlayTitle(true);
//        if (AndroidUtilities.isTablet()) {
//            actionBar.setOccupyStatusBar(false);
//        }
//        actionBar.setAddToContainer(false);
//
//        ActionBarMenu menu = actionBar.createMenu();
//
//        overlayView = new MapOverlayView(context);
//        searchItem = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
//            @Override
//            public void onSearchExpand() {
//                searching = true;
//            }
//
//            @Override
//            public void onSearchCollapse() {
//
//                //reseting search parameters
//                searching = false;
//                searchWas = false;
//                searchAdapter.searchDelayed(null, null);
//                updateEmptyView();
//            }
//
//            @Override
//            public void onTextChanged(EditText editText) {
//                if (searchAdapter == null) {
//                    return;
//                }
//                String text = editText.getText().toString();
//                if (text.length() != 0) {
//                    searchWas = true;
//                    searchItem.setShowSearchProgress(true);
//                    if (otherItem != null) {
//                        otherItem.setVisibility(View.GONE);
//                    }
//                    listView.setVisibility(View.GONE);
//                    mapViewClip.setVisibility(View.GONE);///top over lay vew at the top of the view
//                    if (searchListView.getAdapter() != searchAdapter) {
//                        searchListView.setAdapter(searchAdapter);
//                    }
//                    searchListView.setVisibility(View.VISIBLE);//
//                    searchInProgress = searchAdapter.getItemCount() == 0;
//                } else {
//                    if (otherItem != null) {
//                        otherItem.setVisibility(View.VISIBLE);
//                    }
//                    listView.setVisibility(View.VISIBLE);
//                    mapViewClip.setVisibility(View.VISIBLE);
//                    searchListView.setAdapter(null);
//                    searchListView.setVisibility(View.GONE);
//                }
//                updateEmptyView();
//                searchAdapter.searchDelayed(text, userLocation);
//            }
//        });
//        searchItem.setSearchFieldHint(LocaleController.getString("Search", R.string.Search));
//        searchItem.setContentDescription(LocaleController.getString("Search", R.string.Search));
//        EditTextBoldCursor editText = searchItem.getSearchField();
//        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
//        editText.setHintTextColor(Theme.getColor(Theme.key_chat_messagePanelHint));
//
//
//        fragmentView = new FrameLayout(context) {
//            private boolean first = true;
//
//            @Override
//            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//                super.onLayout(changed, left, top, right, bottom);
//                if (changed) {
//                    //initial setup the measureet and the posation of the child items
//                   // fixLayoutInternal(first);
//                    first = false;
//                } else {
//
//                   // updateClipView(true);
//                }
//            }
//
//            @Override
//            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//                boolean result = super.drawChild(canvas, child, drawingTime);
//                if (child == actionBar && parentLayout != null) {
//                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight());
//                }
//                return result;
//            }
//        };
//        FrameLayout frameLayout = (FrameLayout) fragmentView;
//        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
//
//
//        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
//        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));
//        Rect padding = new Rect();
//        shadowDrawable.getPadding(padding);
//
//        FrameLayout.LayoutParams layoutParams;
//        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(21) + padding.top);
//        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
//
//
//        mapViewClip = new FrameLayout(context) {
//            @Override
//            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//                if (overlayView != null) {
//                    overlayView.updatePositions();
//                }
//            }
//        };
//        mapViewClip.setBackgroundDrawable(new MapPlaceholderDrawable());
//
//        locationButton = new ImageView(context);
//        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_location_actionBackground), Theme.getColor(Theme.key_location_actionPressedBackground));
//        if (Build.VERSION.SDK_INT < 21) {
//            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
//            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
//            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
//            combinedDrawable.setIconSize(AndroidUtilities.dp(40), AndroidUtilities.dp(40));
//            drawable = combinedDrawable;
//        } else {
//            StateListAnimator animator = new StateListAnimator();
//            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(locationButton, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
//            animator.addState(new int[]{}, ObjectAnimator.ofFloat(locationButton, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
//            locationButton.setStateListAnimator(animator);
//            locationButton.setOutlineProvider(new ViewOutlineProvider() {
//                @SuppressLint("NewApi")
//                @Override
//                public void getOutline(View view, Outline outline) {
//                    outline.setOval(0, 0, AndroidUtilities.dp(40), AndroidUtilities.dp(40));
//                }
//            });
//        }
//        locationButton.setBackgroundDrawable(drawable);
//        locationButton.setImageResource(R.drawable.location_current);
//        locationButton.setScaleType(ImageView.ScaleType.CENTER);
//        locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
//        locationButton.setTag(Theme.key_location_actionActiveIcon);
//        locationButton.setContentDescription(LocaleController.getString("AccDescrMyLocation", R.string.AccDescrMyLocation));
//        FrameLayout.LayoutParams layoutParams1 = LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 40 : 44, Build.VERSION.SDK_INT >= 21 ? 40 : 44, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 12, 12);
//        layoutParams1.bottomMargin += layoutParams.height - padding.top;
//        mapViewClip.addView(locationButton, layoutParams1);
//        locationButton.setOnClickListener(v -> {
//            if (Build.VERSION.SDK_INT >= 23) {
//                Activity activity = getParentActivity();
//                if (activity != null) {
//                    if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        showPermissionAlert(false);
//                        return;
//                    }
//                }
//            }
//            if (!checkGpsEnabled()) {
//                return;
//            }
//            if (myLocation != null && googleMap != null) {
//                locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionActiveIcon), PorterDuff.Mode.MULTIPLY));
//                locationButton.setTag(Theme.key_location_actionActiveIcon);
//                userLocationMoved = false;
//                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())));
//                if (searchedForCustomLocations) {
//                    searchedForCustomLocations = false;
//                }
//            }
//            //removeInfoView();
//        });
//
//        sendLocationCell = new SendLocationCell(context,false);
//        frameLayout.addView(sendLocationCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM));
//
//        emptyView = new LinearLayout(context);
//        emptyView.setOrientation(LinearLayout.VERTICAL);
//        emptyView.setGravity(Gravity.CENTER_HORIZONTAL);
//        emptyView.setPadding(0, AndroidUtilities.dp(60 + 100), 0, 0);
//        emptyView.setVisibility(View.GONE);
//        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        emptyView.setOnTouchListener((v, event) -> true);
//
//        emptyImageView = new ImageView(context);
//        emptyImageView.setImageResource(R.drawable.location_empty);
//        emptyImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogEmptyImage), PorterDuff.Mode.MULTIPLY));
//        emptyView.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
//
//        emptyTitleTextView = new TextView(context);
//        emptyTitleTextView.setTextColor(Theme.getColor(Theme.key_dialogEmptyText));
//        emptyTitleTextView.setGravity(Gravity.CENTER);
//        emptyTitleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        emptyTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
//        emptyTitleTextView.setText(LocaleController.getString("NoPlacesFound", R.string.NoPlacesFound));
//        emptyView.addView(emptyTitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 11, 0, 0));
//
//        emptySubtitleTextView = new TextView(context);
//        emptySubtitleTextView.setTextColor(Theme.getColor(Theme.key_dialogEmptyText));
//        emptySubtitleTextView.setGravity(Gravity.CENTER);
//        emptySubtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
//        emptySubtitleTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), 0);
//        emptyView.addView(emptySubtitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 6, 0, 0));
//
//        frameLayout.addView(mapViewClip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
//
//
//        mapView = new MapView(context) {
//
//            @Override
//            public boolean onTouchEvent(MotionEvent event) {
//                return super.onTouchEvent(event);
//            }
//
//            @Override
//            public boolean dispatchTouchEvent(MotionEvent ev) {
//                MotionEvent eventToRecycle = null;
//                if (yOffset != 0) {
//                    ev = eventToRecycle = MotionEvent.obtain(ev);
//                    eventToRecycle.offsetLocation(0, -yOffset / 2);
//                }
//                boolean result = super.dispatchTouchEvent(ev);
//                if (eventToRecycle != null) {
//                    eventToRecycle.recycle();
//                }
//                return result;
//            }
//
//            @Override
//            public boolean onInterceptTouchEvent(MotionEvent ev) {
//                if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//                    if (!userLocationMoved) {
//                        locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionIcon), PorterDuff.Mode.MULTIPLY));
//                        locationButton.setTag(Theme.key_location_actionIcon);
//                        userLocationMoved = true;
//                    }
//                    if (googleMap != null) {
//                        if (userLocation != null) {
//                            userLocation.setLatitude(googleMap.getCameraPosition().target.latitude);
//                            userLocation.setLongitude(googleMap.getCameraPosition().target.longitude);
//                        }
//                    }
//                }
//                return super.onInterceptTouchEvent(ev);
//            }
//
//            @Override
//            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//                super.onLayout(changed, left, top, right, bottom);
//                AndroidUtilities.runOnUIThread(() -> {
//                    if (moveToBounds != null) {
//                        googleMap.moveCamera(moveToBounds);
//                        moveToBounds = null;
//                    }
//                });
//            }
//        };
//        final MapView map = mapView;
//        new Thread(() -> {
//            try {
//                map.onCreate(null);
//            } catch (Exception e) {
//                //this will cause exception, but will preload google maps?
//            }
//            AndroidUtilities.runOnUIThread(() -> {
//                if (mapView != null && getParentActivity() != null) {
//                    try {
//                        map.onCreate(null);
//                        MapsInitializer.initialize(ApplicationLoader.applicationContext);
//                        mapView.getMapAsync(map1 -> {
//                            googleMap = map1;
//                            if (isActiveThemeDark()) {
//                                currentMapStyleDark = true;
//                                MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(ApplicationLoader.applicationContext, R.raw.mapstyle_night);
//                                googleMap.setMapStyle(style);
//                            }
//                            googleMap.setPadding(AndroidUtilities.dp(70), 0, AndroidUtilities.dp(70), AndroidUtilities.dp(10));
//                            onMapInit();
//                        });
//                        mapsInitialized = true;
//                        if (onResumeCalled) {
//                            mapView.onResume();
//                        }
//                    } catch (Exception e) {
//                        FileLog.e(e);
//                    }
//                }
//            });
//        }).start();
//
//
//        searchListView = new RecyclerListView(context);
//        searchListView.setVisibility(View.GONE);
//        searchListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        searchAdapter = new LocationActivitySearchAdapter(context) {
//            @Override
//            public void notifyDataSetChanged() {
//                if (searchItem != null) {
//                    searchItem.setShowSearchProgress(searchAdapter.isSearching());
//                }
//                if (emptySubtitleTextView != null) {
//                    emptySubtitleTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("NoPlacesFoundInfo", R.string.NoPlacesFoundInfo, searchAdapter.getLastSearchString())));
//                }
//                super.notifyDataSetChanged();
//            }
//        };
//        searchAdapter.setDelegate(0, places -> {
//            searchInProgress = false;
//            updateEmptyView();
//        });
//        frameLayout.addView(searchListView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));
//        searchListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
//                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
//                }
//            }
//        });
//        searchListView.setOnItemClickListener((view, position) -> {
//
//        });
//
//        shadow = new View(context) {
//
//            private RectF rect = new RectF();
//
//            @Override
//            protected void onDraw(Canvas canvas) {
//                shadowDrawable.setBounds(-padding.left, 0, getMeasuredWidth() + padding.right, getMeasuredHeight());
//                shadowDrawable.draw(canvas);
//                int w = AndroidUtilities.dp(36);
//                int y = padding.top + AndroidUtilities.dp(10);
//                rect.set((getMeasuredWidth() - w) / 2, y, (getMeasuredWidth() + w) / 2, y + AndroidUtilities.dp(4));
//                int color = Theme.getColor(Theme.key_sheet_scrollUp);
//                int alpha = Color.alpha(color);
//                Theme.dialogs_onlineCirclePaint.setColor(color);
//                canvas.drawRoundRect(rect, AndroidUtilities.dp(2), AndroidUtilities.dp(2), Theme.dialogs_onlineCirclePaint);
//
//            }
//        };
//        if (Build.VERSION.SDK_INT >= 21) {
//            shadow.setTranslationZ(AndroidUtilities.dp(6));
//        }
//        mapViewClip.addView(shadow, layoutParams);
//
//
//        frameLayout.addView(actionBar);
//        updateEmptyView();
//
//        return fragmentView;
//    }
//
//    private void updateEmptyView() {
//        if (searching) {
//            if (searchInProgress) {
//                searchListView.setEmptyView(null);
//                emptyView.setVisibility(View.GONE);
//                searchListView.setVisibility(View.GONE);
//            } else {
//                searchListView.setEmptyView(emptyView);
//            }
//        } else {
//            emptyView.setVisibility(View.GONE);
//        }
//    }
//    private View shadow;
//
//    private Bitmap[] bitmapCache = new Bitmap[7];
//    private Bitmap createPlaceBitmap(int num) {
//        if (bitmapCache[num % 7] != null) {
//            return bitmapCache[num % 7];
//        }
//        try {
//            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            paint.setColor(0xffffffff);
//            Bitmap bitmap = Bitmap.createBitmap(AndroidUtilities.dp(12), AndroidUtilities.dp(12), Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            canvas.drawCircle(AndroidUtilities.dp(6), AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);
//            paint.setColor(LocationCell.getColorForIndex(num));
//            canvas.drawCircle(AndroidUtilities.dp(6), AndroidUtilities.dp(6), AndroidUtilities.dp(5), paint);
//            canvas.setBitmap(null);
//            return bitmapCache[num % 7] = bitmap;
//        } catch (Throwable e) {
//            FileLog.e(e);
//        }
//        return null;
//    }
//
//    private boolean firstFocus;
//    private void onMapInit(){
//        if(googleMap == null){
//            return;
//        }
//        LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
//        try {
//            googleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin2)));
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 4);
//        googleMap.moveCamera(position);
//        firstFocus = false;
//
//        userLocation = new Location("network");
//        userLocation.setLatitude(20.659322);
//        userLocation.setLongitude(-11.406250);
//
//
////        if (initialLocation != null) {
////            LatLng latLng = new LatLng(initialLocation.geo_point.lat, initialLocation.geo_point._long);
////            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 4));
////            userLocation.setLatitude(initialLocation.geo_point.lat);
////            userLocation.setLongitude(initialLocation.geo_point._long);
////            adapter.setCustomLocation(userLocation);
////        } else {
////            userLocation.setLatitude(20.659322);
////            userLocation.setLongitude(-11.406250);
////        }
////
//        // getRecentLocations();
//
//        try {
//            googleMap.setMyLocationEnabled(true);
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//
//        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
//        googleMap.getUiSettings().setZoomControlsEnabled(false);
//        googleMap.getUiSettings().setCompassEnabled(false);
//
////        googleMap.setOnCameraMoveStartedListener(reason -> {
////            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
////               // showSearchPlacesButton(true);
////                //removeInfoView();
////
////                if (!scrolling && listView.getChildCount() > 0) {
////                    View view = listView.getChildAt(0);
////                    if (view != null) {
////                        RecyclerView.ViewHolder holder = listView.findContainingViewHolder(view);
////                        if (holder != null && holder.getAdapterPosition() == 0) {
////                            int min = locationType == LOCATION_TYPE_SEND ? 0 : AndroidUtilities.dp(66);
////                            int top = view.getTop();
////                            if (top < -min) {
////                                CameraPosition cameraPosition = googleMap.getCameraPosition();
////                                forceUpdate = CameraUpdateFactory.newLatLngZoom(cameraPosition.target, cameraPosition.zoom);
////                                listView.smoothScrollBy(0, top + min);
////                            }
////                        }
////                    }
////                }
////            }
////        });
//
//
//        googleMap.setOnMyLocationChangeListener(location -> {
//            positionMarker(location);
//            getLocationController().setGoogleMapLocation(location, isFirstLocation);
//            isFirstLocation = false;
//        });
//
////        googleMap.setOnMarkerClickListener(marker -> {
////            if (!(marker.getTag() instanceof LocationActivity.VenueLocation)) {
////                return true;
////            }
////            markerImageView.setVisibility(View.INVISIBLE);
////            if (!userLocationMoved) {
////                locationButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_location_actionIcon), PorterDuff.Mode.MULTIPLY));
////                locationButton.setTag(Theme.key_location_actionIcon);
////                userLocationMoved = true;
////            }
////            overlayView.addInfoView(marker);
////            return true;
////        });
//
//        googleMap.setOnCameraMoveListener(() -> {
//            if (overlayView != null) {
//                overlayView.updatePositions();
//            }
//        });
//
//        positionMarker(myLocation = getLastLocation());
//
//        if (checkGpsEnabled && getParentActivity() != null) {
//            checkGpsEnabled = false;
//            checkGpsEnabled();
//        }
//    }
//
//
//
//    private void showPermissionAlert(boolean byButton) {
//        if (getParentActivity() == null) {
//            return;
//        }
//        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
//        builder.setTitle(LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
//        if (byButton) {
//            builder.setMessage(LocaleController.getString("PermissionNoLocationPosition", R.string.PermissionNoLocationPosition));
//        } else {
//            builder.setMessage(LocaleController.getString("PermissionNoLocation", R.string.PermissionNoLocation));
//        }
//        builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), (dialog, which) -> {
//            if (getParentActivity() == null) {
//                return;
//            }
//            try {
//                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
//                getParentActivity().startActivity(intent);
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//        });
//        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
//        showDialog(builder.create());
//    }
//
//    private boolean checkGpsEnabled() {
//        if (!getParentActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
//            return true;
//        }
//        try {
//            LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
//            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
//                builder.setTitle(LocaleController.getString("GpsDisabledAlertTitle", R.string.GpsDisabledAlertTitle));
//                builder.setMessage(LocaleController.getString("GpsDisabledAlertText", R.string.GpsDisabledAlertText));
//                builder.setPositiveButton(LocaleController.getString("ConnectingToProxyEnable", R.string.ConnectingToProxyEnable), (dialog, id) -> {
//                    if (getParentActivity() == null) {
//                        return;
//                    }
//                    try {
//                        getParentActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                    } catch (Exception ignore) {
//
//                    }
//                });
//                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
//                showDialog(builder.create());
//                return false;
//            }
//        } catch (Exception e) {
//            FileLog.e(e);
//        }
//        return true;
//    }
//
//    private void positionMarker(Location location) {
//        if (location == null) {
//            return;
//        }
//        myLocation = new Location(location);
//        if ( googleMap != null) {
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            if (adapter != null) {
//                adapter.searchPlacesWithQuery(null, myLocation, true);
//                adapter.setGpsLocation(myLocation);
//            }
//            if (!userLocationMoved) {
//                userLocation = new Location(location);
//                if (firstWas) {
//                    CameraUpdate position = CameraUpdateFactory.newLatLng(latLng);
//                    googleMap.animateCamera(position);
//                } else {
//                    firstWas = true;
//                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getMaxZoomLevel() - 4);
//                    googleMap.moveCamera(position);
//                }
//            }
//        } else {
//            adapter.setGpsLocation(myLocation);
//        }
//    }
//
//    private Location getLastLocation() {
//        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
//        List<String> providers = lm.getProviders(true);
//        Location l = null;
//        for (int i = providers.size() - 1; i >= 0; i--) {
//            l = lm.getLastKnownLocation(providers.get(i));
//            if (l != null) {
//                break;
//            }
//        }
//        return l;
//    }
//
//
//    @Override
//    public void didReceivedNotification(int id, int account, Object... args) {
//        if (id == NotificationCenter.locationPermissionGranted) {
//            if (googleMap != null) {
//                try {
//                    googleMap.setMyLocationEnabled(true);
//                } catch (Exception e) {
//                    FileLog.e(e);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (mapView != null && mapsInitialized) {
//            try {
//                mapView.onPause();
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//        }
//
//        onResumeCalled = false;
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
//        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
//        if (mapView != null && mapsInitialized) {
//            try {
//                mapView.onResume();
//            } catch (Throwable e) {
//                FileLog.e(e);
//            }
//        }
//        onResumeCalled = true;
//        if (googleMap != null) {
//            try {
//                googleMap.setMyLocationEnabled(true);
//            } catch (Exception e) {
//                FileLog.e(e);
//            }
//        }
//        if (checkPermission && Build.VERSION.SDK_INT >= 23) {
//            Activity activity = getParentActivity();
//            if (activity != null) {
//                checkPermission = false;
//                if (activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    activity.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        if (mapView != null && mapsInitialized) {
//            mapView.onLowMemory();
//        }
//    }
//
//
//    @Override
//    public boolean onBackPressed() {
//
//        return super.onBackPressed();
//    }
//
//    private boolean isActiveThemeDark() {
//        Theme.ThemeInfo info = Theme.getActiveTheme();
//        if (info.isDark()) {
//            return true;
//        }
//        int color = Theme.getColor(Theme.key_windowBackgroundWhite);
//        return AndroidUtilities.computePerceivedBrightness(color) < 0.721f;
//    }
//
//
//
//    @Override
//    public void onTransitionAnimationEnd(boolean isOpen, boolean backward) {
//        if (isOpen && !backward) {
//            try {
//                if (mapView.getParent() instanceof ViewGroup) {
//                    ViewGroup viewGroup = (ViewGroup) mapView.getParent();
//                    viewGroup.removeView(mapView);
//                }
//            } catch (Exception ignore) {
//
//            }
//            if (mapViewClip != null) {
//                mapViewClip.addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT,0,0,0,66));
//                if (overlayView != null) {
//                    try {
//                        if (overlayView.getParent() instanceof ViewGroup) {
//                            ViewGroup viewGroup = (ViewGroup) overlayView.getParent();
//                            viewGroup.removeView(overlayView);
//                        }
//                    } catch (Exception ignore) {
//
//                    }
//                    mapViewClip.addView(overlayView, 1, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT + AndroidUtilities.dp(10), Gravity.TOP | Gravity.LEFT,0,0,0,66));
//                }
//                updateClipView(false);
//            } else if (fragmentView != null) {
//                ((FrameLayout) fragmentView).addView(mapView, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT,0,0,0,66));
//            }
//        }
//    }
//
//}
