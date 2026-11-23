package com.example.alarmacomunitariazaror.viewmodel

import com.example.alarmacomunitariazaror.data.repository.ResultadoAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val repo = FakeRepositorio()
    private val vm = AuthViewModel(repo)

    private fun registroValido() {
        vm.onRegNombre("Ana Perez"); vm.onRegCorreo("ana@demo.cl")
        vm.onRegClave("secreta1"); vm.onRegConfirmar("secreta1")
        vm.onRegDireccion("Calle 123"); vm.onRegTerminos(true)
    }
    private fun loginValido() { vm.onLoginCorreo("ana@demo.cl"); vm.onLoginClave("secreta1") }

    @Test fun `registro invalido informa todos los campos y no consulta datos`() = runTest {
        vm.registrar(); advanceUntilIdle()
        val errores = vm.registro.value.errores
        assertTrue(errores.hayErrores()); assertNotNull(errores.nombre)
        assertNotNull(errores.correo); assertNotNull(errores.clave)
        assertNotNull(errores.confirmar); assertNotNull(errores.direccion); assertNotNull(errores.terminos)
        assertEquals(0, repo.registros)
        registroValido()
        assertFalse(vm.registro.value.errores.hayErrores())
    }
    @Test fun `registro exitoso pasa por cargando y se limpia despues de navegar`() = runTest {
        registroValido(); repo.demora = 100
        vm.registrar(); runCurrent(); assertTrue(vm.registro.value.cargando)
        advanceUntilIdle()
        assertFalse(vm.registro.value.cargando); assertTrue(vm.registro.value.registroExitoso)
        assertEquals(1, repo.registros); vm.registroConsumido()
        assertEquals("", vm.registro.value.correo); assertFalse(vm.registro.value.registroExitoso)
    }
    @Test fun `correo duplicado se presenta como error del campo`() = runTest {
        registroValido(); repo.auth = ResultadoAuth.CorreoDuplicado
        vm.registrar(); advanceUntilIdle()
        assertNotNull(vm.registro.value.errores.correo); assertFalse(vm.registro.value.registroExitoso)
    }
    @Test fun `registro con error remoto termina la carga y muestra el mensaje`() = runTest {
        registroValido(); repo.auth = ResultadoAuth.Error("Servicio no disponible")
        vm.registrar(); advanceUntilIdle()
        assertEquals("Servicio no disponible", vm.registro.value.errorGeneral)
        assertFalse(vm.registro.value.cargando)
    }
    @Test fun `registro con credenciales invalidas no marca exito`() = runTest {
        registroValido(); repo.auth = ResultadoAuth.CredencialesInvalidas
        vm.registrar(); advanceUntilIdle()
        assertFalse(vm.registro.value.cargando); assertFalse(vm.registro.value.registroExitoso)
    }
    @Test fun `login vacio no consulta y editar limpia los errores`() = runTest {
        vm.iniciarSesion(); advanceUntilIdle()
        assertNotNull(vm.login.value.errorCorreo); assertNotNull(vm.login.value.errorClave)
        assertEquals(0, repo.logins); loginValido()
        assertNull(vm.login.value.errorCorreo); assertNull(vm.login.value.errorClave)
    }
    @Test fun `login conserva usuario y cerrar sesion lo elimina`() = runTest {
        loginValido(); repo.demora = 100
        vm.iniciarSesion(); runCurrent(); assertTrue(vm.login.value.cargando)
        advanceUntilIdle()
        assertEquals(repo.usuario, vm.usuario.value); assertTrue(vm.login.value.sesionIniciada)
        vm.loginConsumido(); assertFalse(vm.login.value.sesionIniciada)
        assertEquals(repo.usuario, vm.usuario.value)
        vm.cerrarSesion(); assertNull(vm.usuario.value); assertEquals("", vm.login.value.correo)
    }
    @Test fun `login incorrecto no crea sesion`() = runTest {
        loginValido(); repo.auth = ResultadoAuth.CredencialesInvalidas
        vm.iniciarSesion(); advanceUntilIdle()
        assertNotNull(vm.login.value.errorGeneral); assertNull(vm.usuario.value)
        vm.onLoginClave("otra1"); assertNull(vm.login.value.errorGeneral)
    }
    @Test fun `login comunica el error del repositorio`() = runTest {
        loginValido(); repo.auth = ResultadoAuth.Error("Sin cuenta local")
        vm.iniciarSesion(); advanceUntilIdle()
        assertEquals("Sin cuenta local", vm.login.value.errorGeneral); assertFalse(vm.login.value.cargando)
    }
}
