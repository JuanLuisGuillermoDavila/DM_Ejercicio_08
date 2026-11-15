package com.upiiz.dm_jlgdc_08.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.upiiz.dm_jlgdc_08.R;
import com.upiiz.dm_jlgdc_08.models.PeticionCliente;
import com.upiiz.dm_jlgdc_08.models.RespuestaCliente;
import com.upiiz.dm_jlgdc_08.network.ClienteRetrofit;
import com.upiiz.dm_jlgdc_08.network.api.ApiCliente;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormularioClienteActivity extends AppCompatActivity {

    private EditText etNombre, etCorreo, etTelefono, etDireccion;
    private CheckBox cbRegistrado;
    private Button btnGuardar;

    private ApiCliente apiCliente;

    private Long clienteId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_cliente);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular componentes de la interfaz
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        cbRegistrado = findViewById(R.id.cbRegistrado);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Instancia de la API con Retrofit
        apiCliente = ClienteRetrofit.obtenerApiCliente();

        // Verificar si la actividad recibió datos (Modo Actualización / EDIT)
        if (getIntent().hasExtra("id")) {
            clienteId = getIntent().getLongExtra("id", -1L);
            etNombre.setText(getIntent().getStringExtra("nombre"));
            etCorreo.setText(getIntent().getStringExtra("correo"));
            etTelefono.setText(getIntent().getStringExtra("telefono"));
            etDireccion.setText(getIntent().getStringExtra("direccion"));
            cbRegistrado.setChecked(getIntent().getBooleanExtra("registrado", false));

            btnGuardar.setText("Actualizar Cliente");
        } else {
            btnGuardar.setText("Registrar Cliente");
        }

        // Listener del botón Guardar / Actualizar
        btnGuardar.setOnClickListener(v -> validarYEnviarDatos());
    }

    private void validarYEnviarDatos() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        boolean registrado = cbRegistrado.isChecked();

        // Validaciones básicas de campos vacíos
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de formato de correo electrónico válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Ingrese un correo electrónico válido (ej. usuario@dominio.com)");
            etCorreo.requestFocus();
            return;
        }

        // Crear el objeto de petición para la API
        PeticionCliente peticionCliente = new PeticionCliente(nombre, correo, telefono, direccion, registrado);

        if (clienteId == -1L) {
            // --- OPERACIÓN CREATE (POST) ---
            apiCliente.crearCliente(peticionCliente).enqueue(new Callback<RespuestaCliente>() {
                @Override
                public void onResponse(Call<RespuestaCliente> call, Response<RespuestaCliente> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FormularioClienteActivity.this, "¡Cliente registrado con éxito!", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad y regresa al listado principal
                    } else {
                        Toast.makeText(FormularioClienteActivity.this, "Error al registrar el cliente", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RespuestaCliente> call, Throwable t) {
                    Toast.makeText(FormularioClienteActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // --- OPERACIÓN UPDATE (PUT) ---
            apiCliente.actualizarCliente(clienteId, peticionCliente).enqueue(new Callback<RespuestaCliente>() {
                @Override
                public void onResponse(Call<RespuestaCliente> call, Response<RespuestaCliente> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FormularioClienteActivity.this, "¡Cliente actualizado con éxito!", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra la actividad y regresa al listado principal
                    } else {
                        Toast.makeText(FormularioClienteActivity.this, "Error al actualizar el cliente", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RespuestaCliente> call, Throwable t) {
                    Toast.makeText(FormularioClienteActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}