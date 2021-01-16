package org.plus.apps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.cells.BusinessCell;
import org.plus.experment.PlusBuildVars;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.ArrayList;

public class AppsBottomSheet extends BottomSheet{

    private FrameLayout frameLayout;


    private RecyclerListView gridView;
    private GridLayoutManager layoutManager;
    private ListAdapter adapter;

    private EmptyTextProgressView searchEmptyView;
    private Drawable shadowDrawable;
    private View[] shadow = new View[2];
    private AnimatorSet[] shadowAnimation = new AnimatorSet[2];

    private AppDelegate delegate;

    private int scrollOffsetY;
    private int topBeforeSwitch;


    private int attachItemSize = AndroidUtilities.dp(85);


    public AppsBottomSheet(Context context, boolean needFocus) {
        super(context, needFocus);

        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));

        SizeNotifierFrameLayout sizeNotifierFrameLayout = new SizeNotifierFrameLayout(context) {

            private boolean ignoreLayout = false;
            private RectF rect1 = new RectF();
            private boolean fullHeight;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int totalHeight = MeasureSpec.getSize(heightMeasureSpec);
                if (Build.VERSION.SDK_INT >= 21 && !isFullscreen) {
                    ignoreLayout = true;
                    setPadding(backgroundPaddingLeft, AndroidUtilities.statusBarHeight, backgroundPaddingLeft, 0);
                    ignoreLayout = false;
                }
                int availableHeight = totalHeight - getPaddingTop();
                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
                if (!AndroidUtilities.isInMultiwindow && keyboardSize <= AndroidUtilities.dp(20)) {
                    availableHeight -= 0;
                }

                int size = Math.max(0, adapter.getItemCount());
                int contentSize = AndroidUtilities.dp(0) + Math.max(3, (int) Math.ceil(size / 4.0f)) * AndroidUtilities.dp(103) + backgroundPaddingTop;
                int padding = (contentSize < availableHeight ? 0 : availableHeight - (availableHeight / 5 * 3)) + AndroidUtilities.dp(8);
                if (gridView.getPaddingTop() != padding) {
                    ignoreLayout = true;
                    gridView.setPadding(0, padding, 0, AndroidUtilities.dp(48));
                    ignoreLayout = false;
                }
                fullHeight = contentSize >= totalHeight;
                onMeasureInternal(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Math.min(contentSize, totalHeight), MeasureSpec.EXACTLY));
            }

            private void onMeasureInternal(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                widthSize -= backgroundPaddingLeft * 2;

                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
                if (keyboardSize <= AndroidUtilities.dp(20)) {
                    if (!AndroidUtilities.isInMultiwindow) {
                        heightSize -= 0;
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
                    }

                } else {
                    ignoreLayout = true;

                    ignoreLayout = false;
                }

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE) {
                        continue;
                    }
                    if (false) {
                        if (AndroidUtilities.isInMultiwindow || AndroidUtilities.isTablet()) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(AndroidUtilities.isTablet() ? 200 : 320), heightSize - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
                            }
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        }
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int keyboardSize = SharedConfig.smoothKeyboard ? 0 : measureKeyboardHeight();
                int paddingBottom = keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow && !AndroidUtilities.isTablet() ? 0 : 0;
                setBottomClip(paddingBottom);

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = (r - l) - width - lp.rightMargin - getPaddingRight() - backgroundPaddingLeft;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin + getPaddingLeft();
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (false) {
                        if (AndroidUtilities.isTablet()) {
                            childTop = getMeasuredHeight() - child.getMeasuredHeight();
                        } else {
                            childTop = getMeasuredHeight() + keyboardSize - child.getMeasuredHeight();
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
                updateLayout();
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && scrollOffsetY != 0 && ev.getY() < scrollOffsetY - AndroidUtilities.dp(30)) {
                    dismiss();
                    return true;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return !isDismissed() && super.onTouchEvent(e);
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int y = scrollOffsetY - backgroundPaddingTop + AndroidUtilities.dp(6);
                int top = scrollOffsetY - backgroundPaddingTop - AndroidUtilities.dp(13);
                int height = getMeasuredHeight() + AndroidUtilities.dp(30) + backgroundPaddingTop;
                int statusBarHeight = 0;
                float radProgress = 1.0f;
                if (!isFullscreen && Build.VERSION.SDK_INT >= 21) {
                    top += AndroidUtilities.statusBarHeight;
                    y += AndroidUtilities.statusBarHeight;
                    height -= AndroidUtilities.statusBarHeight;

                    if (fullHeight) {
                        if (top + backgroundPaddingTop < AndroidUtilities.statusBarHeight * 2) {
                            int diff = Math.min(AndroidUtilities.statusBarHeight, AndroidUtilities.statusBarHeight * 2 - top - backgroundPaddingTop);
                            top -= diff;
                            height += diff;
                            radProgress = 1.0f - Math.min(1.0f, (diff * 2) / (float) AndroidUtilities.statusBarHeight);
                        }
                        if (top + backgroundPaddingTop < AndroidUtilities.statusBarHeight) {
                            statusBarHeight = Math.min(AndroidUtilities.statusBarHeight, AndroidUtilities.statusBarHeight - top - backgroundPaddingTop);
                        }
                    }
                }

                shadowDrawable.setBounds(0, top, getMeasuredWidth(), height);
                shadowDrawable.draw(canvas);

                if (radProgress != 1.0f) {
                    Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_dialogBackground));
                    rect1.set(backgroundPaddingLeft, backgroundPaddingTop + top, getMeasuredWidth() - backgroundPaddingLeft, backgroundPaddingTop + top + AndroidUtilities.dp(24));
                    canvas.drawRoundRect(rect1, AndroidUtilities.dp(12) * radProgress, AndroidUtilities.dp(12) * radProgress, Theme.dialogs_onlineCirclePaint);
                }

                int w = AndroidUtilities.dp(36);
                rect1.set((getMeasuredWidth() - w) / 2, y, (getMeasuredWidth() + w) / 2, y + AndroidUtilities.dp(4));
                Theme.dialogs_onlineCirclePaint.setColor(Theme.getColor(Theme.key_sheet_scrollUp));
                canvas.drawRoundRect(rect1, AndroidUtilities.dp(2), AndroidUtilities.dp(2), Theme.dialogs_onlineCirclePaint);

                if (statusBarHeight > 0) {
                    int color1 = Theme.getColor(Theme.key_dialogBackground);
                    int finalColor = Color.argb(0xff, (int) (Color.red(color1) * 0.8f), (int) (Color.green(color1) * 0.8f), (int) (Color.blue(color1) * 0.8f));
                    Theme.dialogs_onlineCirclePaint.setColor(finalColor);
                    canvas.drawRect(backgroundPaddingLeft, AndroidUtilities.statusBarHeight - statusBarHeight, getMeasuredWidth() - backgroundPaddingLeft, AndroidUtilities.statusBarHeight, Theme.dialogs_onlineCirclePaint);
                }
            }
        };

        containerView = sizeNotifierFrameLayout;
        containerView.setWillNotDraw(false);
        containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);

        frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

        gridView = new RecyclerListView(context);
        gridView.setTag(13);
        gridView.setPadding(0, AndroidUtilities.dp(16), 0, AndroidUtilities.dp(16));
        gridView.setClipToPadding(false);
        gridView.setLayoutManager(layoutManager = new GridLayoutManager(getContext(), 4));
        gridView.setHorizontalScrollBarEnabled(false);
        gridView.setVerticalScrollBarEnabled(false);
        gridView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                RecyclerListView.Holder holder = (RecyclerListView.Holder) parent.getChildViewHolder(view);
                if (holder != null) {
                    int pos = holder.getAdapterPosition();
                    outRect.left = pos % 4 == 0 ? 0 : AndroidUtilities.dp(4);
                    outRect.right = pos % 4 == 3 ? 0 : AndroidUtilities.dp(4);
                } else {
                    outRect.left = AndroidUtilities.dp(4);
                    outRect.right = AndroidUtilities.dp(4);
                }
            }
        });
        containerView.addView(gridView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 8, 0, 0));
        gridView.setAdapter(adapter = new ListAdapter(context));
        adapter.notifyDataSetChanged();
        gridView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        gridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                updateLayout();
            }
        });
        gridView.setOnItemClickListener((view, position) -> {
            if(delegate != null){
                selectedId = (Integer) view.getTag();
                delegate.didSelected(selectedId);
                dismiss();
            }
        });

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight(), Gravity.TOP | Gravity.LEFT);
        frameLayoutParams.topMargin = AndroidUtilities.dp(16);
        shadow[0] = new View(context);
        shadow[0].setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        shadow[0].setAlpha(0.0f);
        shadow[0].setTag(1);
        containerView.addView(shadow[0], frameLayoutParams);

        containerView.addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 16, Gravity.LEFT | Gravity.TOP));

        frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight(), Gravity.BOTTOM | Gravity.LEFT);
        frameLayoutParams.bottomMargin = AndroidUtilities.dp(48);
        shadow[1] = new View(context);
        shadow[1].setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        containerView.addView(shadow[1], frameLayoutParams);
        shadow[1].setAlpha(0.0f);


    }

    public void setDelegate(AppDelegate delegate) {
        this.delegate = delegate;
    }

    @SuppressLint("NewApi")
    private void updateLayout() {
        if (gridView.getChildCount() <= 0) {
            return;
        }
        View child = gridView.getChildAt(0);
        RecyclerListView.Holder holder = (RecyclerListView.Holder) gridView.findContainingViewHolder(child);
        int top = child.getTop() - AndroidUtilities.dp(8);
        int newOffset = top > 0 && holder != null && holder.getAdapterPosition() == 0 ? top : 0;
        if (top >= 0 && holder != null && holder.getAdapterPosition() == 0) {
            newOffset = top;
            runShadowAnimation(0, false);
        } else {
            runShadowAnimation(0, true);
        }
        if (scrollOffsetY != newOffset) {
            gridView.setTopGlowOffset(scrollOffsetY = newOffset);
            frameLayout.setTranslationY(scrollOffsetY);
            searchEmptyView.setTranslationY(scrollOffsetY);
            containerView.invalidate();
        }
    }


    private void runShadowAnimation(final int num, final boolean show) {
        if (show && shadow[num].getTag() != null || !show && shadow[num].getTag() == null) {
            shadow[num].setTag(show ? null : 1);
            if (show) {
                shadow[num].setVisibility(View.VISIBLE);
            }
            if (shadowAnimation[num] != null) {
                shadowAnimation[num].cancel();
            }
            shadowAnimation[num] = new AnimatorSet();
            shadowAnimation[num].playTogether(ObjectAnimator.ofFloat(shadow[num], View.ALPHA, show ? 1.0f : 0.0f));
            shadowAnimation[num].setDuration(150);
            shadowAnimation[num].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (shadowAnimation[num] != null && shadowAnimation[num].equals(animation)) {
                        if (!show) {
                            shadow[num].setVisibility(View.INVISIBLE);
                        }
                        shadowAnimation[num] = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (shadowAnimation[num] != null && shadowAnimation[num].equals(animation)) {
                        shadowAnimation[num] = null;
                    }
                }
            });
            shadowAnimation[num].start();
        }
    }


    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    public interface AppDelegate {
        void didSelected(int id);
    }


    @Override
    public void dismissInternal() {
        super.dismissInternal();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }


    private class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        private int buttonCount;
        private int scanButton;
        private int shopButton;
        private int walletButton;
        private int rideButton;
        private int settingRow;


        public ListAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public int getItemCount() {
            return buttonCount;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View  view = new AttachButton(mContext);
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void notifyDataSetChanged() {
            buttonCount = 0;
            scanButton  = buttonCount++;
            shopButton = buttonCount++;
            if(PlusBuildVars.DEBUG_PRIVATE){
                walletButton = buttonCount++;
                rideButton = buttonCount++;
            }else{
                walletButton = -1;
                rideButton = -1;
            }
            settingRow = -1;
            super.notifyDataSetChanged();
        }


        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            applyAttachButtonColors(holder.itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AttachButton attachButton = (AttachButton) holder.itemView;
            if (position == walletButton) {
                attachButton.setTextAndIcon(1, LocaleController.getString("Wallet", R.string.Wallet), Theme.wallet_drawale, Theme.key_chat_attachGalleryBackground, Theme.key_chat_attachGalleryText);
                attachButton.setTag(1);
            } else if (position == shopButton) {
                attachButton.setTextAndIcon(2, LocaleController.getString("Shop", R.string.Shop), Theme.getDrawable(R.drawable.ic_store), Theme.key_chat_attachAudioBackground, Theme.key_chat_attachAudioText);
                attachButton.setTag(2);
            }else if(position == scanButton){
                attachButton.setTextAndIcon(3, LocaleController.getString("Scan", R.string.Scan), Theme.getDrawable(R.drawable.wallet_qr), Theme.key_chat_attachLocationBackground, Theme.key_chat_attachLocationText);
                attachButton.setTag(3);
            }else if(position == rideButton){
                attachButton.setTextAndIcon(4, LocaleController.getString("Ride", R.string.Ride), Theme.getDrawable(R.drawable.ic_ride), Theme.key_chat_attachLocationBackground, Theme.key_chat_attachLocationText);
                attachButton.setTag(4);
            }else if(position == settingRow){
                attachButton.setTextAndIcon(5, LocaleController.getString("Settings", R.string.Settings), Theme.getDrawable(R.drawable.menu_settings), Theme.key_chat_attachLocationBackground, Theme.key_chat_attachLocationText);
                attachButton.setTag(5);
            }
        }

        private void applyAttachButtonColors(View view) {
            if (view instanceof AttachButton) {
                AttachButton button = (AttachButton) view;
                button.textView.setTextColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_dialogTextGray2), Theme.getColor(button.textKey), button.checkedState));
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

    }

    private Paint attachButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float bottomPannelTranslation;
    private int selectedId;


    private class AttachButton extends FrameLayout {

        private TextView textView;
        private ImageView imageView;
        private boolean checked;
        private String backgroundKey;
        private String textKey;
        private float checkedState;
        private Animator checkAnimator;
        private int currentId;

        public AttachButton(Context context) {
            super(context);
            setWillNotDraw(false);

            imageView = new RLottieImageView(context) {
                @Override
                public void setScaleX(float scaleX) {
                    super.setScaleX(scaleX);
                    AttachButton.this.invalidate();
                }
            };
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            addView(imageView, LayoutHelper.createFrame(32, 32, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 18, 0, 0));

            textView = new TextView(context);
            textView.setMaxLines(2);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textView.setLineSpacing(-AndroidUtilities.dp(2), 1.0f);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 62, 0, 0));
        }




        @Keep
        public float getCheckedState() {
            return checkedState;
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(attachItemSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(84), MeasureSpec.EXACTLY));
        }


        public void setTextAndIcon(int id, CharSequence text, Drawable drawable, String background, String textColor) {
            currentId = id;
            textView.setText(text);
            imageView.setImageDrawable(drawable);
            backgroundKey = background;
            textKey = textColor;
            textView.setTextColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_dialogTextGray2), Theme.getColor(textKey), checkedState));
        }
        @Override
        protected void onDraw(Canvas canvas) {
            float scale = imageView.getScaleX() + 0.06f * checkedState;
            float radius = AndroidUtilities.dp(23) * scale;

            float cx = imageView.getLeft() + imageView.getMeasuredWidth() / 2;
            float cy = imageView.getTop() + imageView.getMeasuredWidth() / 2;

            attachButtonPaint.setColor(Theme.getColor(backgroundKey));
            attachButtonPaint.setStyle(Paint.Style.STROKE);
            attachButtonPaint.setStrokeWidth(AndroidUtilities.dp(3) * scale);
            attachButtonPaint.setAlpha(Math.round(255f * checkedState));
            canvas.drawCircle(cx, cy, radius - 0.5f * attachButtonPaint.getStrokeWidth(), attachButtonPaint);

            attachButtonPaint.setAlpha(255);
            attachButtonPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, radius - AndroidUtilities.dp(5) * checkedState, attachButtonPaint);
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }
    }



}
