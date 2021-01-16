package org.plus.apps.wallet.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;


public class PaymentBottomSheet extends BottomSheet {

    private ButtonsAdapter buttonsAdapter;
    private BaseFragment baseFragment;

    private int scrollOffsetY;
    private int previousScrollOffsetY;
    private int topBeforeSwitch;
    private boolean panTranslationMoveLayout;

    private float currentPanTranslationY;

    private float captionEditTextTopOffset;
    private float chatActivityEnterViewAnimateFromTop;
    private ValueAnimator topBackgroundAnimator;

    private int topOffset;

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;


    public interface PaymentDelegate{
        void onSelected(int id);
    }

    private PaymentDelegate paymentDelegate;

    public void setPaymentDelegate(PaymentDelegate paymentDelegate) {
        this.paymentDelegate = paymentDelegate;
    }
    private int transferButton;
    private int airTimeButton;
    private int buttonsCount;

    private void updateRow(){
    buttonsCount = 0;
    transferButton = buttonsCount++;
    airTimeButton = buttonsCount++;
}

    private int attachItemSize = AndroidUtilities.dp(85);

    private int selectedId;
    private Paint attachButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private FrameLayout frameLayout;
    private FrameLayout frameLayout2;

    public PaymentBottomSheet(Context context, final BaseFragment parentFragment) {
        super(context, true);

        updateRow();
        baseFragment = parentFragment;
        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogBackground), PorterDuff.Mode.MULTIPLY));

        isFullscreen = false;

        FrameLayout sizeNotifierFrameLayout = new FrameLayout(context);
        containerView = sizeNotifierFrameLayout;
        containerView.setWillNotDraw(false);
        containerView.setClipChildren(false);
        containerView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);

        frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));

        listView = new RecyclerListView(context);
        listView.setAdapter(buttonsAdapter = new ButtonsAdapter(context));
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setHorizontalScrollBarEnabled(false);
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        listView.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 84, Gravity.BOTTOM | Gravity.LEFT));

        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(paymentDelegate != null){
                    paymentDelegate.onSelected((int)view.getTag());
                }
            }
        });

        containerView.addView(frameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP));

    }


    private class ButtonsAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;


        public ButtonsAdapter(Context context) {
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    view = new AttachButton(mContext);
                    break;
                case 1:
                default:
                    view = new AttachBotButton(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0:
                    AttachButton attachButton = (AttachButton) holder.itemView;
                    if (position == transferButton) {
                        attachButton.setTextAndIcon(1, LocaleController.getString("ChatGallery", R.string.ChatGallery), Theme.chat_attachButtonDrawables[0], Theme.key_chat_attachGalleryBackground, Theme.key_chat_attachGalleryText);
                        attachButton.setTag(1);
                    } else if (position == airTimeButton) {
                        attachButton.setTextAndIcon(2, LocaleController.getString("ChatDocument", R.string.ChatDocument), Theme.chat_attachButtonDrawables[2], Theme.key_chat_attachFileBackground, Theme.key_chat_attachFileText);
                        attachButton.setTag(2);
                    }
                    break;
                case 1:
                    position -= buttonsCount;
                    AttachBotButton child = (AttachBotButton) holder.itemView;
                    child.setTag(position);
                    child.setUser(MessagesController.getInstance(currentAccount).getUser(MediaDataController.getInstance(currentAccount).inlineBots.get(position).peer.user_id));
                    break;
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            applyAttachButtonColors(holder.itemView);
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @Override
        public int getItemCount() {
            return buttonsCount;
        }



        public int getButtonsCount() {
            return buttonsCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < buttonsCount) {
                return 0;
            }
            return 1;
        }
    }

    private void applyAttachButtonColors(View view) {
        if (view instanceof AttachButton) {
            AttachButton button = (AttachButton) view;
            button.textView.setTextColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_dialogTextGray2), Theme.getColor(button.textKey), button.checkedState));
        } else if (view instanceof AttachBotButton) {
            AttachBotButton button = (AttachBotButton) view;
            button.nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        }
    }


    private class AttachButton extends FrameLayout {

        private TextView textView;
        private RLottieImageView imageView;
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

        void updateCheckedState(boolean animate) {
            if (checked == (currentId == selectedId)) {
                return;
            }
            checked = currentId == selectedId;
            if (checkAnimator != null) {
                checkAnimator.cancel();
            }
            if (animate) {
                if (checked) {
                    imageView.setProgress(0.0f);
                    imageView.playAnimation();
                }
                checkAnimator = ObjectAnimator.ofFloat(this, "checkedState", checked ? 1f : 0f);
                checkAnimator.setDuration(200);
                checkAnimator.start();
            } else {
                imageView.stopAnimation();
                imageView.setProgress(0.0f);
                setCheckedState(checked ? 1f : 0f);
            }
        }

        @Keep
        public void setCheckedState(float state) {
            checkedState = state;
            imageView.setScaleX(1.0f - 0.06f * state);
            imageView.setScaleY(1.0f - 0.06f * state);
            textView.setTextColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_dialogTextGray2), Theme.getColor(textKey), checkedState));
            invalidate();
        }

        @Keep
        public float getCheckedState() {
            return checkedState;
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            updateCheckedState(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(attachItemSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(84), MeasureSpec.EXACTLY));
        }

        public void setTextAndIcon(int id, CharSequence text, RLottieDrawable drawable, String background, String textColor) {
            currentId = id;
            textView.setText(text);
            imageView.setAnimation(drawable);
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

    private class AttachBotButton extends FrameLayout {

        private BackupImageView imageView;
        private TextView nameTextView;
        private AvatarDrawable avatarDrawable = new AvatarDrawable();

        private TLRPC.User currentUser;

        public AttachBotButton(Context context) {
            super(context);

            imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(25));
            addView(imageView, LayoutHelper.createFrame(46, 46, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 9, 0, 0));

            if (Build.VERSION.SDK_INT >= 21) {
                View selector = new View(context);
                selector.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), 1, AndroidUtilities.dp(23)));
                addView(selector, LayoutHelper.createFrame(46, 46, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 9, 0, 0));
            }

            nameTextView = new TextView(context);
            nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            nameTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            nameTextView.setLines(1);
            nameTextView.setSingleLine(true);
            nameTextView.setEllipsize(TextUtils.TruncateAt.END);
            addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 6, 60, 6, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(attachItemSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(100), MeasureSpec.EXACTLY));
        }

        public void setUser(TLRPC.User user) {
            if (user == null) {
                return;
            }
            nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            currentUser = user;
            nameTextView.setText(ContactsController.formatName(user.first_name, user.last_name));
            avatarDrawable.setInfo(user);
            imageView.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);
            requestLayout();
        }
    }

}
