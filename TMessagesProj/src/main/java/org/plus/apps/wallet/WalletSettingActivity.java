package org.plus.apps.wallet;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

public class WalletSettingActivity extends BaseFragment {

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private ListAdapter adapter;

    private int rowCount;

    private void updateRowCount(int count){
        rowCount = 0;


    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }


    @Override
    public View createView(Context context) {
        actionBar.setTitle("Wallet Setting");
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {

            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout)fragmentView;

        listView = new RecyclerListView(context);
        layoutManager = new LinearLayoutManager(context,RecyclerView.VERTICAL,false);
        listView.setLayoutManager(layoutManager);
        listView.setClipToPadding(false);
        listView.show();

        adapter = new ListAdapter(context);
        listView.setAdapter(adapter);


        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }

    private static class ListAdapter extends RecyclerListView.SelectionAdapter{

        private Context context;

        public ListAdapter(Context context) {
            this.context = context;
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

    @Override
    public void onResume() {
        super.onResume();
    }
}
