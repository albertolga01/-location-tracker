package com.grupopetromar.mylocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IngresaPipa extends AppCompatActivity {

    private SharedPreferences Pipa;
    public static String noPipa;
    private Spinner  spinnerPipa;
    Button btnGuardar;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecciona_pipa);

        spinnerPipa = (Spinner) findViewById(R.id.spinnerPipa);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        Pipa = getApplicationContext().getSharedPreferences("appInfo", Context.MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            noPipa = bundle.getString("pipa");

        }

        String[] unidades = {"1", "2", "3", "4", "5", "6", "7", "8","9"};
        spinnerPipa.setAdapter(new ArrayAdapter<String>(IngresaPipa.this, android.R.layout.simple_spinner_dropdown_item, unidades));

        //String unidad = (String) spinnerPipa.getSelectedItem();

        spinnerPipa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String unidad = (String) spinnerPipa.getSelectedItem();

                noPipa = unidad;
                System.out.println("Pipa Seleccionada"+unidad);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No seleccionaron nada
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(IngresaPipa.this, "Okay", Toast.LENGTH_SHORT).show();

                System.out.println("Pipa Seleccionada Preferencia"+noPipa);
                SharedPreferences.Editor editor = Pipa.edit();
                editor.putString("nopipa", spinnerPipa.getSelectedItem().toString());
                editor.apply();
                editor.commit();
                Toast.makeText(IngresaPipa.this, "Pipa actualizada correctamente", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(IngresaPipa.this, MainActivity.class);
                 startActivity( i);

            }
        });

    }



}
