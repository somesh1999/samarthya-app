package org.samarthya.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.form.api.FormEntryPrompt;
import org.samarthya.collect.android.R;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.databinding.ExArbitraryFileWidgetAnswerBinding;
import org.samarthya.collect.android.formentry.questions.QuestionDetails;
import org.samarthya.collect.android.utilities.ActivityAvailability;
import org.samarthya.collect.android.utilities.ApplicationConstants;
import org.samarthya.collect.android.utilities.ExternalAppIntentProvider;
import org.samarthya.collect.android.utilities.MediaUtils;
import org.samarthya.collect.android.utilities.QuestionMediaManager;
import org.samarthya.collect.android.utilities.ToastUtils;
import org.samarthya.collect.android.widgets.BaseArbitraryFileWidget;
import org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class ExArbitraryFileWidget extends BaseArbitraryFileWidget {
    ExArbitraryFileWidgetAnswerBinding binding;

    private final ExternalAppIntentProvider externalAppIntentProvider;
    private final ActivityAvailability activityAvailability;

    public ExArbitraryFileWidget(Context context, QuestionDetails questionDetails, @NonNull MediaUtils mediaUtils,
                                 QuestionMediaManager questionMediaManager, WaitingForDataRegistry waitingForDataRegistry,
                                 ExternalAppIntentProvider externalAppIntentProvider, ActivityAvailability activityAvailability) {
        super(context, questionDetails, mediaUtils, questionMediaManager, waitingForDataRegistry);
        this.externalAppIntentProvider = externalAppIntentProvider;
        this.activityAvailability = activityAvailability;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = ExArbitraryFileWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        setupAnswerFile(prompt.getAnswerText());

        binding.exArbitraryFileButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.exArbitraryFileAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (questionDetails.isReadOnly()) {
            binding.exArbitraryFileButton.setVisibility(GONE);
        } else {
            binding.exArbitraryFileButton.setOnClickListener(v -> onButtonClick());
            binding.exArbitraryFileAnswerText.setOnClickListener(v -> mediaUtils.openFile(getContext(), answerFile, null));
        }

        if (answerFile != null) {
            binding.exArbitraryFileAnswerText.setText(answerFile.getName());
            binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
        }

        return binding.getRoot();
    }

    @Override
    public void clearAnswer() {
        binding.exArbitraryFileAnswerText.setVisibility(GONE);
        deleteFile();
        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener listener) {
        binding.exArbitraryFileButton.setOnLongClickListener(listener);
        binding.exArbitraryFileAnswerText.setOnLongClickListener(listener);
    }

    @Override
    protected void showAnswerText() {
        binding.exArbitraryFileAnswerText.setText(answerFile.getName());
        binding.exArbitraryFileAnswerText.setVisibility(VISIBLE);
    }

    @Override
    protected void hideAnswerText() {
        binding.exArbitraryFileAnswerText.setVisibility(GONE);
    }

    private void onButtonClick() {
        waitingForDataRegistry.waitForData(getFormEntryPrompt().getIndex());
        try {
            Intent intent = externalAppIntentProvider.getIntentToRunExternalApp(getContext(), getFormEntryPrompt(), activityAvailability, Collect.getInstance().getPackageManager());
            fireActivityForResult(intent);
        } catch (Exception | Error e) {
            ToastUtils.showLongToast(e.getMessage());
        }
    }

    private void fireActivityForResult(Intent intent) {
        try {
            ((Activity) getContext()).startActivityForResult(intent, ApplicationConstants.RequestCodes.EX_ARBITRARY_FILE_CHOOSER);
        } catch (SecurityException e) {
            Timber.i(e);
            ToastUtils.showLongToast(R.string.not_granted_permission);
        }
    }
}