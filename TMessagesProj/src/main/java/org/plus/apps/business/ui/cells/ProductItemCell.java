//package org.plus.apps.business.ui.cells;
//
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.PorterDuff;
//import android.graphics.Typeface;
//import android.text.Spannable;
//import android.text.SpannableStringBuilder;
//import android.text.TextUtils;
//import android.text.style.ForegroundColorSpan;
//import android.text.style.RelativeSizeSpan;
//import android.text.style.StyleSpan;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.SimpleTextView;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Components.BackupImageView;
//import org.telegram.ui.Components.LayoutHelper;
//
//import java.time.Instant;
//
//public class ProductCell extends FrameLayout {
//
//    public static String TAG = ProductImageCell.class.getSimpleName();
//
//    public interface ProductCellDelegate{
//        default  void onFavSelected(ShopDataSerializer.Product product,boolean fav){
//        }
//    }
//
//    private ShopDataSerializer.Product currentProduct;
//
//    private SimpleTextView dateTextView;
//    private TextView nameTextView;
//    private TextView priceTextView;
//    private ImageView favImageView;
//    private BackupImageView imageView;
//
//    private ProductCellDelegate delegate;
//    private FrameLayout topContainer;
//
//    private int itemSize;
//    private boolean isFav;
//
//    public ProductCell(Context context) {
//        super(context);
//
//        int rad = 16;
//
//        itemSize =(int) ( getItemSize(2) / AndroidUtilities.density);
//        itemSize = (int) (itemSize - itemSize*0.1);
//
//        imageView = new BackupImageView(context);
//        imageView.getImageReceiver().setRoundRadius(    rad,rad,0,0);
//        addView(imageView, LayoutHelper.createFrame(itemSize,itemSize,Gravity.TOP|Gravity.LEFT));
//
//
//        topContainer = new FrameLayout(context);
//        int color = ShopUtils.getIntAlphaColor(Color.BLACK,0.2f);
//        topContainer.setBackground(ShopUtils.createTopRoundRectDrawable(rad,color));
//        addView(topContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,itemSize,Gravity.TOP|Gravity.LEFT));
//
//        dateTextView = new SimpleTextView(context);
//        dateTextView.setTextSize(12);
//        dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//
//        dateTextView.setTextColor(Theme.getColor(Theme.key_avatar_text));
//        addView(dateTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 8,8 , 8, 8));
//
//        favImageView  = new ImageView(context);
//        favImageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY);
//        favImageView.setScaleType(ImageView.ScaleType.CENTER);
//        favImageView.setImageResource(R.drawable.ic_like_line);
//        favImageView.setOnClickListener(view -> {
//            isFav =!isFav;
//            favImageView.setImageResource(isFav?R.drawable.ic_love_filled:R.drawable.ic_like_line);
//            if(delegate != null && currentProduct != null){
//                delegate.onFavSelected(currentProduct,isFav);
//            }
//        });
//        addView(favImageView,LayoutHelper.createFrame(42,42,Gravity.RIGHT | Gravity.TOP,0,itemSize,0,0));
//
//
//        nameTextView = new TextView(context);
//        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        nameTextView.setMaxLines(1);
//        nameTextView.setSingleLine();
//        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        nameTextView.setGravity(Gravity.LEFT);
//        nameTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
//        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
//        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0,itemSize + 4  , 48, 0));
//
//        priceTextView = new TextView(context);
//        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
//        priceTextView.setLines(1);
//        priceTextView.setMaxLines(1);
//        dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        priceTextView.setSingleLine(true);
//        priceTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        priceTextView.setGravity(Gravity.LEFT);
//        priceTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8));
//        addView(priceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, itemSize + 16 + 16 + 4, 0, 0));
//
//
//        //setBackground(ShopUtils.createRoundStrokeDrwable(rad,3,Theme.key_windowBackgroundGray,Theme.key_windowBackgroundWhite));
//
//    }
//
//    public void setFav(boolean fav){
//        isFav =fav;
//        favImageView.setImageResource(isFav?R.drawable.ic_love_filled:R.drawable.ic_like_line);
//    }
//
//    public boolean isFav() {
//        return isFav;
//    }
//    public void setDelegate(ProductCellDelegate delegate) {
//        this.delegate = delegate;
//    }
//
//
//
//    public void setProduct(ShopDataSerializer.Product product) {
//        if(product == null|| ShopUtils.isEmpty(product.title)){
//            return;
//        }
//        currentProduct = product;
//        if(currentProduct.images != null && !currentProduct.images.isEmpty() && currentProduct.images.get(0) != null && currentProduct.images.get(0).photo != null){
//            imageView.setImage(product.images.get(0).photo,"100_100",Theme.chat_attachEmptyDrawable);
//        }else{
//            imageView.setImage(null,null,Theme.chat_attachEmptyDrawable);
//        }
//        String title = product.title.substring(0, 1).toUpperCase() + currentProduct.title.substring(1).toLowerCase();
//        nameTextView.setText(title);
//
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//        spannableStringBuilder.append(ShopUtils.formatCurrency(currentProduct.price));
//        int start = spannableStringBuilder.length();
//        spannableStringBuilder.append("\n");
//        spannableStringBuilder.append(currentProduct.usage_condition);
//        int end = spannableStringBuilder.length();
//        spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_dialogTextGray2)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        priceTextView.setText(spannableStringBuilder);
//
//        try {
//            dateTextView.setText(ShopUtils.formatLast(product.created_at));
//            Instant instant = Instant.parse(product.created_at);
//            dateTextView.setText(LocaleController.formatSectionDate(instant.toEpochMilli()/1000));
//        }catch (Exception e){
//            Log.e(TAG,e.getMessage());
//        }
//        setFav(product.is_favorite);
//    }
//
//    public ShopDataSerializer.Product getCurrentProduct() {
//        return currentProduct;
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
