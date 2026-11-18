package examen.veterinaria.repository;

import examen.veterinaria.model.Especie;
import examen.veterinaria.model.Mascota;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RepositorioMascotaMySQL implements RepositorioMascota {

    private final String url;
    private final String usuario;
    private final String clave;

    public RepositorioMascotaMySQL() {
        this.url     = variable("VET_DB_URL",      "jdbc:mysql://localhost:3306/veterinaria?serverTimezone=UTC");
        this.usuario = variable("VET_DB_USER",     null);
        this.clave   = variable("VET_DB_PASSWORD", null);

        if (usuario == null || usuario.isBlank() || clave == null) {
            throw new PersistenciaException(
                    "Configure VET_DB_USER y VET_DB_PASSWORD como variables de entorno.", null);
        }
    }

    private static String variable(String nombre, String predeterminado) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? predeterminado : valor;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public List<Mascota> cargarTodos() {
        List<Mascota> resultado = new ArrayList<>();
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_listar_mascotas()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw fallo("consultar las mascotas", e);
        }
    }

    @Override
    public void insertar(Mascota mascota) {
        ejecutarGuardar("{CALL sp_insertar_mascota(?,?,?,?,?)}", mascota);
    }

    @Override
    public void actualizar(Mascota mascota) {
        ejecutarGuardar("{CALL sp_actualizar_mascota(?,?,?,?,?)}", mascota);
    }

    private void ejecutarGuardar(String llamada, Mascota m) {
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall(llamada)) {
            llenarParametros(cs, m);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw fallo("guardar la mascota", e);
        }
    }

    @Override
    public void eliminar(String codigo) {
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_eliminar_mascota(?)}")) {
            cs.setString(1, codigo);
            cs.executeUpdate();
        } catch (SQLException e) {
            throw fallo("eliminar la mascota", e);
        }
    }

    @Override
    public List<String> listarConDueno() {
        List<String> resultado = new ArrayList<>();
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_listar_mascotas_con_dueno()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                String codigo        = rs.getString("codigo");
                String nombre        = rs.getString("nombre");
                String especie       = rs.getString("especie");
                int edad             = rs.getInt("edad");
                String cedulaCliente = rs.getString("cedula_cliente");
                String nombreCliente = rs.getString("nombre_cliente");

                resultado.add(String.format(
                        "%s - %s (%s, %d años) | Dueño: %s (%s)",
                        codigo, nombre, especie, edad, nombreCliente, cedulaCliente
                ));
            }
            return resultado;
        } catch (SQLException e) {
            throw fallo("listar las mascotas con su dueño", e);
        }
    }

    private void llenarParametros(CallableStatement cs, Mascota m) throws SQLException {
        cs.setString(1, m.getCodigo());
        cs.setString(2, m.getNombre());
        cs.setString(3, m.getEspecie().name());
        cs.setInt(4, m.getEdad());
        cs.setString(5, m.getCedulaCliente());
    }

    private Mascota mapear(ResultSet rs) throws SQLException {
        String codigo        = rs.getString("codigo");
        String nombre        = rs.getString("nombre");
        Especie especie      = Especie.valueOf(rs.getString("especie"));
        int edad             = rs.getInt("edad");
        String cedulaCliente = rs.getString("cedula_cliente");
        return new Mascota(codigo, nombre, especie, edad, cedulaCliente);
    }

    private PersistenciaException fallo(String operacion, SQLException e) {
        e.printStackTrace();
        return new PersistenciaException(
                "No fue posible " + operacion + ". Verifique conexión, permisos y datos.", e);
    }
}