package com.mercury.pdf.render.model;

import java.util.List;

/**
 * Represents table data with headers and rows
 */
public class TableData {
    private List<String> headers;
    private List<List<String>> rows;

    public TableData() {
    }

    public TableData(List<String> headers, List<List<String>> rows) {
        this.headers = headers;
        this.rows = rows;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }
}
