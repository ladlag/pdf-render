package com.mercury.report.model;

/**
 * Represents a grouped table block (e.g., 1.1, 1.2, 1.3, 1.4)
 */
public class TableBlock {
    private String blockId;
    private String blockTitle;
    private TableData tableData;

    public TableBlock() {
    }

    public TableBlock(String blockId, String blockTitle, TableData tableData) {
        this.blockId = blockId;
        this.blockTitle = blockTitle;
        this.tableData = tableData;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getBlockTitle() {
        return blockTitle;
    }

    public void setBlockTitle(String blockTitle) {
        this.blockTitle = blockTitle;
    }

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }
}
