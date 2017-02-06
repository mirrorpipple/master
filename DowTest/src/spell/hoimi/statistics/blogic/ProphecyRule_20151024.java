package spell.hoimi.statistics.blogic;


import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

import spell.hoimi.statistics.dto.ProphecyInput;


public class ProphecyRule_20151024 {

    public void execute(){
        //#######################STARTTIMEの定義#############################
        Timestamp firstTime = Timestamp.valueOf("2015-03-01 00:00:00");
        String currency = "USD";
        String pairName = "USD/JPY";
        int funds = 2000000;
        Timestamp startTime = firstTime;
        Calendar endTime = Calendar.getInstance();
        BigDecimal spread = null;
        BigDecimal err = null;
        //#######################STARTTIMEの定義#############################

        //#######################計算用の定義#############################
        ProphecyInput input = new ProphecyInput();
        //#######################計算用の定義#############################

        //#############################SQL設定##############################
        Connection db = null;
        PreparedStatement ps = null;
        String sql1 = "SELECT SPREAD,ERR" +
                      " FROM T_SPREAD_MST" +
                      " WHERE CURRENCY = ?";
        String sql2 = "SELECT PAIRNAME,CHECKTIME,STARTRATE,ENDRATE" +
                      " FROM T_HISTDATA" +
                      " WHERE PAIRNAME = ?" +
         //一旦保留             " CHECKTIME BETWEEN ? and ?" +
                      " ORDER BY CHECKTIME";

        String url = "jdbc:h2:tcp://localhost/~/terasoluna";
        String usr = "sa";
        String pwd = "";
        try {
           db = DriverManager.getConnection(url, usr, pwd);
           db.setAutoCommit(false);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        //#############################SQL設定##############################

        try{
            ps = db.prepareStatement(sql1);
            ResultSet rs = null;
            //スプレッドを取得、テストはドルで
            ps.setString(1, currency);
            rs = ps.executeQuery();
            System.out.println(rs);
            rs.next();
            spread = rs.getBigDecimal("SPREAD");
            err = rs.getBigDecimal("ERR");

            //本来ならばこの辺りはループ処理
            //endtimeを設定する。
            endTime.setTimeInMillis(startTime.getTime());
            endTime.add(Calendar.MONTH, 1);

            //予約SQLへ値を代入する。
            System.out.println(startTime);
            System.out.println(new Timestamp(endTime.getTimeInMillis()));
            ps = db.prepareStatement(sql2);
            ps.setString(1, pairName);
            //ps.setTimestamp(2,startTime);
            //ps.setTimestamp(3,new Timestamp(endTime.getTimeInMillis()));
            rs = ps.executeQuery();


            //prochetを呼び出す。
            Prophet(db, input, rs, funds, err);

            //WORKテーブルから、集計結果を保存テーブルへINSERTする。
//            ps = db.prepareStatement(sql2);
//            ps.executeUpdate();
//            db.commit();
            System.out.println("測定が完了しました");
            ps.close();
            db.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void Prophet(Connection db, ProphecyInput input, ResultSet rs, int funds, BigDecimal err){

        int cnt = 0;
        int same = 0;
        int vlly_koshin = 0;
        int mt_koshin = 0;
        try {
            while(rs.next()){
                cnt ++;
                input.setThisHigh(rs.getBigDecimal("STARTRATE"));
                input.setThisLow(rs.getBigDecimal("ENDRATE"));
                input.setThisTime(rs.getTimestamp("CHECKTIME"));

                if (!input.isSetupFlg()) {
                    input.setBfStart(rs.getBigDecimal("STARTRATE"));
                    input.setBfEnd(rs.getBigDecimal("ENDRATE"));
                    input.setBfHigh(input.getThisHigh());
                    input.setBfLow(input.getThisLow());
                    input.setBfTrend(1);
                    input.setMtStart(rs.getBigDecimal("STARTRATE"));
                    input.setMtEnd(rs.getBigDecimal("ENDRATE"));
                    input.setMtHigh(input.getThisHigh());
                    input.setMtLow(input.getThisLow());
                    input.setMtTrend(1);
                    input.setVllyStart(rs.getBigDecimal("STARTRATE"));
                    input.setVllyEnd(rs.getBigDecimal("ENDRATE"));
                    input.setVllyHigh(input.getThisHigh());
                    input.setVllyLow(input.getThisLow());
                    input.setVllyTrend(1);
                    input.setSetupFlg(true);
                    input.setPairName("USD/JPY");
                    input.setJpyFunds(funds);
                }

                //トレンドチェック
                if (input.getThisHigh().compareTo((input.getBfHigh().add(err))) >= 0){
                    //レート上昇のとき
                    input.setThisTrend(1);
                    //前回トレンドと比較し、凹凸をチェックする。
                    if (input.getThisTrend() != input.getBfTrend()){
                        //谷ができたとき、前谷vllyの値と比較する。
                        if (input.getBfHigh().compareTo((input.getVllyHigh())) >= 0){
                            //上昇が発生：賭けてないなら次でベットする。賭けてたらステイ。

                            if (!input.isBetFlg()){
                                //賭けていなかったとき。ここで賭ける。
                                //値を持たせて別methodで実行させたいときに、setter,getter役に立つのか。
                                input.setBetFlg(true);
                                BetTheGame(db, input, 0);
                            }


                        } else {
                            //下降が発生：賭けていたら降りる。
                            //今は上昇でしか賭けないので、谷の値だけ更新でおｋ
                        }
                        //谷の値の更新
                        input.setVllyStart(input.getBfStart());
                        input.setVllyEnd(input.getBfEnd());
                        input.setVllyHigh(input.getBfHigh());
                        input.setVllyLow(input.getBfLow());
                        vlly_koshin++;
                    }
                    //レート上昇が続いた場合はそのまま次へ
                    input.setBfTrend(1);

                } else if (input.getBfHigh().compareTo((input.getThisHigh().add(err))) >= 0){
                    //レート低下のとき
                    input.setThisTrend(0);
                    //まず始めに損切りチェック
                    if(input.isBetFlg() && input.getVllyHigh().compareTo(input.getThisHigh()) >=0){
                        //下降の閾値超え、かつ賭けていた場合は無条件で損切り
                            input.setBetFlg(false);
                            BetTheGame(db, input, 2);
                    }
                        //前回トレンドと比較し、凹凸をチェックする。
                        if (input.getThisTrend() != input.getBfTrend()){
                            //山ができたとき、前山Mtの値と比較する。
                            if(input.getBfHigh().compareTo(input.getMtHigh()) >=0){
                                //上昇が発生：ベットしている場合も含めてステイ
                            } else {
                                //下降が発生：ベットしている場合は利確
                                if(input.isBetFlg()){
                                    //利確
                                    input.setBetFlg(false);
                                    BetTheGame(db, input, 1);
                                }
                            }
                            //山の値の更新
                            input.setMtStart(input.getBfStart());
                            input.setMtEnd(input.getBfEnd());
                            input.setMtHigh(input.getBfHigh());
                            input.setMtLow(input.getBfLow());
                            mt_koshin++;
                        }
                        //レート下降が続いた場合はそのまま次へ
                        input.setBfTrend(0);

                } else {
                    //揺れが誤差の範囲のとき
                    input.setThisTrend(input.getBfTrend());
                    same++;
                }
                //before値を更新
                input.setBfHigh(input.getThisHigh());
                input.setBfLow(input.getThisLow());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(cnt);
        System.out.println(same);
        System.out.println(vlly_koshin);
        System.out.println(mt_koshin);
    }


    public void BetTheGame(Connection db, ProphecyInput input, int tacticsCode) throws SQLException{

      //#############################SQL設定##############################
        String sql1 = "insert into T_TRADE_HIST values(?,?,?,?,?,?)";
      //#############################SQL設定##############################

        BigDecimal calcs;
        PreparedStatement ps = null;
        ps = db.prepareStatement(sql1);
        ps.setString(1, input.getPairName());
        ps.setTimestamp(2,input.getThisTime());
        ps.setInt(3, tacticsCode);
        if(input.isBetFlg()){
            //betFlg true：買いに走るとき
            calcs = BigDecimal.valueOf(input.getJpyFunds());
            calcs = calcs.divide(input.getThisHigh(), 2, BigDecimal.ROUND_HALF_UP);
            ps.setBigDecimal(4,input.getThisHigh());
            ps.setInt(5, 0);
            ps.setInt(6,  calcs.intValue());
            input.setForeignFunds(calcs.intValue());
            input.setJpyFunds(0);
        } else {
            //betFlg false：売りに走るとき
            calcs = BigDecimal.valueOf(input.getForeignFunds());
            calcs = calcs.multiply(input.getThisLow());
            ps.setBigDecimal(4,input.getThisLow());
            ps.setInt(5, calcs.intValue());
            ps.setInt(6, 0);
            input.setForeignFunds(0);
            input.setJpyFunds(calcs.intValue());
        }

        ps.executeUpdate();
        db.commit();
    }

}
