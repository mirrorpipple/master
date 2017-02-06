package spell.hoimi.batch.blogic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

public class ChartImport {

    public void execute(){
        //#######################取り込み用定義#############################
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
        //#######################取り込み用定義#############################
        //#############################SQL設定##############################
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
        //#############################SQL設定##############################



        //WORKへデータを全投入する。
        try{
            ps = db.prepareStatement(sql1);
            BufferedReader br = new BufferedReader( new FileReader(batchUrl+fileName));
            ChartImport chartImport = new ChartImport();

            while( (line = br.readLine()) != null ) {
                // カンマで分割したString配列を得る
                String records[] = line.split( "," );
                compwork = (Integer.parseInt(records[1].substring(12,14))/compress) * compress;
                if (compwork != bfcomp) {
                    bfcomp = compwork;
                    //ここで始値が設定されていたら、INSERTを実行する。
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
                    //compwork刻みで考えて、同じ時間帯。最大値と最小値の更新チェックをかける。
                    dummyLow = new BigDecimal(records[2]);
                    dummyHigh = new BigDecimal(records[3]);
                    if(dummyHigh.compareTo(highRate) >=0){
                        highRate = dummyHigh;
                    }
                    if(lowRate.compareTo(dummyLow) >=0){
                        lowRate = dummyLow;
                    }
                }


                //予約SQLへ値を代入する。
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

            //WORKテーブルから、集計結果を保存テーブルへINSERTする。
            //ps = db.prepareStatement(sql2);
            //ps.executeUpdate();
            //db.commit();

            //WORKテーブルのレコードを全削除する。
            //ps = db.prepareStatement(sql3);
            //ps.executeUpdate();
            //db.commit();
            System.out.println("データインポートが完了しました");
            ps.close();
            db.close();


        } catch( IOException e )  {
            System.out.println( "入出力エラーがありました" );
            e.printStackTrace();
        } catch( NumberFormatException e )  {
            System.out.println( "フォーマットエラーがありました" );
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 時刻データを、所定の計測期間で圧縮する。
     * @param String 時刻
     * @return Timestamp 圧縮時刻
     */
    public Timestamp TimeCompress(String timeStr, int compress){

        String result = null;
        int compwork = 0;

        //まず、分レベルのint型へ整理。次で0埋めString。
        compwork = (Integer.parseInt(timeStr.substring(12,14))/compress) * compress;
        result = timeStr.substring(0, 4) +  "-" + timeStr.substring(4,6) + "-" + timeStr.substring(6,12) +
                 String.format("%02d", compwork) + ":00";
        return Timestamp.valueOf(result);

    }

}
