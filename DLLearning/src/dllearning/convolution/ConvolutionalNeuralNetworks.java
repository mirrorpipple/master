package dllearning.convolution;

import dllearning.convolution.ConvolutionPoolingLayer;
import dllearning.multilayer.HiddenLayer;
import dllearning.util.ReadingMnist;
import dllearning.logistic.LogisticRegression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ConvolutionalNeuralNetworks {

    public int[] n_kernels; //i回目の畳み込み層で使用するカーネルの数
    public int[][] kernel_sizes; //i回目畳み込み層について、{0=i, 1=j}に対するカーネルのサイズ（ます目の数）
    public int[][] pool_sizes; //i回目プーリング層について、{0=i, 1=j}に対するプーリングのサイズ（ます目の数）
    public int n_hidden; //隠れ層のユニット数
    public int n_out; //最終的な分類の種類  0～9なのでここでは10になりますね。

    public ConvolutionPoolingLayer[] convpool_layers;
    public int[][] convolved_sizes;
    public int[][] pooled_sizes;
    public int flattened_size;
    public HiddenLayer hidden_layer;
    public LogisticRegression logistic_layer;
    public Random rng;


    /*
     * メモ：
     * カーネル数 = 10, 20
     * チャネル数 = 1
     * カーネルサイズ = 5
     * プーリングサイズ = 2
     * 最終出力数（ロジスティックの入力層の数） = 4 * 4 * 20 = 320  大きそう  回るかな
     */


    public ConvolutionalNeuralNetworks(int[] image_size, int channel, int[] n_kernels, int[][] kernel_sizes, int[][] pool_sizes, int n_hidden, int n_out, Random rng) {

        //乱数を生成
        if (rng == null) rng = new Random(1234);

        this.n_kernels = n_kernels;
        this.kernel_sizes = kernel_sizes;
        this.pool_sizes = pool_sizes;
        this.n_hidden = n_hidden;
        this.n_out = n_out;
        this.rng = rng;

        //まず箱を用意する。image_sizeは、入力として受け付ける画像データの配列です。
        //28 x 28の二次元画像なら int[] image_size = {28, 28}
        convpool_layers = new ConvolutionPoolingLayer[n_kernels.length]; //畳み込み層の数だけ、畳み込み用インスタンスを生成する
        convolved_sizes = new int[n_kernels.length][image_size.length]; //convolved_sizes[畳み込み層の数][元の画像データの次元数] image_size.lengthは、画像データの次元数を返します。
        pooled_sizes = new int[n_kernels.length][image_size.length];

        //畳み込み層の回数だけ、ConvolutionPoolingLayerインスタンスを生成し、convpool_layersへ格納する
        for (int i = 0; i < n_kernels.length; i++) {
            int[] size_;
            int channel_;

            if (i == 0) {
              //畳み込み層が0回目、つまり入力元の画像データの大きさを取得する  元の画像は2次元で決め打ちです
                size_ = new int[]{image_size[0], image_size[1]};
                channel_ = channel;
            } else {
                //1回目以降は、畳み込んでダウンサンプリングされた後の大きさを取得する
                size_ = new int[]{pooled_sizes[i-1][0], pooled_sizes[i-1][1]};
                channel_ = n_kernels[i-1]; //特徴マップの数が、次の層のチャンネル数になるのですね
            }

            convolved_sizes[i] = new int[]{size_[0] - kernel_sizes[i][0] + 1, size_[1] - kernel_sizes[i][1] + 1}; //nm行列にij行列のカーネルを当てはめると、特徴マップは(n-i+1)(m-j+1)行列になります
            pooled_sizes[i] = new int[]{convolved_sizes[i][0] / pool_sizes[i][0], convolved_sizes[i][1] / pool_sizes[i][1]}; //カーネル：nm行列 → プーリングの大きさ：st行列 → ij行列  とする場合、i=n/s, j=m/t になります

            convpool_layers[i] = new ConvolutionPoolingLayer(size_, channel_, n_kernels[i], kernel_sizes[i], pool_sizes[i], convolved_sizes[i], pooled_sizes[i], rng);
        }

        // 最終的なプーリング後の配列を1次元に変換し、多層パーセプトロンが入力層で受け取るシグナルの数にする
        flattened_size = n_kernels[n_kernels.length-1] * pooled_sizes[pooled_sizes.length-1][0] * pooled_sizes[pooled_sizes.length-1][1];

        // 隠れ層のインスタンスを生成
        hidden_layer = new HiddenLayer(flattened_size, n_hidden, null, null, rng);

        // 出力層（ロジスティック回帰）のインスタンスを生成
        logistic_layer = new LogisticRegression(n_hidden, n_out);
    }

    /*
     * トレーニング：畳み込み → 活性化 → プーリング の処理を行います。
     */
    public void train(double[][][][] x, int[][] t, int minibatch_size, double learning_rate) {

        //逆伝播のため、畳み込み層とプーリング層に対して、以下の値をキャッシュする  1.活性化前 2.活性化後 3ダウンサンプル後
        List<double[][][][]> pre_activated_x = new ArrayList<>(n_kernels.length); //i回目の層を通っている際の、活性化前
        List<double[][][][]> activated_x = new ArrayList<>(n_kernels.length); //i回目の層を通っている際の、活性化後
        List<double[][][][]> downsampled_x = new ArrayList<>(n_kernels.length+1);  // +1 は、一番最初の元画像データを保管するため
        downsampled_x.add(x);

        //各トレーニングに対して、画像データの大きさを持った行列を生成します。
        for (int i = 0; i < n_kernels.length; i++) {
            pre_activated_x.add(new double[minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]); //i回目のトレーニングの前の、元データの画像サイズを生成
            activated_x.add(new double[minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
            downsampled_x.add(new double[minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
        }

        //多層パーセプトロンの入力層をキャッシュします。
        double[][] flattened_x = new double[minibatch_size][flattened_size];

        //隠れ層の出力シグナルをキャッシュ
        double[][] z = new double[minibatch_size][n_hidden];

        double[][] dy;  // 出力層の出力シグナルのδ
        double[][] dz;  // 隠れ層の出力シグナルのδ
        double[][] dx_flatten = new double[minibatch_size][flattened_size];  // 入力層の出力シグナルのδ  ※"入力層の中"では、入力と出力が同じと考えてもよいかもしれません。

        //最後のプーリング層を経過後の、画像データ行列の大きさを生成します。
        double[][][][] dx = new double[minibatch_size][n_kernels[n_kernels.length-1]][pooled_sizes[pooled_sizes.length-1][0]][pooled_sizes[pooled_sizes.length-1][1]];

        double[][][][] dc;


        //ミニバッチの数だけ実行
        for (int n = 0; n < minibatch_size; n++) {

            //z_をトレーニングの入力にするため、初期値をxから取得
            double[][][] z_ = x[n].clone();
            //順伝播： 畳み込み層 + プーリング層
            for (int i = 0; i < n_kernels.length; i++) {
                z_ = convpool_layers[i].forward(z_, pre_activated_x.get(i)[n], activated_x.get(i)[n]); //z_ はi回目のトレーニング後の出力値、つまりi+1回目の入力値でもあります
                downsampled_x.get(i+1)[n] = z_.clone();
            }

            // 多層パーセプトロンの入力にするため、トレーニングの出力を1次元へ展開する。
            double[] x_ = this.flatten(z_);
            flattened_x[n] = x_.clone();

            // 隠れ層を順伝播させる
            z[n] = hidden_layer.forward(x_); //zは隠れ層の出力値
        }


        //出力層（ロジスティック回帰）に対して、順伝播と逆伝播をかける（ミニバッチを使いません）
        dy = logistic_layer.train(z, t, minibatch_size, learning_rate); //tはテストデータに対する回答値、dyは隠れ層の逆伝播で使います

        // 隠れ層に逆伝播をかける
        dz = hidden_layer.backward(flattened_x, z, dy, logistic_layer.W, minibatch_size, learning_rate);

        // 逆伝播誤差を入力層へ反映させる
        for (int n = 0; n < minibatch_size; n++) {
            for (int i = 0; i < flattened_size; i++) {
                for (int j = 0; j < n_hidden; j++) {
                    dx_flatten[n][i] += hidden_layer.W[j][i] * dz[n][j];
                }
            }
            //各ミニバッチに対して、各カーネルの行列（最後のプーリング後）へ戻す
            dx[n] = unflatten(dx_flatten[n]);
        }

        // 畳み込み層 + プーリング層へ逆伝播させる
        dc = dx.clone();
        //最後の層から回していくので、iを減らしていく順番でループします
        for (int i = n_kernels.length-1; i >= 0; i--) {
            dc = convpool_layers[i].backward(downsampled_x.get(i), pre_activated_x.get(i), activated_x.get(i), downsampled_x.get(i+1), dc, minibatch_size, learning_rate);
        }
    }

    /*
     * 多層パーセプトロンの入力にするため、一次元配列へ展開して返却します。
     */
    public double[] flatten(double[][][] z) {

        double[] x = new double[flattened_size];

        int index = 0;
        //最後の特徴マップの数だけループ
        for (int k = 0; k < n_kernels[n_kernels.length-1]; k++) {
            //最後のダウンサンプリング後の配列の成分を、1つずつ取得する
            for (int i = 0; i < pooled_sizes[pooled_sizes.length-1][0]; i++) {
                for (int j = 0; j < pooled_sizes[pooled_sizes.length-1][1]; j++) {
                    x[index] = z[k][i][j];
                    index += 1;
                }
            }
        }
        return x;
    }

    public double[][][] unflatten(double[] x) {

        double[][][] z = new double[n_kernels[n_kernels.length-1]][pooled_sizes[pooled_sizes.length-1][0]][pooled_sizes[pooled_sizes.length-1][1]];

        int index = 0;
        //flattenで代入していた際と同様の順番で、xを取り出していく
        for (int k = 0; k < z.length; k++) {
            for (int i = 0; i < z[0].length; i++) {
                for (int j = 0; j < z[0][0].length; j++) {
                    z[k][i][j] = x[index];
                    index += 1;
                }
            }
        }

        return z;
    }


    public Integer[] predict(double[][][] x) {

        List<double[][][]> pre_activated = new ArrayList<>(n_kernels.length);
        List<double[][][]> activated = new ArrayList<>(n_kernels.length);

        //各トレーニングに対する、活性化直前と、活性化後をキャッシュ
        for (int i = 0; i < n_kernels.length; i++) {
            pre_activated.add(new double[n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
            activated.add(new double[n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
        }

        // トレーニング（畳み込み + プーリング）を順伝播させる
        double[][][] z = x.clone();
        for (int i = 0; i < n_kernels.length; i++) {
            z = convpool_layers[i].forward(z, pre_activated.get(i), activated.get(i));
        }

        // 多層パーセプトロンを順伝播させる
        return logistic_layer.predict(hidden_layer.forward(this.flatten(z)));
    }



    /*
     * データを読み込み、予測を立てます
     * 畳み込みニューラルネットワークの出発
     */
    public static void main(String[] args) {

        final Random rng = new Random(123);  // 乱数のシード

        //
        // 変数と定数の宣言
        //

        final int patterns = 10; //答えのラベルは0～9の10種類

        final int train_n = 60000;  //数を減らすと精度が著しく下がるためそのままを推奨
        final int test_n = 10000;

        final int[] image_size = {28, 28}; //元画像の大きさ
        final int channel = 1;

        int[] n_kernels = {10, 20};
        int[][] kernel_sizes = { {5, 5}, {5, 5} };
        int[][] pool_sizes = { {2, 2}, {2, 2} };

        int n_hidden = 300;
        final int n_out = patterns;

        double[][][][] train_x = new double[train_n][channel][image_size[0]][image_size[1]];
        int[][] train_t = new int[train_n][n_out];

        double[][][][] test_x = new double[test_n][channel][image_size[0]][image_size[1]];
        //ArrayListを使いたいのでInteger型にする
        Integer[][] test_t = new Integer[test_n][n_out];
        Integer[][] predicted_t = new Integer[test_n][n_out];


        int epochs = 500; //epochs回まわしたら終了  テスト稼働には10程度で大丈夫でした
        double learning_rate = 0.001;

        final int minibatch_size = 25;
        int minibatch_n = train_n / minibatch_size;

        double[][][][][] train_x_minibatch = new double[minibatch_n][minibatch_size][channel][image_size[0]][image_size[1]];
        int[][][] train_t_minibatch = new int[minibatch_n][minibatch_size][n_out];
        List<Integer> minibatch_index = new ArrayList<>();
        for (int i = 0; i < train_n; i++) minibatch_index.add(i);
        Collections.shuffle(minibatch_index, rng); //i個目のデータをどのミニバッチに入れるか、シャッフルする

        //MNISTデータ（バイナリ）を読み込みます
        System.out.print("MNIST is loadeding...");
        ReadingMnist read_mnist = new ReadingMnist();
        train_x = read_mnist.readMnistImages(60000, "C:\\<DIRECTORY_PATH>\\train-images.idx3-ubyte");
        train_t = read_mnist.readMnistLabelsInPrm(60000, "C:\\<DIRECTORY_PATH>\\train-labels.idx1-ubyte");
        test_x = read_mnist.readMnistImages(10000, "C:\\<DIRECTORY_PATH>\\t10k-images.idx3-ubyte");
        test_t = read_mnist.readMnistLabelsInObj(10000, "C:\\<DIRECTORY_PATH>\\t10k-labels.idx1-ubyte");
        System.out.println("done.");

        // ミニバッチを生成（シャッフル結果を反映させ、各ミニバッチに振り分ける）
        for (int j = 0; j < minibatch_size; j++) {
            for (int i = 0; i < minibatch_n; i++) {
                train_x_minibatch[i][j] = train_x[minibatch_index.get(i * minibatch_size + j)];
                train_t_minibatch[i][j] = train_t[minibatch_index.get(i * minibatch_size + j)];
            }
        }

        // 畳み込みニューラルネットワークのモデルを生成
        System.out.print("Building the model...");
        ConvolutionalNeuralNetworks classifier = new ConvolutionalNeuralNetworks(image_size, channel, n_kernels, kernel_sizes, pool_sizes, n_hidden, n_out, rng);
        System.out.println("done.");

        // トレーニングを実行
        System.out.print("Training the model...");
        System.out.println();

        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("\titer = " + (epoch + 1) + " / " + epochs); //長くなるので、途中経過を出力

            //生成したミニバッチの数だけ実行
            for (int batch = 0; batch < minibatch_n; batch++) {
                classifier.train(train_x_minibatch[batch], train_t_minibatch[batch], minibatch_size, learning_rate);
            }
            learning_rate *= 0.99; //学習率は逓減させる
        }
        System.out.println("done.");


        // テストする
        for (int i = 0; i < test_n; i++) {
            predicted_t[i] = classifier.predict(test_x[i]);
        }


        //
        // モデルの精度を検証する
        //

        int[][] confusion_matrix = new int[patterns][patterns];
        double accuracy = 0.;
        double[] precision = new double[patterns];
        double[] recall = new double[patterns];

        //どこに何個予想したかみてみよう
        int[] hits_cnt = new int[10];
        for(int n_i=0; n_i < test_n; n_i++) {
            hits_cnt[Arrays.asList(predicted_t[n_i]).indexOf(1)] += 1;
        }
        for(int i=0; i < patterns; i++) {
            System.out.print(hits_cnt[i] + ", ");
        }
        System.out.println();

        for (int i = 0; i < test_n; i++) {
            int predicted_ = Arrays.asList(predicted_t[i]).indexOf(1); //正解ラベルのIndex番号を返却（テストデータから予測）
            int actual_ = Arrays.asList(test_t[i]).indexOf(1); //正解ラベルのIndex番号を返却（既知の回答）

            confusion_matrix[actual_][predicted_] += 1; //結果を配列に加算  正解していれば、i行目i列に1が入りますね
        }

        for (int i = 0; i < patterns; i++) {
            double col_ = 0.;
            double row_ = 0.;

            for (int j = 0; j < patterns; j++) {

                if (i == j) {
                    accuracy += confusion_matrix[i][j];
                    precision[i] += confusion_matrix[j][i];
                    recall[i] += confusion_matrix[i][j];
                }

                col_ += confusion_matrix[j][i];
                row_ += confusion_matrix[i][j];
            }
            precision[i] /= col_;
            recall[i] /= row_;
        }

        accuracy /= test_n;

        System.out.println("--------------------");
        System.out.println("CNN model evaluation");
        System.out.println("--------------------");
        System.out.printf("Accuracy: %.1f %%\n", accuracy * 100);
        System.out.println("Precision:");
        for (int i = 0; i < patterns; i++) {
            System.out.printf(" class %d: %.1f %%\n", i+1, precision[i] * 100);
        }
        System.out.println("Recall:");
        for (int i = 0; i < patterns; i++) {
            System.out.printf(" class %d: %.1f %%\n", i+1, recall[i] * 100);
        }

    }

}
