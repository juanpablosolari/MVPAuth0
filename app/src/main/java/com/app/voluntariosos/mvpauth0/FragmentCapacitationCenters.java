package com.app.voluntariosos.mvpauth0;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

public class FragmentCapacitationCenters extends Fragment {

    public ListView centersList;
    public static CapacitationCentersAdapter arrayAdapter;
    private ListView lstOpciones;
    public static SharedPreferences prefs;

    private static ApiSrv ApiSrv = new ApiSrv();

    private ArrayList<CapacitationCenterItem> datos = new ArrayList<CapacitationCenterItem>();

    public FragmentCapacitationCenters() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle("Cargando Centros...");
        dialog.setMessage("Aguarde unos segundos...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        long delayInMillis = 1500;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, delayInMillis);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capacitation_centers, container, false);
    }


    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        prefs = this.getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        arrayAdapter = new CapacitationCentersAdapter(this, datos);
        centersList = (ListView)getView().findViewById(R.id.centersList);
        centersList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final CapacitationCenterItem item = ((CapacitationCenterItem) parent.getItemAtPosition(position));
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("capacitationCenterId", item.getId());
                editor.commit();

                FragmentCapacitationCenter fragment = new FragmentCapacitationCenter();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack("");
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.commit();
                MainActivity.setCurrentFragment(fragment);
            }
        });
        centersList.setAdapter(arrayAdapter);
        centersList.setItemsCanFocus(false);
        centersList.setFocusable(false);

        ApiSrv.getCapacitationCenters(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                Log.d("Centers", responseBody.toString());
                if (arrayAdapter != null) {
                    arrayAdapter.clear();
                }

                for (int i = 0; i < responseBody.length(); i++ ) {
                    try {
                        JSONObject item = responseBody.getJSONObject(i);
                        if (arrayAdapter != null) {
                            arrayAdapter.add(new CapacitationCenterItem(item));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("Centers",  "failure: " + responseString);
                Log.e("Centers",  "failurecode: " + statusCode);
            }
        });
    }

    class CapacitationCentersAdapter extends ArrayAdapter<CapacitationCenterItem> {

        Activity context;

        public CapacitationCentersAdapter(Fragment context, ArrayList<CapacitationCenterItem> datos) {
            super(context.getActivity(), R.layout.item_capacitation_center, datos);
            this.context = context.getActivity();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(R.layout.item_capacitation_center, null);

            TextView lblTitulo = (TextView)item.findViewById(R.id.title);
            lblTitulo.setText(datos.get(position).getTitle());

            TextView lblSubtitulo = (TextView)item.findViewById(R.id.body);
            lblSubtitulo.setText(datos.get(position).getBody());

            return(item);
        }
    }
}
