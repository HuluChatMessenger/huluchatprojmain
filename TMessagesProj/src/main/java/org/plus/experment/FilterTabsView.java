//package org.plus.experment;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.animation.AnimatorSet;
//import android.animation.ObjectAnimator;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.RectF;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.os.SystemClock;
//import android.text.StaticLayout;
//import android.text.TextPaint;
//import android.text.TextUtils;
//import android.util.Property;
//import android.util.SparseIntArray;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import androidx.core.graphics.ColorUtils;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.ItemTouchHelper;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.LinearSmoothScroller;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.iMe.android.R;
//import java.util.ArrayList;
//import org.telegram.messenger.AndroidUtilities;
//import org.telegram.messenger.FileLog;
//import org.telegram.messenger.LocaleController;
//import org.telegram.messenger.MessagesController;
//import org.telegram.messenger.MessagesStorage;
//import org.telegram.messenger.SortingFilter;
//import org.telegram.messenger.UserConfig;
//import org.telegram.tgnet.ConnectionsManager;
//import org.telegram.tgnet.TLObject;
//import org.telegram.tgnet.TLRPC$TL_error;
//import org.telegram.tgnet.TLRPC$TL_messages_updateDialogFiltersOrder;
//import org.telegram.ui.ActionBar.Theme;
//import org.telegram.ui.Components.AnimationProperties;
//import org.telegram.ui.Components.RecyclerListView;
//import org.telegram.ui.FilterTabNotificationMode;
//
//public class FilterTabsView extends FrameLayout {
//    private final Property<FilterTabsView, Float> COLORS;
//    /* access modifiers changed from: private */
//    public String aActiveTextColorKey;
//    /* access modifiers changed from: private */
//    public String aBackgroundColorKey;
//    /* access modifiers changed from: private */
//    public String aTabLineColorKey;
//    /* access modifiers changed from: private */
//    public String aUnactiveTextColorKey;
//    /* access modifiers changed from: private */
//    public String activeTextColorKey;
//    /* access modifiers changed from: private */
//    public ListAdapter adapter;
//    /* access modifiers changed from: private */
//    public int additionalTabWidth;
//    private int allTabsWidth;
//    /* access modifiers changed from: private */
//    public boolean animatingIndicator;
//    /* access modifiers changed from: private */
//    public float animatingIndicatorProgress;
//    /* access modifiers changed from: private */
//    public Runnable animationRunnable;
//    /* access modifiers changed from: private */
//    public float animationTime;
//    /* access modifiers changed from: private */
//    public float animationValue;
//    /* access modifiers changed from: private */
//    public String backgroundColorKey;
//    private AnimatorSet colorChangeAnimator;
//    private boolean commitCrossfade;
//    /* access modifiers changed from: private */
//    public Paint counterPaint;
//    private float crossfadeAlpha;
//    private Bitmap crossfadeBitmap;
//    private Paint crossfadePaint;
//    /* access modifiers changed from: private */
//    public int currentPosition;
//    private boolean customForwardArchive;
//    /* access modifiers changed from: private */
//    public FilterTabsViewDelegate delegate;
//    /* access modifiers changed from: private */
//    public Paint deletePaint;
//    /* access modifiers changed from: private */
//    public float editingAnimationProgress;
//    private boolean editingForwardAnimation;
//    /* access modifiers changed from: private */
//    public float editingStartAnimationProgress;
//    private SparseIntArray idToPosition;
//    private boolean ignoreLayout;
//    /* access modifiers changed from: private */
//    public CubicBezierInterpolator interpolator;
//    private boolean invalidated;
//    /* access modifiers changed from: private */
//    public boolean isEditing;
//    /* access modifiers changed from: private */
//    public long lastAnimationTime;
//    private long lastEditingAnimationTime;
//    private LinearLayoutManager layoutManager;
//    private float[] lineCornerRadiiForBottom;
//    private float[] lineCornerRadiiForTop;
//    /* access modifiers changed from: private */
//    public RecyclerListView listView;
//    /* access modifiers changed from: private */
//    public int manualScrollingToId;
//    /* access modifiers changed from: private */
//    public int manualScrollingToPosition;
//    /* access modifiers changed from: private */
//    public TabMode mode;
//    /* access modifiers changed from: private */
//    public boolean orderChanged;
//    private SparseIntArray positionToId;
//    private SparseIntArray positionToWidth;
//    private SparseIntArray positionToX;
//    private int prevLayoutWidth;
//    /* access modifiers changed from: private */
//    public int previousId;
//    /* access modifiers changed from: private */
//    public int previousPosition;
//    private float rad;
//    private int scrollingToChild;
//    /* access modifiers changed from: private */
//    public int selectedTabId;
//    private String selectorColorKey;
//    /* access modifiers changed from: private */
//    public GradientDrawable selectorDrawable;
//    /* access modifiers changed from: private */
//    public String tabLineColorKey;
//    /* access modifiers changed from: private */
//    public ArrayList<Tab> tabs;
//    /* access modifiers changed from: private */
//    public TextPaint textCounterPaint;
//    /* access modifiers changed from: private */
//    public TextPaint textPaint;
//    /* access modifiers changed from: private */
//    public String unactiveTextColorKey;
//    /* access modifiers changed from: private */
//    public final UserConfig userConfig;
//    private boolean withBackground;
//
//    public interface FilterTabsViewDelegate {
//        boolean canPerformActions();
//
//        boolean didSelectTab(TabView tabView, boolean z);
//
//        int getTabCounter(int i);
//
//        boolean isTabMenuVisible();
//
//        void onDeletePressed(int i);
//
//        void onPageReorder(int i, int i2);
//
//        void onPageScrolled(float f);
//
//        void onPageSelected(int i, boolean z);
//
//        void onSamePageSelected();
//
//        void onSwipeProgressChanged(float f);
//
//        void onTabSelected(int i);
//    }
//
//    public enum TabMode {
//        FORWARD,
//        FOLDERS,
//        CLOUD,
//        ARCHIVE,
//        MANAGEMENT
//    }
//
//    static /* synthetic */ void lambda$setIsEditing$2(TLObject tLObject, TLRPC$TL_error tLRPC$TL_error) {
//    }
//
//    public FilterTabsView(Context context, TabMode tabMode) {
//        this(context);
//        this.mode = tabMode;
//        if (tabMode == TabMode.ARCHIVE) {
//            this.backgroundColorKey = "actionBarDefaultArchived";
//            RecyclerListView recyclerListView = this.listView;
//            this.selectorColorKey = "actionBarDefaultArchivedSelector";
//            recyclerListView.setSelectorDrawableColor(Theme.getColor("actionBarDefaultArchivedSelector"));
//        }
//        updateLineCornerRadii();
//    }
//
//    public int getCurrentPosition() {
//        return this.currentPosition;
//    }
//
//    public void updateLineCornerRadii() {
//        GradientDrawable gradientDrawable = this.selectorDrawable;
//        TabMode tabMode = this.mode;
//        gradientDrawable.setCornerRadii(((tabMode == TabMode.FOLDERS || tabMode == TabMode.ARCHIVE) && this.userConfig.filterTabsAtBottom) ? this.lineCornerRadiiForBottom : this.lineCornerRadiiForTop);
//    }
//
//    public void addIconTab(int i, int i2, int i3) {
//        int size = this.tabs.size();
//        if (size == 0 && this.selectedTabId == -1) {
//            this.selectedTabId = i;
//        }
//        this.positionToId.put(size, i);
//        this.idToPosition.put(i, size);
//        int i4 = this.selectedTabId;
//        if (i4 != -1 && i4 == i) {
//            this.currentPosition = size;
//        }
//        TabWithIcon tabWithIcon = new TabWithIcon(i, i2, i3);
//        this.allTabsWidth += tabWithIcon.getWidth(true) + AndroidUtilities.dp(32.0f);
//        this.tabs.add(tabWithIcon);
//    }
//
//    private void notifySwipeProgressChanged() {
//        FilterTabsViewDelegate filterTabsViewDelegate = this.delegate;
//        if (filterTabsViewDelegate != null) {
//            filterTabsViewDelegate.onSwipeProgressChanged(this.animatingIndicatorProgress);
//        }
//    }
//
//    public void updateBackground() {
//        TabMode tabMode = this.mode;
//        if (tabMode != TabMode.FOLDERS && tabMode != TabMode.ARCHIVE) {
//            return;
//        }
//        if (this.userConfig.filterTabsAtBottom) {
//            setBackgroundColor(Theme.getColor(tabMode == TabMode.ARCHIVE ? "actionBarDefaultArchived" : "actionBarDefault"));
//            this.withBackground = true;
//            return;
//        }
//        setBackgroundColor(0);
//        this.withBackground = false;
//    }
//
//    public boolean isWithBackground() {
//        return this.withBackground;
//    }
//
//    public void setCustomForwardArchive(boolean z) {
//        this.customForwardArchive = z;
//    }
//
//    private class TabWithIcon extends Tab {
//        /* access modifiers changed from: private */
//        public int filledIconId;
//        /* access modifiers changed from: private */
//        public int outlinedIconId;
//
//        public int getWidth(boolean z) {
//            int i;
//            int dp = AndroidUtilities.dp(24.0f);
//            this.titleWidth = dp;
//            if (z) {
//                i = FilterTabsView.this.delegate.getTabCounter(this.id);
//                if (i < 0) {
//                    i = 0;
//                }
//                if (z) {
//                    this.counter = i;
//                }
//            } else {
//                i = this.counter;
//            }
//            if (((FilterTabsView.this.mode == TabMode.FOLDERS || FilterTabsView.this.mode == TabMode.ARCHIVE) && FilterTabsView.this.userConfig.notificationMode != FilterTabNotificationMode.NUMBER) || FilterTabsView.this.mode == TabMode.CLOUD || FilterTabsView.this.mode == TabMode.FORWARD) {
//                return dp;
//            }
//            if (i > 0) {
//                dp += Math.max(AndroidUtilities.dp(10.0f), (int) Math.ceil((double) FilterTabsView.this.textCounterPaint.measureText(String.format("%d", new Object[]{Integer.valueOf(i)})))) + AndroidUtilities.dp(10.0f) + AndroidUtilities.dp(6.0f);
//            }
//            return Math.max(AndroidUtilities.dp(40.0f), dp);
//        }
//
//        private TabWithIcon(int i, int i2, int i3) {
//            super(i, "");
//            this.outlinedIconId = i2;
//            this.filledIconId = i3;
//        }
//    }
//
//    private class Tab {
//        public int counter;
//        public int id;
//        public String title;
//        public int titleWidth;
//
//        public Tab(int i, String str) {
//            this.id = i;
//            this.title = str;
//        }
//
//        public int getWidth(boolean z) {
//            int i;
//            int ceil = (int) Math.ceil((double) FilterTabsView.this.textPaint.measureText(this.title));
//            this.titleWidth = ceil;
//            if (z) {
//                i = FilterTabsView.this.delegate.getTabCounter(this.id);
//                if (i < 0) {
//                    i = 0;
//                }
//                if (z) {
//                    this.counter = i;
//                }
//            } else {
//                i = this.counter;
//            }
//            if (((FilterTabsView.this.mode == TabMode.FOLDERS || FilterTabsView.this.mode == TabMode.ARCHIVE) && FilterTabsView.this.userConfig.notificationMode != FilterTabNotificationMode.NUMBER) || FilterTabsView.this.mode == TabMode.CLOUD || FilterTabsView.this.mode == TabMode.FORWARD) {
//                return ceil;
//            }
//            if (i > 0) {
//                ceil += Math.max(AndroidUtilities.dp(10.0f), (int) Math.ceil((double) FilterTabsView.this.textCounterPaint.measureText(String.format("%d", new Object[]{Integer.valueOf(i)})))) + AndroidUtilities.dp(10.0f) + AndroidUtilities.dp(6.0f);
//            }
//            return Math.max(AndroidUtilities.dp(40.0f), ceil);
//        }
//
//        public boolean setTitle(String str) {
//            if (TextUtils.equals(this.title, str)) {
//                return false;
//            }
//            this.title = str;
//            return true;
//        }
//    }
//
//    public class TabView extends View {
//        private Drawable currentFilledIconDrawable;
//        private int currentFilledIconId;
//        private Drawable currentOutLinedIconDrawable;
//        private int currentOutLinedIconId;
//        private int currentPosition;
//        /* access modifiers changed from: private */
//        public Tab currentTab;
//        private String currentText;
//        /* access modifiers changed from: private */
//        public RectF rect = new RectF();
//        /* access modifiers changed from: private */
//        public int tabWidth;
//        private int textHeight;
//        private StaticLayout textLayout;
//        private int textOffsetX;
//
//        public TabView(Context context) {
//            super(context);
//        }
//
//        public void setTab(Tab tab, int i) {
//            this.currentTab = tab;
//            this.currentPosition = i;
//            requestLayout();
//        }
//
//        public int getId() {
//            return this.currentTab.id;
//        }
//
//        /* access modifiers changed from: protected */
//        public void onMeasure(int i, int i2) {
//            setMeasuredDimension(this.currentTab.getWidth(false) + AndroidUtilities.dp(32.0f) + FilterTabsView.this.additionalTabWidth, View.MeasureSpec.getSize(i2));
//        }
//
//        /* access modifiers changed from: protected */
//        /* JADX WARNING: Removed duplicated region for block: B:107:0x02ed  */
//        /* JADX WARNING: Removed duplicated region for block: B:120:0x036c  */
//        /* JADX WARNING: Removed duplicated region for block: B:148:0x04de  */
//        /* JADX WARNING: Removed duplicated region for block: B:149:0x04f2  */
//        /* JADX WARNING: Removed duplicated region for block: B:154:0x0525  */
//        /* JADX WARNING: Removed duplicated region for block: B:163:0x0565  */
//        /* JADX WARNING: Removed duplicated region for block: B:179:0x0606  */
//        /* JADX WARNING: Removed duplicated region for block: B:206:0x072e  */
//        /* JADX WARNING: Removed duplicated region for block: B:211:? A[RETURN, SYNTHETIC] */
//        /* JADX WARNING: Removed duplicated region for block: B:54:0x01a3  */
//        /* JADX WARNING: Removed duplicated region for block: B:55:0x01d1  */
//        /* JADX WARNING: Removed duplicated region for block: B:66:0x0209  */
//        /* JADX WARNING: Removed duplicated region for block: B:67:0x020f  */
//        /* JADX WARNING: Removed duplicated region for block: B:81:0x0258  */
//        /* JADX WARNING: Removed duplicated region for block: B:85:0x026a  */
//        /* JADX WARNING: Removed duplicated region for block: B:98:0x02ad  */
//        /* Code decompiled incorrectly, please refer to instructions dump. */
//        public void onDraw(android.graphics.Canvas r29) {
//            /*
//                r28 = this;
//                r0 = r28
//                r7 = r29
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.id
//                boolean r1 = org.telegram.messenger.SortingFilter.isSortingFilter(r1)
//                r2 = 1065353216(0x3f800000, float:1.0)
//                r8 = 2147483647(0x7fffffff, float:NaN)
//                r9 = 0
//                if (r1 != 0) goto L_0x005f
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r1 = r1.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r3 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r1 == r3) goto L_0x005f
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.id
//                if (r1 == r8) goto L_0x005f
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                float r1 = r1.editingAnimationProgress
//                int r1 = (r1 > r9 ? 1 : (r1 == r9 ? 0 : -1))
//                if (r1 == 0) goto L_0x005f
//                r29.save()
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                float r1 = r1.editingAnimationProgress
//                int r3 = r0.currentPosition
//                int r3 = r3 % 2
//                if (r3 != 0) goto L_0x003f
//                r3 = r2
//                goto L_0x0041
//            L_0x003f:
//                r3 = -1082130432(0xffffffffbf800000, float:-1.0)
//            L_0x0041:
//                float r1 = r1 * r3
//                r3 = 1059648963(0x3f28f5c3, float:0.66)
//                int r3 = org.telegram.messenger.AndroidUtilities.dp(r3)
//                float r3 = (float) r3
//                float r3 = r3 * r1
//                r7.translate(r3, r9)
//                int r3 = r28.getMeasuredWidth()
//                int r3 = r3 / 2
//                float r3 = (float) r3
//                int r4 = r28.getMeasuredHeight()
//                int r4 = r4 / 2
//                float r4 = (float) r4
//                r7.rotate(r1, r3, r4)
//            L_0x005f:
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                int r1 = r1.manualScrollingToId
//                r3 = -1
//                if (r1 == r3) goto L_0x0075
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                int r1 = r1.manualScrollingToId
//                org.telegram.ui.Components.FilterTabsView r4 = org.telegram.ui.Components.FilterTabsView.this
//                int r4 = r4.selectedTabId
//                goto L_0x0081
//            L_0x0075:
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                int r1 = r1.selectedTabId
//                org.telegram.ui.Components.FilterTabsView r4 = org.telegram.ui.Components.FilterTabsView.this
//                int r4 = r4.previousId
//            L_0x0081:
//                org.telegram.ui.Components.FilterTabsView$Tab r5 = r0.currentTab
//                int r5 = r5.id
//                if (r5 != r1) goto L_0x0089
//                r6 = r2
//                goto L_0x008a
//            L_0x0089:
//                r6 = r9
//            L_0x008a:
//                java.lang.String r10 = "chats_tabUnreadActiveBackground"
//                java.lang.String r11 = "chats_tabUnreadUnactiveBackground"
//                if (r5 != r1) goto L_0x00a9
//                org.telegram.ui.Components.FilterTabsView r5 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r5 = r5.activeTextColorKey
//                org.telegram.ui.Components.FilterTabsView r12 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r12 = r12.aActiveTextColorKey
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r13 = r13.unactiveTextColorKey
//                org.telegram.ui.Components.FilterTabsView r14 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r14 = r14.aUnactiveTextColorKey
//                goto L_0x00c6
//            L_0x00a9:
//                org.telegram.ui.Components.FilterTabsView r5 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r5 = r5.unactiveTextColorKey
//                org.telegram.ui.Components.FilterTabsView r12 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r12 = r12.aUnactiveTextColorKey
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r13 = r13.activeTextColorKey
//                org.telegram.ui.Components.FilterTabsView r14 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r14 = r14.aUnactiveTextColorKey
//                r27 = r11
//                r11 = r10
//                r10 = r27
//            L_0x00c6:
//                if (r12 != 0) goto L_0x0123
//                org.telegram.ui.Components.FilterTabsView r12 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r12 = r12.animatingIndicator
//                if (r12 != 0) goto L_0x00d8
//                org.telegram.ui.Components.FilterTabsView r12 = org.telegram.ui.Components.FilterTabsView.this
//                int r12 = r12.manualScrollingToId
//                if (r12 == r3) goto L_0x00e1
//            L_0x00d8:
//                org.telegram.ui.Components.FilterTabsView$Tab r12 = r0.currentTab
//                int r12 = r12.id
//                if (r12 == r1) goto L_0x00f0
//                if (r12 != r4) goto L_0x00e1
//                goto L_0x00f0
//            L_0x00e1:
//                org.telegram.ui.Components.FilterTabsView r12 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r12 = r12.textPaint
//                int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
//                r12.setColor(r5)
//                goto L_0x019b
//            L_0x00f0:
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r6 = r6.textPaint
//                int r12 = org.telegram.ui.ActionBar.Theme.getColor(r13)
//                int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                float r13 = r13.animatingIndicatorProgress
//                int r5 = androidx.core.graphics.ColorUtils.blendARGB(r12, r5, r13)
//                r6.setColor(r5)
//                org.telegram.ui.Components.FilterTabsView$Tab r5 = r0.currentTab
//                int r5 = r5.id
//                if (r5 != r1) goto L_0x0118
//                org.telegram.ui.Components.FilterTabsView r5 = org.telegram.ui.Components.FilterTabsView.this
//                float r5 = r5.animatingIndicatorProgress
//                goto L_0x0120
//            L_0x0118:
//                org.telegram.ui.Components.FilterTabsView r5 = org.telegram.ui.Components.FilterTabsView.this
//                float r5 = r5.animatingIndicatorProgress
//            L_0x011e:
//                float r5 = r2 - r5
//            L_0x0120:
//                r6 = r5
//                goto L_0x019b
//            L_0x0123:
//                int r5 = org.telegram.ui.ActionBar.Theme.getColor(r5)
//                int r12 = org.telegram.ui.ActionBar.Theme.getColor(r12)
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r15 = r15.animatingIndicator
//                if (r15 != 0) goto L_0x013b
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                int r15 = r15.manualScrollingToPosition
//                if (r15 == r3) goto L_0x0144
//            L_0x013b:
//                org.telegram.ui.Components.FilterTabsView$Tab r15 = r0.currentTab
//                int r15 = r15.id
//                if (r15 == r1) goto L_0x0158
//                if (r15 != r4) goto L_0x0144
//                goto L_0x0158
//            L_0x0144:
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r13 = r13.textPaint
//                org.telegram.ui.Components.FilterTabsView r14 = org.telegram.ui.Components.FilterTabsView.this
//                float r14 = r14.animationValue
//                int r5 = androidx.core.graphics.ColorUtils.blendARGB(r5, r12, r14)
//                r13.setColor(r5)
//                goto L_0x019b
//            L_0x0158:
//                int r6 = org.telegram.ui.ActionBar.Theme.getColor(r13)
//                int r13 = org.telegram.ui.ActionBar.Theme.getColor(r14)
//                org.telegram.ui.Components.FilterTabsView r14 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r14 = r14.textPaint
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                float r15 = r15.animationValue
//                int r6 = androidx.core.graphics.ColorUtils.blendARGB(r6, r13, r15)
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                float r13 = r13.animationValue
//                int r5 = androidx.core.graphics.ColorUtils.blendARGB(r5, r12, r13)
//                org.telegram.ui.Components.FilterTabsView r12 = org.telegram.ui.Components.FilterTabsView.this
//                float r12 = r12.animatingIndicatorProgress
//                int r5 = androidx.core.graphics.ColorUtils.blendARGB(r6, r5, r12)
//                r14.setColor(r5)
//                org.telegram.ui.Components.FilterTabsView$Tab r5 = r0.currentTab
//                int r5 = r5.id
//                if (r5 != r1) goto L_0x0194
//                org.telegram.ui.Components.FilterTabsView r5 = org.telegram.ui.Components.FilterTabsView.this
//                float r5 = r5.animatingIndicatorProgress
//                goto L_0x0120
//            L_0x0194:
//                org.telegram.ui.Components.FilterTabsView r5 = org.telegram.ui.Components.FilterTabsView.this
//                float r5 = r5.animatingIndicatorProgress
//                goto L_0x011e
//            L_0x019b:
//                org.telegram.ui.Components.FilterTabsView$Tab r5 = r0.currentTab
//                int r5 = r5.counter
//                r13 = 1
//                r14 = 0
//                if (r5 <= 0) goto L_0x01d1
//                java.lang.Object[] r15 = new java.lang.Object[r13]
//                java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
//                r15[r14] = r5
//                java.lang.String r5 = "%d"
//                java.lang.String r5 = java.lang.String.format(r5, r15)
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r15 = r15.textCounterPaint
//                float r15 = r15.measureText(r5)
//                double r14 = (double) r15
//                double r14 = java.lang.Math.ceil(r14)
//                int r14 = (int) r14
//                r15 = 1092616192(0x41200000, float:10.0)
//                int r12 = org.telegram.messenger.AndroidUtilities.dp(r15)
//                int r12 = java.lang.Math.max(r12, r14)
//                int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
//                int r12 = r12 + r15
//                goto L_0x01d4
//            L_0x01d1:
//                r5 = 0
//                r12 = 0
//                r14 = 0
//            L_0x01d4:
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r15 = r15.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r2 = org.telegram.ui.Components.FilterTabsView.TabMode.FOLDERS
//                if (r15 == r2) goto L_0x01e8
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r15 = r15.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r13 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r15 != r13) goto L_0x01f4
//            L_0x01e8:
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.messenger.UserConfig r13 = r13.userConfig
//                org.telegram.ui.FilterTabNotificationMode r13 = r13.notificationMode
//                org.telegram.ui.FilterTabNotificationMode r15 = org.telegram.ui.FilterTabNotificationMode.NUMBER
//                if (r13 != r15) goto L_0x020f
//            L_0x01f4:
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r13 = r13.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r15 = org.telegram.ui.Components.FilterTabsView.TabMode.CLOUD
//                if (r13 == r15) goto L_0x020f
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r13 = r13.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r15 = org.telegram.ui.Components.FilterTabsView.TabMode.FORWARD
//                if (r13 != r15) goto L_0x0209
//                goto L_0x020f
//            L_0x0209:
//                r27 = r12
//                r12 = r5
//                r5 = r27
//                goto L_0x0212
//            L_0x020f:
//                r5 = 0
//                r12 = 0
//                r14 = 0
//            L_0x0212:
//                org.telegram.ui.Components.FilterTabsView$Tab r13 = r0.currentTab
//                int r13 = r13.id
//                boolean r13 = org.telegram.messenger.SortingFilter.isSortingFilter(r13)
//                r15 = 1101004800(0x41a00000, float:20.0)
//                if (r13 != 0) goto L_0x0250
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r13 = r13.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r3 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r13 == r3) goto L_0x0250
//                org.telegram.ui.Components.FilterTabsView$Tab r3 = r0.currentTab
//                int r3 = r3.id
//                if (r3 == r8) goto L_0x0250
//                org.telegram.ui.Components.FilterTabsView r3 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r3 = r3.isEditing
//                if (r3 != 0) goto L_0x0240
//                org.telegram.ui.Components.FilterTabsView r3 = org.telegram.ui.Components.FilterTabsView.this
//                float r3 = r3.editingStartAnimationProgress
//                int r3 = (r3 > r9 ? 1 : (r3 == r9 ? 0 : -1))
//                if (r3 == 0) goto L_0x0250
//            L_0x0240:
//                float r3 = (float) r5
//                int r13 = org.telegram.messenger.AndroidUtilities.dp(r15)
//                int r13 = r13 - r5
//                float r5 = (float) r13
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                float r13 = r13.editingStartAnimationProgress
//                float r5 = r5 * r13
//                float r3 = r3 + r5
//                int r5 = (int) r3
//            L_0x0250:
//                org.telegram.ui.Components.FilterTabsView$Tab r3 = r0.currentTab
//                int r3 = r3.titleWidth
//                r13 = 1086324736(0x40c00000, float:6.0)
//                if (r5 == 0) goto L_0x026a
//                if (r12 == 0) goto L_0x025d
//                r15 = 1065353216(0x3f800000, float:1.0)
//                goto L_0x0263
//            L_0x025d:
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                float r15 = r15.editingStartAnimationProgress
//            L_0x0263:
//                float r15 = r15 * r13
//                int r15 = org.telegram.messenger.AndroidUtilities.dp(r15)
//                int r15 = r15 + r5
//                goto L_0x026b
//            L_0x026a:
//                r15 = 0
//            L_0x026b:
//                int r3 = r3 + r15
//                r0.tabWidth = r3
//                int r3 = r28.getMeasuredWidth()
//                int r15 = r0.tabWidth
//                int r3 = r3 - r15
//                int r3 = r3 / 2
//                org.telegram.ui.Components.FilterTabsView$Tab r15 = r0.currentTab
//                int r15 = r15.counter
//                r16 = 1073741824(0x40000000, float:2.0)
//                r17 = 1077936128(0x40400000, float:3.0)
//                r18 = 1132396544(0x437f0000, float:255.0)
//                if (r15 <= 0) goto L_0x0366
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r15 = r15.mode
//                if (r15 == r2) goto L_0x0295
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r2 = r2.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r15 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r2 != r15) goto L_0x0366
//            L_0x0295:
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.messenger.UserConfig r2 = r2.userConfig
//                org.telegram.ui.FilterTabNotificationMode r2 = r2.notificationMode
//                org.telegram.ui.FilterTabNotificationMode r15 = org.telegram.ui.FilterTabNotificationMode.POINT
//                if (r2 != r15) goto L_0x0366
//                boolean r2 = org.telegram.ui.ActionBar.Theme.hasThemeKey(r10)
//                if (r2 == 0) goto L_0x02ed
//                boolean r2 = org.telegram.ui.ActionBar.Theme.hasThemeKey(r11)
//                if (r2 == 0) goto L_0x02ed
//                int r2 = org.telegram.ui.ActionBar.Theme.getColor(r10)
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r15 = r15.animatingIndicator
//                if (r15 != 0) goto L_0x02c2
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                int r15 = r15.manualScrollingToPosition
//                r13 = -1
//                if (r15 == r13) goto L_0x02cb
//            L_0x02c2:
//                org.telegram.ui.Components.FilterTabsView$Tab r13 = r0.currentTab
//                int r13 = r13.id
//                if (r13 == r1) goto L_0x02d5
//                if (r13 != r4) goto L_0x02cb
//                goto L_0x02d5
//            L_0x02cb:
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r13 = r13.counterPaint
//                r13.setColor(r2)
//                goto L_0x0300
//            L_0x02d5:
//                int r13 = org.telegram.ui.ActionBar.Theme.getColor(r11)
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r15 = r15.counterPaint
//                org.telegram.ui.Components.FilterTabsView r9 = org.telegram.ui.Components.FilterTabsView.this
//                float r9 = r9.animatingIndicatorProgress
//                int r2 = androidx.core.graphics.ColorUtils.blendARGB(r13, r2, r9)
//                r15.setColor(r2)
//                goto L_0x0300
//            L_0x02ed:
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r2 = r2.counterPaint
//                org.telegram.ui.Components.FilterTabsView r9 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r9 = r9.textPaint
//                int r9 = r9.getColor()
//                r2.setColor(r9)
//            L_0x0300:
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.id
//                if (r2 == r8) goto L_0x032e
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r2 = r2.isEditing
//                if (r2 != 0) goto L_0x0319
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                float r2 = r2.editingStartAnimationProgress
//                r9 = 0
//                int r2 = (r2 > r9 ? 1 : (r2 == r9 ? 0 : -1))
//                if (r2 == 0) goto L_0x032e
//            L_0x0319:
//                if (r12 != 0) goto L_0x032e
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r2 = r2.counterPaint
//                org.telegram.ui.Components.FilterTabsView r9 = org.telegram.ui.Components.FilterTabsView.this
//                float r9 = r9.editingStartAnimationProgress
//                float r9 = r9 * r18
//                int r9 = (int) r9
//                r2.setAlpha(r9)
//                goto L_0x0339
//            L_0x032e:
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r2 = r2.counterPaint
//                r9 = 255(0xff, float:3.57E-43)
//                r2.setAlpha(r9)
//            L_0x0339:
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.titleWidth
//                int r2 = r2 + r3
//                int r9 = org.telegram.messenger.AndroidUtilities.dp(r17)
//                int r2 = r2 + r9
//                float r2 = (float) r2
//                int r9 = r28.getMeasuredHeight()
//                r13 = 1101004800(0x41a00000, float:20.0)
//                int r15 = org.telegram.messenger.AndroidUtilities.dp(r13)
//                int r9 = r9 - r15
//                float r9 = (float) r9
//                float r9 = r9 / r16
//                int r13 = org.telegram.messenger.AndroidUtilities.dp(r17)
//                float r13 = (float) r13
//                float r9 = r9 - r13
//                int r13 = org.telegram.messenger.AndroidUtilities.dp(r17)
//                float r13 = (float) r13
//                org.telegram.ui.Components.FilterTabsView r15 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r15 = r15.counterPaint
//                r7.drawCircle(r2, r9, r13, r15)
//            L_0x0366:
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                boolean r9 = r2 instanceof org.telegram.ui.Components.FilterTabsView.TabWithIcon
//                if (r9 == 0) goto L_0x043d
//                org.telegram.ui.Components.FilterTabsView$TabWithIcon r2 = (org.telegram.ui.Components.FilterTabsView.TabWithIcon) r2
//                int r13 = r2.outlinedIconId
//                int r15 = r0.currentOutLinedIconId
//                if (r13 == r15) goto L_0x038c
//                int r13 = r2.outlinedIconId
//                r0.currentOutLinedIconId = r13
//                android.content.Context r13 = r28.getContext()
//                int r15 = r0.currentOutLinedIconId
//                android.graphics.drawable.Drawable r13 = androidx.appcompat.content.res.AppCompatResources.getDrawable(r13, r15)
//                android.graphics.drawable.Drawable r13 = r13.mutate()
//                r0.currentOutLinedIconDrawable = r13
//            L_0x038c:
//                android.graphics.drawable.Drawable r13 = r0.currentOutLinedIconDrawable
//                com.airbnb.lottie.SimpleColorFilter r15 = new com.airbnb.lottie.SimpleColorFilter
//                org.telegram.ui.Components.FilterTabsView r8 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r8 = r8.textPaint
//                int r8 = r8.getColor()
//                r15.<init>(r8)
//                r13.setColorFilter(r15)
//                int r8 = r2.filledIconId
//                r13 = -1
//                if (r8 != r13) goto L_0x03aa
//                android.graphics.drawable.Drawable r2 = r0.currentOutLinedIconDrawable
//                goto L_0x0411
//            L_0x03aa:
//                int r8 = r2.filledIconId
//                int r13 = r0.currentFilledIconId
//                if (r8 == r13) goto L_0x03c8
//                int r2 = r2.filledIconId
//                r0.currentFilledIconId = r2
//                android.content.Context r2 = r28.getContext()
//                int r8 = r0.currentFilledIconId
//                android.graphics.drawable.Drawable r2 = androidx.appcompat.content.res.AppCompatResources.getDrawable(r2, r8)
//                android.graphics.drawable.Drawable r2 = r2.mutate()
//                r0.currentFilledIconDrawable = r2
//            L_0x03c8:
//                android.graphics.drawable.Drawable r2 = r0.currentOutLinedIconDrawable
//                com.airbnb.lottie.SimpleColorFilter r8 = new com.airbnb.lottie.SimpleColorFilter
//                org.telegram.ui.Components.FilterTabsView r13 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r13 = r13.textPaint
//                int r13 = r13.getColor()
//                r8.<init>(r13)
//                r2.setColorFilter(r8)
//                android.graphics.drawable.Drawable r2 = r0.currentOutLinedIconDrawable
//                float r6 = r6 * r18
//                int r8 = java.lang.Math.round(r6)
//                r13 = 255(0xff, float:3.57E-43)
//                int r8 = 255 - r8
//                r2.setAlpha(r8)
//                android.graphics.drawable.Drawable r2 = r0.currentFilledIconDrawable
//                int r6 = java.lang.Math.round(r6)
//                r2.setAlpha(r6)
//                android.graphics.drawable.Drawable r2 = r0.currentFilledIconDrawable
//                com.airbnb.lottie.SimpleColorFilter r6 = new com.airbnb.lottie.SimpleColorFilter
//                org.telegram.ui.Components.FilterTabsView r8 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r8 = r8.textPaint
//                int r8 = r8.getColor()
//                r6.<init>(r8)
//                r2.setColorFilter(r6)
//                org.telegram.ui.Components.CombinedDrawable r2 = new org.telegram.ui.Components.CombinedDrawable
//                android.graphics.drawable.Drawable r6 = r0.currentOutLinedIconDrawable
//                android.graphics.drawable.Drawable r8 = r0.currentFilledIconDrawable
//                r2.<init>(r6, r8)
//            L_0x0411:
//                r29.save()
//                int r6 = r0.textOffsetX
//                int r6 = r6 + r3
//                float r6 = (float) r6
//                int r8 = r28.getMeasuredHeight()
//                r13 = 1103101952(0x41c00000, float:24.0)
//                int r15 = org.telegram.messenger.AndroidUtilities.dp(r13)
//                int r8 = r8 - r15
//                int r8 = r8 / 2
//                r15 = 1
//                int r8 = r8 + r15
//                float r8 = (float) r8
//                r7.translate(r6, r8)
//                int r6 = org.telegram.messenger.AndroidUtilities.dp(r13)
//                int r8 = org.telegram.messenger.AndroidUtilities.dp(r13)
//                r13 = 0
//                r2.setBounds(r13, r13, r6, r8)
//                r2.draw(r7)
//                r29.restore()
//            L_0x043d:
//                if (r9 != 0) goto L_0x0494
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                java.lang.String r2 = r2.title
//                java.lang.String r6 = r0.currentText
//                boolean r2 = android.text.TextUtils.equals(r2, r6)
//                if (r2 != 0) goto L_0x0494
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                java.lang.String r2 = r2.title
//                r0.currentText = r2
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r6 = r6.textPaint
//                android.graphics.Paint$FontMetricsInt r6 = r6.getFontMetricsInt()
//                r8 = 1097859072(0x41700000, float:15.0)
//                int r8 = org.telegram.messenger.AndroidUtilities.dp(r8)
//                r13 = 0
//                java.lang.CharSequence r20 = org.telegram.messenger.Emoji.replaceEmoji(r2, r6, r8, r13)
//                android.text.StaticLayout r2 = new android.text.StaticLayout
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r21 = r6.textPaint
//                r6 = 1137180672(0x43c80000, float:400.0)
//                int r22 = org.telegram.messenger.AndroidUtilities.dp(r6)
//                android.text.Layout$Alignment r23 = android.text.Layout.Alignment.ALIGN_NORMAL
//                r24 = 1065353216(0x3f800000, float:1.0)
//                r25 = 0
//                r26 = 0
//                r19 = r2
//                r19.<init>(r20, r21, r22, r23, r24, r25, r26)
//                r0.textLayout = r2
//                int r2 = r2.getHeight()
//                r0.textHeight = r2
//                android.text.StaticLayout r2 = r0.textLayout
//                r6 = 0
//                float r2 = r2.getLineLeft(r6)
//                float r2 = -r2
//                int r2 = (int) r2
//                r0.textOffsetX = r2
//            L_0x0494:
//                if (r9 != 0) goto L_0x04b8
//                android.text.StaticLayout r2 = r0.textLayout
//                if (r2 == 0) goto L_0x04b8
//                r29.save()
//                int r2 = r0.textOffsetX
//                int r2 = r2 + r3
//                float r2 = (float) r2
//                int r6 = r28.getMeasuredHeight()
//                int r8 = r0.textHeight
//                int r6 = r6 - r8
//                int r6 = r6 / 2
//                r8 = 1
//                int r6 = r6 + r8
//                float r6 = (float) r6
//                r7.translate(r2, r6)
//                android.text.StaticLayout r2 = r0.textLayout
//                r2.draw(r7)
//                r29.restore()
//            L_0x04b8:
//                if (r12 != 0) goto L_0x04d6
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.id
//                r6 = 2147483647(0x7fffffff, float:NaN)
//                if (r2 == r6) goto L_0x0706
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r2 = r2.isEditing
//                if (r2 != 0) goto L_0x04d6
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                float r2 = r2.editingStartAnimationProgress
//                r6 = 0
//                int r2 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
//                if (r2 == 0) goto L_0x0706
//            L_0x04d6:
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r2 = r2.aBackgroundColorKey
//                if (r2 != 0) goto L_0x04f2
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r2 = r2.textCounterPaint
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r6 = r6.backgroundColorKey
//                int r6 = org.telegram.ui.ActionBar.Theme.getColor(r6)
//                r2.setColor(r6)
//                goto L_0x0519
//            L_0x04f2:
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r2 = r2.backgroundColorKey
//                int r2 = org.telegram.ui.ActionBar.Theme.getColor(r2)
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                java.lang.String r6 = r6.aBackgroundColorKey
//                int r6 = org.telegram.ui.ActionBar.Theme.getColor(r6)
//                org.telegram.ui.Components.FilterTabsView r8 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r8 = r8.textCounterPaint
//                org.telegram.ui.Components.FilterTabsView r9 = org.telegram.ui.Components.FilterTabsView.this
//                float r9 = r9.animationValue
//                int r2 = androidx.core.graphics.ColorUtils.blendARGB(r2, r6, r9)
//                r8.setColor(r2)
//            L_0x0519:
//                boolean r2 = org.telegram.ui.ActionBar.Theme.hasThemeKey(r10)
//                if (r2 == 0) goto L_0x0565
//                boolean r2 = org.telegram.ui.ActionBar.Theme.hasThemeKey(r11)
//                if (r2 == 0) goto L_0x0565
//                int r2 = org.telegram.ui.ActionBar.Theme.getColor(r10)
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r6 = r6.animatingIndicator
//                if (r6 != 0) goto L_0x053a
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                int r6 = r6.manualScrollingToPosition
//                r8 = -1
//                if (r6 == r8) goto L_0x0543
//            L_0x053a:
//                org.telegram.ui.Components.FilterTabsView$Tab r6 = r0.currentTab
//                int r6 = r6.id
//                if (r6 == r1) goto L_0x054d
//                if (r6 != r4) goto L_0x0543
//                goto L_0x054d
//            L_0x0543:
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r1 = r1.counterPaint
//                r1.setColor(r2)
//                goto L_0x0578
//            L_0x054d:
//                int r1 = org.telegram.ui.ActionBar.Theme.getColor(r11)
//                org.telegram.ui.Components.FilterTabsView r4 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r4 = r4.counterPaint
//                org.telegram.ui.Components.FilterTabsView r6 = org.telegram.ui.Components.FilterTabsView.this
//                float r6 = r6.animatingIndicatorProgress
//                int r1 = androidx.core.graphics.ColorUtils.blendARGB(r1, r2, r6)
//                r4.setColor(r1)
//                goto L_0x0578
//            L_0x0565:
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r1 = r1.counterPaint
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r2 = r2.textPaint
//                int r2 = r2.getColor()
//                r1.setColor(r2)
//            L_0x0578:
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.titleWidth
//                int r3 = r3 + r1
//                r1 = 1086324736(0x40c00000, float:6.0)
//                int r1 = org.telegram.messenger.AndroidUtilities.dp(r1)
//                int r3 = r3 + r1
//                int r1 = r28.getMeasuredHeight()
//                r2 = 1101004800(0x41a00000, float:20.0)
//                int r4 = org.telegram.messenger.AndroidUtilities.dp(r2)
//                int r1 = r1 - r4
//                int r1 = r1 / 2
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.id
//                boolean r2 = org.telegram.messenger.SortingFilter.isSortingFilter(r2)
//                if (r2 != 0) goto L_0x05e1
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r2 = r2.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r4 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r2 == r4) goto L_0x05e1
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.id
//                r4 = 2147483647(0x7fffffff, float:NaN)
//                if (r2 == r4) goto L_0x05d6
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r2 = r2.isEditing
//                if (r2 != 0) goto L_0x05c1
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                float r2 = r2.editingStartAnimationProgress
//                r4 = 0
//                int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
//                if (r2 == 0) goto L_0x05d6
//            L_0x05c1:
//                if (r12 != 0) goto L_0x05d6
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r2 = r2.counterPaint
//                org.telegram.ui.Components.FilterTabsView r4 = org.telegram.ui.Components.FilterTabsView.this
//                float r4 = r4.editingStartAnimationProgress
//                float r4 = r4 * r18
//                int r4 = (int) r4
//                r2.setAlpha(r4)
//                goto L_0x05e1
//            L_0x05d6:
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r2 = r2.counterPaint
//                r4 = 255(0xff, float:3.57E-43)
//                r2.setAlpha(r4)
//            L_0x05e1:
//                android.graphics.RectF r2 = r0.rect
//                float r4 = (float) r3
//                float r6 = (float) r1
//                int r3 = r3 + r5
//                float r3 = (float) r3
//                r5 = 1101004800(0x41a00000, float:20.0)
//                int r5 = org.telegram.messenger.AndroidUtilities.dp(r5)
//                int r5 = r5 + r1
//                float r5 = (float) r5
//                r2.set(r4, r6, r3, r5)
//                android.graphics.RectF r2 = r0.rect
//                float r3 = org.telegram.messenger.AndroidUtilities.density
//                r4 = 1094189056(0x41380000, float:11.5)
//                float r5 = r3 * r4
//                float r3 = r3 * r4
//                org.telegram.ui.Components.FilterTabsView r4 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r4 = r4.counterPaint
//                r7.drawRoundRect(r2, r5, r3, r4)
//                if (r12 == 0) goto L_0x0657
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.id
//                boolean r2 = org.telegram.messenger.SortingFilter.isSortingFilter(r2)
//                if (r2 != 0) goto L_0x0639
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r2 = r2.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r3 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r2 == r3) goto L_0x0639
//                org.telegram.ui.Components.FilterTabsView$Tab r2 = r0.currentTab
//                int r2 = r2.id
//                r3 = 2147483647(0x7fffffff, float:NaN)
//                if (r2 == r3) goto L_0x0639
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r2 = r2.textCounterPaint
//                org.telegram.ui.Components.FilterTabsView r3 = org.telegram.ui.Components.FilterTabsView.this
//                float r3 = r3.editingStartAnimationProgress
//                r4 = 1065353216(0x3f800000, float:1.0)
//                float r3 = r4 - r3
//                float r3 = r3 * r18
//                int r3 = (int) r3
//                r2.setAlpha(r3)
//            L_0x0639:
//                android.graphics.RectF r2 = r0.rect
//                float r3 = r2.left
//                float r2 = r2.width()
//                float r4 = (float) r14
//                float r2 = r2 - r4
//                float r2 = r2 / r16
//                float r3 = r3 + r2
//                r2 = 1097334784(0x41680000, float:14.5)
//                int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
//                int r1 = r1 + r2
//                float r1 = (float) r1
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r2 = r2.textCounterPaint
//                r7.drawText(r12, r3, r1, r2)
//            L_0x0657:
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.id
//                boolean r1 = org.telegram.messenger.SortingFilter.isSortingFilter(r1)
//                if (r1 != 0) goto L_0x0706
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r1 = r1.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r2 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r1 == r2) goto L_0x0706
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.id
//                r2 = 2147483647(0x7fffffff, float:NaN)
//                if (r1 == r2) goto L_0x0706
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                boolean r1 = r1.isEditing
//                if (r1 != 0) goto L_0x0687
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                float r1 = r1.editingStartAnimationProgress
//                r2 = 0
//                int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
//                if (r1 == 0) goto L_0x0706
//            L_0x0687:
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r1 = r1.deletePaint
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                android.text.TextPaint r2 = r2.textCounterPaint
//                int r2 = r2.getColor()
//                r1.setColor(r2)
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r1 = r1.deletePaint
//                org.telegram.ui.Components.FilterTabsView r2 = org.telegram.ui.Components.FilterTabsView.this
//                float r2 = r2.editingStartAnimationProgress
//                float r2 = r2 * r18
//                int r2 = (int) r2
//                r1.setAlpha(r2)
//                int r1 = org.telegram.messenger.AndroidUtilities.dp(r17)
//                android.graphics.RectF r2 = r0.rect
//                float r2 = r2.centerX()
//                float r8 = (float) r1
//                float r2 = r2 - r8
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerY()
//                float r3 = r1 - r8
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerX()
//                float r4 = r1 + r8
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerY()
//                float r5 = r1 + r8
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r6 = r1.deletePaint
//                r1 = r29
//                r1.drawLine(r2, r3, r4, r5, r6)
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerX()
//                float r2 = r1 - r8
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerY()
//                float r3 = r1 + r8
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerX()
//                float r4 = r1 + r8
//                android.graphics.RectF r1 = r0.rect
//                float r1 = r1.centerY()
//                float r5 = r1 - r8
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                android.graphics.Paint r6 = r1.deletePaint
//                r1 = r29
//                r1.drawLine(r2, r3, r4, r5, r6)
//            L_0x0706:
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.id
//                boolean r1 = org.telegram.messenger.SortingFilter.isSortingFilter(r1)
//                if (r1 != 0) goto L_0x0731
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                org.telegram.ui.Components.FilterTabsView$TabMode r1 = r1.mode
//                org.telegram.ui.Components.FilterTabsView$TabMode r2 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//                if (r1 == r2) goto L_0x0731
//                org.telegram.ui.Components.FilterTabsView$Tab r1 = r0.currentTab
//                int r1 = r1.id
//                r2 = 2147483647(0x7fffffff, float:NaN)
//                if (r1 == r2) goto L_0x0731
//                org.telegram.ui.Components.FilterTabsView r1 = org.telegram.ui.Components.FilterTabsView.this
//                float r1 = r1.editingAnimationProgress
//                r2 = 0
//                int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
//                if (r1 == 0) goto L_0x0731
//                r29.restore()
//            L_0x0731:
//                return
//            */
//            throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.FilterTabsView.TabView.onDraw(android.graphics.Canvas):void");
//        }
//    }
//
//    public FilterTabsView(Context context) {
//        super(context);
//        this.mode = TabMode.FOLDERS;
//        float dpf2 = AndroidUtilities.dpf2(3.0f);
//        this.rad = dpf2;
//        this.lineCornerRadiiForBottom = new float[]{0.0f, 0.0f, 0.0f, 0.0f, dpf2, dpf2, dpf2, dpf2};
//        this.lineCornerRadiiForTop = new float[]{dpf2, dpf2, dpf2, dpf2, 0.0f, 0.0f, 0.0f, 0.0f};
//        this.textPaint = new TextPaint(1);
//        this.textCounterPaint = new TextPaint(1);
//        this.deletePaint = new TextPaint(1);
//        this.counterPaint = new Paint(1);
//        this.tabs = new ArrayList<>();
//        this.crossfadePaint = new Paint();
//        this.selectedTabId = -1;
//        this.manualScrollingToPosition = -1;
//        this.manualScrollingToId = -1;
//        this.scrollingToChild = -1;
//        this.tabLineColorKey = "actionBarTabLine";
//        this.activeTextColorKey = "actionBarTabActiveText";
//        this.unactiveTextColorKey = "actionBarTabUnactiveText";
//        this.selectorColorKey = "actionBarTabSelector";
//        this.backgroundColorKey = "actionBarDefault";
//        this.interpolator = CubicBezierInterpolator.EASE_OUT_QUINT;
//        this.positionToId = new SparseIntArray(5);
//        this.idToPosition = new SparseIntArray(5);
//        this.positionToWidth = new SparseIntArray(5);
//        this.positionToX = new SparseIntArray(5);
//        this.animationRunnable = new Runnable() {
//            public void run() {
//                if (FilterTabsView.this.animatingIndicator) {
//                    long elapsedRealtime = SystemClock.elapsedRealtime() - FilterTabsView.this.lastAnimationTime;
//                    if (elapsedRealtime > 17) {
//                        elapsedRealtime = 17;
//                    }
//                    FilterTabsView filterTabsView = FilterTabsView.this;
//                    float unused = filterTabsView.animationTime = filterTabsView.animationTime + (((float) elapsedRealtime) / 200.0f);
//                    FilterTabsView filterTabsView2 = FilterTabsView.this;
//                    filterTabsView2.setAnimationIdicatorProgress(filterTabsView2.interpolator.getInterpolation(FilterTabsView.this.animationTime));
//                    if (FilterTabsView.this.animationTime > 1.0f) {
//                        float unused2 = FilterTabsView.this.animationTime = 1.0f;
//                    }
//                    if (FilterTabsView.this.animationTime < 1.0f) {
//                        AndroidUtilities.runOnUIThread(FilterTabsView.this.animationRunnable);
//                        return;
//                    }
//                    boolean unused3 = FilterTabsView.this.animatingIndicator = false;
//                    FilterTabsView.this.setEnabled(true);
//                    if (FilterTabsView.this.delegate != null) {
//                        FilterTabsView.this.delegate.onPageScrolled(1.0f);
//                    }
//                }
//            }
//        };
//        this.COLORS = new AnimationProperties.FloatProperty<FilterTabsView>("animationValue") {
//            public void setValue(FilterTabsView filterTabsView, float f) {
//                float unused = FilterTabsView.this.animationValue = f;
//                FilterTabsView.this.selectorDrawable.setColor(ColorUtils.blendARGB(Theme.getColor(FilterTabsView.this.tabLineColorKey), Theme.getColor(FilterTabsView.this.aTabLineColorKey), f));
//                if (FilterTabsView.this.aBackgroundColorKey != null && FilterTabsView.this.isWithBackground()) {
//                    filterTabsView.setBackgroundColor(ColorUtils.blendARGB(Theme.getColor(FilterTabsView.this.backgroundColorKey), Theme.getColor(FilterTabsView.this.aBackgroundColorKey), f));
//                }
//                FilterTabsView.this.listView.invalidateViews();
//                FilterTabsView.this.listView.invalidate();
//                filterTabsView.invalidate();
//            }
//
//            public Float get(FilterTabsView filterTabsView) {
//                return Float.valueOf(FilterTabsView.this.animationValue);
//            }
//        };
//        this.userConfig = UserConfig.getInstance(UserConfig.selectedAccount);
//        this.textCounterPaint.setTextSize((float) AndroidUtilities.dp(13.0f));
//        this.textCounterPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        this.textPaint.setTextSize((float) AndroidUtilities.dp(15.0f));
//        this.textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//        this.deletePaint.setStyle(Paint.Style.STROKE);
//        this.deletePaint.setStrokeCap(Paint.Cap.ROUND);
//        this.deletePaint.setStrokeWidth((float) AndroidUtilities.dp(1.5f));
//        this.selectorDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, (int[]) null);
//        updateLineCornerRadii();
//        this.selectorDrawable.setColor(Theme.getColor(this.tabLineColorKey));
//        setHorizontalScrollBarEnabled(false);
//        AnonymousClass3 r1 = new RecyclerListView(context) {
//            public void setAlpha(float f) {
//                super.setAlpha(f);
//                FilterTabsView.this.invalidate();
//            }
//
//            /* access modifiers changed from: protected */
//            public boolean allowSelectChildAtPosition(View view) {
//                return FilterTabsView.this.isEnabled() && FilterTabsView.this.delegate.canPerformActions();
//            }
//
//            /* access modifiers changed from: protected */
//            public boolean canHighlightChildAt(View view, float f, float f2) {
//                if (FilterTabsView.this.isEditing) {
//                    TabView tabView = (TabView) view;
//                    float dp = (float) AndroidUtilities.dp(6.0f);
//                    if (tabView.rect.left - dp < f && tabView.rect.right + dp > f) {
//                        return false;
//                    }
//                }
//                return super.canHighlightChildAt(view, f, f2);
//            }
//        };
//        this.listView = r1;
//        ((DefaultItemAnimator) r1.getItemAnimator()).setDelayAnimations(false);
//        this.listView.setSelectorType(7);
//        this.listView.setSelectorDrawableColor(Theme.getColor(this.selectorColorKey));
//        RecyclerListView recyclerListView = this.listView;
//        AnonymousClass4 r2 = new LinearLayoutManager(context, 0, false) {
//            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int i) {
//                AnonymousClass1 r2 = new LinearSmoothScroller(recyclerView.getContext()) {
//                    /* access modifiers changed from: protected */
//                    public void onTargetFound(View view, RecyclerView.State state, RecyclerView.SmoothScroller.Action action) {
//                        int calculateDxToMakeVisible = calculateDxToMakeVisible(view, getHorizontalSnapPreference());
//                        if (calculateDxToMakeVisible > 0 || (calculateDxToMakeVisible == 0 && view.getLeft() - AndroidUtilities.dp(21.0f) < 0)) {
//                            calculateDxToMakeVisible += AndroidUtilities.dp(60.0f);
//                        } else if (calculateDxToMakeVisible < 0 || (calculateDxToMakeVisible == 0 && view.getRight() + AndroidUtilities.dp(21.0f) > FilterTabsView.this.getMeasuredWidth())) {
//                            calculateDxToMakeVisible -= AndroidUtilities.dp(60.0f);
//                        }
//                        int calculateDyToMakeVisible = calculateDyToMakeVisible(view, getVerticalSnapPreference());
//                        int max = Math.max(180, calculateTimeForDeceleration((int) Math.sqrt((double) ((calculateDxToMakeVisible * calculateDxToMakeVisible) + (calculateDyToMakeVisible * calculateDyToMakeVisible)))));
//                        if (max > 0) {
//                            action.update(-calculateDxToMakeVisible, -calculateDyToMakeVisible, max, this.mDecelerateInterpolator);
//                        }
//                    }
//                };
//                r2.setTargetPosition(i);
//                startSmoothScroll(r2);
//            }
//
//            public int scrollHorizontallyBy(int i, RecyclerView.Recycler recycler, RecyclerView.State state) {
//                if (FilterTabsView.this.delegate.isTabMenuVisible()) {
//                    i = 0;
//                }
//                return super.scrollHorizontallyBy(i, recycler, state);
//            }
//        };
//        this.layoutManager = r2;
//        recyclerListView.setLayoutManager(r2);
//        new ItemTouchHelper(new TouchHelperCallback()).attachToRecyclerView(this.listView);
//        this.listView.setPadding(AndroidUtilities.dp(7.0f), 0, AndroidUtilities.dp(7.0f), 0);
//        this.listView.setClipToPadding(false);
//        this.listView.setDrawSelectorBehind(true);
//        RecyclerListView recyclerListView2 = this.listView;
//        ListAdapter listAdapter = new ListAdapter(context);
//        this.adapter = listAdapter;
//        recyclerListView2.setAdapter(listAdapter);
//        this.listView.setOnItemClickListener((RecyclerListView.OnItemClickListenerExtended) new RecyclerListView.OnItemClickListenerExtended() {
//            public final void onItemClick(View view, int i, float f, float f2) {
//                FilterTabsView.this.lambda$new$0$FilterTabsView(view, i, f, f2);
//            }
//        });
//        this.listView.setOnItemLongClickListener((RecyclerListView.OnItemLongClickListener) new RecyclerListView.OnItemLongClickListener() {
//            public final boolean onItemClick(View view, int i) {
//                return FilterTabsView.this.lambda$new$1$FilterTabsView(view, i);
//            }
//        });
//        this.listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
//                FilterTabsView.this.invalidate();
//            }
//        });
//        addView(this.listView, LayoutHelper.createFrame(-1, -1.0f));
//    }
//
//    /* access modifiers changed from: private */
//    /* renamed from: lambda$new$0 */
//    public /* synthetic */ void lambda$new$0$FilterTabsView(View view, int i, float f, float f2) {
//        FilterTabsViewDelegate filterTabsViewDelegate;
//        TabMode tabMode;
//        if (this.delegate.canPerformActions()) {
//            TabView tabView = (TabView) view;
//            if (this.isEditing) {
//                if (!SortingFilter.isSortingFilter(tabView.currentTab.id) && (tabMode = this.mode) != TabMode.ARCHIVE) {
//                    if ((tabMode == TabMode.FOLDERS && this.userConfig.disableAllChatsTab) || i != 0) {
//                        float dp = (float) AndroidUtilities.dp(6.0f);
//                        if (tabView.rect.left - dp < f && tabView.rect.right + dp > f) {
//                            this.delegate.onDeletePressed(tabView.currentTab.id);
//                        }
//                    }
//                }
//            } else if (i != this.currentPosition || (filterTabsViewDelegate = this.delegate) == null) {
//                scrollToTab(tabView.currentTab.id, i);
//            } else {
//                filterTabsViewDelegate.onSamePageSelected();
//            }
//        }
//    }
//
//    /* access modifiers changed from: private */
//    /* renamed from: lambda$new$1 */
//    public /* synthetic */ boolean lambda$new$1$FilterTabsView(View view, int i) {
//        if (this.delegate.canPerformActions() && !this.isEditing) {
//            if (this.delegate.didSelectTab((TabView) view, i == this.currentPosition)) {
//                this.listView.hideSelector(true);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void setDelegate(FilterTabsViewDelegate filterTabsViewDelegate) {
//        this.delegate = filterTabsViewDelegate;
//    }
//
//    public boolean isAnimatingIndicator() {
//        return this.animatingIndicator;
//    }
//
//    private void scrollToTab(int i, int i2) {
//        int i3 = this.currentPosition;
//        boolean z = i3 < i2;
//        this.scrollingToChild = -1;
//        this.previousPosition = i3;
//        this.previousId = this.selectedTabId;
//        this.currentPosition = i2;
//        this.selectedTabId = i;
//        if (this.animatingIndicator) {
//            AndroidUtilities.cancelRunOnUIThread(this.animationRunnable);
//            this.animatingIndicator = false;
//        }
//        this.animationTime = BitmapDescriptorFactory.HUE_RED;
//        this.animatingIndicatorProgress = BitmapDescriptorFactory.HUE_RED;
//        notifySwipeProgressChanged();
//        this.animatingIndicator = true;
//        setEnabled(false);
//        AndroidUtilities.runOnUIThread(this.animationRunnable, 16);
//        FilterTabsViewDelegate filterTabsViewDelegate = this.delegate;
//        if (filterTabsViewDelegate != null) {
//            filterTabsViewDelegate.onTabSelected(i);
//            this.delegate.onPageSelected(i, z);
//        }
//        scrollToChild(i2);
//    }
//
//    public void selectFirstTab() {
//        scrollToTab(this.tabs.get(0).id, 0);
//    }
//
//    public void setAnimationIdicatorProgress(float f) {
//        this.animatingIndicatorProgress = f;
//        notifySwipeProgressChanged();
//        this.listView.invalidateViews();
//        invalidate();
//        FilterTabsViewDelegate filterTabsViewDelegate = this.delegate;
//        if (filterTabsViewDelegate != null) {
//            filterTabsViewDelegate.onPageScrolled(f);
//        }
//    }
//
//    public Drawable getSelectorDrawable() {
//        return this.selectorDrawable;
//    }
//
//    public RecyclerListView getTabsContainer() {
//        return this.listView;
//    }
//
//    public int getNextPageId(boolean z) {
//        return this.positionToId.get(this.currentPosition + (z ? 1 : -1), -1);
//    }
//
//    public void removeTabs() {
//        this.tabs.clear();
//        this.positionToId.clear();
//        this.idToPosition.clear();
//        this.positionToWidth.clear();
//        this.positionToX.clear();
//        this.allTabsWidth = 0;
//    }
//
//    public void resetTabId() {
//        this.selectedTabId = -1;
//    }
//
//    public void beginCrossfade() {
//        try {
//            Bitmap createBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//            draw(new Canvas(createBitmap));
//            this.crossfadeBitmap = createBitmap;
//            this.crossfadeAlpha = 1.0f;
//            this.commitCrossfade = false;
//            this.listView.invalidate();
//            invalidate();
//        } catch (Throwable th) {
//            FileLog.e(th);
//        }
//    }
//
//    public void commitCrossfade() {
//        if (this.crossfadeBitmap != null) {
//            this.commitCrossfade = true;
//            this.listView.invalidate();
//            invalidate();
//        }
//    }
//
//    public void addTab(int i, String str) {
//        int size = this.tabs.size();
//        if (size == 0 && this.selectedTabId == -1) {
//            this.selectedTabId = i;
//        }
//        this.positionToId.put(size, i);
//        this.idToPosition.put(i, size);
//        int i2 = this.selectedTabId;
//        if (i2 != -1 && i2 == i) {
//            this.currentPosition = size;
//        }
//        Tab tab = new Tab(i, str);
//        this.allTabsWidth += tab.getWidth(true) + AndroidUtilities.dp(32.0f);
//        this.tabs.add(tab);
//    }
//
//    public void finishAddingTabs() {
//        this.adapter.notifyDataSetChanged();
//    }
//
//    public void animateColorsTo(String str, String str2, String str3, String str4, String str5) {
//        AnimatorSet animatorSet = this.colorChangeAnimator;
//        if (animatorSet != null) {
//            animatorSet.cancel();
//        }
//        this.aTabLineColorKey = str;
//        this.aActiveTextColorKey = str2;
//        this.aUnactiveTextColorKey = str3;
//        this.aBackgroundColorKey = str5;
//        this.selectorColorKey = str4;
//        this.listView.setSelectorDrawableColor(Theme.getColor(str4));
//        AnimatorSet animatorSet2 = new AnimatorSet();
//        this.colorChangeAnimator = animatorSet2;
//        animatorSet2.playTogether(new Animator[]{ObjectAnimator.ofFloat(this, this.COLORS, new float[]{BitmapDescriptorFactory.HUE_RED, 1.0f})});
//        this.colorChangeAnimator.setDuration(200);
//        this.colorChangeAnimator.addListener(new AnimatorListenerAdapter() {
//            public void onAnimationEnd(Animator animator) {
//                FilterTabsView filterTabsView = FilterTabsView.this;
//                String unused = filterTabsView.tabLineColorKey = filterTabsView.aTabLineColorKey;
//                FilterTabsView filterTabsView2 = FilterTabsView.this;
//                String unused2 = filterTabsView2.backgroundColorKey = filterTabsView2.aBackgroundColorKey;
//                FilterTabsView filterTabsView3 = FilterTabsView.this;
//                String unused3 = filterTabsView3.activeTextColorKey = filterTabsView3.aActiveTextColorKey;
//                FilterTabsView filterTabsView4 = FilterTabsView.this;
//                String unused4 = filterTabsView4.unactiveTextColorKey = filterTabsView4.aUnactiveTextColorKey;
//                String unused5 = FilterTabsView.this.aTabLineColorKey = null;
//                String unused6 = FilterTabsView.this.aActiveTextColorKey = null;
//                String unused7 = FilterTabsView.this.aUnactiveTextColorKey = null;
//                String unused8 = FilterTabsView.this.aBackgroundColorKey = null;
//            }
//        });
//        this.colorChangeAnimator.start();
//    }
//
//    public int getCurrentTabId() {
//        return this.selectedTabId;
//    }
//
//    public int getFirstTabId() {
//        return this.positionToId.get(0, 0);
//    }
//
//    /* access modifiers changed from: private */
//    public void updateTabsWidths() {
//        this.positionToX.clear();
//        this.positionToWidth.clear();
//        int dp = AndroidUtilities.dp(7.0f);
//        int size = this.tabs.size();
//        for (int i = 0; i < size; i++) {
//            int width = this.tabs.get(i).getWidth(false);
//            this.positionToWidth.put(i, width);
//            this.positionToX.put(i, (this.additionalTabWidth / 2) + dp);
//            dp += width + AndroidUtilities.dp(32.0f) + this.additionalTabWidth;
//        }
//    }
//
//    /* access modifiers changed from: protected */
//    /* JADX WARNING: Removed duplicated region for block: B:103:0x01db  */
//    /* JADX WARNING: Removed duplicated region for block: B:105:0x01de  */
//    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e4  */
//    /* JADX WARNING: Removed duplicated region for block: B:47:0x0116  */
//    /* JADX WARNING: Removed duplicated region for block: B:98:0x01c3  */
//    /* Code decompiled incorrectly, please refer to instructions dump. */
//    public boolean drawChild(android.graphics.Canvas r11, android.view.View r12, long r13) {
//        /*
//            r10 = this;
//            boolean r13 = super.drawChild(r11, r12, r13)
//            org.telegram.ui.Components.RecyclerListView r14 = r10.listView
//            r0 = 0
//            r1 = 0
//            if (r12 != r14) goto L_0x0126
//            int r12 = r10.getMeasuredHeight()
//            android.graphics.drawable.GradientDrawable r14 = r10.selectorDrawable
//            org.telegram.ui.Components.RecyclerListView r2 = r10.listView
//            float r2 = r2.getAlpha()
//            r3 = 1132396544(0x437f0000, float:255.0)
//            float r2 = r2 * r3
//            int r2 = (int) r2
//            r14.setAlpha(r2)
//            boolean r14 = r10.animatingIndicator
//            r2 = -1
//            if (r14 != 0) goto L_0x0076
//            int r14 = r10.manualScrollingToPosition
//            if (r14 == r2) goto L_0x0027
//            goto L_0x0076
//        L_0x0027:
//            org.telegram.ui.Components.RecyclerListView r14 = r10.listView
//            int r2 = r10.currentPosition
//            androidx.recyclerview.widget.RecyclerView$ViewHolder r14 = r14.findViewHolderForAdapterPosition(r2)
//            if (r14 == 0) goto L_0x0073
//            android.view.View r14 = r14.itemView
//            org.telegram.ui.Components.FilterTabsView$TabView r14 = (org.telegram.ui.Components.FilterTabsView.TabView) r14
//            org.telegram.ui.Components.FilterTabsView$TabMode r2 = r10.mode
//            org.telegram.ui.Components.FilterTabsView$TabMode r4 = org.telegram.ui.Components.FilterTabsView.TabMode.FOLDERS
//            if (r2 == r4) goto L_0x003f
//            org.telegram.ui.Components.FilterTabsView$TabMode r4 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//            if (r2 != r4) goto L_0x0047
//        L_0x003f:
//            org.telegram.messenger.UserConfig r4 = r10.userConfig
//            org.telegram.ui.FilterTabNotificationMode r4 = r4.notificationMode
//            org.telegram.ui.FilterTabNotificationMode r5 = org.telegram.ui.FilterTabNotificationMode.NUMBER
//            if (r4 != r5) goto L_0x005f
//        L_0x0047:
//            org.telegram.ui.Components.FilterTabsView$TabMode r4 = org.telegram.ui.Components.FilterTabsView.TabMode.CLOUD
//            if (r2 == r4) goto L_0x005f
//            org.telegram.ui.Components.FilterTabsView$TabMode r4 = org.telegram.ui.Components.FilterTabsView.TabMode.FORWARD
//            if (r2 != r4) goto L_0x0050
//            goto L_0x005f
//        L_0x0050:
//            r2 = 1109393408(0x42200000, float:40.0)
//            int r2 = org.telegram.messenger.AndroidUtilities.dp(r2)
//            int r4 = r14.tabWidth
//            int r2 = java.lang.Math.max(r2, r4)
//            goto L_0x0063
//        L_0x005f:
//            int r2 = r14.tabWidth
//        L_0x0063:
//            float r4 = r14.getX()
//            int r14 = r14.getMeasuredWidth()
//            int r14 = r14 - r2
//            int r14 = r14 / 2
//            float r14 = (float) r14
//            float r4 = r4 + r14
//            int r14 = (int) r4
//            goto L_0x00e2
//        L_0x0073:
//            r14 = r0
//            r2 = r14
//            goto L_0x00e2
//        L_0x0076:
//            androidx.recyclerview.widget.LinearLayoutManager r14 = r10.layoutManager
//            int r14 = r14.findFirstVisibleItemPosition()
//            if (r14 == r2) goto L_0x0073
//            org.telegram.ui.Components.RecyclerListView r2 = r10.listView
//            androidx.recyclerview.widget.RecyclerView$ViewHolder r2 = r2.findViewHolderForAdapterPosition(r14)
//            if (r2 == 0) goto L_0x0073
//            boolean r4 = r10.animatingIndicator
//            if (r4 == 0) goto L_0x008f
//            int r4 = r10.previousPosition
//            int r5 = r10.currentPosition
//            goto L_0x0093
//        L_0x008f:
//            int r4 = r10.currentPosition
//            int r5 = r10.manualScrollingToPosition
//        L_0x0093:
//            android.util.SparseIntArray r6 = r10.positionToX
//            int r6 = r6.get(r4)
//            android.util.SparseIntArray r7 = r10.positionToX
//            int r7 = r7.get(r5)
//            android.util.SparseIntArray r8 = r10.positionToWidth
//            int r4 = r8.get(r4)
//            android.util.SparseIntArray r8 = r10.positionToWidth
//            int r5 = r8.get(r5)
//            int r8 = r10.additionalTabWidth
//            r9 = 1098907648(0x41800000, float:16.0)
//            if (r8 == 0) goto L_0x00bf
//            float r14 = (float) r6
//            int r7 = r7 - r6
//            float r2 = (float) r7
//            float r6 = r10.animatingIndicatorProgress
//            float r2 = r2 * r6
//            float r14 = r14 + r2
//            int r14 = (int) r14
//            int r2 = org.telegram.messenger.AndroidUtilities.dp(r9)
//            int r14 = r14 + r2
//            goto L_0x00da
//        L_0x00bf:
//            android.util.SparseIntArray r8 = r10.positionToX
//            int r14 = r8.get(r14)
//            float r8 = (float) r6
//            int r7 = r7 - r6
//            float r6 = (float) r7
//            float r7 = r10.animatingIndicatorProgress
//            float r6 = r6 * r7
//            float r8 = r8 + r6
//            int r6 = (int) r8
//            android.view.View r2 = r2.itemView
//            int r2 = r2.getLeft()
//            int r14 = r14 - r2
//            int r6 = r6 - r14
//            int r14 = org.telegram.messenger.AndroidUtilities.dp(r9)
//            int r14 = r14 + r6
//        L_0x00da:
//            float r2 = (float) r4
//            int r5 = r5 - r4
//            float r4 = (float) r5
//            float r5 = r10.animatingIndicatorProgress
//            float r4 = r4 * r5
//            float r2 = r2 + r4
//            int r2 = (int) r2
//        L_0x00e2:
//            if (r2 == 0) goto L_0x0112
//            org.telegram.ui.Components.FilterTabsView$TabMode r4 = r10.mode
//            org.telegram.ui.Components.FilterTabsView$TabMode r5 = org.telegram.ui.Components.FilterTabsView.TabMode.FOLDERS
//            r6 = 1082130432(0x40800000, float:4.0)
//            if (r4 == r5) goto L_0x00f0
//            org.telegram.ui.Components.FilterTabsView$TabMode r5 = org.telegram.ui.Components.FilterTabsView.TabMode.ARCHIVE
//            if (r4 != r5) goto L_0x0101
//        L_0x00f0:
//            org.telegram.messenger.UserConfig r4 = r10.userConfig
//            boolean r4 = r4.filterTabsAtBottom
//            if (r4 == 0) goto L_0x0101
//            android.graphics.drawable.GradientDrawable r12 = r10.selectorDrawable
//            int r2 = r2 + r14
//            int r4 = org.telegram.messenger.AndroidUtilities.dpr(r6)
//            r12.setBounds(r14, r0, r2, r4)
//            goto L_0x010d
//        L_0x0101:
//            android.graphics.drawable.GradientDrawable r4 = r10.selectorDrawable
//            int r5 = org.telegram.messenger.AndroidUtilities.dpr(r6)
//            int r5 = r12 - r5
//            int r2 = r2 + r14
//            r4.setBounds(r14, r5, r2, r12)
//        L_0x010d:
//            android.graphics.drawable.GradientDrawable r12 = r10.selectorDrawable
//            r12.draw(r11)
//        L_0x0112:
//            android.graphics.Bitmap r12 = r10.crossfadeBitmap
//            if (r12 == 0) goto L_0x0126
//            android.graphics.Paint r12 = r10.crossfadePaint
//            float r14 = r10.crossfadeAlpha
//            float r14 = r14 * r3
//            int r14 = (int) r14
//            r12.setAlpha(r14)
//            android.graphics.Bitmap r12 = r10.crossfadeBitmap
//            android.graphics.Paint r14 = r10.crossfadePaint
//            r11.drawBitmap(r12, r1, r1, r14)
//        L_0x0126:
//            long r11 = android.os.SystemClock.elapsedRealtime()
//            r2 = 17
//            long r4 = r10.lastEditingAnimationTime
//            long r4 = r11 - r4
//            long r2 = java.lang.Math.min(r2, r4)
//            r10.lastEditingAnimationTime = r11
//            boolean r11 = r10.isEditing
//            r12 = 1065353216(0x3f800000, float:1.0)
//            r14 = 1
//            if (r11 != 0) goto L_0x0146
//            float r4 = r10.editingAnimationProgress
//            int r4 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1))
//            if (r4 == 0) goto L_0x0144
//            goto L_0x0146
//        L_0x0144:
//            r4 = r0
//            goto L_0x0194
//        L_0x0146:
//            boolean r4 = r10.editingForwardAnimation
//            r5 = 1123024896(0x42f00000, float:120.0)
//            if (r4 == 0) goto L_0x016f
//            float r4 = r10.editingAnimationProgress
//            int r6 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1))
//            if (r6 > 0) goto L_0x0154
//            r6 = r14
//            goto L_0x0155
//        L_0x0154:
//            r6 = r0
//        L_0x0155:
//            float r7 = (float) r2
//            float r7 = r7 / r5
//            float r4 = r4 + r7
//            r10.editingAnimationProgress = r4
//            if (r11 != 0) goto L_0x0164
//            if (r6 == 0) goto L_0x0164
//            int r4 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1))
//            if (r4 < 0) goto L_0x0164
//            r10.editingAnimationProgress = r1
//        L_0x0164:
//            float r4 = r10.editingAnimationProgress
//            int r4 = (r4 > r12 ? 1 : (r4 == r12 ? 0 : -1))
//            if (r4 < 0) goto L_0x0193
//            r10.editingAnimationProgress = r12
//            r10.editingForwardAnimation = r0
//            goto L_0x0193
//        L_0x016f:
//            float r4 = r10.editingAnimationProgress
//            int r6 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1))
//            if (r6 < 0) goto L_0x0177
//            r6 = r14
//            goto L_0x0178
//        L_0x0177:
//            r6 = r0
//        L_0x0178:
//            float r7 = (float) r2
//            float r7 = r7 / r5
//            float r4 = r4 - r7
//            r10.editingAnimationProgress = r4
//            if (r11 != 0) goto L_0x0187
//            if (r6 == 0) goto L_0x0187
//            int r4 = (r4 > r1 ? 1 : (r4 == r1 ? 0 : -1))
//            if (r4 > 0) goto L_0x0187
//            r10.editingAnimationProgress = r1
//        L_0x0187:
//            float r4 = r10.editingAnimationProgress
//            r5 = -1082130432(0xffffffffbf800000, float:-1.0)
//            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
//            if (r4 > 0) goto L_0x0193
//            r10.editingAnimationProgress = r5
//            r10.editingForwardAnimation = r14
//        L_0x0193:
//            r4 = r14
//        L_0x0194:
//            r5 = 1127481344(0x43340000, float:180.0)
//            if (r11 == 0) goto L_0x01ab
//            float r11 = r10.editingStartAnimationProgress
//            int r6 = (r11 > r12 ? 1 : (r11 == r12 ? 0 : -1))
//            if (r6 >= 0) goto L_0x01bf
//            float r4 = (float) r2
//            float r4 = r4 / r5
//            float r11 = r11 + r4
//            r10.editingStartAnimationProgress = r11
//            int r11 = (r11 > r12 ? 1 : (r11 == r12 ? 0 : -1))
//            if (r11 <= 0) goto L_0x01a9
//            r10.editingStartAnimationProgress = r12
//        L_0x01a9:
//            r4 = r14
//            goto L_0x01bf
//        L_0x01ab:
//            if (r11 != 0) goto L_0x01bf
//            float r11 = r10.editingStartAnimationProgress
//            int r12 = (r11 > r1 ? 1 : (r11 == r1 ? 0 : -1))
//            if (r12 <= 0) goto L_0x01bf
//            float r12 = (float) r2
//            float r12 = r12 / r5
//            float r11 = r11 - r12
//            r10.editingStartAnimationProgress = r11
//            int r11 = (r11 > r1 ? 1 : (r11 == r1 ? 0 : -1))
//            if (r11 >= 0) goto L_0x01a9
//            r10.editingStartAnimationProgress = r1
//            goto L_0x01a9
//        L_0x01bf:
//            boolean r11 = r10.commitCrossfade
//            if (r11 == 0) goto L_0x01db
//            float r11 = r10.crossfadeAlpha
//            float r12 = (float) r2
//            float r12 = r12 / r5
//            float r11 = r11 - r12
//            r10.crossfadeAlpha = r11
//            int r11 = (r11 > r1 ? 1 : (r11 == r1 ? 0 : -1))
//            if (r11 >= 0) goto L_0x01dc
//            r10.commitCrossfade = r0
//            android.graphics.Bitmap r11 = r10.crossfadeBitmap
//            if (r11 == 0) goto L_0x01dc
//            r11.recycle()
//            r11 = 0
//            r10.crossfadeBitmap = r11
//            goto L_0x01dc
//        L_0x01db:
//            r14 = r4
//        L_0x01dc:
//            if (r14 == 0) goto L_0x01e6
//            org.telegram.ui.Components.RecyclerListView r11 = r10.listView
//            r11.invalidateViews()
//            r10.invalidate()
//        L_0x01e6:
//            return r13
//        */
//        throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.Components.FilterTabsView.drawChild(android.graphics.Canvas, android.view.View, long):boolean");
//    }
//
//    /* access modifiers changed from: protected */
//    public void onMeasure(int i, int i2) {
//        if (!this.tabs.isEmpty()) {
//            int size = (View.MeasureSpec.getSize(i) - AndroidUtilities.dp(7.0f)) - AndroidUtilities.dp(7.0f);
//            Tab tab = this.tabs.get(0);
//            int i3 = tab.id;
//            int i4 = R.string.FilterAllChats;
//            String str = "FilterAllChats";
//            if (i3 == Integer.MAX_VALUE && !this.customForwardArchive) {
//                tab.setTitle(LocaleController.getString(str, R.string.FilterAllChats));
//            }
//            if (tab.id == Integer.MAX_VALUE && this.customForwardArchive) {
//                tab.setTitle(LocaleController.getString("ArchivedChats", R.string.ArchivedChats));
//            }
//            int width = tab.getWidth(false);
//            if (tab.id == Integer.MAX_VALUE && !this.customForwardArchive) {
//                if (this.allTabsWidth > size) {
//                    i4 = R.string.FilterAllChatsShort;
//                    str = "FilterAllChatsShort";
//                }
//                tab.setTitle(LocaleController.getString(str, i4));
//            }
//            if (tab.id == Integer.MAX_VALUE && this.customForwardArchive) {
//                tab.setTitle(LocaleController.getString("ArchivedChats", R.string.ArchivedChats));
//            }
//            int width2 = (this.allTabsWidth - width) + tab.getWidth(false);
//            int i5 = this.additionalTabWidth;
//            int size2 = width2 < size ? (size - width2) / this.tabs.size() : 0;
//            this.additionalTabWidth = size2;
//            if (i5 != size2) {
//                this.ignoreLayout = true;
//                this.adapter.notifyDataSetChanged();
//                this.ignoreLayout = false;
//            }
//            updateTabsWidths();
//            this.invalidated = false;
//        }
//        super.onMeasure(i, i2);
//    }
//
//    public void requestLayout() {
//        if (!this.ignoreLayout) {
//            super.requestLayout();
//        }
//    }
//
//    private void scrollToChild(int i) {
//        if (!this.tabs.isEmpty() && this.scrollingToChild != i && i >= 0 && i < this.tabs.size()) {
//            this.scrollingToChild = i;
//            this.listView.smoothScrollToPosition(i);
//        }
//    }
//
//    /* access modifiers changed from: protected */
//    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
//        super.onLayout(z, i, i2, i3, i4);
//        int i5 = i3 - i;
//        if (this.prevLayoutWidth != i5) {
//            this.prevLayoutWidth = i5;
//            this.scrollingToChild = -1;
//            if (this.animatingIndicator) {
//                AndroidUtilities.cancelRunOnUIThread(this.animationRunnable);
//                this.animatingIndicator = false;
//                setEnabled(true);
//                FilterTabsViewDelegate filterTabsViewDelegate = this.delegate;
//                if (filterTabsViewDelegate != null) {
//                    filterTabsViewDelegate.onPageScrolled(1.0f);
//                }
//            }
//        }
//    }
//
//    public void selectTabWithId(int i, float f) {
//        int i2 = this.idToPosition.get(i, -1);
//        if (i2 >= 0) {
//            if (f < BitmapDescriptorFactory.HUE_RED) {
//                f = 0.0f;
//            } else if (f > 1.0f) {
//                f = 1.0f;
//            }
//            if (f > BitmapDescriptorFactory.HUE_RED) {
//                this.manualScrollingToPosition = i2;
//                this.manualScrollingToId = i;
//            } else {
//                this.manualScrollingToPosition = -1;
//                this.manualScrollingToId = -1;
//            }
//            this.animatingIndicatorProgress = f;
//            notifySwipeProgressChanged();
//            this.listView.invalidateViews();
//            invalidate();
//            scrollToChild(i2);
//            if (f >= 1.0f) {
//                this.manualScrollingToPosition = -1;
//                this.manualScrollingToId = -1;
//                this.currentPosition = i2;
//                if (this.selectedTabId != i) {
//                    this.delegate.onTabSelected(i);
//                }
//                this.selectedTabId = i;
//            }
//        }
//    }
//
//    public boolean isEditing() {
//        return this.isEditing;
//    }
//
//    public void setIsEditing(boolean z) {
//        this.isEditing = z;
//        this.editingForwardAnimation = true;
//        this.listView.invalidateViews();
//        invalidate();
//        if (!this.isEditing && this.orderChanged) {
//            MessagesStorage.getInstance(UserConfig.selectedAccount).saveDialogFiltersOrder();
//            TLRPC$TL_messages_updateDialogFiltersOrder tLRPC$TL_messages_updateDialogFiltersOrder = new TLRPC$TL_messages_updateDialogFiltersOrder();
//            ArrayList<MessagesController.DialogFilter> arrayList = MessagesController.getInstance(UserConfig.selectedAccount).dialogFilters;
//            int size = arrayList.size();
//            for (int i = 0; i < size; i++) {
//                MessagesController.DialogFilter dialogFilter = arrayList.get(i);
//                tLRPC$TL_messages_updateDialogFiltersOrder.order.add(Integer.valueOf(arrayList.get(i).id));
//            }
//            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(tLRPC$TL_messages_updateDialogFiltersOrder, $$Lambda$FilterTabsView$0gw0M2Mpnl7SUImCutpjvb1JvBw.INSTANCE);
//            this.orderChanged = false;
//        }
//    }
//
//    public void checkTabsCounter() {
//        int size = this.tabs.size();
//        int i = 0;
//        boolean z = false;
//        while (true) {
//            if (i >= size) {
//                break;
//            }
//            Tab tab = this.tabs.get(i);
//            if (tab.counter != this.delegate.getTabCounter(tab.id)) {
//                if (this.positionToWidth.get(i) != tab.getWidth(true) || this.invalidated) {
//                    this.invalidated = true;
//                    requestLayout();
//                    this.adapter.notifyDataSetChanged();
//                    this.allTabsWidth = 0;
//                } else {
//                    z = true;
//                }
//            }
//            i++;
//        }
//        this.invalidated = true;
//        requestLayout();
//        this.adapter.notifyDataSetChanged();
//        this.allTabsWidth = 0;
//        if (this.tabs.get(0).id == Integer.MAX_VALUE && this.customForwardArchive) {
//            this.tabs.get(0).setTitle(LocaleController.getString("ArchivedChats", R.string.ArchivedChats));
//        } else if (this.tabs.get(0).id == Integer.MAX_VALUE) {
//            this.tabs.get(0).setTitle(LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
//        }
//        for (int i2 = 0; i2 < size; i2++) {
//            this.allTabsWidth += this.tabs.get(i2).getWidth(true) + AndroidUtilities.dp(32.0f);
//        }
//        z = true;
//        if (z) {
//            this.listView.invalidateViews();
//        }
//    }
//
//    public void notifyTabCounterChanged(int i) {
//        int i2 = this.idToPosition.get(i, -1);
//        if (i2 >= 0 && i2 < this.tabs.size()) {
//            Tab tab = this.tabs.get(i2);
//            if (tab.counter != this.delegate.getTabCounter(tab.id)) {
//                this.listView.invalidateViews();
//                if (this.positionToWidth.get(i2) != tab.getWidth(true) || this.invalidated) {
//                    this.invalidated = true;
//                    requestLayout();
//                    this.adapter.notifyDataSetChanged();
//                    this.allTabsWidth = 0;
//                    if (this.tabs.get(0).id == Integer.MAX_VALUE && this.customForwardArchive) {
//                        this.tabs.get(0).setTitle(LocaleController.getString("ArchivedChats", R.string.ArchivedChats));
//                    } else if (this.tabs.get(0).id == Integer.MAX_VALUE) {
//                        this.tabs.get(0).setTitle(LocaleController.getString("FilterAllChats", R.string.FilterAllChats));
//                    }
//                    int size = this.tabs.size();
//                    for (int i3 = 0; i3 < size; i3++) {
//                        this.allTabsWidth += this.tabs.get(i3).getWidth(true) + AndroidUtilities.dp(32.0f);
//                    }
//                }
//            }
//        }
//    }
//
//    private class ListAdapter extends RecyclerListView.SelectionAdapter {
//        private Context mContext;
//
//        public long getItemId(int i) {
//            return (long) i;
//        }
//
//        public int getItemViewType(int i) {
//            return 0;
//        }
//
//        public boolean isEnabled(RecyclerView.ViewHolder viewHolder) {
//            return true;
//        }
//
//        public ListAdapter(Context context) {
//            this.mContext = context;
//        }
//
//        public int getItemCount() {
//            return FilterTabsView.this.tabs.size();
//        }
//
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//            return new RecyclerListView.Holder(new TabView(this.mContext));
//        }
//
//        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
//            ((TabView) viewHolder.itemView).setTab((Tab) FilterTabsView.this.tabs.get(i), i);
//        }
//
//        public void swapElements(int i, int i2) {
//            int i3 = i - 1;
//            int i4 = i2 - 1;
//            int size = FilterTabsView.this.tabs.size() - 1;
//            if (FilterTabsView.this.userConfig.disableAllChatsTab) {
//                i3++;
//                i4++;
//                size++;
//            }
//            if (FilterTabsView.this.userConfig.sortingChatsEnabled) {
//                int activeTabsCount = FilterTabsView.this.userConfig.sortingTabsManager.getActiveTabsCount();
//                i3 -= activeTabsCount;
//                i4 -= activeTabsCount;
//                size -= activeTabsCount;
//            }
//            if (i3 >= 0 && i4 >= 0 && i3 < size && i4 < size) {
//                ArrayList<MessagesController.DialogFilter> arrayList = MessagesController.getInstance(UserConfig.selectedAccount).dialogFilters;
//                MessagesController.DialogFilter dialogFilter = arrayList.get(i3);
//                MessagesController.DialogFilter dialogFilter2 = arrayList.get(i4);
//                int i5 = dialogFilter.order;
//                dialogFilter.order = dialogFilter2.order;
//                dialogFilter2.order = i5;
//                arrayList.set(i3, dialogFilter2);
//                arrayList.set(i4, dialogFilter);
//                Tab tab = (Tab) FilterTabsView.this.tabs.get(i);
//                Tab tab2 = (Tab) FilterTabsView.this.tabs.get(i2);
//                int i6 = tab.id;
//                tab.id = tab2.id;
//                tab2.id = i6;
//                FilterTabsView.this.delegate.onPageReorder(tab2.id, tab.id);
//                if (FilterTabsView.this.currentPosition == i) {
//                    int unused = FilterTabsView.this.currentPosition = i2;
//                    int unused2 = FilterTabsView.this.selectedTabId = tab.id;
//                } else if (FilterTabsView.this.currentPosition == i2) {
//                    int unused3 = FilterTabsView.this.currentPosition = i;
//                    int unused4 = FilterTabsView.this.selectedTabId = tab2.id;
//                }
//                if (FilterTabsView.this.previousPosition == i) {
//                    int unused5 = FilterTabsView.this.previousPosition = i2;
//                    int unused6 = FilterTabsView.this.previousId = tab.id;
//                } else if (FilterTabsView.this.previousPosition == i2) {
//                    int unused7 = FilterTabsView.this.previousPosition = i;
//                    int unused8 = FilterTabsView.this.previousId = tab2.id;
//                }
//                FilterTabsView.this.tabs.set(i, tab2);
//                FilterTabsView.this.tabs.set(i2, tab);
//                FilterTabsView.this.updateTabsWidths();
//                boolean unused9 = FilterTabsView.this.orderChanged = true;
//                notifyItemMoved(i, i2);
//            }
//        }
//    }
//
//    public class TouchHelperCallback extends ItemTouchHelper.Callback {
//        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
//        }
//
//        public TouchHelperCallback() {
//        }
//
//        public boolean isLongPressDragEnabled() {
//            return FilterTabsView.this.isEditing;
//        }
//
//        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//            View view = viewHolder.itemView;
//            if ((view instanceof TabView) && (SortingFilter.isSortingFilter(((TabView) view).getId()) || FilterTabsView.this.mode == TabMode.ARCHIVE)) {
//                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
//            }
//            if (FilterTabsView.this.userConfig.disableAllChatsTab || (FilterTabsView.this.isEditing && viewHolder.getAdapterPosition() != 0)) {
//                return ItemTouchHelper.Callback.makeMovementFlags(12, 0);
//            }
//            return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
//        }
//
//        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
//            if (!FilterTabsView.this.userConfig.disableAllChatsTab && (viewHolder.getAdapterPosition() == 0 || viewHolder2.getAdapterPosition() == 0)) {
//                return false;
//            }
//            FilterTabsView.this.adapter.swapElements(viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition());
//            return true;
//        }
//
//        public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float f, float f2, int i, boolean z) {
//            super.onChildDraw(canvas, recyclerView, viewHolder, f, f2, i, z);
//        }
//
//        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
//            if (i != 0) {
//                FilterTabsView.this.listView.cancelClickRunnables(false);
//                viewHolder.itemView.setPressed(true);
//                viewHolder.itemView.setBackgroundColor(Theme.getColor(FilterTabsView.this.backgroundColorKey));
//            }
//            super.onSelectedChanged(viewHolder, i);
//        }
//
//        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//            super.clearView(recyclerView, viewHolder);
//            viewHolder.itemView.setPressed(false);
//            viewHolder.itemView.setBackground((Drawable) null);
//        }
//    }
//}