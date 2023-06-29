package com.example.visiteur;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.visiteur.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {

    private ListView listView;
    private List<String> dataList;
    private List<Integer> dataIds;
    private DatabaseHelper databaseHelper;
    private int totalCost;
    private int minCost;
    private int maxCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        listView = findViewById(R.id.listView);
        databaseHelper = new DatabaseHelper(this);

        dataList = new ArrayList<>();
        dataIds = new ArrayList<>();

        // Afficher les données dans la ListView
        showDataInListView();

        // Enregistrer le menu contextuel pour la ListView
        registerForContextMenu(listView);
    }

    private void fetchDataFromDatabase() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {"id", "nom", "nbJour", "tarifJour"};
        Cursor cursor = db.query("visiteur", projection, null, null, null, null, null);

        totalCost = 0;
        minCost = Integer.MAX_VALUE;
        maxCost = Integer.MIN_VALUE;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
                int nbJour = cursor.getInt(cursor.getColumnIndexOrThrow("nbJour"));
                int tarifJour = cursor.getInt(cursor.getColumnIndexOrThrow("tarifJour"));
                int tarif = nbJour * tarifJour;
                totalCost += tarif;

                if (tarif < minCost) {
                    minCost = tarif;
                }

                if (tarif > maxCost) {
                    maxCost = tarif;
                }

                String data = "Nom: " + nom + ", NbJour: " + nbJour + ", TarifJour: " + tarifJour + ", Tarif: " + tarif;
                dataList.add(data);
                dataIds.add(id);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
    }

    private void showDataInListView() {
        dataList.clear();
        dataIds.clear();

        // Récupérer les données de la base de données
        fetchDataFromDatabase();

        // Ajoutez le coût total, minimal et maximal en bas du tableau
        String total = "Coût total: " + totalCost;
        String min = "Coût minimal: " + minCost;
        String max = "Coût maximal: " + maxCost;

        dataList.add(total);
        dataList.add(min);
        dataList.add(max);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.listView) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position = info.position;

            menu.setHeaderTitle("Options");
            menu.add(0, position, 0, "Modifier");
            menu.add(0, position, 0, "Supprimer");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getItemId();

        if (item.getTitle().equals("Modifier")) {
            // Récupérer l'ID de l'enregistrement à modifier
            int id = dataIds.get(position);

            // Récupérer les détails de l'enregistrement
            fetchRecordById(id);
        } else if (item.getTitle().equals("Supprimer")) {
            showConfirmationDialog(position);
        }

        return super.onContextItemSelected(item);
    }

    private void fetchRecordById(int id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {"id", "nom", "nbJour", "tarifJour"};
        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query("visiteur", projection, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
            int nbJour = cursor.getInt(cursor.getColumnIndexOrThrow("nbJour"));
            int tarifJour = cursor.getInt(cursor.getColumnIndexOrThrow("tarifJour"));

            showEditDialog(id, nom, nbJour, tarifJour);
        }

        cursor.close();
        db.close();
    }

    private void showEditDialog(final int id, String nom, int nbJour, int tarifJour) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modifier l'enregistrement");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        builder.setView(view);

        final EditText editTextNom = view.findViewById(R.id.editTextNom);
        final EditText editTextNbJour = view.findViewById(R.id.editTextNbJour);
        final EditText editTextTarifJour = view.findViewById(R.id.editTextTarifJour);

        editTextNom.setText(nom);
        editTextNbJour.setText(String.valueOf(nbJour));
        editTextTarifJour.setText(String.valueOf(tarifJour));

        builder.setPositiveButton("Enregistrer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newNom = editTextNom.getText().toString().trim();
                int newNbJour = Integer.parseInt(editTextNbJour.getText().toString().trim());
                int newTarifJour = Integer.parseInt(editTextTarifJour.getText().toString().trim());

                updateRecord(id, newNom, newNbJour, newTarifJour);
            }
        });

        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void updateRecord(int id, String nom, int nbJour, int tarifJour) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        ContentValues values = new ContentValues();
        values.put("nom", nom);
        values.put("nbJour", nbJour);
        values.put("tarifJour", tarifJour);

        int updatedRows = db.update("visiteur", values, selection, selectionArgs);

        if (updatedRows > 0) {
            Toast.makeText(this, "Enregistrement modifié avec succès", Toast.LENGTH_SHORT).show();
            showDataInListView();
        } else {
            Toast.makeText(this, "Échec de la modification de l'enregistrement", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    private void showConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation de suppression");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer cet enregistrement ?");

        builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRecord(position);
            }
        });

        builder.setNegativeButton("Non", null);
        builder.create().show();
    }

    private void deleteRecord(int position) {
        int id = dataIds.get(position);

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(id)};

        int deletedRows = db.delete("visiteur", selection, selectionArgs);

        if (deletedRows > 0) {
            Toast.makeText(this, "Enregistrement supprimé avec succès", Toast.LENGTH_SHORT).show();
            showDataInListView();
        } else {
            Toast.makeText(this, "Échec de la suppression de l'enregistrement", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }
}
