package ca.on.conestogac.clo.taxcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.preference.PreferenceManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int DEFAULT_TAX_RATE = 13;

    private Button buttonReset;
    private Button buttonCalculateTax;
    private Button buttonDecrement;
    private Button buttonIncrement;
    private TextView textViewTotal;
    private EditText editTextTaxRate;
    private EditText editTextCost;
    private ImageView imageViewFace;

    private String customCurrencyPrefix;
    private String decimalPointType;

    private boolean creatingActivity = false;
    private boolean saveState;

    private SharedPreferences sharedPref;
    private Timer timerForSmileyFace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final AppCompatActivity SELF = this;

        creatingActivity = true;

        View.OnClickListener taxRateIncDecListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextTaxRate.setText("" +
                        (
                            Float.parseFloat(editTextTaxRate.getText().toString())
                                + (R.id.buttonIncrement == view.getId() ? 1 : -1)
                        )
                );
            }
        };

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        buttonReset = findViewById(R.id.buttonReset);
        buttonCalculateTax = findViewById(R.id.buttonCalculateTax);
        buttonDecrement = findViewById(R.id.buttonDecrement);
        buttonIncrement = findViewById(R.id.buttonIncrement);
        textViewTotal = findViewById(R.id.textViewTotal);
        editTextTaxRate = findViewById(R.id.editTextTaxRate);
        editTextCost = findViewById(R.id.editTextCost);
        imageViewFace = findViewById(R.id.imageViewFace);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performReset(false);
            }
        });

        buttonCalculateTax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateTotal(SELF);
            }
        });

        buttonIncrement.setOnClickListener(taxRateIncDecListener);
        buttonDecrement.setOnClickListener(taxRateIncDecListener);

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void calculateTotal(AppCompatActivity self) {
        if (editTextTaxRate.getText().toString().equals("")
                || editTextCost.getText().toString().equals("")) {
            if (self != null) {
//                Toast.makeText(self, R.string.mandatory, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(R.id.mainLayout), R.string.mandatory, Snackbar.LENGTH_LONG).show();
            }
        }
        else {
            float taxRate = Float.parseFloat(editTextTaxRate.getText().toString());
            float cost = Float.parseFloat(editTextCost.getText().toString());
            float taxPaid = taxRate / 100 * cost;
            float total = cost + taxPaid;

            if (self != null)
                ((TaxCalculatorApplication) getApplication()).addTransaction(cost, taxPaid);
            else {
                textViewTotal.setText(((TaxCalculatorApplication) getApplication())
                        .formatCurrency(total, customCurrencyPrefix, decimalPointType)
                );
            }

            textViewTotal.setVisibility(View.VISIBLE);
            imageViewFace.setImageResource(R.drawable.ic_sad_face);

            imageViewFace.setAlpha(0f);
            imageViewFace.animate().alpha(1f).setDuration(2000).setListener(
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            textViewTotal.setText(((TaxCalculatorApplication) getApplication())
                                    .formatCurrency(total, customCurrencyPrefix, decimalPointType)
                            );
                            performReset(true);
                        }
                    }
            );

            if (self != null)
                Toast.makeText(self, "Animation launched", Toast.LENGTH_LONG).show();

            sendBroadcast(new Intent("ca.on.conestogac.clo.taxcalculator.TAX_CALCULATED"));
        }
    }

    private void performReset(boolean setTimer) {
        editTextCost.setText("");
        editTextTaxRate.setText("" + DEFAULT_TAX_RATE);

        if (setTimer) {
            if (timerForSmileyFace != null)
                timerForSmileyFace.cancel();

            timerForSmileyFace = new Timer(true);

            timerForSmileyFace.schedule(new TimerTask() {
                @Override
                public void run() { // belongs to the background thread, which is the timer
                    timerForSmileyFace.cancel();
                    timerForSmileyFace = null;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { // belongs to the main UI thread of the activity
                            imageViewFace.setImageResource(R.drawable.ic_happy_face);
                        }
                    });
                }
            }, 5000);
        }
        else {
            imageViewFace.setImageResource(R.drawable.ic_happy_face);
            textViewTotal.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        Editor ed = sharedPref.edit();

        ed.putString("cost", editTextCost.getText().toString());
        ed.putString("taxRate", editTextTaxRate.getText().toString());
        ed.commit();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        customCurrencyPrefix = sharedPref.getString("currencyPrefix", "$");
        decimalPointType = sharedPref.getString("decimalType", ".");
        saveState = sharedPref.getBoolean("saveOnClose", false);

        /* Restore the previous state if the preference is on or we are NOT creating
           the activity (orientation flip, or deactivate/activate). */
        if (saveState || !creatingActivity) {
            editTextCost.setText(sharedPref.getString("cost", ""));
            editTextTaxRate.setText(sharedPref.getString("taxRate", "" + DEFAULT_TAX_RATE));
        }

        creatingActivity = false;

        calculateTotal(null);
    }

    @Override
    protected void onStop() {
        startService(new Intent(getApplicationContext(), NotificationService.class));
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tax_calculator_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean ret = true;

        switch (item.getItemId()) {
            case R.id.menu_reset:
                performReset(false);
                break;
            case R.id.menu_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case R.id.menu_stats:
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                intent.putExtra("currency", customCurrencyPrefix);
                intent.putExtra("decimal", decimalPointType);

                startActivity(intent);
                break;
            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }

        return ret;
    }
}