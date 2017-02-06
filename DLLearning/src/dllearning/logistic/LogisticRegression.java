package dllearning.logistic;

import static dllearning.util.ActivationFunction.softmax;

public class LogisticRegression {

    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;


    public LogisticRegression(int n_in, int n_out) {
        //�������t�B�[���h�ϐ��֔��f
        this.n_in = n_in;
        this.n_out = n_out;

        W = new double[n_out][n_in]; //�d��W�͓��͂Əo�͂̑g�ݍ��킹�̐���������
        b = new double[n_out]; //�o�C�A�Xb�͏o�͂̐���������
    }

    public double[][] train(double[][] x, int t[][], int minibatch_size, double learning_rate) {

        double[][] grad_W = new double[n_out][n_in]; //W�̌��z
        double[] grad_b = new double[n_out]; //b�̌��z

        double[][] dy = new double[minibatch_size][n_out]; //softmax�֐��Ō����Ƃ��́Ay�̌��z

        // 1. �d��W�ƃo�C�A�Xb�̌��z���v�Z  �~�j�o�b�`�̐��ŕ��ς������̂𔽉f�����܂�
        for (int n = 0; n < minibatch_size; n++) {

            //����x�ɑ΂���softmax�֐���ʂ����l���擾
            double[] predicted_y_ = output(x[n]);

            for (int j = 0; j < n_out; j++) {
                dy[n][j] = predicted_y_[j] - t[n][j]; //t�͊��m�̐���(�����f�[�^�x�N�g��)  �N���Xj�ɑ����Ă����1�A�����Ă��Ȃ����0�����܂�

                for (int i = 0; i < n_in; i++) {
                    grad_W[j][i] += dy[n][j] * x[n][i]; //�d�݂̌��z���v�Z
                }
                grad_b[j] += dy[n][j]; //�o�C�A�X�̌��z���v�Z
            }
        }

        // 2. �v�Z�������z�����ƂɁA�p�����[�^���X�V  �~�j�o�b�`�̐��Ŕ��f�𕽋ςɂȂ炵�܂�
        for (int j = 0; j < n_out; j++) {
            for (int i = 0; i < n_in; i++) {
                W[j][i] -= learning_rate * grad_W[j][i] / minibatch_size;
            }
            b[j] -= learning_rate * grad_b[j] / minibatch_size;
        }

        return dy; //�B��w�̋t�`�d���Ŏg�p���������߁Ady��ԋp���܂�
    }

    /*
     * �e���͂����ƂɁAsoftmax�֐���ʂ����l��ԋp���܂�
     */
    public double[] output(double[] x) {

        double[] pre_activation = new double[n_out];

        for (int j = 0; j < n_out; j++) {
            for (int i = 0; i < n_in; i++) {
                pre_activation[j] += W[j][i] * x[i]; //���̓V�O�i���Əo�̓V�O�i���̑g�ݍ��킹�ŁA�d�݂Â����v�Z
            }
            pre_activation[j] += b[j];  //�o�̓V�O�i���ɑ΂��āA�o�C�A�X�����Z  ���O�v�Z�̊���
        }

        return softmax(pre_activation, n_out); //softmax�֐���ʂ��Ēl��ԋp
    }

    public Integer[] predict(double[] x) {

        double[] y = output(x);  // �n���ꂽ�l�ɑ΂��āAsoftmax�֐��������܂�
        Integer[] t = new Integer[n_out]; // �o�͂͊m���̂��߁A���x���ɕϊ�

        int argmax = -1;
        double max = 0.;

        for (int i = 0; i < n_out; i++) {
            if (max < y[i]) {
                max = y[i];
                argmax = i;
            }
        }

        //�m���ő�̃��x���ɑ΂��āA�t���O�𗧂Ă�
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
