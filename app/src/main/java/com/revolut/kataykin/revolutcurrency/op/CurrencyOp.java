package com.revolut.kataykin.revolutcurrency.op;

import com.revolut.kataykin.revolutcurrency.model.Currency;

import java.util.List;

public class CurrencyOp {

    public Currency getByKey(String key, List<Currency> lCur) {
        if (lCur == null || key == null)
            return null;
        for (Currency c : lCur) {
            if (c.getKey() != null && c.getKey().equalsIgnoreCase(key))
                return c;
        }
        return null;
    }

    public Currency.Info getInfoByKey(String key) {
        if (key == null)
            return null;
        for (Currency.Info ci : Currency.Info.values()) {
            if (ci.getKey().equalsIgnoreCase(key))
                return ci;
        }
        return null;
    }
}
