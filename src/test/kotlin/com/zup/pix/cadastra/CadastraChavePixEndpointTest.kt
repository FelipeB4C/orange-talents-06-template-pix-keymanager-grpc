package com.zup.pix.cadastra

import com.zup.CadastraChavePixRequest
import com.zup.KeymanagerCadastraServiceGrpc
import com.zup.TipoDeChave
import com.zup.TipoDeConta
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceBlockingStub
) {

/*    @Inject
    lateinit var itauClient: ItauClient*/

    /*
    * Cadastra chave pix - ok
    * Exception campos inválidos - ok
    * Exception chave já cadastrada - ok
    * UUID do cliente é inválido - ok
    * Exception usuário não existe no client do itáu - ok
    * */


    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }


    companion object {
        val CLIENTE_ID = "5260263c-a3c1-4727-ae32-3bdb2538841b"
    }

    @Nested
    inner class CadastrandoChavePix {

        @Test
        fun `deve cadastrar uma chave pix usando cpf`() {

            // Ação
            val response = grpcClient.cadastraChavePix(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID)
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("05944255145")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )


            // Verificação
            assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
            assertNotNull(response.pixId)

        }

        @Test
        fun `deve cadastrar uma chave pix usando celular`() {

            // Ação
            val response = grpcClient.cadastraChavePix(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID)
                    .setTipoDeChave(TipoDeChave.CELULAR)
                    .setChave("+55985315806")
                    .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
                    .build()
            )


            // Verificação
            assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
            assertNotNull(response.pixId)

        }

        @Test
        fun `deve cadastrar uma chave pix usando e-mail`() {

            // Ação
            val response = grpcClient.cadastraChavePix(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID)
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("felipe@email.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )


            // Verificação
            assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
            assertNotNull(response.pixId)

        }

        @Test
        fun `deve cadastrar uma chave pix usando chave aleatoria`() {

            // Ação
            val response = grpcClient.cadastraChavePix(
                CadastraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID)
                    .setTipoDeChave(TipoDeChave.CHAVE_ALEATORIA)
                    .setChave("")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )


            // Verificação
            assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", response.clienteId)
            assertNotNull(response.pixId)

        }

    }

    @Nested
    inner class NaoDeveCadastrar {

        @Test
        fun `deve capturar excecao de cpf invalido, chave pix invalida`() {

            // Ação
            val request = CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.CPF)
                .setChave("059-442-551-45")
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
                .setChave("985315806")
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
                .setChave("felipeemail.com")
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
                .setChave("5243-523YY-53452-34XX-5312")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
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

            // Cenário
            val uuidCliente = UUID.fromString(CLIENTE_ID)
            val usuario = ChavePix(uuidCliente, "felipe@email.com", TipoDeChavePix.EMAIL, TipoDeConta.CONTA_CORRENTE)
            repository.save(usuario)

            // Ação
            val request = CadastraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("felipe@email.com")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
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
                .setChave("felipe@email.com")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
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
        fun `deve capturar excecao de usuario  nao encontrado no client do itau`() {

            // Ação
            val request = CadastraChavePixRequest.newBuilder()
                .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841")
                .setTipoDeChave(TipoDeChave.CELULAR)
                .setChave("+55985314485")
                .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
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
    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceBlockingStub? {
            return KeymanagerCadastraServiceGrpc.newBlockingStub(channel)
        }
    }

/*    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }*/

}