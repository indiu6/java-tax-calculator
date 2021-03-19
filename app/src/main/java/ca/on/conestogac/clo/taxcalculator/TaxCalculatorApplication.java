package ca.on.conestogac.clo.taxcalculator;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class TaxCalculatorApplication extends Application {

    private static final String DB_NAME = "db_tax_stats";
    private static final int DB_VERSION = 1;

    private SQLiteOpenHelper helper;


    @Override
    public void onCreate() {
            /*
                 Tax Paid
                 Cost Paid
                 Total transactions

                 Cost Paid | Tax Paid
                 --------------------
                 1000        130
                 2000        260

                 SELECT SUM(cost_paid) FROM tbl_stats;
                 SELECT SUM(tax_paid) FROM tbl_stats;
                 SELECT COUNT(*) FROM tbl_stats;
            */

        helper = new SQLiteOpenHelper(this, DB_NAME, null, DB_VERSION) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {
                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS tbl_stats(" +
                        "cost_paid REAL, tax_paid REAL)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
                // No-op
            }
        };

        super.onCreate();

    }

    public void addTransaction(double cost, double tax) {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("INSERT INTO tbl_stats (cost_paid, tax_paid) "
                + "VALUES (" + cost + ", " + tax + ")");
    }

    public double getTotalTaxPaid() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(tax_paid) AS taxPaid FROM tbl_stats", null);
        double ret;

        cursor.moveToFirst();
        ret = cursor.getDouble(0);
        cursor.close();

        return(ret);
    }

    public double getTotalCostsPaid() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(cost_paid) FROM tbl_stats", null);
        double ret;

        cursor.moveToFirst();
        ret = cursor.getDouble(0);
        cursor.close();

        return(ret);
    }

    public int getTransactions() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tbl_stats", null);
        int ret;

        cursor.moveToFirst();
        ret = cursor.getInt(0);
        cursor.close();

        return(ret);
    }

    public String formatCurrency(double total, String customCurrencyPrefix, String decimalPointType) {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols customDecimal = new DecimalFormatSymbols();

        customDecimal.setDecimalSeparator(decimalPointType.isEmpty()
                ? ' '
                : decimalPointType.charAt(0));
        df.setDecimalFormatSymbols(customDecimal);
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);

        return(customCurrencyPrefix.concat(df.format(total)));
    }

    public void resetTableStats() {
        SQLiteDatabase db = helper.getWritableDatabase();

        db.execSQL("DELETE FROM tbl_stats;");
    }
}
