package com.revolut.kataykin.revolutcurrency.view;

import com.revolut.kataykin.revolutcurrency.model.Rate;

import java.util.List;

public abstract class DoEventGetRates {

    public abstract void run(List<Rate> rates);
}
