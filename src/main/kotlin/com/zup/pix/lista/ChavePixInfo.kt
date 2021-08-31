package com.zup.pix.lista

import com.zup.TipoDeConta
import com.zup.client.DadosDaContaResponse
import com.zup.pix.cadastra.ChavePix
import com.zup.pix.cadastra.TipoDeChavePix
import java.time.LocalDateTime
import java.util.*

class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipoDeChave: TipoDeChavePix?,
    val valorDaChave: String,
    val tipoDeConta: TipoDeConta?,
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix, contaInfo: DadosDaContaResponse): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipoDeChave = chave.tipoDeChave,
                valorDaChave = chave.valorDaChave,
                tipoDeConta = chave.tipoDeConta,
                instituicao = contaInfo.instituicao.nome,
                nomeDoTitular = contaInfo.titular.nome,
                cpfDoTitular = contaInfo.titular.cpf,
                agencia = contaInfo.agencia,
                numeroDaConta = contaInfo.numero,
                registradaEm = chave.criadaEm
            )
        }
    }

}
