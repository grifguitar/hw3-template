package me.index.segment;

public enum Windows {
    CONVEX(CHT.INSTANCE), LINEAR(RSpline.INSTANCE);

    Windows(Window w) {
        this.INSTANCE = w;
    }

    public final Window INSTANCE;
}
