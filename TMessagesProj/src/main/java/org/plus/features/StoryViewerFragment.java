package org.plus.features;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.plus.experment.stories.StoriesProgressView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.concurrent.CountDownLatch;

public class StoryViewerFragment extends BaseFragment implements StoriesProgressView.StoriesListener{

    private StoriesProgressView storiesProgressView;


    private int count;
    private int userId;

    private TLRPC.User currentUser;

    public StoryViewerFragment(Bundle args){
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        userId = arguments.getInt("user_id");
        count = arguments.getInt("count");

        if (userId != 0) {
            currentUser = getMessagesController().getUser(userId);
            if (currentUser == null) {
                final MessagesStorage messagesStorage = getMessagesStorage();
                final CountDownLatch countDownLatch = new CountDownLatch(1);
                messagesStorage.getStorageQueue().postRunnable(() -> {
                    currentUser = messagesStorage.getUser(userId);
                    countDownLatch.countDown();
                });
                try {
                    countDownLatch.await();
                } catch (Exception e) {
                    FileLog.e(e);
                }
                if (currentUser != null) {
                    getMessagesController().putUser(currentUser, true);
                } else {
                    return false;
                }
            }
        }else{
            return false;
        }
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {

        actionBar.setAddToContainer(false);

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;


        BackupImageView backupImageView = new BackupImageView(context);
        backupImageView.setAspectFit(true);
        AvatarDrawable avatarDrawable = new AvatarDrawable(currentUser);
        avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
        backupImageView.setImage(ImageLocation.getForUser(currentUser, true), null, avatarDrawable, currentUser);
        frameLayout.addView(backupImageView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT, Gravity.CENTER,0,0,0,0));

        storiesProgressView = new StoriesProgressView(context,2);
        storiesProgressView.setStoryDuration(3000L);
        storiesProgressView.setStoriesListener(this);
        int top = actionBar.getOccupyStatusBar()? AndroidUtilities.statusBarHeight:0;
        frameLayout.addView(storiesProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,3, Gravity.LEFT|Gravity.TOP,8,8,top + 8,8));


        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        storiesProgressView.startStories();

    }

    @Override
    public void onNext() {

    }

    @Override
    public void onPrev() {

    }



    @Override
    public void onComplete() {
        finishFragment();
    }
}
