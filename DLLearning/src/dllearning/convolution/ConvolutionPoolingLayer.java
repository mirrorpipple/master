package dllearning.convolution;

import java.util.Random;
import static dllearning.util.RandomGenerator.*;
import static dllearning.util.ActivationFunction.*;

/*
 * ��ݍ��ݑw�ƃv�[�����O�w��ʉ߂����邽�߂̃N���X
 * �������֐��ɂ́AReLU���g�p
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

    //�R���X�g���N�^
    public ConvolutionPoolingLayer(int[] image_size, int channel, int n_kernel, int[] kernel_size, int[] pool_size, int[] convolved_size, int[] pooled_size, Random rng) {

        if (rng == null) rng = new Random(1234);

        //�d�݂̏����l�𐶐����܂��B
        if (W == null) {

            // W = {�J�[�l����, �`�����l��, �J�[�l���̑傫���ii�j, �J�[�l���̑傫���ij�j}
            W = new double[n_kernel][channel][kernel_size[0]][kernel_size[1]];

            double in_ = channel * kernel_size[0] * kernel_size[1]; //�v�[�����O���s�O�̍s��̐�����
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

        //�o�C�A�X������������
        if (b == null) {
            b = new double[n_kernel];
        }

        //�t�B�[���h�ϐ����X�V����
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
     * ���`�d������
     * �^����ꂽ�z��ɑ΂��āA��ݍ��݂Ɗ������A�v�[�����O��^����
     */
    public double[][][] forward(double[][][] x, double[][][] pre_activated_x, double[][][] activated_x) {

        double[][][] z = this.convolve(x, pre_activated_x, activated_x);
        return  this.downsample(z);

    }

    /*
     * �t�`�d������
     */

    public double[][][][] backward(double[][][][] x, double[][][][] pre_activated_x, double[][][][] activated_x, double[][][][] downsampled_x, double[][][][] dy, int minibatch_size, double learning_rate) {

        double[][][][] dz = this.upsample(activated_x, downsampled_x, dy, minibatch_size);
        return this.deconvolve(x, pre_activated_x, dz, minibatch_size, learning_rate);

    }

    /*
     * �n���ꂽx�ɑ΂��ď�ݍ��݂������A�����ReLU�Ŋ����������z���ԋp����B
     */
    public double[][][] convolve(double[][][] x, double[][][] pre_activated_x, double[][][] activated_x) {

        //y�͓����}�b�v��������������̔z��
        double[][][] y = new double[n_kernel][convolved_size[0]][convolved_size[1]]; //y[���̏�ݍ��݂Ŏg�p����J�[�l����][�����}�b�v�̍s�̐�][�����}�b�v�̗�̐�]

        for (int kernel_i = 0; kernel_i < n_kernel; kernel_i++) {
            for (int i = 0; i < convolved_size[0]; i++) {
                for(int j = 0; j < convolved_size[1]; j++) {

                    double convolved_ = 0.;

                    //��ݍ��݂��s���A�J�[�l���ň��k���ꂽ�l�iconvolved_�j�𐶐�
                    for (int channel_i = 0; channel_i < channel; channel_i++) {
                        for (int s = 0; s < kernel_size[0]; s++) {
                            for (int t = 0; t < kernel_size[1]; t++) {
                                convolved_ += W[kernel_i][channel_i][s][t] * x[channel_i][i+s][j+t];
                            }
                        }
                    }

                    //pre_activated�i�������O�j�̒l�i�����}�b�v�̐��� + �o�C�A�X�j���L���b�V������
                    pre_activated_x[kernel_i][i][j] = convolved_ + b[kernel_i];
                    //�����}�b�v��ReLU�Ŋ���������
                    activated_x[kernel_i][i][j] = stepByReLU(pre_activated_x[kernel_i][i][j]);
                    //�Ԃ�l�֑��
                    y[kernel_i][i][j] = activated_x[kernel_i][i][j];
                }
            }
        }

        return y;
    }
    /*
     * x�ɑ΂��ăv�[�����O���������z���ԋp����B
     */
    public double[][][] downsample(double[][][] x) {

        double[][][] y = new double[n_kernel][pooled_size[0]][pooled_size[1]];

        for (int kernel_i = 0; kernel_i < n_kernel; kernel_i++) {
            for (int i = 0; i < pooled_size[0]; i++) {
                for (int j = 0; j < pooled_size[1]; j++) {

                    //�T�u�f�[�^���̍ő�l�𐶐�
                    double max_ = 0.;

                    //�v�[�����O�̑傫���������[�v�i�T�u�f�[�^�̂��ׂĂ̒l�ɑ΂��āA�ǂꂪ��ԑ傫�������`�F�b�N�j
                    for (int s = 0; s < pool_size[0]; s++) {
                        for (int t = 0; t < pool_size[1]; t++) {

                            //max_�̏����l�͈�ԍ���̒l
                            if (s == 0 && t == 0) {
                                max_ = x[kernel_i][pool_size[0]*i][pool_size[1]*j];
                                continue;
                            }
                            //���傫���l������������Amax_���X�V����
                            if (max_ < x[kernel_i][pool_size[0]*i+s][pool_size[1]*j+t]) {
                                max_ = x[kernel_i][pool_size[0]*i+s][pool_size[1]*j+t];
                            }
                        }
                    }

                    //�_�E���T���v�����O���ꂽ�z��ցA�l�������Ă���
                    y[kernel_i][i][j] = max_;
                }
            }
        }

        return y;
    }

    /*
     * �v�[�����O�őI�����ꂽ�����ɑ΂��Č덷��`�d�����邽�߁A�덷�̍s���ԋp���܂�
     * �I������Ȃ����������ɑ΂��Ă͌덷0��ԋp���܂�
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

                                //�_�E���T���v���őI�΂ꂽ�����ɑ΂��āA��`�d������  ���̐����ɂ͌덷0��n��
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
     * y = pre_activated_x, dy = dz �Ŏ擾���܂�
     * pre_activated_x = [minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]
     *
     */
    public double[][][][] deconvolve(double[][][][] x, double[][][][] y, double[][][][] dy, int minibatch_size, double learning_rate) {

        double[][][][] grad_W = new double[n_kernel][channel][kernel_size[0]][kernel_size[1]];
        double[] grad_b = new double[n_kernel];
        double[][][][] dx = new double[minibatch_size][channel][image_size[0]][image_size[1]];

        //W, b�̌��z���v�Z�i�~�j�o�b�`�����ׂĔ��f�j
        for (int n = 0; n < minibatch_size; n++) {
            for (int k = 0; k < n_kernel; k++) {
                for (int i = 0; i < convolved_size[0]; i++) {
                    for (int j = 0; j < convolved_size[1]; j++) {


                        double d_ = dy[n][k][i][j] * differentiateByReLU(y[n][k][i][j]);

                        //b�̌��z���v�Z
                        grad_b[k] += d_;

                        for (int c = 0; c < channel; c++) {
                            for (int s = 0; s < kernel_size[0]; s++) {
                                for (int t = 0; t < kernel_size[1]; t++) {
                                    //W�̌��z���v�Z
                                    grad_W[k][c][s][t] += d_ * x[n][c][i+s][j+t];
                                }
                            }
                        }
                    }
                }
            }
        }

        //���z���X�V
        for (int k = 0; k < n_kernel; k++) {
            b[k] -= learning_rate * grad_b[k] / minibatch_size; //b�͊������֐��̌덷�Ȃ̂ŁA�J�[�l���v�Z�̌덷�Ƃ��Ă͊֌W���܂���

            for (int c = 0; c < channel; c++) {
                for (int s = 0; s < kernel_size[0]; s++) {
                    for(int t = 0; t < kernel_size[1]; t++) {
                        W[k][c][s][t] -= learning_rate * grad_W[k][c][s][t] / minibatch_size; //�J�[�l���̑S�Ă̍��ɑ΂��āA�Ɨ������d��W���g�p���Ă��܂�
                    }
                }
            }
        }

        // x�i���́j�ɑ΂���f���^���v�Z���܂�
        for (int n = 0; n < minibatch_size; n++) {
            for (int c = 0; c < channel; c++) {
                for (int i = 0; i < image_size[0]; i++) {
                    for (int j = 0; j < image_size[1]; j++) {

                        for (int k = 0; k < n_kernel; k++) {
                            for (int s = 0; s < kernel_size[0]; s++) {
                                for (int t = 0; t < kernel_size[1]; t++) {

                                    double d_ = 0.;

                                    //TODO:�����}�b�v�̍����0�p�f�B���O�Ȃ̂͂킩�邯�ǁA�ǂ�����kernel_size�Ō��Z���Ă���̂��킩��Ȃ�...
                                    //���̂悤��if�������������ꌩ�A�����ɒ����Ɍ����邯���...�H
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
