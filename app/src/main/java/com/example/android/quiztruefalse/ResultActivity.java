package com.example.android.quiztruefalse;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.quiztruefalse.Adapter.ScoreLists;
import com.example.android.quiztruefalse.kelasobjek.ScoreObjek;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import static com.example.android.quiztruefalse.JawabKuisActivity.ARRAYLISTSOAL;
import static com.example.android.quiztruefalse.JawabKuisActivity.SOALBENAR;
import static com.example.android.quiztruefalse.JawabKuisActivity.SOALKUIS;
import static com.example.android.quiztruefalse.JawabKuisActivity.STR_IDKUIS;
import static com.example.android.quiztruefalse.ListKuisActivity.KUIS_ID;
import static com.example.android.quiztruefalse.ListKuisActivity.NAMA_KUIS;
import static com.example.android.quiztruefalse.ListKuisActivity.ONE_TIME;

public class ResultActivity extends AppCompatActivity {

    public static final String REF_SCORE_KUIS = "scoreKuis";
    public static final String REF_SCORE_USER = "scoreUser";
    private TextView mscore, msoal, mUser,mbenar;
    private Button mhide, mcekactivity;
    private LinearLayout mlinearLayout;


    String idKuis, namaUser;
    ArrayList<ScoreObjek> scoreObjeks;
    private ListView listViewscore;

    FirebaseDatabase mFireDatabase;
    DatabaseReference refRoot;
    FirebaseUser mCurrentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //widget
        Button dissmiss = (Button) findViewById(R.id.btn_dismiss_score);
        dissmiss.setVisibility(View.GONE);
        mhide = findViewById(R.id.hideScore);
        mcekactivity = findViewById(R.id.cekJawaban);

        mlinearLayout = findViewById(R.id.scoreUser);

        mUser = findViewById(R.id.you);
        mbenar = findViewById(R.id.correct);
        mscore =findViewById(R.id.score);
        msoal = findViewById(R.id.attempted);
        listViewscore = findViewById(R.id.list_score);


//        akses database dan autentikasi
        mFireDatabase = FirebaseDatabase.getInstance();
        refRoot = mFireDatabase.getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

//        buat list
        scoreObjeks = new ArrayList<>();

//        ngambil id dan jawaban bener
        final Intent intent = getIntent();
        String[] jawabanSoal = intent.getStringArrayExtra(SOALKUIS);
        final String[] jawabanUser = intent.getStringArrayExtra(SOALBENAR);
        idKuis = intent.getStringExtra(STR_IDKUIS);
        namaUser = mCurrentUser.getEmail().split("@")[0];


        // ngitung jawaban bener
        int soalBenar = 0;
        int i = 0;
        for (String soal : jawabanSoal){
            if (TextUtils.equals(soal,jawabanUser[i++])){
                soalBenar += 1;
            }
        }
        final int scoreAkhir = 100* soalBenar/i;

        //set text
        listViewscore.setVisibility(View.INVISIBLE);
        if (!intent.getBooleanExtra(ONE_TIME, false))mcekactivity.setVisibility(View.GONE);

        mscore.setText(String.valueOf(scoreAkhir));
        msoal.setText(String.valueOf(i));
        mbenar.setText(String.valueOf(soalBenar));
        mUser.setText(namaUser);


        // akses ref scoreKuis dan scoreUser untuk dipush
        final DatabaseReference refscore = refRoot.child(REF_SCORE_KUIS).child(idKuis);
        final DatabaseReference refscoreUser = refRoot.child(REF_SCORE_USER).child(mCurrentUser.getUid()).child(idKuis);

//        nampilin/ngilanign score
        mhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listViewscore.getVisibility()==View.INVISIBLE) {
                    mlinearLayout.setVisibility(View.GONE);
                    listViewscore.setVisibility(View.VISIBLE);
                    mhide.setText("Hide List");
                }else {
                    mlinearLayout.setVisibility(View.VISIBLE);
                    listViewscore.setVisibility(View.INVISIBLE);
                    mhide.setText("Show List");
                }
            }
        });

        //tombol cek jawaban
        mcekactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cekJawaban = new Intent(getApplicationContext(),CekJawabanActivity.class);
                cekJawaban.putExtra(SOALBENAR, jawabanUser);
                cekJawaban.putExtra(ARRAYLISTSOAL, intent.getSerializableExtra(ARRAYLISTSOAL));
                cekJawaban.putExtra(KUIS_ID, idKuis);
                cekJawaban.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityIfNeeded(cekJawaban, 0);
            }
        });

//        /negecek apakah nilai sudah ada di databse atau blm
        refscoreUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                ?jika tidak adad buat baru
                if (!dataSnapshot.exists()){
                    String idScore = refscore.push().getKey();

                    ScoreObjek score = new ScoreObjek(idScore,idKuis,getIntent().getStringExtra(NAMA_KUIS),mCurrentUser.getUid(),
                            mCurrentUser.getEmail().split("@")[0],scoreAkhir);

                    refscore.child(idScore).setValue(score);
                    refscoreUser.setValue(score);
//                    jika ada cekkalo cuma sekali aja
                }else if (!intent.getBooleanExtra(ONE_TIME, false)){
                    ScoreObjek score = dataSnapshot.getValue(ScoreObjek.class);
                    score.setScore(scoreAkhir);
                    refscore.child(score.getId()).setValue(score);
                    refscoreUser.setValue(score);
                }
//                jika ada terus soalnya cuma one-time maka nilai tidak diubah
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference refScore = refRoot.child(REF_SCORE_KUIS).child(idKuis);
        // nagmbil daftar nilai kuis
        refScore.orderByChild("score").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                scoreObjeks.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    ScoreObjek score = data.getValue(ScoreObjek.class);
                    scoreObjeks.add(score);
                }
                Collections.reverse(scoreObjeks);

                ScoreLists scoreAdapter = new ScoreLists(ResultActivity.this, scoreObjeks);
                listViewscore.setAdapter(scoreAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
