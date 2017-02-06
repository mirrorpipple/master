package dllearning.logistic;

import static dllearning.util.ActivationFunction.softmax;

public class LogisticRegression {

    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;


    public LogisticRegression(int n_in, int n_out) {
        //引数をフィールド変数へ反映
        this.n_in = n_in;
        this.n_out = n_out;

        W = new double[n_out][n_in]; //重みWは入力と出力の組み合わせの数だけ存在
        b = new double[n_out]; //バイアスbは出力の数だけ存在
    }

    public double[][] train(double[][] x, int t[][], int minibatch_size, double learning_rate) {

        double[][] grad_W = new double[n_out][n_in]; //Wの勾配
        double[] grad_b = new double[n_out]; //bの勾配

        double[][] dy = new double[minibatch_size][n_out]; //softmax関数で見たときの、yの勾配

        // 1. 重みWとバイアスbの勾配を計算  ミニバッチの数で平均したものを反映させます
        for (int n = 0; n < minibatch_size; n++) {

            //入力xに対してsoftmax関数を通した値を取得
            double[] predicted_y_ = output(x[n]);

            for (int j = 0; j < n_out; j++) {
                dy[n][j] = predicted_y_[j] - t[n][j]; //tは既知の正答(正解データベクトル)  クラスjに属していれば1、属していなければ0を取ります

                for (int i = 0; i < n_in; i++) {
                    grad_W[j][i] += dy[n][j] * x[n][i]; //重みの勾配を計算
                }
                grad_b[j] += dy[n][j]; //バイアスの勾配を計算
            }
        }

        // 2. 計算した勾配をもとに、パラメータを更新  ミニバッチの数で反映を平均にならします
        for (int j = 0; j < n_out; j++) {
            for (int i = 0; i < n_in; i++) {
                W[j][i] -= learning_rate * grad_W[j][i] / minibatch_size;
            }
            b[j] -= learning_rate * grad_b[j] / minibatch_size;
        }

        return dy; //隠れ層の逆伝播等で使用したいため、dyを返却します
    }

    /*
     * 各入力をもとに、softmax関数を通した値を返却します
     */
    public double[] output(double[] x) {

        double[] pre_activation = new double[n_out];

        for (int j = 0; j < n_out; j++) {
            for (int i = 0; i < n_in; i++) {
                pre_activation[j] += W[j][i] * x[i]; //入力シグナルと出力シグナルの組み合わせで、重みづけを計算
            }
            pre_activation[j] += b[j];  //出力シグナルに対して、バイアスを加算  事前計算の完成
        }

        return softmax(pre_activation, n_out); //softmax関数を通して値を返却
    }

    public Integer[] predict(double[] x) {

        double[] y = output(x);  // 渡された値に対して、softmax関数をかけます
        Integer[] t = new Integer[n_out]; // 出力は確率のため、ラベルに変換

        int argmax = -1;
        double max = 0.;

        for (int i = 0; i < n_out; i++) {
            if (max < y[i]) {
                max = y[i];
                argmax = i;
            }
        }

        //確率最大のラベルに対して、フラグを立てる
        for (int i = 0; i < n_out; i++) {
            if (i == argmax) {
                t[i] = 1;
            } else {
                t[i] = 0;
            }
        }

        return t;
    }

}
