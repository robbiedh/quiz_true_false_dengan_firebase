package com.example.android.quiztruefalse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.android.quiztruefalse.kelasobjek.PertanyaanObjek;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.example.android.quiztruefalse.Buat_kuis_Activity.JUMLAH_SOAL;
import static com.example.android.quiztruefalse.Buat_kuis_Activity.REF_TESTKUIS;
import static com.example.android.quiztruefalse.Buat_kuis_Activity.REF_TESTKUISUSER;

public class BuatTestKuis extends AppCompatActivity {

    public static final String REF_SOAL = "soalKuis";
    //widget
    RadioGroup mpilihanGanda;
    EditText mpertanyaan, mpilihan1,mpilihan2,mpilihan3,mpilihan4;
    Button mjawabsoal, mbuatsoal;
    ArrayList<PertanyaanObjek> mtestObjekArrayList;

//    constant
    private String PERTANYAAN_CHILD = "pertanyaan";
    private String TEST_CHILD = "testKuis";

    String idKuis;
    private int jumlahSoal;


    // firbase instance
    DatabaseReference mFirebaseReference;
    FirebaseUser mUser;

    int iterasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_kuis);
        getSupportActionBar().hide();

        // ngambil jumlah soalnya
        Intent i = getIntent();
        idKuis =  i.getStringExtra(Buat_kuis_Activity.ID_KUIS);
        jumlahSoal = i.getIntExtra(JUMLAH_SOAL,5);

        //widget do layout
        mpilihanGanda = findViewById(R.id.pilihanganda);

        mpertanyaan = findViewById(R.id.pertanyaan);
        mpilihan1 = findViewById(R.id.textpilihan1);
        mpilihan2 = findViewById(R.id.textpilihan2);
        mpilihan3 = findViewById(R.id.textpilihan3);
        mpilihan4 = findViewById(R.id.textpilihan4);
        mbuatsoal = findViewById(R.id.btn_buatsoal);
        mjawabsoal = findViewById(R.id.btn_selesai);

        //akses database
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mtestObjekArrayList = new ArrayList<>();
        iterasi = 0;

//        buat klik next
        mbuatsoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buat_soal();
            }
        });

        mjawabsoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Soal kuis Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    //kalo misal gak jadi buat kuis data-nya dihapus di database
    @Override
    public void onBackPressed() {
        // hapu semisalkan user pencet back
        mFirebaseReference.child(REF_SOAL).child(idKuis).removeValue();
        mFirebaseReference.child(REF_TESTKUIS).child(idKuis).removeValue();
        mFirebaseReference.child(REF_TESTKUISUSER).child(mUser.getUid()).child(idKuis).removeValue();
        super.onBackPressed();
    }

    // ini buat soalnya
    public void buat_soal() {
        //akses database ref soalKuis
        DatabaseReference refSoal = mFirebaseReference.child(REF_SOAL);

//        ngambil text di widget
        String jawab = null;
        String A = mpilihan1.getText().toString();
        String B = mpilihan2.getText().toString();
        String C = mpilihan3.getText().toString();
        String D = mpilihan4.getText().toString();
        String pertanyaan = mpertanyaan.getText().toString();

        // kalo widget-nya terisi semua bakal dipush
        if (!TextUtils.isEmpty(A) && !TextUtils.isEmpty(B) && !TextUtils.isEmpty(C) && !TextUtils.isEmpty(D) && mpilihanGanda.getCheckedRadioButtonId() != -1){
            // ngambil pilihan mana yang benar dari radio
            int selected = mpilihanGanda.getCheckedRadioButtonId();
            switch (selected){
                case R.id.pilihan1:
                    jawab = "A"; break;
                case R.id.pilihan2:
                    jawab = "B"; break;
                case R.id.pilihan3:
                    jawab = "C"; break;
                case R.id.pilihan4:
                    jawab = "D"; break;
            }

            //push soal
            String idSoal =  refSoal.child(idKuis).push().getKey();
            PertanyaanObjek test = new PertanyaanObjek(idSoal, pertanyaan,A,B,C,D,jawab);
            refSoal.child(idKuis).child(idSoal).setValue(test);

//            hapus semua text
            if (iterasi < jumlahSoal-1){
                mpertanyaan.getText().clear();
                mpilihan1.getText().clear();
                mpilihan2.getText().clear();
                mpilihan3.getText().clear();
                mpilihan4.getText().clear();
                mpilihanGanda.clearCheck();
                iterasi ++;
            }
            else  {
                iterasi = 0;
                mbuatsoal.setVisibility(View.GONE);
                mjawabsoal.setVisibility(View.VISIBLE);
            }

        }else {
            Toast.makeText(getApplicationContext(),"Isikan seluruh field", Toast.LENGTH_SHORT).show();
        }
    }
}