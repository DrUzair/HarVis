package yt.vis;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

public final class VertexStrokeHighlight<V,E> implements 
Transformer<V,Stroke> 
{ 
    protected boolean highlight = false; 
    protected Stroke heavy = new BasicStroke(5); 
    protected Stroke medium = new BasicStroke(3); 
    protected Stroke light = new BasicStroke(1); 
    protected PickedInfo<V> pi; 
    protected Graph<V,E> graph; 
      
    public VertexStrokeHighlight(Graph<V,E> graph, PickedInfo<V> pi) 
    { 
        this.graph = graph; 
        this.pi = pi; 
    } 
      
    public void setHighlight(boolean highlight) 
    { 
        this.highlight = highlight; 
    } 
      
    public Stroke transform(V v) 
    { 
        if (highlight) 
        { 
            if (pi.isPicked(v)) 
                return heavy; 
            else 
            { 
                for(V w : graph.getNeighbors(v)) { 
                    if (pi.isPicked(w)) 
                        return medium; 
                } 
                return light; 
            } 
        } 
        else 
            return light;  
    } 

}