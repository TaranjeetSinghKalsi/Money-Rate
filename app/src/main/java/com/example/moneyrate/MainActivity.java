package com.example.moneyrate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class MainActivity extends AppCompatActivity {

    static StringBuilder sb = new StringBuilder();

    protected class DataExtractor extends Thread {
        protected String url;
        protected String className;

        DataExtractor(String url, String className) {
            this.url = url;
            this.className = className;
        }

        @Override
        public void run() {
            try {
                Document document = Jsoup.connect(this.url).get(); //extracting data from url
                Elements elements = document.getElementsByClass(className); //extraction data from classname
                String s = elements.text(); //extracting text
                sb.append(s + "\n\n"); //appending data into string builder sb
            } catch (Exception e) {
                Log.d("DataExtractor", "DataExtractor: " + e.toString());
            }
        }
    }

    protected class RunInBackground extends AsyncTask<Void,Void,Void>{
        protected String city;
        RunInBackground(String city){
            this.city=city;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            DataExtractor gold = new DataExtractor("https://www.bankbazaar.com/gold-rate-" + city + ".html", "gold-rate-today");
            gold.start();
            DataExtractor silver = new DataExtractor("https://www.bankbazaar.com/silver-rate-" + city + ".html", "gold-rate-today");
            silver.start();
            DataExtractor petrol = new DataExtractor("https://www.bankbazaar.com/fuel/petrol-price-" + city + ".html", "gold-rate-today");
            petrol.start();
            DataExtractor diesel = new DataExtractor("https://www.bankbazaar.com/fuel/diesel-price-" + city + ".html", "gold-rate-today");
            diesel.start();
            DataExtractor usd = new DataExtractor("https://www.bookmyforex.com/currency-converter/usd-to-inr", "first_live_trade");
            usd.start();
            DataExtractor cad = new DataExtractor("https://www.bookmyforex.com/currency-converter/cad-to-inr", "first_live_trade");
            cad.start();
            DataExtractor euro = new DataExtractor("https://www.bookmyforex.com/currency-converter/eur-to-inr", "first_live_trade");
            euro.start();
            try {
                euro.join();
                cad.join();
                usd.join();
                diesel.join();
                petrol.join();
                silver.join();
                gold.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            TextView heroText = (TextView) findViewById(R.id.hero);
            heroText.setText(sb.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Using SharedPreferences to store and retrieve data locally
        SharedPreferences sharedPreferences = getSharedPreferences("MoneyRateSP", MODE_PRIVATE);
        String city = sharedPreferences.getString("city", "Enter City");//getting city value
        TextView tCity = findViewById(R.id.city);
        tCity.setText(city); // set Input Text with previously fed city
        Button viewRate = findViewById(R.id.viewrate);
        viewRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Getting Latest rates...", Toast.LENGTH_SHORT).show();
                String city = tCity.getText().toString().trim().replace(' ', '-').toLowerCase(); // url may contains '-' but not ' '

                // Using SharedPreferences to store city data locally
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putString("city", city);
                myEdit.commit();

                sb.delete(0, sb.length());
                new RunInBackground(city).execute();

            }
        });
    }

}