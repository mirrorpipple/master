package dllearning.multilayer;

import java.util.Random;
import static dllearning.util.RandomGenerator.*;
import static dllearning.util.ActivationFunction.*;

/*
 * �B��w  �������֐��ɂ�ReLU���g�p���܂�
 */
public class HiddenLayer {

    public int n_in;
    public int n_out;
    public double[][] W;
    public double[] b;
    public Random rng;

    public HiddenLayer(int n_in, int n_out, double[][] W, double[] b, Random rng) {

        if (rng == null) rng = new Random(1234);  // �����𐶐�

        //�d�݂̏����l�𐶐����܂�
        if (W == null) {
            W = new double[n_out][n_in]; //���͂Əo�͂̑g�ݍ��킹�ŁA�قȂ�d�݂��g�p
            double w_ = 1. / n_in;

            for(int j = 0; j < n_out; j++) {
                for(int i = 0; i < n_in; i++) {
                    W[j][i] = uniform(-w_, w_, rng);
                }
            }
        }

        //�o�C�A�X�̔��𐶐�  �o�C�A�X�́A�o�͂ɑ΂���1���݂��܂�
        if (b == null) b = new double[n_out];

        //�������t�B�[���h�ϐ��ֈڍs
        this.n_in = n_in;
        this.n_out = n_out;
        this.W = W;
        this.b = b;
        this.rng = rng;
    }

    /*
     * �C���v�b�g�̃V�O�i���ɑ΂��āA�d�݂Â����s���A�������֐���ʂ��āA�������ꂽ�o�͑w��Ԃ��܂��B
     */
    public double[] output(double[] x) {

        double[] y = new double[n_out]; //�o�͗p�̃V�O�i���̏���

        for (int j = 0; j < n_out; j++) {
            double pre_activation_ = 0.;

            //���͂Əo�͂̑g�ݍ��킹�ɂ��āA�d�݂Â����v�Z
            for (int i = 0; i < n_in; i++) {
                pre_activation_ += W[j][i] * x[i];
            }
            //�o��j�ɑ΂���o�C�A�X�����Z����
            pre_activation_ += b[j];

            //�������֐�ReLU��ʂ�
            y[j] = stepByReLU(pre_activation_);
        }

        return y;
    }

    /*
     * ���`�d������
     */
    public double[] forward(double[] x) {
        return output(x);
    }

    /*
     * �t�`�d�����܂�
     * �o�͑w��1�O�̉B��w���ƁAdy��y�͏o�͑w��Ώۂɂ��Ă���̂ŁAdy���Ay=softmax(x)����Z�o�ł��܂�
     */
    public double[][] backward(double[][] x, double[][] z, double[][] dy, double[][] W_prev, int minibatch_size, double learning_rate) {

        double[][] dz = new double[minibatch_size][n_out];  // �t�`�d�덷

        double[][] grad_W = new double[n_out][n_in]; //�o�b�N�v���p�Q�[�V�����Œ����ς݂�W
        double[] grad_b = new double[n_out]; //�o�b�N�v���p�Q�[�V�����Œ����ς݂�b

        // SGD��p���Ċw�K��
        // �܂��t�`�d�덷�����߂āA W��b�̌��z�����߂�  �~�j�o�b�`���Ɏ��s���A�Ō�ɕ��ςɂȂ炷�`�ő������킹��
        for (int n = 0; n < minibatch_size; n++) {

            //�e�o��j�ɑ΂��āA�t�`�d�����Ō�����1�O�̑w
            //�i���`�d�Ō���1��̑w�A�B��w��1�Ȃ�o�͑w�j�̏o��k�����ׂĂɑ΂��Čv�Z�������̂�g�ݍ���ł����܂��B
            for (int j = 0; j < n_out; j++) {
                for (int k = 0; k < dy[0].length; k++) {  // k < ( �O�w��n_out)
                    dz[n][j] += W_prev[k][j] * dy[n][k]; //��w���v�Z
                }
                dz[n][j] *= differentiateByReLU(z[n][j]); //��w�� ��h'(a)��������  ����ŏo��j�ɑ΂�����v�Z�ł��܂���

                for (int i = 0; i < n_in; i++) {
                    grad_W[j][i] += dz[n][j] * x[n][i]; //�d�݂̌��z���v�Z
                }

                grad_b[j] += dz[n][j]; //�o�C�A�X�̌��z���v�Z
            }
        }

        // �p�����[�^W,b���X�V  ���z�̔��f�𕽋ω������邽�߁A�~�j�o�b�`�̐��ŏ��Z���������t�B�[���h�ϐ��ɉ��Z�i���Z�j���܂�
        for (int j = 0; j < n_out; j++) { //�o�̓V�O�i���̐��������[�v
            for(int i = 0; i < n_in; i++) { //���̓V�O�i���̐��������[�v
                W[j][i] -= learning_rate * grad_W[j][i] / minibatch_size; //�d�݂͓��̓V�O�i���Əo�̓V�O�i���̑g�ݍ��킹�̐���������
            }
            b[j] -= learning_rate * grad_b[j] / minibatch_size; //�o�C�A�X�͏o�̓V�O�i���̐���������
        }

        return dz; //���ꂪ���̑w�i�t�`�d�����Ō��āj�́Ady�ɂȂ�̂ł���  �������Ȃ�
    }



}
