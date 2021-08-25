package com.zup.pix.cadastra

import com.zup.TipoDeConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {

    @Test
    fun `deve validar e aceitar todos os campos preenchido corretamente`(){

        val chavePix = ChavePix(null!!, null!!, null, null)

        assertEquals("usuario@email.com", chavePix.valorDaChave)

    }

}