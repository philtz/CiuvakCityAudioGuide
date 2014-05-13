package test.ciuvak.cluj.mock;

/**
 * Created by philtz on 10/05/2014.
 */
public class TestPoiMockData {

    public int id;

    public String name;

    public double latitude;

    public double longitude;

    public int priority;

    @Override
    public String toString() {
        return "TestPoiMockData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", priority=" + priority +
                '}';
    }
}
