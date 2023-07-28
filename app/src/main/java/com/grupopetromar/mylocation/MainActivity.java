package com.grupopetromar.mylocation;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SeekBar SbMarcadorinicial, SbMarcadorFinal;
    TextView TxtMInicio, TxtMFinal, TxtPrecioActual, TxtPorcentaje, TxtLitros, TxtDinero, TxtDineroSugerido;
    Spinner spinnerCapacidad, spinnerPipa;
    Button btnCalcular;
    EditText eTXTPrecio;
    int inicialM = 0, finalM = 0;
    String noPipa;
    private SharedPreferences Pipa;

    TextView latitud,longitud;
    TextView direccion;

    public static final String url = "https://monitoreogas.grupopetromar.com/apirest/index.php" ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        latitud = (TextView) findViewById(R.id.txtLatitud);
        longitud = (TextView) findViewById(R.id.txtLongitud);
        direccion = (TextView) findViewById(R.id.txtDireccion);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
        System.out.println("Pipa Seleccionada: --------> "+Pipa);
        Pipa = this.getSharedPreferences("appInfo", Context.MODE_PRIVATE);
        String pipaSelecciona = Pipa.getString("nopipa", "00000");
        if(pipaSelecciona.equals("00000")){
            Intent i = new Intent(MainActivity.this, IngresaPipa.class);
            startActivity(i);
        }else{
           // Toast.makeText(MainActivity.this, pipaSelecciona, Toast.LENGTH_SHORT).show();

        }

        if(!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(this, MyForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            }
        }


        TxtMInicio = (TextView) findViewById(R.id.TxtMInicio);
        TxtMFinal = (TextView) findViewById(R.id.TxtMFinal);
        TxtPrecioActual = (TextView) findViewById(R.id.TxtPrecioActual);

        TxtPorcentaje = (TextView) findViewById(R.id.TxtPorcentaje);
        TxtLitros = (TextView) findViewById(R.id.TxtLitros);
        TxtDinero = (TextView) findViewById(R.id.TxtDinero);
        TxtDineroSugerido = (TextView) findViewById(R.id.TxtDineroSugerido);

        eTXTPrecio = (EditText) findViewById(R.id.eTXTPrecio);

        btnCalcular = (Button) findViewById(R.id.btnCalcular);

        spinnerCapacidad = (Spinner) findViewById(R.id.spinnerCapacidad);
        SbMarcadorinicial = (SeekBar) findViewById(R.id.SbMarcadorinicial);

        SbMarcadorFinal = (SeekBar) findViewById(R.id.SbMarcadorFinal);

        ObtenerPrecio();
        ObtenerCapacidades();

        btnCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // regresar al menu

                TxtPorcentaje.setText(String.valueOf(finalM - inicialM));
                int capacidad = Integer.parseInt(spinnerCapacidad.getSelectedItem().toString());
                double porcentaje = (finalM  - inicialM);
                porcentaje = porcentaje / 100;
                System.out.println(String.valueOf(capacidad)  + "  " + String.valueOf(porcentaje) );
                double litros = capacidad * porcentaje;
              //  DecimalFormat df = new DecimalFormat("#.###");
                litros = Double.parseDouble(String.valueOf(litros));
                TxtLitros.setText(String.valueOf(litros)+ " Litros");
                double total = litros * Double.parseDouble(eTXTPrecio.getText().toString());
                total = Double.parseDouble(String.valueOf(total));
                TxtDinero.setText("$ " +String.valueOf(total)+ " Pesos");



                double totalSugerido = 0;
                double litrosSugerido = 0;
                double residuo = 0;
                residuo = total % 100;
                if(residuo > 40){
                    residuo = 100 - residuo;
                }else{
                    residuo = 50 - residuo;
                }


                totalSugerido = total + residuo;
                litrosSugerido = totalSugerido / Double.parseDouble(String.valueOf(eTXTPrecio.getText()));
                TxtDineroSugerido.setText( "$ " +String.valueOf(totalSugerido) + " (" + String.valueOf(litrosSugerido) + " L" + ")");

            }
        });
        SbMarcadorinicial.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {

            public void onStopTrackingTouch(SeekBar SbMarcadorinicial)
            {
                int value = SbMarcadorinicial.getProgress(); // the value of the seekBar progress
            }

            public void onStartTrackingTouch(SeekBar SbMarcadorinicial)
            {

            }

            public void onProgressChanged(SeekBar SbMarcadorinicial,
                                          int paramInt, boolean paramBoolean)
            {
                inicialM = paramInt;
                TxtMInicio.setText("" + paramInt + "%"); // here in textView the percent will be shown
            }
        });

        SbMarcadorFinal.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {

            public void onStopTrackingTouch(SeekBar SbMarcadorFinal)
            {
                int value = SbMarcadorFinal.getProgress(); // the value of the seekBar progress
            }

            public void onStartTrackingTouch(SeekBar SbMarcadorinicial)
            {

            }

            public void onProgressChanged(SeekBar SbMarcadorFinal,
                                          int paramInt, boolean paramBoolean)
            {

                finalM = paramInt;
                TxtMFinal.setText("" + paramInt + "%"); // here in textView the percent will be shown
            }
        });
        spinnerCapacidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here

                //  ObtenerCapacidades(spinnerCapacidad.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here


            }

        });
/*
        spinnerpipa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here


            }

        });
*/




    }


    private void ObtenerCapacidades() {
        //send request, display a message that nip is incorrect or let it continue to the next step
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String urlCapacidad = "https://monitoreogas.grupopetromar.com/apirest/index.php"; // <----enter your post url here// <----enter your post url here
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, urlCapacidad, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    System.out.print("_____ObtenerDatos______"+response);
                    JSONObject obj = new JSONObject(response);
                    List<String> tanques = new ArrayList<String>();
                    JSONArray cast = obj.getJSONArray("tanques");
                    for (int i = 0; i < cast.length(); i++) {
                        JSONObject cap = cast.getJSONObject(i);
                        tanques.add(cap.getString("capcidad"));
                        System.out.println(tanques+" ---tanques");
                    }
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, tanques);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCapacidad = (Spinner) findViewById(R.id.spinnerCapacidad);
                    spinnerCapacidad.setAdapter(dataAdapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "No se encontraron las capacidades ni las unidades", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {

                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("id", "getTanques");

                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);

    }

    private void ObtenerPrecio() {
        //send request, display a message that nip is incorrect or let it continue to the next step
        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = "https://sistemagas.grupopetromar.com/scripts/obtenerPrecioGPLP.php"; // <----enter your post url here
        StringRequest MyStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);

                   /* String productos = obj.getString("productos");
                    JSONObject obj1 = new JSONObject(productos);
                    String gas = obj1.getString("GAS");
                    JSONObject gasobj = new JSONObject(gas);
                    String preciogas = gasobj.getString("precio");
                    */
                    TxtPrecioActual.setText(obj.getString("precio_venta"));
                    eTXTPrecio.setText(obj.getString("precio_venta"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                //MyData.put("id", "");
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }




    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(MyForegroundService.class.getName().equals(serviceInfo.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(MyForegroundService.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, (LocationListener) Local);


        latitud.setText("Localización agregada");
        direccion.setText("");
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }
    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    direccion.setText(DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;
        public MainActivity getMainActivity() {
            return mainActivity;
        }
        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            String sLatitud = String.valueOf(loc.getLatitude());
            String sLongitud = String.valueOf(loc.getLongitude());
            latitud.setText(sLatitud);
            longitud.setText(sLongitud);
            this.mainActivity.setLocation(loc);
           // ObtenerUbicacionPipa("8", sLatitud, sLongitud);
            System.out.println("SLatitud-"+sLatitud);
            System.out.println("SLongitud-"+sLongitud);


        }
        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            latitud.setText("GPS Desactivado");
        }
        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            latitud.setText("GPS Activado");
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    public void ObtenerUbicacionPipa(final String id_pipa, final String lati, final String longi){
        //send request, display a message that nip is incorrect or let it continue to the next step
        // Instantiate the cache
        //Toast.makeText(context, "enviar datos" , Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, MainActivity.url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println("--------response MainActivity"+response);
                    System.out.println("latitud "+lati);
                    System.out.println("longitud "+longi);

                } catch (Exception e) {
                  //  Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                System.out.println("HANDLE ERROR MAIN ACTIVITY----"+error);
              //  Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("id", "coordenadasPipas");
                MyData.put("id_pipa", id_pipa);
                MyData.put("lati", lati);
                MyData.put("longi", longi);
                return MyData;
            }
        };
        queue.add(MyStringRequest);
    }

}