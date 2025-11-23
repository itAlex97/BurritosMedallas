package com.gutierrez_rodriguez.burritosmedallas;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gutierrez_rodriguez.burritosmedallas.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    // 1. NECESITAMOS FIRESTORE AQUÍ TAMBIÉN PARA LEER EL ROL
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. CAMBIO EN EL CHEQUEO AUTOMÁTICO
        // Si ya estaba logueado, hay que verificar su rol de nuevo antes de dejarlo pasar.
        if (mAuth.getCurrentUser() != null) {
            checkRoleAndGoToMenu(mAuth.getCurrentUser().getUid());
        }

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Login con Auth (Correo y Contraseña)
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // 3. SI EL LOGIN ES CORRECTO, AHORA BUSCAMOS EL ROL EN FIRESTORE
                    String userId = mAuth.getCurrentUser().getUid();
                    checkRoleAndGoToMenu(userId);
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.error_login_failed) + ": " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // NUEVO MÉTODO: Busca el rol y abre el menú
    private void checkRoleAndGoToMenu(String userId) {
        Toast.makeText(this, "Verificando permisos...", Toast.LENGTH_SHORT).show();

        // Leemos el documento del usuario en la colección "users"
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        // Obtenemos el campo "role". Si no existe, asumimos que es "user".
                        String role = documentSnapshot.getString("role");
                        if (role == null) {
                            role = "user";
                        }

                        // Pasamos el rol a la siguiente actividad
                        goToMenu(role);
                    }
                })
                .addOnFailureListener(e -> {
                    // Si falla la lectura, lo dejamos pasar como usuario normal por seguridad
                    Toast.makeText(LoginActivity.this, "Error al verificar rol, entrando como invitado.", Toast.LENGTH_SHORT).show();
                    goToMenu("user");
                });
    }

    // MODIFICADO: Ahora recibe el rol y lo mete en la "mochila" del Intent
    private void goToMenu(String role) {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        intent.putExtra("USER_ROLE", role); // <--- ¡AQUÍ PASAMOS EL DATO CLAVE!
        startActivity(intent);
        finish();
    }
}