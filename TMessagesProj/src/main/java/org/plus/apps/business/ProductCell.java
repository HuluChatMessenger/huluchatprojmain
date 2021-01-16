package org.plus.apps.business;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.time.Instant;

public class ProductCell extends FrameLayout {

    public interface ProductDelegate{


        default void onFavSelected(long chat_id,int  product_id,boolean favorite){
        }

        default void onShopClicked(long chat_id){
        }
    }


    private ShopDataSerializer.Product currentProduct;
    private ShopDataSerializer.FeaturedProduct feature_product;

    private ProductDelegate delegate;

    private SimpleTextView dateTextView;
    private TextView nameTextView;
    private TextView priceTextView;
    private ImageView favImageView;
    private BackupImageView productImageView;
    private TextView oldPriceTextView;

    private GradientDrawable topOverlayGradient;

    private BackupImageView shopAvatarImageView;
    private SimpleTextView shopTitleTextView;
    private FrameLayout shopFrameLayout;

    public boolean isFavorite() {
        return favorite;
    }

    private boolean favorite;
    private boolean with_shop;

    private long chat_id;
    private int product_id;

    public void setDelegate(ProductDelegate delegate) {
        this.delegate = delegate;
    }

    public ProductCell(Context context){
       this(context,false);
    }


    private int itemSize = (int) ( getItemSize(2) / AndroidUtilities.density);;
    public ProductCell(Context context,boolean withShop) {
        super(context);

        with_shop  = withShop;
        itemSize = (int) (itemSize - itemSize*0.1);

        productImageView = new BackupImageView(context);
        productImageView.setSize(AndroidUtilities.dp(itemSize),AndroidUtilities.dp(itemSize));
        productImageView.getImageReceiver().setRoundRadius(16,16,0,0);
        addView(productImageView, LayoutHelper.createFrame(itemSize,itemSize, Gravity.TOP|Gravity.LEFT));


        topOverlayGradient = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0x42000000, 0});
        topOverlayGradient.setCornerRadii(new float[] { 16, 16, 16, 16, 0, 0, 0, 0 });
        topOverlayGradient.setShape(GradientDrawable.RECTANGLE);

        View view = new View(context);
        view.setBackground(topOverlayGradient);
        addView(view, LayoutHelper.createFrame(itemSize,itemSize, Gravity.TOP|Gravity.LEFT));

        dateTextView = new SimpleTextView(context);
        dateTextView.setTextSize(12);
        dateTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
        dateTextView.setTextColor(Theme.getColor(Theme.key_avatar_text));
        addView(dateTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 8,8 , 32, 8));

        favImageView  = new ImageView(context);
        favImageView.setVisibility(GONE);
        favImageView.setClickable(false);
        favImageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY);
        favImageView.setScaleType(ImageView.ScaleType.CENTER);
        favImageView.setOnClickListener(v -> {
            favorite =!favorite;
            favImageView.setImageResource(favorite?R.drawable.ic_ab_fave:R.drawable.outline_fave);
            if(delegate != null){
                delegate.onFavSelected(chat_id,product_id,favorite);
            }
        });
        addView(favImageView,LayoutHelper.createFrame(32,32,Gravity.RIGHT | Gravity.TOP,0,itemSize ,0,0));

        nameTextView = new TextView(context);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine();
        //nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
        nameTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0,itemSize  , 32, 0));

        priceTextView = new TextView(context);
        priceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        priceTextView.setLines(1);
        priceTextView.setMaxLines(1);
        priceTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
        priceTextView.setSingleLine(true);
        priceTextView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        priceTextView.setGravity(Gravity.LEFT);
        priceTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
        addView(priceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, itemSize + 14  + 14, 0, 0));

        oldPriceTextView = new TextView(context);
        oldPriceTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        oldPriceTextView.setLines(1);
        oldPriceTextView.setMaxLines(1);
        oldPriceTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        oldPriceTextView.setSingleLine(true);
        oldPriceTextView.setEllipsize(TextUtils.TruncateAt.END);
        oldPriceTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        oldPriceTextView.setGravity(Gravity.LEFT);
        oldPriceTextView.setPadding(AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4),AndroidUtilities.dp(4));
        addView(oldPriceTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, itemSize + 14  + 14 + 20 , 0, 0));

        shopFrameLayout = new FrameLayout(context);
        if(with_shop){
            FrameLayout shopFrame = new FrameLayout(context);
            shopFrameLayout = shopFrame;
           // shopFrame.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarWhiteSelector)));
            shopFrame.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                   if(delegate != null){
                       delegate.onShopClicked(chat_id);
                   }
                }
            });
            shopAvatarImageView = new BackupImageView(context);
            shopAvatarImageView.setSize(AndroidUtilities.dp(18),AndroidUtilities.dp(18));
            shopAvatarImageView.setRoundRadius(AndroidUtilities.dp(9));
            shopFrame.addView(shopAvatarImageView, LayoutHelper.createFrame(18, 18, Gravity.LEFT|Gravity.TOP, 0, 0, 0, 0));

            shopTitleTextView = new SimpleTextView(context);
            shopTitleTextView.setTextSize(10);
            shopTitleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            shopTitleTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
            shopFrame.addView(shopTitleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT|Gravity.CENTER_VERTICAL, 8 + 19 + 8, 0, 0, 0));

            addView(shopFrameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT|Gravity.TOP, 0, itemSize + 14  + 14 + 20 + 20 + 20, 0, 0));
        }else{
            shopFrameLayout.setVisibility(GONE);
        }
    }




    public void setFavorite(boolean fav){
        favorite = fav;
        favImageView.setImageResource(favorite?R.drawable.ic_ab_fave:R.drawable.outline_fave);
    }

    public void setFeatureProduct(ShopDataSerializer.FeaturedProduct featureProduct){
        if (featureProduct == null || ShopUtils.isEmpty(featureProduct.title) || featureProduct.shopSnip == null) {
            return;
        }
        feature_product = featureProduct;
        product_id = featureProduct.id;
        chat_id  = featureProduct.shopSnip.channel;
        favorite = featureProduct.is_favorite;

        favImageView.setImageResource(favorite?R.drawable.ic_ab_fave:R.drawable.outline_fave);

        if (featureProduct.pictureSnip != null && featureProduct.pictureSnip.photo != null) {
            productImageView.setImage(featureProduct.pictureSnip.photo, "100_100", Theme.chat_attachEmptyDrawable);
        } else {
            productImageView.setImage(null, null, Theme.chat_attachEmptyDrawable);
        }
        String title = featureProduct.title.substring(0, 1).toUpperCase() + featureProduct.title.substring(1).toLowerCase();
        nameTextView.setText(title);
        priceTextView.setText(ShopUtils.formatCurrency(featureProduct.price));
        try {
            dateTextView.setText(ShopUtils.formatShopDate(featureProduct.created_at));
        } catch (Exception ignore) {
        }
        if(featureProduct.old_price > 0){
            String product_price = ShopUtils.formatCurrency(featureProduct.old_price);
            int oldEnd = product_price.length();
            double gap = featureProduct.old_price - featureProduct.price;
            double per = Math.abs((gap / featureProduct.old_price)) * 100;
            String percent = " ";
            int color;
            if (gap > 0) {
                color = Theme.getColor(Theme.key_chat_inGreenCall);
                percent += String.format("%.1f", per) + "% off";
            } else {
                color = Theme.getColor(Theme.key_chat_inRedCall);
                percent += String.format("%.1f", per) + "% up";
            }
            product_price += percent;
            SpannableString string = new SpannableString(product_price);
            string.setSpan(new StrikethroughSpan(), 0, oldEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            string.setSpan(new ForegroundColorSpan(color), oldEnd, product_price.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            string.setSpan(new RelativeSizeSpan(0.6f), oldEnd, product_price.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            oldPriceTextView.setText(string);
            oldPriceTextView.setVisibility(VISIBLE);
        }else{
            oldPriceTextView.setVisibility(GONE);
            if(with_shop){
                FrameLayout.LayoutParams frameLayoutLayoutParams =  ( FrameLayout.LayoutParams) shopFrameLayout.getLayoutParams();
                frameLayoutLayoutParams.topMargin = AndroidUtilities.dp(itemSize + 14  + 14 + 20 + 10);
                shopFrameLayout.setLayoutParams(frameLayoutLayoutParams);
            }

        }

        if(featureProduct.shopSnip != null && shopAvatarImageView != null){
            if (featureProduct.shopSnip.profile_picture != null && featureProduct.shopSnip.profile_picture.photo != null) {
                shopAvatarImageView.setImage(featureProduct.shopSnip.profile_picture.photo, "100_100", Theme.chat_attachEmptyDrawable);
            } else {
                shopAvatarImageView.setImage(null, null, Theme.chat_attachEmptyDrawable);
            }
            shopTitleTextView.setText(featureProduct.shopSnip.title);
        }
    }

    public void setProduct(ShopDataSerializer.Product product ) {
        if (product == null || ShopUtils.isEmpty(product.title) || product.shop == null) {
            return;
        }
        currentProduct = product;

        product_id = currentProduct.id;
        chat_id  = currentProduct.shop.chanel;
        favorite = product.is_favorite;

        favImageView.setImageResource(favorite?R.drawable.ic_ab_fave:R.drawable.outline_fave);

        if (currentProduct.images != null && !currentProduct.images.isEmpty() && currentProduct.images.get(0) != null && currentProduct.images.get(0).photo != null) {
            productImageView.setImage(product.images.get(0).photo, "100_100", Theme.chat_attachEmptyDrawable);
        } else {
            productImageView.setImage(null, null, Theme.chat_attachEmptyDrawable);
        }
        String title = product.title.substring(0, 1).toUpperCase() + currentProduct.title.substring(1).toLowerCase();
        nameTextView.setText(title);
        priceTextView.setText(ShopUtils.formatCurrency(currentProduct.price));
        try {
            dateTextView.setText(ShopUtils.formatShopDate(currentProduct.created_at));
        } catch (Exception ignore) {
        }
        if(product.old_price > 0){
            String product_price = ShopUtils.formatCurrency(product.old_price);
            int oldEnd = product_price.length();
            double gap = product.old_price - product.price;
            double per = Math.abs((gap / product.old_price)) * 100;
            String percent = " ";
            int color;
            if (gap > 0) {
                color = Theme.getColor(Theme.key_chat_inGreenCall);
                percent += String.format("%.1f", per) + "% off";
            } else {
                color = Theme.getColor(Theme.key_chat_inRedCall);
                percent += String.format("%.1f", per) + "% up";
            }
            product_price += percent;
            SpannableString string = new SpannableString(product_price);
            string.setSpan(new StrikethroughSpan(), 0, oldEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            string.setSpan(new ForegroundColorSpan(color), oldEnd, product_price.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            string.setSpan(new RelativeSizeSpan(0.6f), oldEnd, product_price.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            oldPriceTextView.setText(string);
            oldPriceTextView.setVisibility(VISIBLE);
        }else{
            oldPriceTextView.setVisibility(GONE);
        }
    }


    public static int getItemSize(int itemsCount) {
        final int itemWidth;
        if (AndroidUtilities.isTablet()) {
            itemWidth = (AndroidUtilities.dp(490) - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
        } else {
            itemWidth = (AndroidUtilities.displaySize.x - (itemsCount - 1) * AndroidUtilities.dp(2)) / itemsCount;
        }
        return itemWidth;
    }

}
