# -*- coding: utf-8 -*-

import networkx as nx
import numpy
import itertools
import csv
import sys

def simrank(G, max_iter, c_uu, c_ii, c_uc, c_cc, c_other):
    nodes = G.nodes()
    # �m�[�h��index�𐶐��B
    nodes_i = {k: v for(k, v) in [(nodes[i], i) for i in range(0, len(nodes))]}
    # neighbors���Ɍv�Z���ăL���b�V�����܂��B
    neighbors_i = {k: v for(k, v) in [(nodes[i], tuple(G.neighbors(nodes[i]))) for i in range(0, len(nodes))]}
    # simrank�v�Z�p�ɍs��������B
    sim_prev = numpy.zeros(len(nodes))
    sim = numpy.identity(len(nodes))

    # G�m�[�h���Aitem�m�[�h��user,ct�m�[�h�֕������܂��B���[�v�Z�k�p�ł��B
    Gi = nx.Graph()
    Guc = nx.Graph()
    for j in nodes:
        j_mod = j % 10
        if j_mod in [1,3]:
            Guc.add_node(j)
        else:
            Gi.add_node(j)
    nodes_uc = Guc.nodes()
    nodes_ii = Gi.nodes()

    # 1���[�v���ɂǂꂾ���i�񂾂��\�����܂��B�i�s�x���C�ɂȂ�Ƃ��ɁB
    # max_iter��A���[�v���J��Ԃ��܂��B
    print('times:',max_iter)
    for i in range(max_iter):

        print('loop:',i,'start')
        sim_prev = numpy.copy(sim)

        for i in range(0, len(nodes)):
            sim_prev[i][i] = 1
        # simrank�̌v�Z�J�n uc : uc �ł��B
        for u, v in itertools.product(nodes_uc, nodes_uc):
            u_ns, v_ns = neighbors_i[u], neighbors_i[v]
            s_uv = sum([sim_prev[nodes_i[u_n]][nodes_i[v_n]] for u_n, v_n in itertools.product(u_ns, v_ns)])

            # r(�W��)�̒l���A�G�b�W�̑g�ݍ��킹�ɂ���ĕύX���܂��B
            roots = (u %10) * (v %10)
            # user*user=1, item*item=4, user*ct=3, ct*ct=9 �ɂȂ�A�g�ݍ��킹����ӂɔ��ʉ\�B
            # ��`����͏o�Ȃ��g�ݍ��킹���A���[�v��Ŏ��s���Ă��܂��B
            # �~�����g�ݍ��킹�ւ͒l�Ƃ��ĉe���͂��Ȃ��͂��ł�(���0)...
            # else�̒l�͂��̑��̏ꍇ�ɓ���l�ł��B
            if roots in [1]:
                r = c_uu
            elif roots in [3]:
                r = c_uc
            elif roots in [9]:
                r = c_cc
            else:
                r = c_other

            sim[nodes_i[u]][nodes_i[v]] = (r * s_uv) / (len(u_ns) * len(v_ns))

        # simrank�̌v�Z�J�n  �c��́Ai:i �����܂��B
        # c_other���l�����Ȃ��ꍇ�ł��B�l������ꍇ�̓R�����g�A�E�g���܂��B(�����p�Fc_mark)
        r = c_ii
        
        # ���[�v�J�n
        for u, v in itertools.product(nodes_ii, nodes_ii):
            u_ns, v_ns = neighbors_i[u], neighbors_i[v]
            s_uv = sum([sim_prev[nodes_i[u_n]][nodes_i[v_n]] for u_n, v_n in itertools.product(u_ns, v_ns)])

            # c_other ���l������ꍇ�́A�ȉ����g���܂��B�R�����g�A�E�g�������܂��B(�����p�Fc_mark)
            # roots = (u %10) * (v %10)
            # if roots in [4]:
            #    r = c_ii
            # else:
            #    r = c_other

            sim[nodes_i[u]][nodes_i[v]] = (r * s_uv) / (len(u_ns) * len(v_ns))


    # ���ʂ�ԋp����  print(sim)�̓e�X�g�\���p�ł��B
    for i in range(0, len(nodes)):
        sim[i][i] = 1
    print(sim)
    return sim

def sort(result, array, stop_value):
    # �s��̌��𐶐�  lists_r�֏�ʂ�id���Alists_v��simrank�̒l�����܂��B
    lists_r = numpy.zeros([len(array),6])
    lists_v = numpy.zeros([len(array),6])
    for i in range(len(array)):
        #���g��id��0��ڂɑ��
        lists_r[i][0] = array[i]
        lists_v[i][0] = array[i]
    # ���[�U�ƃJ�e�S���̑g�ݍ��킹(��=3)��0�������ă��[�U�ւ̏�ʌ��o�����
    for u, v in itertools.product(range(len(array)),range(len(array))):
        check = (array[u] % 10)*(array[v] % 10)
        if check in [3]:
            result[u][v] = 0
    # �ő�l���Z�o
    for i in range(len(array)):
        # simrank�̎��g�̃Z����0�������A�ő�l�������珜�O���܂�
        result[i][i] = 0
        for j in range(1,6):
            # �ő�l��index�l��num�֑��
            num = numpy.argmax(result[i])
            
            # �ő�l���Ⴗ����ꍇ�͒��f���Ď��̍s��
            #if result[i][num] < stop_value:
            #    break
            
            # max��index�l���Asimrank��ʂ���j�Ԗڂ�id�Ƃ��đ��
            lists_r[i][j] = array[num]
            lists_v[i][j] = result[i][num]
            # �ő�l��0�������A���̎��̍ő�l�����[�v�ŒT��
            result[i][num] = 0
            
    return lists_r, lists_v



#ver.1.4, ver1.3����ύX�_�Fif���̍팸�ɂ��v�Z���x�̏㏸
#***********************������************************#
#
#���O�����\�ł��Ȃ��f�[�^�ɑ΂��āA
#  �d�ݕt�����@���A�����W����simrank�����������������̂ł��B
#
#
#
�͂��߂ɁF
#
#�����܂�method�����A����������s����
#�t�@�C�������ׂ��������̐ݒ�͌����o���Ă��������B
#
#�eid�̈�̈ʂ́A���L�̂悤�ɂȂ��Ă�����̂Ƃ��Ă��܂��B
#user_id:1, item:2, category:3
#���̏ꍇ�A�e�g�����ɂ���Đς��قȂ�̂ŁA
#�^����ꂽ�G�b�W�̑g�ݍ��킹�͂���Ŕ��ʂ��Ă��܂��B
#
#�g�����F
#�����t�H���_�� 'edge.csv' �Ƃ������O(�f�t�H���g)�ŁA
#�G�b�W���X�g��u���Ă�����s���Ă�������
#*******************�ݒ肷��l************************#
#simrank�ߎ��̃��[�v��
max_iter = 5
#user * user �̏ꍇ�̌W�� C
c_uu = 0.8
#item * item �̏ꍇ�̌W�� C
c_ii = 0.7
#user * category �̏ꍇ�̌W�� C
c_uc = 0.6
#category * category �̏ꍇ�̌W�� C
c_cc = 0.5
#����ȊO�̑g�ݍ��킹 �̏ꍇ�̌W�� C
#�O�̂��߁B�^����ꂽ�G�b�W�̑g�ݍ��킹��
#���������0�ł��~�������ʂւ͖��Ȃ��͂��ł��B
#c_other���g���ꍇ(!=0�̏ꍇ)�́A�R�����g�A�E�g�̐������K�v�ł��B�uc_mark�v�Ō������Ă��������B
c_other = 0.0
#simrank�̒l�����ȉ��̏ꍇ�A�����ŏ����𒆒f���܂��B
#0�ɋ߂��Ȃ��Ă���ۂɁA���[�v�񐔂����炷�p�ł��B
stop_value = 0.00001
#********************�����܂�**************************#



#�G�b�W���X�g�𐶐�
G = nx.Graph()
G = nx.read_edgelist("edge.csv", nodetype=int, delimiter=',')
#�Y���m�[�h�̃J��������ۑ�
#�O���t��̔ԍ��ƁA���̔ԍ��̓ˍ����Ɏg�p
array = G.nodes()

#�ˍ����p���X�g��O�̂��ߏo��
f = open('list.csv', 'w')
writecsv = csv.writer(f,lineterminator='\n')
for i in range(len(G.nodes())):
    writecsv.writerow([array[i]])
f.close()

#simrank���v�Z
result = simrank(G, max_iter, c_uu, c_ii, c_uc, c_cc, c_other)

#�e�X�g�p�ɐ�������simrank���o�́A�S�f�[�^���͗e�ʑ�̂��ߍ폜���E
#numpy.savetxt("result.csv", result, fmt="%0.5f",delimiter=",")

#�l�̍����g�b�v�T�������Ă��܂��A������simrank < 0.00001 �͏ȗ�
tbl_r, tbl_v = sort(result, array, stop_value)

#sims_r/v.csv �Ƃ������O�ŁA���5��R�t����csv���o�͂��܂��B�Y������0������܂�
numpy.savetxt("sims_r.csv", tbl_r, fmt="%0.f",delimiter=",")
numpy.savetxt("sims_v.csv", tbl_v, fmt="%0.5f",delimiter=",")
