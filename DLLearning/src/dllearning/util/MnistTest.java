package dllearning.util;

public class MnistTest {

    public static void main(String[] args){

        // MNIST�ǂݍ��݂����܂��������e�X�g���܂��iJUnit�ł�������������ł����A��ʏo�͂����������Ƃ肠�����j

        ReadingMnist read_mnist = new ReadingMnist();

        double[][][][] test_x = read_mnist.readMnistImages(10000, "C:\\Users\\bell\\Desktop\\MNIST\\data\\t10k-images.idx3-ubyte");
        Integer[][] test_t = read_mnist.readMnistLabelsInObj(10000, "C:\\Users\\bell\\Desktop\\MNIST\\data\\t10k-labels.idx1-ubyte");


        //�e�X�g�o��
        String line="";
        String label_msg ="";
        for(int cnt = 0; cnt < 3; cnt++) {
            //���x�����擾
            for(int l_i = 0; l_i < 10; l_i++) {
                if(test_t[cnt][l_i] == 1) {
                    label_msg = "Number is " + l_i;
                }
            }

            //�摜�f�[�^�𐶐�  �F��0���傫���ꍇ�A#��\������
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
                //1�s���̕�������擾�����̂ŁA���s����������
                line = line + "\r\n";
            }
            //�摜�f�[�^1�����o�͂���
            System.out.println(label_msg);
            System.out.print(line);
            System.out.println(); //�u�����N��1�s�͂���
            //�o�͂�����������
            label_msg="";
            line="";
        }
    }



}
