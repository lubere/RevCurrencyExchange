package com.revolut.kataykin.revolutcurrency;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.revolut.kataykin.revolutcurrency.db.RateDBOp;
import com.revolut.kataykin.revolutcurrency.model.Currency;
import com.revolut.kataykin.revolutcurrency.model.Rate;
import com.revolut.kataykin.revolutcurrency.op.CurrencyOp;
import com.revolut.kataykin.revolutcurrency.op.HandlerOp;
import com.revolut.kataykin.revolutcurrency.query.QGetData;
import com.revolut.kataykin.revolutcurrency.view.DoEvent;
import com.revolut.kataykin.revolutcurrency.view.DoEventGetRates;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AMain extends AppCompatActivity {

    private RecyclerView rvMain;
    private boolean[] active = {false};
    //    private DecimalFormat df;
    private LinearLayout llMask;
    private ImageView ivProgressRotate;

    private Currency cBase;
    private CurrencyOp currencyOp;

    private RateDBOp rateDBOp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.amain);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = this.getWindow();
            if (window != null) {
                window.setStatusBarColor(Color.WHITE);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        rvMain = findViewById(R.id.am_rv_main);
        llMask = findViewById(R.id.am_ll_mask);
        ivProgressRotate = findViewById(R.id.am_iv_progress_rotate);

        List<Currency> lCur = new ArrayList<>();
        lCur.add(new Currency(Currency.Info.USD.getKey(), BigDecimal.ONE));
//        lCur.add(new Currency(Currency.Info.EUR.getKey(), BigDecimal.ONE));
//        lCur.add(new Currency(Currency.Info.RUB.getKey(), BigDecimal.ONE));

        rvMain.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rvMain.setLayoutManager(llm);

        MyAdapter mAdapter = new MyAdapter(lCur);
        rvMain.setAdapter(mAdapter);

        startGridLoading();
        rvMain.setAlpha(0.0f);

        if (!new Utils().isOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_connecting), Toast.LENGTH_LONG).show();

        getRateDBOp();
        reloadFromUrl(lCur.get(0).getKey(), new DoEvent() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!active[0])
                            return;
                        stopGridLoading();

                        Animation a = new Animation() {
                            @Override
                            protected void applyTransformation(float interpolatedTime, Transformation t) {
                                rvMain.setAlpha(interpolatedTime);
                            }
                        };
                        a.setDuration(400L);
                        rvMain.startAnimation(a);
                    }
                }, 1000L);
            }
        });
    }

    /**
     * CH: GRID VIEW CELLS
     */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        List<Currency> lCur;

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView tvTitle;
            private EditText etValue;
            //            private TextView tvDescr;
            private ImageView ivIcon;
            private boolean enableTextListener;

            MyViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.vc_tv_title);
                etValue = v.findViewById(R.id.vc_et_value);
//                tvDescr = v.findViewById(R.id.vc_tv_descr);
                ivIcon = v.findViewById(R.id.vc_iv_icon);

                Drawable drawable = etValue.getBackground();
                drawable.setColorFilter(getResources().getColor(R.color.color_4), PorterDuff.Mode.SRC_ATOP);
                etValue.setBackground(drawable);

                char separator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
                etValue.setKeyListener(DigitsKeyListener.getInstance("0123456789" + separator));

                etValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (lCur != null && lCur.size() > 0 && lCur.get(0).getKey().equalsIgnoreCase(getKey()))
                            return;
                        View v = getView(MyViewHolder.this);
                        if (v != null)
                            v.callOnClick();
                    }
                });
                setTextFocusable(false);
                etValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE || (event != null && (event.getAction() == KeyEvent.ACTION_DOWN ||
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            if (imm != null)
                                imm.hideSoftInputFromWindow(etValue.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    }
                });
                etValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (!enableTextListener)
                            return;
                        Currency c = getCurrencyOp().getByKey(tvTitle.getText().toString(), lCur);
                        if (c == null)
                            return;
                        BigDecimal v = new BigDecimal(0.0);
                        String sVal = etValue.getText() != null ? etValue.getText().toString().trim() : null;
                        if (sVal != null && sVal.length() > 0) {
                            try {
//                                NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
//                                Number number = format.parse(sVal);
                                v = new BigDecimal(sVal);
                            } catch (Exception e) {
                                e.printStackTrace();
                                setValue(v);
                            }
                        }
                        c.setValue(v);
                        if (v.compareTo(BigDecimal.ZERO) < 0) {
//                        if (v < 0) {
                            setValue(v.abs());
                            etValue.setSelection(etValue.getText().length());
                        }
                        reloadValues();
                    }
                });
            }

            void setValue(BigDecimal val) {
                enableTextListener = false;
                DecimalFormat df = getTitles().getDecimalFormat();
                etValue.setText(val != null ? df.format(val) : "-");
                enableTextListener = true;
            }

            void setTextFocusable(boolean focusable) {
                etValue.setFocusable(focusable);
                etValue.setFocusableInTouchMode(focusable);
            }

            void showKeyboard() {
                if (etValue == null)
                    return;
                etValue.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(AMain.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.showSoftInput(etValue, InputMethodManager.SHOW_IMPLICIT);
            }

            String getKey() {
                return tvTitle != null && tvTitle.getText() != null ? tvTitle.getText().toString().trim() : null;
            }

            String getText() {
                return etValue.getText().toString();
            }
        }

        private View getView(MyViewHolder panel) {
            if (panel == null || panel.getKey() == null)
                return null;
            for (int i = 0; i < rvMain.getChildCount(); i++) {
                View v = rvMain.getChildAt(i);
                MyViewHolder p = (MyViewHolder) rvMain.getChildViewHolder(v);
                if (p.getKey() != null && p.getKey().equalsIgnoreCase(panel.getKey()))
                    return v;
            }
            return null;
        }

        MyAdapter(List<Currency> lCur) {
            this.lCur = lCur;
        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.v_currency, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyViewHolder panel = (MyViewHolder) rvMain.getChildViewHolder(view);
//                    panel.setTextFocusable(true);
                    refreshTextFocusable(view);
                    panel.etValue.selectAll();
                    panel.showKeyboard();

                    int pos = rvMain.getChildLayoutPosition(view);

                    cBase = lCur != null && pos < lCur.size() ? lCur.get(pos) : cBase;
                    reloadValues();

                    moveToTop(pos);
                }
            });
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Currency cur = lCur.get(position);
            holder.tvTitle.setText(cur.getKey());
            holder.setValue(cur.getValue());
            Currency.Info ci = getCurrencyOp().getInfoByKey(cur.getKey());
            holder.ivIcon.setImageResource(ci != null ? ci.getFlagImage() : 0);
//            holder.tvDescr.setText(ci != null ? getResources().getString(ci.getTitleId()) : null);
            holder.setTextFocusable(position == 0);
        }

        @Override
        public int getItemCount() {
            return lCur.size();
        }

        void moveToTop(int itm) {

            if (itm < 1 || itm >= lCur.size())
                return;

            Currency c = lCur.get(itm);
            lCur.remove(itm);
            lCur.add(0, c);

            notifyItemMoved(itm, 0);
        }
    }

    /**
     * LOAD DATA FROM URL & SAVE IN DB
     */

    private void reloadFromUrl(final String baseKey, final DoEvent doAfter) {
        if (QGetData.isRun())
            return;
        QGetData qGetData = new QGetData(new WeakReference<Context>(this), baseKey, new DoEventGetRates() {
            @Override
            public void run(List<Rate> rates) {
                if (!active[0])
                    return;
//                RateDBOp rateDBOp = new RateDBOp();
//                rateDBOp.update(AMain.this, rates);
                if (doAfter != null)
                    doAfter.run();
            }
        }, doAfter);
        qGetData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    /**
     * SHOW NEW DATA BY TIMER
     */
    private void reloadValues() {
        final MyAdapter adapter = (MyAdapter) rvMain.getAdapter();
        if (adapter == null)
            return;
        List<Currency> lCur = adapter.lCur;
        if (lCur == null || lCur.size() == 0)
            return;
        cBase = cBase == null ? lCur.get(0) : cBase;
        cBase.setValue(cBase.getValue() == null ? new BigDecimal(1.0) : cBase.getValue().abs());

        List<Currency> lCurNew = reloadCurVal(cBase, lCur);

        // REFRESH IN GRID
        for (int i = 0; i < rvMain.getChildCount(); i++) {
            View v = rvMain.getChildAt(i);
            if (v == null)
                continue;
            MyAdapter.MyViewHolder panel = (MyAdapter.MyViewHolder) rvMain.getChildViewHolder(v);

            // FIND CURRENCY
            Currency c = getCurrencyOp().getByKey(panel.tvTitle.getText().toString(), lCur);
            if (c != null && c != cBase) {
                panel.setValue(c.getValue());
            }
        }

        if (lCurNew != null && lCurNew.size() > 0) {
            adapter.lCur.addAll(lCurNew);
            adapter.notifyItemRangeInserted(adapter.lCur.size(), lCurNew.size());
        }

        reloadFromUrl(cBase.getKey(), null);
    }

    /**
     * @return List of currencies that not contain in <code>lCur</code>
     */
    public List<Currency> reloadCurVal(Currency cBase, List<Currency> lCur) {

        // GET RATES FROM DB
        List<Rate> lRates = getRateDBOp().getByKeyBase(this, cBase.getKey());

        // CLEAR VALUES IN CURRENCIES
        for (Currency c : lCur) {
            if (c != cBase) {
                c.setRate(null);
                c.setValue(null);
            }
        }

        // SET RATES DEPENDS FROM BASE CURRENCY AND VALUE
        List<Currency> lCurNew = null;
        if (lRates != null)
            for (Rate r : lRates) {
                if (r.getRate() <= 0.0)
                    continue;
                Currency c = getCurrencyOp().getByKey(r.getKeyTo(), lCur);
                if (c == null) {
                    lCurNew = lCurNew != null ? lCurNew : new ArrayList<Currency>();
                    c = new Currency(r.getKeyTo(), new BigDecimal(r.getRate()));
                    lCurNew.add(c);
                }
                c.setRate(r.getRate());
                BigDecimal bd1 = new BigDecimal(r.getRate());
                c.setValue(bd1.multiply(cBase.getValue()));
            }
        return lCurNew;
    }

//    private void refreshTextFocusable(int posInFocus) {
//        if (posInFocus < 0 || posInFocus >= rvMain.getChildCount())
//            return;
//        refreshTextFocusable(rvMain.getChildAt(posInFocus));
//    }

    private void refreshTextFocusable(View vInFocus) {
        if (vInFocus == null)
            return;
        MyAdapter.MyViewHolder panelInFocus = (MyAdapter.MyViewHolder) rvMain.getChildViewHolder(vInFocus);
        for (int i = 0; i < rvMain.getChildCount(); i++) {
            View v = rvMain.getChildAt(i);
            if (v != null) {
                MyAdapter.MyViewHolder panel = (MyAdapter.MyViewHolder) rvMain.getChildViewHolder(v);
                if (panel != null)
                    panel.setTextFocusable(panel == panelInFocus);
            }
        }
    }

    /**
     * TIMER
     */
    private Timer timer;
    private HandlerOp.MyHandler taskReload;

    private void runTimer() {
        if (timer != null)
            return;

        taskReload = new HandlerOp.MyHandler(new DoEvent() {
            @Override
            public void run() {
                if (active[0])
                    reloadValues();
            }
        });

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!active[0])
                    return;
                try {
                    Message msg = taskReload.obtainMessage();
                    taskReload.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000L, 1000L);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        taskReload = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        active[0] = true;
        runTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        active[0] = false;
        stopTimer();
    }

    public CurrencyOp getCurrencyOp() {
        currencyOp = currencyOp != null ? currencyOp : new CurrencyOp();
        return currencyOp;
    }

    private RateDBOp getRateDBOp() {
        rateDBOp = rateDBOp != null ? rateDBOp : new RateDBOp();
        return rateDBOp;
    }

    /**
     * MASK
     */
    private void startGridLoading() {
        if (llMask.getVisibility() == View.VISIBLE)
            return;

        Animation anim_rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        ivProgressRotate.clearAnimation();
        ivProgressRotate.startAnimation(anim_rotate);
        llMask.setVisibility(View.VISIBLE);
    }

    private void stopGridLoading() {
        if (llMask.getVisibility() == View.VISIBLE) {
            ivProgressRotate.clearAnimation();
            llMask.setVisibility(View.GONE);
        }
    }

    private Titles titles;

    private Titles getTitles() {
        titles = titles != null ? titles : new Titles();
        return titles;
    }

    private void l(String log) {
        Log.i(getClass(), log);
    }
}
