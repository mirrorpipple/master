package dllearning.convolution;

import java.util.Random;
import static dllearning.util.RandomGenerator.*;
import static dllearning.util.ActivationFunction.*;

/*
 * 畳み込み層とプーリング層を通過させるためのクラス
 * 活性化関数には、ReLUを使用
 */
public class ConvolutionPoolingLayer {

    public int[] image_size;
    public int channel;
    public int n_kernel;
    public int[] kernel_size;
    public int[] pool_size;
    public int[] convolved_size;
    public int[] pooled_size;
    public double[][][][] W;
    public double[] b;
    public Random rng;

    //コンストラクタ
    public ConvolutionPoolingLayer(int[] image_size, int channel, int n_kernel, int[] kernel_size, int[] pool_size, int[] convolved_size, int[] pooled_size, Random rng) {

        if (rng == null) rng = new Random(1234);

        //重みの初期値を生成します。
        if (W == null) {

            // W = {カーネル数, チャンネル, カーネルの大きさ（i）, カーネルの大きさ（j）}
            W = new double[n_kernel][channel][kernel_size[0]][kernel_size[1]];

            double in_ = channel * kernel_size[0] * kernel_size[1]; //プーリング実行前の行列の成分数
            double out_ = n_kernel * kernel_size[0] * kernel_size[1] / (pool_size[0] * pool_size[1]);
            double w_ = Math.sqrt(6. / (in_ + out_));

            for (int kernel_i = 0; kernel_i < n_kernel; kernel_i++) {
                for (int channel_i = 0; channel_i < channel; channel_i++) {
                    for (int k_size_i = 0; k_size_i < kernel_size[0]; k_size_i++) {
                        for (int k_size_j = 0; k_size_j < kernel_size[1]; k_size_j++) {
                            W[kernel_i][channel_i][k_size_i][k_size_j] = uniform(-w_, w_, rng);
                        }
                    }
                }
            }
        }

        //バイアスを初期化する
        if (b == null) {
            b = new double[n_kernel];
        }

        //フィールド変数を更新する
        this.image_size = image_size;
        this.channel = channel;
        this.n_kernel = n_kernel;
        this.kernel_size = kernel_size;
        this.pool_size = pool_size;
        this.convolved_size = convolved_size;
        this.pooled_size = pooled_size;
        this.rng = rng;
    }

    /*
     * 順伝播させる
     * 与えられた配列に対して、畳み込みと活性化、プーリングを与える
     */
    public double[][][] forward(double[][][] x, double[][][] pre_activated_x, double[][][] activated_x) {

        double[][][] z = this.convolve(x, pre_activated_x, activated_x);
        return  this.downsample(z);

    }

    /*
     * 逆伝播させる
     */

    public double[][][][] backward(double[][][][] x, double[][][][] pre_activated_x, double[][][][] activated_x, double[][][][] downsampled_x, double[][][][] dy, int minibatch_size, double learning_rate) {

        double[][][][] dz = this.upsample(activated_x, downsampled_x, dy, minibatch_size);
        return this.deconvolve(x, pre_activated_x, dz, minibatch_size, learning_rate);

    }

    /*
     * 渡されたxに対して畳み込みをかけ、それをReLUで活性化した配列を返却する。
     */
    public double[][][] convolve(double[][][] x, double[][][] pre_activated_x, double[][][] activated_x) {

        //yは特徴マップを活性化した後の配列
        double[][][] y = new double[n_kernel][convolved_size[0]][convolved_size[1]]; //y[この畳み込みで使用するカーネル数][特徴マップの行の数][特徴マップの列の数]

        for (int kernel_i = 0; kernel_i < n_kernel; kernel_i++) {
            for (int i = 0; i < convolved_size[0]; i++) {
                for(int j = 0; j < convolved_size[1]; j++) {

                    double convolved_ = 0.;

                    //畳み込みを行い、カーネルで圧縮された値（convolved_）を生成
                    for (int channel_i = 0; channel_i < channel; channel_i++) {
                        for (int s = 0; s < kernel_size[0]; s++) {
                            for (int t = 0; t < kernel_size[1]; t++) {
                                convolved_ += W[kernel_i][channel_i][s][t] * x[channel_i][i+s][j+t];
                            }
                        }
                    }

                    //pre_activated（活性化前）の値（特徴マップの成分 + バイアス）をキャッシュする
                    pre_activated_x[kernel_i][i][j] = convolved_ + b[kernel_i];
                    //特徴マップをReLUで活性化する
                    activated_x[kernel_i][i][j] = stepByReLU(pre_activated_x[kernel_i][i][j]);
                    //返り値へ代入
                    y[kernel_i][i][j] = activated_x[kernel_i][i][j];
                }
            }
        }

        return y;
    }
    /*
     * xに対してプーリングをかけた配列を返却する。
     */
    public double[][][] downsample(double[][][] x) {

        double[][][] y = new double[n_kernel][pooled_size[0]][pooled_size[1]];

        for (int kernel_i = 0; kernel_i < n_kernel; kernel_i++) {
            for (int i = 0; i < pooled_size[0]; i++) {
                for (int j = 0; j < pooled_size[1]; j++) {

                    //サブデータ内の最大値を生成
                    double max_ = 0.;

                    //プーリングの大きさだけループ（サブデータのすべての値に対して、どれが一番大きいかをチェック）
                    for (int s = 0; s < pool_size[0]; s++) {
                        for (int t = 0; t < pool_size[1]; t++) {

                            //max_の初期値は一番左上の値
                            if (s == 0 && t == 0) {
                                max_ = x[kernel_i][pool_size[0]*i][pool_size[1]*j];
                                continue;
                            }
                            //より大きい値が見つかったら、max_を更新する
                            if (max_ < x[kernel_i][pool_size[0]*i+s][pool_size[1]*j+t]) {
                                max_ = x[kernel_i][pool_size[0]*i+s][pool_size[1]*j+t];
                            }
                        }
                    }

                    //ダウンサンプリングされた配列へ、値を代入していく
                    y[kernel_i][i][j] = max_;
                }
            }
        }

        return y;
    }

    /*
     * プーリングで選択された成分に対して誤差を伝播させるため、誤差の行列を返却します
     * 選択されなかった成分に対しては誤差0を返却します
     */
    public double[][][][] upsample(double[][][][] x, double[][][][] y, double[][][][] dy, int minibatch_size) {

        double[][][][] dX = new double[minibatch_size][n_kernel][convolved_size[0]][convolved_size[1]];

        for (int n = 0; n < minibatch_size; n++) {

            for (int k = 0; k < n_kernel; k++) {
                for (int i = 0; i < pooled_size[0]; i++) {
                    for (int j = 0; j < pooled_size[1]; j++) {

                        for (int s = 0; s < pool_size[0]; s++) {
                            for (int t = 0; t < pool_size[1]; t++) {

                                double d_ = 0.;

                                //ダウンサンプルで選ばれた成分に対して、δを伝播させる  他の成分には誤差0を渡す
                                if (y[n][k][i][j] == x[n][k][pool_size[0]*i+s][pool_size[1]*j+t]) {
                                    d_ = dy[n][k][i][j];
                                }

                                dX[n][k][pool_size[0]*i+s][pool_size[1]*j+t] = d_;
                            }
                        }
                    }
                }
            }
        }

        return dX;
    }

    /*
     * y = pre_activated_x, dy = dz で取得します
     * pre_activated_x = [minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]
     *
     */
    public double[][][][] deconvolve(double[][][][] x, double[][][][] y, double[][][][] dy, int minibatch_size, double learning_rate) {

        double[][][][] grad_W = new double[n_kernel][channel][kernel_size[0]][kernel_size[1]];
        double[] grad_b = new double[n_kernel];
        double[][][][] dx = new double[minibatch_size][channel][image_size[0]][image_size[1]];

        //W, bの勾配を計算（ミニバッチをすべて反映）
        for (int n = 0; n < minibatch_size; n++) {
            for (int k = 0; k < n_kernel; k++) {
                for (int i = 0; i < convolved_size[0]; i++) {
                    for (int j = 0; j < convolved_size[1]; j++) {


                        double d_ = dy[n][k][i][j] * differentiateByReLU(y[n][k][i][j]);

                        //bの勾配を計算
                        grad_b[k] += d_;

                        for (int c = 0; c < channel; c++) {
                            for (int s = 0; s < kernel_size[0]; s++) {
                                for (int t = 0; t < kernel_size[1]; t++) {
                                    //Wの勾配を計算
                                    grad_W[k][c][s][t] += d_ * x[n][c][i+s][j+t];
                                }
                            }
                        }
                    }
                }
            }
        }

        //勾配を更新
        for (int k = 0; k < n_kernel; k++) {
            b[k] -= learning_rate * grad_b[k] / minibatch_size; //bは活性化関数の誤差なので、カーネル計算の誤差としては関係しません

            for (int c = 0; c < channel; c++) {
                for (int s = 0; s < kernel_size[0]; s++) {
                    for(int t = 0; t < kernel_size[1]; t++) {
                        W[k][c][s][t] -= learning_rate * grad_W[k][c][s][t] / minibatch_size; //カーネルの全ての項に対して、独立した重みWを使用しています
                    }
                }
            }
        }

        // x（入力）に対するデルタを計算します
        for (int n = 0; n < minibatch_size; n++) {
            for (int c = 0; c < channel; c++) {
                for (int i = 0; i < image_size[0]; i++) {
                    for (int j = 0; j < image_size[1]; j++) {

                        for (int k = 0; k < n_kernel; k++) {
                            for (int s = 0; s < kernel_size[0]; s++) {
                                for (int t = 0; t < kernel_size[1]; t++) {

                                    double d_ = 0.;

                                    //TODO:特徴マップの左上を0パディングなのはわかるけど、どうしてkernel_sizeで減算しているのかわからない...
                                    //下のようにif文書いた方が一見、公式に忠実に見えるけれど...？
                                    //if (i - (kernel_size[0] - 1) - s >= 0 &&
                                    //    j - (kernel_size[1] - 1) - t >= 0) {

                                    if (i - s >= 0 &&
                                        j - t >= 0 &&

                                        i - s <= kernel_size[0]-1 &&
                                        j - t <= kernel_size[1]-1)
                                        {
                                        //d_ = dy[n][k][i-(kernel_size[0]-1)-s][j-(kernel_size[1]-1)-t] * differentiateByReLU(y[n][k][i-(kernel_size[0]-1)-s][j-(kernel_size[1]-1)-t]) * W[k][c][s][t];
                                        d_ = dy[n][k][i-s][j-t] * differentiateByReLU(y[n][k][i-s][j-t]) * W[k][c][s][t];
                                    }

                                    dx[n][c][i][j] += d_;
                                }
                            }
                        }
                    }
                }
            }
        }

        return dx;
    }






}
