package org.plus.apps.business.ui.cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.plus.apps.business.ShopUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;


@SuppressLint("ViewConstructor")
public class Ui_image_cell extends FrameLayout {

    public static class ImageInput{
        public TLRPC.PhotoSize bigSize;
        public  TLRPC.PhotoSize smallSize;
        public int pos;
    }


    private ArrayList<ImageInput> imageInputs = new ArrayList<>();

    public interface ProductLayoutImageDelegate{
        void onItemClick(View view, int position,ImageInput imageInput,float x, float y);
        void onItemLonClick(View view, int position);
    }

    private RecyclerListView listView;
    private ItemTouchHelper itemTouchHelper;
    private LinearLayoutManager layoutManager;
    private ProductImageAdapter adapter;
    private ProductLayoutImageDelegate delegate;
    private boolean divider;

    public Ui_image_cell(Context context,int max,ProductLayoutImageDelegate layoutImageDelegate,boolean needDivider) {
        super(context);
        divider = needDivider;
        setWillNotDraw(!needDivider);

        delegate = layoutImageDelegate;

        for(int a = 0; a < max; a++)
        {
            imageInputs.add(new ImageInput());
        }
        listView = new RecyclerListView(context);
        listView.setPadding(AndroidUtilities.dp(16),0,16,AndroidUtilities.dp(16));
        listView.setClipToPadding(false);
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        listView.setLayoutManager(layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        listView.setHorizontalScrollBarEnabled(false);
        listView.setGlowColor(ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),0.1f));
        listView.setAdapter(adapter = new ProductImageAdapter(context));
        addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = AndroidUtilities.dp(4);
                outRect.bottom = AndroidUtilities.dp(4);
                outRect.top = AndroidUtilities.dp(4);
                outRect.right = AndroidUtilities.dp(4);
            }
        });
        listView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if(delegate != null){
                ImageInput input = imageInputs.get(position);
                delegate.onItemClick(view,position,input,x,y);
            }
        });

        listView.setOnItemLongClickListener((view, position) -> {
            if(delegate != null){
                delegate.onItemLonClick(view,position);
            }
            return false;
        });
    }

    public RecyclerListView getListView() {
        return listView;
    }

    public ArrayList<ImageInput> getImageInputs() {
        return imageInputs;
    }

    public void setImageInputs(ImageInput input){
        if(input == null){
            return;
        }
        imageInputs.set(input.pos,input);
        if(adapter != null){
            adapter.notifyItemChanged(input.pos);
        }
    }

    private class ProductImageAdapter extends RecyclerListView.SelectionAdapter{

        private Context mContext;

        public ProductImageAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new ProductImageCell(mContext));
        }


        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ProductImageCell ProductCell = (ProductImageCell)holder.itemView;
            ProductCell.setBackground(Theme.createSimpleSelectorRoundRectDrawable(16,ShopUtils.getIntAlphaColor(Theme.getColor(Theme.key_chats_actionBackground),0.1f),Theme.getColor(Theme.key_actionBarWhiteSelector)));
            ImageInput input = imageInputs.get(position);
            ProductCell.setImage(input);
        }

        @Override
        public int getItemCount() {
            return imageInputs.size();
        }

        public void swapElements(int fromIndex, int toIndex) {
            if (fromIndex < 0 || toIndex < 0) {
                return;
            }
            ImageInput imageInput1 = imageInputs.get(fromIndex);
            ImageInput imageInput2 = imageInputs.get(toIndex);
            if(imageInput1.smallSize == null || imageInput2.smallSize == null){
                return;
            }
            int temp = imageInput1.pos;
            imageInput1.pos = imageInput2.pos;
            imageInput2.pos = temp;
            imageInputs.set(fromIndex, imageInput2);
            imageInputs.set(toIndex, imageInput1);
            notifyItemMoved(fromIndex, toIndex);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (divider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }


    public class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            adapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
        }
    }

    public class ProductImageCell extends FrameLayout {

        private BackupImageView imageView;

        public ProductImageCell( Context context) {
            super(context);

            imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(16));
            addView(imageView, LayoutHelper.createFrame(100,100));
        }

        public void setImage(ImageInput input){
            if(input == null){
                return;
            }
            Drawable thumb = getResources().getDrawable(R.drawable.menu_camera2);
            thumb.setColorFilter(Theme.getShareColorFilter(Theme.getColor(Theme.key_chats_actionBackground),false));
            if (input.smallSize != null) {
                imageView.setSize(input.smallSize.w,input.smallSize.h);
                imageView.setSize(AndroidUtilities.dp(100),AndroidUtilities.dp(100));
                imageView.setImage(FileLoader.getPathToAttach(input.smallSize, true).getAbsolutePath(),"100_100",null);
            }else{
                imageView.setSize(AndroidUtilities.dp(28),AndroidUtilities.dp(28));
                imageView.setImage(null,null,thumb);
            }
        }

        public BackupImageView getImageView() {
            return imageView;
        }
    }

}
