package org.plus.apps.wallet.component;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ui.components.OfferBottomSheet;
import org.telegram.messenger.NotificationCenter;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.Components.RecyclerListView;

public class WalletSmsInputAlert extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private boolean inLayout;
    private ListAdapter listAdapter;
    private NestedScrollView scrollView;
    private LinearLayout linearLayout;
    private ActionBar actionBar;
    private View actionBarShadow;
    private View shadow;

    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private BaseFragment parentFragment;

    private int scrollOffsetY;
    private AnimatorSet actionBarAnimation;
    private AnimatorSet shadowAnimation;

    private String commentString = "";
    private double amountValue ;

    private int amountHeaderRow;
    private int amountRow;
    private int commentRow;
    private int invoiceInfoRow;
    private int rowCount;
    private int titleRow;


    private void updateRows() {

        titleRow = -1;
        invoiceInfoRow = -1;
        amountHeaderRow = -1;
        amountRow = -1;
        commentRow = -1;


        titleRow = rowCount++;
        invoiceInfoRow = rowCount++;
        amountHeaderRow = rowCount++;
        amountRow = rowCount++;
        commentRow = rowCount++;
    }

    public WalletSmsInputAlert(Context context, boolean needFocus) {
        super(context, needFocus);

        init(context);
    }


    private void init(Context context){

    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {

    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ListAdapter(Context mContext) {

            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return false;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
