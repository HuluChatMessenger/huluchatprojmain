package org.plus.apps.business.ui.components;

import android.content.Context;
import android.widget.FrameLayout;

import org.telegram.messenger.ImageReceiver;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

@SuppressWarnings("FieldCanBeLocal")
public class ProductPagerInnerCell extends FrameLayout {

    private BackupImageView imageView;


    public ProductPagerInnerCell(Context context, String images) {
        super(context);

        imageView = new BackupImageView(context);
        imageView.setImage(images,"500_500",null);
        imageView.getImageReceiver().setDelegate(new ImageReceiver.ImageReceiverDelegate() {
            @Override
            public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb, boolean memCache) {

            }
        });
        addView(imageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));
    }


}
