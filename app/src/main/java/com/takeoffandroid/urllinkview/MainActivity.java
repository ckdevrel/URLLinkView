package com.takeoffandroid.urllinkview;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.takeoffandroid.urllinkview.library.LinkViewCallback;
import com.takeoffandroid.urllinkview.library.LinkSourceContent;
import com.takeoffandroid.urllinkview.library.TextCrawler;

import java.util.List;
import java.util.Random;


@SuppressWarnings("unused")
public class MainActivity extends Activity {

    private EditText editText;

    private Button submitButton;

    private Context context;

    private TextCrawler textCrawler;
    private ViewGroup dropPreview;

    private String currentTitle, currentUrl, currentCannonicalUrl,
            currentDescription;

    private Bitmap[] currentImageSet;
    private Bitmap currentImage;
    private int currentItem = 0;
    private int countBigImages = 0;
    private boolean noThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        setContentView(R.layout.main);

        editText = (EditText) findViewById(R.id.input);

        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5f8ee4")));
        /** --- From ShareVia Intent */
        if (getIntent().getExtras() != null) {
            String shareVia = (String) getIntent().getExtras().get(Intent.EXTRA_TEXT);
            if (shareVia != null) {
                editText.setText(shareVia);
            }
        }
        if (getIntent().getAction() == Intent.ACTION_VIEW) {
            Uri data = getIntent().getData();
            String scheme = data.getScheme();
            String host = data.getHost();
            List<String> params = data.getPathSegments();
            String builded = scheme + "://" + host + "/";

            for (String string : params) {
                builded += string + "/";
            }

            if (data.getQuery() != null && !data.getQuery().equals("")) {
                builded = builded.substring(0, builded.length() - 1);
                builded += "?" + data.getQuery();
            }

            System.out.println(builded);

            editText.setText(builded);

        }
        /** --- */

        submitButton = (Button) findViewById(R.id.action_go);

        /** Where the previews will be dropped */
        dropPreview = (ViewGroup) findViewById(R.id.drop_preview);

        /** Where the previews will be dropped */

        textCrawler = new TextCrawler();

        initSubmitButton();

    }


    /**
     * Adding listener to the button
     */
    public void initSubmitButton() {
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                textCrawler
                        .makePreview(callback, editText.getText().toString());
            }
        });
    }

    /** Callback to update your view. Totally customizable. */
    /** onBeforeLoading() will be called before the crawling. onAfterLoading() after. */
    /**
     * You can customize this to update your view
     */
    private LinkViewCallback callback = new LinkViewCallback() {
        /**
         * This view is used to be updated or added in the layout after getting
         * the result
         */
        private View mainView;
        private LinearLayout linearLayout;
        private View loading;
        private ImageView imageView;

        @Override
        public void onBeforeLoading() {
            hideSoftKeyboard();

            currentImageSet = null;
            currentItem = 0;


            currentImage = null;
            noThumb = false;
            currentTitle = currentDescription = currentUrl = currentCannonicalUrl = "";

            submitButton.setEnabled(false);

            /** Inflating the preview layout */
            mainView = getLayoutInflater().inflate(R.layout.main_view, null);

            linearLayout = (LinearLayout) mainView.findViewById(R.id.external);

            /**
             * Inflating a loading layout into MainActivity View LinearLayout
             */


            loading = getLayoutInflater().inflate(R.layout.loading,
                    linearLayout);

            dropPreview.addView(mainView);
        }

        @Override
        public void onAfterLoading(final LinkSourceContent linkSourceContent, boolean isNull) {

            /** Removing the loading layout */
            linearLayout.removeAllViews();

            if (isNull || linkSourceContent.getFinalUrl().equals("")) {
                /**
                 * Inflating the content layout into MainActivity View LinearLayout
                 */
                View failed = getLayoutInflater().inflate(R.layout.failed,
                        linearLayout);

                TextView titleTextView = (TextView) failed
                        .findViewById(R.id.text);
                titleTextView.setText(getString(R.string.failed_preview) + "\n"
                        + linkSourceContent.getFinalUrl());

                failed.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        releasePreviewArea();
                    }
                });

            } else {
//                postButton.setVisibility(View.VISIBLE);

                currentImageSet = new Bitmap[linkSourceContent.getImages().size()];

                /**
                 * Inflating the content layout into MainActivity View LinearLayout
                 */
                final View content = getLayoutInflater().inflate(
                        R.layout.preview_content, linearLayout);

                /** Fullfilling the content layout */
                final LinearLayout infoWrap = (LinearLayout) content
                        .findViewById(R.id.info_wrap);
                final LinearLayout titleWrap = (LinearLayout) infoWrap
                        .findViewById(R.id.title_wrap);

                final ImageView imageSet = (ImageView) content
                        .findViewById(R.id.image_post_set);

                final ImageView close = (ImageView ) titleWrap
                        .findViewById(R.id.close);
                final TextView titleTextView = (TextView) titleWrap
                        .findViewById(R.id.title);
                final TextView titleEditText = (TextView) titleWrap
                        .findViewById(R.id.input_title);
                final TextView urlTextView = (TextView) content
                        .findViewById(R.id.url);
                final TextView descriptionTextView = (TextView) content
                        .findViewById(R.id.description);
                final TextView descriptionEditText = (TextView) content
                        .findViewById(R.id.input_description);

                titleTextView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        titleTextView.setVisibility(View.GONE);

                        titleEditText.setText(TextCrawler
                                .extendedTrim(titleTextView.getText()
                                        .toString()));
                        titleEditText.setVisibility(View.VISIBLE);
                    }
                });
                titleEditText
                        .setOnEditorActionListener(new OnEditorActionListener() {

                            @Override
                            public boolean onEditorAction(TextView arg0,
                                                          int arg1, KeyEvent arg2) {

                                if (arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                                    titleEditText.setVisibility(View.GONE);

                                    currentTitle = TextCrawler
                                            .extendedTrim(titleEditText
                                                    .getText().toString());

                                    titleTextView.setText(currentTitle);
                                    titleTextView.setVisibility(View.VISIBLE);

                                    hideSoftKeyboard();
                                }

                                return false;
                            }
                        });
                descriptionTextView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        descriptionTextView.setVisibility(View.GONE);

                        descriptionEditText.setText(TextCrawler
                                .extendedTrim(descriptionTextView.getText()
                                        .toString()));
                        descriptionEditText.setVisibility(View.VISIBLE);
                    }
                });
                descriptionEditText
                        .setOnEditorActionListener(new OnEditorActionListener() {

                            @Override
                            public boolean onEditorAction(TextView arg0,
                                                          int arg1, KeyEvent arg2) {

                                if (arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                                    descriptionEditText
                                            .setVisibility(View.GONE);

                                    currentDescription = TextCrawler
                                            .extendedTrim(descriptionEditText
                                                    .getText().toString());

                                    descriptionTextView
                                            .setText(currentDescription);
                                    descriptionTextView
                                            .setVisibility(View.VISIBLE);

                                    hideSoftKeyboard();
                                }

                                return false;
                            }
                        });

                close.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        releasePreviewArea();
                    }
                });


                if (linkSourceContent.getImages().size() > 0) {


                    UrlImageViewHelper.setUrlDrawable(imageSet, linkSourceContent
                            .getImages().get(0), new UrlImageViewCallback() {

                        @Override
                        public void onLoaded(ImageView imageView,
                                             Bitmap loadedBitmap, String url,
                                             boolean loadedFromCache) {
                            if (loadedBitmap != null) {
                                currentImage = loadedBitmap;
                                currentImageSet[0] = loadedBitmap;
                            }
                        }
                    });

                } else {
                    showHideImage(imageSet, infoWrap, false);
                }

                if (linkSourceContent.getTitle().equals(""))
                    linkSourceContent.setTitle(getString(R.string.enter_title));
                if (linkSourceContent.getDescription().equals(""))
                    linkSourceContent
                            .setDescription(getString(R.string.enter_description));

                titleTextView.setText(linkSourceContent.getTitle());
                urlTextView.setText(linkSourceContent.getCannonicalUrl());
                descriptionTextView.setText(linkSourceContent.getDescription());

            }

            currentTitle = linkSourceContent.getTitle();
            currentDescription = linkSourceContent.getDescription();
            currentUrl = linkSourceContent.getUrl();
            currentCannonicalUrl = linkSourceContent.getCannonicalUrl();

            Log.i("URL",currentUrl);
        }
    };

    /**
     * Change the current image in image set
     */
    private void changeImage(Button previousButton, Button forwardButton,
                             final int index, LinkSourceContent linkSourceContent,
                             TextView countTextView, ImageView imageSet, String url,
                             final int current) {

        if (currentImageSet[index] != null) {
            currentImage = currentImageSet[index];
            imageSet.setImageBitmap(currentImage);
        } else {
            UrlImageViewHelper.setUrlDrawable(imageSet, url,
                    new UrlImageViewCallback() {

                        @Override
                        public void onLoaded(ImageView imageView,
                                             Bitmap loadedBitmap, String url,
                                             boolean loadedFromCache) {
                            if (loadedBitmap != null) {
                                currentImage = loadedBitmap;
                                currentImageSet[index] = loadedBitmap;
                            }
                        }
                    });

        }

        currentItem = index;

        if (index == 0)
            previousButton.setEnabled(false);
        else
            previousButton.setEnabled(true);

        if (index == linkSourceContent.getImages().size() - 1)
            forwardButton.setEnabled(false);
        else
            forwardButton.setEnabled(true);

        countTextView.setText((index + 1) + " " + getString(R.string.of) + " "
                + linkSourceContent.getImages().size());
    }

    /**
     * Show or hide the image layout according to the "No Thumbnail" ckeckbox
     */
    private void showHideImage(View image, View parent, boolean show) {
        if (show) {
            image.setVisibility(View.VISIBLE);
            parent.setPadding(5, 5, 5, 5);
            parent.setLayoutParams(new LayoutParams(0,
                    LayoutParams.WRAP_CONTENT, 2f));
        } else {
            image.setVisibility(View.GONE);
            parent.setPadding(5, 5, 5, 5);
            parent.setLayoutParams(new LayoutParams(0,
                    LayoutParams.WRAP_CONTENT, 3f));
        }
    }

    /**
     * Hide keyboard
     */
    private void hideSoftKeyboard() {
        hideSoftKeyboard(editText);
    }

    private void hideSoftKeyboard(EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager
                .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * Just a set of urls
     */
    private final String[] RANDOM_URLS = {
            "http://vnexpress.net/ ",
            "http://facebook.com/ ",
            "http://gmail.com",
            "http://goo.gl/jKCPgp",
            "http://www3.nhk.or.jp/",
            "http://habrahabr.ru",
            "http://www.youtube.com/watch?v=cv2mjAgFTaI",
            "http://vimeo.com/67992157",
            "https://lh6.googleusercontent.com/-aDALitrkRFw/UfQEmWPMQnI/AAAAAAAFOlQ/mDh1l4ej15k/w337-h697-no/db1969caa4ecb88ef727dbad05d5b5b3.jpg",
            "http://www.nasa.gov/", "http://twitter.com",
            "http://bit.ly/14SD1eR"};

    /**
     * Returns a random url
     */
    private String getRandomUrl() {
        int random = new Random().nextInt(RANDOM_URLS.length);
        return RANDOM_URLS[random];
    }

    private void releasePreviewArea() {
        submitButton.setEnabled(true);
        dropPreview.removeAllViews();
    }
}