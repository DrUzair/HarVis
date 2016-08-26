package yt.vis;

public class MyEdge {

    private int flow;
    private int capacity;

    private String name;
    private int eIndex;

    public MyEdge(String name, int eIndex) {
        this.name = name;
        this.eIndex = eIndex;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int edgeCapacity) {
        this.capacity = edgeCapacity;
    }

    public int getFlow() {
        return this.flow;
    }

    public void setFlow(int edgeFlow) {
        this.flow = edgeFlow;
    }

    public String toString() {
        return this.name;
    }

}