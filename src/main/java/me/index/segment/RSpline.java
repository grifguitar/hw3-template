package me.index.segment;

public enum RSpline implements Window {
    INSTANCE;

    private double key0;
    private long eps;
    private boolean done;

    private double x;
    private double y;
    private double ux;
    private double uy;
    private double lx;
    private double ly;

    @Override
    public int init_skip(long key, long err) {
        this.key0 = key;
        this.eps = err;
        this.done = false;
        return 1;
    }

    @Override
    public boolean can_expand(long key, long pos, int err) {
        double k = (double) key;
        double p = (double) pos;
        if (!done) {
            done = true;
            x = (k - key0);
            y = p;
            ux = x;
            uy = y + eps;
            lx = x;
            ly = y - eps;
            return true;
        }
        if (cross((k - key0), p, ux, uy) > 0 &&
                cross((k - key0), p, lx, ly) < 0) {
            x = (k - key0);
            y = p;
            if (cross(ux, uy, x, y + eps) < 0) {
                ux = x;
                uy = y + eps;
            }
            if (cross(lx, ly, x, y - eps) > 0) {
                lx = x;
                ly = y - eps;
            }
            return true;
        }
        return false;
    }

    @Override
    public double[] get_result() {
        double[] cf = new double[2];
        if (done) {
            cf[0] = (y) / (x);
            cf[1] = -cf[0] * key0;
        }
        return cf;
    }

    private static double cross(double ax, double ay, double bx, double by) {
        return ax * by - bx * ay;
    }
}
