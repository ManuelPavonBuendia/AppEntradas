package com.dam.gs.appentradas.injection

import android.content.Context
import androidx.room.Room
import com.dam.gs.appentradas.core.constants.AppConstants
import com.dam.gs.appentradas.data.local.AppDatabase
import com.dam.gs.appentradas.data.local.dao.EntradaDao
import com.dam.gs.appentradas.data.local.dao.EventoDao
import com.dam.gs.appentradas.data.local.dao.SesionDao
import com.dam.gs.appentradas.data.repository.TicketRepositoryImpl
import com.dam.gs.appentradas.domain.repository.TicketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val passphrase = SQLiteDatabase.getBytes(AppConstants.DB_ROOM_KEY.toCharArray())
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppConstants.DB_ROOM_NAME
        )
            .openHelperFactory(factory)
            .build()
    }

    @Provides
    @Singleton
    fun provideEntradaDao(db: AppDatabase): EntradaDao = db.entradaDao()

    @Provides
    @Singleton
    fun provideEventoDao(db: AppDatabase): EventoDao = db.eventoDao()

    @Provides
    @Singleton
    fun provideSesionDao(db: AppDatabase): SesionDao = db.sesionDao()

    @Provides
    @Singleton
    fun provideTicketRepository(impl: TicketRepositoryImpl): TicketRepository = impl

    @Provides
    @Singleton
    fun provideTicketRepositoryImpl(
        entradaDao: EntradaDao,
        eventoDao: EventoDao,
        sesionDao: SesionDao,
        @ApplicationContext context: Context
    ): TicketRepositoryImpl = TicketRepositoryImpl(entradaDao, eventoDao, sesionDao, context)

}