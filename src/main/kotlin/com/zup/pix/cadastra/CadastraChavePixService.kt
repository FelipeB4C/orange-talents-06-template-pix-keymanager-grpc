package com.zup.pix.cadastra

import com.zup.client.BcbClient
import com.zup.client.CreatePixKeyRequest
import com.zup.compartilhados.exceptions.ObjectAlreadyExistsException
import com.zup.compartilhados.exceptions.ObjectNotFoundException
import com.zup.client.ItauClient
import com.zup.compartilhados.annotations.ErrorHandler
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class CadastraChavePixService(
    val repository: ChavePixRepository,
    val itauClient: ItauClient,
    val bcbClient: BcbClient) {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun cadastraChavePix(@Valid chavePix: ChavePixFieldValidation): ChavePix {

        // Verifica se conta existe no sistema do ITAÚ
        val dadosDoClient = itauClient.consultaConta(chavePix.clienteId.toString(), chavePix.tipoDeConta.toString())?.body()
            ?: throw ObjectNotFoundException("Cliente não encontrado")

        // Verifica se já existe uma chave pix com o mesmo valor
        if( repository.findByValorDaChave(chavePix.valorDaChave).isPresent) throw ObjectAlreadyExistsException("Chave pix já cadastrada")

        val chavePix = chavePix.toModel()

        val cadastraChaveBcbRequest = CreatePixKeyRequest.of(chavePix, dadosDoClient)
        val response = bcbClient.cadastraChavePixBcb(cadastraChaveBcbRequest).also {
            LOGGER.info("Chave pix cadastrada no Bcb")
        }

        if (chavePix.tipoDeChave == TipoDeChavePix.CHAVE_ALEATORIA) chavePix.valorDaChave = response.body().key


        val chavePixSalva = repository.save(chavePix).also {
            LOGGER.info("Chave pix cadastrada no sistema interno")
        }

        return chavePixSalva
    }

}