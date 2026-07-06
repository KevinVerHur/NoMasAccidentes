package com.example.NoMasAccidentes.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Ejecutar con: mvn clean verify -Pe2e
// Abre Chrome visible y prueba la aplicacion publicada en e2e.base-url/E2E_BASE_URL.
public class CajaNegraWebDriverTest {

    private static final String BASE_URL = normalizarBaseUrl(
            propiedadOEnv("e2e.base-url", "E2E_BASE_URL", "https://nomasaccidentes.duckdns.org"));
    private static final String MAILPIT_URL = normalizarBaseUrl(
            propiedadOEnv("e2e.mailpit-url", "E2E_MAILPIT_URL", "http://localhost:8025"));
    private static final String ADMIN_EMAIL = propiedadOEnv("e2e.admin-email", "E2E_ADMIN_EMAIL", "admin@nma.cl");
    private static final String ADMIN_PASSWORD = propiedadOEnv("e2e.admin-password", "E2E_ADMIN_PASSWORD", "123456");
    private static final boolean HEADLESS = Boolean.parseBoolean(
            propiedadOEnv("e2e.headless", "E2E_HEADLESS", "false"));
    private static final boolean MAILPIT_ENABLED = !"disabled".equalsIgnoreCase(MAILPIT_URL)
            && !"none".equalsIgnoreCase(MAILPIT_URL);

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    WebDriver driver;
    WebDriverWait wait;

    record ClientePrueba(String rut, String email, String razonSocial) {}

    @BeforeEach
    void iniciar() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1366,768");
        if (HEADLESS) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void cerrar() {
        if (driver != null) {
            driver.quit();
        }
    }

    void login(String email, String password) {
        driver.get(BASE_URL + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email"))).sendKeys(email);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
    }

    void loginAdmin() {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
    }

    void cerrarSesionLocal() {
        ((JavascriptExecutor) driver).executeScript("localStorage.removeItem('nma_token');");
    }

    void clicTexto(String texto) {
        By selector = By.xpath("//*[self::button or self::a][contains(normalize-space(), '" + texto + "')]");
        for (int intento = 0; intento < 3; intento++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(selector)).click();
                return;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        throw new RuntimeException("No se pudo hacer clic en: " + texto);
    }

    void escribir(String name, String valor) {
        for (int intento = 0; intento < 3; intento++) {
            try {
                WebElement campo = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
                campo.clear();
                campo.sendKeys(valor);
                return;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        throw new RuntimeException("No se pudo escribir en el campo: " + name);
    }

    void seleccionar(String name, String textoVisible) {
        WebElement campo = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
        new Select(campo).selectByVisibleText(textoVisible);
    }

    void esperarTexto(String texto) {
        wait.until(d -> d.getPageSource().contains(texto));
    }

    String sufijoUnico() {
        return String.valueOf(System.currentTimeMillis());
    }

    String rutUnico(String sufijo) {
        long base = Long.parseLong(sufijo.substring(Math.max(0, sufijo.length() - 7)));
        return (10000000L + base % 80000000L) + "-0";
    }

    ClientePrueba crearClienteDesdeFormulario(String etiqueta) {
        String sufijo = sufijoUnico();
        String rut = rutUnico(sufijo);
        String razonSocial = "Empresa " + etiqueta + " Selenium SpA";
        String email = etiqueta.toLowerCase() + "." + sufijo + "@nma.cl";

        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", razonSocial);
        escribir("rut", rut);
        escribir("nombreContacto", "Contacto " + etiqueta);
        escribir("email", email);
        escribir("telefono", "912345678");
        seleccionar("idRubro", "Construcción");
        clicTexto("Guardar cliente");

        esperarBotonEnFila(rut, "Suspender");
        return new ClientePrueba(rut, email, razonSocial);
    }

    ClientePrueba crearClienteConInvitacion(String etiqueta) throws Exception {
        requiereMailpit();
        limpiarMailpit();

        loginAdmin();
        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        return crearClienteDesdeFormulario(etiqueta);
    }

    ClientePrueba crearYActivarCliente(String etiqueta, String password) throws Exception {
        ClientePrueba cliente = crearClienteConInvitacion(etiqueta);
        String urlInvitacion = leerLinkMailpit(cliente.email(), "Activa tu cuenta");

        driver.get(urlInvitacion);
        escribir("nuevaPassword", password);
        escribir("confirmarPassword", password);
        clicTexto("Guardar nueva");

        wait.until(d -> d.getCurrentUrl().contains("/login"));
        cerrarSesionLocal();

        return cliente;
    }

    void clicEnFilaPorRut(String rut, String textoBoton) {
        By selector = By.xpath("//tr[td[normalize-space()='" + rut + "']]//button[contains(normalize-space(), '" + textoBoton + "')]");
        for (int intento = 0; intento < 3; intento++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(selector)).click();
                return;
            } catch (StaleElementReferenceException ignored) {
            }
        }
        throw new RuntimeException("No se encontró el botón '" + textoBoton + "' en la fila del RUT " + rut);
    }

    void esperarBotonEnFila(String rut, String textoBoton) {
        By selector = By.xpath("//tr[td[normalize-space()='" + rut + "']]//button[contains(normalize-space(), '" + textoBoton + "')]");
        wait.until(ExpectedConditions.presenceOfElementLocated(selector));
    }

    void esperarFilaAusente(String rut) {
        By selector = By.xpath("//tr[td[normalize-space()='" + rut + "']]");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
    }

    void captura(String nombre) throws Exception {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Path destino = Path.of("target", "selenium-screenshots", nombre + ".png");
        Files.createDirectories(destino.getParent());
        Files.copy(src.toPath(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    void limpiarMailpit() throws Exception {
        requiereMailpit();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MAILPIT_URL + "/api/v1/messages"))
                .DELETE()
                .build();

        try {
            http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException ex) {
            Assumptions.abort("Prueba omitida: Mailpit no responde en " + MAILPIT_URL);
        }
    }

    String leerLinkMailpit(String destinatario, String textoAsunto) throws Exception {
        requiereMailpit();
        long fin = System.currentTimeMillis() + 15_000;

        while (System.currentTimeMillis() < fin) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MAILPIT_URL + "/api/v1/messages"))
                    .GET()
                    .build();

            String body;
            try {
                body = http.send(request, HttpResponse.BodyHandlers.ofString()).body();
            } catch (ConnectException ex) {
                Assumptions.abort("Prueba omitida: Mailpit no responde en " + MAILPIT_URL);
                return null;
            }
            JsonNode root = mapper.readTree(body);
            JsonNode mensajes = root.has("messages") ? root.get("messages") : root.get("Messages");

            if (mensajes != null && mensajes.isArray()) {
                for (JsonNode mensaje : mensajes) {
                    String id = texto(mensaje, "ID", "Id", "id");
                    String subject = texto(mensaje, "Subject", "subject");

                    if (id != null
                            && subject != null
                            && subject.contains(textoAsunto)
                            && mensaje.toString().contains(destinatario)) {
                        String detalle = leerMensajeMailpit(id);
                        String link = extraerLinkRestablecimiento(detalle);

                        if (link != null) {
                            return normalizarUrlMailpit(link);
                        }
                    }
                }
            }

            Thread.sleep(500);
        }

        throw new AssertionError("No llegó correo a Mailpit para " + destinatario + " con asunto " + textoAsunto);
    }

    String leerMensajeMailpit(String id) throws Exception {
        requiereMailpit();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MAILPIT_URL + "/api/v1/message/" + id))
                .GET()
                .build();

        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (ConnectException ex) {
            Assumptions.abort("Prueba omitida: Mailpit no responde en " + MAILPIT_URL);
            return "";
        }
    }

    String extraerLinkRestablecimiento(String contenido) {
        var matcher = Pattern
                .compile(Pattern.quote(BASE_URL) + "/restablecer-contrasena\\?token=[a-f0-9\\-]+")
                .matcher(contenido);

        return matcher.find() ? matcher.group() : null;
    }

    static String propiedadOEnv(String propiedad, String env, String valorPorDefecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null || valor.isBlank()) {
            valor = System.getenv(env);
        }
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor.trim();
    }

    static String normalizarBaseUrl(String url) {
        if (url == null) {
            return "";
        }
        String limpia = url.trim();
        while (limpia.endsWith("/")) {
            limpia = limpia.substring(0, limpia.length() - 1);
        }
        return limpia;
    }

    void requiereMailpit() {
        Assumptions.assumeTrue(
                MAILPIT_ENABLED,
                "Prueba omitida: Mailpit esta deshabilitado. Configure -De2e.mailpit-url=http://localhost:8025 para flujos de correo."
        );
    }

    String normalizarUrlMailpit(String url) {
        return url
                .replace("\\u0026", "&")
                .replace("\\/", "/")
                .replace("&amp;", "&");
    }

    String texto(JsonNode node, String... nombres) {
        for (String nombre : nombres) {
            JsonNode valor = node.get(nombre);
            if (valor != null && !valor.isNull()) {
                return valor.asText();
            }
        }
        return null;
    }

    @Test
    void cp01_loginValido() throws Exception {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);

        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
        esperarTexto("Dashboard General");

        captura("CP-01-login-valido");
    }

    @Test
    void cp02_loginPasswordIncorrecta() throws Exception {
        login(ADMIN_EMAIL, "clave-mala");

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
    void cp06_usuarioSinPermisoRolAdmin() throws Exception {
        ClientePrueba cliente = crearYActivarCliente("SinPermiso", "!Cliente123");

        login(cliente.email(), "!Cliente123");
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

        ClientePrueba cliente = crearClienteDesdeFormulario("Valido");

        esperarTexto(cliente.razonSocial());
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
        escribir("email", "rut.invalido." + sufijoUnico() + "@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("idRubro", "Construcción");

        clicTexto("Guardar cliente");

        esperarTexto("RUT");
        captura("CP-08-validar-formato-rut");
    }

    @Test
    void cp09_crearClienteRutDuplicado() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        String sufijo = sufijoUnico();
        String rutComun = rutUnico(sufijo);

        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa RUT Base SpA");
        escribir("rut", rutComun);
        escribir("nombreContacto", "Contacto Base");
        escribir("email", "rutbase" + sufijo + "@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("idRubro", "Construcción");
        clicTexto("Guardar cliente");
        esperarBotonEnFila(rutComun, "Suspender");

        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa RUT Duplicado SpA");
        escribir("rut", rutComun);
        escribir("nombreContacto", "Contacto Duplicado");
        escribir("email", "rutdup" + sufijo + "@nma.cl");
        escribir("telefono", "912345678");
        seleccionar("idRubro", "Construcción");
        clicTexto("Guardar cliente");

        esperarTexto("Ya existe una empresa con RUT");
        captura("CP-09-rut-duplicado");
    }

    @Test
    void cp10_crearClienteEmailDuplicado() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        String sufijo = sufijoUnico();
        String correoComun = "duplicado" + sufijo + "@nma.cl";
        String rut1 = rutUnico(sufijo);
        String rut2 = (20000000L + Long.parseLong(sufijo.substring(Math.max(0, sufijo.length() - 7))) % 70000000L) + "-1";

        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa Correo Base SpA");
        escribir("rut", rut1);
        escribir("nombreContacto", "Contacto Base");
        escribir("email", correoComun);
        escribir("telefono", "912345678");
        seleccionar("idRubro", "Construcción");
        clicTexto("Guardar cliente");
        esperarBotonEnFila(rut1, "Suspender");

        clicTexto("+ Nuevo cliente");
        escribir("razonSocial", "Empresa Correo Duplicado SpA");
        escribir("rut", rut2);
        escribir("nombreContacto", "Contacto Duplicado");
        escribir("email", correoComun);
        escribir("telefono", "912345678");
        seleccionar("idRubro", "Construcción");
        clicTexto("Guardar cliente");

        esperarTexto("Ya existe un usuario con ese correo");
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

        ClientePrueba cliente = crearClienteDesdeFormulario("Suspender");

        clicEnFilaPorRut(cliente.rut(), "Suspender");
        clicTexto("Confirmar");

        esperarBotonEnFila(cliente.rut(), "Reactivar");
        captura("CP-12-suspender-cliente");
    }

    @Test
    void cp13_reactivarClienteSuspendido() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        ClientePrueba cliente = crearClienteDesdeFormulario("Reactivar");

        clicEnFilaPorRut(cliente.rut(), "Suspender");
        clicTexto("Confirmar");
        esperarBotonEnFila(cliente.rut(), "Reactivar");

        clicEnFilaPorRut(cliente.rut(), "Reactivar");

        esperarBotonEnFila(cliente.rut(), "Suspender");
        captura("CP-13-reactivar-cliente");
    }

    @Test
    void cp14_eliminarCliente() throws Exception {
        loginAdmin();

        driver.get(BASE_URL + "/clientes");
        esperarTexto("Clientes");

        ClientePrueba cliente = crearClienteDesdeFormulario("Eliminar");

        clicEnFilaPorRut(cliente.rut(), "Eliminar");
        clicTexto("Confirmar");

        esperarFilaAusente(cliente.rut());
        captura("CP-14-eliminar-cliente");
    }

    @Test
    void cp15_envioCorreoInvitacion() throws Exception {
        ClientePrueba cliente = crearClienteConInvitacion("Invitacion");

        String urlInvitacion = leerLinkMailpit(cliente.email(), "Activa tu cuenta");

        assertTrue(urlInvitacion.contains("/restablecer-contrasena?token="));
        captura("CP-15-cliente-creado-correo-invitacion");
    }

    @Test
    void cp16_activarCuentaDesdeInvitacion() throws Exception {
        ClientePrueba cliente = crearClienteConInvitacion("Activacion");
        String urlInvitacion = leerLinkMailpit(cliente.email(), "Activa tu cuenta");

        driver.get(urlInvitacion);

        escribir("nuevaPassword", "!Prueba123");
        escribir("confirmarPassword", "!Prueba123");
        clicTexto("Guardar nueva");

        wait.until(d -> d.getCurrentUrl().contains("/login"));

        login(cliente.email(), "!Prueba123");

        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));
        captura("CP-16-activar-cuenta-invitacion");
    }

    @Test
    void cp17_solicitarRecuperacionCorreoExistente() throws Exception {
        limpiarMailpit();

        driver.get(BASE_URL + "/login");

        clicTexto("Olvidaste");

        escribir("email", ADMIN_EMAIL);
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");

        String urlRecuperacion = leerLinkMailpit(ADMIN_EMAIL, "Recuperaci");
        assertTrue(urlRecuperacion.contains("/restablecer-contrasena?token="));

        captura("CP-17-recuperacion-correo-existente");
    }

    @Test
    void cp18_restablecerPasswordEnlaceValido() throws Exception {
        String passwordInicial = "!Inicial123";
        String passwordNueva = "!Nueva456";

        ClientePrueba cliente = crearYActivarCliente("ResetValido", passwordInicial);

        limpiarMailpit();

        driver.get(BASE_URL + "/login");
        clicTexto("Olvidaste");

        escribir("email", cliente.email());
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");

        String urlRecuperacion = leerLinkMailpit(cliente.email(), "Recuperaci");

        driver.get(urlRecuperacion);

        escribir("nuevaPassword", passwordNueva);
        escribir("confirmarPassword", passwordNueva);
        clicTexto("Guardar nueva");

        wait.until(d -> d.getCurrentUrl().contains("/login"));
        esperarTexto("actualizada");

        login(cliente.email(), passwordNueva);
        wait.until(d -> d.getCurrentUrl().contains("/dashboard"));

        captura("CP-18-restablecer-password");
    }

    @Test
    void cp19_enlaceExpiradoSolicitarNuevo() throws Exception {
        limpiarMailpit();

        String urlExpirada = BASE_URL + "/restablecer-contrasena?token=token-invalido-selenium";

        driver.get(urlExpirada);

        esperarTexto("Enlace no");

        clicTexto("Solicitar");

        escribir("email", ADMIN_EMAIL);
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");
        captura("CP-19-enlace-expirado-nuevo-enlace");
    }

    @Test
    void cp20_validarNuevaPassword() throws Exception {
        limpiarMailpit();

        driver.get(BASE_URL + "/login");

        clicTexto("Olvidaste");

        escribir("email", ADMIN_EMAIL);
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");

        String urlRecuperacion = leerLinkMailpit(ADMIN_EMAIL, "Recuperaci");

        driver.get(urlRecuperacion);

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
        limpiarMailpit();

        driver.get(BASE_URL + "/login");

        clicTexto("Olvidaste");

        escribir("email", "correo.no.registrado@nma.cl");
        clicTexto("Enviar enlace");

        esperarTexto("Correo enviado");
        captura("CP-21-recuperacion-correo-no-registrado");
    }
}
