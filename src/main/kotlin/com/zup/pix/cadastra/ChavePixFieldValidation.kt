package com.zup.pix.cadastra

import com.zup.TipoDeConta
import com.zup.compartilhados.annotations.ValidPixKey
import com.zup.compartilhados.annotations.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class ChavePixFieldValidation(

    @ValidUUID
    @field:NotNull val clienteId: UUID,
    @field:Size(max = 77) val valorDaChave: String,
    @field:NotNull val tipoDeChave: TipoDeChavePix?,
    @field:NotNull val tipoDeConta: TipoDeConta?,
) {
    fun toModel(): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            valorDaChave = if(this.tipoDeChave == TipoDeChavePix.CHAVE_ALEATORIA) UUID.randomUUID().toString() else this.valorDaChave,
            tipoDeChave = tipoDeChave,
            tipoDeConta = tipoDeConta)
    }

}