package com.zup.compartilhados.exceptions

import java.lang.RuntimeException

class ObjectAlreadyExistsException(error: String): RuntimeException(error) {
}