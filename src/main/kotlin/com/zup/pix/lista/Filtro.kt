package com.zup.pix.lista

import com.zup.client.BcbClient
import com.zup.client.ItauClient
import com.zup.compartilhados.annotations.ValidUUID
import com.zup.compartilhados.exceptions.ObjectNotFoundException
import com.zup.pix.cadastra.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    /*
    * Deve retornar chave encontrada ou lançar uma exceção de erro de chave não encontrada
    * */

    abstract fun filtra(repository: ChavePixRepository, itauClient: ItauClient, bcbClient: BcbClient): ChavePixInfo

    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String,
    ) : Filtro() {

        val pixIdAsUuid = UUID.fromString(pixId)
        val clienteIdAsUuid = UUID.fromString(clienteId)

        override fun filtra(
            repository: ChavePixRepository,
            itauClient: ItauClient,
            bcbClient: BcbClient
        ): ChavePixInfo {
/*            return repository.findById(pixIdAsUuid())
                .filter { it.pertenceAo(clienteIdAsUuid()) }
                .map(ChavePixInfo::of)
                .orElseThrow { ObjectNotFoundException("Chave pix não encontrada") }*/

            val chavePix = repository.findByIdAndClienteId(pixIdAsUuid, clienteIdAsUuid)
                .orElseThrow { ObjectNotFoundException("Chave pix não encontrada ou não pertence a esse usuário") }

            val contaInfo = itauClient.consultaConta(clienteIdAsUuid.toString(), chavePix.tipoDeConta.toString()).body()

            return ChavePixInfo.of(chavePix, contaInfo)

        }

    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val valorDaChave: String) : Filtro() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(
            repository: ChavePixRepository,
            itauClient: ItauClient,
            bcbClient: BcbClient
        ): ChavePixInfo {

            val chavePix = repository.findByValorDaChave(valorDaChave)

            if (chavePix.isPresent) {
                val contaInfo = itauClient.consultaConta(
                    chavePix.get().clienteId.toString(),
                    chavePix.get().tipoDeConta.toString()
                )
                    .body()
                return ChavePixInfo.of(chave = chavePix.get(), contaInfo = contaInfo)
            }


            LOGGER.info("Consultando chave Pix no Banco Central do Brasil")
            val response = bcbClient.findByKey(valorDaChave)
            if (response.status != HttpStatus.OK){
                throw ObjectNotFoundException("Chave pix não encontrada")
            }

            return response.body().toModel()
        }
    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(
            repository: ChavePixRepository,
            itauClient: ItauClient,
            bcbClient: BcbClient
        ): ChavePixInfo {
            throw IllegalArgumentException("Chave pix inválida ou não informada")
        }
    }

}
