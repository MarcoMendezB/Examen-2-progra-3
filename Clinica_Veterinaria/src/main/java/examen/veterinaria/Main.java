package examen.veterinaria;
import examen.veterinaria.controller.VeterinariaController;
import examen.veterinaria.repository.*;
import examen.veterinaria.service.VeterinariaService;
import examen.veterinaria.view.VentanaVeterinaria;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RepositorioCliente repoCliente = new RepositorioClienteMySQL();
            RepositorioMascota repoMascota = new RepositorioMascotaMySQL();
            VeterinariaService service = new VeterinariaService(repoCliente,repoMascota);
            new VentanaVeterinaria(new VeterinariaController(service)).setVisible(true);
        });
    }
}
