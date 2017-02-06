package dllearning.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/*
 * MNISTファイルを読み込みます
 * Training data: 60,000
 * test data: 10,000
 */
public class ReadingMnist {


    public ReadingMnist(){

    }

    /*
     * 28 x 28 のMNIST画像データを読み込む
     *
     * 0    integer  2051    マジックナンバー？
     * 4    integer  60000, 10000  画像データ数
     * 8    integer  28 行の数
     * 12   integer  28 列の数
     * 16～ unsigned byte  0～255 1byteずつ色の値（白黒）
     */
    public double[][][][] readMnistImages(int data_n, String file_path) {

        int image_size = 28;
        double [][][][] txt_data = new double[data_n][1][image_size][image_size];


        // binaryでファイルを読み込む
        int data_i=0;
        try {
            InputStream input_s = new BufferedInputStream(new FileInputStream(file_path));
            // 画像の大きさは定数とみなしているため、ヘッダは読み飛ばします
            byte[] buf_bef = new byte[16];
            input_s.read(buf_bef);

            //画像１つ分ずつ読み込む
            byte[] buf = new byte[784];
            while(input_s.read(buf) != -1) {
                for(int buf_i = 0; buf_i < 784; buf_i++) {
                    double d = (double) (buf[buf_i] & 0xff) / 255;
                    txt_data[data_i][0][(int) buf_i/28][buf_i % 28] = d;
                }
                data_i++;
            }

            input_s.close();

        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();

        }
        return txt_data;
    }

    /*
     * MNISTのラベルデータを読み込む（int型で返却）
     * 0    integer  2049    マジックナンバー？
     * 4    integer  60000, 10000  ラベルデータ数
     */
    public int[][] readMnistLabelsInPrm(int data_n, String file_path) {

        int[][] label_data = new int[data_n][10];

        // binaryでファイルを読み込む
        try {
            InputStream input_s = new BufferedInputStream(new FileInputStream(file_path));
            // 画像の大きさは定数とみなしているため、ヘッダは読み飛ばします
            byte[] buf_bef = new byte[8];
            input_s.read(buf_bef);

            //画像１つ分ずつ読み込む
            byte[] buf = new byte[1];
            int data_i=0;
            while(input_s.read(buf) != -1) {

                for(int n = 0; n < 10; n++) {
                    label_data[data_i][n] = 0; //初期値を埋め込む  int型なので、なくてもよかったかもしれません
                }
                label_data[data_i][buf[0] & 0xff] = 1;
                data_i++;
            }

            input_s.close();

        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        return label_data;
    }

    /*
     * MNISTのラベルデータを読み込む（Integer型で返却）
     * 0    integer  2049    マジックナンバー？
     * 4    integer  60000, 10000  画像データ数
     */
    public Integer[][] readMnistLabelsInObj(int data_n, String file_path) {

        Integer[][] label_data = new Integer[data_n][10];

        // binaryでファイルを読み込む
        try {
            InputStream input_s = new BufferedInputStream(new FileInputStream(file_path));
            // 画像の大きさは定数とみなしているため、ヘッダは読み飛ばします
            byte[] buf_bef = new byte[8];
            input_s.read(buf_bef);

            //画像１つ分ずつ読み込む
            byte[] buf = new byte[1];
            int data_i=0;
            while(input_s.read(buf) != -1) {

                for(int n = 0; n < 10; n++) {
                    label_data[data_i][n] = 0; //初期値を埋め込む  Integer型にしているので、初期値がnullになっています
                }
                label_data[data_i][buf[0] & 0xff] = 1;
                data_i++;
            }

            input_s.close();

        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        return label_data;
    }
}
