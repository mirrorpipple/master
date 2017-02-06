package spell.hoimi.batch.blogic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class ChartImport {

    public void execute(){
        //#######################��荞�ݗp��`#############################
        String batchUrl = "C:\\eclipse\\behomazun\\behoma\\hozonpot\\";
        String fileName = "pepperstone_historical.dat";
        String line;
        BigDecimal highRate = BigDecimal.valueOf(0.00);
        BigDecimal lowRate = BigDecimal.valueOf(999.00);
        BigDecimal dummyHigh;
        BigDecimal dummyLow;
        boolean startFlg = false;
        int compwork = 0;
        int bfcomp = 99;
        int compress = 15;
        //#######################��荞�ݗp��`#############################
        //#############################SQL�ݒ�##############################
        Connection db = null;
        PreparedStatement ps = null;
        String sql1 = "insert into T_HISTDATA values(?,?,?,?)";
        //String sql2 = "INSERT INTO T_HISTDATA" +
        //              " SELECT PAIRNAME, CHECKTIME, STARTRATE, ENDRATE" +
        //             " FROM T_IMPORT_WORK" +
        //              " GROUP BY PAIRNAME,CHECKTIME";
        //String sql3 = "delete FROM T_IMPORT_WORK";
        String url = "jdbc:h2:tcp://localhost/~/terasoluna";
        String usr = "sa";
        String pwd = "";
        int i = 1;
        try {
           db = DriverManager.getConnection(url, usr, pwd);
           db.setAutoCommit(false);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        //#############################SQL�ݒ�##############################



        //WORK�փf�[�^��S��������B
        try{
            ps = db.prepareStatement(sql1);
            BufferedReader br = new BufferedReader( new FileReader(batchUrl+fileName));
            ChartImport chartImport = new ChartImport();

            while( (line = br.readLine()) != null ) {
                // �J���}�ŕ�������String�z��𓾂�
                String records[] = line.split( "," );
                compwork = (Integer.parseInt(records[1].substring(12,14))/compress) * compress;
                if (compwork != bfcomp) {
                    bfcomp = compwork;
                    //�����Ŏn�l���ݒ肳��Ă�����AINSERT�����s����B
                    if (startFlg){
                        ps.executeUpdate();
                        highRate = BigDecimal.valueOf(0.00);
                        lowRate = BigDecimal.valueOf(999.00);
                    }else{
                        startFlg = true;
                        highRate = new BigDecimal(records[3]);
                        lowRate = new BigDecimal(records[2]);
                    }

                } else {
                    //compwork���݂ōl���āA�������ԑсB�ő�l�ƍŏ��l�̍X�V�`�F�b�N��������B
                    dummyLow = new BigDecimal(records[2]);
                    dummyHigh = new BigDecimal(records[3]);
                    if(dummyHigh.compareTo(highRate) >=0){
                        highRate = dummyHigh;
                    }
                    if(lowRate.compareTo(dummyLow) >=0){
                        lowRate = dummyLow;
                    }
                }


                //�\��SQL�֒l��������B
                ps.setString(1,records[0]);
                ps.setTimestamp(2,chartImport.TimeCompress(records[1], compress));
                ps.setBigDecimal(3, highRate);
                ps.setBigDecimal(4, lowRate);
                i++;
                if (i % 10000 == 0){
                    db.commit();
                }
            }
            ps.executeUpdate();
            db.commit();
            br.close();

            //WORK�e�[�u������A�W�v���ʂ�ۑ��e�[�u����INSERT����B
            //ps = db.prepareStatement(sql2);
            //ps.executeUpdate();
            //db.commit();

            //WORK�e�[�u���̃��R�[�h��S�폜����B
            //ps = db.prepareStatement(sql3);
            //ps.executeUpdate();
            //db.commit();
            System.out.println("�f�[�^�C���|�[�g���������܂���");
            ps.close();
            db.close();


        } catch( IOException e )  {
            System.out.println( "���o�̓G���[������܂���" );
            e.printStackTrace();
        } catch( NumberFormatException e )  {
            System.out.println( "�t�H�[�}�b�g�G���[������܂���" );
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * �����f�[�^���A����̌v�����Ԃň��k����B
     * @param String ����
     * @return Timestamp ���k����
     */
    public Timestamp TimeCompress(String timeStr, int compress){

        String result = null;
        int compwork = 0;

        //�܂��A�����x����int�^�֐����B����0����String�B
        compwork = (Integer.parseInt(timeStr.substring(12,14))/compress) * compress;
        result = timeStr.substring(0, 4) +  "-" + timeStr.substring(4,6) + "-" + timeStr.substring(6,12) +
                 String.format("%02d", compwork) + ":00";
        return Timestamp.valueOf(result);

    }

}
