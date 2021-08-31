package com.zup.pix.cadastra

import com.zup.CadastraChavePixRequest
import com.zup.KeymanagerCadastraServiceGrpc
import com.zup.TipoDeChave
import com.zup.TipoDeConta
import com.zup.client.*
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class CadastraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceBlockingStub
) {


    /*
    * Cadastra chave pix - ok
    * Exception campos inválidos - ok
    * Exception chave já cadastrada - ok
    * UUID do cliente é inválido - ok
    * Exception usuário não existe no client do itáu - ok
    * */


    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }


    companion object {
        val CLIENTE_ID = "5260263c-a3c1-4727-ae32-3bdb2538841b"
    }

    @Test
    fun `deve cadastrar uma chave pix usando cpf`() {

        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        Mockito.`when`(bcbClient.cadastraChavePixBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // Ação
        val response = grpcClient.cadastraChavePix(
            CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.CPF)
                .setValorDaChave("05944255145")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )


        // Verificação
        assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
        assertNotNull(response.pixId)

    }

    @Test
    fun `deve cadastrar uma chave pix usando celular`() {

        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        Mockito.`when`(bcbClient.cadastraChavePixBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // Ação
        val response = grpcClient.cadastraChavePix(
            CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.CELULAR)
                .setValorDaChave("+55985315806")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )


        // Verificação
        assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
        assertNotNull(response.pixId)

    }

    @Test
    fun `deve cadastrar uma chave pix usando e-mail`() {

        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        Mockito.`when`(bcbClient.cadastraChavePixBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // Ação
        val response = grpcClient.cadastraChavePix(
            CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setValorDaChave("felipe@email.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )


        // Verificação
        assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
        assertNotNull(response.pixId)

    }

/*    @Test
    fun `deve cadastrar uma chave pix usando chave aleatoria`() {

        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        Mockito.`when`(bcbClient.cadastraChavePixBcb(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))

        // Ação
        val response = grpcClient.cadastraChavePix(
            CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
                .setValorDaChave("")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )


        // Verificação
        assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
        assertNotNull(response.pixId)

    }*/


    @Test
    fun `deve capturar excecao de cpf invalido, chave pix invalida`() {

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID)
            .setTipoDeChave(TipoDeChave.CPF)
            .setValorDaChave("059-442-551-45")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        // Verificação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("parametros de entrada inválidos", this.status.description)
        }

    }

    @Test
    fun `deve capturar excecao de celular invalido, chave pix invalida`() {

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID)
            .setTipoDeChave(TipoDeChave.CELULAR)
            .setValorDaChave("985315806")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        // Verificação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("parametros de entrada inválidos", this.status.description)
        }

    }

    @Test
    fun `deve capturar excecao de email invalido, chave pix invalida`() {


        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID)
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setValorDaChave("felipeemail.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        // Verificação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("parametros de entrada inválidos", this.status.description)
        }
    }

    @Test
    fun `deve capturar excecao de chave aleatoria preenchida, chave pix invalida`() {

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID)
            .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
            .setValorDaChave("5243-523YY-53452-34XX-5312")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        // Verificação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("parametros de entrada inválidos", this.status.description)
        }

    }

    @Test
    fun `deve capturar excecao de chave pix repetida`() {

        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // Cenário
        val uuidCliente = UUID.fromString(CLIENTE_ID)
        val usuario = ChavePix(uuidCliente, "felipe@email.com", TipoDeChavePix.EMAIL, TipoDeConta.CONTA_CORRENTE)
        repository.save(usuario)

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID)
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setValorDaChave("felipe@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
            assertEquals("Chave pix já cadastrada", this.status.description)
        }

    }

    @Test
    fun `deve capturar excecao de uuid do cliente esta formatado invalido`() {

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId("17849717416713867345-8984386-3892")
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setValorDaChave("felipe@email.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        // Verificação
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            assertEquals("Invalid UUID string: 17849717416713867345-8984386-3892", this.status.description)
        }
    }

    @Test
    fun `deve capturar excecao quando tipo de chave ou tipo de conta for do tipo desconhecido`(){

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.CHAVE_DESCONHECIDA)
                .setValorDaChave("05944255145")
                .setTipoDeConta(TipoDeConta.CONTA_DESCONHECIDO)
                .build()


        val erro = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }


        // Verificação
        with(erro){
            assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
        }

    }

    @Test
    fun `deve capturar excecao de usuario  nao encontrado no client do itau`() {

        Mockito.`when`(itauClient.consultaConta(clienteId = "5260263c-a3c1-4727-ae32-3bdb2538841", tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.noContent())

        // Ação
        val request = CadastraChavePixRequest.newBuilder()
            .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841")
            .setTipoDeChave(TipoDeChave.CELULAR)
            .setValorDaChave("+55985314485")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(request)
        }

        // Verificação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, this.status.code)
            assertEquals("Cliente não encontrado", this.status.description)
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceBlockingStub? {
            return KeymanagerCadastraServiceGrpc.newBlockingStub(channel)
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

    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = PixKeyType.CPF,
            key = "Alguma chave",
            bankAccount = BankAccount("60701190", "0001", "291900", BankAccount.AccountType.CACC),
            owner = Owner(Owner.OwnerType.NATURAL_PERSON, "Nome da pessoa", "05988210933"),
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = PixKeyType.CPF,
            key = "Alguma chave",
            bankAccount = BankAccount("60701190", "0001", "291900", BankAccount.AccountType.CACC),
            owner = Owner(Owner.OwnerType.NATURAL_PERSON, "Nome da pessoa", "05988210933"),
            createdAt = LocalDateTime.now()
        )
    }


}