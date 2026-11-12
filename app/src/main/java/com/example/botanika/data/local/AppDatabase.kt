package com.example.botanika.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LembreteEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun lemebreteDAO(): LembreteDAO

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val TAG = "ROOM_DB_VALIDATION"

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "botanika_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d(TAG, "SUCESSO: Banco de dados Room 'botanika_database' criado com sucesso pela primeira vez!")
                        }

                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "SUCESSO: Banco de dados Room 'botanika_database' aberto e validado.")
                        }
                    })
                    .build()

                INSTANCE = instance
                Log.d(TAG, "Instância do AppDatabase criada/obtida com sucesso.")
                instance
            }
        }
    }
}
