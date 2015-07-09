package com.eps.udl.bitcoincheckout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bitpay.sdk.android.BitPayAndroid;
import com.bitpay.sdk.controller.BitPayException;

public class Main_menu extends Activity {

    private final String BITPAY_URL = "https://test.bitpay.com/";

    private final String APP_DEVICE_NAME = "BitcoinCheckout client device";

    public static BitPayAndroid bitpay;

    private AlertDialog.Builder builder;

    private boolean codeGenerated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Pairing code:")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        checkClientStatus();
    }

    public void tryPairing(View view) {
        pairDevice(APP_DEVICE_NAME);
    }

    public void createNewInvoice(View view) {
        Intent intent = new Intent(this, Invoice_Generator.class);
        startActivity(intent);
    }

    public void viewInvocesHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    private void checkClientStatus() {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Holo_Dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Checking pairing status...");
        dialog.show();

        final boolean[] paired = {false, false};
        new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    bitpay = new BitPayAndroid(Main_menu.this.APP_DEVICE_NAME,
                            Main_menu.this.BITPAY_URL, Main_menu.this);
                    paired[0] = bitpay.clientIsAuthorized(BitPayAndroid.FACADE_MERCHANT);
                    if(paired[1]) {
                        String code = bitpay.requestClientAuthorization(BitPayAndroid.FACADE_POS);
                        bitpay.authorizeClient(code);
                        paired[1] = bitpay.clientIsAuthorized(BitPayAndroid.FACADE_POS);
                    }
                    System.out.println("App paired: " + paired[0]);
                } catch (BitPayException bpex) {
                    System.err.println("Error in BitPay auth: " + bpex.getMessage());
                    bpex.printStackTrace();
                    paired[0] = false;
                }
                dialog.dismiss();
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        if(paired[0]) {
                            setContentView(R.layout.activity_main_menu_paired);
                        } else {
                            setContentView(R.layout.activity_main_menu);
                        }
                    }
                });
            }
        }).start();
    }

    private void pairDevice(final String deviceName) {
        if(codeGenerated) {
            checkClientStatus();
            return;
        }

        Button pairButton = (Button) findViewById(R.id.pairButton);
        pairButton.setActivated(false);

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Holo_Dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Pairing device...");
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!bitpay.clientIsAuthorized(BitPayAndroid.FACADE_MERCHANT)) {
                    try {
                        final String pairCode =
                                bitpay.requestClientAuthorization(BitPayAndroid.FACADE_MERCHANT);
                        dialog.dismiss();
                        codeGenerated = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog alert = builder.create();
                                alert.setMessage(pairCode);
                                alert.show();
                            }
                        });

                    } catch (BitPayException bpex) {
                        System.err.println("Error in pairing request: " +bpex.getMessage());
                        bpex.printStackTrace();
                    }
                } else {
                    dialog.dismiss();
                    runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.activity_main_menu_paired);
                        }
                    });
                }
            }
        }).start();



    }
}
