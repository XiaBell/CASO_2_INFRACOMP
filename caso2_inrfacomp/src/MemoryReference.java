public class MemoryReference {
    private final String matrixCell;
    private final int pageNumber;
    private final int offset;
    private final String action;

    public MemoryReference(String matrixCell, int pageNumber, int offset, String action) {
        this.matrixCell = matrixCell;
        this.pageNumber = pageNumber;
        this.offset = offset;
        this.action = action;
    }

    public String getMatrixCell() {
        return matrixCell;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getOffset() {
        return offset;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%d,%s", matrixCell, pageNumber, offset, action);
    }
} 