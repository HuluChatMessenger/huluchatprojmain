package org.plus.features;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.features.PlusConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class ChatSortingFragment extends BaseFragment {

    private boolean orderChange;
    private boolean visibilityChange;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;

    private int rowCount;

    private int autoSortingRow;
    private int firstDividerRow;
    private int chatSortStart;
    private int chatSortEnd;
    private int secondDividerRow;

    public static class SortCell extends FrameLayout {

        private ImageView iconImageView;
        private TextView textView;
        private boolean needDivider;

        public SortCell(Context context) {
            super(context);
            setWillNotDraw(false);

            ImageView moveImageView = new ImageView(context);
            moveImageView.setFocusable(false);
            moveImageView.setScaleType(ImageView.ScaleType.CENTER);
            moveImageView.setImageResource(R.drawable.list_reorder);
            moveImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
            moveImageView.setContentDescription(LocaleController.getString("FilterReorder", R.string.FilterReorder));
            moveImageView.setClickable(true);
            addView(moveImageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 6, 0, 6, 0));

            iconImageView = new ImageView(context);
            iconImageView.setFocusable(false);
            iconImageView.setScaleType(ImageView.ScaleType.CENTER);
            iconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
            iconImageView.setClickable(true);
            addView(iconImageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 54, 0, 54, 0));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 120 : 104, 14, LocaleController.isRTL ? 104 : 120, 0));

            TextView valueTextView = new TextView(context);
            valueTextView.setText("Local");
            valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            valueTextView.setLines(1);
            valueTextView.setMaxLines(1);
            valueTextView.setSingleLine(true);
            valueTextView.setPadding(0, 0, 0, 0);
            valueTextView.setEllipsize(TextUtils.TruncateAt.END);
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 120 : 104, 35, LocaleController.isRTL ? 104 : 120, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
        }

        public void setSortCell(String string, int res, boolean divider) {
            textView.setText(string);
            iconImageView.setImageResource(res);
            needDivider = divider;
            setWillNotDraw(!needDivider);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (needDivider) {
                canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(62), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(62) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }

    private void updateRow() {
        rowCount = 0;
        autoSortingRow = rowCount++;
        firstDividerRow = PlusConfig.autoSortingChat ? rowCount++ : -1;

        int count = PlusConfig.getSortDataArrayList().size();
        if (count != 0) {
            chatSortStart = rowCount;
            rowCount += PlusConfig.autoSortingChat ? count : 0;
            chatSortEnd = rowCount;
        } else {
            chatSortStart = -1;
            chatSortEnd = -1;
        }

        secondDividerRow = rowCount++;

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onFragmentCreate() {
        PlusConfig.loadChatSortList();
        updateRow();
        return super.onFragmentCreate();
    }


    @Override
    public void onFragmentDestroy() {
        if(orderChange || visibilityChange){
            PlusConfig.saveChatSortList();
            getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
        }
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("ChatSorting", R.string.ChatSorting));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        fragmentView = frameLayout;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        listView.setLayoutManager(layoutManager);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listAdapter = new ListAdapter(context);
        listView.setAdapter(listAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        listView.setOnItemClickListener((view, position) -> {

            if (position == autoSortingRow) {
                PlusConfig.toggleAutoSortingChat();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(PlusConfig.autoSortingChat);
                }
                updateRow();
                listAdapter.notifyDataSetChanged();
                getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
            } else {
                if (position >= chatSortStart && position < chatSortEnd) {
                    PlusConfig.ChatSortData chatSortData = PlusConfig.getSortDataArrayList().get(position - chatSortStart);
                    chatSortData.enabled = !chatSortData.enabled;
                    if(view instanceof SortCell){
                        SortCell sortCell = (SortCell)view;
                        sortCell.setAlpha(chatSortData.enabled?1f:0.3f);
                    }
                    visibilityChange = true;
                }
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 1 && PlusConfig.autoSortingChat;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 1:
                    view = new SortCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                case 2:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    TextCheckCell view = (TextCheckCell) holder.itemView;
                    if (position == autoSortingRow) {
                        view.setDrawCheckRipple(true);
                        view.setTextAndCheck("Auto-sorting chats", PlusConfig.autoSortingChat, false);
                        view.setTag(PlusConfig.autoSortingChat ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked);
                        view.setBackgroundColor(Theme.getColor(PlusConfig.autoSortingChat ? Theme.key_windowBackgroundChecked : Theme.key_windowBackgroundUnchecked));
                    }
                    break;
                } case 1:{
                    SortCell sortCell = (SortCell) holder.itemView ;
                    PlusConfig.ChatSortData filter =  PlusConfig.getSortDataArrayList().get(position - chatSortStart);
                    sortCell.setSortCell(filter.text, filter.res_filled,true);
                    sortCell.setAlpha(filter.enabled ? 1f : 0.3f);
                    break;
                }
                case 2: {
                    if (position == secondDividerRow || firstDividerRow == -1) {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    } else {
                        holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    }
                    break;
                }

            }
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == autoSortingRow) {
                return 0;
            } else if (position >= chatSortStart && position < chatSortEnd) {
                return 1;
            }
//
             else if (position == firstDividerRow || position == secondDividerRow) {
                return 2;
            }
            return 5;
        }

        public void swapElements(int fromIndex, int toIndex) {
            int idx1 = fromIndex - chatSortStart;
            int idx2 = toIndex - chatSortStart;
            int count = chatSortEnd - chatSortStart;
            if (idx1 < 0 || idx2 < 0 || idx1 >= count || idx2 >= count) {
                return;
            }
            PlusConfig.ChatSortData filter1 = PlusConfig.getSortDataArrayList().get(idx1);
            PlusConfig.ChatSortData filter2 = PlusConfig.getSortDataArrayList().get(idx2);
            int temp = filter1.order;
            filter1.order = filter2.order;
            filter2.order = temp;
            PlusConfig.getSortDataArrayList().set(idx1, filter2);
            PlusConfig.getSortDataArrayList().set(idx2, filter1);
            orderChange = true;
            notifyItemMoved(fromIndex, toIndex);
        }

    }

    private class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getAdapterPosition() == firstDividerRow || viewHolder.getAdapterPosition() == secondDividerRow || viewHolder.getAdapterPosition() == autoSortingRow) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
            if (source.getAdapterPosition() == 0 || target.getAdapterPosition() == 0) {
                return false;
            }
            listAdapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
                viewHolder.itemView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
            viewHolder.itemView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        }
    }
}
