package org.plus.apps.business.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.plus.apps.business.data.SR_object;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.components.OfferBottomSheet;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Components.BetterRatingView;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Locale;

public class StoreReviewFragment extends BaseFragment {


    public static ActionBarLayout[] showAsSheet(BaseFragment parentFragment, ShopDataSerializer.Shop currentShop,int rate,int chatId,String comment,ShopReviewDelegate delegate) {
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return null;
        }
        ActionBarLayout[] actionBarLayout = new ActionBarLayout[]{new ActionBarLayout(parentFragment.getParentActivity())};
        BottomSheet bottomSheet = new BottomSheet(parentFragment.getParentActivity(), false) {
            {
                actionBarLayout[0].init(new ArrayList<>());
                StoreReviewFragment fragment = new StoreReviewFragment(currentShop,rate,chatId,comment,delegate) {
                    @Override
                    public void finishFragment() {
                        dismiss();
                    }

                    @Override
                    public void removeSelfFromStack() {
                        dismiss();
                    }

                    @Override
                    public void onResume() {
                        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
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



    public interface  ShopReviewDelegate{
        void reviewPosted(String comment,int rate);
    }

    private ShopReviewDelegate delegate;

    public void setDelegate(ShopReviewDelegate delegate) {
        this.delegate = delegate;
    }

    private BetterRatingView ratingView;
    private ChatAvatarContainer avatarContainer;
    private PollEditTextCell cell;

    private ShopDataSerializer.Shop shop;


    private int currentRate;
    private String comment = "";
    private int chat_id;


    public StoreReviewFragment(ShopDataSerializer.Shop currentShop, int rate, int chatId, String com, ShopReviewDelegate delegate){
        shop = currentShop;
        currentRate = rate;
        comment = com;
        chat_id = chatId;
        this.delegate = delegate;
    }

    private ActionBarMenuItem menuItem;
    private static final int MAX_COMMENT_LENGTH = 500;

    @Override
    public View createView(Context context) {


        avatarContainer = new ChatAvatarContainer(context, null, false);
        actionBar.addView(avatarContainer, 0, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, !inPreviewMode ? 56 : 0, 0, 40, 0));

       // avatarContainer.setAvatarImage(shop);
        avatarContainer.setTitle(shop.title);
        avatarContainer.setSubtitle("Rate this shop");

        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(final int id) {
                if (id == -1) {
                    finishFragment();
                }else if(id == 1){
                    SR_object.post_review_request post_review_request = new SR_object.post_review_request();
                    post_review_request.comment = comment;
                    post_review_request.rating = currentRate;
                    AlertDialog alertDialog = new AlertDialog(context,3);
                    alertDialog.setCanCacnel(true);
                    int reqId =  ShopDataController.getInstance(currentAccount).postReviewToShop(chat_id, post_review_request, (response, error) -> {
                        alertDialog.cancel();
                        if(error != null){
                           showAlert("error! try again");
                        }else{
                            if(delegate != null){
                                delegate.reviewPosted(comment,currentRate);
                            }
                            finishFragment();
                        }
                    });
                    alertDialog.setOnCancelListener(dialog -> ShopDataController.getInstance(currentAccount).cancelRequest(reqId));
                    alertDialog.show();

                }
            }
        });

        avatarContainer.setTitleColors(Theme.getColor(Theme.key_player_actionBarTitle), Theme.getColor(Theme.key_player_actionBarSubtitle));
        actionBar.setItemsColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), false);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));


        ActionBarMenu menu  = actionBar.createMenu();
        menuItem = menu.addItem(1, "POST");

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ratingView = new BetterRatingView(context);
       // ratingView.setRating(currentRate);
        ratingView.setOnRatingChangeListener(newRating -> currentRate = newRating);
        linearLayout.addView(ratingView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL,16,32,16,16));

        cell = new PollEditTextCell(context, null) {
            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                int left = MAX_COMMENT_LENGTH - comment.getBytes().length;
                if (left <= MAX_COMMENT_LENGTH - MAX_COMMENT_LENGTH * 0.7f) {
                    setText2(String.format(Locale.getDefault(), "%d", left));
                    SimpleTextView textView = getTextView2();
                    String key = left < 0 ? Theme.key_windowBackgroundWhiteRedText5 : Theme.key_windowBackgroundWhiteGrayText3;
                    textView.setTextColor(Theme.getColor(key));
                    textView.setTag(key);
                } else {
                    setText2("");
                }
            }
        };
        EditTextBoldCursor editText = cell.getTextView();
        editText.setBackground(Theme.createEditTextDrawable(context, true));
        editText.setPadding(AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14), AndroidUtilities.dp(14));
        cell.createErrorTextView();
        cell.setTextAndHint(comment, "Tell use what you think!", false);
        editText.setFilters(new InputFilter[]{new OfferBottomSheet.ByteLengthFilter(MAX_COMMENT_LENGTH)});
        cell.addTextWatcher(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (cell.getTag() != null) {
                    return;
                }
                comment = s.toString();
                int left = MAX_COMMENT_LENGTH - comment.getBytes().length;
                if (left <= MAX_COMMENT_LENGTH - MAX_COMMENT_LENGTH * 0.7f) {
                    cell.setText2(String.format(Locale.getDefault(), "%d", left));
                    SimpleTextView textView = cell.getTextView2();
                    String key = left < 0 ? Theme.key_windowBackgroundWhiteRedText5 : Theme.key_windowBackgroundWhiteGrayText3;
                    textView.setTextColor(Theme.getColor(key));
                    textView.setTag(key);
                } else {
                    cell.setText2("");
                }
            }
        });
        linearLayout.addView(cell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL,16,16,16,16));

        frameLayout.addView(linearLayout,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        cell.getTextView().requestFocus();
        AndroidUtilities.showKeyboard(cell.getTextView());

    }


    private void showAlert(String text) {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(LocaleController.getString("AppNameHulu", R.string.AppNameHulu));
        builder.setMessage(text);
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
        showDialog(builder.create());
    }
}
