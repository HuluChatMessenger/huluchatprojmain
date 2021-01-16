package org.plus.apps.business.ui.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.plus.apps.business.data.ShopDataSerializer;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class ShopInfoInnerCell extends FrameLayout {

        private BackupImageView imageView;
        private TextView titleTextView;
        private TextView messageTextView;

        public ShopInfoInnerCell(Context context,boolean occupyStatusBar) {
            super(context);

            final int top = occupyStatusBar? (int) (AndroidUtilities.statusBarHeight / AndroidUtilities.density) :0;

            imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(74/2));
            addView(imageView, LayoutHelper.createFrame(74, 74, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, top + 17, 0, 0));

            titleTextView = new TextView(context);
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setSingleLine();
            titleTextView.setMaxLines(1);
            titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/Roboto-Black.ttf"));
            titleTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
            titleTextView.setGravity(Gravity.CENTER);
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, top + 110, 52, 27));

            messageTextView = new TextView(context);
            messageTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            messageTextView.setGravity(Gravity.CENTER);
            addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 40, top + 151, 40, 27));
        }

    public TextView getTitleTextView() {
        return titleTextView;
    }


    public void setSelf(int account){
        TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
        titleTextView.setText(UserObject.getFirstName(user,false));
        AvatarDrawable avatarDrawable = new AvatarDrawable(user);
        imageView.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, user);
        messageTextView.setText(LocaleController.formatUserStatus(account,user));
    }

    public void setChat(TLRPC.Chat chat){
            if(chat == null){
                return;
            }
            titleTextView.setText(chat.title);
            AvatarDrawable avatarDrawable = new AvatarDrawable(chat);
            imageView.setImage(ImageLocation.getForChat(chat, false), "100_100", avatarDrawable, chat);
            messageTextView.setText(chat.participants_count + " Followers");
        }


     public void setShop(ShopDataSerializer.Shop shop){
          if(shop == null){
              return;
          }
         AvatarDrawable avatarDrawable = new AvatarDrawable();
          avatarDrawable.setAvatarType(AvatarDrawable.AVATAR_TYPE_FILTER_CHANNELS);
         if(shop.profilePic != null){
             imageView.setImage(shop.profilePic.photo,null,avatarDrawable);
         }else{
             imageView.setImage(null,null,avatarDrawable);

         }
         if(!TextUtils.isEmpty(shop.title)){
             titleTextView.setText(shop.title);
         }
         messageTextView.setVisibility(GONE);
     }

    }
