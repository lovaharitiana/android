package com.example.visiteur;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "androidvisiteur.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
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
