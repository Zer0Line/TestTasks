package ru.gurucode.emailtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailTextView extends android.support.v7.widget.AppCompatTextView {

    private WeakReference<Context> mContext;

    public boolean mFoolEmailInChip = true;

    Drawable mDrawableBackground = null;

    public OnChipClickListener onChipClickListener;

    private List<String> mEmailsInText = new ArrayList<>();
    private List<Drawable> mAvatarsBitmap = new ArrayList<>();

    private String mCurrentText = null;

    public EmailTextView(final Context context, AttributeSet attrs)
            throws IOException, ExecutionException, InterruptedException {

        super(context, attrs);

        //Текст из xml
        mCurrentText = attrs.getAttributeValue(
                "http://schemas.android.com/apk/res/android",
                "text");

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ext);
        final int attrsCount = attributes.getIndexCount();
        for (int i = 0; i < attrsCount; ++i) {

            int attr = attributes.getIndex(i);
            if (attr == R.styleable.ext_full_email) {
                mFoolEmailInChip = (attributes.getBoolean(i, false));
            }
            if (attr == R.styleable.ext_chip_background) {
                mDrawableBackground = (attributes.getDrawable(i));
            }
            if (mDrawableBackground == null) {
                mDrawableBackground = getResources().getDrawable(R.drawable.default_background);
            }

        }
        attributes.recycle();

        if (context.isRestricted()) {
            throw new IllegalStateException("The android:onClick attribute cannot "
                    + "be used within a restricted context");
        }

        this.setMovementMethod(LinkMovementMethod.getInstance());
        mContext = new WeakReference<>(context);

        if(mCurrentText.isEmpty()){
            mCurrentText = "тестовое задание oblomov@mail.ru";
        }

        findEmails(mCurrentText);
        downloadAvatars();
    }

    public void setText(String str) {
        mCurrentText = str;
        if (mCurrentText != null) {
            findEmails(mCurrentText);
            try {
                downloadAvatars();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Установить Drawable бэкграунд
    public void setBackground(Drawable background) {
        mDrawableBackground = background;
    }

    //Найти email'ы в строке
    private void findEmails(String str) {
        Matcher matcher = android.util.Patterns.EMAIL_ADDRESS.matcher(str);
        while (matcher.find()) {
            mEmailsInText.add(matcher.group());
        }
    }

    //Загрузить аватары для найденных email'ов
    private void downloadAvatars()
            throws ExecutionException, InterruptedException {
        for (int i = 0; i < mEmailsInText.size(); i++) {
            String emailHash = util.getMd5Hash(mEmailsInText.get(i));
            downLoadAvatar("https://www.gravatar.com/avatar/" + emailHash);
        }

    }

    //Обработать загрузку всех аватаров
    private void onAvatarsLoadReady()
            throws InterruptedException, ExecutionException, IOException {

        SpannableString[] chips = new SpannableString[mEmailsInText.size()];
        for (int i = 0; i < mEmailsInText.size(); i++) {
            String chipText;

            if (mFoolEmailInChip) {
                chipText = mEmailsInText.get(i);
            } else {
                chipText = mEmailsInText.get(i).split("@")[0];
            }

            chips[i] = createChipString(chipText, mEmailsInText.get(i),
                    mContext.get(), mAvatarsBitmap.get(i));
        }

        SpannableStringBuilder outputText = new SpannableStringBuilder(mCurrentText);

        for (int i = 0; i < mEmailsInText.size(); i++) {
            Pattern pattern = Pattern.compile(mEmailsInText.get(i));
            Matcher matcher = pattern.matcher(outputText);
            while (matcher.find()) {
                outputText.replace(matcher.start(), matcher.end(), chips[i]);
            }
        }

        super.setText(outputText);
    }

    //Создать чип
    private SpannableString createChipString(
            final String chipText,
            final String email,
            Context ctx,
            Drawable bitmap)
            throws IOException, ExecutionException, InterruptedException {

        ChipSpan chip = new ChipSpan(ctx, chipText, bitmap, mDrawableBackground);

        SpannableString chipString = new SpannableString(" ");
        chipString.setSpan(chip, 0, 1, 0);
        chipString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                onChipClickListener.onChipClick(email);
            }
        }, 0, chipString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return chipString;
    }

    //Загрузка 1-го аватара.
    private void downLoadAvatar(String base_url) throws
            ExecutionException,
            InterruptedException {

        Glide.with(this)
                .asBitmap()
                .load(base_url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        Drawable drawable = new BitmapDrawable(getResources()
                                , util.getCroppedBitmap(resource));
                        try {
                            mAvatarsBitmap.add(drawable);
                            if (mAvatarsBitmap.size() == mEmailsInText.size()) {
                                onAvatarsLoadReady();
                            }
                        } catch (InterruptedException | ExecutionException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}