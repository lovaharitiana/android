package com.example.visiteur;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNom;
    private EditText editTextNbJour;
    private EditText editTextTarifJournalier;
    private Button btnAjouter;
    private DatabaseHelper databaseHelper;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupérer les références des éléments de l'interface utilisateur
        editTextNom = findViewById(R.id.editTextNom);
        editTextNbJour = findViewById(R.id.editTextNombreJour);
        editTextTarifJournalier = findViewById(R.id.editTextTarifJournalier);
        btnAjouter = findViewById(R.id.btnAjouter);
        listView = findViewById(R.id.listView);

        // Créer une instance de DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Définir l'action à effectuer lors du clic sur le bouton "Ajouter"
        btnAjouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Récupérer les valeurs saisies par l'utilisateur
                String nom = editTextNom.getText().toString();
                int nbJour = Integer.parseInt(editTextNbJour.getText().toString());
                int tarifJour = Integer.parseInt(editTextTarifJournalier.getText().toString());

                // Insérer les données dans la base de données
                insertData(nom, nbJour, tarifJour);

                // Lancer l'activité ListViewActivity
                Intent intent = new Intent(MainActivity.this, ListViewActivity.class);
                startActivity(intent);
            }
        });



    }

    private void displayData() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {"id", "nom", "nbJour", "tarifJour"};

        Cursor cursor = db.query(
                "visiteur",
                projection,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
                int nbJour = cursor.getInt(cursor.getColumnIndexOrThrow("nbJour"));
                int tarifJour = cursor.getInt(cursor.getColumnIndexOrThrow("tarifJour"));

                Log.d("Data", "ID: " + id + ", Nom: " + nom + ", NbJour: " + nbJour + ", TarifJour: " + tarifJour);
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void insertData(String nom, int nbJour, int tarifJour) {
        new InsertDataAsyncTask().execute(nom, String.valueOf(nbJour), String.valueOf(tarifJour));
    }

    private void populateListView() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {"id", "nom", "nbJour", "tarifJour"};

        Cursor cursor = db.query(
                "visiteur",
                projection,
                null,
                null,
                null,
                null,
                null
        );

        List<String> dataList = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
                int nbJour = cursor.getInt(cursor.getColumnIndexOrThrow("nbJour"));
                int tarifJour = cursor.getInt(cursor.getColumnIndexOrThrow("tarifJour"));

                String data = "ID: " + id + ", Nom: " + nom + ", NbJour: " + nbJour + ", TarifJour: " + tarifJour;
                dataList.add(data);
            } while (cursor.moveToNext());

            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
    }

    private class InsertDataAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String nom = params[0];
            int nbJour = Integer.parseInt(params[1]);
            int tarifJour = Integer.parseInt(params[2]);

            // Obtenir une référence à la base de données en mode écriture
            SQLiteDatabase db = databaseHelper.getWritableDatabase();

            // Créer un objet ContentValues pour stocker les valeurs à insérer
            ContentValues values = new ContentValues();
            values.put("nom", nom);
            values.put("nbJour", nbJour);
            values.put("tarifJour", tarifJour);

            // Insérer les valeurs dans la table "visiteur"
            long rowId = db.insert("visiteur", null, values);

            // Fermer la base de données
            db.close();

            return rowId != -1;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d("Success", "Data inserted successfully");
            } else {
                Log.d("Error", "Erreur d'ajout du visiteur");
            }
        }
    }

    // Classe interne DatabaseHelper pour gérer la création et la mise à jour de la base de données
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "androidvisiteur.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(MainActivity context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Créer la table "visiteur" avec les colonnes appropriées
            String createTableQuery = "CREATE TABLE visiteur (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nom TEXT," +
                    "nbJour INTEGER," +
                    "tarifJour INTEGER)";
            db.execSQL(createTableQuery);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Si une version antérieure de la base de données existe, la mettre à jour
            // Ici, vous pouvez gérer la mise à jour de la structure de la table ou d'autres modifications nécessaires
            db.execSQL("DROP TABLE IF EXISTS visiteur");
            onCreate(db);
        }
    }
}