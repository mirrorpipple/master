package spell.hoimi.command.blogic;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import spell.hoimi.batch.blogic.*;
import spell.hoimi.statistics.blogic.*;

public class Commands {

    public static void main(String[] args){

        String str = null;
        boolean checkFlg = false;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        System.out.println("　　　 ∧∧　　／￣￣￣￣￣");
        System.out.println("　　　(,,ﾟДﾟ)＜　コマンドを選ぶんだ");
        System.out.println("　　 (|　　|)　＼＿＿＿＿＿");
        System.out.println("　　～|　　|");
        System.out.println("　,,　　し`J");
        System.out.println("####################");
        System.out.println("1:　標本データ読み込み");
        System.out.println("2:　シミュレーション");
        System.out.println("####################");
        System.out.println("コマンド？");

        while(!checkFlg){
            try{
                str = br.readLine();
                if (Integer.parseInt(str) ==1){
                    checkFlg = true;
                } else if (Integer.parseInt(str) ==2){
                    checkFlg = true;
                }
            }catch(IOException e){
                System.out.println("入力可能な値ではない:" + e.getMessage());
            }
        }


        if (Integer.parseInt(str) ==1){
            ChartImport chartImport = new ChartImport();
            chartImport.execute();
        } else if (Integer.parseInt(str) ==2){
            ProphecyRule prophecyRule = new ProphecyRule();
            prophecyRule.execute();
        }

    }
}
