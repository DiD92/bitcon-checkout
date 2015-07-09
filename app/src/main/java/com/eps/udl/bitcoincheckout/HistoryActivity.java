package com.eps.udl.bitcoincheckout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.model.Invoice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends Activity {

    private class InvoiceAdapter extends ArrayAdapter<Invoice> {

        public InvoiceAdapter(Context context, ArrayList<Invoice> invoices) {
            super(context, 0, invoices);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Invoice invoice = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.invoice_item, parent, false);
            }
            // Lookup view for data population
            TextView invId = (TextView) convertView.findViewById(R.id.invId);
            // Populate the data into the template view using the data object
            invId.setText(invoice.getId());
            //Setting touch listener
            convertView.setOnTouchListener( new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(MotionEvent.ACTION_UP == event.getAction()) {
                        showInvoiceDialog(getItem(position));
                    }

                    return true;
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }

        private void showInvoiceDialog(Invoice invoice) {
            Date date = new Date(Long.parseLong(invoice.getInvoiceTime()));
            double rate = Double.parseDouble(invoice.getExRates().get("EUR"));
            double price = Double.parseDouble(invoice.getBtcPrice()) * rate;
            String message = String.format("Date created: %s\nAmount: %.2f EUR\nStatus: %s",
                    date.toString(), price, invoice.getStatus());
            AlertDialog alert = builder.create();
            alert.setMessage(message);
            alert.show();
        }

    }

    private AlertDialog.Builder builder;

    private ArrayAdapter<Invoice> adapter;

    private ListView list;
    private Spinner spinner;

    private DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        list = (ListView) findViewById(R.id.items_list);
        adapter = new InvoiceAdapter(this, new ArrayList<Invoice>());
        list.setAdapter(adapter);
        spinner = (Spinner) findViewById(R.id.history_spinner);
        fillSpinner(spinner);

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Invoice information:")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void convertInvoicesToList(Date current, Date past) {
        String c_time = format.format(current);
        String p_time = format.format(past);
        try {
            final List<Invoice> invoices = Main_menu.bitpay.getInvoices(p_time, c_time);
            Handler refresh = new Handler(Looper.getMainLooper());
            refresh.post(new Runnable() {
                public void run() {
                    adapter.clear();
                    adapter.addAll(invoices);
                }
            });
        } catch(BitPayException bpex) {
            System.err.println("Error in invoce retrieval: " + bpex.getMessage());
            bpex.printStackTrace();
        }
    }

    private void fillSpinner(Spinner spinner) {
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.date_options, android.R.layout.simple_spinner_item);
        System.out.println("Array count: " + adapter.getCount());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("[SPINNER] Option selected\n");
                final int pos = position;
                //Open dialog
                final Dialog dialog = new Dialog(HistoryActivity.this,
                        android.R.style.Theme_Holo_Dialog);
                dialog.setCancelable(false);
                dialog.setTitle("Getting invoices...");
                dialog.show();

                new Thread( new Runnable () {

                    @Override
                    public void run() {
                        Calendar c_current = Calendar.getInstance();
                        Calendar c_past = Calendar.getInstance();
                        switch (pos) {
                            case 0:
                                // Get time in past
                                c_past.add(Calendar.DAY_OF_MONTH, -1);
                                convertInvoicesToList(c_current.getTime(), c_past.getTime());
                                dialog.dismiss();
                                break;
                            case 1:
                                // Get time in past
                                c_past.add(Calendar.MONTH, -1);
                                convertInvoicesToList(c_current.getTime(), c_past.getTime());
                                dialog.dismiss();
                                break;
                            default:
                                // Get time in past
                                c_past.add(Calendar.YEAR, -1);
                                convertInvoicesToList(c_current.getTime(), c_past.getTime());
                                dialog.dismiss();
                                break;
                        }
                    }
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(adapter != null) {
                    adapter.clear();
                }

                if(list != null) {
                    list.setAdapter(null);
                }
            }
        });
    }
}
