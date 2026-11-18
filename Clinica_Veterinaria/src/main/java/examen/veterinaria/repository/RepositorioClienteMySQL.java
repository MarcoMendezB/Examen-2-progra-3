package examen.veterinaria.repository;

import examen.veterinaria.model.Cliente;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RepositorioClienteMySQL implements RepositorioCliente {

    private final String url;
    private final String usuario;
    private final String clave;

    public RepositorioClienteMySQL() {
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
    public List<Cliente> cargarTodos() {
        List<Cliente> resultado = new ArrayList<>();
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_listar_clientes()}");
             ResultSet rs = cs.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw fallo("consultar los clientes", e);
        }
    }

    @Override
    public void insertar(Cliente cliente) {
        try (Connection c = conectar();
             CallableStatement cs = c.prepareCall("{CALL sp_insertar_cliente(?,?,?)}")) {
            cs.setString(1, cliente.getCedula());
            cs.setString(2, cliente.getNombre());
            cs.setString(3, cliente.getTelefono());
            cs.executeUpdate();
        } catch (SQLException e) {
            throw fallo("registrar el cliente", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        String cedula   = rs.getString("cedula");
        String nombre   = rs.getString("nombre");
        String telefono = rs.getString("telefono");
        return new Cliente(cedula, nombre, telefono);
    }

    private PersistenciaException fallo(String operacion, SQLException e) {
        e.printStackTrace();
        return new PersistenciaException(
                "No fue posible " + operacion + ". Verifique conexión, permisos y datos.", e);
    }
}

