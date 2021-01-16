//package org.plus.apps.wallet;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffColorFilter;
//import android.graphics.Rect;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.text.Editable;
//import android.text.InputType;
//import android.text.Layout;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.text.style.RelativeSizeSpan;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.ActionMode;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.Menu;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.EditorInfo;
//
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.ChatObject;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MediaDataController;
//import org.telegram.messenger.R;
//import org.telegram.messenger.Utilities;
//import org.telegram.tgnet.SerializedData;
//import org.telegram.tgnet.TLRPC;
//import org.telegram.ui.ActionBar.ActionBar;
//import org.telegram.ui.ActionBar.AlertDialog;
//import org.telegram.ui.ActionBar.BaseFragment;
//import org.telegram.ui.ActionBar.SimpleTextView;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.ActionBar.ThemeDescription;
//import org.telegram.ui.Cells.HeaderCell;
//import org.telegram.ui.Cells.PollEditTextCell;
//import org.telegram.ui.Cells.ShadowSectionCell;
//import org.telegram.ui.Cells.TextCell;
//import org.telegram.ui.Cells.TextCheckCell;
//import org.telegram.ui.Cells.TextInfoPrivacyCell;
//import org.telegram.ui.ChatActivity;
//import org.telegram.ui.Components.AlertsCreator;
//import org.telegram.ui.Components.CombinedDrawable;
//import org.telegram.ui.Components.EditTextBoldCursor;
//import org.telegram.ui.Components.FillLastLinearLayoutManager;
//import org.telegram.ui.Components.HintView;
//import org.telegram.ui.Components.LayoutHelper;
//import org.telegram.ui.Components.RecyclerListView;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.LinearSmoothScroller;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.SimpleItemAnimator;
//
//public class WalletAlertLayout extends PaymentAlert.AttachAlertLayout {
//
//    private ListAdapter listAdapter;
//    private RecyclerListView listView;
//    private SimpleItemAnimator itemAnimator;
//    private FillLastLinearLayoutManager layoutManager;
//    private HintView hintView;
//
//
//    private TLRPC.User currentUser;
//
//    private boolean allowNesterScroll;
//
//    private boolean ignoreLayout;
//
//    private PollCreateActivityDelegate delegate;
//
//    private int requestFieldFocusAtPosition = -1;
//
//    private int minimumSendAmount;
//
//    private int paddingRow;
//    private int transferHeaderRow;
//    private int amountSecRow;
//    private int amountHeaderRow;
//    private int amountRow;
//    private int commentRow;
//    private int emptyRow;
//
//    private String amount = "";
//    private String comment = "";
//
//    private int rowCount;
//    private int topPadding;
//
//    public static final int MAX_QUESTION_LENGTH = 255;
//    public static final int MAX_ANSWER_LENGTH = 100;
//    public static final int MAX_SOLUTION_LENGTH = 200;
//
//    private static final int done_button = 40;
//
//    public interface PollCreateActivityDelegate {
//        void sendPoll(TLRPC.TL_messageMediaPoll poll, HashMap<String, String> params, boolean notify, int scheduleDate);
//    }
//
//    private static class EmptyView extends View {
//
//        public EmptyView(Context context) {
//            super(context);
//        }
//    }
//
//
//    public WalletAlertLayout(PaymentAlert alert, Context context) {
//        super(alert, context);
//        updateRows();
//
//       BaseFragment baseFragment =  alert.getBaseFragment();
//       if(baseFragment instanceof ChatActivity){
//           ChatActivity chatActivity = (ChatActivity)baseFragment;
//           currentUser = chatActivity.getCurrentUser();
//       }
//
//        listAdapter = new ListAdapter(context);
//
//        listView = new RecyclerListView(context) {
//            @Override
//            protected void requestChildOnScreen(View child, View focused) {
//                if (!(child instanceof PollEditTextCell)) {
//                    return;
//                }
//                super.requestChildOnScreen(child, focused);
//            }
//
//            @Override
//            public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
//                rectangle.bottom += AndroidUtilities.dp(60);
//                return super.requestChildRectangleOnScreen(child, rectangle, immediate);
//            }
//        };
//        listView.setItemAnimator(itemAnimator = new DefaultItemAnimator() {
//            @Override
//            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
//                if (holder.getAdapterPosition() == 0) {
//                    parentAlert.updateLayout(WalletAlertLayout.this, true, 0);
//                }
//            }
//        });
//        listView.setClipToPadding(false);
//        listView.setVerticalScrollBarEnabled(false);
//        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
//        listView.setLayoutManager(layoutManager = new FillLastLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false, AndroidUtilities.dp(53), listView) {
//
//            @Override
//            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
//                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
//                    @Override
//                    public int calculateDyToMakeVisible(View view, int snapPreference) {
//                        int dy = super.calculateDyToMakeVisible(view, snapPreference);
//                        dy -= (topPadding - AndroidUtilities.dp(7));
//                        return dy;
//                    }
//
//                    @Override
//                    protected int calculateTimeForDeceleration(int dx) {
//                        return super.calculateTimeForDeceleration(dx) * 2;
//                    }
//                };
//                linearSmoothScroller.setTargetPosition(position);
//                startSmoothScroll(linearSmoothScroller);
//            }
//
//            @Override
//            protected int[] getChildRectangleOnScreenScrollAmount(View child, Rect rect) {
//                int[] out = new int[2];
//                final int parentTop = 0;
//                final int parentBottom = getHeight() - getPaddingBottom();
//                final int childTop = child.getTop() + rect.top - child.getScrollY();
//                final int childBottom = childTop + rect.height();
//
//                final int offScreenTop = Math.min(0, childTop - parentTop);
//                final int offScreenBottom = Math.max(0, childBottom - parentBottom);
//
//                final int dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
//                out[0] = 0;
//                out[1] = dy;
//                return out;
//            }
//        });
//        layoutManager.setSkipFirstItem();
//        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
//        listView.setPreserveFocusAfterLayout(true);
//        listView.setAdapter(listAdapter);
//        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                parentAlert.updateLayout(WalletAlertLayout.this, true, dy);
//                if (dy != 0 && hintView != null) {
//                    hintView.hide();
//                }
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    int offset = AndroidUtilities.dp(13);
//                    int backgroundPaddingTop = parentAlert.getBackgroundPaddingTop();
//                    int top = parentAlert.scrollOffsetY[0] - backgroundPaddingTop - offset;
//                    if (top + backgroundPaddingTop < ActionBar.getCurrentActionBarHeight()) {
//                        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findViewHolderForAdapterPosition(1);
//                        if (holder != null && holder.itemView.getTop() > AndroidUtilities.dp(53)) {
//                            listView.smoothScrollBy(0, holder.itemView.getTop() - AndroidUtilities.dp(53));
//                        }
//                    }
//                }
//            }
//        });
//
//        hintView = new HintView(context, 4);
//        hintView.setText(LocaleController.getString("PollTapToSelect", R.string.PollTapToSelect));
//        hintView.setAlpha(0.0f);
//        hintView.setVisibility(View.INVISIBLE);
//        addView(hintView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 19, 0, 19, 0));
//
//        checkDoneButton();
//    }
//
//    @Override
//    int needsActionBar() {
//        return 1;
//    }
//
//    @Override
//    void onResume() {
//        if (listAdapter != null) {
//            listAdapter.notifyDataSetChanged();
//        }
//    }
//
//    @Override
//    void onHideShowProgress(float progress) {
//        Log.i("onHideShowProgress","onhide progress" + progress);
//        Log.i("onHideShowProgress","doneitem enaabled" +  parentAlert.doneItem.isEnabled());
//
//
//        parentAlert.doneItem.setAlpha((parentAlert.doneItem.isEnabled() ? 1.0f : 0.5f) * progress);
//    }
//
//    @Override
//    void onMenuItemClick(int id) {
//        if (id == done_button) {
//
//            }
//
//    }
//
//    @Override
//    int getCurrentItemTop() {
//        if (listView.getChildCount() <= 0) {
//            return Integer.MAX_VALUE;
//        }
//        View child = listView.getChildAt(1);
//        if (child == null) {
//            return Integer.MAX_VALUE;
//        }
//        RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
//        int top = (int) child.getY() - AndroidUtilities.dp(8);
//        int newOffset = top > 0 && holder != null && holder.getAdapterPosition() == 1 ? top : 0;
//        if (top >= 0 && holder != null && holder.getAdapterPosition() == 0) {
//            newOffset = top;
//        }
//        return newOffset + AndroidUtilities.dp(25);
//    }
//
//    @Override
//    int getFirstOffset() {
//        return getListTopPadding() + AndroidUtilities.dp(17);
//    }
//
//    @Override
//    public void setTranslationY(float translationY) {
//        super.setTranslationY(translationY);
//        parentAlert.getSheetContainer().invalidate();
//    }
//
//    @Override
//    int getListTopPadding() {
//        return topPadding;
//    }
//
//    @Override
//    void onPreMeasure(int availableWidth, int availableHeight) {
//        int padding;
//        if (parentAlert.sizeNotifierFrameLayout.measureKeyboardHeight() > AndroidUtilities.dp(20)) {
//            padding = AndroidUtilities.dp(52);
//            parentAlert.setAllowNestedScroll(false);
//        } else {
//            if (!AndroidUtilities.isTablet() && AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
//                padding = (int) (availableHeight / 3.5f);
//            } else {
//                padding = (availableHeight / 5 * 2);
//            }
//            padding -= AndroidUtilities.dp(13);
//            if (padding < 0) {
//                padding = 0;
//            }
//            parentAlert.setAllowNestedScroll(allowNesterScroll);
//        }
//        ignoreLayout = true;
//        if (topPadding != padding) {
//            topPadding = padding;
//            listView.setItemAnimator(null);
//            listAdapter.notifyItemChanged(paddingRow);
//        }
//        ignoreLayout = false;
//    }
//
//    @Override
//    int getButtonsHideOffset() {
//        return AndroidUtilities.dp(70);
//    }
//
//    @Override
//    public void requestLayout() {
//        if (ignoreLayout) {
//            return;
//        }
//        super.requestLayout();
//    }
//
//    @Override
//    void scrollToTop() {
//        listView.smoothScrollToPosition(1);
//    }
//
//    public static CharSequence getFixedString(CharSequence text) {
//        if (TextUtils.isEmpty(text)) {
//            return text;
//        }
//        text = AndroidUtilities.getTrimmedString(text);
//        while (TextUtils.indexOf(text, "\n\n\n") >= 0) {
//            text = TextUtils.replace(text, new String[]{"\n\n\n"}, new CharSequence[]{"\n\n"});
//        }
//        while (TextUtils.indexOf(text, "\n\n\n") == 0) {
//            text = TextUtils.replace(text, new String[]{"\n\n\n"}, new CharSequence[]{"\n\n"});
//        }
//        return text;
//    }
//
//
//    private void showQuizHint() {
//        int count = listView.getChildCount();
////        for (int a = answerStartRow; a < answerStartRow + answersCount; a++) {
////            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(a);
////            if (holder != null && holder.itemView instanceof PollEditTextCell) {
////                PollEditTextCell pollEditTextCell = (PollEditTextCell) holder.itemView;
////                if (pollEditTextCell.getTop() > AndroidUtilities.dp(40)) {
////                    hintView.showForView(pollEditTextCell.getCheckBox(), true);
////                    break;
////                }
////            }
////        }
//    }
//
//    private void checkDoneButton() {
//        boolean enabled = true;
//
//        if(TextUtils.isEmpty(amount)){
//            enabled = false;
//        }
//
//        if (!TextUtils.isEmpty(comment) || !TextUtils.isEmpty(comment)) {
//            allowNesterScroll = false;
//        } else {
//            allowNesterScroll = true;
//        }
//
//        parentAlert.setAllowNestedScroll(allowNesterScroll);
//        parentAlert.doneItem.setEnabled(enabled);
//        parentAlert.doneItem.setAlpha(enabled ? 1.0f : 0.5f);
//    }
//
//
//    private void updateRows() {
//        rowCount = 0;
//        paddingRow = rowCount++;
//        transferHeaderRow = rowCount++;
//        amountSecRow = rowCount++;
//        amountHeaderRow = rowCount++;
//        amountRow = rowCount++;
//        commentRow = rowCount++;
//        emptyRow = rowCount++;
//    }
//
//    @Override
//    void onShow() {
//        parentAlert.actionBar.setTitle("Transfer");
//        parentAlert.doneItem.setVisibility(VISIBLE);
//        layoutManager.scrollToPositionWithOffset(0, 0);
//    }
//
//    @Override
//    void onHidden() {
//        parentAlert.doneItem.setVisibility(INVISIBLE);
//    }
//
//    @Override
//    boolean onBackPressed() {
//        return super.onBackPressed();
//    }
//
//
//
//    public void setDelegate(PollCreateActivityDelegate pollCreateActivityDelegate) {
//        delegate = pollCreateActivityDelegate;
//    }
//
//    private void setTextLeft(View cell, int index) {
//        if (!(cell instanceof PollEditTextCell)) {
//            return;
//        }
//        PollEditTextCell textCell = (PollEditTextCell) cell;
//        int max;
//        int left;
//        if (index == amountRow) {
//            max = MAX_QUESTION_LENGTH;
//            left = MAX_QUESTION_LENGTH - (amount != null ? amount.length() : 0);
//        } else if (index == commentRow) {
//            max = MAX_SOLUTION_LENGTH;
//            left = MAX_SOLUTION_LENGTH - (comment != null ? comment.length() : 0);
//        } else {
//            return;
//        }
//        if (left <= max - max * 0.7f) {
//            textCell.setText2(String.format("%d", left));
//            SimpleTextView textView = textCell.getTextView2();
//            String key = left < 0 ? Theme.key_windowBackgroundWhiteRedText5 : Theme.key_windowBackgroundWhiteGrayText3;
//            textView.setTextColor(Theme.getColor(key));
//            textView.setTag(key);
//        } else {
//            textCell.setText2("");
//        }
//    }
//
//
//
//    private class ListAdapter extends RecyclerListView.SelectionAdapter {
//
//        private Context mContext;
//
//        public ListAdapter(Context context) {
//            mContext = context;
//        }
//
//        @Override
//        public int getItemCount() {
//            return rowCount;
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            switch (holder.getItemViewType()) {
//                case 0: {
//                    HeaderCell cell = (HeaderCell) holder.itemView;
//                    if (position == amountHeaderRow) {
//                        cell.setText("Amount");
//                    }else if(position == transferHeaderRow){
//                        cell.setText("Transfer to " + currentUser.first_name);
//
//                      //  cell.setText(LocaleController.getString("WalletSend", R.string.WalletSend));
//                    }
//                    break;
//                }
//                case 2: {
//                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
//                    Drawable drawable = Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);
//                    CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
//                    combinedDrawable.setFullsize(true);
//                    cell.setBackgroundDrawable(combinedDrawable);
//                    break;
//                }
//                case 3: {
//                    TextCell textCell = (TextCell) holder.itemView;
//                    textCell.setColors(null, Theme.key_windowBackgroundWhiteBlueText4);
//                    Drawable drawable1 = mContext.getResources().getDrawable(R.drawable.poll_add_circle);
//                    Drawable drawable2 = mContext.getResources().getDrawable(R.drawable.poll_add_plus);
//                    drawable1.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_switchTrackChecked), PorterDuff.Mode.MULTIPLY));
//                    drawable2.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_checkboxCheck), PorterDuff.Mode.MULTIPLY));
//                    CombinedDrawable combinedDrawable = new CombinedDrawable(drawable1, drawable2);
//                    textCell.setTextAndIcon(LocaleController.getString("AddAnOption", R.string.AddAnOption), combinedDrawable, false);
//                    break;
//                }
//
//            }
//        }
//
//        @Override
//        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
//            int viewType = holder.getItemViewType();
//            if (viewType == 4) {
//               // PollEditTextCell textCell = (PollEditTextCell) holder.itemView;
//               // textCell.setTag(1);
//                //textCell.setTextAndHint(amount != null ? comment : "", LocaleController.getString("WalletAmount", R.string.WalletAmount), false);
//               // textCell.setTag(null);
//               // setTextLeft(holder.itemView, holder.getAdapterPosition());
//            } else if (viewType == 7) {
//                PollEditTextCell textCell = (PollEditTextCell) holder.itemView;
//                textCell.setTag(1);
//                textCell.setTextAndHint(comment != null ? comment : "", LocaleController.getString("WalletComment", R.string.WalletComment), false);
//                textCell.setTag(null);
//                setTextLeft(holder.itemView, holder.getAdapterPosition());
//            }
//        }
//
//        @Override
//        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
//            if (holder.getItemViewType() == 4) {
//                PollEditTextCell editTextCell = (PollEditTextCell) holder.itemView;
//                EditTextBoldCursor editText = editTextCell.getTextView();
//                if (editText.isFocused()) {
//                    editText.clearFocus();
//                    AndroidUtilities.hideKeyboard(editText);
//                }
//            }
//        }
//
//        @Override
//        public boolean isEnabled(RecyclerView.ViewHolder holder) {
//          return false;
//        }
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view;
//            switch (viewType) {
//                case 0:
//                    view = new HeaderCell(mContext, Theme.key_windowBackgroundWhiteBlueHeader, 21, 15, false);
//                    break;
//                case 1:
//                    view = new ShadowSectionCell(mContext);
//                    Drawable drawable = Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow);
//                    CombinedDrawable combinedDrawable = new CombinedDrawable(new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray)), drawable);
//                    combinedDrawable.setFullsize(true);
//                    view.setBackgroundDrawable(combinedDrawable);
//                    break;
//                case 2:
//                    view = new TextInfoPrivacyCell(mContext);
//                    break;
//                case 3:
//                    view = new TextCell(mContext);
//                    break;
//                case 4: {
//                    PollEditTextCell cell = new PollEditTextCell(mContext, null) {
//                        @Override
//                        protected void onFieldTouchUp(EditTextBoldCursor editText) {
//                            parentAlert.makeFocusable(editText, true);
//                        }
//                    };
//                    EditTextBoldCursor editText = cell.getTextView();
//                    cell.setShowNextButton(true);
//                    editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//                    editText.setHintColor(Theme.getColor(Theme.key_dialogTextHint));
//                    editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
//                    editText.setBackground(Theme.createEditTextDrawable(mContext, true));
//                    editText.setImeOptions(editText.getImeOptions() | EditorInfo.IME_ACTION_NEXT);
//                    editText.setCursorSize(AndroidUtilities.dp(30));
//                    editText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("0.0");
//                    stringBuilder.setSpan(new RelativeSizeSpan(0.73f), stringBuilder.length() - 1, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    editText.setHintText(stringBuilder);
//                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
//                    editText.setOnEditorActionListener((v, actionId, event) -> {
//                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
//                            View commentView = layoutManager.getChildAt(commentRow);
//                            if (commentView != null) {
//                                PollEditTextCell editTextCell = (PollEditTextCell) commentView;
//                                editTextCell.getTextView().requestFocus();
//                            }
//                            return true;
//                        }
//                        return false;
//                    });
//                    cell.createErrorTextView();
//                    cell.addTextWatcher(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (cell.getTag() != null) {
//                                return;
//                            }
//                            amount =  s.toString();
//                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(amountRow);
//                            if (holder != null) {
//                                setTextLeft(holder.itemView, amountRow);
//                            }
//                            checkDoneButton();
//                        }
//                    });
//                    view = cell;
//                    break;
//                }
//                case 6:
//                    view = new TextCheckCell(mContext);
//                    break;
//                case 7: {
//                    PollEditTextCell cell = new PollEditTextCell(mContext, true, null) {
//                        @Override
//                        protected void onFieldTouchUp(EditTextBoldCursor editText) {
//                            parentAlert.makeFocusable(editText, true);
//                        }
//
//                        @Override
//                        protected void onActionModeStart(EditTextBoldCursor editText, ActionMode actionMode) {
//                            if (editText.isFocused() && editText.hasSelection()) {
//                                Menu menu = actionMode.getMenu();
//                                if (menu.findItem(android.R.id.copy) == null) {
//                                    return;
//                                }
//                                ((ChatActivity) parentAlert.baseFragment).fillActionModeMenu(menu);
//                            }
//                        }
//                    };
//                    cell.createErrorTextView();
//                    cell.addTextWatcher(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable s) {
//                            if (cell.getTag() != null) {
//                                return;
//                            }
//                            comment = s.toString();
//                            RecyclerView.ViewHolder holder = listView.findViewHolderForAdapterPosition(commentRow);
//                            if (holder != null) {
//                                setTextLeft(holder.itemView, commentRow);
//                            }
//                            checkDoneButton();
//                        }
//                    });
//                    view = cell;
//                    break;
//                }
//                case 8: {
//                    view = new EmptyView(mContext);
//                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
//                    break;
//                }
//                default:
//                case 9: {
//                    view = new View(mContext) {
//                        @Override
//                        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//                            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), topPadding);
//                        }
//                    };
//                    break;
//                }
//
//            }
//            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
//            return new RecyclerListView.Holder(view);
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            if (position == amountHeaderRow || position == transferHeaderRow) {
//                return 0;
//            } else if (position == amountSecRow) {
//                return 1;
//            }  if (position == amountRow) {
//                return 4;
//            } else if (position == commentRow) {
//                return 7;
//            }  else if (position == emptyRow) {
//                return 8;
//            } else if (position == paddingRow) {
//                return 9;
//            } else {
//                return 5;
//            }
//        }
//
//    }
//
//    @Override
//    public ArrayList<ThemeDescription> getThemeDescriptions() {
//        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();
//
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_dialogScrollGlow));
//
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGray));
//
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyView.class}, null, null, null, Theme.key_windowBackgroundGray));
//
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGray));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));
//
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{HeaderCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_windowBackgroundWhiteRedText5));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{HeaderCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText3));
//
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteHintText));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"deleteImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_HINTTEXTCOLOR, new Class[]{PollEditTextCell.class}, new String[]{"moveImageView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_USEBACKGROUNDDRAWABLE | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, new Class[]{PollEditTextCell.class}, new String[]{"deleteImageView"}, null, null, null, Theme.key_stickers_menuSelector));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKTAG, new Class[]{PollEditTextCell.class}, new String[]{"textView2"}, null, null, null, Theme.key_windowBackgroundWhiteRedText5));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{PollEditTextCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_windowBackgroundWhiteGrayIcon));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{PollEditTextCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
//
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));
//
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
//
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));
//
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueText4));
//        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_switchTrackChecked));
//        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCell.class}, new String[]{"imageView"}, null, null, null, Theme.key_checkboxCheck));
//
//        return themeDescriptions;
//    }
//}
