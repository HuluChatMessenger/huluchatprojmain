package org.plus.apps.business.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CloseProgressDrawable2;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class FakeSearchParent extends FrameLayout {

    public interface SearchDelegate{
        void onSearch(String search);
    }

    private SearchDelegate delegate;

    public void setDelegate(SearchDelegate delegate) {
        this.delegate = delegate;
    }

    private SearchField searchField;
    private FrameLayout fakeFocustaker;

        public FakeSearchParent(@NonNull Context context) {
            super(context);
            searchField = new SearchField(context);
            addView(searchField, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));


            fakeFocustaker = new FrameLayout(context);
            fakeFocustaker.setFocusable(true);
            fakeFocustaker.setFocusableInTouchMode(true);
            addView(fakeFocustaker, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 0));

        }

    public SearchField getSearchField() {
        return searchField;
    }

    public void hideKeyboard() {
            AndroidUtilities.hideKeyboard(searchField);
        }



        public class SearchField extends FrameLayout {

            private View searchBackground;
            private ImageView searchIconImageView;
            private ImageView clearSearchImageView;
            private CloseProgressDrawable2 progressDrawable;
            private EditTextBoldCursor searchEditText;
            private View backgroundView;

            public void setHint(String text){
                searchEditText.setHint(text);
            }


            public SearchField(Context context) {
                super(context);

                searchBackground = new View(context);
                searchBackground.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_dialogSearchBackground)));
                addView(searchBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 4, 0, 4, 0));

                searchIconImageView = new ImageView(context);
                searchIconImageView.setScaleType(ImageView.ScaleType.CENTER);
                searchIconImageView.setImageResource(R.drawable.smiles_inputsearch);
                searchIconImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon), PorterDuff.Mode.MULTIPLY));
                addView(searchIconImageView, LayoutHelper.createFrame(42, 42, Gravity.LEFT | Gravity.TOP, 10, 0, 0, 0));

                clearSearchImageView = new ImageView(context);
                clearSearchImageView.setScaleType(ImageView.ScaleType.CENTER);
                clearSearchImageView.setImageDrawable(progressDrawable = new CloseProgressDrawable2());
                progressDrawable.setSide(AndroidUtilities.dp(7));
                clearSearchImageView.setScaleX(0.1f);
                clearSearchImageView.setScaleY(0.1f);
                clearSearchImageView.setAlpha(0.0f);
                clearSearchImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogSearchIcon), PorterDuff.Mode.MULTIPLY));
                addView(clearSearchImageView, LayoutHelper.createFrame(42, 42, Gravity.RIGHT | Gravity.TOP, 14, 0, 14, 0));
                clearSearchImageView.setOnClickListener(v -> {
                    searchEditText.setText("");
                    AndroidUtilities.showKeyboard(searchEditText);
                });

                searchEditText = new EditTextBoldCursor(context);
                searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                searchEditText.setHintTextColor(Theme.getColor(Theme.key_dialogSearchHint));
                searchEditText.setTextColor(Theme.getColor(Theme.key_dialogSearchText));
                searchEditText.setBackgroundDrawable(null);
                searchEditText.setPadding(0, 0, 0, 0);
                searchEditText.setMaxLines(1);
                searchEditText.setLines(1);
                searchEditText.setSingleLine(true);


                searchEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

                searchEditText.setHint("Search in the seller listing");
                searchEditText.setCursorColor(Theme.getColor(Theme.key_featuredStickers_addedIcon));
                searchEditText.setCursorSize(AndroidUtilities.dp(20));
                searchEditText.setCursorWidth(1.5f);


                addView(searchEditText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 42, Gravity.LEFT | Gravity.TOP, 16 + 38, 0, 16 + 30, 0));
                searchEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        boolean show = searchEditText.length() > 0;
                        boolean showed = clearSearchImageView.getAlpha() != 0;
                        if (show != showed) {
                            clearSearchImageView.animate()
                                    .alpha(show ? 1.0f : 0.0f)
                                    .setDuration(150)
                                    .scaleX(show ? 1.0f : 0.1f)
                                    .scaleY(show ? 1.0f : 0.1f)
                                    .start();
                        }
                    }
                });

                searchEditText.setOnClickListener(v -> {

                });

                setOnClickListener(v -> {

                });
                searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                    if (event != null && (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        if(delegate != null){
                            delegate.onSearch(searchEditText.getText().toString());
                        }
                        AndroidUtilities.hideKeyboard(searchEditText);

                        View view = v.focusSearch(FOCUS_DOWN);
                        if (view != null) {
                            if (!view.requestFocus(FOCUS_DOWN)) {
                                return true;
                            }
                        }
                        return false;


                    }
                    return false;
                });


            }

            public void hideKeyboard() {
                AndroidUtilities.hideKeyboard(searchEditText);
            }
        }

    }