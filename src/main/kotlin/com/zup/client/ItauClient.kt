package com.zup.client

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("\${itau.contas.url}")
interface ItauClient {

    @Get(value = "/clientes/{clienteId}/contas?tipo={tipo}")
    fun verificaCliente(@PathVariable clienteId: String, @QueryValue tipo: String): Optional<Any>

}