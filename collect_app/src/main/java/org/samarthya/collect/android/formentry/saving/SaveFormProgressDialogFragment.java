package org.samarthya.collect.android.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import org.samarthya.collect.android.R;
import org.samarthya.collect.analytics.Analytics;
import org.samarthya.collect.android.formentry.saving.FormSaveViewModel;
import org.samarthya.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.samarthya.collect.android.injection.DaggerUtils;
import org.samarthya.collect.async.Scheduler;

import javax.inject.Inject;

import static org.samarthya.collect.android.formentry.saving.FormSaveViewModel.SaveResult.State.SAVING;

public class SaveFormProgressDialogFragment extends ProgressDialogFragment {

    @Inject
    Analytics analytics;

    @Inject
    Scheduler scheduler;

    @Inject
    org.samarthya.collect.android.formentry.saving.FormSaveViewModel.FactoryFactory formSaveViewModelFactoryFactory;

    private org.samarthya.collect.android.formentry.saving.FormSaveViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        ViewModelProvider.Factory factory = formSaveViewModelFactoryFactory.create(requireActivity(), null);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(FormSaveViewModel.class);

        setCancelable(false);
        setTitle(getString(R.string.saving_form));

        viewModel.getSaveResult().observe(this, result -> {
            if (result != null && result.getState() == SAVING && result.getMessage() != null) {
                setMessage(getString(R.string.please_wait) + "\n\n" + result.getMessage());
            } else {
                setMessage(getString(R.string.please_wait));
            }
        });
    }

    @Override
    protected Cancellable getCancellable() {
        return viewModel;
    }
}
