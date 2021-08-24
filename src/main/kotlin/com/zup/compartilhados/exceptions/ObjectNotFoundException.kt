package com.zup.compartilhados.exceptions

import java.lang.RuntimeException

class ObjectNotFoundException(error: String): RuntimeException(error) {
}