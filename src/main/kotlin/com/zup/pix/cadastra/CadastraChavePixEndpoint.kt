package com.zup.pix.cadastra

import com.zup.*
import com.zup.compartilhados.annotations.ErrorHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CadastraChavePixEndpoint(val cadastraChavePixService: CadastraChavePixService) :
    KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceImplBase() {

    override fun cadastraChavePix(
        request: CadastraChavePixRequest,
        responseObserver: StreamObserver<CadastraChavePixResponse>
    ) {

        val chavePixToModelValidation = request.toModel()

        val chavePixSalva = cadastraChavePixService.cadastraChavePix(chavePixToModelValidation)

        responseObserver.onNext(
            CadastraChavePixResponse.newBuilder()
                .setClienteId(chavePixSalva.clienteId.toString())
                .setPixId(chavePixSalva.id.toString())
                .build()
        )



        responseObserver.onCompleted()

    }

}

fun CadastraChavePixRequest.toModel(): ChavePixFieldValidation {
    val chaveUUID = UUID.fromString(this.clienteId)
    return ChavePixFieldValidation(
        clienteId = chaveUUID,
        valorDaChave = this.chave,
        tipoDeChave = when (tipoDeChave) {
            TipoDeChave.CHAVE_DESCONHECIDA -> null
            else -> TipoDeChavePix.valueOf(tipoDeChave.name)
        },
        tipoDeConta = when (tipoDeConta) {
            TipoDeConta.CONTA_DESCONHECIDO -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}