package yt.vis;
import java.awt.Color;
import java.awt.Paint;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.picking.PickedInfo;

public class VertexPainterFunction<V> implements Transformer<V, Paint>
{
    /**
     * A collection of nodes that were picked
     */
    protected PickedInfo<V> pickedInfo;
 public VertexPainterFunction(PickedInfo<V> pickedInfo_)
    {
        pickedInfo = pickedInfo_;
}
public Paint transform(V node)
    {
        float alpha = 1.0f;
if (pickedInfo.isPicked(node)) //for picked nodes make them blue.
        {
            return new Color(0, 0, 1f, alpha);
        }

 return (new Color(0.25f,0.75f,0.75f,alpha)); //for other nodes make them this color (cyan-ish).
}
}