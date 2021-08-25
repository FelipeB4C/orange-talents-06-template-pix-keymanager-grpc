package com.zup.pix.deleta

import com.zup.DeletaChavePixRequest
import com.zup.DeletaChavePixResponse
import com.zup.KeymanagerDeletaServiceGrpc
import com.zup.client.ItauClient
import com.zup.compartilhados.annotations.ErrorHandler
import com.zup.compartilhados.annotations.ValidUUID
import com.zup.compartilhados.exceptions.ObjectNotFoundException
import com.zup.pix.cadastra.ChavePix
import com.zup.pix.cadastra.ChavePixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@ErrorHandler
@Singleton
class DeletaChavePixEndpoint(val repository: ChavePixRepository, val itauClient: ItauClient) :
    KeymanagerDeletaServiceGrpc.KeymanagerDeletaServiceImplBase() {

    override fun deletaChavePix(
        request: DeletaChavePixRequest,
        responseObserver: StreamObserver<DeletaChavePixResponse>
    ) {

        val id = UUID.fromString(request.pixId)
        val clienteId = UUID.fromString(request.clienteId)

        val validedRequest = request.toFieldValidation(id, clienteId)

        val verificaChavePix = repository.findByIdAndClienteId(id, clienteId)

        if (verificaChavePix.isEmpty) throw ObjectNotFoundException("Chave pix não existe")

        if (verificaChavePix.get().clienteId != clienteId) {
            throw ObjectNotFoundException("Chave pix não encontrada ou não é desse cliente")
        }

        repository.deleteById(id)

        responseObserver.onNext(
            DeletaChavePixResponse.newBuilder()
                .setMessage("Chave deletada com sucesso")
                .build()
        )

        responseObserver.onCompleted()

    }


}

@Validated
@Singleton
fun DeletaChavePixRequest.toFieldValidation(
    @Valid @ValidUUID("id em formato inválido") id: UUID,
    @Valid @ValidUUID("cliente id em formato inválido") clienteId: UUID){
    return
}