package com.zup.compartilhados.exceptions

import com.google.rpc.BadRequest
import com.zup.compartilhados.annotations.ErrorHandler
import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.netty.DefaultHttpClient
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@InterceptorBean(ErrorHandler::class)
@Singleton
class ExceptionHandlerInterceptor: MethodInterceptor<BindableService, Any?> {

    private val LOGGER = LoggerFactory.getLogger(this.javaClass)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {
        LOGGER.info("Interceptando o metodo: ${context.targetMethod}")
        try {
            return context.proceed() // invocar o metodo intereptado: cadastra(request, response)
        } catch (e: Exception) {

            LOGGER.warn("Error: ${e.message}")

            // e.printStackTrace()

            /**
             * RuntimeException -> ALREADY_EXISTS
             * ConstraintViolationException -> INVALID_ARGUMENT + detalhes
             * IllegalArgumentException -> INVALID_ARGUMENT
             * IllegalStateException -> FAILED_PRECONDITION
             */
            val statusError = when(e) {
                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message).asRuntimeException()
                is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message).asRuntimeException()
                is ObjectNotFoundException -> Status.NOT_FOUND.withDescription(e.message).asRuntimeException()
                is ObjectAlreadyExistsException -> Status.ALREADY_EXISTS.withDescription(e.message).asRuntimeException()
             //   is HttpClientResponseException -> Status.UNKNOWN.withDescription(e.message).asRuntimeException()
                is ConstraintViolationException -> handleConstraintViolationException(e)
                else -> Status.UNKNOWN.withDescription("erro inesperado").asRuntimeException()
            }

            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            responseObserver.onError(statusError)
            return null
        }
    }

    private fun handleConstraintViolationException(
        e: ConstraintViolationException
    ): StatusRuntimeException {

        val violations = e.constraintViolations.map {
            BadRequest.FieldViolation.newBuilder()
                .setField(it.propertyPath.last()?.name ?: "Error") // documento: must not be blank
                .setDescription(it.message)
                .build()
        }

        val details = BadRequest.newBuilder()
            .addAllFieldViolations(violations)
            .build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(Status.INVALID_ARGUMENT.code.value())
            .setMessage("parametros de entrada inválidos")
            .addDetails(com.google.protobuf.Any.pack(details)) // array de bytes
            .build()

        LOGGER.error("statusProto = $statusProto" )

        val exception = StatusProto.toStatusRuntimeException(statusProto)
        return exception
    }
}