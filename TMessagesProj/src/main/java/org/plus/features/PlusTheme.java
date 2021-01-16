package org.plus.features;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;

public class PlusTheme {
    //plus
    public static Drawable getDrawable(int res) {
        return ApplicationLoader.applicationContext.getResources().getDrawable(res);
    }

    public static Drawable getLineDrawable(String title){
        if(title == null || title.isEmpty()){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_folder);
        }
        if(title.equals("All")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_grid);
        }else if(title.equals("Personal")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_user);
        }else if(title.equals("Unread")) {
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_bubbles);
        }else if(title.equals("Groups")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_users);
        }else if(title.equals("Channels")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_channel);
        }else if(title.equals("Bots")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.menu_bots);
        }else if(title.equals("Admin")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_admin);
        }else if(title.equals("SuperGroups")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_folder);
        }else{
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_folder);
        }
    }

    public static Drawable getFillDrawable(String title){
        if(title == null || title.isEmpty()){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_folder_filled);
        }
        if(title.equals("All")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_grid_filled);
        }else if(title.equals("Personal")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_user_filled);
        }else if(title.equals("Unread")) {
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_bubbles_filled);
        }else if(title.equals("Groups")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_users_filled);
        }else if(title.equals("Channels")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_channel_filled);
        }else if(title.equals("Bots")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.menu_bots);
        }else if(title.equals("Admin")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_admin_filled);
        }else if(title.equals("Favorite")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.stickers_favorites);
        }else if(title.equals("SuperGroups")){
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_folder_filled);
        }else{
            return ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.ime_filter_icon_folder_filled);
        }
    }
    
    public static final int[] filterIcons_line = {
            R.drawable.ime_filter_icon_flower,
            R.drawable.ime_filter_icon_bubbles,
            R.drawable.ime_filter_icon_albums,
            R.drawable.ime_filter_icon_admin,
            R.drawable.ime_filter_icon_folder,
            R.drawable.ime_filter_icon_bot,
            R.drawable.ime_filter_icon_channel,
            R.drawable.ime_filter_icon_mask,
            R.drawable.ime_filter_icon_bubble,
            R.drawable.ime_filter_icon_headset,
            R.drawable.ime_filter_icon_crown,
            R.drawable.ime_filter_icon_game,
            R.drawable.ime_filter_icon_computer,
            R.drawable.ime_filter_icon_heart,
            R.drawable.ime_filter_icon_lock,
            R.drawable.ime_filter_icon_gallery,
            R.drawable.ime_filter_icon_ball,
            R.drawable.ime_filter_icon_star,
            R.drawable.ime_filter_icon_suitcase,
            R.drawable.ime_filter_icon_owner,
            R.drawable.ime_filter_icon_grid_2,
            R.drawable.ime_filter_icon_grid,
            R.drawable.ime_filter_icon_bubble_point,
            R.drawable.ime_filter_icon_bell,
            R.drawable.ime_filter_icon_home,
            R.drawable.ime_filter_icon_micro,
            R.drawable.ime_filter_icon_camera,
            R.drawable.ime_filter_icon_call,
            R.drawable.ime_filter_icon_gear,
            R.drawable.ime_filter_icon_cloud,
            R.drawable.ime_filter_icon_eye,
            R.drawable.ime_filter_icon_chart,
            R.drawable.ime_filter_icon_chat_admin,
            R.drawable.ime_filter_icon_user,
            R.drawable.ime_filter_icon_globe,
            R.drawable.ime_filter_icon_cat,
            R.drawable.ime_filter_icon_users,
            R.drawable.ime_filter_icon_wineglass


    };

    public static final int[] filterIcons_filled = {

            R.drawable.ime_filter_icon_flower_filled,
            R.drawable.ime_filter_icon_bubbles_filled,
            R.drawable.ime_filter_icon_albums_filled,
            R.drawable.ime_filter_icon_admin_filled,
            R.drawable.ime_filter_icon_folder_filled,
            R.drawable.ime_filter_icon_bot_filled,
            R.drawable.ime_filter_icon_channel_filled,
            R.drawable.ime_filter_icon_mask_filled,
            R.drawable.ime_filter_icon_bubble_filled,
            R.drawable.ime_filter_icon_headset_filled,
            R.drawable.ime_filter_icon_crown_filled,
            R.drawable.ime_filter_icon_game_filled,
            R.drawable.ime_filter_icon_computer_filled,
            R.drawable.ime_filter_icon_heart_filled,
            R.drawable.ime_filter_icon_lock_filled,
            R.drawable.ime_filter_icon_gallery_filled,
            R.drawable.ime_filter_icon_ball_filled,
            R.drawable.ime_filter_icon_star_filled,
            R.drawable.ime_filter_icon_suitcase_filled,
            R.drawable.ime_filter_icon_owner_filled,
            R.drawable.ime_filter_icon_grid_2_filled,
            R.drawable.ime_filter_icon_grid_filled,
            R.drawable.ime_filter_icon_bubble_point_filled,
            R.drawable.ime_filter_icon_bell_filled,
            R.drawable.ime_filter_icon_home_filled,
            R.drawable.ime_filter_icon_micro_filled,
            R.drawable.ime_filter_icon_camera_filled,
            R.drawable.ime_filter_icon_call_filled,
            R.drawable.ime_filter_icon_gear_filled,
            R.drawable.ime_filter_icon_cloud_filled,
            R.drawable.ime_filter_icon_eye_filled,
            R.drawable.ime_filter_icon_chart_filled,
            R.drawable.ime_filter_icon_chat_admin_filled,
            R.drawable.ime_filter_icon_user_filled,
            R.drawable.ime_filter_icon_globe_filled,
            R.drawable.ime_filter_icon_cat_filled,
            R.drawable.ime_filter_icon_users_filled,
            R.drawable.ime_filter_icon_wineglass
    };


    //
}
