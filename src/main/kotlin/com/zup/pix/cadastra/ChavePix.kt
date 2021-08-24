package com.zup.pix.cadastra

import com.zup.TipoDeConta
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(

    @Column(unique = true, nullable = false)
    @field:NotNull val clienteId: UUID,

    @Column(unique = true, length = 77, nullable = false)
    @field:NotBlank @field:Size(max = 77) val valorDaChave: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @field:NotNull val tipoDeChave: TipoDeChavePix?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @field:NotNull val tipoDeConta: TipoDeConta?,

    ) {

    @Id
    @GeneratedValue
    val id: UUID? = null


}