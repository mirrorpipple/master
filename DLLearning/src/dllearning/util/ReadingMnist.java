package dllearning.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/*
 * MNIST�t�@�C����ǂݍ��݂܂�
 * Training data: 60,000
 * test data: 10,000
 */
public class ReadingMnist {


    public ReadingMnist(){

    }

    /*
     * 28 x 28 ��MNIST�摜�f�[�^��ǂݍ���
     *
     * 0    integer  2051    �}�W�b�N�i���o�[�H
     * 4    integer  60000, 10000  �摜�f�[�^��
     * 8    integer  28 �s�̐�
     * 12   integer  28 ��̐�
     * 16�` unsigned byte  0�`255 1byte���F�̒l�i�����j
     */
    public double[][][][] readMnistImages(int data_n, String file_path) {

        int image_size = 28;
        double [][][][] txt_data = new double[data_n][1][image_size][image_size];


        // binary�Ńt�@�C����ǂݍ���
        int data_i=0;
        try {
            InputStream input_s = new BufferedInputStream(new FileInputStream(file_path));
            // �摜�̑傫���͒萔�Ƃ݂Ȃ��Ă��邽�߁A�w�b�_�͓ǂݔ�΂��܂�
            byte[] buf_bef = new byte[16];
            input_s.read(buf_bef);

            //�摜�P�����ǂݍ���
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
            // TODO �����������ꂽ catch �u���b�N
            e.printStackTrace();

        }
        return txt_data;
    }

    /*
     * MNIST�̃��x���f�[�^��ǂݍ��ށiint�^�ŕԋp�j
     * 0    integer  2049    �}�W�b�N�i���o�[�H
     * 4    integer  60000, 10000  ���x���f�[�^��
     */
    public int[][] readMnistLabelsInPrm(int data_n, String file_path) {

        int[][] label_data = new int[data_n][10];

        // binary�Ńt�@�C����ǂݍ���
        try {
            InputStream input_s = new BufferedInputStream(new FileInputStream(file_path));
            // �摜�̑傫���͒萔�Ƃ݂Ȃ��Ă��邽�߁A�w�b�_�͓ǂݔ�΂��܂�
            byte[] buf_bef = new byte[8];
            input_s.read(buf_bef);

            //�摜�P�����ǂݍ���
            byte[] buf = new byte[1];
            int data_i=0;
            while(input_s.read(buf) != -1) {

                for(int n = 0; n < 10; n++) {
                    label_data[data_i][n] = 0; //�����l�𖄂ߍ���  int�^�Ȃ̂ŁA�Ȃ��Ă��悩������������܂���
                }
                label_data[data_i][buf[0] & 0xff] = 1;
                data_i++;
            }

            input_s.close();

        } catch (IOException e) {
            // TODO �����������ꂽ catch �u���b�N
            e.printStackTrace();
        }
        return label_data;
    }

    /*
     * MNIST�̃��x���f�[�^��ǂݍ��ށiInteger�^�ŕԋp�j
     * 0    integer  2049    �}�W�b�N�i���o�[�H
     * 4    integer  60000, 10000  �摜�f�[�^��
     */
    public Integer[][] readMnistLabelsInObj(int data_n, String file_path) {

        Integer[][] label_data = new Integer[data_n][10];

        // binary�Ńt�@�C����ǂݍ���
        try {
            InputStream input_s = new BufferedInputStream(new FileInputStream(file_path));
            // �摜�̑傫���͒萔�Ƃ݂Ȃ��Ă��邽�߁A�w�b�_�͓ǂݔ�΂��܂�
            byte[] buf_bef = new byte[8];
            input_s.read(buf_bef);

            //�摜�P�����ǂݍ���
            byte[] buf = new byte[1];
            int data_i=0;
            while(input_s.read(buf) != -1) {

                for(int n = 0; n < 10; n++) {
                    label_data[data_i][n] = 0; //�����l�𖄂ߍ���  Integer�^�ɂ��Ă���̂ŁA�����l��null�ɂȂ��Ă��܂�
                }
                label_data[data_i][buf[0] & 0xff] = 1;
                data_i++;
            }

            input_s.close();

        } catch (IOException e) {
            // TODO �����������ꂽ catch �u���b�N
            e.printStackTrace();
        }
        return label_data;
    }
}
