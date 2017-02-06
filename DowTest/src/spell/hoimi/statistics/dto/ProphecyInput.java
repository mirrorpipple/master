package spell.hoimi.statistics.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ProphecyInput {

    //全体共有
    private String pairName = null;
    int tradeFlg = 0;

    //計算用Funds
    private int jpyFunds = 0;
    private int foreignFunds = 0;


    //This
    private BigDecimal thisStart = null;
    private BigDecimal thisEnd = null;
    private BigDecimal thisHigh = null;
    private BigDecimal thisLow = null;
    private Timestamp thisTime = null;
    private int thisTrend = 0;

    //before
    private BigDecimal bfStart = null;
    private BigDecimal bfEnd = null;
    private BigDecimal bfHigh = null;
    private BigDecimal bfLow = null;
    private int bfTrend =0;

    //mountain
    private BigDecimal mtStart = null;
    private BigDecimal mtEnd = null;
    private BigDecimal mtHigh = null;
    private BigDecimal mtLow = null;
    private int mtTrend =0;

    //valley
    private BigDecimal vllyStart = null;
    private BigDecimal vllyEnd = null;
    private BigDecimal vllyHigh = null;
    private BigDecimal vllyLow = null;
    private int vllyTrend =0;

    //bet
    private BigDecimal betStart = null;
    private BigDecimal betEnd = null;
    private BigDecimal betHigh = null;
    private BigDecimal betLow = null;

    //flg
    private boolean setupFlg = false;
    //ベットフラグ。建っていれば次でベット。
    private boolean betFlg = false;
    //回収フラグ。建っていれば次で資金に戻す。
    private boolean shotFlg = false;


    //This
    public BigDecimal getThisStart() {
        return thisStart;
    }
    public void setThisStart(BigDecimal thisStart) {
        this.thisStart = thisStart;
    }
    public BigDecimal getThisEnd() {
        return thisEnd;
    }
    public void setThisEnd(BigDecimal thisEnd) {
        this.thisEnd = thisEnd;
    }
    public BigDecimal getThisHigh() {
        return thisHigh;
    }
    public void setThisHigh(BigDecimal thisHigh) {
        this.thisHigh = thisHigh;
    }
    public BigDecimal getThisLow() {
        return thisLow;
    }
    public void setThisLow(BigDecimal thisLow) {
        this.thisLow = thisLow;
    }
    public Timestamp getThisTime() {
        return thisTime;
    }
    public void setThisTime(Timestamp thisTime) {
        this.thisTime = thisTime;
    }
    public int getThisTrend() {
        return thisTrend;
    }
    public void setThisTrend(int thisTrend) {
        this.thisTrend = thisTrend;
    }

    //before
    public BigDecimal getBfStart() {
        return bfStart;
    }
    public void setBfStart(BigDecimal bfStart) {
        this.bfStart = bfStart;
    }
    public BigDecimal getBfEnd() {
        return bfEnd;
    }
    public void setBfEnd(BigDecimal bfEnd) {
        this.bfEnd = bfEnd;
    }
    public BigDecimal getBfHigh() {
        return bfHigh;
    }
    public void setBfHigh(BigDecimal bfHigh) {
        this.bfHigh = bfHigh;
    }
    public BigDecimal getBfLow() {
        return bfLow;
    }
    public void setBfLow(BigDecimal bfLow) {
        this.bfLow = bfLow;
    }
    public int getBfTrend() {
        return bfTrend;
    }
    public void setBfTrend(int bfTrend) {
        this.bfTrend = bfTrend;
    }

    //mountain
    public BigDecimal getMtStart() {
        return mtStart;
    }
    public void setMtStart(BigDecimal mtStart) {
        this.mtStart = mtStart;
    }
    public BigDecimal getMtEnd() {
        return mtEnd;
    }
    public void setMtEnd(BigDecimal mtEnd) {
        this.mtEnd = mtEnd;
    }
    public BigDecimal getMtHigh() {
        return mtHigh;
    }
    public void setMtHigh(BigDecimal mtHigh) {
        this.mtHigh = mtHigh;
    }
    public BigDecimal getMtLow() {
        return mtLow;
    }
    public void setMtLow(BigDecimal mtLow) {
        this.mtLow = mtLow;
    }
    public int getMtTrend() {
        return mtTrend;
    }
    public void setMtTrend(int mtTrend) {
        this.mtTrend = mtTrend;
    }

    //valley
    public BigDecimal getVllyStart() {
        return vllyStart;
    }
    public void setVllyStart(BigDecimal vllyStart) {
        this.vllyStart = vllyStart;
    }
    public BigDecimal getVllyEnd() {
        return vllyEnd;
    }
    public void setVllyEnd(BigDecimal vllyEnd) {
        this.vllyEnd = vllyEnd;
    }
    public BigDecimal getVllyHigh() {
        return vllyHigh;
    }
    public void setVllyHigh(BigDecimal vllyHigh) {
        this.vllyHigh = vllyHigh;
    }
    public BigDecimal getVllyLow() {
        return vllyLow;
    }
    public void setVllyLow(BigDecimal vllyLow) {
        this.vllyLow = vllyLow;
    }
    public int getVllyTrend() {
        return vllyTrend;
    }
    public void setVllyTrend(int vllyTrend) {
        this.vllyTrend = vllyTrend;
    }

    //bet
    public BigDecimal getBetStart() {
        return betStart;
    }
    public void setBetStart(BigDecimal betStart) {
        this.betStart = betStart;
    }
    public BigDecimal getBetEnd() {
        return betEnd;
    }
    public void setBetEnd(BigDecimal betEnd) {
        this.betEnd = betEnd;
    }
    public BigDecimal getBetHigh() {
        return betHigh;
    }
    public void setBetHigh(BigDecimal betHigh) {
        this.betHigh = betHigh;
    }
    public BigDecimal getBetLow() {
        return betLow;
    }
    public void setBetLow(BigDecimal betLow) {
        this.betLow = betLow;
    }

    //flg
    public boolean isSetupFlg() {
        return setupFlg;
    }
    public void setSetupFlg(boolean setupFlg) {
        this.setupFlg = setupFlg;
    }
    public boolean isBetFlg() {
        return betFlg;
    }
    public void setBetFlg(boolean betFlg) {
        this.betFlg = betFlg;
    }
    public boolean isShotFlg() {
        return shotFlg;
    }
    public void setShotFlg(boolean shotFlg) {
        this.shotFlg = shotFlg;
    }

    //計算用Funds
    public int getJpyFunds() {
        return jpyFunds;
    }
    public void setJpyFunds(int jpyFunds) {
        this.jpyFunds = jpyFunds;
    }
    public int getForeignFunds() {
        return foreignFunds;
    }
    public void setForeignFunds(int foreignFunds) {
        this.foreignFunds = foreignFunds;
    }
    public String getPairName() {
        return pairName;
    }
    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

















}
