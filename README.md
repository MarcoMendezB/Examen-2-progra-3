# Clínica Veterinaria — Persistencia Segura con Java, JDBC y MySQL

Migración de la capa de persistencia de archivos planos (`.txt`) hacia **MySQL** utilizando arquitectura en capas (`View → Controller → Service → Repository → JDBC`), procedimientos almacenados (`CallableStatement`) y control de acceso basado en privilegios mínimos.

## 1. Modelo Relacional y Normalización

La base de datos `veterinaria` separa los datos en dos entidades normalizadas con relación **1:N** (un cliente puede registrar múltiples mascotas; cada mascota pertenece a un único cliente):

- **`cliente`**: almacena los datos del propietario (`cedula` `VARCHAR(20)` como **Clave Primaria**, `nombre`, `telefono`). Evita duplicar información del dueño en cada registro de mascota.
- **`mascota`**: almacena los datos del animal (`codigo` `VARCHAR(15)` como **Clave Primaria**, `nombre`, `especie` `ENUM('PERRO','GATO','OTRO')`, `edad`, y `cedula_cliente` como **Clave Foránea** hacia `cliente(cedula)`).
- **Integridad referencial y de dominio**:
  - `fk_mascota_cliente` aplica `ON DELETE RESTRICT`, impidiendo eliminar un cliente si tiene mascotas asociadas y bloqueando el registro de mascotas con dueños inexistentes.
  - Restricciones `CHECK` garantizan cadenas no vacías y rango válido de edad (`18` a `60`).

## 2. Ejecución del Script SQL

1. Abrir **MySQL Workbench** conectado con una cuenta administradora (`root`).
2. Abrir el archivo `database/01_veterinaria.sql`.
3. En la **Sección 2**, reemplazar las contraseñas de ejemplo de `'vet_definer'@'localhost'` y `'vet_app'@'localhost'` por claves locales seguras antes de ejecutar.
4. Ejecutar todo el script (`Ctrl + Shift + Enter`) y refrescar el panel *Schemas* para verificar las tablas `cliente`, `mascota` y los 7 procedimientos almacenados (`sp_*`).

## 3. Configuración de Variables de Entorno y Ejecución

La aplicación no almacena credenciales en el código fuente. Configure las siguientes variables en IntelliJ IDEA (**Run → Edit Configurations → Environment variables**):

- `VET_DB_URL=jdbc:mysql://localhost:3306/veterinaria?serverTimezone=UTC`
- `VET_DB_USER=vet_app`
- `VET_DB_PASSWORD=<contraseña_local_de_vet_app>`

Ejecute la clase principal `examen.veterinaria.Main`.

## 4. Pruebas de Seguridad e Integridad

1. **Acceso controlado mediante procedimientos almacenados (Permitido):**
   Conectado en Workbench como `vet_app`, ejecutar:
   ```sql
   CALL veterinaria.sp_listar_mascotas(); 
