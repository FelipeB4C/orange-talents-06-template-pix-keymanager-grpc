package com.zup.pix.cadastra

import com.zup.compartilhados.exceptions.ObjectAlreadyExistsException
import com.zup.compartilhados.exceptions.ObjectNotFoundException
import com.zup.client.ItauClient
import io.grpc.StatusException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class CadastraChavePixService(val repository: ChavePixRepository, val itauClient: ItauClient) {

    val LOGGER = LoggerFactory.getLogger(this.javaClass)

    @Transactional
    fun cadastraChavePix(@Valid chavePix: ChavePixFieldValidation): ChavePix {

        val verificaSeContaExiste = itauClient.verificaCliente(chavePix.clienteId.toString(), chavePix.tipoDeConta.toString())
        if (verificaSeContaExiste.isEmpty ) throw ObjectNotFoundException("Cliente não encontrado")

        val verificaSeChaveJaExiste = repository.findByValorDaChave(chavePix.valorDaChave)
        if (verificaSeChaveJaExiste.isPresent) throw ObjectAlreadyExistsException("Chave pix já cadastrada")

        val chavePix = chavePix.toModel()

        val chavePixSalva = repository.save(chavePix)

        LOGGER.info("Chave pix cadastrada")

        return chavePixSalva
    }

}