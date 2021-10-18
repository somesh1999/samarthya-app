package org.odk.collect.android.widgets.items;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.databinding.SelectListWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.SelectItemClickListener;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;
import org.odk.collect.android.widgets.utilities.SearchQueryViewModel;

import static org.odk.collect.android.analytics.AnalyticsEvents.PROMPT;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;

public abstract class BaseSelectListWidget extends ItemsWidget implements MultiChoiceWidget, SelectItemClickListener {

    SelectListWidgetAnswerBinding binding;
    protected AbstractSelectListAdapter recyclerViewAdapter;

    public BaseSelectListWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        logAnalytics(questionDetails);
        binding.choicesRecyclerView.initRecyclerView(setUpAdapter(), Appearances.isFlexAppearance(getQuestionDetails().getPrompt()));
        if (Appearances.isAutocomplete(getQuestionDetails().getPrompt())) {
            setUpSearchBox();
        }
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectListWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void setFocus(Context context) {
        if (Appearances.isAutocomplete(getQuestionDetails().getPrompt()) && !questionDetails.isReadOnly()) {
            softKeyboardController.showSoftKeyboard(binding.choicesSearchBox);
        }
    }

    @Override
    public void clearAnswer() {
        recyclerViewAdapter.clearAnswer();
        widgetValueChanged();
    }

    @Override
    public int getChoiceCount() {
        return recyclerViewAdapter.getItemCount();
    }

    private void setUpSearchBox() {
        ComponentActivity activity = (ComponentActivity) getContext();
        SearchQueryViewModel searchQueryViewModel = new ViewModelProvider(activity).get(SearchQueryViewModel.class);

        binding.choicesSearchBox.setVisibility(View.VISIBLE);
        binding.choicesSearchBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        binding.choicesSearchBox.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    recyclerViewAdapter.getFilter().filter(s.toString());
                    searchQueryViewModel.setQuery(getFormEntryPrompt().getIndex().toString(), s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        binding.choicesSearchBox.setText(searchQueryViewModel.getQuery(getFormEntryPrompt().getIndex().toString()));
    }

    private void logAnalytics(QuestionDetails questionDetails) {
        if (items != null) {
            for (SelectChoice choice : items) {
                String audioURI = getPlayableAudioURI(questionDetails.getPrompt(), choice, getReferenceManager());

                if (audioURI != null) {
                    analytics.logEvent(PROMPT, "AudioChoice", questionDetails.getFormAnalyticsID());
                    break;
                }
            }
        }
    }

    protected abstract AbstractSelectListAdapter setUpAdapter();
}
