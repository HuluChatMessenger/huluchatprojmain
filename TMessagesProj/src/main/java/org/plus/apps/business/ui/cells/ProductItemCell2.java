//package org.plus.apps.business.ui.cells;
//
//
//import android.content.Context;
//import android.text.TextUtils;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Components.BackupImageView;
//import org.telegram.ui.Components.LayoutHelper;
//
//public class ProductCell2 extends FrameLayout {
//
//    public interface ProductCellDelegate{
//        void onProductSelect(ShopDataSerializer.Product product, ProductCell2 ProductCell);
//    }
//
//    private ShopDataSerializer.Product product;
//
//    private int itemSize;
//    private int itemHeight;
//
//    private TextView nameTextView;
//    private TextView priceTextView;
//    private ImageView favImageView;
//    private BackupImageView imageView;
//
//    private ProductCellDelegate delegate;
//
//    public ProductCell2(Context context) {
//        super(context);
//
//        itemSize = getItemSize(2);
//        itemHeight = itemSize + 61;
//        itemSize =(int) (itemHeight / AndroidUtilities.density);
//        itemHeight = (int)((itemSize + AndroidUtilities.dp(61))/AndroidUtilities.density);
//
//        imageView = new BackupImageView(context);
//        imageView.setRoundRadius(AndroidUtilities.dp(4));
//        addView(imageView, LayoutHelper.createFrame(100,120,Gravity.CENTER_HORIZONTAL,0,16,0,16));
//
//        favImageView  = new ImageView(context);
//        favImageView.setImageResource(R.drawable.msg_fave);
//        favImageView.setOnClickListener(view -> {
//            if(delegate != null){
//                delegate.onProductSelect(product,ProductCell2.this);
//            }
//        });
//        favImageView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));
//        addView(favImageView,LayoutHelper.createFrame(48,48,Gravity.RIGHT | Gravity.TOP));
//
//
//        nameTextView = new TextView(context);
//        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        nameTextView.setLines(1);
//        nameTextView.setMaxLines(1);
//        nameTextView.setSingleLine(true);
//        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        nameTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//        nameTextView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(4),AndroidUtilities.dp(16),AndroidUtilities.dp(4));
//        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
//        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0,152 , 0, 0));
//
//        priceTextView = new TextView(context);
//        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
//        priceTextView.setLines(1);
//        priceTextView.setMaxLines(1);
//        priceTextView.setSingleLine(true);
//        priceTextView.setTextColor(Theme.getColor(Theme.key_actionBarTabLine));
//        priceTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//        priceTextView.setPadding(AndroidUtilities.dp(0),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
//        addView(priceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, 176 + 16, 0, 0));
//
//        setBackground(Theme.createRoundRectDrawable(16,Theme.getColor(Theme.key_windowBackgroundWhite)));
//    }
//
//
//    private boolean ignoreLayout;
//
//    public void setDelegate(ProductCellDelegate delegate) {
//        this.delegate = delegate;
//    }
//
//
//    public void setItemSize(int size) {
//        itemSize = size;
//        LayoutParams layoutParams = (LayoutParams)getLayoutParams();
//        layoutParams.width = size;
//        layoutParams.height = size + 61;
//        requestLayout();
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
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        final int itemWidth = getItemSize(2);
////        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) getLayoutParams();
////        layoutParams.topMargin  = AndroidUtilities.dp(8);
////        layoutParams.leftMargin = AndroidUtilities.dp(16);
////        if(AndroidUtilities.isTablet()){
////            layoutParams.width = (AndroidUtilities.dp(490) - (AndroidUtilities.dp(16 + 8))/2);
////        }else{
////            layoutParams.width = (AndroidUtilities.displaySize.x - (AndroidUtilities.dp(16 + 8))/2);
////
////        }
////        layoutParams.height = itemWidth + 61;
////        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
////        ignoreLayout = true;
////        setLayoutParams(layoutParams);
////        ignoreLayout = false;
//        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((AndroidUtilities.dp(61)) + itemWidth, MeasureSpec.EXACTLY));
//
//    }
//
//
//
//    public void setData(ShopDataSerializer.Product pro) {
//        if(pro == null){
//            return;
//        }
//        product = pro;
//        imageView.setImage(product.images.get(0).photo,null,null);
//        nameTextView.setText(product.title);
//        priceTextView.setText(ShopUtils.formatCurrency(product.price));
//    }
//
//
//    public ShopDataSerializer.Product getProduct() {
//        return product;
//    }
//
//    public static int getItemSize(int itemsCount) {
//        final int itemWidth;
//        if (AndroidUtilities.isTablet()) {
//            itemWidth = (AndroidUtilities.dp(490) - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
//        } else {
//            itemWidth = (AndroidUtilities.displaySize.x - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
//        }
//        return itemWidth;
//    }
//
//}
