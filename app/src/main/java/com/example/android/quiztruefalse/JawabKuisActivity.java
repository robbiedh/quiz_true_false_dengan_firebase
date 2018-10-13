package com.example.android.quiztruefalse;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.quiztruefalse.kelasobjek.PertanyaanObjek;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;

import static com.example.android.quiztruefalse.BuatTestKuis.REF_SOAL;
import static com.example.android.quiztruefalse.ListKuisActivity.KUIS_ID;
import static com.example.android.quiztruefalse.ListKuisActivity.NAMA_KUIS;
import static com.example.android.quiztruefalse.ListKuisActivity.ONE_TIME;

public class JawabKuisActivity extends AppCompatActivity {

    public static final String SOALKUIS = "soalkuis";
    public static final String SOALBENAR = "soalbenar";
    public static final String STR_IDKUIS = "idkuis";
    public static final String ARRAYLISTSOAL = "arraylistsoal";
    RadioGroup mpilihanGanda;
    TextView mpertanyaan;
    RadioButton mpilihan1,mpilihan2,mpilihan3,mpilihan4;
    Button msoalPrev, msoalNext, msoalSelesai;
    DonutProgress donutProgress;
    Spinner spinner;
    CountDownTimer countDownTimer;

    ArrayList<PertanyaanObjek> listPertanyaan;
    String[] jawabanSoal;
    String[] jawabanUser;

    //    constant
    private String PERTANYAAN_CHILD = "pertanyaan";
    private String TEST_CHILD = "testKuis";
    // firbase instance
    DatabaseReference mFirebaseReference;
    DatabaseReference refKuis;

    private String idKuis;

    int iterasi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jawab_kuis);
        getSupportActionBar().hide();

        //ngambil id kuis
        Intent i = getIntent();
        idKuis =  i.getStringExtra(KUIS_ID);

//        widget layout
        mpilihanGanda = findViewById(R.id.pilihanGanda);

        mpertanyaan = findViewById(R.id.textPertanyaan);
        mpilihan1 = (RadioButton) findViewById(R.id.Pilihan1);
        mpilihan2 = findViewById(R.id.Pilihan2);
        mpilihan3 = findViewById(R.id.Pilihan3);
        mpilihan4 = findViewById(R.id.Pilihan4);
        spinner = findViewById(R.id.spinnerjumlahkuis2);


        //buat progress waktu
        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
        donutProgress.setMax(100);
        donutProgress.setFinishedStrokeColor(Color.parseColor("#FFFB385F"));
        donutProgress.setTextColor(Color.parseColor("#FFFB385F"));
        donutProgress.setKeepScreenOn(true);


//        set reference firebase
        mFirebaseReference = FirebaseDatabase.getInstance().getReference();
        refKuis = mFirebaseReference.child(REF_SOAL).child(idKuis); //ref_soal = soalKuis

        msoalNext = findViewById(R.id.btn_next);
        msoalPrev = findViewById(R.id.btn_before);
        msoalSelesai = findViewById(R.id.btn_selesai);
        listPertanyaan = new ArrayList<>();
        iterasi = 0;

        msoalNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSoal(1, -1);
            }
        });
        msoalPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSoal(-1, -1);
            }
        });
        msoalSelesai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.onFinish();
                finish();
            }
        });

        mpilihanGanda.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int selected = mpilihanGanda.getCheckedRadioButtonId();
                String jawab = null;
                switch (selected){
                    case R.id.Pilihan1:
                        jawab = "A"; enabledButton(); break;
                    case R.id.Pilihan2:
                        jawab = "B"; enabledButton();break;
                    case R.id.Pilihan3:
                        jawab = "C"; enabledButton();break;
                    case R.id.Pilihan4:
                        jawab = "D"; enabledButton();break;
                }

                jawabanUser[iterasi] = jawab;
                Log.v("jawaban",String.valueOf(jawabanUser[iterasi]));
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String soal = String.valueOf(adapterView.getItemAtPosition(i)).split(" ")[1];
                updateSoal(0, (Integer.parseInt(soal))-1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        buat ngambil list soal kuis di databae dengan id
        refKuis.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listPertanyaan.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //nagambil data pertnayaan
                    PertanyaanObjek pertanyaan = postSnapshot.getValue(PertanyaanObjek.class);
                    //masukkan ke arraylisy
                    listPertanyaan.add(pertanyaan);
                }
                int jumlahSoal = listPertanyaan.size();
                jawabanUser = new String[jumlahSoal];
                jawabanSoal = new String[jumlahSoal];
                for (int i = 0; i < listPertanyaan.size(); i++){
                    jawabanSoal[i] = listPertanyaan.get(i).getJawaban();
                }
                showSpinner(jumlahSoal);
                showProgressDonut(jumlahSoal);
                updateSoal(0,-1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        countDownTimer.cancel();
        finish();
        super.onBackPressed();
    }

    private void showSpinner(int jumlah) {
        String[] list = new String[jumlah];
        for (int i = 0; i<jumlah;i++){
            list[i] = "Soal "+String.valueOf(i+1);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    public void updateSoal(int iter, int nomorSoal) {
        //kalo dia klik next/before bakalan ke soal selanjutnya
        // kalo pilih nomor lain dari spinner maka ke nomor tersebut
        if (nomorSoal == -1){
            iterasi += iter;
        }else iterasi = nomorSoal;

        //kalo soal awal button before dihilangin
        // soal terakhir button next dihilangin
        if (iterasi < listPertanyaan.size()-1 && iterasi!=0){
            msoalNext.setVisibility(View.VISIBLE);
            msoalPrev.setVisibility(View.VISIBLE);
        }
        else if (iterasi == 0){
            msoalPrev.setVisibility(View.INVISIBLE);
        }else {
            msoalNext.setVisibility(View.INVISIBLE);
            msoalPrev.setVisibility(View.VISIBLE);
            msoalSelesai.setVisibility(View.VISIBLE);
        }

        PertanyaanObjek pertanyaanObjek = listPertanyaan.get(iterasi);
        //set text
        mpertanyaan.setText(pertanyaanObjek.getPertanyaan());
        mpilihan1.setText(pertanyaanObjek.getPilihanA());
        mpilihan2.setText(pertanyaanObjek.getPilihanB());
        mpilihan3.setText(pertanyaanObjek.getPilihanC());
        mpilihan4.setText(pertanyaanObjek.getPilihanD());

        spinner.setSelection(iterasi);
        ClearRadio(iterasi);
    }


    // hapus yang checknya
    private void ClearRadio( int index) {
        if (jawabanUser[index]!= null) {
            switch (jawabanUser[index]) {
                case "A":
                    mpilihan1.setChecked(true);
                    break;
                case "B":
                    mpilihan2.setChecked(true);
                    break;
                case "C":
                    mpilihan3.setChecked(true);
                    break;
                case "D":
                    mpilihan4.setChecked(true);
                    break;
            }
        }else {
            mpilihanGanda.clearCheck();
            msoalNext.setEnabled(false);
            msoalPrev.setEnabled(false);
        }
    }

    private void enabledButton() {
        msoalNext.setEnabled(true);
        msoalPrev.setEnabled(true);
    }


    public void showProgressDonut(int jumlahSoal){
        final int time, countdown;
//        set waktu 5 soal 2 menit
        switch (jumlahSoal){
            case 5:
                time = 120000;countdown=1200;break;
            case 10:
                time = 240000;countdown=2400;break;
            case 15:
                time = 360000;countdown=3600;break;
            case 20:
                time = 480000;countdown=4800;break;
            default:
                time = 120000;countdown=1200;break;
        }
//        jalankan timer
        countDownTimer =
        new CountDownTimer(time,countdown){
            int i = 100;
            @Override
            public void onTick(long l) {
                donutProgress.setProgress(i--);
            }

//            jika selesai ke result
            @Override
            public void onFinish() {
                Boolean one =getIntent().getBooleanExtra(ONE_TIME, false);
                Intent result = new Intent(getApplicationContext(),ResultActivity.class);
                result.putExtra(SOALKUIS, jawabanSoal);
                result.putExtra(SOALBENAR, jawabanUser);
                result.putExtra(STR_IDKUIS, idKuis);
                result.putExtra(NAMA_KUIS, getIntent().getStringExtra(NAMA_KUIS));
                result.putExtra(ARRAYLISTSOAL, listPertanyaan);
                result.putExtra(ONE_TIME, one);
                startActivity(result);
                countDownTimer.cancel();
                finish();
            }
        };
        countDownTimer.start();
    }
}
