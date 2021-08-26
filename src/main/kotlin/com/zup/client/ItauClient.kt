package com.zup.client

import com.zup.TipoDeConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("\${itau.contas.url}")
interface ItauClient {

    @Get(value = "/clientes/{clienteId}/contas?tipo={tipo}")
    fun consultaConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponse>

    @Get("/clientes/{clienteId}")
    fun consultaCliente(@PathVariable clienteId: UUID ): HttpResponse<DadosDoClienteResponse>

}

data class DadosDaContaResponse(
    val tipo: TipoDeConta,
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    val titular: Titular
)

class DadosDoClienteResponse(
    val id: UUID,
    val nome: String,
    val cpf: String,
    val instituicao: Instituicao
) {
    override fun toString(): String {
        return "DadosDoClienteResponse(id=$id, nome='$nome', cpf='$cpf', instituicao=$instituicao)"
    }
}


data class Instituicao(
    val nome: String,
    val ispb: String
)

data class Titular(
    val nome: String,
    val cpf: String
)





/*
id	string($uuid)
nome	string
cpf	string
instituicao	InstituicaoResponse{
    nome	string
            ispb	string
}*/
