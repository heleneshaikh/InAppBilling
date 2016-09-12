package com.hfad.inappbilling;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.hfad.inappbillign.R;
import com.hfad.util.IabHelper;
import com.hfad.util.IabResult;
import com.hfad.util.Inventory;
import com.hfad.util.Purchase;

public class InAppBillingActivity extends AppCompatActivity {
    private Button clickButton;
    private Button buyButton;
    private static final String TAG = "test";
    IabHelper helper;
    static final String ITEM_SKU = "android.test.purchased";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);
    }

    @Override
    protected void onStart() {
        super.onStart();
        buyButton = (Button) findViewById(R.id.buyButton);
        clickButton = (Button) findViewById(R.id.clickButton);
        clickButton.setEnabled(false);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArSFfKEifa+YsH/jnOoHdUNhvlXhjL/anK+b+MWPJ1BYoFRIsQH1woHBRdoxIdGOgbyCIKD6q0p+Ka0R70Vso3j0xOcCsfcbxQizRyjZ5zaE2dhN+CQB7Pt4GsFICO0QmXDXsQA6O4R6sRR0EzjJ73/ac1962jqSEPdPcdLljPH/wzPW/yxj+hOpcqpxk/ezRaoIIOmRAwO0BZffQU1YNw3sY3m2lVYJJxAxvd3xT8WMubG62wL740b8DnO6UaEwBVNbxGBHNZU029x7tD/a92gS27WWDVj+hDW1g4T1wkblmbRwL/qKiAKcgopCybVnDUfGtpxYNhncqr6VlYZt6ewIDAQAB";

        helper = new IabHelper(this, base64EncodedPublicKey);
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        helper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "failed" + result);
                } else {
                    Log.d(TAG, "set up OK");
                }
            }
        });
    }

    public void buyClick(View view) {
        helper.launchPurchaseFlow(this, ITEM_SKU, 1001, purchaseFinishedListener, "purchasetoken");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!helper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener(){
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isFailure()) {
                //handle
            } else {
                consumeItem();
                buyButton.setEnabled(false);
            }
        }
    };

    private void consumeItem() {
        helper.queryInventoryAsync(receivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener receivedInventoryListener = new IabHelper.QueryInventoryFinishedListener(){
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                //handle
            } else {
                helper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        consumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener consumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isFailure()) {
                //handle
            } else {
                clickButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (helper != null) {
            helper.dispose();
            helper = null;
        }
    }
}
