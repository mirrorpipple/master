package dllearning.multilayer;

import java.util.Random;
import static dllearning.util.RandomGenerator.*;
import static dllearning.util.ActivationFunction.*;

/*
 * 隠れ層  活性化関数にはReLUを使用します
 */
public class HiddenLayer {

    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;
    public Random rng;

    public HiddenLayer(int n_in, int n_out, double[][] W, double[] b, Random rng) {

        if (rng == null) rng = new Random(1234);  // 乱数を生成

        //重みの初期値を生成します
        if (W == null) {
            W = new double[n_out][n_in]; //入力と出力の組み合わせで、異なる重みを使用
            double w_ = 1. / n_in;

            for(int j = 0; j < n_out; j++) {
                for(int i = 0; i < n_in; i++) {
                    W[j][i] = uniform(-w_, w_, rng);
                }
            }
        }

        //バイアスの箱を生成  バイアスは、出力に対して1つ存在します
        if (b == null) b = new double[n_out];

        //引数をフィールド変数へ移行
        this.n_in = n_in;
        this.n_out = n_out;
        this.W = W;
        this.b = b;
        this.rng = rng;
    }

    /*
     * インプットのシグナルに対して、重みづけを行い、活性化関数を通して、生成された出力層を返します。
     */
    public double[] output(double[] x) {

        double[] y = new double[n_out]; //出力用のシグナルの準備

        for (int j = 0; j < n_out; j++) {
            double pre_activation_ = 0.;

            //入力と出力の組み合わせについて、重みづけを計算
            for (int i = 0; i < n_in; i++) {
                pre_activation_ += W[j][i] * x[i];
            }
            //出力jに対するバイアスを加算する
            pre_activation_ += b[j];

            //活性化関数ReLUを通す
            y[j] = stepByReLU(pre_activation_);
        }

        return y;
    }

    /*
     * 順伝播させる
     */
    public double[] forward(double[] x) {
        return output(x);
    }

    /*
     * 逆伝播させます
     * 出力層の1つ前の隠れ層だと、dyのyは出力層を対象にしているので、dyが、y=softmax(x)から算出できます
     */
    public double[][] backward(double[][] x, double[][] z, double[][] dy, double[][] W_prev, int minibatch_size, double learning_rate) {

        double[][] dz = new double[minibatch_size][n_out];  // 逆伝播誤差

        double[][] grad_W = new double[n_out][n_in]; //バックプロパゲーションで調整済みのW
        double[] grad_b = new double[n_out]; //バックプロパゲーションで調整済みのb

        // SGDを用いて学習だ
        // まず逆伝播誤差を求めて、 Wとbの勾配を求める  ミニバッチ毎に実行し、最後に平均にならす形で足し合わせる
        for (int n = 0; n < minibatch_size; n++) {

            //各出力jに対して、逆伝播方向で向いた1つ前の層
            //（順伝播で見た1つ後の層、隠れ層が1つなら出力層）の出力kをすべてに対して計算したものを組み込んでいきます。
            for (int j = 0; j < n_out; j++) {
                for (int k = 0; k < dy[0].length; k++) {  // k < ( 前層のn_out)
                    dz[n][j] += W_prev[k][j] * dy[n][k]; //Σwδを計算
                }
                dz[n][j] *= differentiateByReLU(z[n][j]); //Σwδ にh'(a)をかける  これで出力jに対するδが計算できました

                for (int i = 0; i < n_in; i++) {
                    grad_W[j][i] += dz[n][j] * x[n][i]; //重みの勾配を計算
                }

                grad_b[j] += dz[n][j]; //バイアスの勾配を計算
            }
        }

        // パラメータW,bを更新  勾配の反映を平均化させるため、ミニバッチの数で除算した分をフィールド変数に加算（減算）します
        for (int j = 0; j < n_out; j++) { //出力シグナルの数だけループ
            for(int i = 0; i < n_in; i++) { //入力シグナルの数だけループ
                W[j][i] -= learning_rate * grad_W[j][i] / minibatch_size; //重みは入力シグナルと出力シグナルの組み合わせの数だけ存在
            }
            b[j] -= learning_rate * grad_b[j] / minibatch_size; //バイアスは出力シグナルの数だけ存在
        }

        return dz; //これが次の層（逆伝播方向で見て）の、dyになるのですね  すごいなぁ
    }



}
