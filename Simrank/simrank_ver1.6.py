# -*- coding: utf-8 -*-

import networkx as nx
import numpy
import itertools
import csv
import sys

def simrank(G, max_iter, c_uu, c_ii, c_uc, c_cc, c_other):
    nodes = G.nodes()
    # ノードのindexを生成。
    nodes_i = {k: v for(k, v) in [(nodes[i], i) for i in range(0, len(nodes))]}
    # neighborsを先に計算してキャッシュします。
    neighbors_i = {k: v for(k, v) in [(nodes[i], tuple(G.neighbors(nodes[i]))) for i in range(0, len(nodes))]}
    # simrank計算用に行列を準備。
    sim_prev = numpy.zeros(len(nodes))
    sim = numpy.identity(len(nodes))

    # Gノードを、itemノードとuser,ctノードへ分割します。ループ短縮用です。
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

    # 1ループ毎にどれだけ進んだか表示します。進行度が気になるときに。
    # max_iter回、ループを繰り返します。
    print('times:',max_iter)
    for i in range(max_iter):

        print('loop:',i,'start')
        sim_prev = numpy.copy(sim)

        for i in range(0, len(nodes)):
            sim_prev[i][i] = 1
        # simrankの計算開始 uc : uc です。
        for u, v in itertools.product(nodes_uc, nodes_uc):
            u_ns, v_ns = neighbors_i[u], neighbors_i[v]
            s_uv = sum([sim_prev[nodes_i[u_n]][nodes_i[v_n]] for u_n, v_n in itertools.product(u_ns, v_ns)])

            # r(係数)の値を、エッジの組み合わせによって変更します。
            roots = (u %10) * (v %10)
            # user*user=1, item*item=4, user*ct=3, ct*ct=9 になり、組み合わせを一意に判別可能。
            # 定義式上は出ない組み合わせも、ループ上で実行しています。
            # 欲しい組み合わせへは値として影響はしないはずです(常に0)...
            # elseの値はその他の場合に入る値です。
            if roots in [1]:
                r = c_uu
            elif roots in [3]:
                r = c_uc
            elif roots in [9]:
                r = c_cc
            else:
                r = c_other

            sim[nodes_i[u]][nodes_i[v]] = (r * s_uv) / (len(u_ns) * len(v_ns))

        # simrankの計算開始  残りの、i:i をします。
        # c_otherを考慮しない場合です。考慮する場合はコメントアウトします。(検索用：c_mark)
        r = c_ii
        
        # ループ開始
        for u, v in itertools.product(nodes_ii, nodes_ii):
            u_ns, v_ns = neighbors_i[u], neighbors_i[v]
            s_uv = sum([sim_prev[nodes_i[u_n]][nodes_i[v_n]] for u_n, v_n in itertools.product(u_ns, v_ns)])

            # c_other を考慮する場合は、以下を使います。コメントアウトを消します。(検索用：c_mark)
            # roots = (u %10) * (v %10)
            # if roots in [4]:
            #    r = c_ii
            # else:
            #    r = c_other

            sim[nodes_i[u]][nodes_i[v]] = (r * s_uv) / (len(u_ns) * len(v_ns))


    # 結果を返却する  print(sim)はテスト表示用です。
    for i in range(0, len(nodes)):
        sim[i][i] = 1
    print(sim)
    return sim

def sort(result, array, stop_value):
    # 行列の元を生成  lists_rへ上位のidを、lists_vへsimrankの値を入れます。
    lists_r = numpy.zeros([len(array),6])
    lists_v = numpy.zeros([len(array),6])
    for i in range(len(array)):
        #自身のidを0列目に代入
        lists_r[i][0] = array[i]
        lists_v[i][0] = array[i]
    # ユーザとカテゴリの組み合わせ(積=3)へ0を代入してユーザへの上位検出を回避
    for u, v in itertools.product(range(len(array)),range(len(array))):
        check = (array[u] % 10)*(array[v] % 10)
        if check in [3]:
            result[u][v] = 0
    # 最大値を算出
    for i in range(len(array)):
        # simrankの自身のセルに0を代入し、最大値検索から除外します
        result[i][i] = 0
        for j in range(1,6):
            # 最大値のindex値をnumへ代入
            num = numpy.argmax(result[i])
            
            # 最大値が低すぎる場合は中断して次の行へ
            #if result[i][num] < stop_value:
            #    break
            
            # maxのindex値を、simrank上位からj番目のidとして代入
            lists_r[i][j] = array[num]
            lists_v[i][j] = result[i][num]
            # 最大値に0を代入し、その次の最大値をループで探す
            result[i][num] = 0
            
    return lists_r, lists_v



#ver.1.4, ver1.3から変更点：if文の削減による計算速度の上昇
#***********************説明書************************#
#
#※外部公表できないデータに対して、
#  重み付け方法をアレンジしてsimrank処理を実装したものです。
#
#
#
はじめに：
#
#ここまでmethod準備、ここから実行部分
#ファイル名等細かい部分の設定は見つけ出してください。
#
#各idの一の位は、下記のようになっているものとしています。
#user_id:1, item:2, category:3
#この場合、各組合せによって積が異なるので、
#与えられたエッジの組み合わせはそれで判別しています。
#
#使い方：
#同じフォルダに 'edge.csv' という名前(デフォルト)で、
#エッジリストを置いてから実行してください
#*******************設定する値************************#
#simrank近似のループ回数
max_iter = 5
#user * user の場合の係数 C
c_uu = 0.8
#item * item の場合の係数 C
c_ii = 0.7
#user * category の場合の係数 C
c_uc = 0.6
#category * category の場合の係数 C
c_cc = 0.5
#それ以外の組み合わせ の場合の係数 C
#念のため。与えられたエッジの組み合わせが
#正しければ0でも欲しい結果へは問題ないはずです。
#c_otherを使う場合(!=0の場合)は、コメントアウトの整理が必要です。「c_mark」で検索してください。
c_other = 0.0
#simrankの値が一定以下の場合、そこで処理を中断します。
#0に近くなっている際に、ループ回数を減らす用です。
stop_value = 0.00001
#********************ここまで**************************#



#エッジリストを生成
G = nx.Graph()
G = nx.read_edgelist("edge.csv", nodetype=int, delimiter=',')
#該当ノードのカラム名を保存
#グラフ上の番号と、元の番号の突合せに使用
array = G.nodes()

#突合せ用リストを念のため出力
f = open('list.csv', 'w')
writecsv = csv.writer(f,lineterminator='\n')
for i in range(len(G.nodes())):
    writecsv.writerow([array[i]])
f.close()

#simrankを計算
result = simrank(G, max_iter, c_uu, c_ii, c_uc, c_cc, c_other)

#テスト用に生成したsimrankを出力、全データ時は容量大のため削除推薦
#numpy.savetxt("result.csv", result, fmt="%0.5f",delimiter=",")

#値の高いトップ５を持ってきます、ただしsimrank < 0.00001 は省略
tbl_r, tbl_v = sort(result, array, stop_value)

#sims_r/v.csv という名前で、上位5を紐付けたcsvを出力します。該当無は0が入ります
numpy.savetxt("sims_r.csv", tbl_r, fmt="%0.f",delimiter=",")
numpy.savetxt("sims_v.csv", tbl_v, fmt="%0.5f",delimiter=",")
