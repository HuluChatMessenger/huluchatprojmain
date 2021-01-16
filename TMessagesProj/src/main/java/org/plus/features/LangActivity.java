package org.plus.features;/*
 * This is the source code of Telegram for Android v. 6.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2020.
 */


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.LanguageSelectActivity;
import org.telegram.ui.PhotoViewer;

import java.util.ArrayList;

public class LangActivity extends Activity implements ActionBarLayout.ActionBarLayoutDelegate {

    private boolean finished;
    private ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private ActionBarLayout actionBarLayout;
    protected DrawerLayoutContainer drawerLayoutContainer;


    private long dialogId;
    private int currentAccount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);
        if (SharedConfig.passcodeHash.length() > 0 && !SharedConfig.allowScreenCapture) {
            try {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        super.onCreate(savedInstanceState);

        AndroidUtilities.fillStatusBarHeight(this);
        Theme.createDialogsResources(this);
        Theme.createChatResources(this, false);

        actionBarLayout = new ActionBarLayout(this);
        actionBarLayout.setInBubbleMode(false);
        actionBarLayout.setRemoveActionBarExtraHeight(false);

        drawerLayoutContainer = new DrawerLayoutContainer(this);
        drawerLayoutContainer.setAllowOpenDrawer(false, false);
        setContentView(drawerLayoutContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout launchLayout = new RelativeLayout(this);
        drawerLayoutContainer.addView(launchLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        launchLayout.addView(actionBarLayout, LayoutHelper.createRelative(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
        actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        actionBarLayout.init(mainFragmentsStack);
        actionBarLayout.setDelegate(this);


        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, this);

        actionBarLayout.removeAllFragments();

        handleIntent(getIntent(), false, savedInstanceState != null, false, UserConfig.selectedAccount, 0);
    }


    private boolean handleIntent(final Intent intent, final boolean isNew, final boolean restore, final boolean fromPassword, final int intentAccount, int state) {
        currentAccount = intent.getIntExtra("currentAccount", UserConfig.selectedAccount);
        LanguageSelectActivity chatActivity = new LanguageSelectActivity();
        chatActivity.setDelegate(new LanguageSelectActivity.Delegate() {
            @Override
            public void onLangSelected(LocaleController.LocaleInfo localeInfo) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("data",localeInfo);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats, dialogId);
        actionBarLayout.removeAllFragments();
        actionBarLayout.addFragmentToStack(chatActivity);
        actionBarLayout.showLastFragment();
        return true;
    }

    @Override
    public boolean onPreIme() {
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false, UserConfig.selectedAccount, 0);
    }

    private void onFinish() {
        if (finished) {
            return;
        }
        finished = true;
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        actionBarLayout.onPause();
        ApplicationLoader.externalInterfacePaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentAccount != -1) {
            AccountInstance.getInstance(currentAccount).getNotificationsController().setOpenedInBubble(dialogId, false);
            AccountInstance.getInstance(currentAccount).getConnectionsManager().setAppPaused(false, false);
        }
        onFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarLayout.onResume();
        ApplicationLoader.externalInterfacePaused = false;
    }


    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        AndroidUtilities.checkDisplaySize(this, newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (drawerLayoutContainer.isDrawerOpened()) {
            drawerLayoutContainer.closeDrawer(false);
        } else {
            actionBarLayout.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        actionBarLayout.onLowMemory();
    }

    @Override
    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        return true;
    }

    @Override
    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        return true;
    }

    @Override
    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (layout.fragmentsStack.size() <= 1) {
            onFinish();
            finish();
            return false;
        }
        return true;
    }

    @Override
    public void onRebuildAllFragments(ActionBarLayout layout, boolean last) {

    }
}
