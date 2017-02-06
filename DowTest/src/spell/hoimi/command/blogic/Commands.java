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


        System.out.println("�@�@�@ �ȁȁ@�@�^�P�P�P�P�P");
        System.out.println("�@�@�@(,,߄D�)���@�R�}���h��I�Ԃ�");
        System.out.println("�@�@ (|�@�@|)�@�_�Q�Q�Q�Q�Q");
        System.out.println("�@�@�`|�@�@|");
        System.out.println("�@,,�@�@��`J");
        System.out.println("####################");
        System.out.println("1:�@�W�{�f�[�^�ǂݍ���");
        System.out.println("2:�@�V�~�����[�V����");
        System.out.println("####################");
        System.out.println("�R�}���h�H");

        while(!checkFlg){
            try{
                str = br.readLine();
                if (Integer.parseInt(str) ==1){
                    checkFlg = true;
                } else if (Integer.parseInt(str) ==2){
                    checkFlg = true;
                }
            }catch(IOException e){
                System.out.println("���͉\�Ȓl�ł͂Ȃ�:" + e.getMessage());
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
