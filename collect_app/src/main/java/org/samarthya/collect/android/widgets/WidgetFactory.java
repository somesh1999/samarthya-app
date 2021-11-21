/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.samarthya.collect.android.widgets;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import androidx.lifecycle.LifecycleOwner;

import org.javarosa.core.model.Constants;
import org.javarosa.form.api.FormEntryPrompt;
import org.samarthya.collect.android.application.Collect;
import org.samarthya.collect.android.formentry.FormEntryViewModel;
import org.samarthya.collect.android.formentry.questions.QuestionDetails;
import org.samarthya.collect.android.geo.MapProvider;
import org.samarthya.collect.android.permissions.PermissionsProvider;
import org.samarthya.collect.android.storage.StoragePathProvider;
import org.samarthya.collect.android.utilities.ActivityAvailability;
import org.samarthya.collect.android.utilities.Appearances;
import org.samarthya.collect.android.utilities.CameraUtils;
import org.samarthya.collect.android.utilities.ExternalAppIntentProvider;
import org.samarthya.collect.android.utilities.ExternalWebPageHelper;
import org.samarthya.collect.android.utilities.MediaUtils;
import org.samarthya.collect.android.utilities.QuestionMediaManager;
import org.samarthya.collect.android.widgets.AnnotateWidget;
import org.samarthya.collect.android.widgets.ArbitraryFileWidget;
import org.samarthya.collect.android.widgets.AudioWidget;
import org.samarthya.collect.android.widgets.BarcodeWidget;
import org.samarthya.collect.android.widgets.BearingWidget;
import org.samarthya.collect.android.widgets.DateTimeWidget;
import org.samarthya.collect.android.widgets.DateWidget;
import org.samarthya.collect.android.widgets.DecimalWidget;
import org.samarthya.collect.android.widgets.DrawWidget;
import org.samarthya.collect.android.widgets.ExArbitraryFileWidget;
import org.samarthya.collect.android.widgets.ExAudioWidget;
import org.samarthya.collect.android.widgets.ExDecimalWidget;
import org.samarthya.collect.android.widgets.ExImageWidget;
import org.samarthya.collect.android.widgets.ExIntegerWidget;
import org.samarthya.collect.android.widgets.ExPrinterWidget;
import org.samarthya.collect.android.widgets.ExStringWidget;
import org.samarthya.collect.android.widgets.ExVideoWidget;
import org.samarthya.collect.android.widgets.GeoPointMapWidget;
import org.samarthya.collect.android.widgets.GeoPointWidget;
import org.samarthya.collect.android.widgets.GeoShapeWidget;
import org.samarthya.collect.android.widgets.GeoTraceWidget;
import org.samarthya.collect.android.widgets.ImageWidget;
import org.samarthya.collect.android.widgets.IntegerWidget;
import org.samarthya.collect.android.widgets.OSMWidget;
import org.samarthya.collect.android.widgets.QuestionWidget;
import org.samarthya.collect.android.widgets.RangeDecimalWidget;
import org.samarthya.collect.android.widgets.RangeIntegerWidget;
import org.samarthya.collect.android.widgets.RangePickerDecimalWidget;
import org.samarthya.collect.android.widgets.RangePickerIntegerWidget;
import org.samarthya.collect.android.widgets.RatingWidget;
import org.samarthya.collect.android.widgets.SignatureWidget;
import org.samarthya.collect.android.widgets.StringNumberWidget;
import org.samarthya.collect.android.widgets.StringWidget;
import org.samarthya.collect.android.widgets.TimeWidget;
import org.samarthya.collect.android.widgets.TriggerWidget;
import org.samarthya.collect.android.widgets.UrlWidget;
import org.samarthya.collect.android.widgets.VideoWidget;
import org.samarthya.collect.android.widgets.items.LabelWidget;
import org.samarthya.collect.android.widgets.items.LikertWidget;
import org.samarthya.collect.android.widgets.items.ListMultiWidget;
import org.samarthya.collect.android.widgets.items.ListWidget;
import org.samarthya.collect.android.widgets.items.RankingWidget;
import org.samarthya.collect.android.widgets.items.SelectMultiImageMapWidget;
import org.samarthya.collect.android.widgets.items.SelectMultiMinimalWidget;
import org.samarthya.collect.android.widgets.items.SelectMultiWidget;
import org.samarthya.collect.android.widgets.items.SelectOneImageMapWidget;
import org.samarthya.collect.android.widgets.items.SelectOneMinimalWidget;
import org.samarthya.collect.android.widgets.items.SelectOneWidget;
import org.samarthya.collect.android.widgets.utilities.ActivityGeoDataRequester;
import org.samarthya.collect.android.widgets.utilities.AudioPlayer;
import org.samarthya.collect.android.widgets.utilities.AudioRecorderRecordingStatusHandler;
import org.samarthya.collect.android.widgets.utilities.DateTimeWidgetUtils;
import org.samarthya.collect.android.widgets.utilities.GetContentAudioFileRequester;
import org.samarthya.collect.android.widgets.utilities.RecordingRequester;
import org.samarthya.collect.android.widgets.utilities.RecordingRequesterProvider;
import org.samarthya.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.samarthya.collect.audiorecorder.recording.AudioRecorder;

import static org.samarthya.collect.android.utilities.Appearances.MAPS;
import static org.samarthya.collect.android.utilities.Appearances.PLACEMENT_MAP;
import static org.samarthya.collect.android.utilities.Appearances.hasAppearance;

/**
 * Convenience class that handles creation of widgets.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class WidgetFactory {

    private static final String PICKER_APPEARANCE = "picker";

    private final Activity context;
    private final boolean readOnlyOverride;
    private final boolean useExternalRecorder;
    private final WaitingForDataRegistry waitingForDataRegistry;
    private final QuestionMediaManager questionMediaManager;
    private final AudioPlayer audioPlayer;
    private final ActivityAvailability activityAvailability;
    private final RecordingRequesterProvider recordingRequesterProvider;
    private final FormEntryViewModel formEntryViewModel;
    private final AudioRecorder audioRecorder;
    private final LifecycleOwner viewLifecycle;

    public WidgetFactory(Activity activity,
                         boolean readOnlyOverride,
                         boolean useExternalRecorder,
                         WaitingForDataRegistry waitingForDataRegistry,
                         QuestionMediaManager questionMediaManager,
                         AudioPlayer audioPlayer,
                         ActivityAvailability activityAvailability,
                         RecordingRequesterProvider recordingRequesterProvider,
                         FormEntryViewModel formEntryViewModel,
                         AudioRecorder audioRecorder,
                         LifecycleOwner viewLifecycle) {
        this.context = activity;
        this.readOnlyOverride = readOnlyOverride;
        this.useExternalRecorder = useExternalRecorder;
        this.waitingForDataRegistry = waitingForDataRegistry;
        this.questionMediaManager = questionMediaManager;
        this.audioPlayer = audioPlayer;
        this.activityAvailability = activityAvailability;
        this.recordingRequesterProvider = recordingRequesterProvider;
        this.formEntryViewModel = formEntryViewModel;
        this.audioRecorder = audioRecorder;
        this.viewLifecycle = viewLifecycle;
    }

    public QuestionWidget createWidgetFromPrompt(FormEntryPrompt prompt, PermissionsProvider permissionsProvider) {
        String appearance = Appearances.getSanitizedAppearanceHint(prompt);
        QuestionDetails questionDetails = new QuestionDetails(prompt, Collect.getCurrentFormIdentifierHash(), readOnlyOverride);

        final QuestionWidget questionWidget;
        switch (prompt.getControlType()) {
            case Constants.CONTROL_INPUT:
                switch (prompt.getDataType()) {
                    case Constants.DATATYPE_DATE_TIME:
                        questionWidget = new DateTimeWidget(context, questionDetails, new DateTimeWidgetUtils());
                        break;
                    case Constants.DATATYPE_DATE:
                        questionWidget = new DateWidget(context, questionDetails, new DateTimeWidgetUtils());
                        break;
                    case Constants.DATATYPE_TIME:
                        questionWidget = new TimeWidget(context, questionDetails, new DateTimeWidgetUtils());
                        break;
                    case Constants.DATATYPE_DECIMAL:
                        if (appearance.startsWith(Appearances.EX)) {
                            questionWidget = new ExDecimalWidget(context, questionDetails, waitingForDataRegistry);
                        } else if (appearance.equals(Appearances.BEARING)) {
                            questionWidget = new BearingWidget(context, questionDetails, waitingForDataRegistry,
                                    (SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
                        } else {
                            questionWidget = new DecimalWidget(context, questionDetails);
                        }
                        break;
                    case Constants.DATATYPE_INTEGER:
                        if (appearance.startsWith(Appearances.EX)) {
                            questionWidget = new ExIntegerWidget(context, questionDetails, waitingForDataRegistry);
                        } else {
                            questionWidget = new IntegerWidget(context, questionDetails);
                        }
                        break;
                    case Constants.DATATYPE_GEOPOINT:
                        if (hasAppearance(questionDetails.getPrompt(), PLACEMENT_MAP) || hasAppearance(questionDetails.getPrompt(), MAPS)) {
                            questionWidget = new GeoPointMapWidget(context, questionDetails, waitingForDataRegistry,
                                    new ActivityGeoDataRequester(permissionsProvider));
                        } else {
                            questionWidget = new GeoPointWidget(context, questionDetails, waitingForDataRegistry,
                                    new ActivityGeoDataRequester(permissionsProvider));
                        }
                        break;
                    case Constants.DATATYPE_GEOSHAPE:
                        questionWidget = new GeoShapeWidget(context, questionDetails, waitingForDataRegistry,
                                new ActivityGeoDataRequester(permissionsProvider));
                        break;
                    case Constants.DATATYPE_GEOTRACE:
                        questionWidget = new GeoTraceWidget(context, questionDetails, waitingForDataRegistry,
                                MapProvider.getConfigurator(), new ActivityGeoDataRequester(permissionsProvider));
                        break;
                    case Constants.DATATYPE_BARCODE:
                        questionWidget = new BarcodeWidget(context, questionDetails, waitingForDataRegistry, new CameraUtils());
                        break;
                    case Constants.DATATYPE_TEXT:
                        String query = prompt.getQuestion().getAdditionalAttribute(null, "query");
                        if (query != null) {
                            questionWidget = getSelectOneWidget(appearance, questionDetails);
                        } else if (appearance.startsWith(Appearances.PRINTER)) {
                            questionWidget = new ExPrinterWidget(context, questionDetails, waitingForDataRegistry);
                        } else if (appearance.startsWith(Appearances.EX)) {
                            questionWidget = new ExStringWidget(context, questionDetails, waitingForDataRegistry);
                        } else if (appearance.contains(Appearances.NUMBERS)) {
                            questionWidget = new StringNumberWidget(context, questionDetails);
                        } else if (appearance.equals(Appearances.URL)) {
                            questionWidget = new UrlWidget(context, questionDetails, new ExternalWebPageHelper());
                        } else {
                            questionWidget = new org.samarthya.collect.android.widgets.StringWidget(context, questionDetails);
                        }
                        break;
                    default:
                        questionWidget = new org.samarthya.collect.android.widgets.StringWidget(context, questionDetails);
                        break;
                }
                break;
            case Constants.CONTROL_FILE_CAPTURE:
                if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExArbitraryFileWidget(context, questionDetails, new MediaUtils(), questionMediaManager, waitingForDataRegistry, new ExternalAppIntentProvider(), activityAvailability);
                } else {
                    questionWidget = new ArbitraryFileWidget(context, questionDetails, new MediaUtils(), questionMediaManager, waitingForDataRegistry);
                }
                break;
            case Constants.CONTROL_IMAGE_CHOOSE:
                if (appearance.equals(Appearances.SIGNATURE)) {
                    questionWidget = new SignatureWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath());
                } else if (appearance.contains(Appearances.ANNOTATE)) {
                    questionWidget = new AnnotateWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath());
                } else if (appearance.equals(Appearances.DRAW)) {
                    questionWidget = new DrawWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath());
                } else if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExImageWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry, new MediaUtils(), new ExternalAppIntentProvider(), activityAvailability);
                } else {
                    questionWidget = new ImageWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry, new StoragePathProvider().getTmpImageFilePath());
                }
                break;
            case Constants.CONTROL_OSM_CAPTURE:
                questionWidget = new OSMWidget(context, questionDetails, waitingForDataRegistry,
                        new ActivityAvailability(context), Collect.getInstance().getFormController());
                break;
            case Constants.CONTROL_AUDIO_CAPTURE:
                RecordingRequester recordingRequester = recordingRequesterProvider.create(prompt, useExternalRecorder);
                GetContentAudioFileRequester audioFileRequester = new GetContentAudioFileRequester(context, activityAvailability, waitingForDataRegistry, formEntryViewModel);

                if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExAudioWidget(context, questionDetails, questionMediaManager, audioPlayer, waitingForDataRegistry, new MediaUtils(), new ExternalAppIntentProvider(), activityAvailability);
                } else {
                    questionWidget = new AudioWidget(context, questionDetails, questionMediaManager, audioPlayer, recordingRequester, audioFileRequester, new AudioRecorderRecordingStatusHandler(audioRecorder, formEntryViewModel, viewLifecycle));
                }
                break;
            case Constants.CONTROL_VIDEO_CAPTURE:
                if (appearance.startsWith(Appearances.EX)) {
                    questionWidget = new ExVideoWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry, new MediaUtils(), new ExternalAppIntentProvider(), activityAvailability);
                } else {
                    questionWidget = new VideoWidget(context, questionDetails, questionMediaManager, waitingForDataRegistry);
                }
                break;
            case Constants.CONTROL_SELECT_ONE:
                questionWidget = getSelectOneWidget(appearance, questionDetails);
                break;
            case Constants.CONTROL_SELECT_MULTI:
                // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
                // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
                if (appearance.contains(Appearances.MINIMAL)) {
                    questionWidget = new SelectMultiMinimalWidget(context, questionDetails, waitingForDataRegistry);
                } else if (appearance.contains(Appearances.LIST_NO_LABEL)) {
                    questionWidget = new ListMultiWidget(context, questionDetails, false);
                } else if (appearance.contains(Appearances.LIST)) {
                    questionWidget = new ListMultiWidget(context, questionDetails, true);
                } else if (appearance.contains(Appearances.LABEL)) {
                    questionWidget = new LabelWidget(context, questionDetails);
                } else if (appearance.contains(Appearances.IMAGE_MAP)) {
                    questionWidget = new SelectMultiImageMapWidget(context, questionDetails);
                } else {
                    questionWidget = new SelectMultiWidget(context, questionDetails);
                }
                break;
            case Constants.CONTROL_RANK:
                questionWidget = new RankingWidget(context, questionDetails);
                break;
            case Constants.CONTROL_TRIGGER:
                questionWidget = new TriggerWidget(context, questionDetails);
                break;
            case Constants.CONTROL_RANGE:
                if (appearance.startsWith(Appearances.RATING)) {
                    questionWidget = new RatingWidget(context, questionDetails);
                } else {
                    switch (prompt.getDataType()) {
                        case Constants.DATATYPE_INTEGER:
                            if (prompt.getQuestion().getAppearanceAttr() != null && prompt.getQuestion().getAppearanceAttr().contains(PICKER_APPEARANCE)) {
                                questionWidget = new RangePickerIntegerWidget(context, questionDetails);
                            } else {
                                questionWidget = new RangeIntegerWidget(context, questionDetails);
                            }
                            break;
                        case Constants.DATATYPE_DECIMAL:
                            if (prompt.getQuestion().getAppearanceAttr() != null && prompt.getQuestion().getAppearanceAttr().contains(PICKER_APPEARANCE)) {
                                questionWidget = new RangePickerDecimalWidget(context, questionDetails);
                            } else {
                                questionWidget = new RangeDecimalWidget(context, questionDetails);
                            }
                            break;
                        default:
                            questionWidget = new org.samarthya.collect.android.widgets.StringWidget(context, questionDetails);
                            break;
                    }
                }
                break;
            default:
                questionWidget = new StringWidget(context, questionDetails);
                break;
        }

        return questionWidget;
    }

    private QuestionWidget getSelectOneWidget(String appearance, QuestionDetails questionDetails) {
        final QuestionWidget questionWidget;
        boolean isQuick = appearance.contains(Appearances.QUICK);
        // search() appearance/function (not part of XForms spec) added by SurveyCTO gets
        // considered in each widget by calls to ExternalDataUtil.getSearchXPathExpression.
        if (appearance.contains(Appearances.MINIMAL)) {
            questionWidget = new SelectOneMinimalWidget(context, questionDetails, isQuick, waitingForDataRegistry);
        } else if (appearance.contains(Appearances.LIKERT)) {
            questionWidget = new LikertWidget(context, questionDetails);
        } else if (appearance.contains(Appearances.LIST_NO_LABEL)) {
            questionWidget = new ListWidget(context, questionDetails, false, isQuick);
        } else if (appearance.contains(Appearances.LIST)) {
            questionWidget = new ListWidget(context, questionDetails, true, isQuick);
        } else if (appearance.contains(Appearances.LABEL)) {
            questionWidget = new LabelWidget(context, questionDetails);
        } else if (appearance.contains(Appearances.IMAGE_MAP)) {
            questionWidget = new SelectOneImageMapWidget(context, questionDetails, isQuick);
        } else {
            questionWidget = new SelectOneWidget(context, questionDetails, isQuick);
        }
        return questionWidget;
    }

}
