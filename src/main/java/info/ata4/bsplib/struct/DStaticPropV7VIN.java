package info.ata4.bsplib.struct;

import info.ata4.bsplib.vector.Vector3f;

public class DStaticPropV7VIN extends DStaticPropV6 implements DStaticPropVinScaling {

    public Vector3f scaling = new Vector3f(1, 1, 1);

    @Override
    public Vector3f getScaling() {
        return scaling;
    }

    @Override
    public void setScaling(Vector3f scaling) {
        this.scaling = scaling;
    }
}
