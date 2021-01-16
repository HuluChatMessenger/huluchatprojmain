package org.plus.apps.payment;

import android.app.Activity;

import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.wallet.WalletActionSheet;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;

public class PaymentUtils {

    public static WalletActionSheet createSendAlert(BaseFragment activity, TLRPC.User user){
        WalletActionSheet walletActionSheet = new WalletActionSheet(activity, WalletActionSheet.TYPE_SEND, user,null,false);
        walletActionSheet.setDelegate(new WalletActionSheet.WalletActionSheetDelegate() {
            @Override
            public void onSendToUser(TLRPC.User address) {

            }
        });
        walletActionSheet.setOnDismissListener(dialog -> {
               dialog.cancel();

        });
       return walletActionSheet;

    }

    public static WalletActionSheet createAirTimeAlert(BaseFragment activity, TLRPC.User user){
        WalletActionSheet walletActionSheet = new WalletActionSheet(activity, WalletActionSheet.TYPE_AIT_time, user,null,false);
        walletActionSheet.setDelegate(new WalletActionSheet.WalletActionSheetDelegate() {
            @Override
            public void onTopUp(double amount, TLRPC.User user) {

            }
        });
        walletActionSheet.setOnDismissListener(dialog -> {
            dialog.cancel();

        });
        return walletActionSheet;

    }

}
