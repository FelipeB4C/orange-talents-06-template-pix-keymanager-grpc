package com.zup.pix.cadastra

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, UUID> {

    fun findByValorDaChave(valorDaChave: String): Optional<ChavePix>

    fun findByIdAndClienteId(id: UUID, clienteId: UUID): Optional<ChavePix>

    fun findByClienteId(clienteId: UUID): List<ChavePix>

}