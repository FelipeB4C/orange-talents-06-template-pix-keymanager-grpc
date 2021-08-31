package com.zup.pix.lista

import com.zup.KeymanagerListaServiceGrpc
import com.zup.KeymanagerListaTodasServiceGrpc
import com.zup.ListaChavePixRequest
import com.zup.TipoDeConta
import com.zup.client.*
import com.zup.pix.cadastra.ChavePix
import com.zup.pix.cadastra.ChavePixRepository
import com.zup.pix.cadastra.TipoDeChavePix
import com.zup.pix.listatodos.ListaTodasChavePixEndpointTest
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ListaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerListaServiceGrpc.KeymanagerListaServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    /*
    * Deve listar se pixId e ClientId for passado
    * Deve listar se valorDaChave for passado ao invés do pixId e ClientId
    * Deve listar se valorDaChave for passado e a chave não estiver registrada no sistema interno
    * Não deve listar se os valores da request forem inválidos
    * */

    companion object {
        val CLIENTE_ID = "c56dfef4-7901-44fb-84e2-a2cefb157890"
        var PIX_ID: UUID? = null
    }

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve listar detalhes da por PixId`() {

        Mockito.`when`(itauClient.consultaConta(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // Cenário
        val chave = ChavePix(
            UUID.fromString(CLIENTE_ID),
            "09074274056",
            TipoDeChavePix.CPF,
            TipoDeConta.CONTA_CORRENTE
        )
        PIX_ID = repository.save(chave).id


        // Ação
        val request = ListaChavePixRequest.newBuilder()
            .setPixId(ListaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(PIX_ID.toString())
                .setClienteId(CLIENTE_ID)
                .build())
            .build()

        val response = grpcClient.listaChavePix(request)

        // Verificação
        assertEquals("CPF", response.chave.tipoDeChave.name)
        assertEquals("Yuri Matheus" , response.chave.conta.nomeDoTitular)
        assertEquals("UNIBANCO ITAU SA", response.chave.conta.instituicao)


    }


    @Test
    fun `deve listar se valorDaChave for passado e chave estiver registrada no sistema interno`() {

        Mockito.`when`(itauClient.consultaConta(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // Cenário
        val chave = ChavePix(
            UUID.fromString(CLIENTE_ID),
            "09074274056",
            TipoDeChavePix.CPF,
            TipoDeConta.CONTA_CORRENTE
        )
        PIX_ID = repository.save(chave).id


        // Ação
        val request = ListaChavePixRequest.newBuilder()
            .setValorDaChave("09074274056")
            .build()

        val response = grpcClient.listaChavePix(request)

        // Verificação
        assertEquals("CPF", response.chave.tipoDeChave.name)
        assertEquals("Yuri Matheus" , response.chave.conta.nomeDoTitular)
        assertEquals("UNIBANCO ITAU SA", response.chave.conta.instituicao)

    }

    @Test
    fun  `deve listar se valorDaChave for passado e chave nao estiver registrada no sistema interno mas estar registrada no Bcb`(){

        Mockito.`when`(bcbClient.findByKey("09074274056"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse()))

        // Ação
        val request = ListaChavePixRequest.newBuilder().setValorDaChave("09074274056").build()
        val response = grpcClient.listaChavePix(request)

        // Verificação
        assertEquals("", response.clienteId)
        assertEquals("", response.pixId)
        assertEquals("09074274056", response.chave.valorDaChave)

    }

    @Test
    fun `nao deve listar se pixId e clienteId estiverem em branco`(){

        // Ação
        val request = ListaChavePixRequest.newBuilder().build()

        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.listaChavePix(request)
        }

        // Verificação
        with(erro){
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }

    }

    @Test
    fun `nao deve listar se pixId e clienteId nao forem UUID validos`(){
        // Ação
        val request = ListaChavePixRequest.newBuilder()
            .setPixId(ListaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId("c56dfef4-790144fb84e2-a2cefb157890")
                .setClienteId("c56dfef47901-44fb-84e2a2cefb157890")
                .build()

            )
            .build()

        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.listaChavePix(request)
        }

        // Verificação
        with(erro){
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }
    }

    @Test
    fun `nao deve listar se chave nao for encontrada ou nao for do cliente`(){

        val request = ListaChavePixRequest.newBuilder()
            .setPixId(ListaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
                .setClienteId("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
                .build()
            )
            .build()

        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.listaChavePix(request)
        }

        // Verificação
        with(erro){
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave pix não encontrada ou não pertence a esse usuário", this.status.description)
        }
    }

    @Test
    fun `nao deve listar se a chave nao existe no sistema interno e nem no bcb`(){

        Mockito.`when`(bcbClient.findByKey("09074274056")).thenReturn(HttpResponse.notFound())

        val request = ListaChavePixRequest.newBuilder().setValorDaChave("09074274056").build()

        // Ação
        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.listaChavePix(request)
        }

        // Verificação
        with(erro) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Chave pix não encontrada", this.status.description)
        }

    }



    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerListaServiceGrpc.KeymanagerListaServiceBlockingStub? {
            return KeymanagerListaServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClientMock(): BcbClient {
        return Mockito.mock(BcbClient::class.java)
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

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.CPF,
            key = "09074274056",
            bankAccount = BankAccount(
                participant = "UNIBANCO ITAU SA",
                branch = "0001",
                accountNumber = "291900",
                accountType = BankAccount.AccountType.CACC
            ),
            owner = Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = "Yuri Matheus",
                taxIdNumber = "09074274056"
            ),
            createdAt = LocalDateTime.now()
        )
    }


}