package dllearning.util;

public class MnistTest {

    public static void main(String[] args){

        // MNIST読み込みがうまくいくかテストします（JUnitでした方がいいんですが、画面出力もしたいしとりあえず）

        ReadingMnist read_mnist = new ReadingMnist();

        double[][][][] test_x = read_mnist.readMnistImages(10000, "C:\\Users\\bell\\Desktop\\MNIST\\data\\t10k-images.idx3-ubyte");
        Integer[][] test_t = read_mnist.readMnistLabelsInObj(10000, "C:\\Users\\bell\\Desktop\\MNIST\\data\\t10k-labels.idx1-ubyte");


        //テスト出力
        String line="";
        String label_msg ="";
        for(int cnt = 0; cnt < 3; cnt++) {
            //ラベルを取得
            for(int l_i = 0; l_i < 10; l_i++) {
                if(test_t[cnt][l_i] == 1) {
                    label_msg = "Number is " + l_i;
                }
            }

            //画像データを生成  色が0より大きい場合、#を表示する
            for(int i = 0; i < 28; i++) {
                for(int j = 0; j < 28; j++) {
                    if(test_x[cnt][0][i][j] > 0.5) {
                        line = line + "#";
                    } else if(test_x[cnt][0][i][j] > 0.0) {
                        line = line + "*";
                    } else {
                        line = line + " ";
                    }
                }
                //1行分の文字列を取得したので、改行を差し込む
                line = line + "\r\n";
            }
            //画像データ1つ分を出力する
            System.out.println(label_msg);
            System.out.print(line);
            System.out.println(); //ブランクを1行はさむ
            //出力を初期化する
            label_msg="";
            line="";
        }
    }



}
