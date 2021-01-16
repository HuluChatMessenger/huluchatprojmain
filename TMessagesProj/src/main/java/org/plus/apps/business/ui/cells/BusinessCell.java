package org.plus.apps.business.ui.cells;


import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.wallet.WalletDataSerializer;
import org.plus.wallet.WalletModel;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;

public class BusinessCell extends FrameLayout {

    private BackupImageView imageView;
    private TextView nameTextView;
    private CheckBox2 checkBox;
    private AvatarDrawable avatarDrawable = new AvatarDrawable();
    private TLRPC.User user;

    private float onlineProgress;
    private long lastUpdateTime;

    private int currentAccount = UserConfig.selectedAccount;

    public BusinessCell(Context context) {
        super(context);

        setWillNotDraw(false);

        imageView = new BackupImageView(context);
        imageView.setRoundRadius(AndroidUtilities.dp(28));
        addView(imageView, LayoutHelper.createFrame(56, 56, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 7, 0, 0));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        nameTextView.setMaxLines(2);
        nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        nameTextView.setLines(2);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, 66, 6, 0));

        checkBox = new CheckBox2(context, 21);
        checkBox.setColor(Theme.key_dialogRoundCheckBox, Theme.key_dialogBackground, Theme.key_dialogRoundCheckBoxCheck);
        checkBox.setDrawUnchecked(false);
        checkBox.setDrawBackgroundAsArc(4);
        checkBox.setProgressDelegate(progress -> {
            float scale = 1.0f - (1.0f - 0.857f) * checkBox.getProgress();
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
        });
        addView(checkBox, LayoutHelper.createFrame(24, 24, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 19, 42, 0, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(103), MeasureSpec.EXACTLY));
    }




    public void setProvider(WalletModel.Provider provider)
    {
        if(provider == null){
            return;
        }
        nameTextView.setText(provider.name);
        avatarDrawable.setInfo(5,provider.name,null);
       // imageView.setImage(provider.image, null, avatarDrawable);
    }



    public void setBusiness(ShopDataSerializer.ProductType productType)
    {
        if(productType == null){
            return;
        }
        nameTextView.setText(productType.display_name);
        avatarDrawable.setInfo(5,productType.display_name,null);
        imageView.setImage(productType.icon, null, avatarDrawable);
    }

    public void setChecked(boolean checked, boolean animated) {
        checkBox.setChecked(checked, animated);
    }


    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);
        if (child == imageView) {
            if (user != null && !MessagesController.isSupportUser(user)) {
                long newTime = SystemClock.elapsedRealtime();
                long dt = newTime - lastUpdateTime;
                if (dt > 17) {
                    dt = 17;
                }
                lastUpdateTime = newTime;

                boolean isOnline = !user.self && !user.bot && (user.status != null && user.status.expires > ConnectionsManager.getInstance(currentAccount).getCurrentTime() || MessagesController.getInstance(currentAccount).onlinePrivacy.containsKey(user.id));
                if (isOnline || onlineProgress != 0) {
                    int top = imageView.getBottom() - AndroidUtilities.dp(6);
                    int left = imageView.getRight() - AndroidUtilities.dp(10);
                    Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    canvas.drawCircle(left, top, AndroidUtilities.dp(7) * onlineProgress, Theme.dialogs_onlineCirclePaint);
                    Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_chats_onlineCircle));
                    canvas.drawCircle(left, top, AndroidUtilities.dp(5) * onlineProgress, Theme.dialogs_onlineCirclePaint);
                    if (isOnline) {
                        if (onlineProgress < 1.0f) {
                            onlineProgress += dt / 150.0f;
                            if (onlineProgress > 1.0f) {
                                onlineProgress = 1.0f;
                            }
                            imageView.invalidate();
                            invalidate();
                        }
                    } else {
                        if (onlineProgress > 0.0f) {
                            onlineProgress -= dt / 150.0f;
                            if (onlineProgress < 0.0f) {
                                onlineProgress = 0.0f;
                            }
                            imageView.invalidate();
                            invalidate();
                        }
                    }
                }
            }
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int cx = imageView.getLeft() + imageView.getMeasuredWidth() / 2;
        int cy = imageView.getTop() + imageView.getMeasuredHeight() / 2;
        Theme.checkboxSquare_checkPaint.setColor(Theme.getColor(Theme.key_dialogRoundCheckBox));
        Theme.checkboxSquare_checkPaint.setAlpha((int) (checkBox.getProgress() * 255));
        canvas.drawCircle(cx, cy, AndroidUtilities.dp(28), Theme.checkboxSquare_checkPaint);
    }

}
