package com.example.android.quiztruefalse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.android.quiztruefalse.kelasobjek.KuisObjek;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Buat_kuis_Activity extends AppCompatActivity {

    public static final String ID_KUIS = "id_kuis";
    public static final String USER_COBA = "Aul.dark";
    public static final String JUMLAH_SOAL = "jumlahSoal";


    EditText editText_namakuis, editText_kodekuis;
    Context mContext;

    //    constant
    private String PERTANYAAN_CHILD = "pertanyaan";
    public static final String REF_TESTKUIS = "testKuis";
    public static final String REF_TESTKUISUSER = "testKuisUser";
    private String id;
    private int jumlahsoal;
    private Switch mOnetime;


    // firbase instance
    DatabaseReference mFirebaseReference;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_kuis_);
        mContext = getApplicationContext();

//        untuk akses databse dan autentikasi firebase
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        //ini buat widget di layout taulah.
        editText_namakuis = (EditText)findViewById(R.id.edit_namakuis);
        editText_kodekuis = (EditText)findViewById(R.id.edit_passwordkuis);
        mOnetime = findViewById(R.id.switchOnetime);

        String[] jumlahSoal = {"5","10","15"};

        //membuat spinner untuk jumlah soal
        Spinner spinner = (Spinner) findViewById(R.id.spinnerjumlahkuis);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.jumlahSoal_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // jadi kalo spinner diklik bakal diambil nilainya
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                jumlahsoal = Integer.parseInt(String.valueOf(adapterView.getItemAtPosition(i)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }


    public String get_pref(String nama_file, String nama_variable){
        SharedPreferences pref = getSharedPreferences(nama_file,MODE_PRIVATE);
        String value = pref.getString(nama_variable, null);
        return value;
    }


    public void testquiz(View view) {
        // kalo berhasil di push maka buat soal
        if(pushKuis()) {
            Intent intent = new Intent(mContext, BuatTestKuis.class);
            intent.putExtra(ID_KUIS, id);
            intent.putExtra(JUMLAH_SOAL, jumlahsoal);
            startActivity(intent);
        }
    }

    public Boolean pushKuis() {
        String namakuis = editText_namakuis.getText().toString().trim();
        String kodeKuis = editText_kodekuis.getText().toString().trim();
        Boolean one = mOnetime.isChecked();
        // akses database ref testkuis
        DatabaseReference refTest = mFirebaseReference.child(REF_TESTKUIS);
        DatabaseReference refTestUser = mFirebaseReference.child(REF_TESTKUISUSER).child(mUser.getUid());

        // mastiin kalo edittext gak kosong
        if (!TextUtils.isEmpty(namakuis) || !TextUtils.isEmpty(kodeKuis)) {

            // ngambil nama user dari email sebelum @
            String pembuat = mUser.getEmail().split("@")[0];
//            push database
            id = refTest.push().getKey();
            //membuat objek kuis
            KuisObjek kuis = new KuisObjek(id, kodeKuis, namakuis, pembuat, one);

            //masukkan kuis ke ref database yang baru di push
            refTest.child(id).setValue(kuis);
            refTestUser.child(id).setValue(kuis);

            //kalo baerhasi maka  true
            Toast.makeText(this, "Quiz added", Toast.LENGTH_LONG).show();

            return true;
        }else {
//            kalo gak berhasil jadi ada yang kosong
            Toast.makeText(mContext, "Isiikan sesuatu",Toast.LENGTH_SHORT ).show();
            return false;
        }


    }
}
