package com.zup.client

import com.zup.TipoDeConta
import com.zup.pix.cadastra.ChavePix
import com.zup.pix.cadastra.TipoDeChavePix
import com.zup.pix.lista.ChavePixInfo
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb.chave.url}")
interface BcbClient {

    @Post(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun cadastraChavePixBcb(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(
        value = "/{key}",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
    )
    fun deletaChavePixBcb(
        @PathVariable key: String,
        @Body request: DeletePixKeyRequest
    ): HttpResponse<DeletePixKeyResponse>


    @Get(
        value = "/{key}",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
    )
    fun findByKey(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>

}


class CreatePixKeyRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {

    companion object {

        fun of(chave: ChavePix, dadosItau: DadosDaContaResponse): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                keyType = PixKeyType.by(chave.tipoDeChave!!),
                key = chave.valorDaChave,
                bankAccount = BankAccount(
                    participant = dadosItau.instituicao.ispb,
                    branch = dadosItau.agencia,
                    accountNumber = dadosItau.numero,
                    accountType = BankAccount.AccountType.by(chave.tipoDeConta!!)
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = dadosItau.titular.nome,
                    taxIdNumber = dadosItau.titular.cpf
                )
            )
        }
    }

}

class CreatePixKeyResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)


class DeletePixKeyRequest(
    val key: String,
    val participant: String
)

class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipoDeChave = keyType.domainType!!,
            valorDaChave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                BankAccount.AccountType.CACC -> TipoDeConta.CONTA_CORRENTE
                BankAccount.AccountType.SVGS -> TipoDeConta.CONTA_POUPANCA
            },
            instituicao = bankAccount.participant,
            nomeDoTitular = owner.name,
            cpfDoTitular = owner.taxIdNumber,
            agencia = bankAccount.branch,
            numeroDaConta = bankAccount.accountNumber
        )
    }

    override fun toString(): String {
        return "PixKeyDetailsResponse(keyType=$keyType, key='$key', bankAccount=$bankAccount, owner=$owner, createdAt=$createdAt)"
    }


}


class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {


    enum class AccountType() {
        CACC,
        SVGS;

        companion object {
            fun by(domainType: TipoDeConta): AccountType {
                return when (domainType) {
                    TipoDeConta.CONTA_CORRENTE -> CACC
                    else -> SVGS
                }
            }
        }
    }

}

class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {


    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }

}

enum class PixKeyType(val domainType: TipoDeChavePix?) {
    CPF(TipoDeChavePix.CPF),
    CNPJ(null),
    PHONE(TipoDeChavePix.CELULAR),
    EMAIL(TipoDeChavePix.EMAIL),
    RANDOM(TipoDeChavePix.CHAVE_ALEATORIA);

    companion object {
        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoDeChavePix): PixKeyType {
            return mapping[domainType] ?: throw IllegalArgumentException("PixKeyType inválida")
        }
    }
}
