package com.puuga.exontologyandroid;

import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {
	// new
	
	private String tagname = "exOntologyAndroid";
	private String webURL = "file:///android_asset/www/index.html";
	@SuppressWarnings("unused")
	private String owlURL = "file:///android_asset/tourism.owl";
	
	WebView webView;
	private ProgressDialog progressDialog;
	
	private String jsonResultToWeb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		makeWebView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_reset) {
			webView.loadUrl(webURL);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("JavascriptInterface")
	private void makeWebView() {
		webView = (WebView) findViewById(R.id.webView1);
		webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(new WebChromeClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setGeolocationEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.addJavascriptInterface(this, "Android");
		webView.loadUrl(webURL);
		Log.d(tagname, "loading default Url:"+webURL);
	}
	
	
	@JavascriptInterface
	public void getQueryFromWeb(final String arg) {
		Log.d(tagname, "getQueryFromWeb arg:"+arg);
		//final String jsonResult = null;
		new exeOntologyTask().execute(arg, null, jsonResultToWeb);
	}
	
	class exeOntologyTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... args) {
			String json = args[0];
			Log.d(tagname, "json in doInBackground:"+json);
			Ontology ontology = new Ontology();
			String query = ontology.makeQueryFromJSON(json);
			AssetManager am = MainActivity.this.getAssets();
			InputStream fis = null;
			try {
				fis = am.open("tourism.owl");
			} catch (IOException e) {
				e.printStackTrace();
			}
			String jsonResult = ontology.getResultInJSON(query, fis);
			Log.d(tagname, "jsonResult in doInBackground:"+jsonResult);
			return jsonResult;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading...");
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			Log.d(tagname, "result in onPostExecute:"+result);
			webView.loadUrl("javascript:getResultfromAndroid('"+result+"')");
			super.onPostExecute(result);
		}
	}

}
