package com.zup.pix.listatodos

import com.zup.KeymanagerListaTodasServiceGrpc
import com.zup.ListaTodasChavesPixRequest
import com.zup.TipoDeConta
import com.zup.pix.cadastra.ChavePix
import com.zup.pix.cadastra.ChavePixRepository
import com.zup.pix.cadastra.TipoDeChavePix
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ListaTodasChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaTodasServiceGrpc.KeymanagerListaTodasServiceBlockingStub
) {

    /**
     * Deve litar todas as chaves - ok
     * Nao deve listar quando clienteId estiver nulo ou vazio - ok
     * Deve retornar uma coleção vazia caso o cliente não tenha chave - ok
     */

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    companion object{
        val CLIENTE_ID = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {
        // Cenário
        val chave1 = ChavePix(
            UUID.fromString(CLIENTE_ID),
            "09074274056",
            TipoDeChavePix.CPF,
            TipoDeConta.CONTA_CORRENTE
        )
        val chave2 = ChavePix(
            UUID.fromString(CLIENTE_ID),
            "usuario@email.com",
            TipoDeChavePix.EMAIL,
            TipoDeConta.CONTA_CORRENTE
        )
        repository.saveAll(Arrays.asList(chave1, chave2))

        // Ação
        val request = ListaTodasChavesPixRequest.newBuilder().setClienteId(CLIENTE_ID).build()
        val response = grpcClient.listaTodasChavePix(request)

        // Verificação
        assertEquals(2, response.chavesCount)
        assertEquals(CLIENTE_ID, response.clienteId)

    }

    @Test
    fun `nao deve listar quando clienteId estiver nulo ou vazio`(){
        // Cenario
        val chave1 = ChavePix(
            UUID.fromString(CLIENTE_ID),
            "09074274056",
            TipoDeChavePix.CPF,
            TipoDeConta.CONTA_CORRENTE)

        repository.save(chave1)

        // Ação
        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.listaTodasChavePix(ListaTodasChavesPixRequest.newBuilder().build())
        }

        with(erro){
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Cliente Id não pode ser nulo ou vazio", this.status.description)
        }

    }

    @Test
    fun `deve retornar uma colecao vazia quando cliente id nao tiver chave cadastrada`(){
        // Cenário
            // -> Sem chave pix cadastrada

        // Ação
        val response = grpcClient.listaTodasChavePix(
            ListaTodasChavesPixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .build())

        // Verificação
        assertEquals(0, response.chavesCount)
        assertEquals(response.clienteId, CLIENTE_ID)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaTodasServiceGrpc.KeymanagerListaTodasServiceBlockingStub? {
            return KeymanagerListaTodasServiceGrpc.newBlockingStub(channel)
        }
    }


}