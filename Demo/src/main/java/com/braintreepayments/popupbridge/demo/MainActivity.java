package com.braintreepayments.popupbridge.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

//    private static final String POPUP_BRIDGE_URL = "file:///android_asset/index.html";
//    private static final String POPUP_BRIDGE_URL = "https://gse-appstestbed.com/";
    private static final String POPUP_BRIDGE_URL = "https://braintree.github.io/popup-bridge-example/";
    private static final String PAYPAL_POPUP_BRIDGE_URL = "https://braintree.github.io/popup-bridge-example/paypal";
    private static final String PAYPAL_CHECKOUTJS_POPUP_BRIDGE_URL = "https://braintree.github.io/popup-bridge-example/paypal-checkout.html";
    private static final String VENMO_POPUP_BRIDGE_URL = "https://braintree.github.io/popup-bridge-example/venmo";
    private static final String LPM_POPUP_BRIDGE_URL = "https://braintree.github.io/popup-bridge-example/local-payment-methods";

    // mockmerchantapp deployment — branch pedrofsn/feat/popup-bridge-android-support adds
    // window.popupBridge detection so the Braintree JS SDK pages work inside a popup-bridge
    // WebView against te-braintree (stage).
    // Use the localhost variant when running mockmerchantapp locally (npm run start:https):
    //   "https://10.0.2.2:8443/bt-js-sdk"  (Android emulator alias for the host machine)
    // gse-appstestbed.com runs develop; until this branch is merged it will NOT have the
    // PopupBridge guards.
    private static final String MOCKMERCHANTAPP_BT_PAYPAL_URL = "http://10.0.2.2:8443/bt-js-sdk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void switchToWebView(String url) {
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    public void onPopupBridgeClick(View view) {
        switchToWebView(POPUP_BRIDGE_URL);
    }

    public void onPayPalPopupBridgeClick(View view) {
        switchToWebView(PAYPAL_POPUP_BRIDGE_URL);
    }

    public void onPayPalCheckoutJSPopupBridgeClick(View view) {
        switchToWebView(PAYPAL_CHECKOUTJS_POPUP_BRIDGE_URL);
    }

    public void onVenmoPopupBridgeClick(View view) {
        switchToWebView(VENMO_POPUP_BRIDGE_URL);
    }

    public void onLPMPopupBridgeClick(View view) {
        switchToWebView(LPM_POPUP_BRIDGE_URL);
    }

    public void onMockMerchantBtPayPalClick(View view) {
        switchToWebView(MOCKMERCHANTAPP_BT_PAYPAL_URL);
    }
}
