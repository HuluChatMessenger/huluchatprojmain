package org.plus.features;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.SharedAudioCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FillLastLinearLayoutManager;
import org.telegram.ui.Components.FragmentContextView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.io.File;
import java.util.ArrayList;

public class MusicFragment extends BaseFragment implements NotificationCenter.NotificationCenterDelegate{

    private ListAdapter listAdapter;
    private SearchAdapter searchAdapter;
    private LinearLayoutManager layoutManager;
    private EmptyTextProgressView progressView;
    private LinearLayout emptyView;
    private ImageView emptyImageView;
    private TextView emptyTitleTextView;
    private TextView emptySubtitleTextView;
    private RecyclerListView listView;
    private View currentEmptyView;

    private boolean loadingAudio;


    private FragmentContextView fragmentContextView;

    private MessageObject playingAudio;


    private ArrayList<MediaController.AudioEntry> audioEntries = new ArrayList<>();
    private ArrayList<MessageObject> playlist = new ArrayList<>();

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.messagePlayingDidReset || id == NotificationCenter.messagePlayingDidStart || id == NotificationCenter.messagePlayingPlayStateChanged) {
            if (id == NotificationCenter.messagePlayingDidReset || id == NotificationCenter.messagePlayingPlayStateChanged) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View view = listView.getChildAt(a);
                    if (view instanceof SharedAudioCell) {
                        SharedAudioCell cell = (SharedAudioCell) view;
                        MessageObject messageObject = cell.getMessage();
                        if (messageObject != null) {
                            cell.updateButtonState(false, true);
                        }
                    }
                }
            } else if (id == NotificationCenter.messagePlayingDidStart) {
                MessageObject messageObject = (MessageObject) args[0];
                if (messageObject.eventId != 0) {
                    return;
                }
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View view = listView.getChildAt(a);
                    if (view instanceof SharedAudioCell) {
                        SharedAudioCell cell = (SharedAudioCell) view;
                        MessageObject messageObject1 = cell.getMessage();
                        if (messageObject1 != null) {
                            cell.updateButtonState(false, true);
                        }
                    }
                }
            }
        }

    }

    @Override
    public boolean onFragmentCreate() {
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        loadAudio();
        return super.onFragmentCreate();
    }



    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                super.onItemClick(id);
                if(id == -1){
                    finishFragment();
                }
            }
        });
        actionBar.setTitle("Music");

//        ActionBarMenu menu =  actionBar.createMenu();
//        menu.addItem(1,R.drawable.ic_ab_search);


        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

//        searchField = new SearchField(context) ;
//        searchField.setHint(LocaleController.getString("SearchMusic", R.string.SearchMusic));
//        frameLayout.addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));

        progressView = new EmptyTextProgressView(context);
        progressView.showProgress();
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setVisibility(View.GONE);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyView.setOnTouchListener((v, event) -> true);

        emptyImageView = new ImageView(context);
        emptyImageView.setImageResource(R.drawable.music_empty);
        emptyImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogEmptyImage), PorterDuff.Mode.MULTIPLY));
        emptyView.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        emptyTitleTextView = new TextView(context);
        emptyTitleTextView.setTextColor(Theme.getColor(Theme.key_dialogEmptyText));
        emptyTitleTextView.setGravity(Gravity.CENTER);
        emptyTitleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        emptyTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        emptyTitleTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), 0);
        emptyView.addView(emptyTitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 11, 0, 0));

        emptySubtitleTextView = new TextView(context);
        emptySubtitleTextView.setTextColor(Theme.getColor(Theme.key_dialogEmptyText));
        emptySubtitleTextView.setGravity(Gravity.CENTER);
        emptySubtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        emptySubtitleTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), 0);
        emptyView.addView(emptySubtitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 6, 0, 0));

        listView = new RecyclerListView(context);
        listView.setClipToPadding(false);
        listView.setLayoutManager(layoutManager = new FillLastLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false, AndroidUtilities.dp(9), listView) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
                    @Override
                    public int calculateDyToMakeVisible(View view, int snapPreference) {
                        int dy = super.calculateDyToMakeVisible(view, snapPreference);
                        dy -= (listView.getPaddingTop() - AndroidUtilities.dp(7));
                        return dy;
                    }

                    @Override
                    protected int calculateTimeForDeceleration(int dx) {
                        return super.calculateTimeForDeceleration(dx) * 2;
                    }
                };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        });
        listView.setHorizontalScrollBarEnabled(false);
        listView.setVerticalScrollBarEnabled(false);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));
        listView.setAdapter(listAdapter = new ListAdapter(context));
        listView.setGlowColor(Theme.getColor(Theme.key_dialogScrollGlow));
        listView.setOnItemClickListener((view, position) -> onItemClick(view));
        listView.setOnItemLongClickListener((view, position) -> {
            onItemClick(view);
            return true;
        });
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //updateLayout(ChatAttachAlertAudioLayout.this, true, dy);
                updateEmptyViewPosition();
            }
        });

        searchAdapter = new SearchAdapter(context);

        updateEmptyView();

        fragmentContextView = new FragmentContextView(context, this, false);
        fragmentContextView.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        frameLayout.addView(fragmentContextView);


        return fragmentView;
    }

    private void loadAudio() {
        loadingAudio = true;
        Utilities.globalQueue.postRunnable(() -> {
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM
            };

            final ArrayList<MediaController.AudioEntry> newAudioEntries = new ArrayList<>();

            try (Cursor cursor = ApplicationLoader.applicationContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, MediaStore.Audio.Media.TITLE)) {
                int id = -2000000000;
                while (cursor.moveToNext()) {
                    MediaController.AudioEntry audioEntry = new MediaController.AudioEntry();
                    audioEntry.id = cursor.getInt(0);
                    audioEntry.author = cursor.getString(1);
                    audioEntry.title = cursor.getString(2);
                    audioEntry.path = cursor.getString(3);
                    audioEntry.duration = (int) (cursor.getLong(4) / 1000);
                    audioEntry.genre = cursor.getString(5);

                    File file = new File(audioEntry.path);

                    TLRPC.TL_message message = new TLRPC.TL_message();
                    message.out = true;
                    message.id = id;
                    message.peer_id = new TLRPC.TL_peerUser();
                    message.from_id = new TLRPC.TL_peerUser();
                    message.peer_id.user_id = message.from_id.user_id = UserConfig.getInstance(currentAccount).getClientUserId();
                    message.date = (int) (System.currentTimeMillis() / 1000);
                    message.message = "";
                    message.attachPath = audioEntry.path;
                    message.media = new TLRPC.TL_messageMediaDocument();
                    message.media.flags |= 3;
                    message.media.document = new TLRPC.TL_document();
                    message.flags |= TLRPC.MESSAGE_FLAG_HAS_MEDIA | TLRPC.MESSAGE_FLAG_HAS_FROM_ID;

                    String ext = FileLoader.getFileExtension(file);

                    message.media.document.id = 0;
                    message.media.document.access_hash = 0;
                    message.media.document.file_reference = new byte[0];
                    message.media.document.date = message.date;
                    message.media.document.mime_type = "audio/" + (ext.length() > 0 ? ext : "mp3");
                    message.media.document.size = (int) file.length();
                    message.media.document.dc_id = 0;

                    TLRPC.TL_documentAttributeAudio attributeAudio = new TLRPC.TL_documentAttributeAudio();
                    attributeAudio.duration = audioEntry.duration;
                    attributeAudio.title = audioEntry.title;
                    attributeAudio.performer = audioEntry.author;
                    attributeAudio.flags |= 3;
                    message.media.document.attributes.add(attributeAudio);

                    TLRPC.TL_documentAttributeFilename fileName = new TLRPC.TL_documentAttributeFilename();
                    fileName.file_name = file.getName();
                    message.media.document.attributes.add(fileName);


                    audioEntry.messageObject = new MessageObject(currentAccount, message, false, true);

                    newAudioEntries.add(audioEntry);
                    id--;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            AndroidUtilities.runOnUIThread(() -> {
                audioEntries.clear();
                playlist.clear();
                loadingAudio = false;
                audioEntries = newAudioEntries;
                for (int i = 0; i <audioEntries.size() ; i++) {
                    playlist.add(audioEntries.get(i).messageObject);

                }
                listAdapter.notifyDataSetChanged();
            });
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if(listAdapter != null){
            listAdapter.notifyDataSetChanged();
        }
    }

    private void onItemClick(View view) {
        if ((view instanceof SharedAudioCell)) {
            SharedAudioCell audioCell = (SharedAudioCell) view;
            playingAudio = audioCell.getMessage();
//            ArrayList<MessageObject> arrayList = new ArrayList<>();
//            arrayList.add(audioCell.getMessage());
            MediaController.getInstance().setPlaylist(playlist, audioCell.getMessage(), 0);
        }

    }


    @Override
    public void onFragmentDestroy() {
//        if (playingAudio != null && MediaController.getInstance().isPlayingMessage(playingAudio)) {
//            MediaController.getInstance().cleanupPlayer(true, true);
//        }
//        playingAudio = null;

        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);

        super.onFragmentDestroy();
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            if (audioEntries.isEmpty()) {
                return 1;
            }
            return audioEntries.size() + (audioEntries.isEmpty() ? 0 : 2);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    SharedAudioCell sharedAudioCell = new SharedAudioCell(mContext) {
                        @Override
                        public boolean needPlayMessage(MessageObject messageObject) {
                            playingAudio = messageObject;
//                            ArrayList<MessageObject> arrayList = new ArrayList<>();
//                            arrayList.add(messageObject);
                            return MediaController.getInstance().setPlaylist(playlist, messageObject, 0);
                        }
                    };
                    sharedAudioCell.setCheckForButtonPress(true);
                    view = sharedAudioCell;
                    break;
                case 1:
                    view = new View(mContext);
                    view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(1)));
                    break;
                default:
                    view = new View(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                position--;
                MediaController.AudioEntry audioEntry = audioEntries.get(position);

                SharedAudioCell audioCell = (SharedAudioCell) holder.itemView;
                audioCell.setTag(audioEntry);
                audioCell.setMessageObject(audioEntry.messageObject, position != audioEntries.size() - 1);
            }
        }

        @Override
        public int getItemViewType(int i) {
            if (i == getItemCount() - 1) {
                return 2;
            }
            if (i == 0) {
                return 1;
            }
            return 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            updateEmptyView();
        }

    }

    private void updateEmptyView() {
        if (loadingAudio) {
            currentEmptyView = progressView;
            emptyView.setVisibility(View.GONE);
        } else {
            if (listView.getAdapter() == searchAdapter) {
                emptyTitleTextView.setText(LocaleController.getString("NoAudioFound", R.string.NoAudioFound));
            } else {
                emptyTitleTextView.setText(LocaleController.getString("NoAudioFiles", R.string.NoAudioFiles));
                emptySubtitleTextView.setText(LocaleController.getString("NoAudioFilesInfo", R.string.NoAudioFilesInfo));
            }
            currentEmptyView = emptyView;
            progressView.setVisibility(View.GONE);
        }

        boolean visible;
        if (listView.getAdapter() == searchAdapter) {
            visible = searchAdapter.searchResult.isEmpty();
        } else {
            visible = audioEntries.isEmpty();
        }
        currentEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        updateEmptyViewPosition();
    }

    private void updateEmptyViewPosition() {
        if (currentEmptyView.getVisibility() != View.VISIBLE) {
            return;
        }
    }



    public class SearchAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;
        private ArrayList<MediaController.AudioEntry> searchResult = new ArrayList<>();
        private Runnable searchRunnable;
        private int lastSearchId;
        private int reqId = 0;
        private int lastReqId;

        public SearchAdapter(Context context) {
            mContext = context;
        }

        public void search(final String query) {
            if (searchRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(searchRunnable);
                searchRunnable = null;
            }
            if (TextUtils.isEmpty(query)) {
                if (!searchResult.isEmpty()) {
                    searchResult.clear();
                }
                if (listView.getAdapter() != listAdapter) {
                    listView.setAdapter(listAdapter);
                }
                notifyDataSetChanged();
            } else {
                int searchId = ++lastSearchId;
                AndroidUtilities.runOnUIThread(searchRunnable = () -> {
                    final ArrayList<MediaController.AudioEntry> copy = new ArrayList<>(audioEntries);
                    Utilities.searchQueue.postRunnable(() -> {
                        String search1 = query.trim().toLowerCase();
                        if (search1.length() == 0) {
                            updateSearchResults(new ArrayList<>(), query, lastSearchId);
                            return;
                        }
                        String search2 = LocaleController.getInstance().getTranslitString(search1);
                        if (search1.equals(search2) || search2.length() == 0) {
                            search2 = null;
                        }
                        String[] search = new String[1 + (search2 != null ? 1 : 0)];
                        search[0] = search1;
                        if (search2 != null) {
                            search[1] = search2;
                        }

                        ArrayList<MediaController.AudioEntry> resultArray = new ArrayList<>();

                        for (int a = 0; a < copy.size(); a++) {
                            MediaController.AudioEntry entry = copy.get(a);
                            for (int b = 0; b < search.length; b++) {
                                String q = search[b];

                                boolean ok = false;
                                if (entry.author != null) {
                                    ok = entry.author.toLowerCase().contains(q);
                                }
                                if (!ok && entry.title != null) {
                                    ok = entry.title.toLowerCase().contains(q);
                                }
                                if (ok) {
                                    resultArray.add(entry);
                                    break;
                                }
                            }
                        }

                        updateSearchResults(resultArray, query, searchId);
                    });
                }, 300);
            }
        }

        private void updateSearchResults(final ArrayList<MediaController.AudioEntry> result, String query, final int searchId) {
            AndroidUtilities.runOnUIThread(() -> {
                if (searchId != lastSearchId) {
                    return;
                }
                if (searchId != -1 && listView.getAdapter() != searchAdapter) {
                    listView.setAdapter(searchAdapter);
                }
                if (listView.getAdapter() == searchAdapter) {
                    emptySubtitleTextView.setText(AndroidUtilities.replaceTags(LocaleController.formatString("NoAudioFoundInfo", R.string.NoAudioFoundInfo, query)));
                }
                searchResult = result;
                notifyDataSetChanged();
            });
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            updateEmptyView();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == 0;
        }

        @Override
        public int getItemCount() {
            if (searchResult.isEmpty()) {
                return 1;
            }
            return searchResult.size() + (searchResult.isEmpty() ? 0 : 2);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 0:
                    SharedAudioCell sharedAudioCell = new SharedAudioCell(mContext) {
                        @Override
                        public boolean needPlayMessage(MessageObject messageObject) {
                            playingAudio = messageObject;
                            ArrayList<MessageObject> arrayList = new ArrayList<>();
                            arrayList.add(messageObject);
                            return MediaController.getInstance().setPlaylist(arrayList, messageObject, 0);
                        }
                    };
                    sharedAudioCell.setCheckForButtonPress(true);
                    view = sharedAudioCell;
                    break;
                case 1:
                    view = new View(mContext);
                    view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(56)));
                    break;
                default:
                    view = new View(mContext);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                position--;
                MediaController.AudioEntry audioEntry = searchResult.get(position);

                SharedAudioCell audioCell = (SharedAudioCell) holder.itemView;
                audioCell.setTag(audioEntry);
                audioCell.setMessageObject(audioEntry.messageObject, position != searchResult.size() - 1);
            }
        }

        @Override
        public int getItemViewType(int i) {
            if (i == getItemCount() - 1) {
                return 2;
            }
            if (i == 0) {
                return 1;
            }
            return 0;
        }
    }


    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();


        themeDescriptions.add(new ThemeDescription(emptyImageView, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_dialogEmptyImage));
        themeDescriptions.add(new ThemeDescription(emptyTitleTextView, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_dialogEmptyText));
        themeDescriptions.add(new ThemeDescription(emptySubtitleTextView, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_dialogEmptyText));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_dialogScrollGlow));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(progressView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder));
        themeDescriptions.add(new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKBOX, new Class[]{SharedAudioCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkbox));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CHECKBOXCHECK, new Class[]{SharedAudioCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_checkboxCheck));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedAudioCell.class}, Theme.chat_contextResult_titleTextPaint, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{SharedAudioCell.class}, Theme.chat_contextResult_descriptionTextPaint, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        return themeDescriptions;
    }

}
