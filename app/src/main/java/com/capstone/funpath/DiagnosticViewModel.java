package com.capstone.funpath;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.capstone.funpath.Adapters.DiagnosticsRecord;

import java.util.List;

public class DiagnosticViewModel extends ViewModel {
    private final MutableLiveData<List<DiagnosticsRecord>> diagnosticHistory = new MutableLiveData<>();

    public void setDiagnosticHistory(List<DiagnosticsRecord> entry) {
        if (diagnosticHistory != null) {
            diagnosticHistory.setValue(entry);
        }
    }

    public LiveData<List<DiagnosticsRecord>> getDiagnosticHistory() {
        return diagnosticHistory;
    }

    public void addDiagnosticRecord(DiagnosticsRecord record) {
        List<DiagnosticsRecord> currentList = diagnosticHistory.getValue();
        if (currentList != null) {
            currentList.add(record);
            diagnosticHistory.setValue(currentList); // Notify observers
        }
    }
}
