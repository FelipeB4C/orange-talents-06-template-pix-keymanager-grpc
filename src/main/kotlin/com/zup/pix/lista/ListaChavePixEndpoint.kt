package com.zup.pix.lista

import com.zup.KeymanagerListaServiceGrpc
import com.zup.ListaChavePixRequest
import com.zup.ListaChavePixRequest.FiltroCase.*
import com.zup.ListaChavePixResponse
import com.zup.client.BcbClient
import com.zup.client.ItauClient
import com.zup.compartilhados.annotations.ErrorHandler
import com.zup.pix.cadastra.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@ErrorHandler
@Singleton
class ListaChavePixEndpoint(
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BcbClient,
    private val validator: Validator
) : KeymanagerListaServiceGrpc.KeymanagerListaServiceImplBase() {

    override fun listaChavePix(
        request: ListaChavePixRequest,
        responseObserver: StreamObserver<ListaChavePixResponse>
    ) {

        val filtro = request.toModel(validator)

        val chaveInfo = filtro.filtra(repository = repository, itauClient = itauClient,bcbClient = bcbClient)

        responseObserver.onNext(ListaChavePixResponseConverter().convert(chaveInfo))
        responseObserver.onCompleted()

    }

}

fun ListaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when(filtroCase) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        VALORDACHAVE -> Filtro.PorChave(valorDaChave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }
    return filtro
}