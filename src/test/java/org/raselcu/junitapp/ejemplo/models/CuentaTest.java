package org.raselcu.junitapp.ejemplo.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import jdk.jfr.Enabled;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.raselcu.junitapp.ejemplo.exceptions.DineroInsuficienteException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaTest {
    //    métodos verifican el comportamiento de la clase Cuenta mediante pruebas unitarias
    Cuenta cuenta;

    private TestInfo testInfo;
    private TestReporter testReporter;

    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        this.cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
        this.testInfo = testInfo;
        this.testReporter = testReporter;

        System.out.println("iniciando el metodo");
        testReporter.publishEntry("ejecutando: " + testInfo.getDisplayName() + "" + testInfo.getTestMethod().orElse(null).getName()
                + " con las etiquetas " + testInfo.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("finalizando el metodo de prueba");
    }

    @BeforeAll
    static void beforeAll() {
//    void beforeAll(){
        System.out.println("inicializando el test");
    }

    @AfterAll
    static void afterAll() {
//    void afterAll(){
        System.out.println("finalizando el test");
    }

    @Tag("cuenta")
    @Nested
    @DisplayName("probando atributos de la cuenta corriente")
    class CuentaTestNombreSaldo {
        @Test
        @DisplayName("probando el nombre!!!")
        void testNombreCuenta() {
            testReporter.publishEntry(testInfo.getTags().toString());
            if (testInfo.getTags().contains("cuenta")) {
                testReporter.publishEntry("hacer algo con la etiqueta cuenta");
            }
            cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
//        cuenta.setPersona("andres");
            String esperado = "andres";
            String real = cuenta.getPersona();

            assertNotNull(real, "la cuenta no puede ser nula");

            assertEquals(esperado, real, () -> "nombre de cuenta no esperado");
            assertTrue(real.equals("andres"), "nombre cuenta esperada debe ser igual al real");

        }

        @Test
        @DisplayName("probando el saldo, no sea null, mayor que cero, valorr esperado")
        void testSaldoCuenta() {
//        cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));

            assertNotNull(cuenta.getSaldo());

            assertEquals(1000.12345, cuenta.getSaldo().doubleValue()); // Verificar que el saldo sea el esperado
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0); // Verificar que el saldo no sea negativo
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("testeando referencias que sean iguales con el metodo equals")
        void testReferentiaCuenta() {
            cuenta = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.9997"));

//        assertNotEquals(cuenta2, cuenta); // compara por referencia, puntero de memoria
            assertEquals(cuenta2, cuenta); // Comparación por valores
        }

    }


    @Nested
    class CuentaOperacionesTest {

        @Tag("cuenta")
        @Test
        void testDebitoCuenta() {
            // verifica la funcionalidad del método debito()
//        Cuenta cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
            cuenta.debito(new BigDecimal(100));

            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Test
        void testCreditoCuenta() {
//        verifica la funcionalidad del método credito()
//        Cuenta cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
            cuenta.credito(new BigDecimal(100));

            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Tag("banco")
        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("andres", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.setNombre("banco del estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }


    @Tag("cuenta")
    @Tag("error")
    @Test
    void testDIneroInsuficienteExceptionCuenta() {
        // Verificar que intentar un saldo  incosistenten, se lanza una excepción DineroInsuficienteException con el mensaje "dinero insuficiente"
//        Cuenta cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito(new BigDecimal(1500));
        });

        String actual = exception.getMessage();
        String esperado = "dinero insuficiente";

        assertEquals(esperado, actual);
    }

// un one to one, una conversacion de uno a uno


    @Tag("cuenta")
    @Tag("banco")
    @Test
//    @Disabled
    @DisplayName("probando relaciones entre las cuentas y el banco con assertAll")
    void testRelacionBancoCuenta() {
//        fail();
        Cuenta cuenta1 = new Cuenta("John Doe", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("andres", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);


        banco.setNombre("banco del estado");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));

        assertAll(() -> {
            assertEquals("1000.8989", cuenta2.getSaldo().toPlainString());
        }, () -> {
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }, () -> {
            assertEquals(2, banco.getCuentas().size());
        }, () -> {
            assertEquals("banco del estado", cuenta1.getBanco().getNombre());
        }, () -> {
            assertEquals("andres", banco.getCuentas().stream()
                    .filter(c -> c.getPersona().equals("andres"))
                    .findFirst()
                    .get().getPersona());
        }, () -> {
            assertTrue(banco.getCuentas().stream()
                    .anyMatch(c -> c.getPersona().equals("John Doe")));
        });
    }

    @Nested
    class SistemaOperativoTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {

        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac() {

        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {

        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void soloJdk8() {

        }

        @Test
        @EnabledOnJre(JRE.JAVA_15)
        void soloJDK15() {

        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void testNoJDK15() {

        }
    }

    @Nested
    class SistemaPropertiesTest {

        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + " :" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "17.0.2")
        void testJavaVersion() {

        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testSolo64() {

        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNo64() {

        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "Pc")
        void testUserName() {

        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testDev() {

        }
    }

    @Nested
    class VariablesAmbienteTest {
        @Test
        void imprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> System.out.println(k + "=" + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = "D:\\Cursos\\Java\\jdk...")
        void testJavaHome() {

        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "8")
        void testProcesadores() {

        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIROMENT", matches = "dev")
        void testEnv() {

        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIROMENT", matches = "prod")
        void testEnvProdDisable() {

        }
    }

    @Test
    @DisplayName("probando assumeTrue")
    void testSaldoCuentaDev() {
//        cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
        boolean esDev = "dev".equals(System.getProperty("ENV"));

        assumeTrue(esDev);
        assertNotNull(cuenta.getSaldo());

        assertEquals(1000.12345, cuenta.getSaldo().doubleValue()); // Verificar que el saldo sea el esperado
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0); // Verificar que el saldo no sea negativo
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("probando assumeTrue 2")
    void testSaldoCuentaDev2() {
//        cuenta = new Cuenta("andres", new BigDecimal("1000.12345"));
        boolean esDev = "dev".equals(System.getProperty("ENV"));

        assumingThat(esDev, () -> {
            assertNotNull(cuenta.getSaldo());

            assertEquals(1000.12345, cuenta.getSaldo().doubleValue()); // Verificar que el saldo sea el esperado
        });
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0); // Verificar que el saldo no sea negativo
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @DisplayName("probando debito cuenta repetir!")
    @RepeatedTest(value = 5, name = "{displayName} repeticion numero {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetir(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("estamos en la repeticion " + info.getCurrentRepetition());
        }
        cuenta.debito(new BigDecimal(100));

        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadasTest {
        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000"})
        void testDebitoCuentaValueSource(String monto) {
            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000"})
        void testDebitoCuentaCsvSource(String index, String monto) {
            System.out.println(index + " -> " + monto);
            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100,John,Andres", "250,200,Pepe,Pepe", "300,300,maria,Maria", "510,500,Pepa,Pepa", "750,700,Lucas,Luca", "1000.12345,1000.12345,Cata,Cata"})
        void testDebitoCuentaCsvSource2(String saldo, String monto, String esperado, String actual) {
            System.out.println(saldo + " -> " + monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }


        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaCsvFileSource(String monto) {
//        System.out.println(index + " -> "+ monto);
            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }


        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data2.csv")
        void testDebitoCuentaCsvFileSource2(String monto) {
//        System.out.println(index + " -> "+ monto);
            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Tag("param")
    @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaMethodSource(String monto) {
//        System.out.println(index + " -> "+ monto);
        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> montoList() {
//        return Arrays.asList("100", "200", "300", "500", "700", "1000", "1000.12345");
        return Arrays.asList("100", "200", "300", "500", "700", "1000");
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeoutTest {
        @Test
        @Timeout(1)
        void pruebaTimeout() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(1000);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeout2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeoutAssertions() {
            assertTimeout(Duration.ofSeconds(5), () -> {
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }

    }


}