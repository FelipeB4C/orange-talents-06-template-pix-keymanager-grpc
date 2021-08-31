package com.zup.pix.lista

import com.google.protobuf.Timestamp
import com.zup.ListaChavePixResponse
import com.zup.TipoDeChave
import java.time.ZoneId

class ListaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): ListaChavePixResponse {
        return ListaChavePixResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChave(
                ListaChavePixResponse.ChavePix.newBuilder()
                    .setTipoDeChave(TipoDeChave.valueOf(chaveInfo.tipoDeChave!!.name))
                    .setValorDaChave(chaveInfo.valorDaChave)
                    .setConta(
                        ListaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipoDeConta(chaveInfo.tipoDeConta)
                            .setInstituicao(chaveInfo.instituicao)
                            .setNomeDoTitular(chaveInfo.nomeDoTitular)
                            .setCpfDoTitular(chaveInfo.cpfDoTitular)
                            .setAgencia(chaveInfo.agencia)
                            .setNumeroDaConta(chaveInfo.numeroDaConta)
                            .build()
                    )
                    .setCriadoEm(chaveInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }

}
