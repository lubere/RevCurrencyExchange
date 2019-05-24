package com.revolut.kataykin.revolutcurrency.model;

import android.support.annotation.NonNull;

import com.revolut.kataykin.revolutcurrency.R;

import java.math.BigDecimal;

public class Currency {

    public enum Info {
        USD("USD", R.string.USD, R.drawable.z_flag_usd),
        AUD("AUD", R.string.AUD, R.drawable.z_flag_aud),
        BGN("BGN", R.string.BGN, R.drawable.z_flag_bgn),
        BRL("BRL", R.string.BRL, R.drawable.z_flag_brl),
        CAD("CAD", R.string.CAD, R.drawable.z_flag_cad),
        CHF("CHF", R.string.CHF, R.drawable.z_flag_chf),
        CNY("CNY", R.string.CNY, R.drawable.z_flag_cny),
        CZK("CZK", R.string.CZK, R.drawable.z_flag_czk),
        DKK("DKK", R.string.DKK, R.drawable.z_flag_dkk),
        GBP("GBP", R.string.GBP, R.drawable.z_flag_gbp),
        HKD("HKD", R.string.HKD, R.drawable.z_flag_hkd),
        HRK("HRK", R.string.HRK, R.drawable.z_flag_hrk),
        HUF("HUF", R.string.HUF, R.drawable.z_flag_huf),
        IDR("IDR", R.string.IDR, R.drawable.z_flag_idr),
        ILS("ILS", R.string.ILS, R.drawable.z_flag_ils),
        INR("INR", R.string.INR, R.drawable.z_flag_inr),
        ISK("ISK", R.string.ISK, R.drawable.z_flag_isk),
        JPY("JPY", R.string.JPY, R.drawable.z_flag_jpy),
        KRW("KRW", R.string.KRW, R.drawable.z_flag_krw),
        MXN("MXN", R.string.MXN, R.drawable.z_flag_mxn),
        MYR("MYR", R.string.MYR, R.drawable.z_flag_myr),
        NOK("NOK", R.string.NOK, R.drawable.z_flag_nok),
        NZD("NZD", R.string.NZD, R.drawable.z_flag_nzd),
        PHP("PHP", R.string.PHP, R.drawable.z_flag_php),
        PLN("PLN", R.string.PLN, R.drawable.z_flag_pln),
        RON("RON", R.string.RON, R.drawable.z_flag_ron),
        RUB("RUB", R.string.RUB, R.drawable.z_flag_rub),
        SEK("SEK", R.string.SEK, R.drawable.z_flag_sek),
        SGD("SGD", R.string.SGD, R.drawable.z_flag_sgd),
        THB("THB", R.string.THB, R.drawable.z_flag_thb),
        TRY("TRY", R.string.TRY, R.drawable.z_flag_try),
        ZAR("ZAR", R.string.ZAR, R.drawable.z_flag_zar),
        EUR("EUR", R.string.EUR, R.drawable.z_flag_eur);

        private String key;
        private Integer titleId;
        private Integer flag_image;

        Info(String key, Integer titleId, Integer flag_image) {
            this.key = key;
            this.titleId = titleId;
            this.flag_image = flag_image;
        }

        public String getKey() {
            return key;
        }

        public Integer getTitleId() {
            return titleId;
        }

        public Integer getFlagImage() {
            return flag_image;
        }
    }

    private String key;
    private String descr;
    private BigDecimal value;
    private Double rate;


    public Currency(String key, BigDecimal value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    @NonNull
    @Override
    public String toString() {
        return "Currency{" +
                "key='" + key + '\'' +
                ", descr='" + descr + '\'' +
                ", value=" + value +
                ", rate=" + rate +
                '}';
    }
}
