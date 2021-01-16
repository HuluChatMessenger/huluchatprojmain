
package org.plus.wallet.cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.telegram.messenger.R;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

public class WalletSyncCell extends FrameLayout {

    public WalletSyncCell(Context context) {
        super(context);

        RLottieImageView imageView = new RLottieImageView(context);
        imageView.setAutoRepeat(true);
        imageView.setAnimation(R.raw.wallet_sync, 112, 112);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.playAnimation();
        addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
    }
}
