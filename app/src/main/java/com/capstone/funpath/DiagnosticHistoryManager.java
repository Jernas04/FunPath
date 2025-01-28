package com.capstone.funpath;

import com.capstone.funpath.Adapters.DiagnosticsRecord;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticHistoryManager {
    private static DiagnosticHistoryManager instance;
    private List<DiagnosticsRecord> diagnosticHistory;

    private DiagnosticHistoryManager() {
        diagnosticHistory = new ArrayList<>();
    }

    public static DiagnosticHistoryManager getInstance() {
        if (instance == null) {
            instance = new DiagnosticHistoryManager();
        }
        return instance;
    }

    public List<DiagnosticsRecord> getDiagnosticHistory() {
        return diagnosticHistory;
    }

    public void addDiagnosticRecord(DiagnosticsRecord entry) {
        diagnosticHistory.add(entry);
    }

    public void clearHistory() {
        diagnosticHistory.clear();
    }
}
