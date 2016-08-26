package yt.vis;

import org.apache.commons.collections15.Factory;

public class MyEdgeFactory implements Factory {

    private static int defaultFlow = 10;
    private static int defaultCapacity = 100;
    private int edgeCount;

    private MyEdgeFactory() {            

    }

    public MyEdge create() {
        String name = "E" + edgeCount;
        MyEdge e = new MyEdge(name, edgeCount);
        edgeCount++;
        e.setFlow(defaultFlow);
        e.setCapacity(defaultCapacity);
        return e;
    }    

}