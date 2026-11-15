package com.upiiz.dm_jlgdc_08;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.upiiz.dm_jlgdc_08.models.Cliente;
import com.upiiz.dm_jlgdc_08.models.RespuestaClientes;
import com.upiiz.dm_jlgdc_08.models.RespuestaEliminarCliente;
import com.upiiz.dm_jlgdc_08.network.ClienteRetrofit;
import com.upiiz.dm_jlgdc_08.network.api.ApiCliente;
import com.upiiz.dm_jlgdc_08.views.FormularioClienteActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ApiCliente apiCliente;
    private ListView listViewClientes;
    private FloatingActionButton fabAgregar;
    private List<Cliente> listaClientes = new ArrayList<>();
    private ArrayAdapter<Cliente> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular vistas
        listViewClientes = findViewById(R.id.listViewClientes);
        fabAgregar = findViewById(R.id.fabAgregar);

        // Inicializar adaptador
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaClientes);
        listViewClientes.setAdapter(adapter);

        // Instancia Retrofit
        apiCliente = ClienteRetrofit.obtenerApiCliente();

        // 1. BOTÓN AGREGAR (CREATE) -> Abre la actividad del formulario vacío
        fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormularioClienteActivity.class);
            startActivity(intent);
        });

        // 2. CLICK NORMAL EN UN ITEM -> Actualizar cliente (UPDATE)
        listViewClientes.setOnItemClickListener((parent, view, position, id) -> {
            Cliente clienteSeleccionado = listaClientes.get(position);
            Intent intent = new Intent(MainActivity.this, FormularioClienteActivity.class);
            // Mandamos los datos actuales para rellenar el formulario de edición
            intent.putExtra("id", clienteSeleccionado.getId());
            intent.putExtra("nombre", clienteSeleccionado.getNombre());
            intent.putExtra("correo", clienteSeleccionado.getCorreo());
            intent.putExtra("telefono", clienteSeleccionado.getTelefono());
            intent.putExtra("direccion", clienteSeleccionado.getDireccion());
            intent.putExtra("registrado", clienteSeleccionado.isRegistrado());
            startActivity(intent);
        });

        // 3. CLICK LARGO EN UN ITEM -> Eliminar cliente (DELETE)
        listViewClientes.setOnItemLongClickListener((parent, view, position, id) -> {
            Cliente clienteSeleccionado = listaClientes.get(position);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Eliminar Cliente")
                    .setMessage("¿Deseas eliminar a " + clienteSeleccionado.getNombre() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> eliminarCliente(clienteSeleccionado.getId()))
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();

            return true; // Indica que el evento de long click fue consumido
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar el listado automáticamente cada vez que regresemos a la pantalla principal
        obtenerClientes();
    }

    // MÉTODO READ (Listar)
    public void obtenerClientes() {
        apiCliente.obtenerClientes().enqueue(new Callback<RespuestaClientes>() {
            @Override
            public void onResponse(Call<RespuestaClientes> call, Response<RespuestaClientes> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Cliente> clientesServidor = response.body().getDatos().getContent();
                    if (clientesServidor != null) {
                        listaClientes.clear();
                        listaClientes.addAll(clientesServidor);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No se pudieron cargar los clientes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaClientes> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // MÉTODO DELETE (Eliminar de la API)
    private void eliminarCliente(Long id) {
        apiCliente.eliminarCliente(id).enqueue(new Callback<RespuestaEliminarCliente>() {
            @Override
            public void onResponse(Call<RespuestaEliminarCliente> call, Response<RespuestaEliminarCliente> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Cliente eliminado con éxito", Toast.LENGTH_SHORT).show();
                    obtenerClientes(); // Refrescar lista
                } else {
                    Toast.makeText(MainActivity.this, "Error al eliminar el cliente", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaEliminarCliente> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}