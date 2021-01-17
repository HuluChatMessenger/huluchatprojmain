/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.plus.features.data.FeatureDataStorage;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DrawerActionCell;
import org.telegram.ui.Cells.DividerCell;
import org.telegram.ui.Cells.DrawerAddCell;
import org.telegram.ui.Cells.DrawerUserCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.DrawerProfileCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SideMenultItemAnimator;

import java.util.ArrayList;
import java.util.Collections;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;

public class DrawerLayoutAdapter extends RecyclerListView.SelectionAdapter {

    private Context mContext;
    private ArrayList<Item> items = new ArrayList<>(11);
    private ArrayList<Integer> accountNumbers = new ArrayList<>();

    private boolean accountsShown;
    private boolean chatShown;
    private boolean featuresShown;

    private DrawerProfileCell profileCell;

    private DrawerExpandActionCell drawerExpandActionCell;
    //private DrawerExpandActionCell featuresExpandCell;

    private SideMenultItemAnimator itemAnimator;

    public DrawerLayoutAdapter(Context context, SideMenultItemAnimator animator) {
        mContext = context;
        itemAnimator = animator;
        accountsShown = UserConfig.getActivatedAccountsCount() > 1 && MessagesController.getGlobalMainSettings().getBoolean("accountsShown", true);

        Theme.createDialogsResources(context);
        resetItems();
    }

    private int getAccountRowsCount() {
        int count = accountNumbers.size() + 1;
        if (accountNumbers.size() < UserConfig.MAX_ACCOUNT_COUNT) {
            count++;
        }
        return count;
    }

    @Override
    public int getItemCount() {
        int count = items.size() + 2;
        if (accountsShown) {
            count += getAccountRowsCount();
        }
        return count;
    }

    public boolean isChatShown() {
        return chatShown;
    }

    public boolean isFeaturesShown() {
        return featuresShown;
    }

    public void setChatShow(boolean shown, boolean animated){
        if(chatShown == shown || itemAnimator.isRunning()){
            return;
        }
        chatShown = shown;

        if(drawerExpandActionCell != null){
            drawerExpandActionCell.setShow(chatShown);
            drawerExpandActionCell.setArrowState(animated);

        }
        if (animated) {
            itemAnimator.setShouldClipChildren(true);
            resetItems();
            int positionStart = 3;
            if (accountsShown) {
                positionStart =+ getAccountRowsCount();
            }
            if (chatShown) {
                notifyItemRangeInserted(positionStart , 3);
            } else {
                notifyItemRangeRemoved(positionStart   , 3);
            }
        } else {
            notifyDataSetChanged();
        }
    }

//    public void setFeaturesShown(boolean shown, boolean animated){
//        if(featuresShown == shown || itemAnimator.isRunning()){
//            return;
//        }
//        featuresShown = shown;
//        if(featuresExpandCell != null){
//            featuresExpandCell.setShow(featuresShown);
//            featuresExpandCell.setArrowState(animated);
//        }
//
//        if (animated) {
//            itemAnimator.setShouldClipChildren(true);
//            resetItems();
//            int positionStart = 5;
//            if (accountsShown) {
//                positionStart += getAccountRowsCount();
//            }
//
//            if(chatShown){
//                positionStart += 3;
//            }
//
//            if (featuresShown) {
//                notifyItemRangeInserted(positionStart , 2);
//            } else {
//                notifyItemRangeRemoved(positionStart  , 3);
//            }
//        } else {
//            notifyDataSetChanged();
//        }
//    }

    public void setAccountsShown(boolean value, boolean animated) {
        if (accountsShown == value || itemAnimator.isRunning()) {
            return;
        }
        accountsShown = value;
        if (profileCell != null) {
            profileCell.setAccountsShown(accountsShown, animated);
        }

        MessagesController.getGlobalMainSettings().edit().putBoolean("accountsShown", accountsShown).commit();
        if (animated) {
            itemAnimator.setShouldClipChildren(true);
            if (accountsShown) {
                notifyItemRangeInserted(2, getAccountRowsCount());
            } else {
                notifyItemRangeRemoved(2, getAccountRowsCount());
            }
        } else {
            notifyDataSetChanged();
        }
    }

    public boolean isAccountsShown() {
        return accountsShown;
    }

    @Override
    public void notifyDataSetChanged() {
        resetItems();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(RecyclerView.ViewHolder holder) {
        int itemType = holder.getItemViewType();
        return itemType == 3 || itemType == 4 || itemType == 5 || itemType == 6;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = profileCell = new DrawerProfileCell(mContext);
                break;
            case 2:
                view = new DividerCell(mContext);
                break;
            case 3:
                view = new DrawerActionCell(mContext);
                break;
            case 4:
                view = new DrawerUserCell(mContext);
                break;
            case 5:
                view = new DrawerAddCell(mContext);
                break;
            case 6:
                view = new DrawerExpandActionCell(mContext);
                break;
            case 1:
            default:
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
                break;
        }
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RecyclerListView.Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0: {
                DrawerProfileCell profileCell = (DrawerProfileCell) holder.itemView;
                profileCell.setUser(MessagesController.getInstance(UserConfig.selectedAccount).getUser(UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId()), accountsShown);
                break;
            } case 6:
                DrawerExpandActionCell expandCell = (DrawerExpandActionCell) holder.itemView;
                position -= 2;
                if (accountsShown) {
                    position -= getAccountRowsCount();
                }
                Item item = items.get(position);
                if(item.id == 12){
                    drawerExpandActionCell = expandCell;
                }
                items.get(position).bind(expandCell);
                drawerExpandActionCell.setPadding(0, 0, 0, 0);
                break;
            case 3: {
                DrawerActionCell drawerActionCell = (DrawerActionCell) holder.itemView;
                position -= 2;
                if (accountsShown) {
                    position -= getAccountRowsCount();
                }
                items.get(position).bind(drawerActionCell);
                if(items.get(position).id == 14){
                    //drawerActionCell.setCount(FeatureDataStorage.getInstance(UserConfig.selectedAccount).profileNumber);
                }else{
                    //drawerActionCell.removeCount();
                }
                drawerActionCell.setPadding(0, 0, 0, 0);
                break;
            }
            case 4: {
                DrawerUserCell drawerUserCell = (DrawerUserCell) holder.itemView;
                drawerUserCell.setAccount(accountNumbers.get(position - 2));
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        }
        i -= 2;
        if (accountsShown) {
            if (i < accountNumbers.size()) {
                return 4;
            } else {
                if (accountNumbers.size() < UserConfig.MAX_ACCOUNT_COUNT) {
                    if (i == accountNumbers.size()){
                        return 5;
                    } else if (i == accountNumbers.size() + 1) {
                        return 2;
                    }
                } else {
                    if (i == accountNumbers.size()) {
                        return 2;
                    }
                }
            }
            i -= getAccountRowsCount();
        }
        if (items.get(i) == null) {
            return 2;
        }

        if(items.get(i).id == 12 || items.get(i).id == 13){
            return 6;
        }
        return 3;
    }

    public void swapElements(int fromIndex, int toIndex) {
        int idx1 = fromIndex - 2;
        int idx2 = toIndex - 2;
        if (idx1 < 0 || idx2 < 0 || idx1 >= accountNumbers.size() || idx2 >= accountNumbers.size()) {
            return;
        }
        final UserConfig userConfig1 = UserConfig.getInstance(accountNumbers.get(idx1));
        final UserConfig userConfig2 = UserConfig.getInstance(accountNumbers.get(idx2));
        final int tempLoginTime = userConfig1.loginTime;
        userConfig1.loginTime = userConfig2.loginTime;
        userConfig2.loginTime = tempLoginTime;
        userConfig1.saveConfig(false);
        userConfig2.saveConfig(false);
        Collections.swap(accountNumbers, idx1, idx2);
        notifyItemMoved(fromIndex, toIndex);
    }

    private void resetItems() {
        accountNumbers.clear();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                accountNumbers.add(a);
            }
        }
        Collections.sort(accountNumbers, (o1, o2) -> {
            long l1 = UserConfig.getInstance(o1).loginTime;
            long l2 = UserConfig.getInstance(o2).loginTime;
            if (l1 > l2) {
                return 1;
            } else if (l1 < l2) {
                return -1;
            }
            return 0;
        });

        items.clear();
        if (!UserConfig.getInstance(UserConfig.selectedAccount).isClientActivated()) {
            return;
        }


        int eventType = Theme.getEventType();
        int newGroupIcon;
        int newSecretIcon;
        int newChannelIcon;
        int contactsIcon;
        int callsIcon;
        int savedIcon;
        int settingsIcon;
        int inviteIcon;
        int helpIcon;
        if (eventType == 0) {
            newGroupIcon = R.drawable.menu_groups_ny;
            newSecretIcon = R.drawable.menu_secret_ny;
            newChannelIcon = R.drawable.menu_channel_ny;
            contactsIcon = R.drawable.menu_contacts_ny;
            callsIcon = R.drawable.menu_calls_ny;
            savedIcon = R.drawable.menu_bookmarks_ny;
            settingsIcon = R.drawable.menu_settings_ny;
            inviteIcon = R.drawable.menu_invite_ny;
            helpIcon = R.drawable.menu_help_ny;
        } else if (eventType == 1) {
            newGroupIcon = R.drawable.menu_groups_14;
            newSecretIcon = R.drawable.menu_secret_14;
            newChannelIcon = R.drawable.menu_broadcast_14;
            contactsIcon = R.drawable.menu_contacts_14;
            callsIcon = R.drawable.menu_calls_14;
            savedIcon = R.drawable.menu_bookmarks_14;
            settingsIcon = R.drawable.menu_settings_14;
            inviteIcon = R.drawable.menu_secret_ny;
            helpIcon = R.drawable.menu_help;
        } else if (eventType == 2) {
            newGroupIcon = R.drawable.menu_groups_hw;
            newSecretIcon = R.drawable.menu_secret_hw;
            newChannelIcon = R.drawable.menu_broadcast_hw;
            contactsIcon = R.drawable.menu_contacts_hw;
            callsIcon = R.drawable.menu_calls_hw;
            savedIcon = R.drawable.menu_bookmarks_hw;
            settingsIcon = R.drawable.menu_settings_hw;
            inviteIcon = R.drawable.menu_invite_hw;
            helpIcon = R.drawable.menu_help_hw;
        } else {
            newGroupIcon = R.drawable.menu_groups;
            newSecretIcon = R.drawable.menu_secret;
            newChannelIcon = R.drawable.menu_broadcast;
            contactsIcon = R.drawable.menu_contacts;
            callsIcon = R.drawable.menu_calls;
            savedIcon = R.drawable.menu_saved;
            settingsIcon = R.drawable.menu_settings;
            inviteIcon = R.drawable.menu_invite;
            helpIcon = R.drawable.menu_help;
        }
        items.add(new Item(12,"Create Chat",R.drawable.msg_edit));
        if(chatShown){
            items.add(new Item(2, LocaleController.getString("NewGroup", R.string.NewGroup), newGroupIcon));
            items.add(new Item(3, LocaleController.getString("NewSecretChat", R.string.NewSecretChat), newSecretIcon));
            items.add(new Item(4, LocaleController.getString("NewChannel", R.string.NewChannel), newChannelIcon));//3
        }
        items.add(null);
//        items.add(new Item(13,"Features",R.drawable.ime_filter_icon_albums));
//        if(featuresShown){
//            items.add(new Item(14,"Contact Change",R.drawable.menu_contacts_changes));
//           // items.add(new Item(15,"Hidden Chat",R.drawable.ic_incoginito));
//            items.add(new Item(15,"Music",R.drawable.ime_cloud_filter_music));
//        }
        items.add(new Item(15,LocaleController.getString("AttachMusic",R.string.AttachMusic),R.drawable.ime_cloud_filter_music));
        items.add(new Item(14,"Feed",R.drawable.menu_contacts_changes));
        // items.add(null);
        items.add(new Item(16, LocaleController.getString("PeopleNearby", R.string.PeopleNearby), R.drawable.menu_nearby));
        items.add(new Item(6, LocaleController.getString("Contacts", R.string.Contacts), contactsIcon));
        items.add(new Item(10, LocaleController.getString("Calls", R.string.Calls), callsIcon));
        items.add(new Item(11, LocaleController.getString("SavedMessages", R.string.SavedMessages), savedIcon));
        items.add(new Item(8, LocaleController.getString("Settings", R.string.Settings), settingsIcon));
        items.add(null); // divider
        items.add(new Item(7, LocaleController.getString("OfficialChannel", R.string.OfficialChannel), R.drawable.menu_broadcast));
        items.add(new Item(9,"Share HuluChat" ,R.drawable.msg_share));




    }




    public int getId(int position) {
        position -= 2;
        if (accountsShown) {
            position -= getAccountRowsCount();
        }
        if (position < 0 || position >= items.size()) {
            return -1;
        }
        Item item = items.get(position);
        return item != null ? item.id : -1;
    }

    public int getFirstAccountPosition() {
        if (!accountsShown) {
            return RecyclerView.NO_POSITION;
        }
        return 2;
    }

    public int getLastAccountPosition() {
        if (!accountsShown) {
            return RecyclerView.NO_POSITION;
        }
        return 1 + accountNumbers.size();
    }

    private static class Item {
        public int icon;
        public String text;
        public int id;

        public Item(int id, String text, int icon) {
            this.icon = icon;
            this.id = id;
            this.text = text;
        }

        public void bind(DrawerActionCell actionCell) {
            actionCell.setTextAndIcon(text, icon);
        }

        public void bind(DrawerExpandActionCell actionCell) {
            actionCell.setTextAndIcon(text, icon);
        }

        public void setCount(DrawerActionCell actionCell,int count){
          //  actionCell.setCount(count);
        }
    }


    public static class DrawerExpandActionCell extends FrameLayout {

        private TextView textView;
        private RectF rect = new RectF();
        private ImageView arrowView;


        private boolean show;

        public DrawerExpandActionCell(Context context) {
            super(context);

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView.setCompoundDrawablePadding(AndroidUtilities.dp(29));
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 19, 0, 16, 0));

            arrowView = new ImageView(context);
            arrowView.setScaleType(ImageView.ScaleType.CENTER);
            Drawable drawable = getResources().getDrawable(R.drawable.menu_expand).mutate();
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.MULTIPLY));
            }
            arrowView.setImageDrawable(drawable);
            addView(arrowView, LayoutHelper.createFrame(59, 59, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
            setArrowState(false);
        }



        public void setShow(boolean expand){
            show = expand;
        }

        private void setArrowState(boolean animated) {
            final float rotation = show ?-90.0f : 0.0f;
            if (animated) {
                arrowView.animate().rotation(rotation).setDuration(220).setInterpolator(CubicBezierInterpolator.EASE_OUT).start();
            } else {
                arrowView.animate().cancel();
                arrowView.setRotation(rotation);
            }
            arrowView.setContentDescription(show ? LocaleController.getString("AccDescrHideAccounts", R.string.AccDescrHideAccounts) : LocaleController.getString("AccDescrShowAccounts", R.string.AccDescrShowAccounts));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            arrowView.getDrawable().setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.MULTIPLY));
        }

        public void setTextAndIcon(String text, int resId) {
            try {
                textView.setText(text);
                Drawable drawable = getResources().getDrawable(resId).mutate();
                if (drawable != null) {
                    drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_menuItemIcon), PorterDuff.Mode.MULTIPLY));
                }
                textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

}
