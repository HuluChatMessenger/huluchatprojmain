package org.plus.experment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class AvatarSerializer {

    public static String getStringFromBitmap(Bitmap bitmap,int size){
        Bitmap circleBitmap = createCircleBitmap(bitmap);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(circleBitmap,size,size,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG,70,byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap getBitmapFromString(String encodedString){
       byte[] bytes =  Base64.decode(encodedString,Base64.DEFAULT);
       return BitmapFactory.decodeByteArray(bytes,0,bytes.length);

    }


    public static Bitmap createCircleBitmap(Bitmap b){
        Bitmap bitmap;
        if(b.getHeight() > b.getWidth()){
            bitmap = Bitmap.createBitmap(b.getWidth(),b.getWidth(),Bitmap.Config.ARGB_8888);
        }else{
            bitmap = Bitmap.createBitmap(b.getHeight(),b.getHeight(),Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        int r;

        if(b.getHeight() > b.getWidth()){
            r = b.getWidth()/2;
        }else{
            r = b.getHeight()/2;
        }
        int color = -0xbdbdbe;
        Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0,0,0,0);
        canvas.drawCircle(r,r,r,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        return bitmap;
    }

    public static Bitmap createCircleBitmap(Bitmap b,int color,float stroke_width){
        Bitmap bitmap;
        if(b.getHeight() > b.getWidth()){
            bitmap = Bitmap.createBitmap(b.getWidth(),b.getWidth(),Bitmap.Config.ARGB_8888);
        }else{
            bitmap = Bitmap.createBitmap(b.getHeight(),b.getHeight(),Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        int r;

        if(b.getHeight() > b.getWidth()){
            r = b.getWidth()/2;
        }else{
            r = b.getHeight()/2;
        }
        Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(stroke_width);
        paint.setColor(color);
        canvas.drawARGB(0,0,0,0);
        canvas.drawCircle(r,r,r,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);
        return bitmap;
    }

    //    private void createUiTableView(Context context,ShopDataSerializer.FieldSet fieldSet){
//
//        createUiTableView(context,fieldSet);
//
////        shadowSectionCell = new ShadowSectionCell(context);
////        shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////        linearLayout.addView(shadowSectionCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
////
//        for (int a = 0; a < fieldSet.fields.size(); a++) {
//            ShopDataSerializer.Field field = fieldSet.fields.get(a);
//            if (field == null || TextUtils.isEmpty(field.label)) {
//                continue;
//            }
//            if (TextUtils.isEmpty(field.key)) {
//                continue;
//            }
//            if (!productFull.containsKey(field.key)) {
//                continue;
//            }
//            Object value = productFull.get(field.key);
//            if (value == null) {
//                continue;
//            }
//            TableCell tableCell = new TableCell(context);
//
//            tableCell.setData(null,value.toString(),field.key);
//            linearLayout.addView(tableCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT,8,8,8,8));
//        }
//
//
//
////
////
////        TextView detailCell = new TextView(context);
////        detailCell.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
////        detailCell.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
////        detailCell.setTextSize(18);
////        detailCell.setPadding(AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8),AndroidUtilities.dp(8));
////        linearLayout.addView(detailCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT));
//
//
////        TermsOfServiceView.addBulletsToText(builder, '-', AndroidUtilities.dp(10f), 0xff50a8eb, AndroidUtilities.dp(4f));
////        detailCell.setText(builder);
//
////        shadowSectionCell = new ShadowSectionCell(context);
////        shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
////        linearLayout.addView(shadowSectionCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
//
//    }
}
