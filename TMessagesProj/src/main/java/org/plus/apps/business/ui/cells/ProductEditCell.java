package org.plus.apps.business.ui.cells;/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LetterDrawable;
import org.telegram.ui.Components.LinkPath;

import java.util.ArrayList;

public class ProductEditCell extends FrameLayout {

    public interface SharedLinkCellDelegate {
        void needOpenWebView(TLRPC.WebPage webPage);
        boolean canPerformActions();
        void onLinkPress(final String urlFinal, boolean longPress);
    }

    private ShopDataSerializer.Product product;

    private boolean checkingForLongPress = false;
    private CheckForLongPress pendingCheckForLongPress = null;
    private int pressCount = 0;
    private CheckForTap pendingCheckForTap = null;

    private final class CheckForTap implements Runnable {
        public void run() {
            if (pendingCheckForLongPress == null) {
                pendingCheckForLongPress = new CheckForLongPress();
            }
            pendingCheckForLongPress.currentPressCount = ++pressCount;
            postDelayed(pendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() - ViewConfiguration.getTapTimeout());
        }
    }

    class CheckForLongPress implements Runnable {
        public int currentPressCount;

        public void run() {
            if (checkingForLongPress && getParent() != null && currentPressCount == pressCount) {
                checkingForLongPress = false;
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                if (pressedLink >= 0) {
                    delegate.onLinkPress(links.get(pressedLink), true);
                }
                MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                onTouchEvent(event);
                event.recycle();
            }
        }
    }

    protected void startCheckLongPress() {
        if (checkingForLongPress) {
            return;
        }
        checkingForLongPress = true;
        if (pendingCheckForTap == null) {
            pendingCheckForTap = new CheckForTap();
        }
        postDelayed(pendingCheckForTap, ViewConfiguration.getTapTimeout());
    }

    protected void cancelCheckLongPress() {
        checkingForLongPress = false;
        if (pendingCheckForLongPress != null) {
            removeCallbacks(pendingCheckForLongPress);
        }
        if (pendingCheckForTap != null) {
            removeCallbacks(pendingCheckForTap);
        }
    }


    private boolean linkPreviewPressed;
    private LinkPath urlPath;
    private int pressedLink;

    private ImageReceiver linkImageView;
    private boolean drawLinkImageView;
    private LetterDrawable letterDrawable;
    private CheckBox2 checkBox;

    private SharedLinkCellDelegate delegate;

    private boolean needDivider;

    ArrayList<String> links = new ArrayList<>();
    private int linkY;
    private ArrayList<StaticLayout> linkLayout = new ArrayList<>();

    private int titleY = AndroidUtilities.dp(10);
    private StaticLayout titleLayout;

    private int descriptionY = AndroidUtilities.dp(30);
    private StaticLayout descriptionLayout;

    private int description2Y = AndroidUtilities.dp(30);
    private StaticLayout descriptionLayout2;

    private TextPaint titleTextPaint;
    private TextPaint descriptionTextPaint;


    public ProductEditCell(Context context) {
        super(context);
        setFocusable(true);

        urlPath = new LinkPath();
        urlPath.setUseRoundRect(true);

        titleTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        titleTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        titleTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        descriptionTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        titleTextPaint.setTextSize(AndroidUtilities.dp(14));
        descriptionTextPaint.setTextSize(AndroidUtilities.dp(14));

        setWillNotDraw(false);
        linkImageView = new ImageReceiver(this);
        linkImageView.setRoundRadius(AndroidUtilities.dp(4));
        letterDrawable = new LetterDrawable();

        checkBox = new CheckBox2(context, 21);
        checkBox.setVisibility(INVISIBLE);
        checkBox.setColor(null, Theme.key_windowBackgroundWhite, Theme.key_checkboxCheck);
        checkBox.setDrawUnchecked(false);
        checkBox.setDrawBackgroundAsArc(2);
        addView(checkBox, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 0 : 44, 44, LocaleController.isRTL ? 44 : 0, 0));


    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        drawLinkImageView = false;
        descriptionLayout = null;
        titleLayout = null;
        descriptionLayout2 = null;


        int maxWidth = MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(AndroidUtilities.leftBaseline) - AndroidUtilities.dp(8);

        String title = null;
        String description = null;
        String description2 = null;
        boolean hasPhoto = false;

        if(product != null){
             title = product.title;
             description = ShopUtils.formatCurrency(product.price);
             description2 = product.in_stock?"In stock":"Out of stock";
             hasPhoto = true;
        }

        if (title != null) {
            try {
                titleLayout = ChatMessageCell.generateStaticLayout(title, titleTextPaint, maxWidth, maxWidth, 0, 3);
                if (titleLayout.getLineCount() > 0) {
                    descriptionY = titleY + titleLayout.getLineBottom(titleLayout.getLineCount() - 1) + AndroidUtilities.dp(4);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            letterDrawable.setTitle(title);
        }


        description2Y = descriptionY;
        int desctiptionLines = Math.max(1, 4 - (titleLayout != null ? titleLayout.getLineCount() : 0));

        if (description != null) {
            try {
                descriptionLayout = ChatMessageCell.generateStaticLayout(description, descriptionTextPaint, maxWidth, maxWidth, 0, desctiptionLines);
                if (descriptionLayout.getLineCount() > 0) {
                    description2Y = descriptionY + descriptionLayout.getLineBottom(descriptionLayout.getLineCount() - 1) + AndroidUtilities.dp(5);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        if (description2 != null) {
            try {
                descriptionLayout2 = ChatMessageCell.generateStaticLayout(description2, descriptionTextPaint, maxWidth, maxWidth, 0, desctiptionLines);
                if (descriptionLayout != null) {
                    description2Y += AndroidUtilities.dp(10);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        if (!links.isEmpty()) {
            for (int a = 0; a < links.size(); a++) {
                try {
                    String link = links.get(a);
                    int width = (int) Math.ceil(descriptionTextPaint.measureText(link));
                    CharSequence linkFinal = TextUtils.ellipsize(link.replace('\n', ' '), descriptionTextPaint, Math.min(width, maxWidth), TextUtils.TruncateAt.MIDDLE);
                    StaticLayout layout = new StaticLayout(linkFinal, descriptionTextPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    linkY = description2Y;
                    if (descriptionLayout2 != null && descriptionLayout2.getLineCount() != 0) {
                        linkY += descriptionLayout2.getLineBottom(descriptionLayout2.getLineCount() - 1) + AndroidUtilities.dp(5);
                    }
                    linkLayout.add(layout);
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }


        int maxPhotoWidth = AndroidUtilities.dp(52);
        int x = LocaleController.isRTL ? MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(10) - maxPhotoWidth : AndroidUtilities.dp(10);
        letterDrawable.setBounds(x, AndroidUtilities.dp(11), x + maxPhotoWidth, AndroidUtilities.dp(63));

        if (hasPhoto) {
            linkImageView.setImageCoords(x, AndroidUtilities.dp(11), maxPhotoWidth, maxPhotoWidth);
            linkImageView.setImage(product.images.get(0).photo,null,letterDrawable,null,100);
            drawLinkImageView = true;
        }

        int height = 0;
        if (titleLayout != null && titleLayout.getLineCount() != 0) {
            height += titleLayout.getLineBottom(titleLayout.getLineCount() - 1) + AndroidUtilities.dp(4);
        }
        if (descriptionLayout != null && descriptionLayout.getLineCount() != 0) {
            height += descriptionLayout.getLineBottom(descriptionLayout.getLineCount() - 1) + AndroidUtilities.dp(5);
        }
        if (descriptionLayout2 != null && descriptionLayout2.getLineCount() != 0) {
            height += descriptionLayout2.getLineBottom(descriptionLayout2.getLineCount() - 1) + AndroidUtilities.dp(5);
            if (descriptionLayout != null) {
                height += AndroidUtilities.dp(10);
            }
        }
        for (int a = 0; a < linkLayout.size(); a++) {
            StaticLayout layout = linkLayout.get(a);
            if (layout.getLineCount() > 0) {
                height += layout.getLineBottom(layout.getLineCount() - 1);
            }
        }
        checkBox.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24), MeasureSpec.EXACTLY));
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), Math.max(AndroidUtilities.dp(76), height + AndroidUtilities.dp(17)) + (needDivider ? 1 : 0));
    }

    public void setLink(ShopDataSerializer.Product messageObject, boolean divider) {
        needDivider = divider;
        product = messageObject;
        if(messageObject.in_stock){
            setAlpha(1.0f);
        }else{
            setAlpha(0.5f);
        }
        requestLayout();
    }




    public void setDelegate(SharedLinkCellDelegate sharedLinkCellDelegate) {
        delegate = sharedLinkCellDelegate;
    }

    public ShopDataSerializer.Product getProduct() {
        return product;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (drawLinkImageView) {
            linkImageView.onDetachedFromWindow();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (drawLinkImageView) {
            linkImageView.onAttachedToWindow();
        }
    }

    public String getLink(int num) {
        if (num < 0 || num >= links.size()) {
            return null;
        }
        return links.get(num);
    }

    protected void resetPressedLink() {
        pressedLink = -1;
        linkPreviewPressed = false;
        cancelCheckLongPress();
        invalidate();
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checkBox.getVisibility() != VISIBLE) {
            checkBox.setVisibility(VISIBLE);
        }
        checkBox.setChecked(checked, animated);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (titleLayout != null) {
            canvas.save();
            canvas.translate(AndroidUtilities.dp(LocaleController.isRTL ? 8 : AndroidUtilities.leftBaseline), titleY);
            titleLayout.draw(canvas);
            canvas.restore();
        }

        if (descriptionLayout != null) {
            descriptionTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            canvas.save();
            canvas.translate(AndroidUtilities.dp(LocaleController.isRTL ? 8 : AndroidUtilities.leftBaseline), descriptionY);
            descriptionLayout.draw(canvas);
            canvas.restore();
        }

        if (descriptionLayout2 != null) {
            descriptionTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            canvas.save();
            canvas.translate(AndroidUtilities.dp(LocaleController.isRTL ? 8 : AndroidUtilities.leftBaseline), description2Y);
            descriptionLayout2.draw(canvas);
            canvas.restore();
        }

        if (!linkLayout.isEmpty()) {
            descriptionTextPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
            int offset = 0;
            for (int a = 0; a < linkLayout.size(); a++) {
                StaticLayout layout = linkLayout.get(a);
                if (layout.getLineCount() > 0) {
                    canvas.save();
                    canvas.translate(AndroidUtilities.dp(LocaleController.isRTL ? 8 : AndroidUtilities.leftBaseline), linkY + offset);
                    if (pressedLink == a) {
                        canvas.drawPath(urlPath, Theme.linkSelectionPaint);
                    }
                    layout.draw(canvas);
                    canvas.restore();
                    offset += layout.getLineBottom(layout.getLineCount() - 1);
                }
            }
        }

        letterDrawable.draw(canvas);
        if (drawLinkImageView) {
            linkImageView.draw(canvas);
        }

        if (needDivider) {
            if (LocaleController.isRTL) {
                canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, Theme.dividerPaint);
            } else {
                canvas.drawLine(AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }
    }

}
