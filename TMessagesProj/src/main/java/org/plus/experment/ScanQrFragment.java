package org.plus.experment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.plus.apps.business.ShopUtils;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.camera.CameraController;
import org.telegram.messenger.camera.CameraSession;
import org.telegram.messenger.camera.CameraView;
import org.telegram.messenger.camera.Size;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScrollSlidingTextTabStrip;
import org.telegram.ui.PhotoAlbumPickerActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@TargetApi(18)
public class ScanQrFragment extends BaseFragment implements Camera.PreviewCallback{

    private static class ViewPage extends FrameLayout{

        private int selectedType;
        private FrameLayout innerFrameLayout;

        public ViewPage( Context context) {
            super(context);
        }
    }

    private Paint backgroundPaint = new Paint();
    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;
    private ViewPage[] viewPages = new ViewPage[2];
    private AnimatorSet tabsAnimation;
    private boolean tabsAnimationInProgress;
    private boolean animatingForward;
    private boolean backAnimation;
    private int maximumVelocity;
    private static final Interpolator interpolator = t -> {
        --t;
        return t * t * t * t * t + 1.0F;
    };



    private CameraView cameraView;
    private TextView titleTextView;
    private TextView descriptionText;
    private HandlerThread backgroundHandlerThread = new HandlerThread("CameraHandlerThread");
    private Handler handler;
    private TextView recognizedMrzView;
    private Paint paint = new Paint();
    private Paint cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path path = new Path();
    private ImageView galleryButton;
    private ImageView flashButton;
    private AnimatorSet flashAnimator;

    private ScanQrFragmentDelegate delegate;
    private boolean recognized;

    private QRCodeReader qrReader;
    private BarcodeDetector visionQrReader;


    public interface ScanQrFragmentDelegate {

        default void didFindQr(String text) {

        }
    }

    private boolean swipeBackEnabled = true;

    public static ActionBarLayout[] showAsSheet(BaseFragment parentFragment, int chat_id,ScanQrFragmentDelegate delegate) {
        if (parentFragment == null || parentFragment.getParentActivity() == null) {
            return null;
        }
        ActionBarLayout[] actionBarLayout = new ActionBarLayout[]{new ActionBarLayout(parentFragment.getParentActivity())};
        BottomSheet bottomSheet = new BottomSheet(parentFragment.getParentActivity(), false) {
            {
                actionBarLayout[0].init(new ArrayList<>());
                ScanQrFragment fragment = new ScanQrFragment(chat_id) {
                    @Override
                    public void finishFragment() {
                        dismiss();
                    }

                    @Override
                    public void removeSelfFromStack() {
                        dismiss();
                    }
                };
                actionBarLayout[0].addFragmentToStack(fragment);
                actionBarLayout[0].showLastFragment();
                actionBarLayout[0].setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, 0);
                fragment.setDelegate(delegate);
                containerView = actionBarLayout[0];
                setApplyBottomPadding(false);
                setApplyBottomPadding(false);
                setOnDismissListener(dialog -> fragment.onFragmentDestroy());
            }

            @Override
            protected boolean canDismissWithSwipe() {
                return false;
            }

            @Override
            public void onBackPressed() {
                if (actionBarLayout[0] == null || actionBarLayout[0].fragmentsStack.size() <= 1) {
                    super.onBackPressed();
                } else {
                    actionBarLayout[0].onBackPressed();
                }
            }

            @Override
            public void dismiss() {
                super.dismiss();
                actionBarLayout[0] = null;
            }
        };

        bottomSheet.show();
        return actionBarLayout;
    }

    private int chat_id;

    public ScanQrFragment(int chat_id) {
        super();
        CameraController.getInstance().initCamera(() -> {
            if (cameraView != null) {
                cameraView.initCamera();
            }
        });
        this.chat_id = chat_id;
        qrReader = new QRCodeReader();
        visionQrReader = new BarcodeDetector.Builder(ApplicationLoader.applicationContext).setBarcodeFormats(Barcode.QR_CODE).build();

    }
    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        if (getParentActivity() != null) {
            getParentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        if (visionQrReader != null) {
             visionQrReader.release();
        }
        super.onFragmentDestroy();
    }



    @Override
    public View createView(Context context) {

        actionBar.setBackButtonImage(R.drawable.edit_cancel);
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        actionBar.setAllowOverlayTitle(false);
        actionBar.setAddToContainer(false);
        actionBar.setClipContent(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        paint.setColor(0x7f000000);
        cornerPaint.setColor(0xffffffff);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(AndroidUtilities.dp(4));
        cornerPaint.setStrokeJoin(Paint.Join.ROUND);


        int initTab = 0;

        ViewConfiguration configuration = ViewConfiguration.get(context);
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();

        hasOwnBackground = true;
        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
        scrollSlidingTextTabStrip.setUseSameWidth(true);
        scrollSlidingTextTabStrip.setInitialTabId(initTab);
        actionBar.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));

        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
            @Override
            public void onPageSelected(int id, boolean forward) {
                if (viewPages[0].selectedType == id) {
                    return;
                }
                swipeBackEnabled = id == scrollSlidingTextTabStrip.getFirstTabId();
                viewPages[1].selectedType = id;
                viewPages[1].setVisibility(View.VISIBLE);
                switchToCurrentSelectedMode(true);
                animatingForward = forward;
            }

            @Override
            public void onPageScrolled(float progress) {
                if (progress == 1 && viewPages[1].getVisibility() != View.VISIBLE) {
                    return;
                }

                if (animatingForward) {
                    viewPages[0].setTranslationX(-progress * viewPages[0].getMeasuredWidth());
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() - progress * viewPages[0].getMeasuredWidth());
                } else {
                    viewPages[0].setTranslationX(progress * viewPages[0].getMeasuredWidth());
                    viewPages[1].setTranslationX(progress * viewPages[0].getMeasuredWidth() - viewPages[0].getMeasuredWidth());
                }

                if (progress == 1) {
                    ViewPage tempPage = viewPages[0];
                    viewPages[0] = viewPages[1];
                    viewPages[1] = tempPage;
                    viewPages[1].setVisibility(View.GONE);
                }
            }
        });

        FrameLayout frameLayout;
        fragmentView = frameLayout = new FrameLayout(context) {

            private int startedTrackingPointerId;
            private boolean startedTracking;
            private boolean maybeStartTracking;
            private int startedTrackingX;
            private int startedTrackingY;
            private VelocityTracker velocityTracker;
            private boolean globalIgnoreLayout;

            private boolean prepareForMoving(MotionEvent ev, boolean forward) {
                int id = scrollSlidingTextTabStrip.getNextPageId(forward);
                if (id < 0) {
                    return false;
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                maybeStartTracking = false;
                startedTracking = true;
                startedTrackingX = (int) ev.getX();
                actionBar.setEnabled(false);
                scrollSlidingTextTabStrip.setEnabled(false);
                viewPages[1].selectedType = id;
                viewPages[1].setVisibility(View.VISIBLE);
                animatingForward = forward;
                switchToCurrentSelectedMode(true);
                if (forward) {
                    viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth());
                } else {
                    viewPages[1].setTranslationX(-viewPages[0].getMeasuredWidth());
                }
                return true;
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int actionBarHeight = actionBar.getMeasuredHeight();
                globalIgnoreLayout = true;
                for (int a = 0; a < viewPages.length; a++) {
                    if (viewPages[a] == null) {
                        continue;
                    }
                    if (viewPages[a]!= null) {
                        viewPages[a].setPadding(0, actionBarHeight, 0, AndroidUtilities.dp(4));
                    }
                }
                globalIgnoreLayout = false;

                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == actionBar) {
                        continue;
                    }
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                }
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (parentLayout != null) {
                    parentLayout.drawHeaderShadow(canvas, actionBar.getMeasuredHeight() + (int) actionBar.getTranslationY());
                }
            }

            @Override
            public void requestLayout() {
                if (globalIgnoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            public boolean checkTabsAnimationInProgress() {
                if (tabsAnimationInProgress) {
                    boolean cancel = false;
                    if (backAnimation) {
                        if (Math.abs(viewPages[0].getTranslationX()) < 1) {
                            viewPages[0].setTranslationX(0);
                            viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? 1 : -1));
                            cancel = true;
                        }
                    } else if (Math.abs(viewPages[1].getTranslationX()) < 1) {
                        viewPages[0].setTranslationX(viewPages[0].getMeasuredWidth() * (animatingForward ? -1 : 1));
                        viewPages[1].setTranslationX(0);
                        cancel = true;
                    }
                    if (cancel) {
                        if (tabsAnimation != null) {
                            tabsAnimation.cancel();
                            tabsAnimation = null;
                        }
                        tabsAnimationInProgress = false;
                    }
                    return tabsAnimationInProgress;
                }
                return false;
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return checkTabsAnimationInProgress() || scrollSlidingTextTabStrip.isAnimatingIndicator() || onTouchEvent(ev);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                backgroundPaint.setColor(Theme.getColor(Theme.key_windowBackgroundGray));
                canvas.drawRect(0, actionBar.getMeasuredHeight() + actionBar.getTranslationY(), getMeasuredWidth(), getMeasuredHeight(), backgroundPaint);
            }

            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                if (!parentLayout.checkTransitionAnimation() && !checkTabsAnimationInProgress()) {
                    if (ev != null) {
                        if (velocityTracker == null) {
                            velocityTracker = VelocityTracker.obtain();
                        }
                        velocityTracker.addMovement(ev);
                    }
                    if (ev != null && ev.getAction() == MotionEvent.ACTION_DOWN && !startedTracking && !maybeStartTracking) {
                        startedTrackingPointerId = ev.getPointerId(0);
                        maybeStartTracking = true;
                        startedTrackingX = (int) ev.getX();
                        startedTrackingY = (int) ev.getY();
                        velocityTracker.clear();
                    } else if (ev != null && ev.getAction() == MotionEvent.ACTION_MOVE && ev.getPointerId(0) == startedTrackingPointerId) {
                        int dx = (int) (ev.getX() - startedTrackingX);
                        int dy = Math.abs((int) ev.getY() - startedTrackingY);
                        if (startedTracking && (animatingForward && dx > 0 || !animatingForward && dx < 0)) {
                            if (!prepareForMoving(ev, dx < 0)) {
                                maybeStartTracking = true;
                                startedTracking = false;
                                viewPages[0].setTranslationX(0);
                                viewPages[1].setTranslationX(animatingForward ? viewPages[0].getMeasuredWidth() : -viewPages[0].getMeasuredWidth());

                                scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, 0);

                            }
                        }
                        if (maybeStartTracking && !startedTracking) {
                            float touchSlop = AndroidUtilities.getPixelsInCM(0.3f, true);
                            if (Math.abs(dx) >= touchSlop && Math.abs(dx) > dy) {
                                prepareForMoving(ev, dx < 0);
                            }
                        } else if (startedTracking) {
                            if (animatingForward) {
                                viewPages[0].setTranslationX(dx);
                                viewPages[1].setTranslationX(viewPages[0].getMeasuredWidth() + dx);

                            } else {
                                viewPages[0].setTranslationX(dx);
                                viewPages[1].setTranslationX(dx - viewPages[0].getMeasuredWidth());

                            }
                            float scrollProgress = Math.abs(dx) / (float) viewPages[0].getMeasuredWidth();
                            scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, scrollProgress);


                        }
                    } else if (ev == null || ev.getPointerId(0) == startedTrackingPointerId && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_POINTER_UP)) {
                        velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                        float velX;
                        float velY;
                        if (ev != null && ev.getAction() != MotionEvent.ACTION_CANCEL) {
                            velX = velocityTracker.getXVelocity();
                            velY = velocityTracker.getYVelocity();
                            if (!startedTracking) {
                                if (Math.abs(velX) >= 3000 && Math.abs(velX) > Math.abs(velY)) {
                                    prepareForMoving(ev, velX < 0);
                                }
                            }
                        } else {
                            velX = 0;
                            velY = 0;
                        }
                        if (startedTracking) {
                            float x = viewPages[0].getX();
                            tabsAnimation = new AnimatorSet();
                            backAnimation = Math.abs(x) < viewPages[0].getMeasuredWidth() / 3.0f && (Math.abs(velX) < 3500 || Math.abs(velX) < Math.abs(velY));
                            float distToMove;
                            float dx;
                            if (backAnimation) {
                                dx = Math.abs(x);
                                if (animatingForward) {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, viewPages[1].getMeasuredWidth())
                                    );
                                } else {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, 0),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, -viewPages[1].getMeasuredWidth())
                                    );
                                }
                            } else {
                                dx = viewPages[0].getMeasuredWidth() - Math.abs(x);
                                if (animatingForward) {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, -viewPages[0].getMeasuredWidth()),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
                                    );
                                } else {
                                    tabsAnimation.playTogether(
                                            ObjectAnimator.ofFloat(viewPages[0], View.TRANSLATION_X, viewPages[0].getMeasuredWidth()),
                                            ObjectAnimator.ofFloat(viewPages[1], View.TRANSLATION_X, 0)
                                    );
                                }
                            }
                            tabsAnimation.setInterpolator(interpolator);

                            int width = getMeasuredWidth();
                            int halfWidth = width / 2;
                            float distanceRatio = Math.min(1.0f, 1.0f * dx / (float) width);
                            float distance = (float) halfWidth + (float) halfWidth * AndroidUtilities.distanceInfluenceForSnapDuration(distanceRatio);
                            velX = Math.abs(velX);
                            int duration;
                            if (velX > 0) {
                                duration = 4 * Math.round(1000.0f * Math.abs(distance / velX));
                            } else {
                                float pageDelta = dx / getMeasuredWidth();
                                duration = (int) ((pageDelta + 1.0f) * 100.0f);
                            }
                            duration = Math.max(150, Math.min(duration, 600));

                            tabsAnimation.setDuration(duration);
                            tabsAnimation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    tabsAnimation = null;
                                    if (backAnimation) {
                                        viewPages[1].setVisibility(View.GONE);
                                    } else {
                                        ViewPage tempPage = viewPages[0];
                                        viewPages[0] = viewPages[1];
                                        viewPages[1] = tempPage;
                                        viewPages[1].setVisibility(View.GONE);
                                        swipeBackEnabled = viewPages[0].selectedType == scrollSlidingTextTabStrip.getFirstTabId();
                                        scrollSlidingTextTabStrip.selectTabWithId(viewPages[0].selectedType, 1.0f);

                                    }
                                    tabsAnimationInProgress = false;
                                    maybeStartTracking = false;
                                    startedTracking = false;
                                    actionBar.setEnabled(true);
                                    scrollSlidingTextTabStrip.setEnabled(true);
                                }
                            });
                            tabsAnimation.start();
                            tabsAnimationInProgress = true;
                            startedTracking = false;
                        } else {
                            maybeStartTracking = false;
                            actionBar.setEnabled(true);
                            scrollSlidingTextTabStrip.setEnabled(true);
                        }
                        if (velocityTracker != null) {
                            velocityTracker.recycle();
                            velocityTracker = null;
                        }
                    }
                    return startedTracking;
                }
                return false;
            }
        };
        frameLayout.setWillNotDraw(false);
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_wallet_blackBackground));


        cameraView = new CameraView(context, false);
        cameraView.setUseMaxPreview(true);
        cameraView.setOptimizeForBarcode(true);
        cameraView.setDelegate(new CameraView.CameraViewDelegate() {
            @Override
            public void onCameraCreated(Camera camera) {

            }

            @Override
            public void onCameraInit() {
                startRecognizing();
            }
        });

        titleTextView = new TextView(context);
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);


        descriptionText = new TextView(context);
        descriptionText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText6));
        descriptionText.setGravity(Gravity.CENTER_HORIZONTAL);
        descriptionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);


        recognizedMrzView = new TextView(context);
        recognizedMrzView.setTextColor(0xffffffff);
        recognizedMrzView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        recognizedMrzView.setAlpha(0);

        //titleTextView.setText(LocaleController.getString("WalletScanCode", R.string.WalletScanCode));
        titleTextView.setTextColor(0xffffffff);
        recognizedMrzView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        recognizedMrzView.setPadding(AndroidUtilities.dp(10), 0, AndroidUtilities.dp(10), AndroidUtilities.dp(10));
        //recognizedMrzView.setText(LocaleController.getString("WalletScanCodeNotFound", R.string.WalletScanCodeNotFound));


        galleryButton = new ImageView(context);
        galleryButton.setScaleType(ImageView.ScaleType.CENTER);
        galleryButton.setImageResource(R.drawable.qr_gallery);
        galleryButton.setBackgroundDrawable(Theme.createSelectorDrawableFromDrawables(Theme.createCircleDrawable(AndroidUtilities.dp(60), 0x22ffffff), Theme.createCircleDrawable(AndroidUtilities.dp(60), 0x44ffffff)));


        galleryButton.setOnClickListener(currentImage -> {
            if (getParentActivity() == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (getParentActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getParentActivity().requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
                    return;
                }
            }
            PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(PhotoAlbumPickerActivity.SELECT_TYPE_QR, false, false, null);
            fragment.setMaxSelectedPhotos(1, false);
            fragment.setAllowSearchImages(false);
            fragment.setDelegate(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {
                @Override
                public void didSelectPhotos(ArrayList<SendMessagesHelper.SendingMediaInfo> photos, boolean notify, int scheduleDate) {
                    try {
                        if (!photos.isEmpty()) {
                            SendMessagesHelper.SendingMediaInfo info = photos.get(0);
                            if (info.path != null) {
                                Point screenSize = AndroidUtilities.getRealScreenSize();
                                Bitmap bitmap = ImageLoader.loadBitmap(info.path, null, screenSize.x, screenSize.y, true);
                                String text = tryReadQr(null, null, 0, 0, 0, bitmap);
                                if (text != null) {
                                    if (delegate != null) {
                                        delegate.didFindQr(text);
                                    }
                                    removeSelfFromStack();
                                }
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.e(e);
                    }
                }

                @Override
                public void startPhotoSelectActivity() {
                    try {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        getParentActivity().startActivityForResult(photoPickerIntent, 11);
                    } catch (Exception e) {
                        FileLog.e(e);
                    }
                }
            });
            presentFragment(fragment);
        });

        flashButton = new ImageView(context);
        flashButton.setScaleType(ImageView.ScaleType.CENTER);
        flashButton.setImageResource(R.drawable.qr_flashlight);
        flashButton.setBackgroundDrawable(Theme.createCircleDrawable(AndroidUtilities.dp(60), 0x22ffffff));
        flashButton.setOnClickListener(currentImage -> {
            CameraSession session = cameraView.getCameraSession();
            if (session != null) {
                ShapeDrawable shapeDrawable = (ShapeDrawable) flashButton.getBackground();
                if (flashAnimator != null) {
                    flashAnimator.cancel();
                    flashAnimator = null;
                }
                flashAnimator = new AnimatorSet();
                ObjectAnimator animator = ObjectAnimator.ofInt(shapeDrawable, AnimationProperties.SHAPE_DRAWABLE_ALPHA, flashButton.getTag() == null ? 0x44 : 0x22);
                animator.addUpdateListener(animation -> flashButton.invalidate());
                flashAnimator.playTogether(animator);
                flashAnimator.setDuration(200);
                flashAnimator.setInterpolator(CubicBezierInterpolator.DEFAULT);
                flashAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        flashAnimator = null;
                    }
                });
                flashAnimator.start();
                if (flashButton.getTag() == null) {
                    flashButton.setTag(1);
                    session.setTorchEnabled(true);
                } else {
                    flashButton.setTag(null);
                    session.setTorchEnabled(false);
                }
            }
        });



        for(int a = 0; a < viewPages.length; a++){
            final ViewPage ViewPage = new ViewPage(context) {
                @Override
                public void setTranslationX(float translationX) {
                    super.setTranslationX(translationX);
                    if (tabsAnimationInProgress) {
                        if (viewPages[0] == this) {
                            float scrollProgress = Math.abs(viewPages[0].getTranslationX()) / (float) viewPages[0].getMeasuredWidth();
                            scrollSlidingTextTabStrip.selectTabWithId(viewPages[1].selectedType, scrollProgress);
                            //updateActionBar(viewPages[1].selectedType,scrollProgress,false);
                        }
                    }
                }

            };
            frameLayout.addView(ViewPage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            ViewPage.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            viewPages[a] = ViewPage;
            viewPages[a].innerFrameLayout = createContentView(a,context);

            viewPages[a].addView(viewPages[a].innerFrameLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            if (a != 0) {
                viewPages[a].setVisibility(View.GONE);
            }
        }

        frameLayout.addView(actionBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        updateTabs();
        switchToCurrentSelectedMode(false);
        swipeBackEnabled = scrollSlidingTextTabStrip.getCurrentTabId() == scrollSlidingTextTabStrip.getFirstTabId();

        if (getParentActivity() != null) {
            getParentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        fragmentView.setKeepScreenOn(true);

        return fragmentView;

    }



    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return swipeBackEnabled;
    }


    private void updateTabs() {
        if (scrollSlidingTextTabStrip == null) {
            return;
        }
        scrollSlidingTextTabStrip.addTextTab(0, "My QrCode");
        scrollSlidingTextTabStrip.addTextTab(1,"Scan");
        scrollSlidingTextTabStrip.setVisibility(View.VISIBLE);
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        int id = scrollSlidingTextTabStrip.getCurrentTabId();
        if (id >= 0) {
            viewPages[0].selectedType = id;
        }
        scrollSlidingTextTabStrip.finishAddingTabs();
    }

    private void switchToCurrentSelectedMode(boolean animated) {

        int a = animated ? 1 : 0;

        if (viewPages[a].selectedType == 0) {

        } else if (viewPages[a].selectedType == 1) {

        }
        viewPages[a].innerFrameLayout.setVisibility(View.VISIBLE);

    }


    public void setDelegate(ScanQrFragmentDelegate delegate) {
        this.delegate = delegate;
    }

    public void destroy(boolean async, final Runnable beforeDestroyRunnable) {
        if (cameraView != null) {
            cameraView.destroy(async, beforeDestroyRunnable);
            cameraView = null;
        }
        backgroundHandlerThread.quitSafely();
    }

    private void startRecognizing() {
        backgroundHandlerThread.start();
        handler = new Handler(backgroundHandlerThread.getLooper());
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (cameraView != null && !recognized && cameraView.getCameraSession() != null) {
                    cameraView.getCameraSession().setOneShotPreviewCallback(ScanQrFragment.this);
                    AndroidUtilities.runOnUIThread(this, 500);
                }
            }
        });
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 11) {
                if (data == null || data.getData() == null) {
                    return;
                }
                try {
                    Point screenSize = AndroidUtilities.getRealScreenSize();
                    Bitmap bitmap = ImageLoader.loadBitmap(null, data.getData(), screenSize.x, screenSize.y, true);
                    String text = tryReadQr(null, null, 0, 0, 0, bitmap);
                    if (text != null) {
                        if (delegate != null) {
                            delegate.didFindQr(text);
                        }
                        finishFragment();
                    }
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
        }
    }
    private void onNoQrFound() {
        AndroidUtilities.runOnUIThread(() -> {
            if (recognizedMrzView.getTag() != null) {
                recognizedMrzView.setTag(null);
                recognizedMrzView.animate().setDuration(200).alpha(0.0f).setInterpolator(CubicBezierInterpolator.DEFAULT).start();
            }
        });
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        handler.post(() -> {
            try {
                Size size = cameraView.getPreviewSize();
                int side = (int) (Math.min(size.getWidth(), size.getHeight()) / 1.5f);
                int x = (size.getWidth() - side) / 2;
                int y = (size.getHeight() - side) / 2;

                String text = tryReadQr(data, size, x, y, side, null);
                if (text != null) {
                    recognized = true;
                    camera.stopPreview();
                    AndroidUtilities.runOnUIThread(() -> {
                        if (delegate != null) {
                            delegate.didFindQr(text);
                        }
                        finishFragment();
                    });
                }
            } catch (Throwable ignore) {
                onNoQrFound();
            }
        });
    }

    private String tryReadQr(byte[] data, Size size, int x, int y, int side, Bitmap bitmap) {
        try {
            String text;
            if (visionQrReader.isOperational()) {
                Frame frame;
                if (bitmap != null) {
                    frame = new Frame.Builder().setBitmap(bitmap).build();
                } else {
                    frame = new Frame.Builder().setImageData(ByteBuffer.wrap(data), size.getWidth(), size.getHeight(), ImageFormat.NV21).build();
                }
                SparseArray<Barcode> codes = visionQrReader.detect(frame);
                if (codes != null && codes.size() > 0) {
                    text = codes.valueAt(0).rawValue;
                } else {
                    text = null;
                }
            } else {
                LuminanceSource source;
                if (bitmap != null) {
                    int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                    bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                    source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                } else {
                    source = new PlanarYUVLuminanceSource(data, size.getWidth(), size.getHeight(), x, y, side, side, false);
                }

                Result result = qrReader.decode(new BinaryBitmap(new GlobalHistogramBinarizer(source)));
                if (result == null) {
                    onNoQrFound();
                    return null;
                }
                text = result.getText();
            }
            if (TextUtils.isEmpty(text)) {
                onNoQrFound();
                return null;
            }
            if(!ShopUtils.isHuluChatSupportedLink(text)){
                return null;
            }
            return text;
        } catch (Throwable ignore) {
            onNoQrFound();
        }
        return null;
    }

    private FrameLayout createContentView(int pos,Context context){

        FrameLayout viewGroup = new FrameLayout(context){


            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = MeasureSpec.getSize(heightMeasureSpec);
              //  actionBar.measure(widthMeasureSpec, heightMeasureSpec);
                cameraView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                recognizedMrzView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                if (galleryButton != null) {
                    galleryButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
                }
                flashButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60), MeasureSpec.EXACTLY));
                titleTextView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));
                descriptionText.measure(MeasureSpec.makeMeasureSpec((int) (width * 0.9f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.UNSPECIFIED));

                setMeasuredDimension(width, height);
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {

                int width = r - l;
                int height = b - t;

                if(pos == 1){
                    int y = 0;
                    {
                       // actionBar.layout(0, 0, actionBar.getMeasuredWidth(), actionBar.getMeasuredHeight());
                        cameraView.layout(0, 0, cameraView.getMeasuredWidth(), cameraView.getMeasuredHeight());
                        int size = (int) (Math.min(cameraView.getWidth(), cameraView.getHeight()) / 1.5f);
                        y = (cameraView.getMeasuredHeight() - size) / 2 - titleTextView.getMeasuredHeight() - AndroidUtilities.dp(30);
                        titleTextView.layout(0, y, titleTextView.getMeasuredWidth(), y + titleTextView.getMeasuredHeight());
                        recognizedMrzView.layout(0, getMeasuredHeight() - recognizedMrzView.getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight());

                        int x;
                        x = cameraView.getMeasuredWidth() / 2 + AndroidUtilities.dp(35);
                        y = (cameraView.getMeasuredHeight() - size) / 2 + size + AndroidUtilities.dp(30);
                        flashButton.layout(x, y, x + flashButton.getMeasuredWidth(), y + flashButton.getMeasuredHeight());

                        if (galleryButton != null) {
                            x = cameraView.getMeasuredWidth() / 2 - AndroidUtilities.dp(35) - galleryButton.getMeasuredWidth();
                            galleryButton.layout(x, y, x + galleryButton.getMeasuredWidth(), y + galleryButton.getMeasuredHeight());
                        }
                    }

                    y = (int) (height * 0.74f);
                    int x = (int) (width * 0.05f);
                    descriptionText.layout(x, y, x + descriptionText.getMeasuredWidth(), y + descriptionText.getMeasuredHeight());


                }


            }

            @Override
            protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                boolean result = super.drawChild(canvas, child, drawingTime);

                if (pos == 1 && child == cameraView) {
                    int size = (int) (Math.min(child.getWidth(), child.getHeight()) / 1.5f);
                    int x = (child.getWidth() - size) / 2;
                    int y = (child.getHeight() - size) / 2;
                    canvas.drawRect(0, 0, child.getMeasuredWidth(), y, paint);
                    canvas.drawRect(0, y + size, child.getMeasuredWidth(), child.getMeasuredHeight(), paint);
                    canvas.drawRect(0, y, x, y + size, paint);
                    canvas.drawRect(x + size, y, child.getMeasuredWidth(), y + size, paint);

                    path.reset();
                    path.moveTo(x, y + AndroidUtilities.dp(20));
                    path.lineTo(x, y);
                    path.lineTo(x + AndroidUtilities.dp(20), y);
                    canvas.drawPath(path, cornerPaint);

                    path.reset();
                    path.moveTo(x + size, y + AndroidUtilities.dp(20));
                    path.lineTo(x + size, y);
                    path.lineTo(x + size - AndroidUtilities.dp(20), y);
                    canvas.drawPath(path, cornerPaint);

                    path.reset();
                    path.moveTo(x, y + size - AndroidUtilities.dp(20));
                    path.lineTo(x, y + size);
                    path.lineTo(x + AndroidUtilities.dp(20), y + size);
                    canvas.drawPath(path, cornerPaint);

                    path.reset();
                    path.moveTo(x + size, y + size - AndroidUtilities.dp(20));
                    path.lineTo(x + size, y + size);
                    path.lineTo(x + size - AndroidUtilities.dp(20), y + size);
                    canvas.drawPath(path, cornerPaint);
                }
                return result;
            }
        };
        viewGroup.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        viewGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        if(pos == 1){
                viewGroup.addView(cameraView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                viewGroup.addView(titleTextView);
                viewGroup.addView(recognizedMrzView);
                viewGroup.addView(galleryButton);
                viewGroup.addView(flashButton);
        }else{
            viewGroup = new FrameLayout(context);
            viewGroup.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            ImageView galleryButton = new ImageView(context);
            int w = AndroidUtilities.displaySize.x;
            galleryButton.post(() -> {
                try {
                    Bitmap bitmap = getQRCodeImage(ShopUtils.getShopLink(chat_id),w,w);
                    galleryButton.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            });
            galleryButton.setScaleType(ImageView.ScaleType.CENTER);
            linearLayout.addView(galleryButton, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER , 0, 16, 0, 100));


            TextView textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_chats_menuItemText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setText("Scan to open shop profile!");
            textView.setSingleLine(true);
            textView.setGravity(Gravity.CENTER);
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL , 0, 16, 0, 100));
            viewGroup.addView(linearLayout,LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT,LayoutHelper.WRAP_CONTENT,Gravity.CENTER));

        }


        return viewGroup;
    }



    private Bitmap getQRCodeImage(String text, int width, int height) throws WriterException{
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hintMap.put(EncodeHintType.MARGIN,8);
        Bitmap bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height,hintMap,null,getParentActivity());
        return bitMatrix;
    }

}
