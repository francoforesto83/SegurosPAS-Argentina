package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import com.example.utils.DemoDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Cliente::class,
        Vehiculo::class,
        Poliza::class,
        CompaniaAseguradora::class,
        Siniestro::class,
        GestionSiniestro::class,
        AlertaRevisada::class,
        ConfiguracionOficina::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clienteDao(): ClienteDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun polizaDao(): PolizaDao
    abstract fun companiaDao(): CompaniaDao
    abstract fun siniestroDao(): SiniestroDao
    abstract fun gestionSiniestroDao(): GestionSiniestroDao
    abstract fun alertaRevisadaDao(): AlertaRevisadaDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "seguros_pas_argentina.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial companies and config
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.configuracionDao().saveConfiguracion(DemoDataProvider.getDefaultConfig())
                                database.companiaDao().insertAll(DemoDataProvider.getDemoCompanias())
                                // Preload starter sample data so user immediately has a functional experience
                                database.clienteDao().insertAll(DemoDataProvider.getDemoClientes())
                                database.vehiculoDao().insertAll(DemoDataProvider.getDemoVehiculos())
                                database.polizaDao().insertAll(DemoDataProvider.getDemoPolizas())
                                database.siniestroDao().insertAll(DemoDataProvider.getDemoSiniestros())
                                database.gestionSiniestroDao().insertAll(DemoDataProvider.getDemoGestiones())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
