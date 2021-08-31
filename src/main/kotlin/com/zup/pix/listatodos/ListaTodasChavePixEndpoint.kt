package com.zup.pix.listatodos

import com.google.protobuf.Timestamp
import com.zup.*
import com.zup.compartilhados.annotations.ErrorHandler
import com.zup.pix.cadastra.ChavePixRepository
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaTodasChavePixEndpoint(val repository: ChavePixRepository): KeymanagerListaTodasServiceGrpc.KeymanagerListaTodasServiceImplBase() {

    override fun listaTodasChavePix(
        request: ListaTodasChavesPixRequest,
        responseObserver: StreamObserver<ListaTodasChavesPixResponse>
    ) {

        if(request.clienteId.isNullOrBlank()) throw IllegalArgumentException("Cliente Id não pode ser nulo ou vazio")

        val chavesPix = repository.findByClienteId(UUID.fromString(request.clienteId)).map {
            ListaTodasChavesPixResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipoDeChave(TipoDeChave.valueOf(it.tipoDeChave!!.name))
                .setValorDaChave(it.valorDaChave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta!!.name))
                .setCriadaEm(it.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(ListaTodasChavesPixResponse.newBuilder()
            .setClienteId(request.clienteId)
            .addAllChaves(chavesPix)
            .build())

        responseObserver.onCompleted()

    }

}
