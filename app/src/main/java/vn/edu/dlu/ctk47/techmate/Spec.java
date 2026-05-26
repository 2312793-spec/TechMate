package vn.edu.dlu.ctk47.techmate;

public class Spec {
    private String key;
    private String value1;
    private String value2;

    public Spec(String key, String value1, String value2) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getKey() {
        return key;
    }

    public String getValue1() {
        return value1;
    }

    public String getValue2() {
        return value2;
    }
}
