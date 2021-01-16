package org.plus.apps.ride;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.plus.apps.business.ui.components.FakeSearchParent;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.LocationController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.BaseLocationAdapter;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.LocationCell;
import org.telegram.ui.Cells.LocationDirectionCell;
import org.telegram.ui.Cells.LocationLoadingCell;
import org.telegram.ui.Cells.LocationPoweredCell;
import org.telegram.ui.Cells.SendLocationCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.SharingLiveLocationCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchField;
import org.telegram.ui.LocationActivity;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class LocationAdapter extends BaseLocationAdapter implements LocationController.LocationFetchCallback {

    private int currentAccount = UserConfig.selectedAccount;
    private Context mContext;
    private int overScrollHeight;
    private SendLocationCell sendLocationCell;
    private Location gpsLocation;
    private Location customLocation;
    private String addressName;
    private Location previousFetchedLocation;
    private int locationType;
    private int shareLiveLocationPotistion = -1;
    private MessageObject currentMessageObject;
    private TLRPC.TL_channelLocation chatLocation;
    private ArrayList<LocationActivity.LiveLocation> currentLiveLocations = new ArrayList<>();
    private boolean fetchingLocation;
    private boolean needEmptyView;

    private Runnable updateRunnable;

    private int rowCount;
    private int emptyRow;
    private int locationRow;
    protected int whereRow;
    private int bottomPaddingRow;

    public String getAddress(){
        return addressName;
    }

    private void updateRow(){

        rowCount = 0;
        emptyRow = rowCount++;
        locationRow = rowCount++;
        whereRow = rowCount++;
        bottomPaddingRow = rowCount++;


    }

    public LocationAdapter(Context context, int type, boolean emptyView) {
        super();
        mContext = context;
        locationType = type;
        needEmptyView = emptyView;
        updateRow();
    }

    public void setOverScrollHeight(int value) {
        overScrollHeight = value;
    }

    public void setUpdateRunnable(Runnable runnable) {
        updateRunnable = runnable;
    }

    public void setGpsLocation(Location location) {
        boolean notSet = gpsLocation == null;
        gpsLocation = location;
        if (customLocation == null) {
            fetchLocationAddress();
        }
        if (notSet && shareLiveLocationPotistion > 0) {
            notifyItemChanged(shareLiveLocationPotistion);
        }
        if (currentMessageObject != null) {
            notifyItemChanged(1, new Object());
            updateLiveLocations();
        } else if (locationType != 2) {
            updateCell();
        } else {
            updateLiveLocations();
        }
    }

    public void updateLiveLocationCell() {
        if (shareLiveLocationPotistion > 0) {
            notifyItemChanged(shareLiveLocationPotistion);
        }
    }

    public void updateLiveLocations() {
        if (!currentLiveLocations.isEmpty()) {
            notifyItemRangeChanged(2, currentLiveLocations.size(), new Object());
        }
    }

    public void setCustomLocation(Location location) {
        customLocation = location;
        fetchLocationAddress();
        updateCell();
    }

    public void setLiveLocations(ArrayList<LocationActivity.LiveLocation> liveLocations) {
        currentLiveLocations = new ArrayList<>(liveLocations);
        int uid = UserConfig.getInstance(currentAccount).getClientUserId();
        for (int a = 0; a < currentLiveLocations.size(); a++) {
            if (currentLiveLocations.get(a).id == uid || currentLiveLocations.get(a).object.out) {
                currentLiveLocations.remove(a);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setMessageObject(MessageObject messageObject) {
        currentMessageObject = messageObject;
        notifyDataSetChanged();
    }


    public void setChatLocation(TLRPC.TL_channelLocation location) {
        chatLocation = location;
    }

    private void updateCell() {

        if (sendLocationCell != null) {
            if (true) {
                String address;
                if (!TextUtils.isEmpty(addressName)) {
                    address = addressName;
                } else if (customLocation == null && gpsLocation == null || fetchingLocation) {
                    address = LocaleController.getString("Loading", R.string.Loading);
                } else if (customLocation != null) {
                    address = String.format(Locale.US, "(%f,%f)", customLocation.getLatitude(), customLocation.getLongitude());
                } else if (gpsLocation != null) {
                    address = String.format(Locale.US, "(%f,%f)", gpsLocation.getLatitude(), gpsLocation.getLongitude());
                } else {
                    address = LocaleController.getString("Loading", R.string.Loading);
                }
                sendLocationCell.setText("Set pickup", address);

//                if (locationType == LocationActivity.LOCATION_TYPE_GROUP) {
//                    sendLocationCell.setText(LocaleController.getString("ChatSetThisLocation", R.string.ChatSetThisLocation), address);
//                } else {
//                    sendLocationCell.setText(LocaleController.getString("SendSelectedLocation", R.string.SendSelectedLocation), address);
//                }
            } else {
                if (gpsLocation != null) {
                    sendLocationCell.setText(LocaleController.getString("SendLocation", R.string.SendLocation), LocaleController.formatString("AccurateTo", R.string.AccurateTo, LocaleController.formatPluralString("Meters", (int) gpsLocation.getAccuracy())));
                } else {
                    sendLocationCell.setText(LocaleController.getString("SendLocation", R.string.SendLocation), LocaleController.getString("Loading", R.string.Loading));
                }
            }
        }
    }

    private String getAddressName() {
        return addressName;
    }

    @Override
    public void onLocationAddressAvailable(String address, String displayAddress, Location location) {
        fetchingLocation = false;
        previousFetchedLocation = location;
        addressName = address;
        updateCell();
    }

    protected void onDirectionClick() {

    }

    public void fetchLocationAddress() {
        if (locationType == LocationActivity.LOCATION_TYPE_GROUP) {
            Location location;
            if (customLocation != null) {
                location = customLocation;
            } else if (gpsLocation != null) {
                location = gpsLocation;
            } else {
                return;
            }
            if (previousFetchedLocation == null || previousFetchedLocation.distanceTo(location) > 100) {
                addressName = null;
            }
            fetchingLocation = true;
            updateCell();
            LocationController.fetchLocationAddress(location, this);
        } else {
            Location location;
            if (customLocation != null) {
                location = customLocation;
            } else {
                return;
            }
            if (previousFetchedLocation == null || previousFetchedLocation.distanceTo(location) > 20) {
                addressName = null;
            }
            fetchingLocation = true;
            updateCell();
            LocationController.fetchLocationAddress(location, this);
        }
    }

    @Override
    public int getItemCount() {
        return rowCount;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = new EmptyCell(mContext) {
                    @Override
                    public ViewPropertyAnimator animate() {
                        ViewPropertyAnimator animator = super.animate();
                        if (Build.VERSION.SDK_INT >= 19) {
                            animator.setUpdateListener(animation -> {
                                if (updateRunnable != null) {
                                    updateRunnable.run();
                                }
                            });
                        }
                        return animator;
                    }
                };
                break;
            case 1:
                view = new SendLocationCell(mContext, false);
                break;
            case 2:
                view = new HeaderCell(mContext);
                break;
            case 3:
                view = new LocationCell(mContext, false);
                break;
            case 4:
                view = new LocationLoadingCell(mContext);
                break;
            case 5:
                view = new LocationPoweredCell(mContext);
                break;
            case 6: {
                SendLocationCell cell = new SendLocationCell(mContext, false);
                view = cell;
                break;
            }
            case 7:
                view = new SharingLiveLocationCell(mContext, true, locationType == LocationActivity.LOCATION_TYPE_GROUP || locationType == LocationActivity.LOCATION_TYPE_GROUP_VIEW ? 16 : 54);
                break;
            case 8: {
                LocationDirectionCell cell = new LocationDirectionCell(mContext);
                cell.setOnButtonClick(v -> onDirectionClick());
                view = cell;
                break;
            }
            case 9: {
                view = new ShadowSectionCell(mContext);
                Drawable drawable = Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);
                CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
                combinedDrawable.setFullsize(true);
                view.setBackgroundDrawable(combinedDrawable);
                break;
            }
            case 56:
                view = new SearchDestinationCell(mContext);
                break;
            case 11:
                view = new TitleCell(mContext);
                break;
            case 12:
                view = new FakeSearchParent(mContext);
                break;
            case 13:
                view = new  EmptyCell(mContext,34);
                break;
            case 10:
            default: {
                view = new View(mContext);
                break;
            }
        }
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ((EmptyCell) holder.itemView).setHeight(overScrollHeight);
                break;
            case 1:
                sendLocationCell = (SendLocationCell) holder.itemView;
                updateCell();
                break;
            case 2: {
                HeaderCell cell = (HeaderCell) holder.itemView;
                if (currentMessageObject != null) {
                    cell.setText(LocaleController.getString("LiveLocations", R.string.LiveLocations));
                } else {
                    cell.setText(LocaleController.getString("NearbyVenue", R.string.NearbyVenue));
                }
                break;
            }
            case 3: {
                LocationCell cell = (LocationCell) holder.itemView;
                if (locationType == 0) {
                    position -= 4;
                } else {
                    position -= 5;
                }
                cell.setLocation(places.get(position), iconUrls.get(position), position, true);
                break;
            }
            case 4:
                ((LocationLoadingCell) holder.itemView).setLoading(searching);
                break;
            case 11:
                TitleCell titleCell = (TitleCell)holder.itemView;
                titleCell.setText("Where do you want to go?");
               // titleCell.setText("Welcome " + UserConfig.getInstance(currentAccount).getCurrentUser().first_name + "!");
                break;
            case 12:
                FakeSearchParent searchField = (FakeSearchParent)holder.itemView;
                searchField.getSearchField().setHint("Where do you want to go?");
                break;
            case 6:
              SendLocationCell sendLocationCell =   ((SendLocationCell) holder.itemView);
              sendLocationCell.setHasLocation(gpsLocation != null);

                break;
            case 7:
                SharingLiveLocationCell locationCell = (SharingLiveLocationCell) holder.itemView;
                break;
        }
    }


    @Override
    public int getItemViewType(int position) {

        if(position == emptyRow){
            return 0;
        }else if(position == locationRow){
            return 11;
        }else if(position == whereRow){
            return 56;
        }else if(position == bottomPaddingRow){
            return 13;
        }
        return super.getItemViewType(position);

    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        return false;
       // viewType == 56
    }

    private static class TitleCell extends FrameLayout {

        private TextView titleView;

        public TitleCell(Context context) {
            super(context);

            titleView = new TextView(getContext());
            titleView.setLines(1);
            titleView.setSingleLine(true);
            titleView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            titleView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            titleView.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(15), AndroidUtilities.dp(22), AndroidUtilities.dp(8));
            titleView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            titleView.setGravity(Gravity.CENTER_VERTICAL);
            addView(titleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
        }

        public void setText(String text) {
            titleView.setText(text);
        }
    }


    public static class SearchDestinationCell extends FrameLayout{

       private CardView cardView;


        public SearchDestinationCell(@NonNull Context context) {
            super(context);

            cardView = new CardView(context);
            cardView.setCardElevation(AndroidUtilities.dp(3));
            cardView.setRadius(AndroidUtilities.dp(4));
           // cardView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));
            cardView.setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            addView(cardView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,24 + 16 + 16,Gravity.CENTER_HORIZONTAL,16,16,16,16));

            ImageView searchImageView = new ImageView(context);
            searchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlue), PorterDuff.Mode.MULTIPLY));
            searchImageView.setImageResource(R.drawable.ic_ab_search);
            cardView.addView(searchImageView,LayoutHelper.createFrame(24,24,Gravity.LEFT|Gravity.CENTER_VERTICAL,16,0,0,0));

            SimpleTextView simpleTextView = new SimpleTextView(context);
            simpleTextView.setText("Search destination");
            simpleTextView.setTextSize(16);
            simpleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            cardView.addView(simpleTextView,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT|Gravity.CENTER_VERTICAL,24 + 16 + 16,0,0,0));

        }

//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            setMeasuredDimension(widthMeasureSpec,MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(72),MeasureSpec.EXACTLY));
//        }

        public void setOnClick(OnClickListener onClickListener){
           cardView.setOnClickListener(onClickListener);
        }
    }
}
