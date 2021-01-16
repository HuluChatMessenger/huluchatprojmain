package org.plus.apps.business.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ShopUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import static android.view.View.MeasureSpec.EXACTLY;

public class  ShopActionListView extends FrameLayout {

    private final int[] res = {R.drawable.wallet_qr,R.drawable.menu_chats,R.drawable.menu_calls,R.drawable.menu_location};
    private final String[] titles = {"Scan","Chat","Call","Location"};
    private final int[] actions = {ACTION_QR,ACTION_CHAT,ACTION_CALL,ACTION_LOCATION};

    public static final int ACTION_CALL = 1;
    public static final int ACTION_CHAT = 2;
    public static final int ACTION_LOCATION = 3;
    public static final int ACTION_QR= 4;

    public interface ShopActionDelegate{
        void onActionSelected(int action);
    }

    private RecyclerListView listView;

    private ShopActionDelegate shopActionDelegate;

    public void setShopActionDelegate(ShopActionDelegate shopActionDelegate) {
        this.shopActionDelegate = shopActionDelegate;
    }

    private LinearLayoutManager horizontalLayoutManager;
    private ActionAdapter actionAdapter;

    public ShopActionListView(Context context) {
        super(context);
        setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        listView = new RecyclerListView(context);
        listView.setItemAnimator(null);
        setLayoutAnimation(null);
        horizontalLayoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        setClipToPadding(false);
        horizontalLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(horizontalLayoutManager);
        listView.setAdapter(actionAdapter = new ActionAdapter(context));
        listView.setOnItemClickListener((view1, position) -> {
            int left = view1.getLeft();
            int right = view1.getRight();
            if (left < 0) {
                listView.smoothScrollBy(left - AndroidUtilities.dp(8), 0);
            } else if (right > getMeasuredWidth()) {
                listView.smoothScrollBy(right - getMeasuredWidth(), 0);
            }
            if(shopActionDelegate != null){
                shopActionDelegate.onActionSelected(actions[position]);
            }
        });
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = 16;

            }
        });

        addView(listView,LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER));
    }


    private class ActionAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ActionAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            ActionButton actionButton = new ActionButton(mContext);

            int height = parent.getMeasuredHeight();
            int width = parent.getMeasuredWidth()/getItemCount() - 16;
            actionButton.setLayoutParams(new RecyclerView.LayoutParams(width, height));
            return new RecyclerListView.Holder(actionButton);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ActionButton simpleImageTextCell = (ActionButton)holder.itemView;
            simpleImageTextCell.setTextAndIcon(titles[position],res[position]);
        }

        @Override
        public int getItemCount() {
            return res.length;
        }
    }

    private static class ActionButton extends FrameLayout {

        private TextView textView;
        private ImageView imageView;

        public ActionButton(Context context) {
            super(context);
            setWillNotDraw(false);

            imageView = new ImageView(context);
            imageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlue));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 12, 0, 0));

            textView = new TextView(context);
            textView.setMaxLines(1);
            textView.setSingleLine();
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlue));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            textView.setLineSpacing(-AndroidUtilities.dp(2), 1.0f);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 24 + 22, 0, 0));

        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(widthMeasureSpec, EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(62 + 16), EXACTLY));
        }


        public void setTextAndIcon( String text, int icon) {
            textView.setText(text);
            imageView.setImageResource(icon);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            Theme.dividerPaint.setStrokeWidth(2);
            canvas.drawLine(getMeasuredWidth() ,12,getMeasuredWidth() + 2, 62 + 4 ,Theme.dividerPaint);
        }

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = AndroidUtilities.dp(62 + 16);
//        setMeasuredDimension(width,height);
//    }
//
//



}
