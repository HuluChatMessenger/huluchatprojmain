package org.plus.apps.business.ui.cells;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.checkerframework.checker.index.qual.PolyUpperBound;
import org.plus.apps.business.ui.components.ProductImageLayout;
import org.plus.experment.DataLoader;
import org.plus.net.CountingRequestBody;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class ProductImageCell extends FrameLayout implements CountingRequestBody.FileUploadTaskDelegate {

    private BackupImageView imageView;
    private ImageView deleteImageView;
    private int currentAccount = UserConfig.selectedAccount;


    public ProductImageCell(Context context) {
        super(context);

        imageView = new BackupImageView(context) ;
        imageView.setRoundRadius(AndroidUtilities.dp(16));
        addView(imageView, LayoutHelper.createFrame(100,100));

        deleteImageView = new ImageView(context);
        deleteImageView.setVisibility(GONE);
        deleteImageView.setTag(null);
        deleteImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_ab_delete);
        drawable.setColorFilter(Theme.getShareColorFilter(Theme.getColor(Theme.key_dialogTextBlack),false));
        deleteImageView.setImageDrawable(drawable);
        addView(deleteImageView, LayoutHelper.createFrame(24,24, Gravity.TOP|Gravity.RIGHT,8,8,8,8));

    }


    public void showButton(){
        deleteImageView.setTag(1);
        deleteImageView.setVisibility(VISIBLE);
    }



    public void hideButton(){
        deleteImageView.setTag(null);
        deleteImageView.setVisibility(GONE);
    }


    public void setDeleteListner(OnClickListener listner){
        deleteImageView.setOnClickListener(listner);
    }


    public void setImage(ProductImageLayout.ImageInput input){
        if(input == null){
            return;
        }
        Drawable thumb = getResources().getDrawable(R.drawable.menu_camera2);
        thumb.setColorFilter(Theme.getShareColorFilter(Theme.getColor(Theme.key_dialogTextBlack),false));

        if(input.productImage != null){
            imageView.setSize(AndroidUtilities.dp(100),AndroidUtilities.dp(100));
            imageView.setImage(input.productImage.photo,"100_100",null);
            return;
        }

        if (input.smallSize != null) {
            imageView.setSize(AndroidUtilities.dp(100),AndroidUtilities.dp(100));
            imageView.setImage(FileLoader.getPathToAttach(input.smallSize, true).getAbsolutePath(),"100_100",null);
        }else{
            imageView.setSize(AndroidUtilities.dp(28),AndroidUtilities.dp(28));
            imageView.setImage(null,null,thumb);
        }
    }

    public BackupImageView getImageView() {
        return imageView;
    }




    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    @Override
    public void didFinishUploadingFile(long id) {

    }

    @Override
    public void didFailedUploadingFile() {

    }

    @Override
    public void didChangedUploadProgress(long uploadedSize, long totalSize) {

    }
}
