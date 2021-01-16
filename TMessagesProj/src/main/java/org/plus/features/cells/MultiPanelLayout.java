package org.plus.features.cells;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiPanelLayout extends FrameLayout {


    //personal
    public static int personal_action_notification = 1;
    public static int personal_action_shared_content = 2;
    public static int personal_action_cache = 3;
    public static int personal_action_search = 4;

    //groups
    public static int groups_action_notification = 1;
    public static int groups_action_members= 2;
    public static int groups_action_shared_content = 3;
    public static int groups_action_recent_action= 4;
    public static int groups_action_cache = 5;
    public static int groups_action_search = 6;

    //channels
    public static int channels_action_subscribers= 1;
    public static int channels_action_shared_content= 2;
    public static int channels_action_recent_action = 3;
    public static int channels_action_cache = 4;
    public static int channels_action_search = 5;

    //bots
    public static int  bots_action_subscribers= 1;
    public static int  bots_action_shared_content= 2;
    public static int  bots_action_recent_action = 3;
    public static int  bots_action_cache = 4;
    public static int  bots_action_search = 5;


    //types
    public static final int type_personal = 1;
    public static final int type_group = 2;
    public static final int type_channel = 3;
    public static final int type_bots = 4;



    public interface MultiPanelDelegate{

        void onItemClicked();
    }

    private MultiPanelDelegate multiPanelDelegate;
    private TabLayout tabLayout;


    public MultiPanelLayout(Context context) {
        super(context);

        tabLayout = new TabLayout(context);
        tabLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        tabLayout.setSelectedTabIndicatorColor(0);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        addView(tabLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(multiPanelDelegate != null){
                    multiPanelDelegate.onItemClicked();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(multiPanelDelegate != null){
                    multiPanelDelegate.onItemClicked();
                }
            }
        });

    }

    public void loadPanels(){
        SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("multiPanelPref", (Context.MODE_PRIVATE));
        String data;
        if((data = sharedPreferences.getString("multi_panel","")).isEmpty()){
             loadDefaultAction();
        }else{

        }

    }

    public void loadDefaultAction(){

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(48));
    }

    public void setMultiPanelDelegate(MultiPanelDelegate multiPanelDelegate) {
        this.multiPanelDelegate = multiPanelDelegate;
    }
}
