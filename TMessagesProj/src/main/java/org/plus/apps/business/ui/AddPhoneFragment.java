package org.plus.apps.business.ui;

import android.content.Context;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.HintEditText;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.CountrySelectActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class AddPhoneFragment extends BaseFragment {

    private final static int FIELD_PHONECOUNTRY = 0;
    private final static int FIELD_PHONECODE = 1;
    private final static int FIELD_PHONE = 2;
    private boolean ignoreOnTextChange;
    private boolean ignoreOnPhoneChange;
    private TextView plusTextView;
    private ArrayList<View> dividers = new ArrayList<>();
    private LinearLayout linearLayout2;
    private boolean useCurrentValue = true;
    private String currentPhone;

    private ArrayList<String> countriesArray = new ArrayList<>();
    private HashMap<String, String> countriesMap = new HashMap<>();
    private HashMap<String, String> codesMap = new HashMap<>();
    private HashMap<String, String> phoneFormatMap = new HashMap<>();
    private HashMap<String, String> languageMap;
    private TextCheckCell textSettingsCell;
    EditTextBoldCursor[] inputFields;

    private ArrayList<String> phoneNumber = new ArrayList<>();

    public interface Delegate{

        void setPhoneNumbers(ArrayList<String> phoneNumber);
    }

    private final static int done_button = 1;

    private Delegate delegate;

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    private View doneButton;
    private boolean donePressed;

    private ArrayList<String> phoneNumbers  = new ArrayList<>(2);


    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Phone number");

        ActionBarMenu menu = actionBar.createMenu();
        doneButton = menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }else if(id == done_button){
                    if (donePressed || getParentActivity() == null) {
                        return;
                    }

                    if(useCurrentValue){
                        phoneNumbers.add(UserConfig.getInstance(currentAccount).getClientPhone());
                    }

                    if(inputFields != null &&  inputFields[FIELD_PHONE] != null)
                    {
                        String phone = PhoneFormat.stripExceptNumbers("" + inputFields[FIELD_PHONECODE].getText() + inputFields[FIELD_PHONE].getText());
                        if(!phoneNumbers.contains(phone)){
                            phoneNumbers.add(phone);
                        }
                    }

                    if (inputFields != null && inputFields[FIELD_PHONE] != null && !useCurrentValue && TextUtils.isEmpty(inputFields[FIELD_PHONE].getText())) {
                        if (inputFields[FIELD_PHONE] != null) {
                            Vibrator v = (Vibrator) getParentActivity().getSystemService(Context.VIBRATOR_SERVICE);
                            if (v != null) {
                                v.vibrate(200);
                            }
                            AndroidUtilities.shakeView(inputFields[FIELD_PHONE], 2, 0);
                        }
                        return;
                    }

                    if(delegate != null){
                        delegate.setPhoneNumbers(phoneNumbers);
                    }
                    finishFragment();

                }
            }
        });
        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        createPhoneInterface(context);
        frameLayout.addView(linearLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT,LayoutHelper.WRAP_CONTENT));

        return fragmentView;
    }

    private void createPhoneInterface(Context context) {

        linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        TextInfoPrivacyCell bottomCell;
        HeaderCell headerCell;

        actionBar.setTitle(LocaleController.getString("PassportPhone", R.string.PassportPhone));

        languageMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("countries.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] args = line.split(";");
                countriesArray.add(0, args[2]);
                countriesMap.put(args[2], args[0]);
                codesMap.put(args[0], args[2]);
                if (args.length > 3) {
                    phoneFormatMap.put(args[0], args[3]);
                }
                languageMap.put(args[1], args[2]);
            }
            reader.close();
        } catch (Exception e) {
            FileLog.e(e);
        }

        Collections.sort(countriesArray, String::compareTo);
        String currentPhone = UserConfig.getInstance(currentAccount).getCurrentUser().phone;


        textSettingsCell = new TextCheckCell(context);
        textSettingsCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        textSettingsCell.setTextAndCheck("Use " + PhoneFormat.getInstance().format("+" + currentPhone),useCurrentValue,true);
        textSettingsCell.setOnClickListener(v -> {
            useCurrentValue = !useCurrentValue;
            ((TextCheckCell) v).setChecked(useCurrentValue);
        });
        linearLayout2.addView(textSettingsCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));




        bottomCell = new TextInfoPrivacyCell(context);
        bottomCell.setBackgroundDrawable(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
        bottomCell.setText(LocaleController.getString("PassportPhoneUseSameInfo", R.string.PassportPhoneUseSameInfo));
        linearLayout2.addView(bottomCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        headerCell = new HeaderCell(context);
        headerCell.setText("or add new  phone number");
        headerCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        linearLayout2.addView(headerCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        inputFields = new EditTextBoldCursor[3];
        for (int a = 0; a < 3; a++) {

            if (a == FIELD_PHONE) {
                inputFields[a] = new HintEditText(context);
            } else {
                inputFields[a] = new EditTextBoldCursor(context);
            }

            ViewGroup container;
            if (a == FIELD_PHONECODE) {
                container = new LinearLayout(context);
                ((LinearLayout) container).setOrientation(LinearLayout.HORIZONTAL);
                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else if (a == FIELD_PHONE) {
                container = (ViewGroup) inputFields[FIELD_PHONECODE].getParent();
            } else {
                container = new FrameLayout(context);
                linearLayout2.addView(container, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
                container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }

            inputFields[a].setTag(a);
            inputFields[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            inputFields[a].setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
            inputFields[a].setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setBackgroundDrawable(null);
            inputFields[a].setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            inputFields[a].setCursorSize(AndroidUtilities.dp(20));
            inputFields[a].setCursorWidth(1.5f);
            if (a == FIELD_PHONECOUNTRY) {
                inputFields[a].setOnTouchListener((v, event) -> {
                    if (getParentActivity() == null) {
                        return false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        CountrySelectActivity fragment = new CountrySelectActivity(false);
                        fragment.setCountrySelectActivityDelegate((name, shortName) -> {
                            inputFields[FIELD_PHONECOUNTRY].setText(name);
                            int index = countriesArray.indexOf(name);
                            if (index != -1) {
                                ignoreOnTextChange = true;
                                String code = countriesMap.get(name);
                                inputFields[FIELD_PHONECODE].setText(code);
                                String hint = phoneFormatMap.get(code);
                                inputFields[FIELD_PHONE].setHintText(hint != null ? hint.replace('X', '–') : null);
                                ignoreOnTextChange = false;
                            }
                            AndroidUtilities.runOnUIThread(() -> AndroidUtilities.showKeyboard(inputFields[FIELD_PHONE]), 300);
                            inputFields[FIELD_PHONE].requestFocus();
                            inputFields[FIELD_PHONE].setSelection(inputFields[FIELD_PHONE].length());
                        });
                        presentFragment(fragment);
                    }
                    return true;
                });
                inputFields[a].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
                inputFields[a].setInputType(0);
                inputFields[a].setFocusable(false);
            } else {
                inputFields[a].setInputType(InputType.TYPE_CLASS_PHONE);
                if (a == FIELD_PHONE) {
                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                } else {
                    inputFields[a].setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                }
            }
            inputFields[a].setSelection(inputFields[a].length());

            if (a == FIELD_PHONECODE) {
                plusTextView = new TextView(context);
                plusTextView.setText("+");
                plusTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                plusTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                container.addView(plusTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 21, 12, 0, 6));

                inputFields[a].setPadding(AndroidUtilities.dp(10), 0, 0, 0);
                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = new InputFilter.LengthFilter(5);
                inputFields[a].setFilters(inputFilters);
                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                container.addView(inputFields[a], LayoutHelper.createLinear(55, LayoutHelper.WRAP_CONTENT, 0, 12, 16, 6));
                inputFields[a].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (ignoreOnTextChange) {
                            return;
                        }
                        ignoreOnTextChange = true;
                        String text = PhoneFormat.stripExceptNumbers(inputFields[FIELD_PHONECODE].getText().toString());
                        inputFields[FIELD_PHONECODE].setText(text);
                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
                        if (text.length() == 0) {
                            phoneField.setHintText(null);
                            phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
                            inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("ChooseCountry", R.string.ChooseCountry));
                        } else {
                            String country;
                            boolean ok = false;
                            String textToSet = null;
                            if (text.length() > 4) {
                                for (int a = 4; a >= 1; a--) {
                                    String sub = text.substring(0, a);
                                    country = codesMap.get(sub);
                                    if (country != null) {
                                        ok = true;
                                        textToSet = text.substring(a) + inputFields[FIELD_PHONE].getText().toString();
                                        inputFields[FIELD_PHONECODE].setText(text = sub);
                                        break;
                                    }
                                }
                                if (!ok) {
                                    textToSet = text.substring(1) + inputFields[FIELD_PHONE].getText().toString();
                                    inputFields[FIELD_PHONECODE].setText(text = text.substring(0, 1));
                                }
                            }
                            country = codesMap.get(text);
                            boolean set = false;
                            if (country != null) {
                                int index = countriesArray.indexOf(country);
                                if (index != -1) {
                                    inputFields[FIELD_PHONECOUNTRY].setText(countriesArray.get(index));
                                    String hint = phoneFormatMap.get(text);
                                    set = true;
                                    if (hint != null) {
                                        phoneField.setHintText(hint.replace('X', '–'));
                                        phoneField.setHint(null);
                                    }
                                }
                            }
                            if (!set) {
                                phoneField.setHintText(null);
                                phoneField.setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
                                inputFields[FIELD_PHONECOUNTRY].setText(LocaleController.getString("WrongCountry", R.string.WrongCountry));
                            }
                            if (!ok) {
                                inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].getText().length());
                            }

                            if (textToSet != null) {
                                phoneField.requestFocus();
                                phoneField.setText(textToSet);
                                phoneField.setSelection(phoneField.length());

                            }
                        }
                        ignoreOnTextChange = false;
                    }
                });
            } else if (a == FIELD_PHONE) {
                inputFields[a].setPadding(0, 0, 0, 0);
                inputFields[a].setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                inputFields[a].setHintText(null);
                inputFields[a].setHint(LocaleController.getString("PaymentShippingPhoneNumber", R.string.PaymentShippingPhoneNumber));
                container.addView(inputFields[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 21, 6));
                inputFields[a].addTextChangedListener(new TextWatcher() {
                    private int characterAction = -1;
                    private int actionPosition;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        if (count == 0 && after == 1) {
                            characterAction = 1;
                        } else if (count == 1 && after == 0) {
                            if (s.charAt(start) == ' ' && start > 0) {
                                characterAction = 3;
                                actionPosition = start - 1;
                            } else {
                                characterAction = 2;
                            }
                        } else {
                            characterAction = -1;
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (ignoreOnPhoneChange) {
                            return;
                        }
                        HintEditText phoneField = (HintEditText) inputFields[FIELD_PHONE];
                        int start = phoneField.getSelectionStart();
                        String phoneChars = "0123456789";
                        String str = phoneField.getText().toString();
                        if (characterAction == 3) {
                            str = str.substring(0, actionPosition) + str.substring(actionPosition + 1);
                            start--;
                        }
                        StringBuilder builder = new StringBuilder(str.length());
                        for (int a = 0; a < str.length(); a++) {
                            String ch = str.substring(a, a + 1);
                            if (phoneChars.contains(ch)) {
                                builder.append(ch);
                            }
                        }
                        ignoreOnPhoneChange = true;
                        String hint = phoneField.getHintText();
                        if (hint != null) {
                            for (int a = 0; a < builder.length(); a++) {
                                if (a < hint.length()) {
                                    if (hint.charAt(a) == ' ') {
                                        builder.insert(a, ' ');
                                        a++;
                                        if (start == a && characterAction != 2 && characterAction != 3) {
                                            start++;
                                        }
                                    }
                                } else {
                                    builder.insert(a, ' ');
                                    if (start == a + 1 && characterAction != 2 && characterAction != 3) {
                                        start++;
                                    }
                                    break;
                                }
                            }
                        }
                        phoneField.setText(builder);
                        if (start >= 0) {
                            phoneField.setSelection(Math.min(start, phoneField.length()));
                        }
                        phoneField.onTextChange();
                        ignoreOnPhoneChange = false;
                    }
                });
            } else {
                inputFields[a].setPadding(0, 0, 0, AndroidUtilities.dp(6));
                inputFields[a].setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
                container.addView(inputFields[a], LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 21, 12, 21, 6));
            }
            inputFields[a].setOnEditorActionListener((textView, i, keyEvent) -> {
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    inputFields[FIELD_PHONE].requestFocus();
                    return true;
                } else if (i == EditorInfo.IME_ACTION_DONE) {
                    doneButton.callOnClick();
                    return true;
                }
                return false;
            });
            if (a == FIELD_PHONE) {
                inputFields[a].setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_DEL && inputFields[FIELD_PHONE].length() == 0) {
                        inputFields[FIELD_PHONECODE].requestFocus();
                        inputFields[FIELD_PHONECODE].setSelection(inputFields[FIELD_PHONECODE].length());
                        inputFields[FIELD_PHONECODE].dispatchKeyEvent(event);
                        return true;
                    }
                    return false;
                });
            }

            if (a == FIELD_PHONECOUNTRY) {
                View divider = new View(context);
                dividers.add(divider);
                divider.setBackgroundColor(Theme.getColor(Theme.key_divider));
                container.addView(divider, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, Gravity.LEFT | Gravity.BOTTOM));
            }
        }

        String country = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) ApplicationLoader.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                country = telephonyManager.getSimCountryIso().toUpperCase();
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (country != null) {
            String countryName = languageMap.get(country);
            if (countryName != null) {
                int index = countriesArray.indexOf(countryName);
                if (index != -1) {
                    inputFields[FIELD_PHONECODE].setText(countriesMap.get(countryName));
                }
            }
        }

    }




}
