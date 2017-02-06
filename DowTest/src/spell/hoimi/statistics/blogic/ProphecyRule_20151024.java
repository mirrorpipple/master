package spell.hoimi.statistics.blogic;


import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;

import spell.hoimi.statistics.dto.ProphecyInput;


public class ProphecyRule_20151024 {

    public void execute(){
        //#######################STARTTIME�̒�`#############################
        Timestamp firstTime = Timestamp.valueOf("2015-03-01 00:00:00");
        String currency = "USD";
        String pairName = "USD/JPY";
        int funds = 2000000;
        Timestamp startTime = firstTime;
        Calendar endTime = Calendar.getInstance();
        BigDecimal spread = null;
        BigDecimal err = null;
        //#######################STARTTIME�̒�`#############################

        //#######################�v�Z�p�̒�`#############################
        ProphecyInput input = new ProphecyInput();
        //#######################�v�Z�p�̒�`#############################

        //#############################SQL�ݒ�##############################
        Connection db = null;
        PreparedStatement ps = null;
        String sql1 = "SELECT SPREAD,ERR" +
                      " FROM T_SPREAD_MST" +
                      " WHERE CURRENCY = ?";
        String sql2 = "SELECT PAIRNAME,CHECKTIME,STARTRATE,ENDRATE" +
                      " FROM T_HISTDATA" +
                      " WHERE PAIRNAME = ?" +
         //��U�ۗ�             " CHECKTIME BETWEEN ? and ?" +
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
        //#############################SQL�ݒ�##############################

        try{
            ps = db.prepareStatement(sql1);
            ResultSet rs = null;
            //�X�v���b�h���擾�A�e�X�g�̓h����
            ps.setString(1, currency);
            rs = ps.executeQuery();
            System.out.println(rs);
            rs.next();
            spread = rs.getBigDecimal("SPREAD");
            err = rs.getBigDecimal("ERR");

            //�{���Ȃ�΂��̕ӂ�̓��[�v����
            //endtime��ݒ肷��B
            endTime.setTimeInMillis(startTime.getTime());
            endTime.add(Calendar.MONTH, 1);

            //�\��SQL�֒l��������B
            System.out.println(startTime);
            System.out.println(new Timestamp(endTime.getTimeInMillis()));
            ps = db.prepareStatement(sql2);
            ps.setString(1, pairName);
            //ps.setTimestamp(2,startTime);
            //ps.setTimestamp(3,new Timestamp(endTime.getTimeInMillis()));
            rs = ps.executeQuery();


            //prochet���Ăяo���B
            Prophet(db, input, rs, funds, err);

            //WORK�e�[�u������A�W�v���ʂ�ۑ��e�[�u����INSERT����B
//            ps = db.prepareStatement(sql2);
//            ps.executeUpdate();
//            db.commit();
            System.out.println("���肪�������܂���");
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

                //�g�����h�`�F�b�N
                if (input.getThisHigh().compareTo((input.getBfHigh().add(err))) >= 0){
                    //���[�g�㏸�̂Ƃ�
                    input.setThisTrend(1);
                    //�O��g�����h�Ɣ�r���A���ʂ��`�F�b�N����B
                    if (input.getThisTrend() != input.getBfTrend()){
                        //�J���ł����Ƃ��A�O�Jvlly�̒l�Ɣ�r����B
                        if (input.getBfHigh().compareTo((input.getVllyHigh())) >= 0){
                            //�㏸�������F�q���ĂȂ��Ȃ玟�Ńx�b�g����B�q���Ă���X�e�C�B

                            if (!input.isBetFlg()){
                                //�q���Ă��Ȃ������Ƃ��B�����œq����B
                                //�l���������ĕ�method�Ŏ��s���������Ƃ��ɁAsetter,getter���ɗ��̂��B
                                input.setBetFlg(true);
                                BetTheGame(db, input, 0);
                            }


                        } else {
                            //���~�������F�q���Ă�����~���B
                            //���͏㏸�ł����q���Ȃ��̂ŁA�J�̒l�����X�V�ł���
                        }
                        //�J�̒l�̍X�V
                        input.setVllyStart(input.getBfStart());
                        input.setVllyEnd(input.getBfEnd());
                        input.setVllyHigh(input.getBfHigh());
                        input.setVllyLow(input.getBfLow());
                        vlly_koshin++;
                    }
                    //���[�g�㏸���������ꍇ�͂��̂܂܎���
                    input.setBfTrend(1);

                } else if (input.getBfHigh().compareTo((input.getThisHigh().add(err))) >= 0){
                    //���[�g�ቺ�̂Ƃ�
                    input.setThisTrend(0);
                    //�܂��n�߂ɑ��؂�`�F�b�N
                    if(input.isBetFlg() && input.getVllyHigh().compareTo(input.getThisHigh()) >=0){
                        //���~��臒l�����A���q���Ă����ꍇ�͖������ő��؂�
                            input.setBetFlg(false);
                            BetTheGame(db, input, 2);
                    }
                        //�O��g�����h�Ɣ�r���A���ʂ��`�F�b�N����B
                        if (input.getThisTrend() != input.getBfTrend()){
                            //�R���ł����Ƃ��A�O�RMt�̒l�Ɣ�r����B
                            if(input.getBfHigh().compareTo(input.getMtHigh()) >=0){
                                //�㏸�������F�x�b�g���Ă���ꍇ���܂߂ăX�e�C
                            } else {
                                //���~�������F�x�b�g���Ă���ꍇ�͗��m
                                if(input.isBetFlg()){
                                    //���m
                                    input.setBetFlg(false);
                                    BetTheGame(db, input, 1);
                                }
                            }
                            //�R�̒l�̍X�V
                            input.setMtStart(input.getBfStart());
                            input.setMtEnd(input.getBfEnd());
                            input.setMtHigh(input.getBfHigh());
                            input.setMtLow(input.getBfLow());
                            mt_koshin++;
                        }
                        //���[�g���~���������ꍇ�͂��̂܂܎���
                        input.setBfTrend(0);

                } else {
                    //�h�ꂪ�덷�͈̔͂̂Ƃ�
                    input.setThisTrend(input.getBfTrend());
                    same++;
                }
                //before�l���X�V
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

      //#############################SQL�ݒ�##############################
        String sql1 = "insert into T_TRADE_HIST values(?,?,?,?,?,?)";
      //#############################SQL�ݒ�##############################

        BigDecimal calcs;
        PreparedStatement ps = null;
        ps = db.prepareStatement(sql1);
        ps.setString(1, input.getPairName());
        ps.setTimestamp(2,input.getThisTime());
        ps.setInt(3, tacticsCode);
        if(input.isBetFlg()){
            //betFlg true�F�����ɑ���Ƃ�
            calcs = BigDecimal.valueOf(input.getJpyFunds());
            calcs = calcs.divide(input.getThisHigh(), 2, BigDecimal.ROUND_HALF_UP);
            ps.setBigDecimal(4,input.getThisHigh());
            ps.setInt(5, 0);
            ps.setInt(6,  calcs.intValue());
            input.setForeignFunds(calcs.intValue());
            input.setJpyFunds(0);
        } else {
            //betFlg false�F����ɑ���Ƃ�
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
