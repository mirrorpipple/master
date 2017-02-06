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

    public int[] n_kernels; //i��ڂ̏�ݍ��ݑw�Ŏg�p����J�[�l���̐�
    public int[][] kernel_sizes; //i��ڏ�ݍ��ݑw�ɂ��āA{0=i, 1=j}�ɑ΂���J�[�l���̃T�C�Y�i�܂��ڂ̐��j
    public int[][] pool_sizes; //i��ڃv�[�����O�w�ɂ��āA{0=i, 1=j}�ɑ΂���v�[�����O�̃T�C�Y�i�܂��ڂ̐��j
    public int n_hidden; //�B��w�̃��j�b�g��
    public int n_out; //�ŏI�I�ȕ��ނ̎��  0�`9�Ȃ̂ł����ł�10�ɂȂ�܂��ˁB

    public ConvolutionPoolingLayer[] convpool_layers;
    public int[][] convolved_sizes;
    public int[][] pooled_sizes;
    public int flattened_size;
    public HiddenLayer hidden_layer;
    public LogisticRegression logistic_layer;
    public Random rng;


    /*
     * �����F
     * �J�[�l���� = 10, 20
     * �`���l���� = 1
     * �J�[�l���T�C�Y = 5
     * �v�[�����O�T�C�Y = 2
     * �ŏI�o�͐��i���W�X�e�B�b�N�̓��͑w�̐��j = 4 * 4 * 20 = 320  �傫����  ��邩��
     */


    public ConvolutionalNeuralNetworks(int[] image_size, int channel, int[] n_kernels, int[][] kernel_sizes, int[][] pool_sizes, int n_hidden, int n_out, Random rng) {

        //�����𐶐�
        if (rng == null) rng = new Random(1234);

        this.n_kernels = n_kernels;
        this.kernel_sizes = kernel_sizes;
        this.pool_sizes = pool_sizes;
        this.n_hidden = n_hidden;
        this.n_out = n_out;
        this.rng = rng;

        //�܂�����p�ӂ���Bimage_size�́A���͂Ƃ��Ď󂯕t����摜�f�[�^�̔z��ł��B
        //28 x 28�̓񎟌��摜�Ȃ� int[] image_size = {28, 28}
        convpool_layers = new ConvolutionPoolingLayer[n_kernels.length]; //��ݍ��ݑw�̐������A��ݍ��ݗp�C���X�^���X�𐶐�����
        convolved_sizes = new int[n_kernels.length][image_size.length]; //convolved_sizes[��ݍ��ݑw�̐�][���̉摜�f�[�^�̎�����] image_size.length�́A�摜�f�[�^�̎�������Ԃ��܂��B
        pooled_sizes = new int[n_kernels.length][image_size.length];

        //��ݍ��ݑw�̉񐔂����AConvolutionPoolingLayer�C���X�^���X�𐶐����Aconvpool_layers�֊i�[����
        for (int i = 0; i < n_kernels.length; i++) {
            int[] size_;
            int channel_;

            if (i == 0) {
              //��ݍ��ݑw��0��ځA�܂���͌��̉摜�f�[�^�̑傫�����擾����  ���̉摜��2�����Ō��ߑł��ł�
                size_ = new int[]{image_size[0], image_size[1]};
                channel_ = channel;
            } else {
                //1��ڈȍ~�́A��ݍ���Ń_�E���T���v�����O���ꂽ��̑傫�����擾����
                size_ = new int[]{pooled_sizes[i-1][0], pooled_sizes[i-1][1]};
                channel_ = n_kernels[i-1]; //�����}�b�v�̐����A���̑w�̃`�����l�����ɂȂ�̂ł���
            }

            convolved_sizes[i] = new int[]{size_[0] - kernel_sizes[i][0] + 1, size_[1] - kernel_sizes[i][1] + 1}; //nm�s���ij�s��̃J�[�l���𓖂Ă͂߂�ƁA�����}�b�v��(n-i+1)(m-j+1)�s��ɂȂ�܂�
            pooled_sizes[i] = new int[]{convolved_sizes[i][0] / pool_sizes[i][0], convolved_sizes[i][1] / pool_sizes[i][1]}; //�J�[�l���Fnm�s�� �� �v�[�����O�̑傫���Fst�s�� �� ij�s��  �Ƃ���ꍇ�Ai=n/s, j=m/t �ɂȂ�܂�

            convpool_layers[i] = new ConvolutionPoolingLayer(size_, channel_, n_kernels[i], kernel_sizes[i], pool_sizes[i], convolved_sizes[i], pooled_sizes[i], rng);
        }

        // �ŏI�I�ȃv�[�����O��̔z���1�����ɕϊ����A���w�p�[�Z�v�g���������͑w�Ŏ󂯎��V�O�i���̐��ɂ���
        flattened_size = n_kernels[n_kernels.length-1] * pooled_sizes[pooled_sizes.length-1][0] * pooled_sizes[pooled_sizes.length-1][1];

        // �B��w�̃C���X�^���X�𐶐�
        hidden_layer = new HiddenLayer(flattened_size, n_hidden, null, null, rng);

        // �o�͑w�i���W�X�e�B�b�N��A�j�̃C���X�^���X�𐶐�
        logistic_layer = new LogisticRegression(n_hidden, n_out);
    }

    /*
     * �g���[�j���O�F��ݍ��� �� ������ �� �v�[�����O �̏������s���܂��B
     */
    public void train(double[][][][] x, int[][] t, int minibatch_size, double learning_rate) {

        //�t�`�d�̂��߁A��ݍ��ݑw�ƃv�[�����O�w�ɑ΂��āA�ȉ��̒l���L���b�V������  1.�������O 2.�������� 3�_�E���T���v����
        List<double[][][][]> pre_activated_x = new ArrayList<>(n_kernels.length); //i��ڂ̑w��ʂ��Ă���ۂ́A�������O
        List<double[][][][]> activated_x = new ArrayList<>(n_kernels.length); //i��ڂ̑w��ʂ��Ă���ۂ́A��������
        List<double[][][][]> downsampled_x = new ArrayList<>(n_kernels.length+1);  // +1 �́A��ԍŏ��̌��摜�f�[�^��ۊǂ��邽��
        downsampled_x.add(x);

        //�e�g���[�j���O�ɑ΂��āA�摜�f�[�^�̑傫�����������s��𐶐����܂��B
        for (int i = 0; i < n_kernels.length; i++) {
            pre_activated_x.add(new double[minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]); //i��ڂ̃g���[�j���O�̑O�́A���f�[�^�̉摜�T�C�Y�𐶐�
            activated_x.add(new double[minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
            downsampled_x.add(new double[minibatch_size][n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
        }

        //���w�p�[�Z�v�g�����̓��͑w���L���b�V�����܂��B
        double[][] flattened_x = new double[minibatch_size][flattened_size];

        //�B��w�̏o�̓V�O�i�����L���b�V��
        double[][] z = new double[minibatch_size][n_hidden];

        double[][] dy;  // �o�͑w�̏o�̓V�O�i���̃�
        double[][] dz;  // �B��w�̏o�̓V�O�i���̃�
        double[][] dx_flatten = new double[minibatch_size][flattened_size];  // ���͑w�̏o�̓V�O�i���̃�  ��"���͑w�̒�"�ł́A���͂Əo�͂������ƍl���Ă��悢��������܂���B

        //�Ō�̃v�[�����O�w���o�ߌ�́A�摜�f�[�^�s��̑傫���𐶐����܂��B
        double[][][][] dx = new double[minibatch_size][n_kernels[n_kernels.length-1]][pooled_sizes[pooled_sizes.length-1][0]][pooled_sizes[pooled_sizes.length-1][1]];

        double[][][][] dc;


        //�~�j�o�b�`�̐��������s
        for (int n = 0; n < minibatch_size; n++) {

            //z_���g���[�j���O�̓��͂ɂ��邽�߁A�����l��x����擾
            double[][][] z_ = x[n].clone();
            //���`�d�F ��ݍ��ݑw + �v�[�����O�w
            for (int i = 0; i < n_kernels.length; i++) {
                z_ = convpool_layers[i].forward(z_, pre_activated_x.get(i)[n], activated_x.get(i)[n]); //z_ ��i��ڂ̃g���[�j���O��̏o�͒l�A�܂�i+1��ڂ̓��͒l�ł�����܂�
                downsampled_x.get(i+1)[n] = z_.clone();
            }

            // ���w�p�[�Z�v�g�����̓��͂ɂ��邽�߁A�g���[�j���O�̏o�͂�1�����֓W�J����B
            double[] x_ = this.flatten(z_);
            flattened_x[n] = x_.clone();

            // �B��w�����`�d������
            z[n] = hidden_layer.forward(x_); //z�͉B��w�̏o�͒l
        }


        //�o�͑w�i���W�X�e�B�b�N��A�j�ɑ΂��āA���`�d�Ƌt�`�d��������i�~�j�o�b�`���g���܂���j
        dy = logistic_layer.train(z, t, minibatch_size, learning_rate); //t�̓e�X�g�f�[�^�ɑ΂���񓚒l�Ady�͉B��w�̋t�`�d�Ŏg���܂�

        // �B��w�ɋt�`�d��������
        dz = hidden_layer.backward(flattened_x, z, dy, logistic_layer.W, minibatch_size, learning_rate);

        // �t�`�d�덷����͑w�֔��f������
        for (int n = 0; n < minibatch_size; n++) {
            for (int i = 0; i < flattened_size; i++) {
                for (int j = 0; j < n_hidden; j++) {
                    dx_flatten[n][i] += hidden_layer.W[j][i] * dz[n][j];
                }
            }
            //�e�~�j�o�b�`�ɑ΂��āA�e�J�[�l���̍s��i�Ō�̃v�[�����O��j�֖߂�
            dx[n] = unflatten(dx_flatten[n]);
        }

        // ��ݍ��ݑw + �v�[�����O�w�֋t�`�d������
        dc = dx.clone();
        //�Ō�̑w����񂵂Ă����̂ŁAi�����炵�Ă������ԂŃ��[�v���܂�
        for (int i = n_kernels.length-1; i >= 0; i--) {
            dc = convpool_layers[i].backward(downsampled_x.get(i), pre_activated_x.get(i), activated_x.get(i), downsampled_x.get(i+1), dc, minibatch_size, learning_rate);
        }
    }

    /*
     * ���w�p�[�Z�v�g�����̓��͂ɂ��邽�߁A�ꎟ���z��֓W�J���ĕԋp���܂��B
     */
    public double[] flatten(double[][][] z) {

        double[] x = new double[flattened_size];

        int index = 0;
        //�Ō�̓����}�b�v�̐��������[�v
        for (int k = 0; k < n_kernels[n_kernels.length-1]; k++) {
            //�Ō�̃_�E���T���v�����O��̔z��̐������A1���擾����
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
        //flatten�ő�����Ă����ۂƓ��l�̏��ԂŁAx�����o���Ă���
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

        //�e�g���[�j���O�ɑ΂���A���������O�ƁA����������L���b�V��
        for (int i = 0; i < n_kernels.length; i++) {
            pre_activated.add(new double[n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
            activated.add(new double[n_kernels[i]][convolved_sizes[i][0]][convolved_sizes[i][1]]);
        }

        // �g���[�j���O�i��ݍ��� + �v�[�����O�j�����`�d������
        double[][][] z = x.clone();
        for (int i = 0; i < n_kernels.length; i++) {
            z = convpool_layers[i].forward(z, pre_activated.get(i), activated.get(i));
        }

        // ���w�p�[�Z�v�g���������`�d������
        return logistic_layer.predict(hidden_layer.forward(this.flatten(z)));
    }



    /*
     * �f�[�^��ǂݍ��݁A�\���𗧂Ă܂�
     * ��ݍ��݃j���[�����l�b�g���[�N�̏o��
     */
    public static void main(String[] args) {

        final Random rng = new Random(123);  // �����̃V�[�h

        //
        // �ϐ��ƒ萔�̐錾
        //

        final int patterns = 10; //�����̃��x����0�`9��10���

        final int train_n = 60000;  //�������炷�Ɛ��x�������������邽�߂��̂܂܂𐄏�
        final int test_n = 10000;

        final int[] image_size = {28, 28}; //���摜�̑傫��
        final int channel = 1;

        int[] n_kernels = {10, 20};
        int[][] kernel_sizes = { {5, 5}, {5, 5} };
        int[][] pool_sizes = { {2, 2}, {2, 2} };

        int n_hidden = 300;
        final int n_out = patterns;

        double[][][][] train_x = new double[train_n][channel][image_size[0]][image_size[1]];
        int[][] train_t = new int[train_n][n_out];

        double[][][][] test_x = new double[test_n][channel][image_size[0]][image_size[1]];
        //ArrayList���g�������̂�Integer�^�ɂ���
        Integer[][] test_t = new Integer[test_n][n_out];
        Integer[][] predicted_t = new Integer[test_n][n_out];


        int epochs = 500; //epochs��܂킵����I��  �e�X�g�ғ��ɂ�10���x�ő��v�ł���
        double learning_rate = 0.001;

        final int minibatch_size = 25;
        int minibatch_n = train_n / minibatch_size;

        double[][][][][] train_x_minibatch = new double[minibatch_n][minibatch_size][channel][image_size[0]][image_size[1]];
        int[][][] train_t_minibatch = new int[minibatch_n][minibatch_size][n_out];
        List<Integer> minibatch_index = new ArrayList<>();
        for (int i = 0; i < train_n; i++) minibatch_index.add(i);
        Collections.shuffle(minibatch_index, rng); //i�ڂ̃f�[�^���ǂ̃~�j�o�b�`�ɓ���邩�A�V���b�t������

        //MNIST�f�[�^�i�o�C�i���j��ǂݍ��݂܂�
        System.out.print("MNIST is loadeding...");
        ReadingMnist read_mnist = new ReadingMnist();
        train_x = read_mnist.readMnistImages(60000, "C:\\<DIRECTORY_PATH>\\train-images.idx3-ubyte");
        train_t = read_mnist.readMnistLabelsInPrm(60000, "C:\\<DIRECTORY_PATH>\\train-labels.idx1-ubyte");
        test_x = read_mnist.readMnistImages(10000, "C:\\<DIRECTORY_PATH>\\t10k-images.idx3-ubyte");
        test_t = read_mnist.readMnistLabelsInObj(10000, "C:\\<DIRECTORY_PATH>\\t10k-labels.idx1-ubyte");
        System.out.println("done.");

        // �~�j�o�b�`�𐶐��i�V���b�t�����ʂ𔽉f�����A�e�~�j�o�b�`�ɐU�蕪����j
        for (int j = 0; j < minibatch_size; j++) {
            for (int i = 0; i < minibatch_n; i++) {
                train_x_minibatch[i][j] = train_x[minibatch_index.get(i * minibatch_size + j)];
                train_t_minibatch[i][j] = train_t[minibatch_index.get(i * minibatch_size + j)];
            }
        }

        // ��ݍ��݃j���[�����l�b�g���[�N�̃��f���𐶐�
        System.out.print("Building the model...");
        ConvolutionalNeuralNetworks classifier = new ConvolutionalNeuralNetworks(image_size, channel, n_kernels, kernel_sizes, pool_sizes, n_hidden, n_out, rng);
        System.out.println("done.");

        // �g���[�j���O�����s
        System.out.print("Training the model...");
        System.out.println();

        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("\titer = " + (epoch + 1) + " / " + epochs); //�����Ȃ�̂ŁA�r���o�߂��o��

            //���������~�j�o�b�`�̐��������s
            for (int batch = 0; batch < minibatch_n; batch++) {
                classifier.train(train_x_minibatch[batch], train_t_minibatch[batch], minibatch_size, learning_rate);
            }
            learning_rate *= 0.99; //�w�K���͒���������
        }
        System.out.println("done.");


        // �e�X�g����
        for (int i = 0; i < test_n; i++) {
            predicted_t[i] = classifier.predict(test_x[i]);
        }


        //
        // ���f���̐��x�����؂���
        //

        int[][] confusion_matrix = new int[patterns][patterns];
        double accuracy = 0.;
        double[] precision = new double[patterns];
        double[] recall = new double[patterns];

        //�ǂ��ɉ��\�z�������݂Ă݂悤
        int[] hits_cnt = new int[10];
        for(int n_i=0; n_i < test_n; n_i++) {
            hits_cnt[Arrays.asList(predicted_t[n_i]).indexOf(1)] += 1;
        }
        for(int i=0; i < patterns; i++) {
            System.out.print(hits_cnt[i] + ", ");
        }
        System.out.println();

        for (int i = 0; i < test_n; i++) {
            int predicted_ = Arrays.asList(predicted_t[i]).indexOf(1); //�������x����Index�ԍ���ԋp�i�e�X�g�f�[�^����\���j
            int actual_ = Arrays.asList(test_t[i]).indexOf(1); //�������x����Index�ԍ���ԋp�i���m�̉񓚁j

            confusion_matrix[actual_][predicted_] += 1; //���ʂ�z��ɉ��Z  �������Ă���΁Ai�s��i���1������܂���
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
