package com.revolut.kataykin.revolutcurrency;


import android.app.Activity;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.revolut.kataykin.revolutcurrency.db.RateDBOp;
import com.revolut.kataykin.revolutcurrency.model.Rate;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<AMain> mainActivityTestRule = new ActivityTestRule<>(AMain.class);

    private Activity mActivity;
    private RecyclerView mRecyclerView;
    private int itemCount;

    @Before
    public void setUpTest() {
        this.mActivity = mainActivityTestRule.getActivity();
        this.mRecyclerView = mActivity.findViewById(R.id.am_rv_main);
        this.itemCount = mRecyclerView.getAdapter().getItemCount();
    }

    /**
     * CHECK to OPEN APP
     */
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        Assert.assertEquals("com.revolut.kataykin.revolutcurrency", appContext.getPackageName());
    }


    /**
     * CHECK to CLICK IN EVERY ITEMS
     */
    @Test
    public void testClickToItem() {
        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                onView(ViewMatchers.withId(R.id.am_rv_main))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i,
                                ViewActions.click()), ViewActions.closeSoftKeyboard(), RecyclerViewActions.scrollToPosition(i));
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /* check if the ViewHolder is being displayed */
                onView(new RecyclerViewMatcher(R.id.am_rv_main)
                        .atPositionOnView(i, R.id.vc_et_value))
                        .check(matches(ViewMatchers.isDisplayed()));
            }
        }
    }


    /**
     * CHECK to WRITE LONG NUMER
     */
    @Test
    public void testWriteToItem() {
        if (itemCount > 0) {

            onView(new RecyclerViewMatcher(R.id.am_rv_main)
                    .atPositionOnView(0, R.id.vc_et_value))
                    .perform(ViewActions.click(), ViewActions.clearText());

            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            DecimalFormat df = new Titles().getDecimalFormat();
            String s = "";
            for (int i = 0; i < 30; i++) {
                s += "9";
                onView(new RecyclerViewMatcher(R.id.am_rv_main)
                        .atPositionOnView(0, R.id.vc_et_value))
                        .perform(ViewActions.typeText("9"));

                BigDecimal db = new BigDecimal(s);

                onView(new RecyclerViewMatcher(R.id.am_rv_main)
                        .atPositionOnView(0, R.id.vc_et_value))
                        .check(matches(ViewMatchers.withText(df.format(db))));

                try {
                    Thread.sleep(20L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * CHECK THE CORRECT CALCULATION
     */
    @Test
    public void chechCalc() {

        if (itemCount < 2)
            return;

        View v0 = mRecyclerView.getChildAt(0);
        View v1 = mRecyclerView.getChildAt(1);
        AMain.MyAdapter.MyViewHolder panel1 = (AMain.MyAdapter.MyViewHolder) mRecyclerView.getChildViewHolder(v0);
        AMain.MyAdapter.MyViewHolder panel2 = (AMain.MyAdapter.MyViewHolder) mRecyclerView.getChildViewHolder(v1);

        // GET RATES FROM DB
        List<Rate> lRates = new RateDBOp().getByKeyBase(mActivity, panel1.getKey());

        double rate = 1.0;
        for (Rate r : lRates) {
            if (r.getKeyTo().equalsIgnoreCase(panel2.getKey())) {
                rate = r.getRate();
                break;
            }
        }

        String[] aVal = {"0.0001", "0.1", "1", "0", "10000", "999999999999999999999"};
        for (String val : aVal) {
            BigDecimal dbBase = new BigDecimal(val);
            DecimalFormat df = new Titles().getDecimalFormat();

            onView(new RecyclerViewMatcher(R.id.am_rv_main)
                    .atPositionOnView(0, R.id.vc_et_value))
                    .perform(ViewActions.clearText(), ViewActions.typeText(df.format(dbBase)), ViewActions.closeSoftKeyboard());

            try {
                Thread.sleep(150L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // GET INTERVAL BACAUSE RATE CAN CHAGE
            BigDecimal bdValue = dbBase.multiply(new BigDecimal(rate));
            BigDecimal bdValueDiff = bdValue.multiply(new BigDecimal("0.01"));
            BigDecimal bdValueUp = bdValue.add(bdValueDiff);
            BigDecimal bdValueDown = bdValue.subtract(bdValueDiff);

            BigDecimal valueInPanel = new BigDecimal(panel2.getText());

//            String sHasToBe = df.format(dbBase.multiply(new BigDecimal(rate)));

            MatcherAssert.assertThat(valueInPanel, Matchers.greaterThanOrEqualTo(bdValueDown));
            MatcherAssert.assertThat(valueInPanel, Matchers.lessThanOrEqualTo(bdValueUp));

//            onView(new RecyclerViewMatcher(R.id.am_rv_main)
//                    .atPositionOnView(1, R.id.vc_et_value))
//                    .check(matches(ViewMatchers.withText(sHasToBe)));
        }
    }


    public static org.hamcrest.Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    public static Matcher<View> noDrawable() {
        return new DrawableMatcher(-1);
    }


    private void l(String log) {
        System.out.println(log);
    }
}
