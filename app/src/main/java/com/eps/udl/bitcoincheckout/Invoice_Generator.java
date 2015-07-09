package com.eps.udl.bitcoincheckout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bitpay.sdk.android.BitPayAndroid;
import com.bitpay.sdk.android.InvoiceActivity;
import com.bitpay.sdk.android.interfaces.InvoicePromiseCallback;
import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.model.Invoice;

public class Invoice_Generator extends Activity {

    private final int PAYMENT_RESULT = 12;

    private String inputStringInteger = "0";
    private String inputStringDecimal = "0";

    private boolean inputDecimal = false;

    private TextView displayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invoice__generator);
        // Retrieve a reference to the EditText field for displaying the result.
        displayText = (TextView) findViewById(R.id.quantity_display);
        displayText.setText(inputStringInteger);

        // Register listener (this class) for all the buttons
        ButtonListener listener = new ButtonListener();
        ((Button) findViewById(R.id.btn_0)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_1)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_2)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_3)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_4)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_5)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_6)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_7)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_8)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_9)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_Erase)).setOnClickListener(listener);
        ((Button) findViewById(R.id.btn_Comma)).setOnClickListener(listener);

    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    public void generateInvoice(View view) {
        final Button generate_button = (Button) findViewById(R.id.generate_invoice);
        generate_button.setClickable(false);
        double invoice_value = Double.valueOf(inputStringInteger + "." + inputStringDecimal);
        Main_menu.bitpay.createNewInvoice( new Invoice(invoice_value, "EUR")).then(
                new InvoicePromiseCallback() {
                    @Override
                    public void onSuccess(Invoice promised) {
                        Intent invoice_intent = new Intent(Invoice_Generator.this,
                                InvoiceActivity.class);
                        invoice_intent.putExtra(InvoiceActivity.INVOICE, promised);
                        invoice_intent.putExtra(InvoiceActivity.CLIENT, Main_menu.bitpay);
                        startActivityForResult(invoice_intent, PAYMENT_RESULT);
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                generate_button.setActivated(true);
                            }
                        });
                    }

                    @Override
                    public void onError(BitPayException e) {
                        System.err.println("Error in invoice generation: " + e.getMessage());
                        e.printStackTrace();
                        Invoice_Generator.this.finish();
                    }
                }
        );
    }

    private class ButtonListener implements View.OnClickListener {

        private final int MAX_DECIMALS = 2;
        private final int MAX_INTEGER = 5;
        // On-click event handler for all the buttons
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                // Number buttons: '0' to '9'
                case R.id.btn_0:
                case R.id.btn_1:
                case R.id.btn_2:
                case R.id.btn_3:
                case R.id.btn_4:
                case R.id.btn_5:
                case R.id.btn_6:
                case R.id.btn_7:
                case R.id.btn_8:
                case R.id.btn_9:
                    String inDigit = ((Button) view).getText().toString();
                    if(!inputDecimal && inputStringInteger.length() < MAX_INTEGER) {
                        if (inputStringInteger.equals("0")) {
                            inputStringInteger = inDigit; // no leading zero
                        } else {
                            inputStringInteger += inDigit; // accumulate input digit
                        }
                    } else if(inputDecimal && inputStringDecimal.length() < MAX_DECIMALS) {
                        if (inputStringDecimal.equals("0")) {
                            inputStringDecimal = inDigit; // no leading zero
                        } else {
                            inputStringDecimal += inDigit; // accumulate input digit
                        }
                    }

                    if(Integer.valueOf(inputStringDecimal) > 0) {
                        displayText.setText(inputStringInteger + "," + inputStringDecimal);
                    } else {
                        displayText.setText(inputStringInteger);
                    }
                    break;

                // Change to decimal input
                case R.id.btn_Comma:
                    if(inputStringDecimal.length() < MAX_DECIMALS) {
                        inputDecimal = true;
                    }
                    break;

                // Clear button
                case R.id.btn_Erase:
                    inputStringInteger = "0";
                    inputStringDecimal = "0";
                    inputDecimal = false;
                    displayText.setText(inputStringInteger);
                    break;
            }
        }
    }
}
