package pl.tmkd.serverz;

public class MyData {
    private String id;
    private String greet;

    public MyData(String id, String greet) {
        this.id = id;
        this.greet = greet;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getGreet() {

        return greet;
    }

    public void setGreet(String greet) {
        this.greet = greet;
    }
}
