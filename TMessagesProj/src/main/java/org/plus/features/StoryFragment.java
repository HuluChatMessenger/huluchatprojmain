package org.plus.features;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.ui.ProductDetailFragment;
import org.plus.apps.business.ui.components.LoadingView;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.plus.database.DataStorage;
import org.plus.features.data.FeatureDataModel;
import org.plus.features.data.FeatureDataStorage;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DialogsEmptyCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.SharedPhotoVideoCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.NotificationsCustomSettingsActivity;
import org.telegram.ui.PeopleNearbyActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.ProfileNotificationsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StoryFragment extends BaseFragment implements ImageUpdater.ImageUpdaterDelegate {

    private List<FeatureDataModel.ProfileChange> userChanges = new ArrayList<>();

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private ListAdapter adapter;

    private ImageUpdater imageUpdater;
    private ImageView floatingButton;
    private FrameLayout floatingButtonContainer;

    private TLRPC.FileLocation avatar;
    private TLRPC.FileLocation avatarBig;

    private View actionBarBackground;
    private AnimatorSet actionBarAnimator;

    @Override
    public boolean onFragmentCreate() {
        loadData();
        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        if (imageUpdater != null) {
            imageUpdater.clear();
        }

        super.onFragmentDestroy();
    }
    private boolean list;

    private void clearContactChange(){
      AlertDialog.Builder alert =   AlertsCreator.createSimpleAlert(getParentActivity(),"Clear","Clear all feed!");
      alert.setNegativeButton("Yes", (dialog, which) -> {

          FeatureDataStorage.getInstance(currentAccount).clearProfileChange(new ShopDataController.BooleanCallBack() {
              @Override
              public void onResponse(boolean susscess) {
                  AndroidUtilities.runOnUIThread(new Runnable() {
                      @Override
                      public void run() {
                          userChanges.clear();
                          updateRowCount();
                      }
                  });
              }
          });
          if(dialog != null){
              dialog.dismiss();
          }
      });
        alert.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
      showDialog(alert.create());
    }


    private int rowCount;
    private int yourFeedRow;
    private int yourFeedSecRow;
    private int feedHeaderRow;
    private int feedStartRow;
    private int feedEndRow;
    private int emptyFeedRow;


    public void updateRowCount(){
        rowCount = 0;
        yourFeedRow = rowCount++;
        yourFeedSecRow = rowCount++;

        int count  = userChanges.size();
        if (count != 0) {
            feedHeaderRow = -1;
            feedStartRow = rowCount;
            rowCount += count;
            feedEndRow = rowCount;
        } else {
            feedHeaderRow = -1;
            feedStartRow = -1;
            feedEndRow = -1;
        }

        if(feedStartRow == -1){
            emptyFeedRow = rowCount++;
        }else{
            emptyFeedRow = -1;
        }

        if(adapter != null){
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setBackgroundDrawable(null);
        actionBar.setTitleColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_listSelector), false);
        actionBar.setCastShadows(false);
        actionBar.setAddToContainer(false);
        actionBar.setOccupyStatusBar(Build.VERSION.SDK_INT >= 21 && !AndroidUtilities.isTablet());
        actionBar.setTitle("Your Feed");
        actionBar.getTitleTextView().setAlpha(0.0f);
        if (!AndroidUtilities.isTablet()) {
            actionBar.showActionModeTop();
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });


        fragmentView = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) actionBarBackground.getLayoutParams();
                layoutParams.height = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + AndroidUtilities.dp(3);

                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                checkScroll(false);
            }
        };
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        fragmentView.setTag(Theme.key_windowBackgroundGray);
        FrameLayout frameLayout = (FrameLayout) fragmentView;



        ActionBarMenu menu = actionBar.createMenu();
       // menu.addItem(1,list?R.drawable.ic_baseline_feed:R.drawable.ic_baseline_list);
        ActionBarMenuItem otherItem =  menu.addItem(2,R.drawable.ic_ab_other);
        otherItem.addSubItem(3,R.drawable.ic_ab_delete,LocaleController.getString("Delete",R.string.Delete));
      //  otherItem.addSubItem(5,R.drawable.menu_settings,LocaleController.getString("Settings",R.string.Settings));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }else if(id == 1){
                    list  = !list;
                    menu.getItem(1).setIcon(list?R.drawable.ic_baseline_feed:R.drawable.ic_baseline_list);
                    if(adapter != null){
                        adapter.notifyDataSetChanged();
                    }
                }else if(id == 3){
                    clearContactChange();
                }else if(id == 5){

                }
            }
        });



        listView = new RecyclerListView(context);
        listView.setGlowColor(0);
        listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        listView.setVerticalScrollBarEnabled(true);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(adapter = new ListAdapter(context));
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
               checkScroll(true);
            }
        });

        listView.setOnItemClickListener((view, position) -> {

        });

        ShopsEmptyCell dialogsEmptyCell  = new ShopsEmptyCell(context);
        dialogsEmptyCell.setType(ShopsEmptyCell.TYPE_FEED);
        frameLayout.addView(dialogsEmptyCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        listView.setEmptyView(dialogsEmptyCell);


        floatingButtonContainer = new FrameLayout(context);
        //frameLayout.addView(floatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 14, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 4 : 0, 0, LocaleController.isRTL ? 0 : 4, 0));
        floatingButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUpdater != null) {
                    TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId());
                    if (user == null) {
                        user = UserConfig.getInstance(currentAccount).getCurrentUser();
                    }
                    if (user == null) {
                        return;
                    }
                    imageUpdater.openMenu(user.photo != null && user.photo.photo_big != null && !(user.photo instanceof TLRPC.TL_userProfilePhotoEmpty), () -> MessagesController.getInstance(currentAccount).deleteUserPhoto(null));

                }
            }
        });


        floatingButton = new ImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButton.setBackgroundDrawable(drawable);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setImageResource(R.drawable.menu_camera2);

        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, View.TRANSLATION_Z, AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, View.TRANSLATION_Z, AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        floatingButtonContainer.setContentDescription(LocaleController.getString("NewMessageTitle", R.string.NewMessageTitle));
        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60), (Build.VERSION.SDK_INT >= 21 ? 56 : 60), Gravity.LEFT | Gravity.TOP, 10, 0, 10, 0));


        imageUpdater = new ImageUpdater(true);
        imageUpdater.setOpenWithFrontfaceCamera(true);
        imageUpdater.parentFragment = this;
        imageUpdater.setDelegate(this);
        getMediaDataController().checkFeaturedStickers();
        getMessagesController().loadSuggestedFilters();
        getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, classGuid);


        actionBarBackground = new View(context) {

            private Paint paint = new Paint();

            @Override
            protected void onDraw(Canvas canvas) {
                paint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                int h = getMeasuredHeight() - AndroidUtilities.dp(3);
                canvas.drawRect(0, 0, getMeasuredWidth(), h, paint);
                parentLayout.drawHeaderShadow(canvas, h);
            }
        };
        actionBarBackground.setAlpha(0.0f);
        frameLayout.addView(actionBarBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));


        return fragmentView;
    }
    @SuppressWarnings("FieldCanBeLocal")
    public class HintInnerCell extends CardView {

        private BackupImageView imageView;
        private TextView titleTextView;
        private TextView messageTextView;

        public HintInnerCell(Context context) {
            super(context);


            setCardElevation(4);
            setCardBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            int top = (int) ((ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0)) / AndroidUtilities.density) - 44;

            TLRPC.User user = getUserConfig().getCurrentUser();

            imageView = new BackupImageView(context);
            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
            imageView.setRoundRadius(AndroidUtilities.dp(74/2));
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            imageView.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);
            addView(imageView, LayoutHelper.createFrame(74, 74, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, top + 27, 0, 0));

            titleTextView = new TextView(context);
            titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            titleTextView.setGravity(Gravity.CENTER);
            titleTextView.setText(UserObject.getFirstName(user));
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, top + 120, 52, 27));

            messageTextView = new TextView(context);
            messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            messageTextView.setGravity(Gravity.CENTER);
            //messageTextView.setText("My profile");
          //  addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 40, top + 161, 40, 27));
        }
    }

    private int[] location = new int[2];
    private void checkScroll(boolean animated) {
        int first = layoutManager.findFirstVisibleItemPosition();
        boolean show;
        if (first != 0) {
            show = true;
        } else {
            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(first);
            if (holder == null) {
                show = true;
            } else {
                HintInnerCell hintInnerCell = (HintInnerCell) holder.itemView;
                hintInnerCell.titleTextView.getLocationOnScreen(location);
                show = location[1] + hintInnerCell.titleTextView.getMeasuredHeight() < actionBar.getBottom();
            }
        }
        boolean visible = actionBarBackground.getTag() == null;
        if (show != visible) {
            actionBarBackground.setTag(show ? null : 1);
            if (actionBarAnimator != null) {
                actionBarAnimator.cancel();
                actionBarAnimator = null;
            }
            if (animated) {
                actionBarAnimator = new AnimatorSet();
                actionBarAnimator.playTogether(
                        ObjectAnimator.ofFloat(actionBarBackground, View.ALPHA, show ? 1.0f : 0.0f),
                        ObjectAnimator.ofFloat(actionBar.getTitleTextView(), View.ALPHA, show ? 1.0f : 0.0f)
                );
                actionBarAnimator.setDuration(150);
                actionBarAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animation.equals(actionBarAnimator)) {
                            actionBarAnimator = null;
                        }
                    }
                });
                actionBarAnimator.start();
            } else {
                actionBarBackground.setAlpha(show ? 1.0f : 0.0f);
                actionBar.getTitleTextView().setAlpha(show ? 1.0f : 0.0f);
            }
        }
    }

    private void loadData(){
        getDataStorage().getStorageQueue().postRunnable(() -> {
            userChanges.clear();
            userChanges = getDataStorage().featureDao().getProfileChanges();
            Collections.sort(userChanges, profileChangeComparator);
            AndroidUtilities.runOnUIThread(() -> {
                updateRowCount();

            });
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
        if (imageUpdater != null) {
            imageUpdater.onResume();
        }

    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (imageUpdater != null) {
            imageUpdater.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void dismissCurrentDialog() {
        if (imageUpdater != null && imageUpdater.dismissCurrentDialog(visibleDialog)) {
            return;
        }
        super.dismissCurrentDialog();
    }
    @Override
    public boolean dismissDialogOnPause(Dialog dialog) {
        return (imageUpdater == null || imageUpdater.dismissDialogOnPause(dialog)) && super.dismissDialogOnPause(dialog);
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (imageUpdater != null) {
            imageUpdater.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void saveSelfArgs(Bundle args) {
        if (imageUpdater != null && imageUpdater.currentPicturePath != null) {
            args.putString("path", imageUpdater.currentPicturePath);
        }
    }

    @Override
    public void restoreSelfArgs(Bundle args) {
        if (imageUpdater != null) {
            imageUpdater.currentPicturePath = args.getString("path");
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        if (imageUpdater != null) {
            imageUpdater.onPause();
        }
    }

    private final Comparator<FeatureDataModel.ProfileChange> profileChangeComparator = (o1, o2) -> {
        if (o1.timeStamp > o2.timeStamp) {
            return -1;
        } else if (o1.timeStamp < o2.timeStamp) {
            return 1;
        }
        return 0;
    };

    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize) {
        AndroidUtilities.runOnUIThread(() -> {
            if (photo != null || video != null) {
                TLRPC.TL_photos_uploadProfilePhoto req = new TLRPC.TL_photos_uploadProfilePhoto();
                if (photo != null) {
                    req.file = photo;
                    req.flags |= 1;
                }
                if (video != null) {
                    req.video = video;
                    req.flags |= 2;
                    req.video_start_ts = videoStartTimestamp;
                    req.flags |= 4;
                }
                getConnectionsManager().sendRequest(req, (response, error) -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (error == null) {
                            TLRPC.User user = getMessagesController().getUser(getUserConfig().getClientUserId());
                            if (user == null) {
                                user = getUserConfig().getCurrentUser();
                                if (user == null) {
                                    return;
                                }
                                getMessagesController().putUser(user, false);
                            } else {
                                getUserConfig().setCurrentUser(user);
                            }
                            TLRPC.TL_photos_photo photos_photo = (TLRPC.TL_photos_photo) response;
                            ArrayList<TLRPC.PhotoSize> sizes = photos_photo.photo.sizes;
                            TLRPC.PhotoSize small = FileLoader.getClosestPhotoSizeWithSize(sizes, 150);
                            TLRPC.PhotoSize big = FileLoader.getClosestPhotoSizeWithSize(sizes, 800);
                            TLRPC.VideoSize videoSize = photos_photo.photo.video_sizes.isEmpty() ? null : photos_photo.photo.video_sizes.get(0);
                            user.photo = new TLRPC.TL_userProfilePhoto();
                            user.photo.photo_id = photos_photo.photo.id;
                            if (small != null) {
                                user.photo.photo_small = small.location;
                            }
                            if (big != null) {
                                user.photo.photo_big = big.location;
                            }

                            if (small != null && avatar != null) {
                                File destFile = FileLoader.getPathToAttach(small, true);
                                File src = FileLoader.getPathToAttach(avatar, true);
                                src.renameTo(destFile);
                                String oldKey = avatar.volume_id + "_" + avatar.local_id + "@50_50";
                                String newKey = small.location.volume_id + "_" + small.location.local_id + "@50_50";
                                ImageLoader.getInstance().replaceImageInCache(oldKey, newKey, ImageLocation.getForUser(user, false), true);
                            }
                            if (big != null && avatarBig != null) {
                                File destFile = FileLoader.getPathToAttach(big, true);
                                File src = FileLoader.getPathToAttach(avatarBig, true);
                                src.renameTo(destFile);
                            }
                            if (videoSize != null && videoPath != null) {
                                File destFile = FileLoader.getPathToAttach(videoSize, "mp4", true);
                                File src = new File(videoPath);
                                src.renameTo(destFile);
                            }

                            getMessagesStorage().clearUserPhotos(user.id);
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add(user);
                            getMessagesStorage().putUsersAndChats(users, null, false, true);
                        }
                        avatar = null;
                        avatarBig = null;
                        getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL);
                        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                        getUserConfig().saveConfig(true);
                    });
                });
            } else {
                avatar = smallSize.location;
                avatarBig = bigSize.location;
            }
        });
    }

    
    public void showMenu(FeatureDataModel.ProfileChange profileChange, BackupImageView  avatarImageView){
        BaseFragment parentFragment = StoryFragment.this;
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return;
        }
        TLRPC.User user = getMessagesController().getUser(profileChange.user_id);
        if(user == null){
            return;
        }

        String[] descriptions = new String[]{
                "Save photo",
                "Remove"
        };

        int[] icons = new int[]{
                R.drawable.files_gallery,
                R.drawable.msg_delete,
        };

        final LinearLayout linearLayout = new LinearLayout(parentFragment.getParentActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder builder = new AlertDialog.Builder(parentFragment.getParentActivity());

        for (int a = 0; a < descriptions.length; a++) {
            if (descriptions[a] == null) {
                continue;
            }
            TextView textView = new TextView(parentFragment.getParentActivity());
            Drawable drawable = parentFragment.getParentActivity().getResources().getDrawable(icons[a]);
            if (a == descriptions.length - 1) {
                textView.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogRedIcon), PorterDuff.Mode.MULTIPLY));
            } else {
                textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogIcon), PorterDuff.Mode.MULTIPLY));
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            textView.setTag(a);
            textView.setBackgroundDrawable(Theme.getSelectorDrawable(false));
            textView.setPadding(AndroidUtilities.dp(24), 0, AndroidUtilities.dp(24), 0);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(26));
            textView.setText(descriptions[a]);
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.TOP));
            textView.setOnClickListener(v -> {
                int i = (Integer) v.getTag();
                if(i == 0){
                    if (getParentActivity() == null) {
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 23 && getParentActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        getParentActivity().requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
                        return;
                    }
                    ImageLocation location = avatarImageView.getImageReceiver().getImageLocation();
                    if (location == null) {
                        return;
                    }
                    final boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
                    File f = FileLoader.getPathToAttach(location.location, isVideo ? "mp4" : null, true);
                    if (f.exists()) {
                        MediaController.saveFile(f.toString(), getParentActivity(), 0, null, null, () -> {
                            if (getParentActivity() == null) {
                                return;
                            }
                            BulletinFactory.createSaveToGalleryBulletin(StoryFragment.this, isVideo).show();
                        });
                    }
                }else if(i == 1){
                    DataStorage.storageQueue.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            getDataStorage().featureDao().delete(profileChange);
                            AndroidUtilities.runOnUIThread(new Runnable() {
                                @Override
                                public void run() {
                                    int index = -1;
                                    for (int j = 0; j < userChanges.size(); j++) {
                                        if(profileChange.user_id == userChanges.get(j).user_id){
                                            index = j;
                                            break;
                                        }
                                    }
                                   if(index != -1){
                                       userChanges.remove(index);
                                       updateRowCount();
                                   }
                                }
                            });
                        }
                    });
                }

                builder.getDismissRunnable().run();
            });
        }
        builder.setTitle(UserObject.getFirstName(user));
        builder.setView(linearLayout);
        parentFragment.showDialog(builder.create());
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if(viewType ==1){
                view = new HintInnerCell(mContext);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }else if(viewType == 2){
                view = new ShadowSectionCell(mContext);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            }else if(viewType == 3){
                view = new HeaderCell(mContext);
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            }else if(viewType == 4){
                ContactChangeCellFeed contactChangeCellFeed = new ContactChangeCellFeed(mContext);
                contactChangeCellFeed.setDelegate(new ContactChangeCellFeed.Delegate() {
                    @Override
                    public void onRxnPressed(FeatureDataModel.ProfileChange profileChange) {

                    }

                    @Override
                    public void onProfilePressed(FeatureDataModel.ProfileChange profileChange) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("user_id", profileChange.user_id);
                        bundle.putBoolean("expandPhoto", true);
                        ProfileActivity profileActivity = new ProfileActivity(bundle);
                        presentFragment(profileActivity);
                    }

                    @Override
                    public void onMessagePressed(FeatureDataModel.ProfileChange profileChange) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("user_id", profileChange.user_id);
                        ChatActivity profileActivity = new ChatActivity(bundle);
                        presentFragment(profileActivity);
                    }

                    @Override
                    public void onSharePressed(FeatureDataModel.ProfileChange profileChange,BackupImageView avatarImageView) {

                        ImageLocation location = avatarImageView.getImageReceiver().getImageLocation();
                        if (location == null) {
                            return;
                        }
                        final boolean isVideo = location.imageType == FileLoader.IMAGE_TYPE_ANIMATION;
                        File f = FileLoader.getPathToAttach(location.location, isVideo ? "mp4" : null, true);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        if (isVideo) {
                            intent.setType("video/mp4");
                        } else {
                            intent.setType("image/*");
                        }
                        if (Build.VERSION.SDK_INT >= 24) {
                            try {
                                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", f));
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignore)
                            {
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                            }
                        } else {
                            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                        }

                        startActivityForResult(Intent.createChooser(intent, LocaleController.getString("ShareFile", R.string.ShareFile)), 500);
                    }


                    @Override
                    public void onPhotoPressed(FeatureDataModel.ProfileChange profileChange,BackupImageView avtar) {
                        openPic(profileChange.user_id,avtar);

                    }

                    @Override
                    public void onMorePressed(FeatureDataModel.ProfileChange profileChange,BackupImageView backupImageView) {
                        showMenu(profileChange,backupImageView);
                    }
                });
                view = contactChangeCellFeed;
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            }else if(viewType == 5){
                ShopsEmptyCell dialogsEmptyCell  = new ShopsEmptyCell(mContext);
                dialogsEmptyCell.setType(ShopsEmptyCell.TYPE_FEED);
                view = dialogsEmptyCell;
            }else {
                view = new EmptyCell(mContext);
            }
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if(holder.getItemViewType() == 4){
                FeatureDataModel.ProfileChange profileChange = userChanges.get(position - feedStartRow);
                ContactChangeCellFeed contactChangeCell = (ContactChangeCellFeed)holder.itemView;
                contactChangeCell.setData(profileChange);
            }else if(holder.getItemViewType() == 3){
                HeaderCell contactChangeCellFeed = (HeaderCell)holder.itemView;
                contactChangeCellFeed.setText("Your Feed");
            }

        }


        @Override
        public int getItemViewType(int position) {
           if(position == yourFeedRow){
               return 1;
           } else if (position == yourFeedSecRow) {
               return 2;
           }else if(position == feedHeaderRow){
               return 3;
           }else if(position >= feedStartRow && position < feedEndRow){
               return 4;
           }else if(position == emptyFeedRow){
               return 5;
           }
           return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
    }



    private static class ContactChangeCellFeed extends LinearLayout{


        public  interface Delegate{

            void onRxnPressed(FeatureDataModel.ProfileChange profileChange);

            void onProfilePressed(FeatureDataModel.ProfileChange profileChange);

            void onMessagePressed(FeatureDataModel.ProfileChange profileChange);

            void onSharePressed(FeatureDataModel.ProfileChange profileChange,BackupImageView avatarImageView);

            void onPhotoPressed(FeatureDataModel.ProfileChange profileChange,BackupImageView avatarImageView);

            void onMorePressed(FeatureDataModel.ProfileChange profileChange,BackupImageView avtarImagView);
        }


        private Delegate delegate;

        public void setDelegate(Delegate delegate) {
            this.delegate = delegate;
        }



        private UserCell userCell;
        private BackupImageView imageView;
        private LinearLayout bottomLayout;


        private int itemSize;
        private View shadowView;

        private final GradientDrawable bottomOverlayGradient;

        private TextView textView;
        private ImageView moreImageView;

        private FeatureDataModel.ProfileChange proChange;


       // private LoadingView loadingView;
        public ContactChangeCellFeed(Context context) {
            super(context);

            setOrientation(VERTICAL);


            FrameLayout topFrame = new FrameLayout(context);
            addView(topFrame,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.TOP|Gravity.LEFT,0,0,0,0));



            moreImageView  = new ImageView(context);
            Drawable drawable = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ic_ab_other);
            drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            moreImageView.setImageDrawable(drawable);
            moreImageView.setScaleType(ImageView.ScaleType.CENTER);
            topFrame.addView(moreImageView,LayoutHelper.createFrame(48,48,Gravity.TOP|Gravity.RIGHT,0,0,0,0));
            moreImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delegate != null && proChange != null && imageView != null){
                        delegate.onMorePressed(proChange,imageView);
                    }
                }
            });


            userCell = new UserCell(context,16,0,true);
            topFrame.addView(userCell,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.TOP|Gravity.LEFT,0,0,48,0));
            userCell.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delegate != null && proChange != null){
                        delegate.onProfilePressed(proChange);
                    }
                }
            });
            FrameLayout frameLayout = new FrameLayout(context);
            imageView = new BackupImageView(context);
           // imageView.setVisibility(INVISIBLE);
            itemSize =(int) (SharedPhotoVideoCell.getItemSize(1) / AndroidUtilities.density);
            int itemHeight = (itemSize * 3)/2;
            frameLayout.addView(imageView,LayoutHelper.createFrame(itemSize,itemSize,Gravity.TOP|Gravity.LEFT,0,0,0,0));

//             loadingView = new LoadingView(context){
//
//                 @Override
//                 public int getHeightForFeed() {
//                     return itemHeight;
//                 }
//
//                 @Override
//                 public int getViewType() {
//                     return LoadingView.feed;
//                 }
//             };
//             frameLayout.addView(loadingView,LayoutHelper.createFrame(itemSize,itemSize,Gravity.TOP|Gravity.LEFT,0,0,0,0));


            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(delegate != null && proChange != null){
                        delegate.onPhotoPressed(proChange,imageView);
                    }
                }
            });

            bottomOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {0x80000000, 0});
            bottomOverlayGradient.setShape(GradientDrawable.RECTANGLE);


            shadowView = new View(context);
           // shadowView.setVisibility(INVISIBLE);
            shadowView.setBackground(bottomOverlayGradient);
            frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, (float)( itemHeight*0.2),Gravity.LEFT | Gravity.BOTTOM , 0, 0, 0, 0));

            int[] icons = new int[]{
                  //  R.drawable.msg_sticker,
                    R.drawable.msg_openprofile,
                    R.drawable.profile_newmsg,
                    R.drawable.msg_share,
            };
            bottomLayout = new LinearLayout(context);
            bottomLayout.setOrientation(HORIZONTAL);
            bottomLayout.setWeightSum(icons.length);

            for(int a = 0; a < icons.length; a ++){
                FrameLayout holder = new FrameLayout(context){
                    @Override
                    protected void onDraw(Canvas canvas) {
                        super.onDraw(canvas);
                        canvas.drawLine(getMeasuredWidth(),AndroidUtilities.dp(2),getMeasuredWidth() + 2,getMeasuredHeight(),Theme.dividerPaint);

                    }
                };
                holder.setWillNotDraw(false);
                holder.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

                drawable = ApplicationLoader.applicationContext.getResources().getDrawable(icons[a]);
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
                ImageView imageView2 = new ImageView(context){
                    @Override
                    protected void onDraw(Canvas canvas) {
                        super.onDraw(canvas);

                    }
                };
                imageView2.setImageDrawable(drawable);
                imageView2.setScaleType(ImageView.ScaleType.CENTER);
                holder.addView(imageView2, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                holder.setTag(a);
                holder.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      Integer tag =(Integer) v.getTag();
                      if(tag == -1){
                          if(delegate != null && proChange != null){
                              delegate.onRxnPressed(proChange);
                          }
                      }else if(tag == 0){
                          if(delegate != null && proChange != null){
                              delegate.onProfilePressed(proChange);
                          }
                      }else if(tag == 1){
                          if(delegate != null && proChange != null){
                              delegate.onMessagePressed(proChange);
                          }
                      }else if(tag == 2){
                          if(delegate != null && proChange != null){
                              delegate.onSharePressed(proChange, imageView);
                          }
                      }

                    }
                });
                bottomLayout.addView(holder,LayoutHelper.createLinear(0,LayoutHelper.MATCH_PARENT,1f));

            }

            addView(frameLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));


            addView(bottomLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,48));



            textView = new TextView(context);
            textView.setTextSize(16);
            textView.setMaxLines(2);
            textView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));
            textView.setGravity(Gravity.LEFT);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setVisibility(VISIBLE);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,Gravity.LEFT | Gravity.BOTTOM , 0, 0, 0, 0));


            ShadowSectionCell shadowSectionCell = new ShadowSectionCell(context);
            shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            addView(shadowSectionCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        }


        public void setData(FeatureDataModel.ProfileChange profileChange){

            proChange = profileChange;
            TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(profileChange.user_id);

            String status = LocaleController.formatUserStatus(UserConfig.selectedAccount,user);
            userCell.setData(user, null, status, 0, true);


            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(UserObject.getFirstName(user));
            int end = spannableStringBuilder.length();
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_dialogTextBlack)), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            long time = profileChange.timeStamp;
            spannableStringBuilder.append(" Changed profile at "  + LocaleController.formatDate(time));
            textView.setText(spannableStringBuilder);

             Drawable avatarDrawable = Theme.getDrawable(R.drawable.icplaceholder);
            imageView.setImage(ImageLocation.getForUser(user, true), null, avatarDrawable, user);
             imageView.getImageReceiver().setDelegate(new ImageReceiver.ImageReceiverDelegate() {
                 @Override
                 public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb, boolean memCache) {
                     if(set){
                        // loadingView.setVisibility(GONE);
                        // shadowView.setVisibility(VISIBLE);
                         //imageView.setVisibility(VISIBLE);
                     }
                 }
             });
        }
    }

    public void openPic(int user_idd,BackupImageView avatarImageView) {
        final Activity activity = (Activity)getParentActivity();
        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(user_idd);
        PhotoViewer.PhotoViewerProvider provider = new PhotoViewer.EmptyPhotoViewerProvider() {

            @Override
            public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index, boolean needPreview) {
                if (fileLocation == null) {
                    return null;
                }

                TLRPC.FileLocation photoBig = null;
                if (user_idd != 0) {
                    TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(user_idd);
                    if (user != null && user.photo != null && user.photo.photo_big != null) {
                        photoBig = user.photo.photo_big;
                    }
                }
//            else if (chat_id != 0) {
//                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(chat_id);
//                if (chat != null && chat.photo != null && chat.photo.photo_big != null) {
//                    photoBig = chat.photo.photo_big;
//                }
//            }

                if (photoBig != null && photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                    int[] coords = new int[2];
                    avatarImageView.getLocationInWindow(coords);
                    PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                    object.viewX = coords[0];
                    object.viewY = coords[1] - (Build.VERSION.SDK_INT >= 21 ? 0 : AndroidUtilities.statusBarHeight);
                    object.parentView = avatarImageView;
                    object.imageReceiver = avatarImageView.getImageReceiver();
                    if (user_idd != 0) {
                        object.dialogId = user_idd;
                    }
//                else if (chat_id != 0) {
//                    object.dialogId = -chat_id;
//                }
                    object.thumb = object.imageReceiver.getBitmapSafe();
                    object.size = -1;
                    object.radius = avatarImageView.getImageReceiver().getRoundRadius();
                    object.scale = avatarImageView.getScaleX();
                    return object;
                }
                return null;
            }

            @Override
            public void willHidePhotoViewer() {
                avatarImageView.getImageReceiver().setVisible(true, true);
            }
        };

        if (user_idd != 0) {
            if (user != null && user.photo != null && user.photo.photo_big != null) {
                PhotoViewer.getInstance().setParentActivity(activity);
                if (user.photo.dc_id != 0) {
                    user.photo.photo_big.dc_id = user.photo.dc_id;
                }
                PhotoViewer.getInstance().openPhoto(user.photo.photo_big, provider);
            }
        }

    }
}


