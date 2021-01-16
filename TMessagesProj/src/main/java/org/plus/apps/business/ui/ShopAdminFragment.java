package org.plus.apps.business.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataModels;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxUserCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Cells.UserCell2;
import org.telegram.ui.ChatRightsEditActivity;
import org.telegram.ui.ChatUsersActivity;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.GroupCreateActivity;

import java.util.ArrayList;

public class ShopAdminFragment extends BaseFragment {

    private ArrayList<ShopDataSerializer.User> admins = new ArrayList<>();
    private ArrayList<Integer> admins_id = new ArrayList<>();

    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private LinearLayoutManager layoutManager;
    private EmptyTextProgressView progressView;
    private SwipeRefreshLayout refreshLayout;




    private int chat_id;

    private int rowCount;
    private int addAdminRow;
    private int adminsSecRow;
    private int adminsStartRow;
    private int adminsEndRow;
    private int infoRow;

    private void updateRow(){

         rowCount = 0;
         addAdminRow= -1;
         adminsSecRow= -1;
         adminsStartRow= -1;
         adminsEndRow= -1;
         infoRow = -1;

         int count = admins.size();

         if(count > 0){
             addAdminRow =  rowCount++;
             adminsSecRow = rowCount++;

             adminsStartRow = rowCount;
             rowCount += count;
             adminsEndRow = rowCount;

             infoRow = rowCount++;
        }

         if(listAdapter != null){
             listAdapter.notifyDataSetChanged();
         }

    }
    private ShopDataSerializer.Shop currentShop;
    public ShopAdminFragment(Bundle args,ShopDataSerializer.Shop shop){
        super(args);
        currentShop = shop;
        chat_id = getArguments().getInt("chat_id");
        loadAdmins();
    }


    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }


    private boolean adminLoaded;

    private void loadAdmins(){
        if(adminLoaded){
            return;
        }
        adminLoaded = true;
        getShopDataController().getShopAdmins(chat_id, new ShopDataController.ResponseDelegate() {
            @Override
            public void run(Object response, APIError error) {
                adminLoaded = false;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if(refreshLayout !=null && refreshLayout.isRefreshing()){
                            refreshLayout.setRefreshing(false);
                        }
                        if(error == null){
                            admins = (ArrayList<ShopDataSerializer.User>)response;
                            admins_id.clear();
                            for(int a = 0; a < admins.size();a++){
                                ShopDataSerializer.User user = admins.get(a);
                                if(user != null){
                                    admins_id.add(user.id);
                                }
                            }
                            updateRow();
                        }
                    }
                });

            }
        });
    }





    @Override
    public View createView(Context context) {
        actionBar.setTitle(LocaleController.getString("ChannelAdministrator", R.string.ChannelAdministrator));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if(id == -1){
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;

        refreshLayout = new SwipeRefreshLayout(context);
        frameLayout.addView(refreshLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));


        listView = new RecyclerListView(context);
        listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        ((SimpleItemAnimator) listView.getItemAnimator()).setSupportsChangeAnimations(false);
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);
        refreshLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adminLoaded =false;
                loadAdmins();
            }
        });

        progressView = new EmptyTextProgressView(context);
        progressView.showProgress();
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setEmptyView(progressView);

        return fragmentView;
    }




    private class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() != 3;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           View view;
           switch (viewType){
               case 1:
                   view =new  ManageChatTextCell(context);
                   break;
               case 2:
                   ManageChatUserCell  actionCell = new ManageChatUserCell(context,6,2,true);
                   view = actionCell;
                   break;
               case 4:
                   TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(context);
                   view = textInfoPrivacyCell;
                   textInfoPrivacyCell.setText(LocaleController.getString(",R.string.ChannelAdminsInfo",R.string.ChannelAdminsInfo));
                   break;
               case 3:
               default:
                   view = new ShadowSectionCell(context);
                   view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
           }
           view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }



        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 1) {
                ManageChatTextCell actionCell = (ManageChatTextCell) holder.itemView;
                actionCell.setColors(Theme.key_windowBackgroundWhiteBlueIcon, Theme.key_windowBackgroundWhiteBlueButton);
                actionCell.setText(LocaleController.getString("ChannelAddAdmin", R.string.ChannelAddAdmin), null, R.drawable.add_admin, false);
                actionCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle args = new Bundle();
                        args.putBoolean("addtoShopAdmin", true);
                        args.putInt("chatId",chat_id);
                        GroupCreateActivity fragment = new GroupCreateActivity(args);
                        fragment.setDelegate(new GroupCreateActivity.ContactsAddActivityDelegate() {
                            @Override
                            public void didSelectUsers(ArrayList<TLRPC.User> users, int fwdCount) {
                                for (int a = 0, N = users.size(); a < N; a++) {
                                    TLRPC.User user = users.get(a);
                                    getShopDataController().addAdmin(chat_id, user.id, new ShopDataController.ResponseDelegate() {
                                        @Override
                                        public void run(Object response, APIError error) {
                                            AndroidUtilities.runOnUIThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(response instanceof ShopDataSerializer.User){
                                                        admins.add((ShopDataSerializer.User) response);
                                                        updateRow();
                                                        if(listAdapter != null){
                                                            listAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void needAddBot(TLRPC.User user) {
                               // openRightsEdit(user.id, null, null, null, "", true, ChatRightsEditActivity.TYPE_ADMIN, false);
                            }
                        });
                        presentFragment(fragment);
                    }
                });
            } else if (holder.getItemViewType() == 2) {
                ManageChatUserCell actionCell = (ManageChatUserCell) holder.itemView;
                ShopDataSerializer.User user = admins.get(position - adminsStartRow);
                boolean show = true;
                if(currentShop.created_by != null && currentShop.created_by.telegramId == user.telegramId){
                    show = false;
                }
                actionCell.setDelegate(new ManageChatUserCell.ManageChatUserCellDelegate() {
                    @Override
                    public boolean onOptionsButtonCheck(ManageChatUserCell cell, boolean click) {
                        createMenu(user,position);
                        return false;
                    }
                });
                if (user != null) {
                   // actionCell.setUser(user, true,show);
                }

            }
        }



        @Override
        public int getItemViewType(int position) {
            if(position == addAdminRow){
                return 1;
            }if(position >= adminsStartRow && position < adminsEndRow){
                return 2;
            }else if(position == adminsSecRow){
                return 3;
            }else if(position == infoRow){
                return 4;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }
    }



    private void createMenu( ShopDataSerializer.User user,int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());

        CharSequence[] items = new CharSequence[]{
                LocaleController.getString("ChannelRemoveUserAdmin", R.string.ChannelRemoveUserAdmin)};
        int[] icons = new int[]{
                R.drawable.actions_remove_user};
        builder.setItems(items, icons, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    getShopDataController().removeAdminFromShop(chat_id, user.id, new ShopDataController.ResponseDelegate() {
                        @Override
                        public void run(Object response, APIError error) {
                            if(error == null){
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        admins.remove(user);
                                        updateRow();
                                        listAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        showDialog(alertDialog);
        alertDialog.setItemColor(items.length - 1, Theme.getColor(Theme.key_dialogTextRed2), Theme.getColor(Theme.key_dialogRedIcon));

    }
}
