package mx.edu.ittepic.ladm_u4_p1_almacensms_18401126_18401186

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import mx.edu.ittepic.ladm_u4_p1_almacensms_18401126_18401186.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    val siPermiso = 1
    var listaIds = ArrayList<String>()
    val manager = SmsManager.getDefault()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cargarDatos()
        binding.btnEnviar.setOnClickListener {
            if(binding.numCelular.text.toString().equals("")){
                AlertDialog.Builder(this)
                    .setTitle("ATENCION")
                    .setMessage("CAMPO NUM CELULAR VACIO")
                    .show()
            }else if(binding.mensajeCelular.text.toString().equals("")){
                AlertDialog.Builder(this)
                    .setTitle("ATENCION")
                    .setMessage("CAMPO MENSAJE SMS VACIO")
                    .show()
            }
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.SEND_SMS
                ),siPermiso)
            }
            else{
                enviarSMS(this)
            }
        }
    }
    fun cargarDatos(){
        FirebaseFirestore.getInstance()
            .collection("smsenviados")
            .addSnapshotListener { value, error ->
                if(error!=null){
                    AlertDialog.Builder(this)
                        .setTitle("ATENCION")
                        .setMessage("¡ERROR! No se pudo consultar")
                        .setPositiveButton("OK"){d,i->}
                        .show()
                    return@addSnapshotListener
                }
                var lista = ArrayList<String>()
                listaIds.clear()
                for (documento in value!!) {
                    val cadena = documento.getString("telefono") + "\n" +documento.get("mensaje")
                    lista.add(cadena)
                    listaIds.add(documento.id)
                    mostrarLista(lista)
                }
            }
    }
    fun mostrarLista(datos: ArrayList<String>) {
        binding.listaMensajes.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datos)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == siPermiso){
            enviarSMS(this)
        }
    }
    fun enviarSMS(context: Context) {
        manager.sendTextMessage(binding.numCelular.text.toString(),null,binding.mensajeCelular.text.toString(),null,null)
        Toast.makeText(this,"Se envio el mensaje.",Toast.LENGTH_LONG).show()

        var datos = hashMapOf(
            "telefono" to binding.numCelular.text.toString(),
            "mensaje" to binding.mensajeCelular.text.toString(),
            "fechayhora" to Date()
        )
        FirebaseFirestore.getInstance()
            .collection("smsenviados")
            .add(datos)
            .addOnSuccessListener {
                Toast.makeText(this,"Se inserto en Firebase.",Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this,it.message,Toast.LENGTH_LONG).show()
            }
        binding.numCelular.text.clear()
        binding.mensajeCelular.text.clear()
    }//EnviarSms
}//Class