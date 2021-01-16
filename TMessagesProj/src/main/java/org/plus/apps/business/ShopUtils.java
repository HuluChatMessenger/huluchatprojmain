package org.plus.apps.business;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.BusinessProfileActivity;
import org.plus.apps.business.ui.ProductDetailFragment;
import org.plus.apps.business.ui.components.ShopsEmptyCell;
import org.plus.net.APIError;
import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.ShareAlert;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShopUtils {

   public static int NETWORK_EXCEPTION = 1;


    public static final String open_product_link = "tg://open?shop_id={shopId}&product_id={productId}";

    public boolean isUserSupported(int account){
        boolean supoported = false;
        TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
        if (user != null && user.phone != null && user.phone.startsWith("251")) {
            supoported = true;
        }
        return supoported;
    }

    public static void openFragment(BaseFragment fragment, String link){
        if(fragment == null || link == null){
            return;
        }
        Integer shop_id =null;
        Integer product_id = null;
        Uri data = Uri.parse(link);
        String url = data.toString();
        if(url.startsWith("tg://open")){
            url = url.replace("tg://open", "tg://open.org");
            data = Uri.parse(url);
            shop_id = Utilities.parseInt(data.getQueryParameter("shop_id"));
            if(url.contains("product_id")){
                product_id = Utilities.parseInt(data.getQueryParameter("product_id"));
            }
            if(product_id == null){
                Bundle bundle = new Bundle();
                bundle.putInt("chat_id",shop_id);
                fragment.presentFragment(new BusinessProfileActivity(bundle));
            }else{
                Bundle bundle = new Bundle();
                bundle.putInt("chat_id", shop_id);
                bundle.putInt("item_id",product_id);
                ProductDetailFragment detailFragment = new ProductDetailFragment(bundle);
                fragment.presentFragment(detailFragment);
            }

        }
    }

    public static  boolean isHuluChatSupportedLink(String text){
        if(text.startsWith("tg://open")){
            return true;
        }
        return false;
    }



    public static boolean containInternalLink(MessageObject messageObject){
        boolean contain = false;
        if(messageObject != null && messageObject.messageOwner != null && messageObject.messageOwner.message != null &&  messageObject.messageOwner.message.contains("tg://open?")){
            return true;
        }
        return contain;
    }

    public static String getProductLink(int channel_id,int item_id){
        return open_product_link.replace("{shopId}",String.valueOf(channel_id)).replace("{productId}",String.valueOf(item_id));
    }

    public static void shareProduct(AccountInstance accountInstance, Map<String,Object> productFull, int dialog_id){
        if(productFull == null) {
            return;
        }



            ArrayList<SendMessagesHelper.SendingMediaInfo> photos = new ArrayList<>();
            SendMessagesHelper.SendingMediaInfo sendingMediaInfo = new SendMessagesHelper.SendingMediaInfo();
            sendingMediaInfo.caption = "This is the cation";
            sendingMediaInfo.isVideo = false;
            sendingMediaInfo.uri= Uri.parse("https://play-lh.googleusercontent.com/iBYjvYuNq8BB7EEEHktPG1fpX9NiY7Jcyg1iRtQxO442r9CZ8H-X9cLkTjpbORwWDG9d=s180-rw");
            photos.add(sendingMediaInfo);
            SendMessagesHelper.prepareSendingMedia(accountInstance,photos,dialog_id,null,null,null,false,false,null,true,0);


        }


    public static String getShopLink(int channel_id){
        return "tg://open?shop_id=" + channel_id;
    }


    public static final String ui_picture = "ui_picture";
    public static final String ui_location = "ui_location";

    public static final String UI_CHOOSE_HORIZONTAL = "ui_choose_hor";
    public static final String UI_INPUT_STRING = "ui_input_str";
    public static final String UI_INPUT_NUM = "ui_input_num";
    public static final String UI_INPUT_LOC = "ui_input_loc";
    public static final String UI_CHOOSE = "ui_choose";
    public static final String ui_radio_box = "ui_radio_box";
    public static final String ui_date_chooser = "ui_date_chooser";

    public static final int SHOP_TYPE_CAR = 1;
    public static final int SHOP_TYPE_HOUSE = 2;
    public static final int SHOP_TYPE_ELECTRONICS = 3;
    public static final int SHOP_TYPE_FASHION =  4;
    public static final int SHOP_TYPE_FURNITURE =  5;
    public static final int SHOP_TYPE_HEALTH_BEAUTY = 6;
    public static final int SHOP_TYPE_FOD_AND_BEVERAGE =  7;
    public static final int SHOP_TYPE_FOD_AND_GENERAL =  8;

    public static String formatReviewData(String date){
        Instant instant = Instant.parse( date);
        long epochSec = instant.toEpochMilli();
        return LocaleController.getInstance().formatterYearMax.format(epochSec);
    }

    public static String formatLast(String data){
        try {

            long date = Instant.parse(data).toEpochMilli();

            Calendar rightNow = Calendar.getInstance();
            int day = rightNow.get(Calendar.DAY_OF_YEAR);
            int year = rightNow.get(Calendar.YEAR);
            rightNow.setTimeInMillis(date);
            int dateDay = rightNow.get(Calendar.DAY_OF_YEAR);
            int dateYear = rightNow.get(Calendar.YEAR);

            if (dateDay == day && year == dateYear) {
                int diff = (int) (ConnectionsManager.getInstance(UserConfig.selectedAccount).getCurrentTime() - date / 1000) / 60;
                if (diff < 1) {
                    return LocaleController.getString("shopUpdatedJustNow", R.string.shopUpdatedJustNow);
                } else if (diff < 60) {
                    return LocaleController.formatPluralString("UpdatedMinutes", diff);
                }
                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, LocaleController.formatString("TodayAtFormatted", R.string.TodayAtFormatted, LocaleController.getInstance().formatterDay.format(new Date(date))));
            } else if (dateDay + 1 == day && year == dateYear) {
                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, LocaleController.formatString("YesterdayAtFormatted", R.string.YesterdayAtFormatted, LocaleController.getInstance().formatterDay.format(new Date(date))));
            } else if (Math.abs(System.currentTimeMillis() - date) < 31536000000L) {
                String format = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterDayMonth.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, format);
            } else {
                String format = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDay.format(new Date(date)));
                return LocaleController.formatString("ShopUpdatedFormatted", R.string.ShopUpdatedFormatted, format);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return "LOC_ERR";


    }




    public static String formatSort(ShopDataSerializer.ProductType.Sort  sort){
        if(sort == null){
            return "";
        }
        return sort.sortby;
    }

    public static String formatDuration(int duration) {
        if (duration <= 0) {
            return LocaleController.formatPluralString("Seconds", 0);
        }
        final int hours = duration / 3600;
        final int minutes = duration / 60 % 60;
        final int seconds = duration % 60;

        final StringBuilder stringBuilder = new StringBuilder();
        if (hours > 0) {
            stringBuilder.append(LocaleController.formatPluralString("Hours", hours));
        }
        if (minutes > 0) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(LocaleController.formatPluralString("Minutes", minutes));
        }
        if (seconds > 0) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(LocaleController.formatPluralString("Seconds", seconds));
        }
        return stringBuilder.toString();
    }


    public static String formatShopDate(String date){
        Instant instant = Instant.parse(date);
        long epochSec = instant.toEpochMilli();
        int sec = (int) ((Instant.now().toEpochMilli() -  epochSec) * 0.001);//secounds


        if(sec < 60){
            return "moments ago";
        }
        int min  = sec/60;
        if(min < 60){
            return min + " min ago";
        }
        int hour = min/60;
        if(hour < 24){
            return hour + " hours ago";
        }
        int days = hour/24;
        if(days < 7){
            return days + " days ago";
        }else if(days >=7 && days < 30){
            int weeks = days / 4;
            return weeks + " weeks ago";
        }else if(days >=30 && days < 365){
            int month = days / 4 / 12 + 1;
            return month + " mon ago";
        }else{
            int year = days / 4 / 12 + 1;
            return year + " years ago";
        }
    }



    public static String formatShopAbout(ShopDataSerializer.Shop shop){
        if(shop == null){
            return "";
        }
        return shop.count + " items";

    }

    public static String formatShopRating(int shop){

        SpannableStringBuilder builder = new SpannableStringBuilder();
        DecimalFormat df = new DecimalFormat("#.#");
        double averageRating = Double.parseDouble(df.format(shop));
        for(int a = 0; a < averageRating; a++){
            builder.append("☆");
        }
        builder.append("(").append(String.valueOf(shop)).append(")");
        return builder.toString();

    }


    public static String formatShopAboutt(ShopDataSerializer.Shop shop){
        if(shop == null){
            return "";
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String rating = shop.rating_average + " ";
        builder.append(rating);
        builder.append(" ⭐⭐⭐ ");

        builder.append(" ( ").append(String.valueOf(shop.rating_count)).append(")");

        if(!isEmpty(shop.website)){
            builder.append(" \uD83D\uDD17  ");
            builder.append(shop.website);
        }

        builder.append("\n");
        builder.append("\n");

        builder.append("\uD83C\uDFD9 ").append(shop.city).append("   ").append("\uD83D\uDCC5  joined ").append(formatReviewData(shop.created_at));
        builder.setSpan(new ForegroundColorSpan(Theme.getColor(Theme.key_chats_secretName)), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("\n");
        builder.append("\n");
        builder.append("\uD83D\uDCCD ");
        builder.append(shop.address);
        builder.append("\n");
        return builder.toString();
    }


    public static String formatCurrency(String price){
        return "ETB "  + price;
    }


    public static String formatCurrency(double price){
        DecimalFormat formatter;
        if(price == (long) price)
            formatter = new DecimalFormat("#,###");
        else
            formatter = new DecimalFormat("#,###.00");
        return "ETB " + formatter.format(price);
    }


    public static int getIntAlphaColor(int color, float factor){
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static long toBotChannelId(int chat_id){
        if(BuildVars.DEBUG_PRIVATE_VERSION){
            return Long.parseLong("-10000" + chat_id);
        }
        return Long.parseLong("-100" + chat_id);
    }

    public static long toBotChannelId(long chat_id){
        if(BuildVars.DEBUG_PRIVATE_VERSION){
            return Long.parseLong("-10000" + chat_id);
        }
        return Long.parseLong("-100" + chat_id);
    }

    public static int toClientChannelId(long chat_id){
        if(BuildVars.DEBUG_PRIVATE_VERSION){
            return Integer.parseInt(String.valueOf(chat_id).replace("-10000",""));

        }
        return Integer.parseInt(String.valueOf(chat_id).replace("-100",""));
    }

    public static Map<Integer,String> businessKeys(){
         Map<Integer,String> keyBusinessMap = new HashMap<>();
         keyBusinessMap.put(SHOP_TYPE_CAR,"car");
        keyBusinessMap.put(SHOP_TYPE_HOUSE,"house");
        keyBusinessMap.put(SHOP_TYPE_ELECTRONICS,"electronics");
        keyBusinessMap.put(SHOP_TYPE_FASHION,"fashion");
        keyBusinessMap.put(SHOP_TYPE_FURNITURE,"furniture");
        keyBusinessMap.put(SHOP_TYPE_HEALTH_BEAUTY,"beauty");
        keyBusinessMap.put(SHOP_TYPE_FOD_AND_BEVERAGE,"beverage");
        keyBusinessMap.put(SHOP_TYPE_FOD_AND_GENERAL,"general");
        return keyBusinessMap;
    }

    public static boolean isEmpty(String string){
        if(TextUtils.isEmpty(string)){
            return true;
        }
        if(string.equals("null")){
            return true;
        }
        return false;
    }

    public static String getEmojiForType(int type){

        if(type  == SHOP_TYPE_CAR){
            return "\uD83D\uDE97";
        }
        return "";
    }

    public static String getDisplayNameForGivenBusiness(int type){
        if(type == SHOP_TYPE_CAR){
            return "Car";
        }
        return "Business";
    }


    public static Drawable createRoundStrokeDrwable(int rad, int stroke, String stroke_color, String fillColor){
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius(rad);
        shape.setStroke(stroke,Theme.getColor(stroke_color));
        shape.setColor(Theme.getColor(fillColor));
        return shape;
    }

    public static Drawable createTopRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, 0, 0, 0, 0}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    public static Drawable createBottomRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{0, 0, 0, 0, rad, rad, rad,rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    public static AlertDialog.Builder createErrorAlert(Context context, APIError apiError){
        if (context == null || apiError == null) {
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        EmptyTextProgressView emptyTextProgressView = new EmptyTextProgressView(context);
        emptyTextProgressView.showTextView();
        emptyTextProgressView.setText(apiError.message());
        builder.setView(emptyTextProgressView);

        return builder;
    }

    public static BottomSheet.Builder createConnectionAlert(Context context,Runnable retry){
        if (context == null) {
            return null;
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(context, false);
        builder.setApplyBottomPadding(false);

        ShopsEmptyCell shopsEmptyCell = new ShopsEmptyCell(context);
        shopsEmptyCell.setType(ShopsEmptyCell.TYPE_RETRY);
        builder.setCustomView(shopsEmptyCell);
        shopsEmptyCell.setRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(retry != null){
                    retry.run();
                    builder.getDismissRunnable().run();
                }
            }
        });
        return builder;
    }

    public static CombinedDrawable createLocBackDrawable(int res){
        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(42), res);
        menuButton.setIconSize(AndroidUtilities.dp(21), AndroidUtilities.dp(21));
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_chats_actionBackground), false);
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_chats_actionIcon), true);
        return  menuButton;
    }
    public static CombinedDrawable createDetailMenuDrawable(int res){
        CombinedDrawable menuButton = Theme.createCircleDrawableWithIcon(AndroidUtilities.dp(42), res);
        menuButton.setIconSize(AndroidUtilities.dp(21), AndroidUtilities.dp(21));
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_actionBarDefault), false);
        Theme.setCombinedDrawableColor(menuButton, Theme.getColor(Theme.key_dialogTextBlack), true);
        return  menuButton;
    }

    public static Bitmap createTonQR(Context context, String key, Bitmap oldBitmap) {
        try {
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            return new QRCodeWriter().encode(key, BarcodeFormat.QR_CODE, 768, 768, hints, oldBitmap, context);
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    public static void shareBitmap(Activity activity, View view, String text) {
        try {
            ImageView imageView = (ImageView) view;
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            File f = AndroidUtilities.getSharingDirectory();
            f.mkdirs();
            f = new File(f, "qr.jpg");
            FileOutputStream outputStream = new FileOutputStream(f.getAbsolutePath());
            bitmapDrawable.getBitmap().compress(Bitmap.CompressFormat.JPEG, 87, outputStream);
            outputStream.close();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            if (!TextUtils.isEmpty(text)) {
                intent.putExtra(Intent.EXTRA_TEXT, text);
            }
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", f));
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignore) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                }
            } else {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            }
            activity.startActivityForResult(Intent.createChooser(intent, LocaleController.getString("WalletShareQr", R.string.WalletShareQr)), 500);
        } catch (Exception e) {
            FileLog.e(e);
        }
    }



}
