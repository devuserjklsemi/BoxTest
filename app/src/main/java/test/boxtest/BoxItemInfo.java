package test.boxtest;

public class BoxItemInfo implements Comparable<BoxItemInfo> {
    private String id;
    private java.util.Date updateDate;

    public BoxItemInfo(String id, java.util.Date updateDate) {
        this.id = id;
        this.updateDate = updateDate;
    }

    public String getId() {
        return id;
    }

    public java.util.Date getUpdateDate() {
        return updateDate;
    }

    @Override
    public int compareTo(BoxItemInfo comparestu) {
        return -1 * this.updateDate.compareTo(comparestu.getUpdateDate());
    }

}