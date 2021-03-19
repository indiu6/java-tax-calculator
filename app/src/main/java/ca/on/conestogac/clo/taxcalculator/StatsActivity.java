package ca.on.conestogac.clo.taxcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity {

    private TextView textViewTotalTaxPaid;
    private TextView textViewTotalCostsPaid;
    private TextView textViewTransactions;
    private Button buttonResetStats;

//    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_stats);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        buttonResetStats = findViewById(R.id.buttonResetStats);

        buttonResetStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TaxCalculatorApplication) getApplication()).resetTableStats();
                refreshStats();
            }
        });

        refreshStats();
    }

    private void refreshStats() {
        String customCurrencyPrefix;
        String decimalPointType;
        final TaxCalculatorApplication application;

        Intent intent = getIntent();

        textViewTotalTaxPaid = findViewById(R.id.textViewTaxesPaid);
        textViewTotalCostsPaid = findViewById(R.id.textViewCosts);
        textViewTransactions = findViewById(R.id.textViewTransactions);

        application = ((TaxCalculatorApplication) getApplication());

//        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        customCurrencyPrefix = intent.getStringExtra("currency");
        decimalPointType = intent.getStringExtra("decimal");

        textViewTotalTaxPaid.setText(
                application.formatCurrency(
                        application.getTotalTaxPaid(),
                        customCurrencyPrefix,
                        decimalPointType
                )
        );
        textViewTotalCostsPaid.setText(
                application.formatCurrency(
                        application.getTotalCostsPaid(),
                        customCurrencyPrefix,
                        decimalPointType
                )
        );
        textViewTransactions.setText("" + application.getTransactions());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean ret = true;

        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }

        return ret;
    }
}