package com.zup.pix.deleta

import com.zup.DeletaChavePixRequest
import com.zup.DeletaChavePixResponse
import com.zup.KeymanagerDeletaServiceGrpc
import com.zup.client.BcbClient
import com.zup.client.DeletePixKeyRequest
import com.zup.client.ItauClient
import com.zup.compartilhados.annotations.ErrorHandler
import com.zup.compartilhados.annotations.ValidUUID
import com.zup.compartilhados.exceptions.ObjectNotFoundException
import com.zup.pix.cadastra.ChavePixRepository
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@ErrorHandler
@Singleton
class DeletaChavePixEndpoint(
    val repository: ChavePixRepository,
    val itauClient: ItauClient,
    val bcbClient: BcbClient) :
    KeymanagerDeletaServiceGrpc.KeymanagerDeletaServiceImplBase() {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun deletaChavePix(
        request: DeletaChavePixRequest,
        responseObserver: StreamObserver<DeletaChavePixResponse>
    ) {

        val id = UUID.fromString(request.pixId)
        val clienteId = UUID.fromString(request.clienteId)

        request.toFieldValidation(id, clienteId)

        val chavePix = repository.findByIdAndClienteId(id, clienteId)
            .orElseThrow{ObjectNotFoundException("Chave pix não existe ou não é desse cliente")}

        val ispb = itauClient.consultaCliente(clienteId).body().instituicao.ispb

        val deletaChavePixBcbRequest = DeletePixKeyRequest(chavePix.valorDaChave, ispb)
        bcbClient.deletaChavePixBcb(chavePix.valorDaChave, deletaChavePixBcbRequest).also {
            LOGGER.info("Chave pix deletado com sucesso do sistema do Bcb")
        }

        repository.deleteById(id).also {
            LOGGER.info("Chave pix deletada com sucesso")
        }

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