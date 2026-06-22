package com.example.NoMasAccidentes.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;


import static org.junit.jupiter.api.Assertions.*;

// .\mvnw.cmd test "-Dtest=CajaNegraWebDriverTest" 

public class CajaNegraWebDriverTest {
    

    WebDriver driver;
    WebDriverWait wait;

    final String BASE_URL = "http://localhost:5173";

    @BeforeEach
    void iniciar() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1366, 768");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void cerrar() {
        if (driver != null) {
            driver.quit();
        }
    }

    void login(String email, String password){
        driver.get(BASE_URL + "/login");
        driver.findElement(By.name("email")).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    void loginAdmin() {
        login("admin@nma.cl", "123456");
        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
    }

    void clicTexto(String texto) {
        By selector = By.xpath("//*[self::button or self::a][contains(normalize-space(), '"+ texto + "')]");
        // Reintenta si React re-renderiza el DOM entre localizar y hacer clic (StaleElementReference)
        for (int intento = 0; intento < 3; intento++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(selector)).click();
                return;
            } catch (StaleElementReferenceException e) {
                // el nodo quedó obsoleto; reintentar
            }
        }
        throw new RuntimeException("No se pudo hacer clic en: " + texto);
    }

    void escribir(String name, String valor){
        // Reintenta si React re-renderiza el DOM tras una navegación (StaleElementReference)
        for (int intento = 0; intento < 3; intento++) {
            try {
                WebElement campo = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
                campo.clear();
                campo.sendKeys(valor);
                return;
            } catch (StaleElementReferenceException e) {
                // el nodo quedó obsoleto; reintentar
            }
        }
        throw new RuntimeException("No se pudo escribir en el campo: " + name);
    }

    void seleccionar(String name, String textoVisible) {
    WebElement campo = driver.findElement(By.name(name));
    new Select(campo).selectByVisibleText(textoVisible);
    }

    void esperarTexto(String texto) {
        wait.until(d -> d.getPageSource().contains(texto));
    }

    // Crea un cliente ACTIVO con datos únicos y devuelve su RUT. Requiere sesión admin en /clientes.
    // Hace el test autosuficiente: no depende de clientes preexistentes ni del orden de ejecución.
    String crearClienteUnico(String etiqueta) {
        long unico = System.currentTimeMillis();
        String rut = (10000000L + unico % 80000000L) + "-0";
        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa " + etiqueta + " SpA");
        escribir("rut", rut);
        escribir("nombreContacto", "Contacto " + etiqueta);
        escribir("email", etiqueta.toLowerCase() + unico + "@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");
        clicTexto("Guardar cliente");
        esperarBotonEnFila(rut, "Suspender");   // confirma que se creó y quedó ACTIVO
        return rut;
    }

    // Hace clic en un botón (Suspender/Reactivar/Eliminar/Editar) dentro de la fila del cliente
    // que tiene ese RUT, en vez del primero de la lista compartida.
    void clicEnFilaPorRut(String rut, String textoBoton) {
        By selector = By.xpath("//tr[td[normalize-space()='" + rut + "']]//button[contains(normalize-space(), '" + textoBoton + "')]");
        for (int intento = 0; intento < 3; intento++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(selector)).click();
                return;
            } catch (StaleElementReferenceException e) {
                // la tabla se re-renderizó; reintentar
            }
        }
        throw new RuntimeException("No se encontró el botón '" + textoBoton + "' en la fila del RUT " + rut);
    }

    // Espera a que la fila del RUT muestre un botón con ese texto (verifica el estado resultante).
    void esperarBotonEnFila(String rut, String textoBoton) {
        By selector = By.xpath("//tr[td[normalize-space()='" + rut + "']]//button[contains(normalize-space(), '" + textoBoton + "')]");
        wait.until(ExpectedConditions.presenceOfElementLocated(selector));
    }

    void captura(String nombre) throws Exception {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Path destino = Path.of("target", "selenium-screenshots", nombre + ".png");
        Files.createDirectories(destino.getParent());
        Files.copy(src.toPath(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void cp01_loginValido() throws Exception {
        login("admin@nma.cl", "123456");

        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
        esperarTexto("Dashboard General");

        captura("CP-01-login-valido");
    }

    @Test
    void cp02_loginPasswordIncorrecta() throws Exception {
        login("admin@nma.cl", "clave-mala");

        esperarTexto("Credenciales incorrectas");
        assertTrue(driver.getCurrentUrl().contains("/login"));

        captura("CP-02-login-password-incorrecta");
    }

    @Test
    void cp03_loginCorreoNoRegistrado() throws Exception {
        login("noexiste@nma.cl", "123456");

        esperarTexto("Credenciales incorrectas");
        assertTrue(driver.getCurrentUrl().contains("/login"));

        captura("CP-03-login-correo-no-registrado");
    }

    @Test
    void cp04_loginCamposObligatorios() throws Exception {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        esperarTexto("El email es obligatorio");
        esperarTexto("obligatoria");
        

        captura("CP-04-login-campos-obligatorios");
    }

    @Test
    void cp05_rutaProtegidaSinSesion() throws Exception {
        driver.get(BASE_URL + "/clientes");

        wait.until(d -> d.getCurrentUrl().contains("/login"));

        
        assertTrue(driver.getCurrentUrl().contains("/login"));
        captura("CP-05-ruta-protegida-sin-sesion");
    }

    @Test
    //Usa el cliente de prueba sembrado en la migración V18 (rol CLIENTE, no-ADMIN).
    void cp06_usuarioSinPermisoRolAdmin() throws Exception {
        login("cliente.test@nma.cl", "123456");

        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));

        driver.get(BASE_URL + "/clientes");

        esperarTexto("No tienes permiso");
        captura("CP-06-usuario-sin-permiso");
    }

    @Test
    void cp07_crearClienteValido() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        clicTexto("+ Nuevo cliente");

        // Datos únicos por ejecución para no chocar con RUT/correo ya existentes.
        // El RUT solo se valida por formato (^\d{7,8}-[\dkK]$), no por dígito verificador.
        long unico = System.currentTimeMillis();
        String rutUnico = (10000000L + unico % 80000000L) + "-0";
        String emailUnico = "selenium.demo" + unico + "@nma.cl";

        escribir("razonSocial", "Empresa Selenium SpA");
        escribir("rut", rutUnico);
        escribir("nombreContacto", "Prueba Selenium");
        escribir("email", emailUnico);
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");

        clicTexto("Guardar cliente");

        esperarTexto("Empresa Selenium SpA");
        captura("CP-07-crear-cliente-valido");
    }

    @Test
    void cp08_validarFormatoRut() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        clicTexto("+ Nuevo cliente");

        escribir("razonSocial", "Empresa Rut Invalido SpA");
        escribir("rut", "761234567");
        escribir("nombreContacto", "Contacto Test");
        escribir("email", "rut.invalido@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");

        clicTexto("Guardar cliente");
        
        esperarTexto("RUT");
        captura("CP-08-validar-formato-rut");
    }

    @Test
    void cp09_crearClienteRutDuplicado() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        // Autosuficiente: crea un cliente y luego intenta crear otro con el MISMO RUT (correo distinto).
        long unico = System.currentTimeMillis();
        String rutComun = (10000000L + unico % 80000000L) + "-0";

        // 1) Cliente base con el RUT
        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa RUT Base SpA");
        escribir("rut", rutComun);
        escribir("nombreContacto", "Contacto Base");
        escribir("email", "rutbase" + unico + "@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");
        clicTexto("Guardar cliente");
        esperarBotonEnFila(rutComun, "Suspender");

        // 2) Segundo cliente con el MISMO RUT → debe rechazarse
        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa RUT Duplicado SpA");
        escribir("rut", rutComun);
        escribir("nombreContacto", "Contacto Duplicado");
        escribir("email", "rutdup" + unico + "@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");
        clicTexto("Guardar cliente");

        esperarTexto("Ya existe un cliente con RUT");
        captura("CP-09-rut-duplicado");
    }

    @Test
    void cp10_crearClienteEmailDuplicado() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        // Autosuficiente: crea un cliente con un correo y luego intenta crear
        // otro con el MISMO correo (RUT distinto), forzando el duplicado.
        long unico = System.currentTimeMillis();
        String correoComun = "duplicado" + unico + "@nma.cl";
        String rut1 = (10000000L + unico % 80000000L) + "-0";
        String rut2 = (10000000L + (unico + 31000000L) % 80000000L) + "-1";

        // 1) Cliente base con el correo
        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa Correo Base SpA");
        escribir("rut", rut1);
        escribir("nombreContacto", "Contacto Base");
        escribir("email", correoComun);
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");
        clicTexto("Guardar cliente");
        esperarBotonEnFila(rut1, "Suspender");

        // 2) Segundo cliente con el MISMO correo → debe rechazarse
        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa Correo Duplicado SpA");
        escribir("rut", rut2);
        escribir("nombreContacto", "Contacto Duplicado");
        escribir("email", correoComun);
        escribir("telefono", "912345678");
        seleccionar("rubro", "Construcción");
        seleccionar("plan", "PRO");
        clicTexto("Guardar cliente");

        esperarTexto("Ya existe un cliente con ese correo");
        captura("CP-10-email-duplicado");
    }

    @Test
    void cp11_verListadoClientesConKpis() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");

        esperarTexto("Activos");
        esperarTexto("Suspendidos");
        esperarTexto("Morosos");
        esperarTexto("Total");
        esperarTexto("Listado de clientes");

        captura("CP-11-listado-clientes-kpis");
    }

    @Test
    void cp12_suspenderClienteActivo() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        // Autosuficiente: crea su propio cliente activo y suspende ESE.
        String rut = crearClienteUnico("Suspender");

        clicEnFilaPorRut(rut, "Suspender");
        clicTexto("Confirmar");

        // El resultado se verifica en la fila: ahora muestra "Reactivar" (quedó suspendido).
        esperarBotonEnFila(rut, "Reactivar");
        captura("CP-12-suspender-cliente");
    }

    @Test
    void cp13_reactivarClienteSuspendido() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        // Autosuficiente: crea su cliente, lo suspende y luego lo reactiva.
        String rut = crearClienteUnico("Reactivar");

        clicEnFilaPorRut(rut, "Suspender");
        clicTexto("Confirmar");
        esperarBotonEnFila(rut, "Reactivar");

        clicEnFilaPorRut(rut, "Reactivar");

        // El resultado se verifica en la fila: vuelve a mostrar "Suspender" (quedó activo).
        esperarBotonEnFila(rut, "Suspender");
        captura("CP-13-reactivar-cliente");
    }

    @Test
    void cp14_eliminarCliente() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        clicTexto("Eliminar");
        clicTexto("Confirmar");

        captura("CP-14-eliminar-cliente");
    }

    @Test
    //Selenium automatiza la creacion de cliente. La revisión del correo se evidencia con captura manual del email recibido.
    void cp15_envioCorreoInvitacion() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        clicTexto("+ Nuevo cliente");

        escribir("razonSocial", "Empresa Invitacion Selenium SpA");
        escribir("rut", "76888777-8");
        escribir("nombreContacto", "Cliente Invitado");
        escribir("email", "n.lavin.loyola+selenium15@gmail.com");
        escribir("telefono", "912345678");
        seleccionar("rubro", "Servicios");
        seleccionar("plan", "PRO");

        clicTexto("Guardar cliente");

        esperarTexto("Empresa Invitacion Selenium SpA");
        captura("CP-15-cliente-creado-correo-invitacion");
    }

    @Test
    //Aqui hay que pegar el enlace recibido por correo en 'URL_INVITACION'
    void cp16_activarCuentaDesdeInvitacion() throws Exception {
        String URL_INVITACION = "http://localhost:5173/restablecer-contrasena?token=2d60ce85-7726-472e-818e-8c3f317a40dd";

        driver.get(URL_INVITACION);

        escribir("nuevaPassword", "!Prueba123");
        escribir("confirmarPassword", "!Prueba123");

        clicTexto("Guardar nueva");

        wait.until(d -> d.getCurrentUrl().contains("/login"));

        login("n.lavin.loyola+selenium15@gmail.com", "!Prueba123");

        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
        captura("CP-16-activar-cuenta-invitacion");
    }

    @Test
    void cp17_solicitarRecuperacionCorreoExistente() throws Exception {
        driver.get(BASE_URL + "/login");

        clicTexto("Olvidaste");

        escribir("email", "n.lavin.loyola@gmail.com");
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");
        captura("CP-17-recuperacion-correo-existente");
    }

    @Test
    //Aqui hay que pegar el enlace recibido en 'URL_RECUPERACION'
    void cp18_restablecerPasswordEnlaceValido() throws Exception {
        String URL_RECUPERACION = "http://localhost:5173/restablecer-contrasena?token=6920d283-4326-4591-b301-a575bec930ff";

        driver.get(URL_RECUPERACION);

        escribir("nuevaPassword", "!Prueba456");
        escribir("confirmarPassword", "!Prueba456");

        clicTexto("Guardar nueva");

        wait.until(d -> d.getCurrentUrl().contains("/login"));
        esperarTexto("actualizada");

        captura("CP-18-restablecer-password");
    }
    
    @Test
    void cp19_enlaceExpiradoSolicitarNuevo() throws Exception {
        String URL_EXPIRADA = BASE_URL + "/restablecer-contrasena?token=token-invalido-selenium";

        driver.get(URL_EXPIRADA);

        esperarTexto("Enlace no");

        clicTexto("Solicitar");

        escribir("email", "n.lavin.loyola@gmail.com");
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");
        captura("CP-19-enlace-expirado-nuevo-enlace");
    }

    @Test
    //Aqui hay que pegar el enlace recibido en 'URL_RECUPERACION'
    void cp20_validarNuevaPassword() throws Exception {
        String URL_RECUPERACION = "http://localhost:5173/restablecer-contrasena?token=6920d283-4326-4591-b301-a575bec930ff";

        driver.get(URL_RECUPERACION);

        escribir("nuevaPassword", "1234");
        escribir("confirmarPassword", "1234");
        clicTexto("Guardar nueva");

        esperarTexto("caracteres");

        escribir("nuevaPassword", "12345678");
        escribir("confirmarPassword", "12345678");
        clicTexto("Guardar nueva");

        esperarTexto("Debe incluir");
        captura("CP-20-validar-nueva-password");
    }

    @Test
    void cp21_recuperacionCorreoNoRegistrado() throws Exception {
        driver.get(BASE_URL + "/login");

        clicTexto("Olvidaste");

        escribir("email", "correo.no.registrado@nma.cl");
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");
        captura("CP-21-recuperacion-correo-no-registrado");
    }
}
