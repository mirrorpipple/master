package dllearning.util;

public final class ActivationFunction {

    public static double stepByReLU(double x) {
        if(x > 0) {
            return x;
        } else {
            return 0.;
        }
    }

    public static double differentiateByReLU(double y) {
        if(y > 0) {
            return 1.;
        } else {
            return 0.;
        }
    }

    /*
     * TODOF‚È‚ºÅ‘å’l‚ÅŒ¸Z‚·‚é‚Ì‚©‚¢‚Ü‚¢‚¿•s–¾
     */
    public static double[] softmax(double[] x, int n) {

        double[] y = new double[n];
        double max = 0.;
        double sum = 0.;

        for (int i = 0; i < n; i++) {
            if (max < x[i]) {
                max = x[i];  // to prevent overflow
            }
        }

        for (int i = 0; i < n; i++) {
            y[i] = Math.exp( x[i] - max );
            sum += y[i];
        }

        for (int i = 0; i < n; i++) {
            y[i] /= sum;
        }

        return y;
    }


}
