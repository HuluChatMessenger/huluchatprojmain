package org.plus.apps.business.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.plus.apps.business.ShopUtils;
import org.plus.apps.business.data.ShopDataController;
import org.plus.apps.business.data.ShopDataSerializer;
import org.plus.apps.business.ui.cells.ProductImageCell;
import org.plus.apps.business.ui.cells.Ui_info_bold_cell;
import org.plus.apps.business.ui.cells.Ui_input_cell;
import org.plus.apps.business.ui.components.ProductImageLayout;
import org.plus.apps.business.ui.components.TagTextLayout;
import org.plus.apps.business.ui.components.ValueSelectBottomSheet;
import org.plus.net.APIError;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DownloadController;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SecureDocument;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.PollEditTextCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.ContextProgressView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.ImageUpdater;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.LocationActivity;
import org.telegram.ui.PassportActivity;

import java.io.File;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductInputActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, ImageUpdater.ImageUpdaterDelegate {

    public static final String ui_picture = "ui_picture";
    public static final String ui_location = "ui_location";

    public static final String UI_CHOOSE_HORIZONTAL = "ui_choose_hor";
    public static final String UI_INPUT_STRING = "ui_input_str";
    public static final String UI_INPUT_NUM = "ui_input_num";
    public static final String UI_INPUT_LOC = "ui_input_loc";
    public static final String UI_CHOOSE = "ui_choose";
    public static final String ui_radio_box = "ui_radio_box";
    public static final String ui_date_chooser = "ui_date_chooser";

    public Map<String,Object> productTags = new HashMap<>();
    private ImageUpdater imageUpdater;
    private int currentUploadPos;

    private ArrayList<Integer> currentUploadPosation = new ArrayList<>();

    private int chat_id;
    private ArrayList<ShopDataSerializer.FieldSet> fieldSets = new ArrayList<>();
    private boolean fieldLoaded;

    private ShopDataSerializer.Product product;
    private boolean creatingNew;
    private ArrayList<ShopDataSerializer.FieldSet> fi;
    private ShopDataSerializer.ProductType productType;

    private final Map<String,View> requiredFields = new HashMap<>();

    private String section;


    public ProductInputActivity(int chat_id,ShopDataSerializer.ProductType product_type){
        this.chat_id = chat_id;
        productType = product_type;
        section  = product_type.key + "-" + "create";
        ShopDataController.getInstance(currentAccount).loadSectionConfiguration(section,true,classGuid);
    }

    private ActionBarMenuItem doneItem;
    private AnimatorSet doneItemAnimation;
    private ContextProgressView progressView;
    private int done_button;

    private ScrollView scrollView;
    private LinearLayout linearLayout;
    private LinearLayout emptyLayout;
    private RadialProgressView radialProgressView;
    private Context context;
    private ProductImageLayout productImageLayout;


    @Override
    public boolean onFragmentCreate() {
        getNotificationCenter().addObserver(this, NotificationCenter.PlusFileDidUpload);
        getNotificationCenter().addObserver(this, NotificationCenter.PlusFileDidFailUpload);
        getNotificationCenter().addObserver(this, NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.didImageUploaded);
        getNotificationCenter().addObserver(this, NotificationCenter.didProductListed);
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        getNotificationCenter().addObserver(this, NotificationCenter.didImageUploaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.didConfigrationLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.PlusFileDidUpload);
        getNotificationCenter().removeObserver(this, NotificationCenter.PlusFileDidFailUpload);
        getNotificationCenter().removeObserver(this, NotificationCenter.didProductListed);
    }

    private boolean checkDiscard() {
        if (productTags != null && !productTags.isEmpty()) {
             AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("DiscardNewProduct", R.string.DiscardNewProduct));
            builder.setMessage(LocaleController.getString("DiscardNewProductAlert", R.string.DiscardNewProductAlert));
            builder.setPositiveButton(LocaleController.getString("DiscardNewProductSave", R.string.DiscardNewProductSave), (dialogInterface, i) -> processDone());
            builder.setNegativeButton(LocaleController.getString("ProductDiscard", R.string.ProductDiscard), (dialog, which) -> finishFragment());
            showDialog(builder.create());
            return false;
        }
        return true;
    }

    private AlertDialog progressDialog;
    private int reqId;



    private void processDone(){
        if(checkForRequiredField()){
            progressDialog = new AlertDialog(getParentActivity(), 3);
            progressDialog.setCanCacnel(true);
            progressDialog.setOnCancelListener(dialog -> {
            });
            progressDialog.show();
            getShopDataController().listProduct(chat_id,productType.key,productImageLayout.getImageInputs(),productTags);
        }
    }

    private void vibrate(View view){
        if(view != null && scrollView != null){
            scrollView.scrollTo(0, (int)view.getY());
        }

        if (view != null) {
            Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
            AndroidUtilities.shakeView(view, 2, 0);
        }
    }


    private boolean checkForRequiredField(){
        int count = linearLayout.getChildCount();
        for(int a = 0; a < count; a++){
            View child = linearLayout.getChildAt(a);
            if(child == null){
                continue;
            }
            Object tag = child.getTag();
            if(tag instanceof String){
                String key = (String) tag;
                Object val = productTags.get(key);
                if(val == null){
                    vibrate(child);
                    return false;
                }
                if(val instanceof String){
                    if(ShopUtils.isEmpty((String)val)){
                        vibrate(child);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean checkRequired(){
        for (Map.Entry<String, View> entry : requiredFields.entrySet()) {
            String key = entry.getKey();
            Object value = productTags.get(key);
            if(value == null){
                vibrate(entry.getValue());
                return false;
            }

            if(value instanceof String){
                String valStr = value.toString();
                if(ShopUtils.isEmpty(valStr)){
                    vibrate(entry.getValue());
                    return false;
                }
            }
        }


            if(productImageLayout != null){
                ArrayList<ProductImageLayout.ImageInput> imageInputs =  productImageLayout.getImageInputs();
                if(imageInputs.isEmpty()){
                    vibrate(productImageLayout);
                    return false;
                }

                boolean hasAtListOne = false;

                for(ProductImageLayout.ImageInput imageInput : imageInputs){
                    if(imageInput.smallSize != null || imageInput.bigSize != null){
                        hasAtListOne = true;
                        break;
                    }
                }


                  if(!hasAtListOne){
                      vibrate(productImageLayout);
                      return false;
                  }

            }

        return true;
    }


    @Override
    public View createView(Context context) {

        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("ListProductTitle",R.string.ListProductTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick(){
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == done_button) {
                    processDone();
                }
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        doneItem = menu.addItem(done_button, "LIST");


        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        scrollView = new ScrollView(context) {
            @Override
            protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
                return false;
            }

            @Override
            public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
                rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
                rectangle.top += AndroidUtilities.dp(30);
                rectangle.bottom += AndroidUtilities.dp(50);
                return super.requestChildRectangleOnScreen(child, rectangle, immediate);
            }
        };
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setScrollViewEdgeEffectColor(scrollView, Theme.getColor(Theme.key_actionBarDefault));
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0,  0));

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        emptyLayout = new LinearLayout(context);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(Gravity.CENTER);
        emptyLayout.setVisibility(View.GONE);
        emptyLayout.setOnTouchListener((v, event) -> true);

        ImageView emptyImageView = new ImageView(context);
        emptyImageView.setImageResource(R.drawable.files_empty);
        emptyLayout.addView(emptyImageView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        TextView emptyTextView = new TextView(context);
        emptyTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        emptyTextView.setGravity(Gravity.CENTER);
        emptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        emptyTextView.setPadding(AndroidUtilities.dp(40), 0, AndroidUtilities.dp(40), AndroidUtilities.dp(128));
        emptyLayout.addView(emptyTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 0, 24, 0, 0));
        emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        emptyTextView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(emptyLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if(imageUpdater == null){
            imageUpdater = new ImageUpdater(true);
            imageUpdater.setUploadAfterSelect(false);
            imageUpdater.setOpenWithFrontfaceCamera(false);
            imageUpdater.parentFragment = this;
            imageUpdater.setDelegate(this);
            getMediaDataController().checkFeaturedStickers();
            getMessagesController().loadSuggestedFilters();
            getMessagesController().loadUserInfo(getUserConfig().getCurrentUser(), true, classGuid);
        }

        radialProgressView = new RadialProgressView(context);
        radialProgressView.setSize(AndroidUtilities.dp(48));
        radialProgressView.setVisibility(View.VISIBLE);
        radialProgressView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        frameLayout.addView(radialProgressView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        return fragmentView;
    }


    private void createListingView(){
        if(!fieldLoaded || fieldSets == null || creatingList){
            return;
        }
        creatingList = true;
        for(int a = 0; a < fieldSets.size(); a++){
            ShopDataSerializer.FieldSet fieldSet = fieldSets.get(a);
            if(fieldSet == null){
                continue;
            }
            if(!ShopUtils.isEmpty(fieldSet.header)){
                Ui_info_bold_cell ui_info_cell = new Ui_info_bold_cell(context);
                ui_info_cell.setText(fieldSet.header);
                linearLayout.addView(ui_info_cell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,Gravity.LEFT,0,0,0,0));

                if(!ShopUtils.isEmpty(fieldSet.info)){
                    ui_info_cell.showInfo(true);
                }
            }

            if(!ShopUtils.isEmpty(fieldSet.description)){
                TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(context);
                textInfoPrivacyCell.setText(fieldSet.description);
                linearLayout.addView(textInfoPrivacyCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
            }

            createFiledSetView(fieldSet);

            ShadowSectionCell shadowSectionCell = new ShadowSectionCell(context,21);
            shadowSectionCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout.addView(shadowSectionCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,0,0,0,0));

        }
    }


    private void createFiledSetView(ShopDataSerializer.FieldSet fieldSet){
        if(!TextUtils.isEmpty(fieldSet.type)){
            switch (fieldSet.type) {
                case ui_picture: {
                    if (fieldSet.fields != null && !fieldSet.fields.isEmpty()) {
                        if (fieldSet.fields.get(0).ui_type.equals("ui_picture_mul") || fieldSet.fields.get(0).max_len >= 1) {
                          createMultiPhotoView(fieldSet);
                        } else {
                            createSinglePhotoView(fieldSet);
                        }
                     }
                     break;
                }
                case ui_location: {
                    createLocationView(fieldSet);
                    break;
                }
            }

        }else{
           ArrayList<ShopDataSerializer.Field> fields =  fieldSet.fields;
            Collections.sort(fields,fieldComparator);
            for(int a = 0; a < fields.size(); a ++){
                ShopDataSerializer.Field field = fields.get(a);
                createFieldView(field);
            }
        }

    }

    private void createMultiPhotoView(ShopDataSerializer.FieldSet  fieldSet){
        if(fieldSet == null){
            return;
        }
        ShopDataSerializer.Field field = fieldSet.fields.get(0);
         productImageLayout = new ProductImageLayout(context, field.max_len, new ProductImageLayout.ProductLayoutImageDelegate() {
            @Override
            public void onItemClick(View view, int position, ProductImageLayout.ImageInput imageInput, float x, float y) {
                if(view instanceof ProductImageCell){
                    ProductImageCell imageCell  =  ((ProductImageCell)view);
                    if(imageCell.getTag() != null){
                        imageCell.hideButton();
                        return;
                    }
                }
                if (imageUpdater != null) {
                    currentUploadPos = position;
                    imageUpdater.openMenu(false, null);
                }
            }


            @Override
            public void onItemLonClick(ProductImageCell view, int position) {
                if(view.getTag() == null){
                    view.showButton();
                }
            }
        }, false);

         linearLayout.addView(productImageLayout,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

         if(field.required){
             requiredFields.put(field.key,productImageLayout);
         }


    }

    private void createSinglePhotoView(ShopDataSerializer.FieldSet  fieldSet){

        SecureDocumentCell secureDocumentCell = new SecureDocumentCell(context);
        linearLayout.addView(secureDocumentCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
    }
    private void createFieldView(ShopDataSerializer.Field field){
        if(field == null || TextUtils.isEmpty(field.ui_type)){
            return;
        }
        switch (field.ui_type) {
            case UI_INPUT_STRING:
            case UI_INPUT_NUM:
                createInputEditText(field);
                break;
            case UI_CHOOSE_HORIZONTAL:
                 createHorizontalChooserView(field);
                break;
            case UI_CHOOSE:
                createChooserView(field);
                break;
            case ui_radio_box:
                createRadioBoxView(field);
                break;
            case ui_date_chooser:
                createDateChooserView(field);
                break;
        }
    }
    private void createDateChooserView(ShopDataSerializer.Field field){
        if(field == null){
            return;
        }
        TextSettingsCell dateView = new TextSettingsCell(context);
        dateView.setTextAndIcon(field.label,R.drawable.menu_date,false);
        dateView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (getParentActivity() == null) {
                        return;
                    }
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int monthOfYear = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                try {
                    final EditTextBoldCursor field1 = (EditTextBoldCursor)view;
                    int num = (Integer) field1.getTag();
                    int minYear;
                    int maxYear;
                    int currentYearDiff;
                    String title;
                    title = LocaleController.getString("PassportSelectBithdayDate", R.string.PassportSelectBithdayDate);
                    minYear = -120;
                    maxYear = 0;
                    currentYearDiff = -18;
                    int selectedDay = -1;
                    int selectedMonth = -1;
                    int selectedYear = -1;
                    String[] args = field1.getText().toString().split("\\.");
                    if (args.length == 3) {
                        selectedDay = Utilities.parseInt(args[0]);
                        selectedMonth = Utilities.parseInt(args[1]);
                        selectedYear = Utilities.parseInt(args[2]);
                    }
                    AlertDialog.Builder builder = AlertsCreator.createDatePickerDialog(context, minYear, maxYear, currentYearDiff, selectedDay, selectedMonth, selectedYear, title, false, (year1, month, dayOfMonth1) -> {
                        String text = String.format(Locale.US, "%02d.%02d.%d", dayOfMonth1, month + 1, year1);
                        field1.setText(text);
                        productTags.put(field.key, text);

                    });
                    showDialog(builder.create());
                } catch (Exception e) {
                    FileLog.e(e);
                }


            }
        });


        if(!ShopUtils.isEmpty(field._default)){
            dateView.setTextAndIcon(field._default,R.drawable.menu_date,true);
            productTags.put(field.key,field._default);
        }

        if(field.required){
            requiredFields.put(field.key,dateView);
        }
        linearLayout.addView(dateView,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

    }
    private void createRadioBoxView(ShopDataSerializer.Field field){
        if(field == null){
            return;
        }
        TextCheckCell textCheckBoxCell = new TextCheckCell(context);
        boolean def_val = false;
        try {
            def_val = Boolean.parseBoolean(field._default);
        }catch (Exception exception){
            FileLog.e(exception);
        }
        productTags.put(field.key,def_val);

        if(field.required){
            requiredFields.put(field.key,textCheckBoxCell);
        }
        textCheckBoxCell.setTextAndCheck(field.label,def_val,false);
        textCheckBoxCell.setBackgroundDrawable(Theme.getSelectorDrawable(true));
        textCheckBoxCell.setOnClickListener(v -> {
            textCheckBoxCell.setChecked(!textCheckBoxCell.isChecked());
            productTags.put(field.key,textCheckBoxCell.isChecked());
        });
        textCheckBoxCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        linearLayout.addView(textCheckBoxCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));
    }
    private void createInputEditText(ShopDataSerializer.Field fieldData){
        if(fieldData == null){
            return;
        }


        if(!ShopUtils.isEmpty(fieldData.label)){

            HeaderCell headerCell = new HeaderCell(context,Theme.key_dialogTextBlack,21);
            headerCell.setText(fieldData.label + (fieldData.required?"":" (optional)"));
            headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            linearLayout.addView(headerCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,0,0,0,8));
        }

        FrameLayout container = new FrameLayout(context);
        container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        if(fieldData.required){
            container.setTag(fieldData.key);
        }



        Ui_input_cell cell = new Ui_input_cell(context,false,fieldData.prefix,fieldData.suffix);
        cell.createErrorTextView();
        String text = fieldData.placeholder;
        if(TextUtils.isEmpty(text)){
            text = text.substring(0,1).toUpperCase() + text.substring(1);
        }
        cell.setTextAndHint("",text,false);
        cell.addTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            @Override
            public void afterTextChanged(Editable s) {
                if(s == null){
                    return;
                }
                String value = s.toString();
                productTags.put(fieldData.key,value);
            }
        });
        EditTextBoldCursor editText = cell.getTextView();
        cell.setShowNextButton(true);
        editText.setOnFocusChangeListener((v, hasFocus) -> cell.getTextView2().setAlpha(1f));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        editText.setOnKeyListener((view1, i, keyEvent) -> false);
        container.addView(cell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 64, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));

        linearLayout.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));


        //setting input
        if(fieldData.ui_type.equals(UI_INPUT_NUM)){
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }else if(fieldData.ui_type.equals(UI_INPUT_STRING)){
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }


        //checking for reqired data
        if(fieldData.required){
            requiredFields.put(fieldData.key,cell);
        }

        //checking for max and min input
        if(fieldData.max_len > 0){
            InputFilter[] inputFilters = new InputFilter[1];
            inputFilters[0] = new InputFilter.LengthFilter(fieldData.max_len);
            editText.setFilters(inputFilters);
        }



        //set default value
        if(!ShopUtils.isEmpty(fieldData._default)){
            productTags.put(fieldData.key,fieldData._default);
            cell.setText(fieldData._default,false);
        }


    }
    private void createHorizontalChooserView(ShopDataSerializer.Field field){

        if(!field.choices.isEmpty()) {

            if (!ShopUtils.isEmpty(field.label)) {
                HeaderCell ui_info_cell = new HeaderCell(context,Theme.key_dialogTextBlack,21);
                ui_info_cell.setText(field.label + (field.required?"":" (optional)"));

                linearLayout.addView(ui_info_cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }

            TagTextLayout tagTextLayout = new TagTextLayout(context);
            ArrayList<TagTextLayout.TagText> tagTexts = new ArrayList<>();
            for (String[] li : field.choices) {
                tagTexts.add(new TagTextLayout.TagText(li[0], li[1]));
            }

            tagTextLayout.setDelegate((view, position, selected) -> {
                tagTextLayout.setSelectedTag(selected);
                productTags.put(field.key,selected.key);
            });
            tagTextLayout.setTagTexts(tagTexts);
            linearLayout.addView(tagTextLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,8,0,0,21));

            if(field.required){
                requiredFields.put(field.key,tagTextLayout);
            }

            if(field.required){
                tagTextLayout.setTag(field.key);
            }


        }


    }

    @Override
    public boolean canBeginSlide() {
        return checkDiscard();
    }

    private void createChooserView(ShopDataSerializer.Field field){
        if(field == null){
            return;
        }

        if(!ShopUtils.isEmpty(field.label)){
            HeaderCell headerCell = new HeaderCell(context,Theme.key_dialogTextBlack,21);
            headerCell.setText(field.label);
            headerCell.setText(field.label + (field.required?"":" (optional)"));

            linearLayout.addView(headerCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,0,0,0,8));
        }

        if(!ShopUtils.isEmpty(field.placeholder)){
            TextSettingsCell textSettingsCell = new TextSettingsCell(context);
            textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            textSettingsCell.setTextAndIcon(field.placeholder,R.drawable.message_arrow,false);
            linearLayout.addView(textSettingsCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,0,0,0,0));
            textSettingsCell.setOnClickListener(v -> {
                ValueSelectBottomSheet.createShareAlert(context, field.choices, false, new ValueSelectBottomSheet.ProductTagValueDelegate() {
                    @Override
                    public void didProductTagSelected(ArrayList<TagTextLayout.TagText> tags) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                productTags.put(field.key,tags.get(0).key);
                                textSettingsCell.setTextAndIcon(tags.get(0).value,R.drawable.message_arrow,false);
                            }
                        },100);

                    }
                }).show();
        });

        if(field.required){
           requiredFields.put(field.key,textSettingsCell);
         }


            if(field.required){
                textSettingsCell.setTag(field.key);
            }


        }


    }

    private void createLocationView(ShopDataSerializer.FieldSet  field){
        TextSettingsCell textSettingsCell = new TextSettingsCell(context);
        textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        textSettingsCell.setTextAndIcon("Location",R.drawable.message_arrow,false);
        linearLayout.addView(textSettingsCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,0,0,0,8));
        textSettingsCell.setOnClickListener(v -> {
            LocationActivity locationActivity = new LocationActivity(LocationActivity.LOCATION_TYPE_GROUP);
            locationActivity.setDelegate((location, live, notify, scheduleDate) -> {
                if(location != null){
                    productTags.put("latitude",location.geo.lat);
                    productTags.put("longitude",location.geo._long);
                    textSettingsCell.setTextAndIcon(location.address,R.drawable.message_arrow,false);
                }
            });
            presentFragment(locationActivity);
        });

        for(int a = 0; a  < field.fields.size(); a++){
            ShopDataSerializer.Field field1 =  field.fields.get(a);
            if(field1 != null){
//                requiredFields.put(field1.key,textSettingsCell);

                if(field1.required){
                    textSettingsCell.setTag(field1.key);
                }
                break;

            }

        }



    }

    private void showEditDoneProgress(final boolean animateDoneItem, final boolean show) {
        if (doneItemAnimation != null) {
            doneItemAnimation.cancel();
        }
        if (animateDoneItem && doneItem != null) {
            doneItemAnimation = new AnimatorSet();
            if (show) {
                progressView.setVisibility(View.VISIBLE);
                doneItem.setEnabled(false);
                doneItemAnimation.playTogether(
                        ObjectAnimator.ofFloat(doneItem.getContentView(), View.SCALE_X, 0.1f),
                        ObjectAnimator.ofFloat(doneItem.getContentView(), View.SCALE_Y, 0.1f),
                        ObjectAnimator.ofFloat(doneItem.getContentView(), View.ALPHA, 0.0f),
                        ObjectAnimator.ofFloat(progressView, View.SCALE_X, 1.0f),
                        ObjectAnimator.ofFloat(progressView, View.SCALE_Y, 1.0f),
                        ObjectAnimator.ofFloat(progressView, View.ALPHA, 1.0f));
            } else {
                doneItem.getContentView().setVisibility(View.VISIBLE);
                doneItem.setEnabled(true);
                doneItemAnimation.playTogether(
                        ObjectAnimator.ofFloat(progressView, View.SCALE_X, 0.1f),
                        ObjectAnimator.ofFloat(progressView, View.SCALE_Y, 0.1f),
                        ObjectAnimator.ofFloat(progressView, View.ALPHA, 0.0f),
                        ObjectAnimator.ofFloat(doneItem.getContentView(), View.SCALE_X, 1.0f),
                        ObjectAnimator.ofFloat(doneItem.getContentView(), View.SCALE_Y, 1.0f),
                        ObjectAnimator.ofFloat(doneItem.getContentView(), View.ALPHA, 1.0f));
            }
            doneItemAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (doneItemAnimation != null && doneItemAnimation.equals(animation)) {
                        if (!show) {
                            progressView.setVisibility(View.INVISIBLE);
                        } else {
                            doneItem.getContentView().setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (doneItemAnimation != null && doneItemAnimation.equals(animation)) {
                        doneItemAnimation = null;
                    }
                }
            });
            doneItemAnimation.setDuration(150);
            doneItemAnimation.start();
        }
    }

    private void showData(){
        if(fieldLoaded){
            linearLayout.setAlpha(0f);
            linearLayout.setVisibility(View.VISIBLE);
            linearLayout.animate().alpha(1f).setDuration(300).setListener(null);
            radialProgressView.animate().alpha(0f).setStartDelay(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    radialProgressView.setVisibility(View.GONE);
                }
            });
        }else{
            emptyLayout.setAlpha(0f);
            emptyLayout.setVisibility(View.VISIBLE);
            emptyLayout.animate().alpha(1f).setDuration(300).setListener(null);
            radialProgressView.animate().alpha(0f).setStartDelay(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    radialProgressView.setVisibility(View.GONE);
                }
            });
        }
    }

    private boolean creatingList;

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if(id == NotificationCenter.didConfigrationLoaded){
            boolean loaded = (boolean )args[0];
            String sec = (String) args[1];
            if(loaded && sec.equals(section)){
                fieldLoaded = true;
                fieldSets = (ArrayList<ShopDataSerializer.FieldSet>)args[2];
            }else{
                fieldLoaded = false;
            }
            Collections.sort(fieldSets,fieldSetComparator);
            createListingView();
            showData();
        }else if(id == NotificationCenter.didProductListed){
            if(progressDialog != null){
                progressDialog.dismiss();
            }
            boolean listed = (boolean )args[0];
            if(listed){
                ShopDataSerializer.Product product = (ShopDataSerializer.Product)args[1];
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.didNewProductListed,product);
//                if(delegate != null){
//                    delegate.onProductListed(product);
//                }
                finishFragment();
            }else{
                APIError  error = (APIError) args[2];
                ShopUtils.createErrorAlert(context,error);
            }
        }
    }


    @Override
    public void didUploadPhoto(TLRPC.InputFile photo, TLRPC.InputFile video, double videoStartTimestamp, String videoPath, TLRPC.PhotoSize bigSize, TLRPC.PhotoSize smallSize) {
        ProductImageLayout.ImageInput imageInput = new ProductImageLayout.ImageInput();
        imageInput.bigSize = bigSize;
        imageInput.smallSize = smallSize;
        imageInput.pos = currentUploadPos;
        if (productImageLayout != null) {
            productImageLayout.setImageInputs(imageInput);
        }
    }

    private Comparator<ShopDataSerializer.FieldSet> fieldSetComparator = (fieldSet1, fieldSet2) -> {
        if(fieldSet1.order > fieldSet2.order){
            return 1;
        }else if(fieldSet1.order < fieldSet2.order){
            return -1;
        }
        return 0;
    };


    private Comparator<ShopDataSerializer.Field> fieldComparator = (field1, field2) -> {
        if(field1.order < field2.order){
            return -1;
        }else if(field1.order > field2.order){
            return 1;
        }
        return 0;
    };

    public class SecureDocumentCell extends FrameLayout implements DownloadController.FileDownloadProgressListener {

        private TextView textView;
        private TextView valueTextView;
        private BackupImageView imageView;
        private RadialProgress radialProgress;

        private int buttonState;
        private SecureDocument currentSecureDocument;

        private int TAG;

        public SecureDocumentCell(Context context) {
            super(context);

            TAG = DownloadController.getInstance(currentAccount).generateObserverTag();
            radialProgress = new RadialProgress(this);

            imageView = new BackupImageView(context);
            addView(imageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 21, 8, 21, 0));

            textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setLines(1);
            textView.setMaxLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 21 : 81), 10, (LocaleController.isRTL ? 81 : 21), 0));

            valueTextView = new TextView(context);
            valueTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
            valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            valueTextView.setLines(1);
            valueTextView.setMaxLines(1);
            valueTextView.setSingleLine(true);
            valueTextView.setPadding(0, 0, 0, 0);
            addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, (LocaleController.isRTL ? 21 : 81), 35, (LocaleController.isRTL ? 81 : 21), 0));

            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64) + 1, MeasureSpec.EXACTLY));
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            int x = imageView.getLeft() + (imageView.getMeasuredWidth() - AndroidUtilities.dp(24)) / 2;
            int y = imageView.getTop() + (imageView.getMeasuredHeight() - AndroidUtilities.dp(24)) / 2;
            radialProgress.setProgressRect(x, y, x + AndroidUtilities.dp(24), y + AndroidUtilities.dp(24));
        }

        @Override
        protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
            boolean result = super.drawChild(canvas, child, drawingTime);
            if (child == imageView) {
                radialProgress.draw(canvas);
            }
            return result;
        }

        public void setTextAndValueAndImage(String text, CharSequence value, SecureDocument document) {
            textView.setText(text);
            valueTextView.setText(value);
            imageView.setImage(document, "48_48");
            currentSecureDocument = document;

            updateButtonState(false);
        }

        public void setValue(CharSequence value) {
            valueTextView.setText(value);
        }

        public void updateButtonState(boolean animated) {
            String fileName = FileLoader.getAttachFileName(currentSecureDocument);
            File path = FileLoader.getPathToAttach(currentSecureDocument);
            boolean fileExists = path.exists();
            if (TextUtils.isEmpty(fileName)) {
                radialProgress.setBackground(null, false, false);
                return;
            }

            if (currentSecureDocument.path != null) {
                if (currentSecureDocument.inputFile != null) {
                    DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                    radialProgress.setBackground(null, false, animated);
                    buttonState = -1;
                } else {
                    DownloadController.getInstance(currentAccount).addLoadingFileObserver(currentSecureDocument.path, this);
                    buttonState = 1;
                    Float progress = ImageLoader.getInstance().getFileProgress(currentSecureDocument.path);
                    radialProgress.setBackground(Theme.chat_photoStatesDrawables[5][0], true, animated);
                    radialProgress.setProgress(progress != null ? progress : 0, false);
                    invalidate();
                }
            } else {
                if (fileExists) {
                    DownloadController.getInstance(currentAccount).removeLoadingFileObserver(this);
                    buttonState = -1;
                    radialProgress.setBackground(null, false, animated);
                    invalidate();
                } else {
                    DownloadController.getInstance(currentAccount).addLoadingFileObserver(fileName, this);
                    buttonState = 1;
                    Float progress = ImageLoader.getInstance().getFileProgress(fileName);
                    radialProgress.setBackground(Theme.chat_photoStatesDrawables[5][0], true, animated);
                    radialProgress.setProgress(progress != null ? progress : 0, animated);
                    invalidate();
                }
            }
        }


        @Override
        public void invalidate() {
            super.invalidate();
            textView.invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }

        @Override
        public void onFailedDownload(String fileName, boolean canceled) {
            updateButtonState(false);
        }

        @Override
        public void onSuccessDownload(String fileName) {
            radialProgress.setProgress(1, true);
            updateButtonState(true);
        }

        @Override
        public void onProgressDownload(String fileName, long downloadedSize, long totalSize) {
            radialProgress.setProgress(Math.min(1f, downloadedSize / (float) totalSize), true);
            if (buttonState != 1) {
                updateButtonState(false);
            }
        }

        @Override
        public void onProgressUpload(String fileName, long uploadedSize, long totalSize, boolean isEncrypted) {
            radialProgress.setProgress(Math.min(1f, uploadedSize / (float) totalSize), true);
        }

        @Override
        public int getObserverTag() {
            return TAG;
        }
    }



    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> arrayList = new ArrayList<>();

        arrayList.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        arrayList.add(new ThemeDescription(scrollView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch));
        arrayList.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));


        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{TextDetailSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));

        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{TextSettingsCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_SELECTORWHITE, new Class[]{PassportActivity.TextDetailSecureCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PassportActivity.TextDetailSecureCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PassportActivity.TextDetailSecureCell.class}, null, null, null, Theme.key_divider));
        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{PassportActivity.TextDetailSecureCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{PassportActivity.TextDetailSecureCell.class}, new String[]{"checkImageView"}, null, null, null, Theme.key_featuredStickers_addedIcon));

        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{HeaderCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        arrayList.add(new ThemeDescription(linearLayout, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        arrayList.add(new ThemeDescription(linearLayout, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        return arrayList;
    }


    private void createInputFieldView(ShopDataSerializer.Field fieldData){
        if(fieldData == null){
            return;
        }


        EditTextBoldCursor field = new EditTextBoldCursor(context);
        ViewGroup container = new FrameLayout(context) {

            private StaticLayout errorLayout;
            float offsetX;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(34);
                errorLayout = field.getErrorLayout(width);
                if (errorLayout != null) {
                    int lineCount = errorLayout.getLineCount();
                    if (lineCount > 1) {
                        int height = AndroidUtilities.dp(64) + (errorLayout.getLineBottom(lineCount - 1) - errorLayout.getLineBottom(0));
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                    }
                    if (LocaleController.isRTL) {
                        float maxW = 0;
                        for (int a = 0; a < lineCount; a++) {
                            float l = errorLayout.getLineLeft(a);
                            if (l != 0) {
                                offsetX = 0;
                                break;
                            }
                            maxW = Math.max(maxW, errorLayout.getLineWidth(a));
                            if (a == lineCount - 1) {
                                offsetX = width - maxW;
                            }
                        }
                    }
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                if (errorLayout != null) {
                    canvas.save();
                    canvas.translate(AndroidUtilities.dp(21) + offsetX, field.getLineY() + AndroidUtilities.dp(3));
                    errorLayout.draw(canvas);
                    canvas.restore();
                }
            }
        };
        container.setWillNotDraw(false);
        linearLayout.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));


        field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        field.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        field.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        field.setBackgroundDrawable(Theme.createEditTextDrawable(context, false));
        field.setPadding(0, 0, 0, 0);
        field.setTransformHintToHeader(true);
        field.setGravity(LocaleController.isRTL ? Gravity.RIGHT|Gravity.CENTER_VERTICAL : Gravity.LEFT|Gravity.CENTER_VERTICAL);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        field.setImeOptions(EditorInfo.IME_ACTION_DONE);
        field.setHint(fieldData.placeholder);
        field.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        field.setCursorSize(AndroidUtilities.dp(20));
        field.setCursorWidth(1.5f);

        container.addView(field, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 64, Gravity.LEFT | Gravity.TOP, 21, 0, 21, 0));
        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //checkFieldForError(field, key, s, false);
            }
        });
    }


    private void createInputView(ShopDataSerializer.Field field){
        if(!TextUtils.isEmpty(field.label)){
            PollEditTextCell pollEditTextCell = new PollEditTextCell(context, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PollEditTextCell p = (PollEditTextCell) v.getParent();
                    p.getTextView().setText("");

                }
            });
            pollEditTextCell.setTag(1);
            pollEditTextCell.getTextView().setTransformHintToHeader(true);
            pollEditTextCell.setTextAndHint("",field.placeholder,false);
            pollEditTextCell.getTextView().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s == null){
                        return;
                    }
                    String value = s.toString();
                    productTags.put(field.key,value);

                }
            });
            linearLayout.addView(pollEditTextCell,LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT,0,8,0,8));
        }
    }

}
