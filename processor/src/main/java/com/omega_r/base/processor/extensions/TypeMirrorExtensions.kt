package com.omega_r.base.processor.extensions

import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

fun TypeMirror.asTypeElement(): TypeElement = (this as DeclaredType).asElement() as TypeElement