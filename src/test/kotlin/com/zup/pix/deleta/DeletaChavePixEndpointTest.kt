package com.zup.pix.deleta

import com.zup.DeletaChavePixRequest
import com.zup.KeymanagerDeletaServiceGrpc
import com.zup.TipoDeConta
import com.zup.client.*
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
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DeletaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerDeletaServiceGrpc.KeymanagerDeletaServiceBlockingStub
) {

    /*
    * Deve deletar uma chave pix - ok
    * Não deve deletar se o pixId não for do ClientId - ok
    * Não deve deletar uma chave se os campos não forem UUID válidos
    * */

    @Inject
    lateinit var itauClient: ItauClient

    @BeforeEach
    fun criaChavePix1() {
        val uuidCliente = UUID.fromString(CLIENTE_ID1)
        val chavePix = ChavePix(uuidCliente, "usuario@email.com", TipoDeChavePix.EMAIL, TipoDeConta.CONTA_CORRENTE)
        val pixId = repository.save(chavePix)
        PIX_ID1 = pixId.id
    }

    @BeforeEach
    fun criaChavePix2() {
        val uuidCliente = UUID.fromString(CLIENTE_ID2)
        val chavePix = ChavePix(uuidCliente, "usuario2@email.com", TipoDeChavePix.EMAIL, TipoDeConta.CONTA_CORRENTE)
        val pixId = repository.save(chavePix)
        PIX_ID2 = pixId.id
    }

    @AfterEach
    fun setup() {
        repository.deleteAll()
    }

    companion object {
        var CLIENTE_ID1 = "c56dfef4-7901-44fb-84e2-a2cefb157890"
        var CLIENTE_ID2 = "5260263c-a3c1-4727-ae32-3bdb2538841b"
        var PIX_ID1: UUID? = null
        var PIX_ID2: UUID? = null
    }

    @Test
    fun `deve deletar uma chave pix`() {

        Mockito.`when`(itauClient.consultaCliente(clienteId = UUID.fromString(CLIENTE_ID1)))
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        // Cenário
            // -> Chaves pix cadastradas anteriormente pelos métodos criaChavePix1 e criaChavePix2

        // Ação
        val request = DeletaChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID1)
            .setPixId(PIX_ID1.toString())
            .build()

        val response = grpcClient.deletaChavePix(request)

        // Verificação
        assertEquals("Chave deletada com sucesso do sistema interno", response.message)
    }

    @Test
    fun `nao deve deletar se o pixId não for do ClientId`() {

        Mockito.`when`(itauClient.consultaCliente(clienteId = UUID.fromString(CLIENTE_ID1)))
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        // Cenário
            // -> Chaves pix cadastradas anteriormente pelos métodos criaChavePix1 e criaChavePix2

        // Ação
        val request = DeletaChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID1)
            .setPixId(PIX_ID2.toString())
            .build()

        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(request)
        }

        with(erro){
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave pix não existe ou não é desse cliente", this.status.description)
        }
    }

    @Test
    fun `nao deve deletar se o pixId e-ou clientId nao forem UUID validos`(){
        // Cenário
            // -> Chaves pix cadastradas anteriormente pelos métodos criaChavePix1 e criaChavePix2

        // Ação
        val request = DeletaChavePixRequest.newBuilder()
            .setClienteId("c56dfef4790144fb-84e2-a2cefb157890")
            .setPixId("ce1440e8-8521-43a0a67a262d9a65212d")
            .build()

        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.deletaChavePix(request)
        }

        with(erro) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Invalid UUID string: ce1440e8-8521-43a0a67a262d9a65212d", this.status.description)
        }

    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerDeletaServiceGrpc.KeymanagerDeletaServiceBlockingStub? {
            return KeymanagerDeletaServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = TipoDeConta.CONTA_CORRENTE,
            instituicao = Instituicao("UNIBANCO ITAU SA", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = Titular("Yuri Matheus", "86135457004")
        )
    }

    private fun dadosDoClienteResponse(): DadosDoClienteResponse {
        return DadosDoClienteResponse(
            id = UUID.fromString(CLIENTE_ID1),
            nome = "Nome do usuário",
            cpf = "86135457004",
            instituicao = Instituicao("UNIBANCO ITAU SA", "60701190"),
        )
    }

}