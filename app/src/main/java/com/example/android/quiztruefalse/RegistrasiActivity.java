package com.example.android.quiztruefalse;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.quiztruefalse.kelasobjek.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.validation.Validator;

public class RegistrasiActivity extends AppCompatActivity {
    // membuat variable
    EditText etEmail;
    EditText etPassword, etRepassword;
    TextView textView_alert;
    Button btnRegister;
    ProgressDialog loading;
    private FirebaseAuth mAuth;
    DatabaseReference databaseUser;

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrasi);
        //parsing variabel
        etRepassword = (EditText)findViewById(R.id.etRe_Password_registrasi);
        etEmail = (EditText) findViewById(R.id.etEmail_registrasi);
        etPassword = (EditText) findViewById(R.id.etPassword_registrasi);
        textView_alert =(TextView)findViewById(R.id.txtAlert_registrasi);
        btnRegister =(Button)findViewById(R.id.btnRegister2);
        mAuth = FirebaseAuth.getInstance();
        databaseUser = FirebaseDatabase.getInstance().getReference("user");
        dialog_progres();
        // jika button  register di click maka akan menjalankan berikut
        btnRegister.setOnClickListener(
                new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       // if jika bernilai true jika  mtoder validationform retrunt true
                       if (validateForm(etEmail.getText().toString().trim(),etPassword.getText().toString().trim(), etRepassword.getText().toString().trim()) == true) {
                           createAccount(etEmail.getText().toString(), etPassword.getText().toString());
                           // lalu akan menjalankan  metode creatAccount
                       } else {
                           //Menampilkan toast ketika salah
                           Toast.makeText(RegistrasiActivity.this, "Please Fill the Form",
                                   Toast.LENGTH_SHORT).show();
                       }
                   }
               }
        );


    }
    private boolean validateForm(String emial, String password, String Repassword) {
        // kondisi ketika email dan password kosong
        if (emial.isEmpty() || password.isEmpty() || Repassword.isEmpty()) {
            textView_alert.setText("Username Dan password  anda kosong");
            return false;
        }if(password.length()<6){ // password kurang dari  6 karakter
            textView_alert.setText("Password anda kurang dari 6 karakter");
            return false;
        }if (!password.equals(Repassword)){ // password dan re password tidak sama
            textView_alert.setText("password tidak sama");
            return false;
        }if(emial.indexOf("@")==-1){ // email tidak mengadung @
            textView_alert.setText("Email anda tidak valid");
            return false;
        }

        else {
            return true;
        }

    }



    private void createAccount(final String email, String password) {
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false); // tidak ada tombol cancle

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { //jika sukses
                            String id = mAuth.getCurrentUser().getUid();
                            String[] username = email.split("@");
                            User user = new User(id, username[0], email);
                            // databaseUser.child(id).setValue(user);
                            //memindahkan laman
                            progressDialog.dismiss();// progress dialog menghilang
                            Intent i = new Intent(RegistrasiActivity.this, LoginActivity.class);
                            startActivity(i);
                        } else {
                            //kondisi ketika gagal
                            Toast.makeText(RegistrasiActivity.this, "gagal", Toast.LENGTH_SHORT);
                            textView_alert.setText("Gagal Melakukan Pendaftaran"); // jika gagal mendaftar
                        }
                    }
                });
    }


    public void back_toLogin(View view) {
        // tombol kebali ke login
        Intent i = new Intent(RegistrasiActivity.this,LoginActivity.class);
        startActivity(i);
        finish();//menutup activity register
    }

    void  dialog_progres(){
        progressDialog = new ProgressDialog(RegistrasiActivity.this);
        progressDialog.setMessage("Please Wait..."); // Setting Message
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        //  progressDialog.show(); // Display Progress Dialog
        //progressDialog.setCancelable(false);

    }
}
