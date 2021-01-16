//package org.plus.features;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.RectF;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.Layout;
//import android.text.StaticLayout;
//import android.text.TextUtils;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.plus.apps.business.ui.components.ShopInfoInnerCell;
//import org.plus.database.DataStorage;
//import org.plus.features.data.FeatureDataModel;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ImageLocation;
//import org.telegram.messenger.ImageReceiver;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MessagesController;
//import org.telegram.messenger.NotificationCenter;
//import org.telegram.messenger.R;
//import org.telegram.messenger.UserConfig;
//import org.telegram.messenger.UserObject;
//import org.telegram.tgnet.ConnectionsManager;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.HintDialogCell;
//import org.telegram.ui.Cells.ShadowSectionCell;
//import org.telegram.ui.Components.AvatarDrawable;
//import org.telegram.ui.Components.BackupImageView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RecyclerListView;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//public class StoryFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{
//
//    private List<FeatureDataModel.ProfileChange> userChanges = new ArrayList<>();
//
//    private RecyclerListView listView;
//    private LinearLayoutManager layoutManager;
//    private ListAdapter adapter;
//
//    private View actionBarBackground;
//    private AnimatorSet actionBarAnimator;
//
//    private interface StoryDelegate{
//
//        void onStoryPressed(FeatureDataModel.ProfileChange profileChange);
//    }
//
//    private RecyclerListView innerListView;
//
//    private int rowCount;
//    private int storyRow;
//    private int timeLineSecRow;
//
//
//    private void updateRow(){
//        rowCount = 0;
//        storyRow = rowCount++;
//        timeLineSecRow = rowCount++;
//
//    }
//
//
//    @Override
//    public boolean onFragmentCreate() {
//        loadData();
//        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
//        return super.onFragmentCreate();
//    }
//
//    @Override
//    public void onFragmentDestroy() {
//        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
//        super.onFragmentDestroy();
//    }
//
//    @Override
//    public View createView(Context context) {
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), false);
//        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_listSelector), false);
//        actionBar.setCastShadows(false);
//        actionBar.setAddToContainer(true);
//        actionBar.setTitle("Feed");
//
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
//            @Override
//            public void onItemClick(int id) {
//                if (id == -1) {
//                    finishFragment();
//                }
//            }
//        });
//
//        fragmentView = new FrameLayout(context);
//        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//        fragmentView.setTag(Theme.key_windowBackgroundWhite);
//        FrameLayout frameLayout = (FrameLayout) fragmentView;
//
//        listView = new RecyclerListView(context);
//        listView.setGlowColor(0);
//        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
//        listView.setAdapter(adapter = new ListAdapter(context));
//        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
//        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//
//
//        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                // checkScroll(true);
//            }
//        });
//
//        listView.setOnItemClickListener((view, position) -> {
//
//        });
//
//
//        updateRow();
//        return fragmentView;
//    }
//
//
//    private void loadData(){
//        DataStorage.getDatabase(currentAccount).getStorageQueue().postRunnable(() -> {
//            if (userChanges == null) {
//                return;
//            }
//            userChanges.clear();
//            userChanges = DataStorage.getDatabase(currentAccount).featureDao().getProfileChanges();
//            Collections.sort(userChanges, profileChangeComparator);
//            AndroidUtilities.runOnUIThread(() -> {
////
//                if(adapter != null){
//                    adapter.notifyDataSetChanged();
//                }
//
//                if (storyAdapter != null) {
//                    storyAdapter.notifyDataSetChanged();
//                    //  adapter.notifyDataSetChanged();
//
//                }
//            });
//        });
//    }
//
//
//    private int[] location = new int[2];
//    private void checkScroll(boolean animated) {
//        int first = layoutManager.findFirstVisibleItemPosition();
//        boolean show;
//        if (first != 0) {
//            show = true;
//        } else {
//            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(first);
//            if (holder == null) {
//                show = true;
//            } else {
//                ShopInfoInnerCell hintInnerCell = (ShopInfoInnerCell) holder.itemView;
//                hintInnerCell.getTitleTextView().getLocationOnScreen(location);
//                show = location[1] + hintInnerCell.getTitleTextView().getMeasuredHeight() < actionBar.getBottom();
//            }
//        }
//        boolean visible = actionBarBackground.getTag() == null;
//        if (show != visible) {
//            actionBarBackground.setTag(show ? null : 1);
//            if (actionBarAnimator != null) {
//                actionBarAnimator.cancel();
//                actionBarAnimator = null;
//            }
//            if (animated) {
//                actionBarAnimator = new AnimatorSet();
//                actionBarAnimator.playTogether(
//                        ObjectAnimator.ofFloat(actionBarBackground, View.ALPHA, show ? 1.0f : 0.0f),
//                        ObjectAnimator.ofFloat(actionBar.getTitleTextView(), View.ALPHA, show ? 1.0f : 0.0f)
//                );
//                actionBarAnimator.setDuration(150);
//                actionBarAnimator.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        if (animation.equals(actionBarAnimator)) {
//                            actionBarAnimator = null;
//                        }
//                    }
//                });
//                actionBarAnimator.start();
//            } else {
//                actionBarBackground.setAlpha(show ? 1.0f : 0.0f);
//                actionBar.getTitleTextView().setAlpha(show ? 1.0f : 0.0f);
//            }
//        }
//    }
//
//
//    @Override
//    public void onResume() {
//        if(adapter != null){
//            adapter.notifyDataSetChanged();
//        }
//        super.onResume();
//    }
//
//    private final Comparator<FeatureDataModel.ProfileChange> profileChangeComparator = (o1, o2) -> {
//        if (o1.timeStamp > o2.timeStamp) {
//            return -1;
//        } else if (o1.timeStamp < o2.timeStamp) {
//            return 1;
//        }
//        return 0;
//    };
//
//
//    private StoryAdapter storyAdapter;
//    private class ListAdapter extends RecyclerListView.SelectionAdapter{
//
//
//        private Context mContext;
//
//        public ListAdapter(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return false;
//        }
//
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType){
//                case 1:
//                default:
//                    view = new ShadowSectionCell(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//                    break;
//                case 2:
//                    RecyclerListView horizontalListView = new RecyclerListView(mContext) {
//                        @Override
//                        public boolean onInterceptTouchEvent(MotionEvent e) {
//                            if (getParent() != null && getParent().getParent() != null) {
//                                getParent().getParent().requestDisallowInterceptTouchEvent(canScrollHorizontally(-1) || canScrollHorizontally(1));
//                            }
//                            return super.onInterceptTouchEvent(e);
//                        }
//                    };
//                    horizontalListView.setTag(9);
//                    // horizontalListView.setItemAnimator(null);
//                    //horizontalListView.setLayoutAnimation(null);
//                    LinearLayoutManager layoutManager = new LinearLayoutManager(mContext) {
//                        @Override
//                        public boolean supportsPredictiveItemAnimations() {
//                            return true;
//                        }
//                    };
//                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//                    horizontalListView.setLayoutManager(layoutManager);
//                    //horizontalListView.setDisallowInterceptTouchEvents(true);
//                    horizontalListView.setAdapter(storyAdapter = new StoryAdapter(mContext));
////                    horizontalListView.setOnItemClickListener((view1, position) -> {
////\                    });
//                    view = horizontalListView;
//                    innerListView = horizontalListView;
//                    break;
//            }
//            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if(position == timeLineSecRow){
//                return 1;
//            }else if(position == storyRow){
//                return 2;
//            }
//            return super.getItemViewType(position);
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//    }
//    @Override
//    public void didReceivedNotification(int id, int account, Object... args) {
//
//    }
//
//    private class StoryAdapter extends RecyclerListView.SelectionAdapter{
//
//        private StoryDelegate storyDelegate;
//
//        public void setStoryDelegate(StoryDelegate storyDelegate) {
//            this.storyDelegate = storyDelegate;
//        }
//
//        private Context mContext;
//
//        public StoryAdapter(Context mContext) {
//            this.mContext = mContext;
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//            return true;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = new StoryCell(mContext);
//            view.setLayoutParams(new RecyclerView.LayoutParams(AndroidUtilities.dp(120), AndroidUtilities.dp(120)));
//            return new RecyclerListView.Holder(view);        }
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//
//            StoryCell hintDialogCell = (StoryCell)holder.itemView;
//            FeatureDataModel.ProfileChange user = userChanges.get(position);
//            hintDialogCell.setUser(user);
//            hintDialogCell.setTag(user.userid);
//            hintDialogCell.setListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("user_id",user.userid);
//                    bundle.putInt("count",1);
//                    StoryViewerFragment storyViewerFragment = new StoryViewerFragment(bundle);
//                    presentFragment(storyViewerFragment);
//                }
//            });
//
////            instaImageView.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View v) {
////
////                    switch (instaImageView.getStatus()) {
////                        case UNCLICKED:
////                            instaImageView.setStatus(InsLoadingView.Status.LOADING);
////                            break;
////                        case LOADING:
////                            instaImageView.setStatus(InsLoadingView.Status.CLICKED);
////                            break;
////                        case CLICKED:
////                            instaImageView.setStatus(InsLoadingView.Status.UNCLICKED);
////                    }
////                }
////            });
//
//        }
//
//        @Override
//        public int getItemCount() {
//            int count = 0;
//            if(userChanges != null){
//                count = userChanges.size();
//            }
//            return count;
//        }
//    }
//
//    public class StoryCell extends FrameLayout {
//
//
//        public void setListener(OnClickListener onClickListener){
//            instaImageView.setOnClickListener(onClickListener);
//        }
//
//        private TextView nameTextView;
//        private AvatarDrawable avatarDrawable = new AvatarDrawable();
//        private RectF rect = new RectF();
//
//
//
//
//        private int lastUnreadCount;
//        private int countWidth;
//        private StaticLayout countLayout;
//        private TLRPC.User currentUser;
//
//        private long dialog_id;
//        private int currentAccount = UserConfig.selectedAccount;
//
//        private InsLoadingView instaImageView;
//
//        public InsLoadingView getInsLoadingView() {
//            return instaImageView;
//        }
//
//        public StoryCell(Context context) {
//            super(context);
//
//            instaImageView  = new InsLoadingView(context);
//            instaImageView.setImageResource(R.drawable.circle_big);
//
//            addView(instaImageView, LayoutHelper.createFrame(100, 100, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 7, 0, 0));
//
//
//            nameTextView = new TextView(context);
//            nameTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
//            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
//            nameTextView.setMaxLines(1);
//            nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
//            nameTextView.setLines(1);
//            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
//            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, 110, 6, 0));
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(130), MeasureSpec.EXACTLY));
//        }
//
//
//        public void setUser(FeatureDataModel.ProfileChange change){
//            TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(change.userid);
//            if(user != null){
//                nameTextView.setText(UserObject.getFirstName(user));
//            }
//            currentUser = user;
//            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
//            instaImageView.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);
//
//        }
//
//
//        @Override
//        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
//            boolean result = super.drawChild(canvas, child, drawingTime);
//            if (child == instaImageView) {
//                if (countLayout != null) {
//                    int top = AndroidUtilities.dp(6);
//                    int left = AndroidUtilities.dp(54);
//                    int x = left - AndroidUtilities.dp(5.5f);
//                    rect.set(x, top, x + countWidth + AndroidUtilities.dp(11), top + AndroidUtilities.dp(23));
//                    canvas.drawRoundRect(rect, 11.5f * AndroidUtilities.density, 11.5f * AndroidUtilities.density, MessagesController.getInstance(currentAccount).isDialogMuted(dialog_id) ? Theme.dialogs_countGrayPaint : Theme.dialogs_countPaint);
//                    canvas.save();
//                    canvas.translate(left, top + AndroidUtilities.dp(4));
//                    countLayout.draw(canvas);
//                    canvas.restore();
//                }
//
//                if (currentUser != null && !currentUser.bot && (currentUser.status != null && currentUser.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime() || MessagesController.getInstance(currentAccount).onlinePrivacy.containsKey(currentUser.id))) {
//                    int top = AndroidUtilities.dp(53);
//                    int left = AndroidUtilities.dp(59);
//                    Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//                    canvas.drawCircle(left, top, AndroidUtilities.dp(7), Theme.dialogs_onlineCirclePaint);
//                    Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_chats_onlineCircle));
//                    canvas.drawCircle(left, top, AndroidUtilities.dp(5), Theme.dialogs_onlineCirclePaint);
//                }
//
//            }
//            return result;
//        }
//    }
//
//}
