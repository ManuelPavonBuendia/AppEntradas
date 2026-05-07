package com.dam.gs.appentradas.injection

import com.dam.gs.appentradas.data.repository.TicketRepositoryImpl
import com.dam.gs.appentradas.domain.repository.TicketRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTicketRepository(): TicketRepositoryImpl = TicketRepositoryImpl()

    @Provides
    @Singleton
    fun provideTicketRepositoryInterface(
        impl: TicketRepositoryImpl
    ): TicketRepository = impl
}