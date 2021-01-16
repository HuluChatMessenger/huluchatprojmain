//package org.plus.features;
//
//import android.content.Context;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.R;
//import org.telegram.messenger.SharedConfig;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.ActionBarMenu;
//import org.telegram.ui.ActionBar.ActionBarMenuItem;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Cells.HintDialogCell;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RecyclerListView;
//
//
//public class HiddenChatFragment extends BaseFragment{
//
//    private RecyclerListView listView;
//    private GridLayoutManager layoutManager;
//    private ListAdapter adapter;
//
//    private LinearLayout emptyLayout;
//    private TextView emptyTextView;
//    private ImageView emptyImageView;
//
//    private int coll_count = 4;
//
//    private static final int password = 1;
//
//    private ActionBarMenuItem passcodeItem;
//    private boolean psscodeItemVisible;
//
//
//    @Override
//    public boolean onFragmentCreate() {
//        return super.onFragmentCreate();
//    }
//
//
//    @Override
//    public void onFragmentDestroy() {
//        super.onFragmentDestroy();
//    }
//
//
//
//    @Override
//    public View createView(Context context) {
//
//        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
//        actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultArchived));
//        actionBar.setAllowOverlayTitle(false);
//        actionBar.setTitle("Hidden Space");
//
//        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
//
//            @Override
//            public void onItemClick(int id) {
//                if(id == -1){
//                    finishFragment();
//                }
//            }
//        });
//
//        ActionBarMenu menu = actionBar.createMenu();
//        passcodeItem = menu.addItem(1, R.drawable.lock_close);
//        updatePasscodeButton();
//
//
//        fragmentView = new FrameLayout(context);
//        FrameLayout frameLayout = (FrameLayout) fragmentView;
//        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
//
//        listView = new RecyclerListView(context);
//        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
//        listView.setLayoutManager(layoutManager = new GridLayoutManager(context, coll_count));
//        listView.setVerticalScrollBarEnabled(false);
//        listView.setGlowColor(Theme.getColor(Theme.key_actionBarDefaultArchived));
//        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        listView.setAdapter(adapter = new ListAdapter(context));
//        listView.setOnItemClickListener((view, position, x, y) -> {
//        });
//
//        emptyLayout = new LinearLayout(context);
//        emptyLayout.setOrientation(LinearLayout.VERTICAL);
//        emptyLayout.setGravity(Gravity.CENTER);
//        emptyLayout.setVisibility(View.GONE);
//        frameLayout.addView(emptyLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
//        emptyLayout.setOnTouchListener((v, event) -> true);
//
//         emptyImageView = new ImageView(context);
//        // emptyImageView.setImageResource(R.drawable.ic_incoginito);
//         emptyLayout.addView(emptyImageView, LayoutHelper.createLinear(200,200));
//
//         emptyTextView = new TextView(context);
//         emptyTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//         emptyTextView.setGravity(Gravity.CENTER);
//         emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
//         emptyTextView.setText("No hidden chat!");
//         emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
//         emptyLayout.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));
//
//         listView.setEmptyView(emptyLayout);
//
//
//        return fragmentView;
//
//    }
//
//
//
//    private void updatePasscodeButton() {
//        if (passcodeItem == null) {
//            return;
//        }
//        if (SharedConfig.passcodeHash.length() != 0) {
//            passcodeItem.setVisibility(View.VISIBLE);
//            psscodeItemVisible = true;
//            if (SharedConfig.appLocked) {
//                passcodeItem.setIcon(R.drawable.lock_close);
//            } else {
//                passcodeItem.setIcon(R.drawable.lock_open);
//            }
//        } else {
//          //  passcodeItem.setVisibility(View.GONE);
//            //psscodeItemVisible = false;
//        }
//    }
//
//    private  class ListAdapter extends RecyclerListView.SelectionAdapter{
//
//        private Context mContext;
//
//        public ListAdapter(Context mContext) {
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
//            return new RecyclerListView.Holder(new HintDialogCell(mContext));
//        }
//
//
//
//        @Override
//        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//            HintDialogCell cell = (HintDialogCell)holder.itemView;
//            TLRPC.Dialog dialog =  FeatureDataController.getInstance(currentAccount).hiddenDialogs.get(position);
//            TLRPC.Chat chat = null;
//            TLRPC.User user = null;
////            int did = 0;
////            if (dialog.peer.user_id != 0) {
////                did = dialog.peer.user_id;
////                user = MessagesController.getInstance(currentAccount).getUser(dialog.peer.user_id);
////            } else if (dialog.peer.channel_id != 0) {
////                did = -dialog.peer.channel_id;
////                chat = MessagesController.getInstance(currentAccount).getChat(dialog.peer.channel_id);
////            } else if (dialog.peer.chat_id != 0) {
////                did = -dialog.peer.chat_id;
////                chat = MessagesController.getInstance(currentAccount).getChat(dialog.peer.chat_id);
////            }            String name = "";
////            if (user != null) {
////                name = UserObject.getFirstName(user);
////            } else if (chat != null) {
////                name = chat.title;
////            }
//            cell.setDialog((int)dialog.id, true,"Chat titile");
//        }
//
//
//        @Override
//        public int getItemCount() {
//            return FeatureDataController.getInstance(currentAccount).hiddenDialogs.size();
//        }
//    }
//
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if(adapter != null){
//            adapter.notifyDataSetChanged();
//        }
//    }
//}
