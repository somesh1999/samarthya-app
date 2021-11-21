package org.samarthya.collect.android.instancemanagement;

import org.samarthya.collect.forms.Form;
import org.samarthya.collect.forms.FormsRepository;
import org.samarthya.collect.forms.instances.Instance;
import org.samarthya.collect.forms.instances.InstancesRepository;

import java.util.List;

public class InstanceDeleter {

    private final InstancesRepository instancesRepository;
    private final FormsRepository formsRepository;

    public InstanceDeleter(InstancesRepository instancesRepository, FormsRepository formsRepository) {
        this.instancesRepository = instancesRepository;
        this.formsRepository = formsRepository;
    }

    public void delete(Long id) {
        Instance instance = instancesRepository.get(id);
        if (instance != null) {
            if (instance.getStatus().equals(Instance.STATUS_SUBMITTED)) {
                instancesRepository.deleteWithLogging(id);
            } else {
                instancesRepository.delete(id);
            }


            Form form = formsRepository.getLatestByFormIdAndVersion(instance.getFormId(), instance.getFormVersion());
            if (form != null && form.isDeleted()) {
                List<Instance> otherInstances = instancesRepository.getAllNotDeletedByFormIdAndVersion(form.getFormId(), form.getVersion());
                if (otherInstances.isEmpty()) {
                    formsRepository.delete(form.getDbId());
                }
            }
        }
    }
}
