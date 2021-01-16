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
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import org.plus.apps.business.ShopUtils;
//import org.plus.apps.business.data.ShopDataSerializer;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.R;
//import org.telegram.ui.ActionBar.SimpleTextView;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Components.AvatarDrawable;
//import org.telegram.ui.Components.BackupImageView;
//import org.telegram.ui.Components.LayoutHelper;
//
//public class ProductCell extends FrameLayout {
//
//    public interface ProductCellDelegate{
//
//       default void onProductSelect(ShopDataSerializer.FeaturedProduct product){
//
//       };
//
//       default void onShopPressed(long channel){
//
//       }
//
//    }
//
//
//    private ShopDataSerializer.Product product;
//
//    private int itemSize;
//
//    private SimpleTextView dateTextView;
//
//    private TextView nameTextView;
//    private TextView priceTextView;
//    private ImageView favImageView;
//    private BackupImageView imageView;
//
//    private ProductCellDelegate delegate;
//    private FrameLayout topContainer;
//
//    private BackupImageView shopProfileImageView;
//    private TextView shopNameTextView;
//
//    private boolean isFav;
//
//    public ProductCell(Context context) {
//        super(context);
//
//        int rad = 16;
//
//        itemSize =(int) ( getItemSize(2) / AndroidUtilities.density) ;
//        itemSize = (int) (itemSize - itemSize*0.2);
//
//        imageView = new BackupImageView(context);
//        imageView.getImageReceiver().setRoundRadius(rad,rad,0,0);
//        addView(imageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,itemSize,Gravity.TOP|Gravity.LEFT));
//        imageView.setOnClickListener(v -> {
//            if(delegate != null && featuredProduct != null){
//                delegate.onProductSelect(featuredProduct);
//            }
//        });
//
//        topContainer = new FrameLayout(context);
//        int color = ShopUtils.getIntAlphaColor(Color.BLACK,0.2f);
//        topContainer.setBackground(ShopUtils.createTopRoundRectDrawable(rad,color));
//        addView(topContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,itemSize,Gravity.TOP|Gravity.LEFT));
//
//        dateTextView = new SimpleTextView(context);
//        dateTextView.setTextSize(12);
//        dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        dateTextView.setTextColor(Theme.getColor(Theme.key_avatar_text));
//        addView(dateTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 8,8 , 8, 8));
//
//
//        favImageView  = new ImageView(context);
//        favImageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY);
//        favImageView.setScaleType(ImageView.ScaleType.CENTER);
//        favImageView.setImageResource(R.drawable.msg_fave);
//        favImageView.setOnClickListener(view -> {
//            isFav =!isFav;
//            favImageView.setImageResource(isFav?R.drawable.ic_ab_fave:R.drawable.msg_fave);
//        });
//        favImageView.setPadding(AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16),AndroidUtilities.dp(16));
//        addView(favImageView,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.RIGHT | Gravity.TOP,0,itemSize  ,0,0));
//
//        nameTextView = new TextView(context);
//        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        nameTextView.setMaxLines(2);
//        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
//        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        nameTextView.setGravity(Gravity.LEFT);
//        nameTextView.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
//        nameTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0,itemSize + 8  , 48, 0));
//
//        priceTextView = new TextView(context);
//        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
//        priceTextView.setLines(1);
//        priceTextView.setMaxLines(1);
//        priceTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        priceTextView.setSingleLine(true);
//        priceTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        priceTextView.setGravity(Gravity.LEFT);
//        priceTextView.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(16));
//        addView(priceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, itemSize + 16 + 16 + 8, 48, 0));
//
//
//        shopProfileImageView = new BackupImageView(context);
//        shopProfileImageView.setRoundRadius(AndroidUtilities.dp(12));
//        addView(shopProfileImageView, LayoutHelper.createFrame(24 ,24, Gravity.LEFT|Gravity.TOP, 8, itemSize + 78, 0, 0));
//
//        shopNameTextView = new TextView(context);
//        shopNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
//        shopNameTextView.setLines(1);
//        shopNameTextView.setMaxLines(1);
//        shopNameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        shopNameTextView.setSingleLine(true);
//        shopNameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
//        shopNameTextView.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
//        shopNameTextView.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(16));
//        addView(shopNameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 28 + 8, itemSize + 72, 8, 0));
//
//        shopProfileImageView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(delegate != null && product != null ){
//                    delegate.onShopPressed(product.shop.chanel);
//                }
//            }
//        });
//
//       // setBackground(ShopUtils.createRoundStrokeDrwable(rad,3,Theme.key_windowBackgroundGray,Theme.key_windowBackgroundWhite));
//
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
//    @Override
//    public void requestLayout() {
//        if (ignoreLayout) {
//            return;
//        }
//        super.requestLayout();
//    }
//
////    @Override
////    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
////        final int itemWidth = getItemSize(2);
////        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec((AndroidUtilities.dp(72)) + itemWidth, MeasureSpec.EXACTLY));
////    }
//
//
//    private ShopDataSerializer.FeaturedProduct featuredProduct;
//
//    public ShopDataSerializer.FeaturedProduct getFeaturedProduct() {
//        return featuredProduct;
//    }
//
//    public void setData(ShopDataSerializer.FeaturedProduct product){
//        if(product == null || ShopUtils.isEmpty(product.title)){
//            return;
//        }
//        featuredProduct = product;
//
//        if(product.pictureSnip != null){
//            imageView.setImage(product.pictureSnip.photo,"100_100",Theme.chat_attachEmptyDrawable);
//        }else{
//            imageView.setImage(null,"100_100",Theme.chat_attachEmptyDrawable);
//
//        }
//        String title = product.title.substring(0, 1).toUpperCase() + product.title.substring(1).toLowerCase();
//        nameTextView.setText(title);
//        priceTextView.setText(ShopUtils.formatCurrency(product.price));
//
//        if(product.shopSnip != null){
//            AvatarDrawable avatarDrawable = new AvatarDrawable();
//            avatarDrawable.setInfo(5,product.title,null);
//            if(product.shopSnip.profile_picture != null){
//                shopProfileImageView.setImage(product.shopSnip.profile_picture.photo,"100_100",avatarDrawable);
//            }else{
//                shopProfileImageView.setImage(null,null,avatarDrawable);
//            }
//            shopNameTextView.setText(product.shopSnip.title);
//        }
//    }
//
//    public void setData(ShopDataSerializer.Product pro) {
//        if(pro == null|| ShopUtils.isEmpty(pro.title)){
//            return;
//        }
//        product = pro;
//        if(product.images != null && !product.images.isEmpty() && product.images.get(0) != null && product.images.get(0).photo != null){
//
//            imageView.setImage(product.images.get(0).photo,"100_100",Theme.chat_attachEmptyDrawable);
//        }else{
//            imageView.setImage(null,"100_100",Theme.chat_attachEmptyDrawable);
//
//        }
//
//        String title = pro.title.substring(0, 1).toUpperCase() + pro.title.substring(1).toLowerCase();
//
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//        spannableStringBuilder.append(title);
//        int start = spannableStringBuilder.length();
//        spannableStringBuilder.append("\n");
//        spannableStringBuilder.append(pro.usage_condition);
//        int end = spannableStringBuilder.length();
//        spannableStringBuilder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_dialogTextGray2)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.setSpan(new StyleSpan(Typeface.NORMAL), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.setSpan(new RelativeSizeSpan(0.8f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        nameTextView.setText(spannableStringBuilder);
//
//        priceTextView.setText(ShopUtils.formatCurrency(pro.price));
//
//
//        dateTextView.setText(ShopUtils.formatLast(product.created_at));
//
//    }
//
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
