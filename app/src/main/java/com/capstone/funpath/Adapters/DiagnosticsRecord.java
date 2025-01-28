package com.capstone.funpath.Adapters;

public class DiagnosticsRecord {
    private String week;
    private String date;
    private String notes;

    // Default constructor required for calls to DataSnapshot.getValue(DiagnosticsRecord.class)
    public DiagnosticsRecord() {}

    public DiagnosticsRecord(String week, String date, String notes) {
        this.week = week;
        this.date = date;
        this.notes = notes;
    }

    public String getWeek() {
        return week;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return "DiagnosticsRecord{" +
                "week='" + week + '\'' +
                ", date='" + date + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}





