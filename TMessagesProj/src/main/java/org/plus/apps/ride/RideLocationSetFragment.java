package org.plus.apps.ride;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.model.LatLng;

import org.plus.apps.ride.data.RideObjects;
import org.plus.apps.ride.ui.cells.SearchField;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.LocationActivity;

import java.util.ArrayList;

public class RideLocationSetFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    public static ActionBarLayout[] showAsSheet(BaseFragment parentFragment,RideObjects.SearchLocation address) {
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return null;
        }
        ActionBarLayout[] actionBarLayout = new ActionBarLayout[]{new ActionBarLayout(parentFragment.getParentActivity())};
        BottomSheet bottomSheet = new BottomSheet(parentFragment.getParentActivity(), true) {
            {
                actionBarLayout[0].init(new ArrayList<>());
                RideLocationSetFragment fragment = new RideLocationSetFragment(address) {
                    @Override
                    public void finishFragment() {
                        dismiss();
                    }

                    @Override
                    public void removeSelfFromStack() {
                        dismiss();
                    }
                };
                actionBarLayout[0].addFragmentToStack(fragment);
                actionBarLayout[0].showLastFragment();
                actionBarLayout[0].setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);
                containerView = actionBarLayout[0];
                setApplyBottomPadding(false);
                setApplyBottomPadding(false);
                setOnDismissListener(dialog -> fragment.onFragmentDestroy());
            }

            @Override
            protected boolean canDismissWithSwipe() {
                return false;
            }

            @Override
            public void onBackPressed() {
                if (actionBarLayout[0] == null || actionBarLayout[0].fragmentsStack.size() <= 1) {
                    super.onBackPressed();
                } else {
                    actionBarLayout[0].onBackPressed();
                }
            }

            @Override
            public void dismiss() {
                super.dismiss();
                actionBarLayout[0] = null;
            }
        };

        bottomSheet.show();
        return actionBarLayout;
    }

    public interface TipLocationSetDelegate{
        void onTripSelected(RideObjects.SearchLocation  pickup,RideObjects.SearchLocation dest);
    }

    private TipLocationSetDelegate delegate;

    public void setDelegate(TipLocationSetDelegate delegate) {
        this.delegate = delegate;
    }

    private TextView chooseOnMapButton;
    private FrameLayout bottomLayout;

    public Location pick_up_location;
    public Location destinationLocation;

    public RideObjects.SearchLocation tripStartLocation;
    public RideObjects.SearchLocation tripDestinationLocation;

    private boolean isDestinationActive;

    public RideLocationSetFragment(RideObjects.SearchLocation address){
        tripStartLocation = address;
        if(address != null){
            pick_up_location = address.location;
        }

    }

    private RecyclerListView listView;
    private RideLocationAdapter adapter;

    private SearchField pickUPEditText;
    private SearchField destinationEditTExt;
    private FrameLayout searchFrame;


    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
        if (adapter != null) {
            adapter.destroy();
        }
    }


    private Paint backgroundPaint = new Paint();

    @Override
    public View createView(Context context) {

        if (adapter != null) {
            adapter.destroy();
        }

        actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        actionBar.setTitle("Enter Destination");
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setCastShadows(false);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setAddToContainer(false);
        actionBar.setExtraHeight(AndroidUtilities.dp(88 + 36 + 6));

        hasOwnBackground = true;

        isDestinationActive = tripDestinationLocation == null;

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id== -1){
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context){

            private boolean globalIgnoreLayout;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int actionBarHeight = actionBar.getMeasuredHeight();
                globalIgnoreLayout = true;
                if (listView != null) {
                    listView.setPadding(0, actionBarHeight, 0, AndroidUtilities.dp(48));
                }
                globalIgnoreLayout = false;

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == actionBar) {
                        continue;
                    }
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                }
            }


            @Override
            protected void onDraw(Canvas canvas) {
                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                canvas.drawRect(0, actionBar.getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);

            }

            @Override
            public void requestLayout() {
                if (globalIgnoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (parentLayout != null) {
                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight());
                 //   parentLayout.drawHeaderShadow(canvas, listView.getMeasuredHeight());
                }
            }
        };

        FrameLayout frameLayout = (FrameLayout) fragmentView;
        fragmentView.setTag(Theme.key_windowBackgroundWhite);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        FrameLayout searchFrame = createSearchFrame(context);
        actionBar.addView(searchFrame,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,88 + 28,Gravity.LEFT | Gravity.BOTTOM,0,0,16,0));


        adapter = new RideLocationAdapter(context);
        listView = new RecyclerListView(context);
        listView.setAdapter(adapter);
        listView.setVerticalScrollBarEnabled(false);
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

               RideObjects.SearchLocation searchLocation =  adapter.getItem(position);
               if(searchLocation == null){
                   return;
               }
               tripDestinationLocation = searchLocation;
               destinationEditTExt.setText(tripDestinationLocation.address);
               if(delegate != null && tripDestinationLocation != null && tripDestinationLocation != null){
                   delegate.onTripSelected(tripStartLocation,tripDestinationLocation);
                   finishFragment();
               }
            }
        });
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP,0,0,0,0));
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);


        SuperLocationAdapter.SearchProgressDelegate searchProgressDelegate = new SuperLocationAdapter.SearchProgressDelegate() {
            @Override
            public void onSearchStart() {
                if(pickUPEditText != null && destinationEditTExt != null){
                    if(isDestinationActive){
                        destinationEditTExt.getProgressDrawable().startAnimation();
                    }else{
                        pickUPEditText.getProgressDrawable().startAnimation();
                    }
                }

            }

            @Override
            public void onSearchStop() {
                if(pickUPEditText != null && destinationEditTExt != null){
                    if(isDestinationActive){
                        destinationEditTExt.getProgressDrawable().stopAnimation();
                    }else{
                        pickUPEditText.getProgressDrawable().stopAnimation();
                    }
                }

            }
        };
        adapter.setDelegate(searchProgressDelegate);


        bottomLayout = new FrameLayout(context);
        bottomLayout.setBackgroundDrawable(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_passport_authorizeBackground), Theme.getColor(Theme.key_passport_authorizeBackgroundSelected)));
        frameLayout.addView(bottomLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM));
        bottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationActivity locationActivity = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP);
                locationActivity.setDelegate(new LocationActivity.LocationActivityDelegate() {
                    @Override
                    public void didSelectLocation(TLRPC.MessageMedia location, int live, boolean notify, int scheduleDate) {
                      String address =   location.address;
                      double lat = location.geo.lat;
                      double aLong = location.geo._long;
                      if(isDestinationActive){
                          destinationEditTExt.setText(address);
                      }else{
                          pickUPEditText.setText(address);
                      }

                    }
                });
                presentFragment(locationActivity);
            }
        });

        chooseOnMapButton = new TextView(context);
        chooseOnMapButton.setCompoundDrawablePadding(AndroidUtilities.dp(8));
        chooseOnMapButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pin, 0, 0, 0);
        chooseOnMapButton.setTextColor(Theme.getColor(Theme.key_passport_authorizeText));
        chooseOnMapButton.setText("Choose on map");
        chooseOnMapButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        chooseOnMapButton.setGravity(Gravity.CENTER);
        chooseOnMapButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        bottomLayout.addView(chooseOnMapButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));


        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);

        return fragmentView;
    }

    private FrameLayout createSearchFrame(Context context){


        searchFrame = new FrameLayout(context){

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int left = AndroidUtilities.dp(20);
                int top = AndroidUtilities.dp(30);

                 Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                 canvas.drawCircle(left, top, AndroidUtilities.dp(8), Theme.dialogs_onlineCirclePaint);
                 Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_chats_onlineCircle));
                 canvas.drawCircle(left, top, AndroidUtilities.dp(5), Theme.dialogs_onlineCirclePaint);


                int newTop  = AndroidUtilities.dp(62 + 32);
                Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                canvas.drawCircle(left, newTop, AndroidUtilities.dp(8), Theme.dialogs_onlineCirclePaint);

                Theme.dialogs_onlineCirclePaint.setColor(Color.parseColor("#ffadd8e6"));
                canvas.drawCircle(left, newTop, AndroidUtilities.dp(5), Theme.dialogs_onlineCirclePaint);


                canvas.drawLine(left,top,left ,newTop,Theme.dividerPaint);


            }
        };
        searchFrame.setWillNotDraw(false);

        pickUPEditText = new SearchField(context){
            @Override
            public void onTextChange(String text) {
                if(adapter != null){
                    adapter.searchDelayed(text, pick_up_location);
                }
                isDestinationActive = false;


            }

            @Override
            protected void onFieldTouchUp(EditTextBoldCursor editText) {
                super.onFieldTouchUp(editText);
                isDestinationActive = false;

            }
        };
        pickUPEditText.setHint("Enter pick-up location");
        if(tripStartLocation != null){
            pickUPEditText.setText(tripStartLocation.address);
        }
        pickUPEditText.getSearchEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
               // isDestinationActive = false;
                actionBar.setTitle("Enter pick-up location");
            }
        });
        searchFrame.addView(pickUPEditText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,47,Gravity.TOP|Gravity.LEFT,40,0,0,8));

//        pickUPEditText = new EditTextBoldCursor(context);
//        pickUPEditText.setHint("Enter pick-up location");
//        if(tripStartLocation != null){
//            pickUPEditText.setText(tripStartLocation.address);
//        }
//        pickUPEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    isDestinationActive = false;
//                    actionBar.setTitle("Enter pick-up location");
//                }
//            }
//        });
//        pickUPEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        pickUPEditText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
//        pickUPEditText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        pickUPEditText.setMaxLines(1);
//        pickUPEditText.setLines(1);
//        pickUPEditText.setGravity(Gravity.CENTER_VERTICAL);
//        pickUPEditText.setBackground(Theme.createRoundRectDrawable(8,Theme.getColor(Theme.key_windowBackgroundGray)));
//        pickUPEditText.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(0), AndroidUtilities.dp(0), AndroidUtilities.dp(0));
//        pickUPEditText.setSingleLine(true);
//        pickUPEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
//        pickUPEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        pickUPEditText.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        pickUPEditText.setCursorSize(AndroidUtilities.dp(20));
//        pickUPEditText.setCursorWidth(1.5f);
//        pickUPEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//
//            }
//        });
//


//        destinationEditTExt = new EditTextBoldCursor(context);
//        destinationEditTExt.setHint("Enter Destination");
//        destinationEditTExt.setBackground(Theme.createRoundRectDrawable(8,Theme.getColor(Theme.key_windowBackgroundGray)));
//        destinationEditTExt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if(hasFocus){
//                    isDestinationActive = true;
//                    actionBar.setTitle("Enter Destination");
//                }
//            }
//        });
//        destinationEditTExt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        destinationEditTExt.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
//        destinationEditTExt.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        destinationEditTExt.setMaxLines(1);
//        destinationEditTExt.setLines(1);
//        destinationEditTExt.setGravity(Gravity.CENTER_VERTICAL);
//        destinationEditTExt.setBackground(Theme.createRoundRectDrawable(8,Theme.getColor(Theme.key_windowBackgroundGray)));
//        destinationEditTExt.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(0), AndroidUtilities.dp(0), AndroidUtilities.dp(0));
//        destinationEditTExt.setSingleLine(true);
//        destinationEditTExt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
//        destinationEditTExt.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        destinationEditTExt.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        destinationEditTExt.setCursorSize(AndroidUtilities.dp(20));
//        destinationEditTExt.setCursorWidth(1.5f);
//        destinationEditTExt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                String text = editable.toString();
//                Log.i("berhanzak",text + " = text");
//                if (text.length() != 0) {
//                    if(adapter != null){
//                        adapter.searchDelayed(text, pick_up_location);
//                    }
//                }
//
//            }
//        });
        destinationEditTExt = new SearchField(context){

            @Override
            protected void onFieldTouchUp(EditTextBoldCursor editText) {
                super.onFieldTouchUp(editText);
                isDestinationActive = true;
            }

            @Override
            public void onTextChange(String text) {
                if(adapter != null){
                    adapter.searchDelayed(text, pick_up_location);
                }
                isDestinationActive = true;

            }
        };
        destinationEditTExt.setHint("Enter Destination");
        destinationEditTExt.getSearchEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    isDestinationActive = true;
                    actionBar.setTitle("Enter Destination");
                }else{
                    isDestinationActive = false;
                }
            }
        });

        searchFrame.addView(destinationEditTExt, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,47,Gravity.TOP|Gravity.LEFT,40,47 + 8,0,8));


        return searchFrame;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }


    @Override
    public void onResume() {
        super.onResume();
        if(destinationEditTExt != null){
            destinationEditTExt.getSearchEditText().requestFocus();
            destinationEditTExt.showKeyboard();
        }
    }



}
